package me.aleksilassila.litematica.printer.v1_17.guides;

import me.aleksilassila.litematica.printer.v1_17.SchematicBlockState;
import net.minecraft.block.WallRedstoneTorchBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.List;

public class WallTorchGuide extends BlockPlacementGuide {
    public WallTorchGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        if (targetState.contains(WallTorchBlock.FACING)) {
            return Collections.singletonList(targetState.get(WallTorchBlock.FACING).getOpposite());
        }

        if (targetState.contains(WallRedstoneTorchBlock.FACING)) {
            return Collections.singletonList(targetState.get(WallRedstoneTorchBlock.FACING).getOpposite());
        }

        return null;
    }
}
