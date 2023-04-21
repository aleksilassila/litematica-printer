package me.aleksilassila.litematica.printer.common.actions;

import me.aleksilassila.litematica.printer.common.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.common.LitematicaMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class InteractAction extends Action {
    public final PrinterPlacementContext context;

    public InteractAction(PrinterPlacementContext context) {
        this.context = context;
    }

    /**
     * This method is overridden in InteractActionMixin of each version implementation.
     */
    protected void interact(MinecraftClient client, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        System.out.println("InteractAction.interact was not overridden!");
    }

    @Override
    public void send(MinecraftClient client, ClientPlayerEntity player) {
        interact(client, player, Hand.MAIN_HAND, context.hitResult);

        if (LitematicaMod.DEBUG)
            System.out.println("InteractAction.send: Blockpos: " + context.getBlockPos() + " Side: " + context.getSide() + " HitPos: " + context.getHitPos());
    }

    @Override
    public String toString() {
        return "InteractAction{" +
                "context=" + context +
                '}';
    }
}
