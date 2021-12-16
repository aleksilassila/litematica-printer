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
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Material;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
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
		int tickRate = LitematicaMixinMod.PRINT_INTERVAL.getIntegerValue();

		tick = tick == 0x7fffffff ? 0 : tick + 1;
		if (tick % tickRate != 0) {
			return;
		}

		int range = LitematicaMixinMod.PRINTING_RANGE.getIntegerValue();
//		shouldPrintInAir = LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();
		shouldReplaceFluids = LitematicaMixinMod.REPLACE_FLUIDS.getBooleanValue();
		worldSchematic = SchematicWorldHandler.getSchematicWorld();

		sendQueuedPlacement();

		// forEachBlockInRadius:
		for (int y = -range; y < range + 1; y++) {
			for (int x = -range; x < range + 1; x++) {
				for (int z = -range; z < range + 1; z++) {
					BlockPos pos = playerEntity.getBlockPos().north(x).west(z).up(y);
					BlockState currentState = clientWorld.getBlockState(pos);
					BlockState requiredState = worldSchematic.getBlockState(pos);

					if (!DataManager.getRenderLayerRange().isPositionWithinRange(pos)) continue;

					PlacementGuide.Placement placement = PlacementGuide.getPlacement(requiredState);
					ClickGuide.Click click = ClickGuide.shouldClickBlock(requiredState, currentState);

					if (click.click && (click.item == null || playerHasAccessToItem(click.item))) {
						sendClick(pos, Vec3d.ofCenter(pos));
						switchToItem(click.item);
						return;
					} else if (shouldPrintHere(pos, placement) && playerHasAccessToItem(requiredState.getBlock().asItem())) {
						boolean doubleChest = requiredState.contains(ChestBlock.CHEST_TYPE) && requiredState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE;
						Direction side = placement.side == null ? Direction.DOWN : placement.side;
						BlockPos neighbor = pos; // If placing in air, there's no neighbor

						Vec3d hit = Vec3d.ofCenter(pos).add(Vec3d.of(side.getVector()).multiply(0.5));

						if (placement.hitModifier != null) {
							hit = hit.add(placement.hitModifier);
						}

						queuePlacement(neighbor,
								side,
								hit,
								placement.look,
								!doubleChest);

						switchToItem(requiredState.getBlock().asItem());
						return;
					}
				}
			}
		}
    }

	private boolean shouldPrintHere(BlockPos position, PlacementGuide.Placement placement) {
		BlockState currentState = clientWorld.getBlockState(position);
		BlockState requiredState = worldSchematic.getBlockState(position);

		if (placement.skip) return false;

		// FIXME water and lava
		// Check if something should be placed in target block
		if (requiredState.isAir()
				|| requiredState.getMaterial().equals(Material.WATER)
				|| requiredState.getMaterial().equals(Material.LAVA)) return false;

		// Check if target block is empty
		if (!currentState.isAir() && !currentState.contains(FluidBlock.LEVEL)) { //current = solid
			// Don't skip unfinished double slabs
			return PrinterUtils.isDoubleSlab(requiredState) && PrinterUtils.isHalfSlab(currentState);
		} else if (currentState.contains(FluidBlock.LEVEL)) { // current = fluid
			return currentState.get(FluidBlock.LEVEL) == 0 && !shouldReplaceFluids;
		}

		// Check if can be placed in world
		return requiredState.canPlaceAt(clientWorld, position);
	}

	private void switchToItem(Item item) {
		PlayerInventory inv = Implementation.getInventory(playerEntity);
		if (inv.getMainHandStack().getItem() == item) return;

		if (Implementation.getAbilities(playerEntity).creativeMode) {
			InventoryUtils.setPickedItemToHand(new ItemStack(item), client);
			client.interactionManager.clickCreativeStack(client.player.getStackInHand(Hand.MAIN_HAND), 36 + inv.selectedSlot);
		} else {
			int slot = 0;
			for (int i = 0; i < inv.size(); i++) {
				if (inv.getStack(i).getItem() == item && inv.getStack(i).getCount() > 0)
					slot = i;
			}

			swapHandWithSlot(slot);
		}
	}

	private boolean playerHasAccessToItem(Item item) {
		if (item == null) return false;
		if (Implementation.getAbilities(playerEntity).creativeMode) return true;
		else {
			Inventory inv = Implementation.getInventory(playerEntity);
			for (int i = 0; i < inv.size(); i++) {
				if (inv.getStack(i).getItem() == item && inv.getStack(i).getCount() > 0)
					return true;
			}
		}

		return false;
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

	private void sendClick(BlockPos neighbor, Vec3d hitVec) {
		((IClientPlayerInteractionManager) client.interactionManager).rightClickBlock(neighbor,
						Direction.UP, hitVec);
	}

	/**
	 * Adds a placement packet to queue
	 * @param neighbor Neighboring block to be clicked
	 * @param side Direction where the neighboring block is
	 * @param hitVec Position where the player would click
	 */
	private void queuePlacement(BlockPos neighbor, Direction side, Vec3d hitVec, Direction playerShouldBeFacing, boolean useShift) {

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