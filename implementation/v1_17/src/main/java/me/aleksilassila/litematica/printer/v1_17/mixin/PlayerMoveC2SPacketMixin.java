package me.aleksilassila.litematica.printer.v1_17.mixin;

import me.aleksilassila.litematica.printer.v1_17.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_17.Printer;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerMoveC2SPacket.class)
public class PlayerMoveC2SPacketMixin {
    @ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 0)
    private static float modifyLookYaw(float yaw) {
        Printer printer = LitematicaMixinMod.printer;
        if (printer == null) return yaw;
        Direction lockedLookDirection = printer.packetHandler.lockedLookDirection;

        if (lockedLookDirection != null && lockedLookDirection.getAxis().isHorizontal()) {
//            System.out.println("RETURNING " + lockedLookDirection);
            return lockedLookDirection.asRotation();
        }

        return yaw;
    }

    @ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 1)
    private static float modifyLookPitch(float pitch) {
        Printer printer = LitematicaMixinMod.printer;
        if (printer == null) return pitch;
        Direction lockedLookDirection = printer.packetHandler.lockedLookDirection;

        if (lockedLookDirection == Direction.UP) {
            return -90;
        } else if (lockedLookDirection == Direction.DOWN) {
            return 90;
        } else if (lockedLookDirection != null) {
            return 0;
        }

        return pitch;
    }
}
