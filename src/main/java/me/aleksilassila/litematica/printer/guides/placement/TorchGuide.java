package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TorchGuide extends GeneralPlacementGuide {
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

    @Override
    protected Optional<Block> getRequiredItemAsBlock(ClientPlayerEntity player) {
        return Optional.of(state.targetState.getBlock());
    }
}
