package me.aleksilassila.litematica.printer.common.guides.placement;

import me.aleksilassila.litematica.printer.common.SchematicBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FlowerPotGuide extends GeneralPlacementGuide {
    public FlowerPotGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(new ItemStack(Items.FLOWER_POT));
    }
}
