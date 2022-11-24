package me.aleksilassila.litematica.printer.v1_19.mixin;

import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.Printer2;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
//    @ModifyVariable(method = "sendImmediately(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), ordinal = 0)
//    private Packet<?> modifyMovePackets(Packet<?> packet) {
//        if (packet instanceof PlayerMoveC2SPacket.Full || packet instanceof PlayerMoveC2SPacket.LookAndOnGround) {
//            Printer2 printer = LitematicaMixinMod.printer;
//            if (printer == null) return packet;
//
//            Direction lockedLookDirection = printer.packetHandler.lockedLookDirection;
//            if (lockedLookDirection != null) {
//                System.out.println("Intercepted packet: " + packet.getClass().getName());
////                return Implementation.getFixedLookPacket(printer.player, packet, lockedLookDirection);
//                Packet<?> fixed = Implementation.getFixedLookPacket(printer.player, packet, lockedLookDirection);
//                System.out.println("Would have sent: " + fixed);
//                return packet;
//            }
//        }
//
//        return packet;
//    }

//    @Inject(at = @At("HEAD"), method = "sendImmediately")
//    private void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
//        Printer2 printer = LitematicaMixinMod.printer;
//        if (printer == null) return;
//        Direction lockedLookDirection = printer.packetHandler.lockedLookDirection;
//
//        if (lockedLookDirection != null) {
//            try {
//                double yaw = ((PlayerMoveC2SPacketAccessor) packet).getYaw();
//                Direction lookDir = Direction.fromRotation(yaw);
//                System.out.println("Directions match: " + (lookDir == lockedLookDirection) + ", lookDir: " + lookDir + ", required: " + lockedLookDirection + ", instance of PlayerMove: " + (packet instanceof PlayerMoveC2SPacket));
//            } catch (Exception ignored) {
//                //            ignored.printStackTrace();
//                System.out.println("Failed to print packet");
//            }
//        }
//    }
}
