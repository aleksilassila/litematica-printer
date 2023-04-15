package me.aleksilassila.litematica.printer.v1_19_4.guides.interaction;

import me.aleksilassila.litematica.printer.v1_19_4.SchematicBlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CampfireExtinguishGuide extends InteractionGuide {
    boolean shouldBeLit;
    boolean isLit;

    public CampfireExtinguishGuide(SchematicBlockState state) {
        super(state);

        shouldBeLit = getProperty(targetState, CampfireBlock.LIT).orElse(false);
        isLit = getProperty(currentState, CampfireBlock.LIT).orElse(false);
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (!super.canExecute(player)) return false;

        return (currentState.getBlock() instanceof CampfireBlock) && !shouldBeLit && isLit;
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Arrays.stream(SHOVEL_ITEMS).map(ItemStack::new).toList();
    }
}
