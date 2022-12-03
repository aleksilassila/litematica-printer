package me.aleksilassila.litematica.printer.v1_17.guides;

import me.aleksilassila.litematica.printer.v1_17.SchematicBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class FarmlandGuide extends BlockPlacementGuide {
    static final Block[] TILLABLE_BLOCKS = new Block[]{
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

//    @Override
//    protected boolean statesEqual(BlockState resultState, BlockState targetState) {
//        if (targetState.getBlock() instanceof FarmlandBlock) {
//            return Arrays.stream(TILLABLE_BLOCKS).anyMatch(b -> b == resultState.getBlock());
//        }
//
//        return super.statesEqual(resultState, targetState);
//    }
}
