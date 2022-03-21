package me.aleksilassila.litematica.printer.printer;

import net.minecraft.block.BlockState;

public enum State {
    MISSING_BLOCK,
    WRONG_STATE,
    CORRECT;

    public static State get(BlockState schematicBlockState, BlockState currentBlockState) {
        if (!schematicBlockState.isAir() && currentBlockState.isAir())
            return State.MISSING_BLOCK;
        else if (schematicBlockState.getBlock().equals(currentBlockState.getBlock())
                && !schematicBlockState.equals(currentBlockState))
            return State.WRONG_STATE;

        return State.CORRECT;
    }
}
