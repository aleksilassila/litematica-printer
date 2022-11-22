package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterUtils;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

abstract public class InteractionGuide extends PrinterUtils {
    private Class<? extends Block>[] classes;

    @SafeVarargs
    public final InteractionGuide hookClasses(Class<? extends Block>... classes) {
        this.classes = classes;
        return this;
    }

    public boolean isApplicable(SchematicBlockState state) {
        if (classes.length == 0) return true;

        for (Class<? extends Block> clazz : classes) {
            if (clazz.isInstance(state.targetState.getBlock())) return true;
        }

        return false;
    }

    abstract public boolean canExecute(ClientPlayerEntity player, SchematicBlockState state);

    abstract public List<AbstractAction> getActions(ClientPlayerEntity player, SchematicBlockState state);

    public List<ItemStack> getRequiredItems(SchematicBlockState state) {
        return Collections.singletonList(state.targetState.getBlock().asItem().getDefaultStack());
    }


    public boolean hasItems(ClientPlayerEntity player, SchematicBlockState state) {
        List<ItemStack> requiredItems = getRequiredItems(state);

        for (ItemStack requiredItem : requiredItems) {
            if (Implementation.getInventory(player).contains(requiredItem)) return true;
        }

        return false;
    }
}
