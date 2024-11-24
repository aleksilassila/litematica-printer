package me.aleksilassila.litematica.printer.guides.interaction;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.mixin.AxeItemAccessor;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LogStrippingGuide extends InteractionGuide {
    static final Item[] AXE_ITEMS = new Item[]{
            Items.NETHERITE_AXE,
            Items.DIAMOND_AXE,
            Items.GOLDEN_AXE,
            Items.IRON_AXE,
            Items.STONE_AXE,
            Items.WOODEN_AXE
    };

    public static final Map<Block, Block> STRIPPED_BLOCKS = AxeItemAccessor.getStrippedBlocks();

    public LogStrippingGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (!Configs.STRIP_LOGS.getBooleanValue())
            return false;

        if (!super.canExecute(player))
            return false;

        Block strippingResult = STRIPPED_BLOCKS.get(currentState.getBlock());
        return strippingResult == targetState.getBlock();
    }

    @Override
    protected @Nonnull List<ItemStack> getRequiredItems() {
        return Arrays.stream(AXE_ITEMS).map(ItemStack::new).toList();
    }
}
