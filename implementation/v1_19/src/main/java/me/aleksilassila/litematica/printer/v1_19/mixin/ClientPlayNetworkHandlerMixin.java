package me.aleksilassila.litematica.printer.v1_19.mixin;

import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.Printer2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.util.math.Direction;
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
        Printer2 printer = LitematicaMixinMod.printer;

        if (printer == null || client.player == null) {
            this.connection.send(packet);
            return;
        }

        Direction lockedLookDirection = printer.packetHandler.lockedLookDirection;

        if (lockedLookDirection != null) {
            if (Implementation.isLookAndMovePacket(packet)) {
                Packet<?> fixedPacket = Implementation.getFixedLookPacket(client.player, packet, lockedLookDirection);

                if (fixedPacket != null) {
                    System.out.println("ONE GOT THROUGH");
//                    this.connection.send(fixedPacket);
                }
                return;
            } else if (Implementation.isLookOnlyPacket(packet)) {
                return;
            }
        }

        this.connection.send(packet);
    }
}
