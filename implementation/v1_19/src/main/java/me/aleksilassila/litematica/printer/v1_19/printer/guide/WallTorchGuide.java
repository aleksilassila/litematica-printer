package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.List;

public class WallTorchGuide extends PlacementGuide {
    @Override
    protected List<Direction> getPossibleSides(SchematicBlockState state, BlockState currentState, BlockState targetState) {
        if (targetState.contains(WallTorchBlock.FACING)) {
            return Collections.singletonList(targetState.get(WallTorchBlock.FACING).getOpposite());
        }

        return null;
    }
}
