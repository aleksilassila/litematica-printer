package me.aleksilassila.litematica.printer.v1_19.interfaces;

import me.aleksilassila.litematica.printer.v1_19.printer.PrinterPlacementContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface IClientPlayerInteractionManager {
    void rightClickBlock(BlockPos pos, Direction side, Vec3d hitVec);

    void rightClickBlock(PrinterPlacementContext context);
}
