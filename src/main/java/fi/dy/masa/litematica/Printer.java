package fi.dy.masa.litematica;

import fi.dy.masa.litematica.interfaces.IClientPlayerInteractionManager;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.Date;

public class Printer {
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

    public void doBlockPlacement(WorldSchematic worldSchematic, int range) {
        loop:
		for (int y = -range; y < range + 1; y++) {
			for (int x = -range; x < range + 1; x++) {
				for (int z = -range; z < range + 1; z++) {
					BlockPos pos = playerEntity.getBlockPos().north(x).west(z).up(y);
    				BlockState requiredBlockState = worldSchematic.getBlockState(pos);
    				Material requiredMaterial = worldSchematic.getBlockState(pos).getMaterial();

    				// FIXME water and lava
    				// Check if something should be placed in target block
    				if (requiredMaterial.equals(Material.AIR) || requiredMaterial.equals(Material.WATER) || requiredMaterial.equals(Material.LAVA)) continue;

					// Check if target block is empty
					if (!clientWorld.getBlockState(pos).getMaterial().equals(Material.AIR) && !isFlowingBlock(pos)) continue;

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
					};

					if (tryToPlaceBlock(pos)) {
						lastPlaced = new Date().getTime();
						break loop;
					};
				}
			}
		}
    }

    public void onTick() {
	    if (sendPlacement && playerEntity != null) {
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



	int getBlockHalf(BlockState state) {
    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equals("type") || prop.getName().equals("half")) {
				return state.get(prop).toString().equals("top") ? 1 : 0;
			}
		}

    	return -1;
	}

	Direction getFacingDirection(BlockState state) {
		Direction dir = null;

		if (state.getBlock() instanceof PillarBlock) {
			return null;
		}

    	for (Property<?> prop : state.getProperties()) {
			if (prop instanceof DirectionProperty) {
				dir = (Direction)state.get(prop);
			}
		}

    	if (dir == null) return null;

		if (shouldGetOpposite(state)) {
			dir = dir.getOpposite();
		}

		if (shouldRotate(state)) {
			dir = dir.rotateYCounterclockwise();
		}

    	return dir;
	}

	private boolean shouldGetOpposite(BlockState state) {
		return state.getBlock() instanceof AbstractFurnaceBlock
				|| state.getBlock() instanceof PistonBlock
				|| state.getBlock() instanceof BarrelBlock
				|| state.getBlock() instanceof TrapdoorBlock
				|| state.getBlock() instanceof StonecutterBlock
				|| state.getBlock() instanceof WallTorchBlock
				|| state.getBlock() instanceof LecternBlock
				|| state.getBlock() instanceof ChestBlock;
	}

	private boolean shouldRotate(BlockState state) {
		return state.getBlock() instanceof AnvilBlock ;
	}

	Direction.Axis availableAxis(BlockState state) {
    	if (state.getBlock() instanceof PillarBlock) {
    		for (Property<?> prop : state.getProperties()) {
    			if (state.get(prop) instanceof Direction.Axis) {
    				return (Direction.Axis) state.get(prop);
				}
			}
		}

    	return null;
	}

    private boolean tryToPlaceBlock(BlockPos pos) {
    	BlockState state = SchematicWorldHandler.getSchematicWorld().getBlockState(pos);

		Vec3d posVec = Vec3d.ofCenter(pos);

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

	private boolean isFlowingBlock(BlockPos pos) {
		BlockState state = clientWorld.getBlockState(pos);

		if (state.getMaterial().equals(Material.WATER) || state.getMaterial().equals(Material.LAVA)) {
			for (Property<?> prop : state.getProperties()) {
				if (prop instanceof IntProperty) {
					if ((Integer) state.get(prop) > 0) return true;
				}
			}
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
		return getState(pos).getOutlineShape(client.world, pos);
	}

	private BlockState getState(BlockPos pos)
	{
		return client.world.getBlockState(pos);
	}
}
