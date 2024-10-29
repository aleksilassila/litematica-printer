package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

public class PropertySpecificGuesserGuide extends GuesserGuide {
    protected static Property<?>[] ignoredProperties = new Property[] {
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
            ScaffoldingBlock.BOTTOM,
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
            EndPortalFrameBlock.EYE,
            Properties.LIT,
            LeavesBlock.DISTANCE,
            LeavesBlock.PERSISTENT,
            Properties.ATTACHED,
            Properties.NOTE,
            Properties.INSTRUMENT,

    };

    public PropertySpecificGuesserGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected boolean statesEqual(BlockState resultState, BlockState targetState) {
        return statesEqualIgnoreProperties(resultState, targetState, ignoredProperties);
    }
}
