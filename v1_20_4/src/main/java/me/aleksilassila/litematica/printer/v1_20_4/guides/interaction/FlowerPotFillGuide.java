package me.aleksilassila.litematica.printer.v1_20_4.guides.interaction;

import me.aleksilassila.litematica.printer.v1_20_4.SchematicBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FlowerPotFillGuide extends InteractionGuide {
    private final Block content;

    public FlowerPotFillGuide(SchematicBlockState state) {
        super(state);

        Block targetBlock = state.targetState.getBlock();
        if (targetBlock instanceof FlowerPotBlock) {
            this.content = ((FlowerPotBlock) targetBlock).getContent();
        } else {
            this.content = null;
        }
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (content == null) return false;
        if (!(currentState.getBlock() instanceof FlowerPotBlock)) return false;

        return super.canExecute(player);
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        if (content == null) return Collections.emptyList();
        else return Collections.singletonList(new ItemStack(content));
    }
}
