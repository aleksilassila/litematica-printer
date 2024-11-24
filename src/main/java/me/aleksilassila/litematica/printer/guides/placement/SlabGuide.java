package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SlabGuide extends GeneralPlacementGuide {
    public SlabGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        List<Direction> resultList = new ArrayList<>();
        SlabType targetSlabType = getProperty(state.targetState, SlabBlock.TYPE).orElse(SlabType.DOUBLE);

        if (targetSlabType == SlabType.DOUBLE) {
            return super.getPossibleSides();
        }

        Direction[] directionsToCheck = {
                Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
        };

        for (Direction direction : directionsToCheck) {
            SlabType neighborSlabType = getProperty(state.offset(direction).currentState, SlabBlock.TYPE).orElse(SlabType.DOUBLE);

            if (neighborSlabType == SlabType.DOUBLE || neighborSlabType == targetSlabType) {
                resultList.add(direction);
            }
        }

        if (targetSlabType == SlabType.TOP || targetSlabType == SlabType.BOTTOM) {
            Direction verticalDirection = targetSlabType == SlabType.TOP ? Direction.UP : Direction.DOWN;
            SlabType neighborSlabType = getProperty(state.offset(verticalDirection).currentState, SlabBlock.TYPE).orElse(SlabType.DOUBLE);

            if (neighborSlabType == SlabType.DOUBLE || neighborSlabType != targetSlabType) {
                resultList.add(verticalDirection);
            }
        }

        return resultList;
    }

    @Override
    protected Vec3d getHitModifier(Direction validSide) {
        Direction requiredHalf = getRequiredHalf(state);
        if (validSide.getHorizontal() != -1) {
            return new Vec3d(0, requiredHalf.getOffsetY() * 0.25, 0);
        } else {
            return new Vec3d(0, 0, 0);
        }
    }

    private Direction getRequiredHalf(SchematicBlockState state) {
        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        if (!currentState.contains(SlabBlock.TYPE)) {
            return targetState.get(SlabBlock.TYPE) == SlabType.TOP ? Direction.UP : Direction.DOWN;
        } else if (currentState.get(SlabBlock.TYPE) != targetState.get(SlabBlock.TYPE)) {
            return currentState.get(SlabBlock.TYPE) == SlabType.TOP ? Direction.DOWN : Direction.UP;
        } else {
            return Direction.DOWN;
        }
    }
}
