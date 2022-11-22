package me.aleksilassila.litematica.printer.v1_19.printer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class CustomItemUsageContext extends ItemUsageContext {
    public CustomItemUsageContext(PlayerEntity player, Hand hand, BlockHitResult hit) {
        super(player, hand, hit);
    }


}
