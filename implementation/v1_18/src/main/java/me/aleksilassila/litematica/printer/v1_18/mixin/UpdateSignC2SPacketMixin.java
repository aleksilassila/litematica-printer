package me.aleksilassila.litematica.printer.v1_18.mixin;

import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UpdateSignC2SPacket.class)
public class UpdateSignC2SPacketMixin {
//    private static String getModifiedText(BlockPos blockPos, int row) {
//        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
//
//        if (worldSchematic == null) return null;
//        BlockEntity blockEntity = worldSchematic.getBlockEntity(blockPos);
//        if (!(blockEntity instanceof SignBlockEntity signBlockEntity)) return null;
//
//        return signBlockEntity.getTextOnRow(row, false).getString();
//    }

//    @ModifyVariable(method = "<init>(Lnet/minecraft/util/math/BlockPos;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD"), ordinal = 0)
//    private static String modifyLine1(BlockPos pos, String original) {
//        String modified = getModifiedText(pos, 0);
//        return modified != null ? modified : original;
//    }
}
