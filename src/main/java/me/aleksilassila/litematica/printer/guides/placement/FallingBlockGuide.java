package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Direction;

public class FallingBlockGuide extends GuesserGuide {

    public FallingBlockGuide(SchematicBlockState state) {
        super(state);
    }

    boolean blockPlacement() {
        if (targetState.getBlock() instanceof FallingBlock) {
            BlockState below = state.world.getBlockState(state.blockPos.offset(Direction.DOWN));
            return FallingBlock.canFallThrough(below);
        }

        return false;
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (blockPlacement())
            return false;

        return super.canExecute(player);
    }

    @Override
    public boolean skipOtherGuides() {
        if (blockPlacement())
            return true;

        return super.skipOtherGuides();
    }
}
