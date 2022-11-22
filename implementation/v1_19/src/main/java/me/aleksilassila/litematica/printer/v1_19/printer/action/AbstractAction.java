package me.aleksilassila.litematica.printer.v1_19.printer.action;

import me.aleksilassila.litematica.printer.v1_19.printer.PrinterUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Direction;

public abstract class AbstractAction extends PrinterUtils {
    public Direction lockedLookDirection() {
        return null;
    }

    abstract public void send(MinecraftClient client, ClientPlayerEntity player);
}
