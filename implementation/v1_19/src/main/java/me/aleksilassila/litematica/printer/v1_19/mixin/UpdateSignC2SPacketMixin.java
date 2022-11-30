package me.aleksilassila.litematica.printer.v1_19.mixin;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.printer.Printer2;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(UpdateSignC2SPacket.class)
public class UpdateSignC2SPacketMixin {
    private static String getModifiedText(BlockPos blockPos, int row) {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null) return null;
        BlockEntity blockEntity = worldSchematic.getBlockEntity(blockPos);
        if (!(blockEntity instanceof SignBlockEntity signBlockEntity)) return null;

        return signBlockEntity.getTextOnRow(row, false).getString();
    }

    @ModifyVariable(method = "<init>(Lnet/minecraft/util/math/BlockPos;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD"), ordinal = 0)
    private static String modifyLine1(BlockPos pos, String original) {
        String modified = getModifiedText(pos, 0);
        return modified != null ? modified : original;
    }

}
