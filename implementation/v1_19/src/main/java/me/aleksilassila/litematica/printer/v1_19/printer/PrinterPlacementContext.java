package me.aleksilassila.litematica.printer.v1_19.printer;

import me.aleksilassila.litematica.printer.v1_19.mixin.ItemPlacementAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class PrinterPlacementContext extends ItemPlacementContext {
    public final Direction lookDirection;
    public final boolean requiresSneaking;
    public final BlockHitResult hitResult;

    public PrinterPlacementContext(PlayerEntity player, BlockHitResult hitResult, ItemStack requiredItem, Direction lookDirection, boolean requiresSneaking) {
        super(player, Hand.MAIN_HAND, requiredItem, hitResult);

        this.lookDirection = lookDirection;
        this.requiresSneaking = requiresSneaking;
        this.hitResult = hitResult;
    }

    @Override
    public String toString() {
        return "PrinterPlacementContext{" +
                "lookDirection=" + lookDirection +
                ", requiresSneaking=" + requiresSneaking +
                ", blockPos=" + hitResult.getBlockPos() +
                ", side=" + hitResult.getSide() +
//                ", hitVec=" + hitResult +
                '}';
    }
}
