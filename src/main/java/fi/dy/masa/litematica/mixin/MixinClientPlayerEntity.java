package fi.dy.masa.litematica.mixin;

import com.mojang.authlib.GameProfile;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.printer.Printer;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Date;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
	}

    @Shadow
	protected MinecraftClient client;

	protected Printer printer;

	@Inject(at = @At("RETURN"), method = "isCamera", cancellable = true)
	protected void isCamera(CallbackInfoReturnable<Boolean> cir) {
		if (printer != null && printer.isPlacementComing) {
			cir.setReturnValue(false);
		}
	}

	@Inject(at = @At("HEAD"), method = "tick")
	public void tick(CallbackInfo ci) {
		if (printer == null) {
			if (client != null && client.player != null && client.world != null) {
				printer = new Printer(client, client.player, client.world);
			}
		} else {
			printer.onTick();
		}
	}

    @Inject(at = @At("HEAD"), method = "move")
    private void onPlayerMoveInput(MovementType type, Vec3d movement, CallbackInfo ci) {
		if (!Configs.Generic.PRINT_MODE.getBooleanValue() || printer == null) return;
    	if (SchematicWorldHandler.getSchematicWorld() == null) return;
    	if (new Date().getTime() < printer.lastPlaced + 1000.0 * Configs.Generic.PRINTING_DELAY.getDoubleValue()) {
			return;
		};

		WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
		int range = Configs.Generic.PRINTING_RANGE.getIntegerValue();

		printer.doBlockPlacement(worldSchematic, range);
    }
}