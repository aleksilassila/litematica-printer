package me.aleksilassila.litematica.printer.guides;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.actions.Action;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
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
    public @Nonnull List<Action> execute(ClientPlayerEntity player) {
        return new ArrayList<>();
    }

    @Override
    protected @Nonnull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(ItemStack.EMPTY);
    }
}
