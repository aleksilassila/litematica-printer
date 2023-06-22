package me.aleksilassila.litematica.printer.v1_20.implementation;

import me.aleksilassila.litematica.printer.v1_20.BlockHelper;
import net.minecraft.block.ButtonBlock;

import java.util.Arrays;

public class BlockHelperImpl extends BlockHelper {
    static {
        interactiveBlocks.addAll(Arrays.asList(
                ButtonBlock.class
        ));
    }
}
