package me.aleksilassila.litematica.printer;

import me.aleksilassila.litematica.printer.event.KeyCallbacks;

import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.ModInitializer;

public class LitematicaMixinMod implements ModInitializer
{
    public static Printer printer;

    @Override
    public void onInitialize()
    {
        KeyCallbacks.init(MinecraftClient.getInstance());
        Printer.logger.info("{} initialized.", PrinterReference.MOD_STRING);
    }
}
