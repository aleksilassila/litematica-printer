package fi.dy.masa.litematica.mixin;

import com.mojang.authlib.GameProfile;
import fi.dy.masa.litematica.config.Configs;
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
import net.minecraft.entity.MovementType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
	}

    @Shadow
	protected MinecraftClient client;

	protected long lastPlaced = new Date().getTime();

	protected boolean isPlacementComing = false;
	protected boolean sendPlacement = false;

	protected BlockPos pNeighbor;
	protected Direction pSide;
	protected Vec3d pHitVec;

	@Inject(at = @At("RETURN"), method = "isCamera", cancellable = true)
	protected void isCamera(CallbackInfoReturnable<Boolean> cir) {
		if (isPlacementComing) {
			cir.setReturnValue(false);
		}
	}

	@Inject(at = @At("HEAD"), method = "tick")
	public void tick(CallbackInfo ci) {
		if (sendPlacement) {
			((IClientPlayerInteractionManager) client.interactionManager).rightClickBlock(pNeighbor,
					pSide, pHitVec);

			sendPlacement = false;
			isPlacementComing = false;
		}

		if (isPlacementComing) {
			sendPlacement = true;
		}
	}

    @Inject(at = @At("HEAD"), method = "move")
    private void onPlayerMoveInput(MovementType type, Vec3d movement, CallbackInfo ci) {
		if (!Configs.Generic.PRINT_MODE.getBooleanValue()) return;
    	if (SchematicWorldHandler.getSchematicWorld() == null) return;
    	if (new Date().getTime() < lastPlaced + 1000.0 * Configs.Generic.PRINTING_DELAY.getDoubleValue()) {
			return;
		};

		WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
		int range = Configs.Generic.PRINTING_RANGE.getIntegerValue();

		loop:
		for (int y = -range; y < range + 1; y++) {
			for (int x = -range; x < range + 1; x++) {
				for (int z = -range; z < range + 1; z++) {
					BlockPos pos = this.getBlockPos().north(x).west(z).up(y);
    				BlockState requiredBlockState = worldSchematic.getBlockState(pos);
    				Material requiredMaterial = worldSchematic.getBlockState(pos).getMaterial();

    				// Check if something should be placed in target block
    				if (requiredMaterial.equals(Material.AIR)) continue;

					// Check if target block is empty
					if (!this.clientWorld.getBlockState(pos).getMaterial().equals(Material.AIR)) continue;

    				// Check if can be placed in world
    				if (!requiredBlockState.canPlaceAt(clientWorld, pos)) continue;

    				// Check if player is holding right block
    				if (!isBlockInHand(requiredBlockState.getBlock())) {
    					if (client.player.abilities.creativeMode) {
    						ItemStack stack = new ItemStack(requiredBlockState.getBlock());
							BlockEntity te = world.getBlockEntity(pos);

							// The creative mode pick block with NBT only works correctly
							// if the server world doesn't have a TileEntity in that position.
							// Otherwise it would try to write whatever that TE is into the picked ItemStack.
							if (GuiBase.isCtrlDown() && te != null && client.world.isAir(pos))
							{
								ItemUtils.storeTEInStack(stack, te);
							}

							InventoryUtils.setPickedItemToHand(stack, client);
							client.interactionManager.clickCreativeStack(client.player.getStackInHand(Hand.MAIN_HAND),
									36 + client.player.inventory.selectedSlot);

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

	private void swapHandWithSlot(int slot) {
		ItemStack stack = client.player.inventory.getStack(slot);
		InventoryUtils.setPickedItemToHand(stack, client);
	}

	private int getBlockInventorySlot(Block block) {
    	Inventory inv = client.player.inventory;

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

    	for (Property<?> prop : state.getProperties()) {
			if (prop instanceof DirectionProperty) {
				dir = (Direction)state.get(prop);
			}
		}

		if (state.getBlock() instanceof AbstractFurnaceBlock || state.getBlock() instanceof PistonBlock) {
			dir = dir.getOpposite();
		}

		if (state.getBlock() instanceof PillarBlock) {
			return null;
		}

    	return dir;
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

			BlockPos neighbor = pos.offset(side);

			if (!canBeClicked(neighbor))
				continue;

			Vec3d hitVec = posVec.add(Vec3d.of(side.getVector()).multiply(0.5));

			if (half == 1 && !side.equals(Direction.UP)) {
				hitVec = hitVec.add(0, 0.25, 0);
			} else if (half == 0 && !side.equals(Direction.DOWN)) {
				hitVec = hitVec.add(0, -0.25, 0);
			}

			sendPlacement(neighbor, side, hitVec, playerShouldBeFacing);

			return true;
		}

		return false;
	}

	private void sendPlacement(BlockPos neighbor, Direction side, Vec3d hitVec, Direction playerShouldBeFacing) {
		if (!isPlacementComing) {
			sendLookPacket(playerShouldBeFacing);

			pNeighbor = neighbor;
			pSide = side.getOpposite();
			pHitVec = hitVec;

			isPlacementComing = true;
		}
	}

	private void sendLookPacket(Direction playerShouldBeFacing) {
    	if (playerShouldBeFacing != null) {
			float yaw = client.player.yaw;
			float pitch = client.player.pitch;

			if (playerShouldBeFacing.getAxis().isHorizontal()) {
				yaw = playerShouldBeFacing.asRotation();
			} else {
				pitch = playerShouldBeFacing.asRotation();
			}

			client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(
				yaw, pitch, client.player.isOnGround()));
		}
	}

    private boolean isBlockInHand(Block targetBlock) {
    	if (this.inventory.getMainHandStack().equals(ItemStack.EMPTY)) return false;
    	if (this.inventory.getMainHandStack().getItem() instanceof BlockItem) {
    		if (((BlockItem) this.inventory.getMainHandStack().getItem()).getBlock().equals(targetBlock)) {
				return true;
			}
		}

    	return false;
	}

	private boolean canBeClicked(BlockPos pos)
	{
		return getOutlineShape(pos) != VoxelShapes.empty();
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