package fi.dy.masa.litematica.mixin;

import com.mojang.authlib.GameProfile;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.interfaces.IClientPlayerInteractionManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
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

import java.util.Date;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
		this.client = null;
	}

    @Shadow
	protected final MinecraftClient client;

	protected long lastPlaced = new Date().getTime();

    @Inject(at = @At("HEAD"),
    method = "move")
    private void onPlayerMoveInput(MovementType type, Vec3d movement, CallbackInfo ci) {
		if (!Configs.Generic.PRINT_MODE.getBooleanValue()) return;
    	if (SchematicWorldHandler.getSchematicWorld() == null) return;
    	if (new Date().getTime() < lastPlaced + 1000.0 * Configs.Generic.PRINTING_DELAY.getDoubleValue()) {
			System.out.println("DELAYED " + lastPlaced);
			return;
		};

		WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
		int range = Configs.Generic.PRINTING_RANGE.getIntegerValue();

		loop:
		for (int x = -range; x < range + 1; x++) {
			for (int y = -range; y < range + 1; y++) {
				for (int z = -range; z < range + 1; z++) {
					BlockPos pos = this.getBlockPos().north(x).west(z).up(y);
					if (!this.clientWorld.getBlockState(pos).getMaterial().equals(Material.AIR)) continue;

    				Block targetBlock = worldSchematic.getBlockState(pos).getBlock();
    				Material targetMaterial = worldSchematic.getBlockState(pos).getMaterial();
					if (targetMaterial.equals(Material.AIR)) continue;
    				if (!isBlockInHand(targetBlock)) continue;

					Vec3d posVec = Vec3d.ofCenter(pos);

					for(Direction side : Direction.values()) {
						BlockPos neighbor = pos.offset(side);

						if (!canBeClicked(neighbor))
							continue;

						Vec3d hitVec = posVec.add(Vec3d.of(side.getVector()).multiply(0.5));

						((IClientPlayerInteractionManager) this.client.interactionManager).rightClickBlock(neighbor,
								side.getOpposite(), hitVec);

						lastPlaced = new Date().getTime();
						break loop;
					}
				}
			}
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
		return getState(pos).getOutlineShape(this.client.world, pos);
	}

	private BlockState getState(BlockPos pos)
	{
		return this.client.world.getBlockState(pos);
	}
}