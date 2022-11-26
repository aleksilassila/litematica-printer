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
    static PrinterPlacementContext contextCache = null;
    static BlockPos contextCachePos = null;

    PrinterPlacementContext getContextCache(SchematicBlockState state) {
        if (state.blockPos != contextCachePos) {
            contextCache = null;
            contextCachePos = null;
        }
        return contextCache;
    }

    void setContextCache(SchematicBlockState state, PrinterPlacementContext context) {
        contextCache = context;
        contextCachePos = state.blockPos;
    }

    @Nullable
    @Override
    public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player, SchematicBlockState state) {
        PrinterPlacementContext cached = getContextCache(state);
        if (cached != null) return cached;

        Direction[] directionsToTry = Direction.values();
        System.out.println(state.targetState.getBlock());

        for (Direction direction : directionsToTry) {
            BlockPos neighborPos = state.blockPos.offset(direction);
            BlockState neighborState = state.world.getBlockState(neighborPos);

            if (!canBeClicked(state.world, neighborPos) || // Handle unclickable grass for example
                    neighborState.getMaterial().isReplaceable())
                continue;

            Vec3d hitVec = Vec3d.ofCenter(state.blockPos)
                    .add(Vec3d.of(direction.getVector()).multiply(0.5));

            Vec3d[] hitVecsToTry = new Vec3d[]{
                    new Vec3d(0.25, 0.25, 0.25),
                    new Vec3d(0.75, 0.25, 0.25),
                    new Vec3d(0.25, 0.75, 0.25),
                    new Vec3d(0.25, 0.25, 0.75),
                    new Vec3d(0.75, 0.75, 0.25),
                    new Vec3d(0.25, 0.75, 0.75),
                    new Vec3d(0.75, 0.25, 0.75),
                    new Vec3d(0.75, 0.75, 0.75),
            };

            for (Vec3d hitVecToTry : hitVecsToTry) {
                Vec3d multiplier = Vec3d.of(direction.getVector());
                multiplier = new Vec3d(multiplier.x == 0 ? 1 : 0, multiplier.y == 0 ? 1 : 0, multiplier.z == 0 ? 1 : 0);

                boolean requiresShift = Implementation.isInteractive(neighborState.getBlock());
                BlockHitResult hitResult = new BlockHitResult(hitVec.add(hitVecToTry.multiply(multiplier)), direction.getOpposite(), neighborPos, false);
                PrinterPlacementContext context = new PrinterPlacementContext(player, hitResult, state.targetState.getBlock().asItem().getDefaultStack(), null, requiresShift);
                BlockState result = state.targetState.getBlock().getPlacementState(context);

                if (result == state.targetState) {
                    setContextCache(state, context);
                    return context;
                }
            }
        }

        return null;
    }
}
