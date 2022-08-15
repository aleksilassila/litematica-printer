package me.aleksilassila.litematica.printer.printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.interfaces.IClientPlayerInteractionManager;
import me.aleksilassila.litematica.printer.interfaces.Implementation;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.Packet;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Printer extends PrinterUtils {
    private static Printer INSTANCE;
    @NotNull
    private final MinecraftClient client;
    public final PlacementGuide guide;
    public final Queue queue;
	public static int tick_1;
	public static int tick_2;

    public static ArrayList<Block> hasFace = new ArrayList<>();
	public static ArrayList<Block> hasNoFace = new ArrayList<>();




	/* int tick = 0; */
	
    public static void init(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        INSTANCE = new Printer(client);

    }

    public static @Nullable Printer getPrinter() {
//        if (INSTANCE == null) {
//            INSTANCE = new Printer(client);
//        }

        return INSTANCE;
    }

    private Printer(@NotNull MinecraftClient client) {
        this.client = client;

        this.guide = new PlacementGuide(client);
        this.queue = new Queue(this);
		/* this.direction = new Direction(); */

        INSTANCE = this;
    }

    /*
    Fixme legit mode:
        - scaffoldings
    Fixme other:
        - signs
        - rotating blocks (signs, skulls)
     */

	
	public void tickNR() {

        ClientPlayerEntity player = client.player;

        int tickRateNR = LitematicaMixinMod.PRINT_INTERVAL_NR.getIntegerValue();

        tick_1 = tick_1 == 0x7fffffff ? 0 : tick_1 + 1;

//      !isArrayEmpty(hasNoFace)
//      tick_1 % tickRateNR == 0

        if (tick_1 % tickRateNR == 0){
            if (!isArrayEmpty(hasNoFace)){
                queue.sendQueue(player);
//                client.inGameHud.getChatHud().addMessage(Text.literal(hasnoface.toString()))
            } else processSides();
        }
    }

    public void tick(){
        ClientPlayerEntity player = client.player;

        int tickRate = LitematicaMixinMod.PRINT_INTERVAL.getIntegerValue();

        tick_2 = tick_2 == 0x7fffffff ? 0 : tick_2 + 1;

        if (tick_2 % tickRate == 0){
            if (!isArrayEmpty(hasFace)){
                queue.sendQueue(player);
//                client.inGameHud.getChatHud().addMessage(Text.literal(hasnoface.toString()))
            } else processSides();

        }
    }


	public void processSides(){
		
		int range = LitematicaMixinMod.PRINTING_RANGE.getIntegerValue();
		
		LitematicaMixinMod.shouldPrintInAir = LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();
        LitematicaMixinMod.shouldReplaceFluids = LitematicaMixinMod.REPLACE_FLUIDS.getBooleanValue();
		
		
		WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
		ClientPlayerEntity player = client.player;
		ClientWorld world = client.world;

		if (worldSchematic == null || player == null || world == null) {
			return;
		}
		
		
		
		for (int y = -range; y < range + 1; y++) {
				for (int x = -range; x < range + 1; x++) {
					for (int z = -range; z < range + 1; z++) {
						BlockPos center = player.getBlockPos().north(x).west(z).up(y);
						BlockState requiredState = worldSchematic.getBlockState(center);
//                        Block requiredBlock = requiredState.getBlock();
						PlacementGuide.Action action = guide.getAction(world, worldSchematic, center);

						if (!DataManager.getRenderLayerRange().isPositionWithinRange(center)) continue;
						if (action == null) continue;

						Direction side = action.getValidSide(world, center);
						
						
						if (side == null) continue;
						
						

						Item[] requiredItems = action.getRequiredItems(requiredState.getBlock());
						if (playerHasAccessToItems(player, requiredItems)) {


							// Handle shift and chest placement
							// Won't be required if clickAction
								
							boolean useShift = false;
							if (requiredState.contains(ChestBlock.CHEST_TYPE)) {
								// Left neighbor from player's perspective
								BlockPos leftNeighbor = center.offset(requiredState.get(ChestBlock.FACING).rotateYClockwise());
								BlockState leftState = world.getBlockState(leftNeighbor);

                                switch (requiredState.get(ChestBlock.CHEST_TYPE)) {
                                    case SINGLE, RIGHT ->
                                        useShift = true;

                                    case LEFT -> { // Actually right
                                        if (leftState.contains(ChestBlock.CHEST_TYPE) && leftState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {

                                            // Check if it is possible to place without shift
                                            if (Implementation.isInteractive(world.getBlockState(center.offset(side)).getBlock())) {
                                                continue;
                                            }
                                        } else {
                                            continue;
                                        }
                                    }
                                }
							} else if (Implementation.isInteractive(world.getBlockState(center.offset(side)).getBlock())) {
								useShift = true;
							}

                            try {
                                if (requiredState.get(Properties.FACING) != null) {
                                    hasFace.add(requiredState.getBlock());
                                }
                            }
                            catch (Exception e){
                                hasNoFace.add(requiredState.getBlock());
                            };

                            Direction lookDir = action.getLookDirection();
                            sendPlacementPreparation(player, requiredItems, lookDir);
                            action.queueAction(queue, center, side, useShift, lookDir != null);
                            return;
						}
					}
				}
			}
	}
	
	
	
	
	
    private void sendPlacementPreparation(ClientPlayerEntity player, Item[] requiredItems, Direction lookDir) {
        switchToItems(player, requiredItems);
        sendLook(player, lookDir);
    }

    private void switchToItems(ClientPlayerEntity player, Item[] items) {
        if (items == null) return;

        PlayerInventory inv = Implementation.getInventory(player);

        for (Item item : items) {
            if (inv.getMainHandStack().getItem() == item) return;
            if (Implementation.getAbilities(player).creativeMode) {
                InventoryUtils.setPickedItemToHand(new ItemStack(item), client);
                assert client.interactionManager != null;
                assert client.player != null;
                client.interactionManager.clickCreativeStack(client.player.getStackInHand(Hand.MAIN_HAND), 36 + inv.selectedSlot);
                return;
            } else {
                int slot = -1;
                for (int i = 0; i < inv.size(); i++) {
                    if (inv.getStack(i).getItem() == item && inv.getStack(i).getCount() > 0)
                        slot = i;
                }

                if (slot != -1) {
                    swapHandWithSlot(player, slot);
                    return;
                }
            }
        }
    }

    private void swapHandWithSlot(ClientPlayerEntity player, int slot) {
        ItemStack stack = Implementation.getInventory(player).getStack(slot);
        InventoryUtils.setPickedItemToHand(stack, client);
    }

    public void sendLook(ClientPlayerEntity player, Direction direction) {
        if (direction != null) {
            Implementation.sendLookPacket(player, direction);
        }

        queue.lookDir = direction;
    }

    public void clearArray(){
        hasFace = new ArrayList<>();
        hasNoFace = new ArrayList<>();
    }

    public boolean isArrayEmpty(ArrayList<Block> array) {
        return array.isEmpty();
    }

    public static class Queue {
        public BlockPos target;
        public Direction side;
        public Vec3d hitModifier;
        public boolean shift = false;
        public boolean didSendLook = true;

        public Direction lookDir = null;

        final Printer printerInstance;

        public Queue(Printer printerInstance) {
            this.printerInstance = printerInstance;
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
		
        public void sendQueue(ClientPlayerEntity player) {
            if (target == null || side == null || hitModifier == null) return;

            boolean wasSneaking = player.isSneaking();

            Direction direction = side.getAxis() == Direction.Axis.Y ?
                    ((lookDir == null || !lookDir.getAxis().isHorizontal())
                            ? Direction.NORTH : lookDir) : side;

            hitModifier = new Vec3d(hitModifier.z, hitModifier.y, hitModifier.x);
            hitModifier = hitModifier.rotateY((direction.asRotation() + 90) % 360);

            Vec3d hitVec = Vec3d.ofCenter(target)
                    .add(Vec3d.of(side.getVector()).multiply(0.5))
                    .add(hitModifier.multiply(0.5));
			
            if (shift && !wasSneaking)
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            else if (!shift && wasSneaking)
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));


            assert printerInstance.client.interactionManager != null;
            ((IClientPlayerInteractionManager) printerInstance.client.interactionManager).rightClickBlock(target, side, hitVec);
			
            System.out.println("Printed at " + (target.toString()) + ", " + side + ", modifier: " + hitVec);

            if (shift && !wasSneaking)
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            else if (!shift && wasSneaking)
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

            for (int i = 0; i<10; i++) clearQueue();

        }

        public void clearQueue() {
            this.target = null;
            this.side = null;
            this.hitModifier = null;
            this.lookDir = null;
            this.shift = false;
            this.didSendLook = true;
            printerInstance.clearArray();
        }

    }
}