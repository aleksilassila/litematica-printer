package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.PrepareAction;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class IncreaseGuide extends AbstractPlacementGuide {
    private static final HashMap<IntProperty, Item> increasingProperties = new HashMap<>();

    static {
        addProperties();
    }

    private Integer currentLevel = null;
    private Integer targetLevel = null;
    private IntProperty increasingProperty = null;

    protected static void addProperties() {
        increasingProperties.put(SnowBlock.LAYERS, null);
        increasingProperties.put(SeaPickleBlock.PICKLES, null);
        increasingProperties.put(CandleBlock.CANDLES, null);
//            increasingProperties.put(LeveledCauldronBlock.LEVEL, Items.GLASS_BOTTLE);
    }

    public IncreaseGuide(SchematicBlockState state) {
        super(state);

        for (IntProperty property : increasingProperties.keySet()) {
            if (targetState.contains(property) && currentState.contains(property)) {
                currentLevel = currentState.get(property);
                targetLevel = targetState.get(property);
                increasingProperty = property;
                break;
            }
        }
    }

    @Override
    protected boolean getRequiresShift(SchematicBlockState state) {
        return false;
    }

    @Override
    public @Nullable PrinterPlacementContext getPlacementContext(ClientPlayerEntity player) {
        return null;
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (currentLevel == null || targetLevel == null || increasingProperty == null) return false;
        if (!statesEqualIgnoreProperties(currentState, targetState, increasingProperty)) return false;
        if (currentLevel >= targetLevel) return false;

        return super.canExecute(player);
    }
}
