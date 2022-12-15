package me.aleksilassila.litematica.printer.v1_18.implementation;

import me.aleksilassila.litematica.printer.v1_18.BlockHelper;
import net.minecraft.block.AbstractButtonBlock;

import java.util.Arrays;

public class BlockHelperImpl extends BlockHelper {
    static {
        interactiveBlocks.addAll(Arrays.asList(
                AbstractButtonBlock.class
        ));
    }
}
