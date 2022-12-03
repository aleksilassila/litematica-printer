package me.aleksilassila.litematica.printer.v1_18.guides;

import me.aleksilassila.litematica.printer.v1_18.SchematicBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.state.property.Properties;

import java.util.Arrays;

public class RailGuesserGuide extends MultiPropertyGuesserGuide {
    static final RailShape[] STRAIGHT_RAIL_SHAPES = new RailShape[]{
            RailShape.NORTH_SOUTH,
            RailShape.EAST_WEST
    };

    public RailGuesserGuide(SchematicBlockState state) {
        super(state);
    }


    @Override
    public boolean canExecute(ClientPlayerEntity player) {
//        PrinterPlacementContext ctx = getPlacementContext(player);
//        BlockState result = targetState.getBlock().getPlacementState(ctx);
//
//        if (result != null) {
//            RailShape shape = result.get(Properties.RAIL_SHAPE);
//
//            if (Arrays.asList(STRAIGHT_RAIL_SHAPES).contains(shape)) {
//                Direction direction1 = shape == RailShape.NORTH_SOUTH ? Direction.NORTH : Direction.EAST;
//                Direction direction2 = shape == RailShape.NORTH_SOUTH ? Direction.SOUTH : Direction.WEST;
//
//
//            }
//
//
//            return Arrays.asList(STRAIGHT_RAIL_SHAPES).contains(shape);
//        }

        return super.canExecute(player);
    }

    @Override
    protected boolean statesEqual(BlockState resultState, BlockState targetState) {
        if (resultState.contains(Properties.RAIL_SHAPE)) {
            if (Arrays.stream(STRAIGHT_RAIL_SHAPES).anyMatch(shape -> shape == resultState.get(Properties.RAIL_SHAPE))) {
                return super.statesEqualIgnoreProperties(resultState, targetState, Properties.RAIL_SHAPE);
            }
        }

        return super.statesEqual(resultState, targetState);
    }
}
