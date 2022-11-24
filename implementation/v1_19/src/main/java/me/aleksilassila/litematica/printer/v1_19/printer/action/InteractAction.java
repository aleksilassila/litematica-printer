package me.aleksilassila.litematica.printer.v1_19.printer.action;

import me.aleksilassila.litematica.printer.v1_19.interfaces.IClientPlayerInteractionManager;
import me.aleksilassila.litematica.printer.v1_19.mixin.ItemPlacementAccessor;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterPlacementContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class InteractAction extends AbstractAction {
//    public final BlockPos blockPos;
//    public final Direction side;
//    public final Vec3d hitVec;

    public final PrinterPlacementContext context;

//    public InteractAction(BlockPos blockPos, Direction side, Vec3d hitVec) {
//        this.blockPos = blockPos;
//        this.side = side;
//        this.hitVec = hitVec;
//    }

    public InteractAction(PrinterPlacementContext context) {
        this.context = context;
    }

    @Override
    public void send(MinecraftClient client, ClientPlayerEntity player) {
//        ((IClientPlayerInteractionManager) client.interactionManager)
//                .rightClickBlock(context);

        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND,
                context.hitResult);
        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);

        System.out.println("InteractAction.send: Blockpos: " + context.getBlockPos() + " Side: " + context.getSide() + " HitPos: " + context.getHitPos());

        //        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND,
//                context.hitResult);
//        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
    }

    @Override
    public String toString() {
        return "InteractAction{" +
                "context=" + context +
                '}';
    }
}
