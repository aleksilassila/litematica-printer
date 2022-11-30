package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.block.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * This is the placement guide that most blocks will use.
 * It will try to predict the correct player state for producing the right blockState.
 */
public class GuesserGuide extends PlacementGuide {
    private PrinterPlacementContext contextCache = null;

    protected static Property<?>[] ignoredProperties = new Property[]{
            RepeaterBlock.DELAY,
            ComparatorBlock.MODE,
            RedstoneWireBlock.POWER,
            RedstoneWireBlock.WIRE_CONNECTION_EAST,
            RedstoneWireBlock.WIRE_CONNECTION_NORTH,
            RedstoneWireBlock.WIRE_CONNECTION_SOUTH,
            RedstoneWireBlock.WIRE_CONNECTION_WEST,
            TrapdoorBlock.POWERED,
            TrapdoorBlock.OPEN,
            DoorBlock.POWERED,
            DoorBlock.OPEN,
            FenceGateBlock.POWERED,
            FenceGateBlock.OPEN,
            PointedDripstoneBlock.THICKNESS,
            HorizontalConnectingBlock.EAST,
            HorizontalConnectingBlock.NORTH,
            HorizontalConnectingBlock.SOUTH,
            HorizontalConnectingBlock.WEST,
            ScaffoldingBlock.DISTANCE,
            CactusBlock.AGE,
            BambooBlock.AGE,
            BambooBlock.LEAVES,
            BambooBlock.STAGE,
            SaplingBlock.STAGE
    };

    protected static Direction[] directionsToTry = new Direction[]{
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST,
            Direction.UP,
            Direction.DOWN
    };
    protected static Vec3d[] hitVecsToTry = new Vec3d[]{
            new Vec3d(-0.25, -0.25, -0.25),
            new Vec3d(-0.75, -0.25, -0.25),
            new Vec3d(-0.25, -0.75, -0.25),
            new Vec3d(-0.25, -0.25, -0.75),
            new Vec3d(-0.75, -0.75, -0.25),
            new Vec3d(-0.25, -0.75, -0.75),
            new Vec3d(-0.75, -0.25, -0.75),
            new Vec3d(-0.75, -0.75, -0.75),
    };

    public GuesserGuide(SchematicBlockState state) {
        super(state);
    }

    @Nullable
    @Override
    public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player) {
        if (contextCache != null) return contextCache;

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
                    BlockState result = targetState.getBlock().getPlacementState(context);

                    if (result != null && (statesMatch(result, targetState) || correctChestPlacement(targetState, result))) {
                        contextCache = context;
                        return context;
                    }
                }
            }
        }

        return null;
    }

    private boolean statesMatch(BlockState state1, BlockState state2) {
        if (state1.getBlock() != state2.getBlock()) return false;

        loop:
        for (Property<?> property : state1.getProperties()) {
            for (Property<?> ignoredProperty : ignoredProperties) {
                if (property == ignoredProperty) continue loop;
            }

            try {
                if (state1.get(property) != state2.get(property)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    private boolean correctChestPlacement(BlockState targetState, BlockState result) {
        if (targetState.contains(ChestBlock.CHEST_TYPE) && result.contains(ChestBlock.CHEST_TYPE) && result.get(ChestBlock.FACING) == targetState.get(ChestBlock.FACING)) {
            ChestType targetChestType = targetState.get(ChestBlock.CHEST_TYPE);
            ChestType resultChestType = result.get(ChestBlock.CHEST_TYPE);

            return targetChestType != ChestType.SINGLE && resultChestType == ChestType.SINGLE;
        }

        return false;
    }
}
