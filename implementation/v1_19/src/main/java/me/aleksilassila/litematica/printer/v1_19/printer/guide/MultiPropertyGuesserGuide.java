package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.block.*;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

public class MultiPropertyGuesserGuide extends GuesserGuide {
    protected static Property<?>[] ignoredProperties = new Property[]{
            RepeaterBlock.DELAY,
            ComparatorBlock.MODE,
            RedstoneWireBlock.POWER,
            RedstoneWireBlock.WIRE_CONNECTION_EAST,
            RedstoneWireBlock.WIRE_CONNECTION_NORTH,
            RedstoneWireBlock.WIRE_CONNECTION_SOUTH,
            RedstoneWireBlock.WIRE_CONNECTION_WEST,
            Properties.POWERED,
            Properties.OPEN,
            PointedDripstoneBlock.THICKNESS,
            ScaffoldingBlock.DISTANCE,
            CactusBlock.AGE,
            BambooBlock.AGE,
            BambooBlock.LEAVES,
            BambooBlock.STAGE,
            SaplingBlock.STAGE,
            HorizontalConnectingBlock.EAST,
            HorizontalConnectingBlock.NORTH,
            HorizontalConnectingBlock.SOUTH,
            HorizontalConnectingBlock.WEST,
            SnowBlock.LAYERS,
            SeaPickleBlock.PICKLES,
            CandleBlock.CANDLES,
    };

    public MultiPropertyGuesserGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected boolean statesEqual(BlockState state1, BlockState state2) {
        return statesEqualIgnoreProperties(state1, state2, ignoredProperties);
    }
}
