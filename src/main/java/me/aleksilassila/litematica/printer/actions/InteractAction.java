package me.aleksilassila.litematica.printer.actions;

import me.aleksilassila.litematica.printer.Printer;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

abstract public class InteractAction extends Action {
    public final PrinterPlacementContext context;

    public InteractAction(PrinterPlacementContext context) {
        this.context = context;
    }

    protected abstract void interact(MinecraftClient client, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);

    @Override
    public void send(MinecraftClient client, ClientPlayerEntity player) {
        interact(client, player, Hand.MAIN_HAND, context.hitResult);
        Printer.printDebug("InteractAction.send: Blockpos: {} Side: {} HitPos: {}", context.getBlockPos(), context.getSide(), context.getHitPos());
    }

    @Override
    public String toString() {
        return "InteractAction{" +
                "context=" + context +
                '}';
    }
}
