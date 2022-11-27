package me.aleksilassila.litematica.printer.v1_19.serverMixin;

import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
//    @Inject(method = "onPlayerMove", at = @At("HEAD"))
//    public void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
//        if (packet.changesLook()) {
//            System.out.println("[SERVER] Player look: " + packet.getYaw(-1) + ", " + packet.getPitch(-1));
//        }
//    }
}
