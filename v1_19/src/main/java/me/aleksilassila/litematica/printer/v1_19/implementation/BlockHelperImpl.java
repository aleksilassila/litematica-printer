package me.aleksilassila.litematica.printer.v1_19.implementation;

import me.aleksilassila.litematica.printer.v1_19.BlockHelper;

public class BlockHelperImpl extends BlockHelper {
    @Override
    public Class<?>[] getInteractiveBlocks() {
        return BlockHelper.interactiveBlocks;
    }
}
