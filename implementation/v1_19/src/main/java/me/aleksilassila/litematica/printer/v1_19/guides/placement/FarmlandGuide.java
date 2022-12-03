package me.aleksilassila.litematica.printer.v1_19.guides.placement;

import me.aleksilassila.litematica.printer.v1_19.SchematicBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class FarmlandGuide extends BlockPlacementGuide {
    public static final Block[] TILLABLE_BLOCKS = new Block[]{
            Blocks.DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.COARSE_DIRT,
            Blocks.ROOTED_DIRT,
            Blocks.DIRT_PATH,
    };

    public FarmlandGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Arrays.stream(TILLABLE_BLOCKS).map(b -> getBlockItem(b.getDefaultState())).toList();
    }
}
