package me.aleksilassila.litematica.printer.v1_19;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LitematicaMixinMod implements ModInitializer {

    public static Logger LOGGET = LoggerFactory.getLogger("LitematicaPrinter");

    // Config settings
    public static final ConfigInteger PRINT_INTERVAL = new ConfigInteger("printInterval", 4, 2, 20, "Print interval in game ticks. Lower values mean faster printing speed.\nIf the printer creates \"ghost blocks\", raise this value.");
    public static final ConfigInteger PRINTING_RANGE = new ConfigInteger("printingRange", 2, 1, 6, "Printing block place range\nLower values are recommended for servers.");
    //    public static final ConfigBoolean PRINT_WATER    = new ConfigBoolean("printWater",    false, "Whether the printer should place water\n source blocks or make blocks waterlogged.");
    public static final ConfigBoolean PRINT_IN_AIR = new ConfigBoolean("printInAir", true, "Whether or not the printer should place blocks without anything to build on.\nBe aware that some anti-cheat plugins might notice this.");
    public static final ConfigBoolean PRINT_MODE = new ConfigBoolean("printingMode", false, "Autobuild / print loaded selection.\nBe aware that some servers and anticheat plugins do not allow printing.");
    public static final ConfigBoolean REPLACE_FLUIDS = new ConfigBoolean("排流体", false, "开启时正常投影将无法使用！");
    public static final ConfigBoolean REPLACE_FLUIDS_UseSelectionRange = new ConfigBoolean("排流体使用投影区域选择", true, "true为投影选区范围，false为原理图范围");
    public static final ConfigBoolean STRIP_LOGS = new ConfigBoolean("stripLogs", false, "Whether or not the printer should use normal logs if stripped\nversions are not available and then strip them with an axe.");


    public static ImmutableList<IConfigBase> getConfigList() {
        List<IConfigBase> list = new java.util.ArrayList<>(Configs.Generic.OPTIONS);
        list.add(PRINT_MODE);
        list.add(PRINT_INTERVAL);
        list.add(PRINTING_RANGE);
        list.add(PRINT_IN_AIR);
        list.add(REPLACE_FLUIDS);
        list.add(REPLACE_FLUIDS_UseSelectionRange);
        list.add(STRIP_LOGS);

        return ImmutableList.copyOf(list);
    }

    // Hotkeys
    public static final ConfigHotkey PRINT = new ConfigHotkey("打印", "V", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY, "按下时打印");
    public static final ConfigHotkey TOGGLE_PRINTING_MODE = new ConfigHotkey("打印开关", "CAPS_LOCK", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY, "允许快速打开/关闭打印模式");
    public static final ConfigHotkey TOGGLE_REPLACE_FLUIDS_UseSelectionRange = new ConfigHotkey("排流体使用投影区域选择", "", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY, "允许快速打开/关闭切换模式");

    public static List<ConfigHotkey> getHotkeyList() {
        List<ConfigHotkey> list = new java.util.ArrayList<>(Hotkeys.HOTKEY_LIST);
        list.add(PRINT);
        list.add(TOGGLE_PRINTING_MODE);
        list.add(TOGGLE_REPLACE_FLUIDS_UseSelectionRange);

        return ImmutableList.copyOf(list);
    }

    @Override
    public void onInitialize() {
        TOGGLE_PRINTING_MODE.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(PRINT_MODE));
        TOGGLE_REPLACE_FLUIDS_UseSelectionRange.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(REPLACE_FLUIDS_UseSelectionRange));
    }
}
