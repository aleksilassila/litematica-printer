package fi.dy.masa.litematica.printer;

import fi.dy.masa.litematica.interfaces.IClientPlayerInteractionManager;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.Date;

public class Printer extends PrinterUtils {
    private final MinecraftClient client;
    private final ClientPlayerEntity playerEntity;
    private final ClientWorld clientWorld;

	public long lastPlaced = new Date().getTime();
	public boolean isPlacementComing = false;

	protected boolean sendPlacement = false;

	protected BlockPos pNeighbor;
	protected Direction pSide;
	protected Vec3d pHitVec;
	protected boolean pNeedsShift;

	public Printer(MinecraftClient client, ClientPlayerEntity playerEntity, ClientWorld clientWorld) {
        this.client = client;
        this.playerEntity = playerEntity;
        this.clientWorld = clientWorld;
    }

    public void onTick() {
	    if (sendPlacement) {
			if (pNeedsShift && !playerEntity.isSneaking())
				playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

			((IClientPlayerInteractionManager) client.interactionManager).rightClickBlock(pNeighbor,
					pSide, pHitVec);

			if (pNeedsShift && !playerEntity.isSneaking())
				playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

			sendPlacement = false;
			isPlacementComing = false;
		}

		if (isPlacementComing) {
			sendPlacement = true;
		}
    }

    public void doBlockPlacement(WorldSchematic worldSchematic, int range) {
		for (int y = -range; y < range + 1; y++) {
			for (int x = -range; x < range + 1; x++) {
				for (int z = -range; z < range + 1; z++) {
					BlockPos pos = playerEntity.getBlockPos().north(x).west(z).up(y);
    				BlockState requiredBlockState = worldSchematic.getBlockState(pos);
    				Material requiredMaterial = worldSchematic.getBlockState(pos).getMaterial();

					if (shouldClickBlock(clientWorld.getBlockState(pos), requiredBlockState)) {
						if (tryToPlaceBlock(pos, true)) {
							lastPlaced = new Date().getTime();
							return;
						}
					}

    				// FIXME water and lava
    				// Check if something should be placed in target block
    				if (requiredMaterial.equals(Material.AIR) || requiredMaterial.equals(Material.WATER) || requiredMaterial.equals(Material.LAVA)) continue;

					// Check if target block is empty
					if (!clientWorld.getBlockState(pos).getMaterial().equals(Material.AIR) && !isFlowingBlock(requiredBlockState)) continue;

    				// Check if can be placed in world
    				if (!requiredBlockState.canPlaceAt(clientWorld, pos)) continue;

    				// Check if player is holding right block
    				if (!isBlockInHand(requiredBlockState.getBlock())) {
    					if (playerEntity.abilities.creativeMode) {
    						ItemStack stack = new ItemStack(requiredBlockState.getBlock());
							BlockEntity te = clientWorld.getBlockEntity(pos);

							// The creative mode pick block with NBT only works correctly
							// if the server world doesn't have a TileEntity in that position.
							// Otherwise it would try to write whatever that TE is into the picked ItemStack.
							if (GuiBase.isCtrlDown() && te != null && clientWorld.isAir(pos))
							{
								ItemUtils.storeTEInStack(stack, te);
							}

							InventoryUtils.setPickedItemToHand(stack, client);
							client.interactionManager.clickCreativeStack(playerEntity.getStackInHand(Hand.MAIN_HAND),
									36 + playerEntity.inventory.selectedSlot);

						} else {
							int slot = getBlockInventorySlot(requiredBlockState.getBlock());

							if (slot == -1) {
								continue;
							}

							swapHandWithSlot(slot);
						}
					}

					if (tryToPlaceBlock(pos, false)) {
						lastPlaced = new Date().getTime();
						return;
					}
				}
			}
		}
    }

	private void swapHandWithSlot(int slot) {
		ItemStack stack = playerEntity.inventory.getStack(slot);
		InventoryUtils.setPickedItemToHand(stack, client);
	}

	private int getBlockInventorySlot(Block block) {
    	Inventory inv = playerEntity.inventory;

    	for (int slot = 0; slot < inv.size(); slot++) {
    		if (inv.getStack(slot).getItem().equals(block.asItem())) return slot;
		}

    	return -1;
	}

