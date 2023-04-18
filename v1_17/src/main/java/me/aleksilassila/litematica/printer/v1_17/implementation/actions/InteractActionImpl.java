package me.aleksilassila.litematica.printer.v1_17.implementation.actions;

import me.aleksilassila.litematica.printer.v1_17.implementation.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_17.actions.InteractAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class InteractActionImpl extends InteractAction {
    public InteractActionImpl(PrinterPlacementContext context) {
        super(context);
    }

    @Override
    protected void interact(MinecraftClient client, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        client.interactionManager.interactBlock(player, client.world, hand, hitResult);
        client.interactionManager.interactItem(player, client.world, hand);
    }
}
