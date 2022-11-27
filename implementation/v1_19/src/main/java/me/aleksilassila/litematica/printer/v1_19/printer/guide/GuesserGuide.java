package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class GuesserGuide extends PlacementGuide {
    private PrinterPlacementContext contextCache = null;

    public GuesserGuide(SchematicBlockState state) {
        super(state);
    }

    @Nullable
    @Override
    public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player) {
        if (contextCache != null) return contextCache;

        Direction[] directionsToTry = Direction.values();
        Vec3d[] hitVecsToTry = new Vec3d[]{
                new Vec3d(-0.25, -0.25, -0.25),
                new Vec3d(-0.75, -0.25, -0.25),
                new Vec3d(-0.25, -0.75, -0.25),
                new Vec3d(-0.25, -0.25, -0.75),
                new Vec3d(-0.75, -0.75, -0.25),
                new Vec3d(-0.25, -0.75, -0.75),
                new Vec3d(-0.75, -0.25, -0.75),
                new Vec3d(-0.75, -0.75, -0.75),
        };

        for (Direction lookDirection : directionsToTry) {
            for (Direction side : directionsToTry) {
                BlockPos neighborPos = state.blockPos.offset(side);
                BlockState neighborState = state.world.getBlockState(neighborPos);

                if (!canBeClicked(state.world, neighborPos) || // Handle unclickable grass for example
                        neighborState.getMaterial().isReplaceable())
                    continue;

                Vec3d hitVec = Vec3d.ofCenter(state.blockPos)
                        .add(Vec3d.of(side.getVector()).multiply(0.5));

                for (Vec3d hitVecToTry : hitVecsToTry) {
                    Vec3d multiplier = Vec3d.of(side.getVector());
                    multiplier = new Vec3d(multiplier.x == 0 ? 1 : 0, multiplier.y == 0 ? 1 : 0, multiplier.z == 0 ? 1 : 0);

                    boolean requiresShift = Implementation.isInteractive(neighborState.getBlock());
                    BlockHitResult hitResult = new BlockHitResult(hitVec.add(hitVecToTry.multiply(multiplier)), side.getOpposite(), neighborPos, false);
                    PrinterPlacementContext context = new PrinterPlacementContext(player, hitResult, getBlockItem(), lookDirection, requiresShift);
                    BlockState result = state.targetState.getBlock().getPlacementState(context);

                    if (result == state.targetState) {
                        contextCache = context;
                        return context;
                    }
                }
            }
        }

        return null;
    }
}
