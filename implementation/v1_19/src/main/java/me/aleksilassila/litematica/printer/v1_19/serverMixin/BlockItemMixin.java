package me.aleksilassila.litematica.printer.v1_19.serverMixin;

import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "useOnBlock", at = @At("HEAD"))
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (LitematicaMixinMod.DEBUG)
            System.out.println("[SERVER] Player interacted with block! " + context.getPlayerFacing());
    }
}
