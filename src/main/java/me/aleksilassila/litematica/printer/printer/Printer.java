package me.aleksilassila.litematica.printer.printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.interfaces.IClientPlayerInteractionManager;
import me.aleksilassila.litematica.printer.interfaces.Implementation;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Printer extends PrinterUtils {
    private static Printer INSTANCE;
    @NotNull
    private final MinecraftClient client;
    @NotNull
    private final ClientPlayerEntity pEntity;
    @NotNull
    private final ClientWorld world;
    private WorldSchematic worldSchematic;
    public final PlacementGuide guide;
    public final Queue queue;

    int tick = 0;

    public static @Nullable Printer init(MinecraftClient client, ClientWorld world, WorldSchematic schematicWorld) {
        if (client == null || client.player == null || client.world == null) {
            return null;
        }

        INSTANCE = new Printer(client);

        return INSTANCE;
    }

    public static @Nullable Printer getPrinter() {
//        if (INSTANCE == null) {
//            INSTANCE = new Printer(client);
//        }

        return INSTANCE;
    }

    private Printer(MinecraftClient client) {
        this.client = client;
        this.pEntity = client.player;
        this.world = client.world;
        this.worldSchematic = SchematicWorldHandler.getSchematicWorld();

        this.guide = new PlacementGuide(client, client.world, this.worldSchematic);
        this.queue = new Queue(this);

        INSTANCE = this;
    }

    /*
    Fixme legit mode:
        - scaffoldings in legit mode
    Fixme air mode:
        - wallmounted levers (and buttons?)
        - doors (hit modifier)
        - trapdoors
    Fixme other:
        - signs
     */

    public void tick() {
        int tickRate = LitematicaMixinMod.PRINT_INTERVAL.getIntegerValue();

        tick = tick == 0x7fffffff ? 0 : tick + 1;
        if (tick % tickRate != 0) {
//            if (!queue.didSendLook) {
                queue.sendQueue();
//            }
            return;
        }

        int range = LitematicaMixinMod.PRINTING_RANGE.getIntegerValue();

        LitematicaMixinMod.shouldPrintInAir = LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();
        LitematicaMixinMod.shouldReplaceFluids = LitematicaMixinMod.REPLACE_FLUIDS.getBooleanValue();

        // forEachBlockInRadius:
        for (int y = -range; y < range + 1; y++) {
            for (int x = -range; x < range + 1; x++) {
                for (int z = -range; z < range + 1; z++) {
                    BlockPos center = pEntity.getBlockPos().north(x).west(z).up(y);
                    BlockState requiredState = worldSchematic.getBlockState(center);
                    PlacementGuide.Action action = guide.getAction(center);

                    if (!DataManager.getRenderLayerRange().isPositionWithinRange(center)) continue;
                    if (action == null) continue;

                    Direction side = action.getValidSide(world, center);
                    if (side == null) continue;

                    Item[] requiredItems = action.getRequiredItems(requiredState.getBlock());
                    if (playerHasAccessToItems(pEntity, requiredItems)) {

                        // Handle shift and chest placement
                        // Won't be required if clickAction
                        boolean useShift = true; // Fixme before ship
                        if (requiredState.contains(ChestBlock.CHEST_TYPE)) {
                            // Left neighbor from player's perspective
                            BlockPos leftNeighbor = center.offset(requiredState.get(ChestBlock.FACING).rotateYClockwise());
                            BlockState leftState = world.getBlockState(leftNeighbor);

                            switch (requiredState.get(ChestBlock.CHEST_TYPE)) {
                                case SINGLE:
                                case RIGHT: {
                                    useShift = true;
                                    break;
                                }
                                case LEFT: {
                                    if (leftState.contains(ChestBlock.CHEST_TYPE) && leftState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
                                        useShift = false;

                                        // Check if it is possible to place without shift
                                        if (Implementation.isInteractable(world.getBlockState(center.offset(side)).getBlock())) {
                                            continue;
                                        }
                                    } else {
                                        continue;
                                    }
                                    break;
                                }
                            }
                        } else if (Implementation.isInteractable(world.getBlockState(center.offset(side)).getBlock())) {
                            useShift = true;
                        }

                        Direction lookDir = action.getLookDirection();
                        sendPlacementPreparation(requiredItems, lookDir);
                        action.queueAction(queue, center, side, useShift, lookDir != null);
                        return;
                    }
                }
            }
        }
    }

    private void sendPlacementPreparation(Item[] requiredItems, Direction lookDir) {
        switchToItems(requiredItems);
        sendLook(lookDir);
    }

    private void switchToItems(Item[] items) {
        if (items == null) return;

        PlayerInventory inv = Implementation.getInventory(pEntity);
//		InventoryUtils.;

        for (Item item : items) {
            if (inv.getMainHandStack().getItem() == item) return;
            if (Implementation.getAbilities(pEntity).creativeMode) {
                InventoryUtils.setPickedItemToHand(new ItemStack(item), client);
                client.interactionManager.clickCreativeStack(client.player.getStackInHand(Hand.MAIN_HAND), 36 + inv.selectedSlot);
                return;
            } else {
                int slot = -1;
                for (int i = 0; i < inv.size(); i++) {
                    if (inv.getStack(i).getItem() == item && inv.getStack(i).getCount() > 0)
                        slot = i;
                }

                if (slot != -1) {
                    swapHandWithSlot(slot);
                    return;
                }
            }
        }
    }

    private void swapHandWithSlot(int slot) {
        ItemStack stack = Implementation.getInventory(pEntity).getStack(slot);
        InventoryUtils.setPickedItemToHand(stack, client);
    }

    public void sendLook(Direction direction) {
        if (direction != null) {
            Implementation.sendLookPacket(client.player, direction);
        }

        queue.lookDir = direction;
    }

    public static class Queue {
        public BlockPos target;
        public Direction side;
        public Vec3d hitModifier;
        public boolean shift = false;
        public boolean didSendLook = true;

        public Direction lookDir = null;

        final Printer printerInstance;
        final ClientPlayerEntity pEntity;

        public Queue(Printer printerInstance) {
            this.printerInstance = printerInstance;
            this.pEntity = printerInstance.pEntity;
        }

        public void queueClick(@NotNull BlockPos target, @NotNull Direction side, @NotNull Vec3d hitModifier) {
            queueClick(target, side, hitModifier, true, true);
        }

        public void queueClick(@NotNull BlockPos target, @NotNull Direction side, @NotNull Vec3d hitModifier, boolean shift, boolean didSendLook) {
            if (this.target != null) {
                System.out.println("Was not ready yet.");
                return;
            }

            this.didSendLook = didSendLook;
            this.target = target;
            this.side = side;
            this.hitModifier = hitModifier;
            this.shift = shift;
        }

        public void sendQueue() {
            if (target == null || side == null || hitModifier == null) return;

            boolean wasSneaking = pEntity.isSneaking();

            Direction direction = side.getAxis() == Direction.Axis.Y ?
                    ((lookDir == null || !lookDir.getAxis().isHorizontal())
                            ? Direction.NORTH : lookDir) : side;

            hitModifier = new Vec3d(hitModifier.z, hitModifier.y, hitModifier.x);
            hitModifier = hitModifier.rotateY((direction.asRotation() + 90) % 360);

            Vec3d hitVec = Vec3d.ofCenter(target)
                    .add(Vec3d.of(side.getVector()).multiply(0.5))
                    .add(hitModifier.multiply(0.5));

            boolean useShift = true; // fixme block tag for clickable blocks

            if (shift && !wasSneaking)
                pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            else if (!shift && wasSneaking)
                pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            ((IClientPlayerInteractionManager) printerInstance.client.interactionManager)
                    .rightClickBlock(target, side, hitVec);

            System.out.println("Right clicked block " + (target.toString()) + ", " + side + ", modifier: " + hitVec);

            if (shift && !wasSneaking)
                pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            else if (!shift && wasSneaking)
                pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

            clearQueue();
        }

        public void clearQueue() {
            this.target = null;
            this.side = null;
            this.hitModifier = null;
            this.lookDir = null;
            this.shift = false;
            this.didSendLook = true;
        }
    }
}