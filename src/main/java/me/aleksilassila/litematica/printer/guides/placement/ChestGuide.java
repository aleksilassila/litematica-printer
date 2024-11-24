package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Whilst making this guide, I learned that chests are much like humans.
 * Some prefer to stay single, and some want to connect with another of its
 * kind.
 * Also, that reversing chest connection logic is an enormous pain in the ass. I
 * spent way too long on this.
 * Thanks for coming to my ted talk
 */
public class ChestGuide extends GeneralPlacementGuide {
    public ChestGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected boolean getRequiresExplicitShift() {
        return true;
    }

    @Override
    public boolean skipOtherGuides() {
        return true;
    }

    @Override
    protected Optional<Direction> getLookDirection() {
        return getProperty(targetState, ChestBlock.FACING)
                .flatMap(facing -> Optional.of(facing.getOpposite()));
    }

    @Override
    protected List<Direction> getPossibleSides() {
        ChestType targetType = getProperty(targetState, ChestBlock.CHEST_TYPE).orElse(null);
        Direction targetFacing = getProperty(targetState, ChestBlock.FACING).orElse(null);

        List<Direction> sides = new ArrayList<>();

        if (targetFacing == null || targetType == null)
            return sides;

        for (Direction direction : Direction.values()) {
            if (targetType == ChestType.SINGLE && !willConnectToSide(state, direction)) {
                sides.add(direction);
            } else if (wantsToConnectToSide(state, direction) && willConnectToSide(state, direction)) { // :D
                sides.add(direction);
            }
        }

        // Place single chests if you cannot connect any existing chests
        if (sides.isEmpty()) {
            for (Direction direction : Direction.values()) {
                if (!wantsToConnectToSide(state, direction) && !willConnectToSide(state, direction)) {
                    sides.add(direction);
                }
            }
        }

        return sides;
    }

    private boolean willConnectToSide(SchematicBlockState state, Direction neighborDirection) {
        BlockState neighbor = state.offset(neighborDirection).currentState;
        ChestType neighborType = getProperty(neighbor, ChestBlock.CHEST_TYPE).orElse(null);
        Direction neighborFacing = getProperty(neighbor, ChestBlock.FACING).orElse(null);
        Direction facing = getProperty(state.targetState, ChestBlock.FACING).orElse(null);

        if (neighborType == null || neighborFacing == null || facing == null) return false;

        if (facing.getAxis() == neighborDirection.getAxis() || neighborDirection.getAxis() == Direction.Axis.Y)
            return false;

        return neighborType == ChestType.SINGLE && neighborFacing == facing
                && state.targetState.getBlock() == neighbor.getBlock();
    }

    private boolean wantsToConnectToSide(SchematicBlockState state, Direction direction) {
        ChestType type = getProperty(state.targetState, ChestBlock.CHEST_TYPE).orElse(null);
        Direction facing = getProperty(state.targetState, ChestBlock.FACING).orElse(null);
        if (type == null || facing == null || type == ChestType.SINGLE) return false;

        Direction neighborDirection = type == ChestType.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();

        return direction == neighborDirection;
    }
}
