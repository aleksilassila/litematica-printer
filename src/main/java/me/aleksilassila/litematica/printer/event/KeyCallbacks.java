package me.aleksilassila.litematica.printer.event;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.config.Hotkeys;

import net.minecraft.client.MinecraftClient;

import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;

public class KeyCallbacks
{
    public static void init(MinecraftClient mc)
    {
        Hotkeys.TOGGLE_PRINTING_MODE.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(Configs.PRINT_MODE));
    }
}
