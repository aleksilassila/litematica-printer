package me.aleksilassila.litematica.printer.v1_19_4.mixin;

import me.aleksilassila.litematica.printer.v1_19_4.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19_4.Printer;
import me.aleksilassila.litematica.printer.v1_19_4.actions.PrepareAction;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerMoveC2SPacket.class)
public class PlayerMoveC2SPacketMixin {
    @ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 0)
    private static float modifyLookYaw(float yaw) {
        Printer printer = LitematicaMixinMod.printer;
        if (printer == null) return yaw;

        PrepareAction action = printer.actionHandler.lookAction;
        if (action != null && action.modifyYaw) {
            if (LitematicaMixinMod.DEBUG) System.out.println("YAW: " + action.yaw);
            return action.yaw;
        } else return yaw;
    }

    @ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 1)
    private static float modifyLookPitch(float pitch) {
        Printer printer = LitematicaMixinMod.printer;
        if (printer == null) return pitch;

        PrepareAction action = printer.actionHandler.lookAction;
        if (action != null && action.modifyPitch) {
            if (LitematicaMixinMod.DEBUG) System.out.println("PITCH: " + action.pitch);
            return action.pitch;
        } else return pitch;
    }
}
