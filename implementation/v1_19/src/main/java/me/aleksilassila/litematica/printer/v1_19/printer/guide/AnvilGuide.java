package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.block.AnvilBlock;
import net.minecraft.util.math.Direction;

public class AnvilGuide extends PlacementGuide {
    public AnvilGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected Direction getLookDirection() {
        return targetState.get(AnvilBlock.FACING).rotateYCounterclockwise();
    }
}
