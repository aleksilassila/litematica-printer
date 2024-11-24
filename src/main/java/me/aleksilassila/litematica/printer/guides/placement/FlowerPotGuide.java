package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class FlowerPotGuide extends GeneralPlacementGuide {
    public FlowerPotGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected @Nonnull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(new ItemStack(Items.FLOWER_POT));
    }
}
