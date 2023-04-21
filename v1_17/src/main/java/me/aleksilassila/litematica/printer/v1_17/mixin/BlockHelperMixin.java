package me.aleksilassila.litematica.printer.v1_17.mixin;

import me.aleksilassila.litematica.printer.common.BlockHelper;
import net.minecraft.block.AbstractButtonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.List;

@Mixin(value = BlockHelper.class, remap = false)
public class BlockHelperMixin {
    @Shadow
    public static List<Class<?>> interactiveBlocks;

    static {
        interactiveBlocks.addAll(Arrays.asList(
                AbstractButtonBlock.class
        ));
    }
}
