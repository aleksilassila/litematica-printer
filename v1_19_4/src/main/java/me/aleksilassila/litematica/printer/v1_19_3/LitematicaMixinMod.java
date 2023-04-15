package me.aleksilassila.litematica.printer.v1_19_4;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import net.fabricmc.api.ModInitializer;

import java.util.List;

public class LitematicaMixinMod implements ModInitializer {

    public static Printer printer;
    public static boolean DEBUG = false;
    // Config settings
    public static final ConfigInteger PRINTING_INTERVAL = new ConfigInteger("printingInterval", 12, 1, 40, "Printing interval. Lower values mean faster printing speed.\nIf the printer creates \"ghost blocks\" or blocks are facing the wrong way, raise this value.");
    public static final ConfigDouble PRINTING_RANGE = new ConfigDouble("printingRange", 5, 2.5, 5, "Printing block place range\nLower values are recommended for servers.");
    //    public static final ConfigBoolean PRINT_WATER    = new ConfigBoolean("printWater",    false, "Whether the printer should place water\n source blocks or make blocks waterlogged.");
//    public static final ConfigBoolean PRINT_IN_AIR = new ConfigBoolean("printInAir", true, "Whether or not the printer should place blocks without anything to build on.\nBe aware that some anti-cheat plugins might notice this.");
    public static final ConfigBoolean PRINT_MODE = new ConfigBoolean("printingMode", false, "Autobuild / print loaded selection.\nBe aware that some servers and anticheat plugins do not allow printing.");
    public static final ConfigBoolean REPLACE_FLUIDS_SOURCE_BLOCKS = new ConfigBoolean("replaceFluidSourceBlocks", true, "Whether or not fluid source blocks should be replaced by the printer.");
    public static final ConfigBoolean STRIP_LOGS = new ConfigBoolean("stripLogs", true, "Whether or not the printer should use normal logs if stripped\nversions are not available and then strip them with an axe.");

    public static ImmutableList<IConfigBase> getConfigList() {
        List<IConfigBase> list = new java.util.ArrayList<>(Configs.Generic.OPTIONS);
        list.add(PRINT_MODE);
        list.add(PRINTING_INTERVAL);
        list.add(PRINTING_RANGE);
//        list.add(PRINT_IN_AIR);
        list.add(REPLACE_FLUIDS_SOURCE_BLOCKS);
        list.add(STRIP_LOGS);

        return ImmutableList.copyOf(list);
    }

    // Hotkeys
    public static final ConfigHotkey PRINT = new ConfigHotkey("print", "V", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY, "Prints while pressed");
    public static final ConfigHotkey TOGGLE_PRINTING_MODE = new ConfigHotkey("togglePrintingMode", "CAPS_LOCK", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY, "Allows quickly toggling on/off Printing mode");

    public static List<ConfigHotkey> getHotkeyList() {
        List<ConfigHotkey> list = new java.util.ArrayList<>(Hotkeys.HOTKEY_LIST);
        list.add(PRINT);
        list.add(TOGGLE_PRINTING_MODE);

        return ImmutableList.copyOf(list);
    }

    @Override
    public void onInitialize() {
        TOGGLE_PRINTING_MODE.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(PRINT_MODE));
    }
}
