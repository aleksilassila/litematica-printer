package me.aleksilassila.litematica.printer.v1_19_4.guides.placement;

import me.aleksilassila.litematica.printer.v1_19_4.implementation.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19_4.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19_4.actions.Action;
import me.aleksilassila.litematica.printer.v1_19_4.actions.PrepareAction;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RotatingBlockGuide extends GeneralPlacementGuide {
    public RotatingBlockGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        Block block = state.targetState.getBlock();
        if (block instanceof WallSkullBlock || block instanceof WallSignBlock || block instanceof WallBannerBlock) {
            Optional<Direction> side = getProperty(state.targetState, Properties.HORIZONTAL_FACING).map(Direction::getOpposite);
            return side.map(Collections::singletonList).orElseGet(Collections::emptyList);
        }

        return Collections.singletonList(Direction.DOWN);
    }

    @Override
    public boolean skipOtherGuides() {
        return true;
    }

    @Override
    public @NotNull List<Action> execute(ClientPlayerEntity player) {
        PrinterPlacementContext ctx = getPlacementContext(player);

        if (ctx == null) return new ArrayList<>();

        int rotation = getProperty(state.targetState, Properties.ROTATION).orElse(0);
        if (targetState.getBlock() instanceof BannerBlock || targetState.getBlock() instanceof SignBlock) {
            rotation = (rotation + 8) % 16;
        }

        int distTo0 = rotation > 8 ? 16 - rotation : rotation;
        float yaw = Math.round(distTo0 / 8f * 180f * (rotation > 8 ? -1 : 1));

        List<Action> actions = super.execute(player);
        actions.set(0, new PrepareAction(ctx, yaw, 0));

        return actions;
    }
}
