package me.aleksilassila.litematica.printer.v1_19_4.guides;

import me.aleksilassila.litematica.printer.v1_19_4.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19_4.actions.Action;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkipGuide extends Guide {
    public SkipGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public boolean skipOtherGuides() {
        return true;
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        return false;
    }

    @Override
    public @NotNull List<Action> execute(ClientPlayerEntity player) {
        return new ArrayList<>();
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(ItemStack.EMPTY);
    }
}
