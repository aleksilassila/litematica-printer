package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.block.BlockState;

public class AdvancedGuesserGuide extends GuesserGuide {
    public AdvancedGuesserGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected boolean statesEqual(BlockState state1, BlockState state2) {
        return super.statesEqual(state1, state2);
    }
}
