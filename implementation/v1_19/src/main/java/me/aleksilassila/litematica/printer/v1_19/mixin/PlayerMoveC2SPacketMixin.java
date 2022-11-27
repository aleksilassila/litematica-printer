package me.aleksilassila.litematica.printer.v1_19.mixin;

import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.printer.Printer2;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Direction;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerMoveC2SPacket.class)
public class PlayerMoveC2SPacketMixin {
    // This didn't work either, it complained that I couldn't hook the constructor or something
//    @ModifyArgs(method = "<init>(DDDFFZZZ)V", at = @At("INVOKE"))
//    private static void modifyLookDir(Args args) {
//        Printer2 printer = LitematicaMixinMod.printer;
//        if (printer == null) return;
//        Direction lockedLookDirection = printer.packetHandler.lockedLookDirection;
//
//        if (lockedLookDirection != null) {
//            args.set(4, (float) lockedLookDirection.asRotation());
//        }
//    }

    @ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 0)
    private static float modifyLookYaw(float yaw) {
        Printer2 printer = LitematicaMixinMod.printer;
        if (printer == null) return yaw;
        Direction lockedLookDirection = printer.packetHandler.lockedLookDirection;

        if (lockedLookDirection != null && lockedLookDirection.getAxis().isHorizontal()) {
            System.out.println("RETURNING " + lockedLookDirection);
            return lockedLookDirection.asRotation();
        }

        return yaw;
    }

    @ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 1)
    private static float modifyLookPitch(float pitch) {
        Printer2 printer = LitematicaMixinMod.printer;
        if (printer == null) return pitch;
        Direction lockedLookDirection = printer.packetHandler.lockedLookDirection;

        if (lockedLookDirection == Direction.UP) {
            return -90;
        } else if (lockedLookDirection == Direction.DOWN) {
            return 90;
        }

        return pitch;
    }

//    @Redirect(method = "<init>(DDDFFZZZ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;yaw:F", opcode = Opcodes.PUTFIELD))
//    private void injected(PlayerMoveC2SPacket packet, float yaw) {
//        Printer2 printer = LitematicaMixinMod.printer;
//        if (printer == null) return;
//        Direction lockedLookDirection = printer.packetHandler.lockedLookDirection;
//
//
//        // this .setYaw fails.
//        if (lockedLookDirection != null) {
//            ((PlayerMoveC2SPacketAccessor) packet).setYaw((float) lockedLookDirection.asRotation());
//        } else {
//            ((PlayerMoveC2SPacketAccessor) packet).setYaw(yaw);
//        }
//    }
}
