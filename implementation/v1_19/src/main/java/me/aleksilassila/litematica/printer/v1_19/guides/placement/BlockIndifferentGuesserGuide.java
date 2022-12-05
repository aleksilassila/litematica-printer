package me.aleksilassila.litematica.printer.v1_19.guides.placement;

import me.aleksilassila.litematica.printer.v1_19.SchematicBlockState;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;

public class BlockIndifferentGuesserGuide extends GuesserGuide {
    public BlockIndifferentGuesserGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected boolean statesEqual(BlockState resultState, BlockState targetState) {
        Block targetBlock = targetState.getBlock();
        Block resultBlock = resultState.getBlock();

        if (targetBlock instanceof BambooBlock) {
            return resultBlock instanceof BambooBlock || resultBlock instanceof BambooSaplingBlock;
        }

        if (targetBlock instanceof BigDripleafStemBlock) {
            if (resultBlock instanceof BigDripleafBlock || resultBlock instanceof BigDripleafStemBlock) {
                return resultState.get(HorizontalFacingBlock.FACING) == targetState.get(HorizontalFacingBlock.FACING);
            }
        }

        if (targetBlock instanceof TwistingVinesPlantBlock) {
            if (resultBlock instanceof TwistingVinesBlock) {
                return true;
            } else if (resultBlock instanceof TwistingVinesPlantBlock) {
                return statesEqualIgnoreProperties(resultState, targetState, TwistingVinesBlock.AGE);
            }
        }

        if (targetBlock instanceof TripwireBlock && resultBlock instanceof TripwireBlock) {
            return statesEqualIgnoreProperties(resultState, targetState,
                    TripwireBlock.ATTACHED, TripwireBlock.DISARMED, TripwireBlock.POWERED, TripwireBlock.NORTH,
                    TripwireBlock.EAST, TripwireBlock.SOUTH, TripwireBlock.WEST);
        }

//        if (getProperty(targetState, SlabBlock.TYPE).orElse(null) == SlabType.DOUBLE && resultBlock instanceof SlabBlock) {
//            return statesEqualIgnoreProperties(resultState, targetState, SlabBlock.TYPE);
//        }

        return super.statesEqual(resultState, targetState);
    }
}
