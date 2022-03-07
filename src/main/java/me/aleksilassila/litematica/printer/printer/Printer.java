package me.aleksilassila.litematica.printer.printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.interfaces.IClientPlayerInteractionManager;
import me.aleksilassila.litematica.printer.interfaces.Implementation;
import me.aleksilassila.litematica.printer.mixin.ClientPlayNetworkHandlerMixin;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
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
    private final PlacementGuide guide;
    public final Queue queue;

    int tick = 0;

    private boolean shouldPrintInAir;
    private boolean shouldReplaceFluids;

    public static @Nullable Printer init(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return null;
        }

        if (INSTANCE == null) {
            INSTANCE = new Printer(client);
        }

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

    public void onTick() {
        int tickRate = LitematicaMixinMod.PRINT_INTERVAL.getIntegerValue();

        tick = tick == 0x7fffffff ? 0 : tick + 1;
        if (tick % tickRate != 0) {
            return;
        }

        int range = LitematicaMixinMod.PRINTING_RANGE.getIntegerValue();

        shouldPrintInAir = LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();
        shouldReplaceFluids = LitematicaMixinMod.REPLACE_FLUIDS.getBooleanValue();

        queue.sendQueue();

        // forEachBlockInRadius:
        for (int y = -range; y < range + 1; y++) {
            for (int x = -range; x < range + 1; x++) {
                for (int z = -range; z < range + 1; z++) {
                    /*
                    1. See if should print in this tick, else return
                    2. Empty (send) queue
                    3. For every block:
                        1. Check if in range
                        2. get required possible placement
                        3. get required possible click
                        4. click if necessary and possible
                        5. else place if necessary and possible
                            1. If shouldn't be skipped
                            2. If has no support
                            3. If dealing with liquids
                            4. Special cases, slabs etc.
                        6. Queue above actions and return if queued

                    Placement: Side, hitvec, look, item, (airplacement, skip)
                    Click: What item, (where)
                    Hybrid: Side(s), hitvec, look, item (item, not required = any), crouch, (airplacement, skip)
                    Sides: Map<Direction, hitvec>?
                     */

                    BlockPos center = pEntity.getBlockPos().north(x).west(z).up(y);
                    if (!DataManager.getRenderLayerRange().isPositionWithinRange(center)) continue;

                    PlacementGuide.Action action = guide.getAction(center);
					/*
						if not exist, click: item, where, how
						if exists: maybe click
						if wrong: maybe click
					 */

                    if (action == null) continue;

                    Direction side = shouldPrintInAir ? action.getSide() : getSupportedSide(world, center, action.getSides());
                    if (side == null) continue;

                    Item requiredItem = action.getRequiredItem(worldSchematic.getBlockState(center).getBlock());
                    if (playerHasAccessToItem(pEntity, requiredItem) &&
                            worldSchematic.getBlockState(center).canPlaceAt(world, center)) {
                        switchToItem(requiredItem);
                        sendLook(action.getLookDirection());

                        System.out.println("Queued click?: " + center.offset(side).toString() + ", side: " + side);
                        queue.queueClick(center.offset(side), side.getOpposite(), action.getSides().get(side));

                        return;
                    }
                }
            }
        }
    }

    private void switchToItem(Item item) {
        switchToItems(new Item[]{item});
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

    private VoxelShape getOutlineShape(BlockPos pos) {
        return getState(pos).getOutlineShape(world, pos);
    }

    private BlockState getState(BlockPos pos) {
        return world.getBlockState(pos);
    }

//    private void sendQueuedPlacement() {
//        if (Queue.neighbor == null) return;
//
//        boolean wasSneaking = pEntity.isSneaking();
//
//        if (Queue.useShift && !wasSneaking)
//            pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
//        else if (!Queue.useShift && wasSneaking)
//            pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//
//        ((IClientPlayerInteractionManager) client.interactionManager).rightClickBlock(Queue.neighbor,
//                Queue.side.getOpposite(), Queue.hitVec);
//
//        if (Queue.useShift && !wasSneaking)
//            pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//        else if (!Queue.useShift && wasSneaking)
//            pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
//
//        Queue.neighbor = null;
//        blockLooks = false;
//    }
//
//    private void sendClick(BlockPos neighbor, Vec3d hitVec) {
//        ((IClientPlayerInteractionManager) client.interactionManager).rightClickBlock(neighbor,
//                Direction.UP, hitVec);
//    }

//    /**
//     * Adds a placement packet to queue
//     *
//     * @param neighbor Neighboring block to be clicked
//     * @param side     Direction where the neighboring block is
//     * @param hitVec   Position where the player would click
//     */
//    private void queuePlacement(BlockPos neighbor, Direction side, Vec3d hitVec, Direction playerShouldBeFacing, boolean useShift) {
//
//        // Skip if last packet hasn't been sent yet.
//        if (Queue.neighbor != null) return;
//
//        if (playerShouldBeFacing != null) {
//            Implementation.sendLookPacket(pEntity, playerShouldBeFacing);
//
//            blockLooks = true;
//        } else {
//            blockLooks = false;
//        }
//
//        Queue.playerShouldBeFacing = playerShouldBeFacing;
//        Queue.neighbor = neighbor;
//        Queue.side = side == null ? Direction.DOWN : side;
//        Queue.hitVec = hitVec;
//        Queue.useShift = useShift;
//    }

    private void swapHandWithSlot(int slot) {
        ItemStack stack = Implementation.getInventory(pEntity).getStack(slot);
        InventoryUtils.setPickedItemToHand(stack, client);
    }

    private boolean canBeClicked(BlockPos pos) {
        return getOutlineShape(pos) != VoxelShapes.empty();
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
        public Vec3d hitVec;

        public Direction lookDir = null;

        final Printer printerInstance;
        final ClientPlayerEntity pEntity;

        public Queue(Printer printerInstance) {
            this.printerInstance = printerInstance;
            this.pEntity = printerInstance.pEntity;
        }

        public void queueClick(@NotNull BlockPos target, @NotNull Direction side, @NotNull Vec3d hitVec) {
            if (this.target != null) {
                System.out.println("Was not ready yet.");
                return;
            }

            this.target = target;
            this.side = side;
            this.hitVec = hitVec;
        }

        public void sendQueue() {
            if (target == null || side == null || hitVec == null) return;

            boolean wasSneaking = pEntity.isSneaking();

            hitVec = Vec3d.ofCenter(target).add(hitVec.multiply(0.5));

            boolean useShift = !(printerInstance.worldSchematic.getBlockState(target).contains(ChestBlock.CHEST_TYPE) &&
                    printerInstance.worldSchematic.getBlockState(target).get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE);

            if (useShift && !wasSneaking)
                pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            else if (!useShift && wasSneaking)
                pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            ((IClientPlayerInteractionManager) printerInstance.client.interactionManager)
                    .rightClickBlock(target, side, hitVec);

            System.out.println("Right clicked block " + (target.toString()) + ", " + side + ", modifier: " + hitVec);

            if (useShift && !wasSneaking)
                pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            else if (!useShift && wasSneaking)
                pEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(pEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

            clearQueue();
        }

        public void clearQueue() {
            this.target = null;
            this.side = null;
            this.hitVec = null;
            this.lookDir = null;
        }
    }
}