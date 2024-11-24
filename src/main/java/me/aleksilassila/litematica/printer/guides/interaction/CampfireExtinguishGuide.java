package me.aleksilassila.litematica.printer.guides.interaction;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
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
        if (!super.canExecute(player))
            return false;

        return (currentState.getBlock() instanceof CampfireBlock) && !shouldBeLit && isLit;
    }

    @Override
    protected @Nonnull List<ItemStack> getRequiredItems() {
        return Arrays.stream(SHOVEL_ITEMS).map(ItemStack::new).toList();
    }
}
