package me.aleksilassila.litematica.printer.v1_19_3.mixin;

import me.aleksilassila.litematica.printer.common.actions.InteractAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = InteractAction.class, remap = false)
public class InteractActionMixin {
    @Overwrite
    protected void interact(MinecraftClient client, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        client.interactionManager.interactBlock(player, hand, hitResult);
        client.interactionManager.interactItem(player, hand);
    }
}