    private boolean tryToPlaceBlock(BlockPos pos, boolean doClick) {
    	BlockState state = SchematicWorldHandler.getSchematicWorld().getBlockState(pos);

		Vec3d posVec = Vec3d.ofCenter(pos);

		if (doClick) {
			sendChangeStateClick(pos, Direction.UP, posVec);
			return true;
		}

		Direction playerShouldBeFacing = getFacingDirection(state);
		Direction.Axis axis = availableAxis(state);
		int half = getBlockHalf(state);

//    	for (Property<?> prop : state.getProperties()) {
//			System.out.println("Block " + state.getBlock().getName() + " has property " + prop.getName() + " with value " + state.get(prop).toString() + " class name " + state.get(prop).getClass().getName());
//		}

		for(Direction side : Direction.values()) {
			if (half == 1 && side.equals(Direction.DOWN)) continue;
			if (half == 0 && side.equals(Direction.UP)) continue;
			if (axis != null && side.getAxis() != axis) continue;
			if (state.getBlock() instanceof WallTorchBlock && playerShouldBeFacing != side) continue;

			BlockPos neighbor = pos.offset(side);

			if (!canBeClicked(neighbor))
				continue;

			Vec3d hitVec = posVec.add(Vec3d.of(side.getVector()).multiply(0.5));

			if (half == 1 && !side.equals(Direction.UP)) {
				hitVec = hitVec.add(0, 0.25, 0);
			} else if (half == 0 && !side.equals(Direction.DOWN)) {
				hitVec = hitVec.add(0, -0.25, 0);
			}

			sendPlacement(neighbor, side, hitVec, playerShouldBeFacing, needsShift(state, new BlockHitResult(hitVec, side, neighbor, false)));

			return true;
		}

		return false;
	}

	private void sendPlacement(BlockPos neighbor, Direction side, Vec3d hitVec, Direction playerShouldBeFacing, boolean needsShift) {
		if (!isPlacementComing) {
			sendLookPacket(playerShouldBeFacing);

			pNeighbor = neighbor;
			pSide = side.getOpposite();
			pHitVec = hitVec;
			pNeedsShift = needsShift;

			isPlacementComing = true;
		}
	}

	private void sendChangeStateClick(BlockPos neighbor, Direction side, Vec3d hitVec) {
		if (!isPlacementComing) {
			pNeighbor = neighbor;
			pSide = side.getOpposite();
			pHitVec = hitVec;
			pNeedsShift = false;

			isPlacementComing = true;
		}
	}

	private void sendLookPacket(Direction playerShouldBeFacing) {
    	if (playerShouldBeFacing != null) {
			float yaw = playerEntity.yaw;
			float pitch = playerEntity.pitch;

			if (playerShouldBeFacing.getAxis().isHorizontal()) {
				yaw = playerShouldBeFacing.asRotation();
			} else {
				pitch = playerShouldBeFacing == Direction.DOWN ? 90 : -90;
			}

			playerEntity.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(
				yaw, pitch, playerEntity.isOnGround()));
		}
	}

    private boolean isBlockInHand(Block targetBlock) {
    	if (playerEntity.inventory.getMainHandStack().equals(ItemStack.EMPTY)) return false;
    	if (playerEntity.inventory.getMainHandStack().getItem() instanceof BlockItem) {
            return ((BlockItem) playerEntity.inventory.getMainHandStack().getItem()).getBlock().equals(targetBlock);
		}

    	return false;
	}

	private boolean canBeClicked(BlockPos pos)
	{
		return getOutlineShape(pos) != VoxelShapes.empty();
	}

	private boolean needsShift(BlockState state, BlockHitResult blockHitResult) {
		// FIXME needShift
//		Block block = state.getBlock();
//
//		ActionResult actionResult = state.onUse(client.world, client.player, Hand.MAIN_HAND, blockHitResult);
//		System.out.println("Result: " + actionResult.name());
//		if (actionResult.isAccepted()) {
//			System.out.println("ACCEPTED OR SOMETHING");
//			return true;
//		}



//		return block instanceof DoorBlock
//				|| block instanceof ChestBlock
//				|| block instanceof CraftingTableBlock
//				|| block instanceof AnvilBlock
//				|| block instanceof AbstractFurnaceBlock
//				|| block instanceof BarrelBlock
//				|| block instanceof ChestBlock
//				|| block instanceof BedBlock
//				|| block instanceof BellBlock
//				|| block instanceof BrewingStandBlock
//				|| block instanceof ChestBlock
//				|| block instanceof ChestBlock
//				|| block instanceof ChestBlock
//				|| block instanceof TrapdoorBlock;

		return true;
	}

	private VoxelShape getOutlineShape(BlockPos pos)
	{
		return getState(pos).getOutlineShape(clientWorld, pos);
	}

	private BlockState getState(BlockPos pos)
	{
		return clientWorld.getBlockState(pos);
	}
}
