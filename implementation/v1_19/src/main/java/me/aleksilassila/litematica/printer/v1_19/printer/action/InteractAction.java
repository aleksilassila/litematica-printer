package me.aleksilassila.litematica.printer.v1_19.printer.action;

import me.aleksilassila.litematica.printer.v1_19.interfaces.IClientPlayerInteractionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class InteractAction extends AbstractAction {
    public final BlockPos blockPos;
    public final Direction side;
    public final Vec3d hitVec;

    public InteractAction(BlockPos blockPos, Direction side, Vec3d hitVec) {
        this.blockPos = blockPos;
        this.side = side;
        this.hitVec = hitVec;
    }

    @Override
    public void send(MinecraftClient client, ClientPlayerEntity player) {
        System.out.println("Interacting with " + blockPos + " " + side + " " + hitVec);
        ((IClientPlayerInteractionManager) client.interactionManager)
                .rightClickBlock(blockPos, side, hitVec);
    }
}
