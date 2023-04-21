package me.aleksilassila.litematica.printer.v1_18.mixin;

import me.aleksilassila.litematica.printer.common.PrinterPlacementContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PrinterPlacementContext.class, remap = false)
public class PrinterPlacementContextMixin extends ItemPlacementContext {

    @Shadow
    public @Nullable Direction lookDirection;

    public PrinterPlacementContextMixin(PlayerEntity player, Hand hand, ItemStack stack, BlockHitResult hitResult) {
        super(player, hand, stack, hitResult);
    }

    @Override
    public Direction getPlayerFacing() {
        if (lookDirection == null || !lookDirection.getAxis().isHorizontal()) return super.getPlayerFacing();

        return lookDirection;
    }
}
