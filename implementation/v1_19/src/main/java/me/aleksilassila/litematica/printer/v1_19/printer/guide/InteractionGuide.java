package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterUtils;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.*;

abstract public class InteractionGuide extends PrinterUtils {
    protected boolean playerHasRightItem(ClientPlayerEntity player, SchematicBlockState state) {
        return getStackSlot(player, state) != -1;
    }

    protected int getStackSlot(ClientPlayerEntity player, SchematicBlockState state) {
        List<ItemStack> requiredItems = getRequiredItems(state);

        for (ItemStack requiredItem : requiredItems) {
            int slot = Implementation.getInventory(player).getSlotWithStack(requiredItem);
            if (slot > -1) return slot;
        }

        return -1;
    }

    public boolean canExecute(ClientPlayerEntity player, SchematicBlockState state) {
        if (!playerHasRightItem(player, state)) return false;

        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        return !targetState.equals(currentState);
    }

    abstract public List<AbstractAction> execute(ClientPlayerEntity player, SchematicBlockState state);

    protected List<ItemStack> getRequiredItems(SchematicBlockState state) {
        return Collections.singletonList(ItemStack.EMPTY);
    }
}
