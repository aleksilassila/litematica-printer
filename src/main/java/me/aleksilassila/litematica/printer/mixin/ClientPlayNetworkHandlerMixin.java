package me.aleksilassila.litematica.printer.mixin;

import me.aleksilassila.litematica.printer.interfaces.Implementation;
import me.aleksilassila.litematica.printer.printer.Printer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientConnection connection;

    @Shadow
    private MinecraftClient client;

    @Overwrite
    public void sendPacket(Packet<?> packet) {
        if (Implementation.isLookPacket(packet) && Printer.shouldBlockLookPackets()) {
            Packet<?> positionOnlyPacket = Implementation.getMoveOnlyPacket(client.player, packet);

            if (positionOnlyPacket != null) {
                this.connection.send(positionOnlyPacket);
            }
        } else {
            this.connection.send(packet);
        }
    }
}
