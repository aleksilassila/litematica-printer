package me.aleksilassila.litematica.printer.implementation;

import me.aleksilassila.litematica.printer.BlockHelper;
import net.minecraft.block.ButtonBlock;

public class BlockHelperImpl extends BlockHelper {
    static {
        interactiveBlocks.add(ButtonBlock.class);
    }
}
