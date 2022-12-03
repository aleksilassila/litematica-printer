package me.aleksilassila.litematica.printer.v1_17.guides;

import me.aleksilassila.litematica.printer.v1_17.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_17.actions.AbstractAction;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkipGuide extends AbstractGuide {
    public SkipGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public boolean shouldSkip() {
        return true;
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        return false;
    }

    @Override
    public List<AbstractAction> execute(ClientPlayerEntity player) {
        return new ArrayList<>();
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(ItemStack.EMPTY);
    }
}
