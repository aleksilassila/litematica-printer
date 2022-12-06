package me.aleksilassila.litematica.printer.v1_19.guides.placement;

import me.aleksilassila.litematica.printer.v1_19.SchematicBlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TorchGuide extends BlockPlacementGuide {
    public TorchGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        Optional<Direction> facing = getProperty(targetState, HorizontalFacingBlock.FACING);

        return facing
                .map(direction -> Collections.singletonList(direction.getOpposite()))
                .orElseGet(() -> Collections.singletonList(Direction.DOWN));
    }
}
