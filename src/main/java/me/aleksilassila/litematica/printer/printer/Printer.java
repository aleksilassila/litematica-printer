package me.aleksilassila.litematica.printer.printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.gui.GuiBase;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.interfaces.IClientPlayerInteractionManager;
import me.aleksilassila.litematica.printer.interfaces.Implementation;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class Printer extends PrinterUtils {
    private final MinecraftClient client;
    private final ClientPlayerEntity playerEntity;
    private final ClientWorld clientWorld;
    private WorldSchematic worldSchematic;

	int tick = 0;
	static boolean blockLooks = false;

	public boolean lockCamera = false;

	private boolean shouldPlaceWater;
	private boolean shouldPrintInAir;
	private boolean shouldReplaceFluids;

	public static boolean shouldBlockLookPackets() {
		return blockLooks;
	}

	public static class Queue {
		public static BlockPos neighbor;
		public static Direction side;
		public static Vec3d hitVec;
		public static boolean useShift;
		public static Direction playerShouldBeFacing;
	}

	public Printer(MinecraftClient client, ClientPlayerEntity playerEntity, ClientWorld clientWorld) {
        this.client = client;
        this.playerEntity = playerEntity;
        this.clientWorld = clientWorld;
    }

    public void onTick() {
		tick = tick == 0x7fffffff ? 0 : tick + 1;
//		lockCamera = false;
//		sendQueuedPackets();

		int tickRate = LitematicaMixinMod.PRINT_INTERVAL.getIntegerValue();
//		if (tick % tickRate == tickRate / 2) {
//			sendQueuedLookPacket();
//			return;
//		} else
		if (tick % tickRate != 0) {
			return;
		}

		int range = LitematicaMixinMod.PRINTING_RANGE.getIntegerValue();
//		shouldPlaceWater = LitematicaMixinMod.PRINT_WATER.getBooleanValue();
		shouldPlaceWater = false;
		shouldPrintInAir = LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();
		shouldReplaceFluids = LitematicaMixinMod.REPLACE_FLUIDS.getBooleanValue();
		worldSchematic = SchematicWorldHandler.getSchematicWorld();

		// FIXME if is in range
		sendQueuedPlacement();

		// forEachBlockInRadius:
		for (int y = -range; y < range + 1; y++) {
			for (int x = -range; x < range + 1; x++) {
				for (int z = -range; z < range + 1; z++) {
					BlockPos pos = playerEntity.getBlockPos().north(x).west(z).up(y);

					if (!DataManager.getRenderLayerRange().isPositionWithinRange(pos)) continue;
					if (shouldSkipPosition(pos)) continue;

					if (processBlock(pos)) return;
				}
			}
		}
    }

	/**
	 * @return true if block was placed.
	 */
	public boolean processBlock(BlockPos pos) {
		BlockState currentState = clientWorld.getBlockState(pos);
		BlockState requiredState = worldSchematic.getBlockState(pos);

		// Check if block should be just clicked (repeaters etc.)
		if (shouldClickBlock(currentState, requiredState)) {
			queuePlacement(pos, Direction.UP, Vec3d.ofCenter(pos), null, false);

			return true;
		}

		// FIXME water and lava
		// Check if something should be placed in target block
		if (requiredState.isAir()
				|| requiredState.getMaterial().equals(Material.WATER)
				|| requiredState.getMaterial().equals(Material.LAVA)) return false;

		// Check if target block is empty
		if (!shouldPlaceWater)
			if (!currentState.isAir() && !currentState.contains(FluidBlock.LEVEL)) {
				if (!PrinterUtils.isDoubleSlab(requiredState)) return false;
				else if (PrinterUtils.isDoubleSlab(currentState)) return false;
			} else if (currentState.contains(FluidBlock.LEVEL)) {
				if (currentState.get(FluidBlock.LEVEL) == 0 && !shouldReplaceFluids) return false;
			}
		else {
			if (isWaterLogged(requiredState) && isWaterLogged(currentState)) return false;
			if (!isWaterLogged(requiredState) && !currentState.isAir()) return false;
		}

		// Check if can be placed in world
		if (!requiredState.canPlaceAt(clientWorld, pos)) return false;

		// Check if player is holding right block
		Item itemInHand = Implementation.getInventory(playerEntity).getMainHandStack().getItem();
		if (!itemInHand.equals(requiredItemInHand(requiredState, currentState))) {
			if (Implementation.getAbilities(playerEntity).creativeMode) {
				ItemStack required = new ItemStack(requiredItemInHand(requiredState, currentState));
				BlockEntity te = clientWorld.getBlockEntity(pos);

				// The creative mode pick block with NBT only works correctly
				// if the server world doesn't have a TileEntity in that position.
				// Otherwise it would try to write whatever that TE is into the picked ItemStack.
				if (GuiBase.isCtrlDown() && te != null && clientWorld.isAir(pos))
				{
					ItemUtils.storeTEInStack(required, te);
				}

				InventoryUtils.setPickedItemToHand(required, client);
				client.interactionManager.clickCreativeStack(playerEntity.getStackInHand(Hand.MAIN_HAND),
						36 + Implementation.getInventory(playerEntity).selectedSlot);

			} else {
				int slot = getBlockInventorySlot(requiredItemInHand(requiredState, currentState));

				if (slot == -1) {
					return false;
				}

				swapHandWithSlot(slot);
			}
		}

		return attemptPlacement(pos, requiredState, currentState);
	}

	public boolean shouldSkipPosition(BlockPos pos) {
		BlockState currentState = clientWorld.getBlockState(pos);
		BlockState requiredState = worldSchematic.getBlockState(pos);

		if (shouldClickBlock(currentState, requiredState)) return false;

		// FIXME water and lava
		// Check if something should be placed in target block
		if (requiredState.isAir()
				|| requiredState.getMaterial().equals(Material.WATER)
				|| requiredState.getMaterial().equals(Material.LAVA)) return true;

		// Check if target block is empty
		if (!shouldPlaceWater)
			if (!currentState.isAir() && !currentState.contains(FluidBlock.LEVEL)) {
				if (!PrinterUtils.isDoubleSlab(requiredState)) return true;
				else if (PrinterUtils.isDoubleSlab(currentState)) return true;
			} else if (currentState.contains(FluidBlock.LEVEL)) {
				if (currentState.get(FluidBlock.LEVEL) == 0 && !shouldReplaceFluids) return true;
			}
		else {
			if (isWaterLogged(requiredState) && isWaterLogged(currentState)) return true;
			if (!isWaterLogged(requiredState) && !currentState.isAir()) return true;
		}

		// Check if can be placed in world
		return !requiredState.canPlaceAt(clientWorld, pos);
	}

	private int getBlockInventorySlot(Item item) {
    	Inventory inv = Implementation.getInventory(playerEntity);

    	for (int slot = 0; slot < inv.size(); slot++) {
    		if (inv.getStack(slot).getItem().equals(item)) return slot;
		}

    	return -1;
	}

    private boolean attemptPlacement(BlockPos pos, BlockState requiredState, BlockState currentState) {
		PlacementGuide.Placement placement = PlacementGuide.getPlacement(requiredState);

		if (placement.skip) return false;

		boolean doubleChest = requiredState.contains(ChestBlock.CHEST_TYPE) && requiredState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE;
		Direction side = placement.side == null ? Direction.DOWN : placement.side;
		BlockPos neighbor = pos; // If placing in air, there's no neighbor

		// FIXME now it prints in air all the time
//		if (!shouldPrintInAir) {
//			if (placement.side == null) {
//				if (!canBeClicked(pos.offset(side))) {
//					for (Direction dir : Direction.values()) {
//						if (canBeClicked(pos.offset(side))) {
//
//						}
//					}
//				} else {w
//
//				}
//
//				for (Direction side : Direction.values()) {
//					if (canBeClicked(pos.offset(side))) {
//						addQueuedPacket(pos.offset(side),
//								side,
//								hit,
//								placement.look,
//								!doubleChest);
//						return true;
//					}
//				}
//			} else {
//				if (canBeClicked(pos.offset(placement.side))) {
//					addQueuedPacket(pos.offset(placement.side),
//							placement.side,
//							hit,
//							placement.look,
//							!doubleChest);
//					return true;
//				}
//			}
//		}

		Vec3d hit = Vec3d.ofCenter(pos).add(Vec3d.of(side.getVector()).multiply(0.5));

		if (placement.hitModifier != null) {
			hit = hit.add(placement.hitModifier);
		}

		queuePlacement(neighbor,
				side,
				hit,
				placement.look,
				!doubleChest);
		return true;
	}

    private Item requiredItemInHand(BlockState requiredState, BlockState currentState) {
//		// If block should be waterlogged
//		if (!currentState.isAir() && isWaterLogged(requiredState))
//			return Items.WATER_BUCKET;
//		else if (requiredState.getBlock().equals(Blocks.WATER))
//			return Items.WATER_BUCKET;
//		else
			return new ItemStack(requiredState.getBlock()).getItem();
	}

	private VoxelShape getOutlineShape(BlockPos pos)
	{
		return getState(pos).getOutlineShape(clientWorld, pos);
	}

	private BlockState getState(BlockPos pos)
	{
		return clientWorld.getBlockState(pos);
	}

	private void sendQueuedPlacement() {
		if (Queue.neighbor == null) return;

		boolean wasSneaking = playerEntity.isSneaking();

		if (Queue.useShift && !wasSneaking)
			playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
		else if (!Queue.useShift && wasSneaking)
			playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

		((IClientPlayerInteractionManager) client.interactionManager).rightClickBlock(Queue.neighbor,
				Queue.side.getOpposite(), Queue.hitVec);

		if (Queue.useShift && !wasSneaking)
			playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
		else if (!Queue.useShift && wasSneaking)
			playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

		Queue.neighbor = null;
		blockLooks = false;
	}

	/**
	 * Adds a placement packet to queue
	 * @param neighbor Neighboring block to be clicked
	 * @param side Direction where the neighboring block is
	 * @param hitVec Position where the player would click
	 */
	public void queuePlacement(BlockPos neighbor, Direction side, Vec3d hitVec, Direction playerShouldBeFacing, boolean useShift) {

		// Skip if last packet hasn't been sent yet.
		if (Queue.neighbor != null) return;

		if (playerShouldBeFacing != null) {
			Implementation.sendLookPacket(playerEntity, playerShouldBeFacing);

			blockLooks = true;
		} else {
			blockLooks = false;
		}

		Queue.playerShouldBeFacing = playerShouldBeFacing;
		Queue.neighbor = neighbor;
		Queue.side = side == null ? Direction.DOWN : side;
		Queue.hitVec = hitVec;
		Queue.useShift = useShift;
	}

	private void swapHandWithSlot(int slot) {
		ItemStack stack = Implementation.getInventory(playerEntity).getStack(slot);
		InventoryUtils.setPickedItemToHand(stack, client);
	}

	private boolean canBeClicked(BlockPos pos)
	{
		return getOutlineShape(pos) != VoxelShapes.empty();
	}

}