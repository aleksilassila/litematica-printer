package me.aleksilassila.litematica.printer.v1_17.actions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public abstract class Action {
    abstract public void send(MinecraftClient client, ClientPlayerEntity player);
}
