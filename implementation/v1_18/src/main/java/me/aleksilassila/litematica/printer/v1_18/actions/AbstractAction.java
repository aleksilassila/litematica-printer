package me.aleksilassila.litematica.printer.v1_18.actions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Direction;

public abstract class AbstractAction {
    public Direction lockedLookDirection() {
        return null;
    }

    abstract public void send(MinecraftClient client, ClientPlayerEntity player);
}
