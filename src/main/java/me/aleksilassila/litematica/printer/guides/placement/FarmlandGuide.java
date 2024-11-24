package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class FarmlandGuide extends GeneralPlacementGuide {
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
    protected @Nonnull List<ItemStack> getRequiredItems() {
        return Arrays.stream(TILLABLE_BLOCKS).map(b -> getBlockItem(b.getDefaultState())).toList();
    }
}
