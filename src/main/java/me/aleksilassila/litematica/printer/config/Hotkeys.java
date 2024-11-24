package me.aleksilassila.litematica.printer.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import me.aleksilassila.litematica.printer.PrinterReference;

import java.util.List;

public class Hotkeys {
    private static final String HOTKEY_KEY = PrinterReference.MOD_KEY + ".config.hotkeys";

    // Hotkeys
    public static final ConfigHotkey PRINT = new ConfigHotkey("print", "V", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY).apply(HOTKEY_KEY);
    public static final ConfigHotkey TOGGLE_PRINTING_MODE = new ConfigHotkey("togglePrintingMode", "CAPS_LOCK", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY).apply(HOTKEY_KEY);

    public static List<ConfigHotkey> getHotkeyList() {
        List<ConfigHotkey> list = new java.util.ArrayList<>(fi.dy.masa.litematica.config.Hotkeys.HOTKEY_LIST);
        list.add(PRINT);
        list.add(TOGGLE_PRINTING_MODE);

        return ImmutableList.copyOf(list);
    }
}
