package me.aleksilassila.litematica.printer.v1_17.guides;

import me.aleksilassila.litematica.printer.v1_17.SchematicBlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ExtinguishCampfiresGuide extends AbstractClickGuide {
    static final Item[] SHOVEL_ITEMS = new Item[]{
            Items.NETHERITE_SHOVEL,
            Items.DIAMOND_SHOVEL,
            Items.GOLDEN_SHOVEL,
            Items.IRON_SHOVEL,
            Items.STONE_SHOVEL,
            Items.WOODEN_SHOVEL
    };

    public ExtinguishCampfiresGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (!super.canExecute(player)) return false;

        if (currentState.getBlock() instanceof CampfireBlock && targetState.getBlock() instanceof CampfireBlock) {
            return currentState.get(CampfireBlock.LIT) && !targetState.get(CampfireBlock.LIT);
        }

        return false;
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Arrays.stream(SHOVEL_ITEMS).map(ItemStack::new).toList();
    }
}
