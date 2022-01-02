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

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientConnection connection;

    @Shadow
    private MinecraftClient client;

    @Overwrite
    public void sendPacket(Packet<?> packet) {
        if (Implementation.isLookAndMovePacket(packet) && Printer.shouldBlockLookPackets()) {
            Packet<?> fixedPacket = Implementation.getFixedLookPacket(client.player, packet);

            if (fixedPacket != null) {
                this.connection.send(fixedPacket);
            }
        } else if (!(Implementation.isLookOnlyPacket(packet) && Printer.shouldBlockLookPackets())) {
            this.connection.send(packet);
        }
    }
}
