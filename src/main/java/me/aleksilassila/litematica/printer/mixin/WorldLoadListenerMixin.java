package me.aleksilassila.litematica.printer.mixin;

import fi.dy.masa.litematica.event.WorldLoadListener;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import me.aleksilassila.litematica.printer.printer.Printer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldLoadListener.class, remap = false)
public class WorldLoadListenerMixin {
    @Inject(at = @At("TAIL"), method = "onWorldLoadPost")
    public void onWorldLoadPost(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc, CallbackInfo ci) {
        Printer.init(mc, worldAfter, SchematicWorldHandler.getSchematicWorld());
    }
}
