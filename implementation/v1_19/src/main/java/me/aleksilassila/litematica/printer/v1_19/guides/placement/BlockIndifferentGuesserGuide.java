package me.aleksilassila.litematica.printer.v1_19.guides.placement;

import me.aleksilassila.litematica.printer.v1_19.SchematicBlockState;
import net.minecraft.block.*;

public class BlockIndifferentGuesserGuide extends GuesserGuide {
    public BlockIndifferentGuesserGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected boolean statesEqual(BlockState resultState, BlockState targetState) {
        if (targetState.getBlock() instanceof BambooBlock) {
            return resultState.getBlock() instanceof BambooBlock || resultState.getBlock() instanceof BambooSaplingBlock;
        }

        if (targetState.getBlock() instanceof BigDripleafStemBlock) {
            if (resultState.getBlock() instanceof BigDripleafBlock || resultState.getBlock() instanceof BigDripleafStemBlock) {
                return resultState.get(HorizontalFacingBlock.FACING) == targetState.get(HorizontalFacingBlock.FACING);
            }
        }

        return super.statesEqual(resultState, targetState);
    }
}
