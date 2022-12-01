package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterUtils;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Property;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

abstract public class AbstractGuide extends PrinterUtils {
    protected final SchematicBlockState state;
    protected final BlockState currentState;
    protected final BlockState targetState;

    public AbstractGuide(SchematicBlockState state) {
        this.state = state;

        this.currentState = state.currentState;
        this.targetState = state.targetState;
    }

    protected boolean playerHasRightItem(ClientPlayerEntity player) {
        return getStackSlot(player) != -1;
    }

    protected int getStackSlot(ClientPlayerEntity player) {
        List<ItemStack> requiredItems = getRequiredItems();

        if (Implementation.getAbilities(player).creativeMode) {
            return Implementation.getInventory(player).selectedSlot;
        }

        for (ItemStack requiredItem : requiredItems) {
            int slot = Implementation.getInventory(player).getSlotWithStack(requiredItem);
            if (slot > -1) return slot;
        }

        return -1;
    }

    public boolean canExecute(ClientPlayerEntity player) {
        if (!playerHasRightItem(player)) return false;

        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        return !targetState.equals(currentState);
    }

    abstract public List<AbstractAction> execute(ClientPlayerEntity player);

    abstract protected @NotNull List<ItemStack> getRequiredItems();

    protected boolean statesEqualIgnoreProperties(BlockState state1, BlockState state2, Property<?>... propertiesToIgnore) {
        if (state1.getBlock() != state2.getBlock()) return false;

        loop:
        for (Property<?> property : state1.getProperties()) {
            for (Property<?> ignoredProperty : propertiesToIgnore) {
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

    protected boolean statesEqual(BlockState state1, BlockState state2) {
        return statesEqualIgnoreProperties(state1, state2);
    }
}
