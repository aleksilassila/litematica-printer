package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.guides.interaction.LogStrippingGuide;
import net.minecraft.block.Block;
import net.minecraft.block.PillarBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LogGuide extends GeneralPlacementGuide {
    public LogGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        if (targetState.contains(PillarBlock.AXIS)) {
            Direction.Axis axis = targetState.get(PillarBlock.AXIS);
            return Arrays.stream(Direction.values()).filter(d -> d.getAxis() == axis).toList();
        }

        return new ArrayList<>();
    }

    @Override
    protected @Nonnull List<ItemStack> getRequiredItems() {
        for (Block log : LogStrippingGuide.STRIPPED_BLOCKS.keySet()) {
            if (targetState.getBlock() == LogStrippingGuide.STRIPPED_BLOCKS.get(log)) {
                return Collections.singletonList(new ItemStack(log));
            }
        }

        return super.getRequiredItems();
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (!Configs.STRIP_LOGS.getBooleanValue())
            return false;

        if (LogStrippingGuide.STRIPPED_BLOCKS.containsValue(targetState.getBlock())) {
            return super.canExecute(player);
        }

        return false;
    }
}
