package me.aleksilassila.litematica.printer.implementation;

import me.aleksilassila.litematica.printer.BlockHelper;
import net.minecraft.block.ButtonBlock;

import java.util.Arrays;

public class BlockHelperImpl extends BlockHelper {
    static {
        interactiveBlocks.addAll(Arrays.asList(
                ButtonBlock.class
        ));
    }
}
