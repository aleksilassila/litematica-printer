package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterUtils;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

abstract public class InteractionGuide extends PrinterUtils {
    protected final SchematicBlockState state;
    protected final BlockState currentState;
    protected final BlockState targetState;

    public InteractionGuide(SchematicBlockState state) {
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

    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(ItemStack.EMPTY);
    }
}
