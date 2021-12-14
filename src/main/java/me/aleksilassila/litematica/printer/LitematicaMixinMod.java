package me.aleksilassila.litematica.printer;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;
import net.fabricmc.api.ModInitializer;

import java.util.List;

public class LitematicaMixinMod implements ModInitializer {

	// Config settings
	public static final ConfigInteger PRINT_INTERVAL = new ConfigInteger( "printInterval", 4,   2, 20, "Print interval in game ticks. Lower values mean faster printing speed.\nIf the printer creates \"ghost blocks\", raise this value.");
    public static final ConfigInteger PRINTING_RANGE = new ConfigInteger("printingRange", 2,     1,   6,   "Printing block place range\nLower values are recommended for servers.");
//    public static final ConfigBoolean PRINT_WATER    = new ConfigBoolean("printWater",    false, "Whether or not the printer should place water\n source blocks or make blocks waterlogged.");
    public static final ConfigBoolean PRINT_IN_AIR = new ConfigBoolean("printInAir",    false, "Whether or not the printer should place blocks without anything to build on.\nBe aware that some anti-cheat plugins might notice this.");
    public static final ConfigBoolean PRINT_MODE 	 = new ConfigBoolean("printingMode",  false, "Autobuild / print loaded selection.\nBe aware that some servers and anticheat plugins do not allow printing.");
    public static final ConfigBoolean REPLACE_FLUIDS = new ConfigBoolean("replaceFluids", false, "Whether or not fluid source blocks should be replaced by the printer.");

	public static ImmutableList<IConfigBase> getConfigList() {
		List<IConfigBase> list = new java.util.ArrayList<>(List.copyOf(Configs.Generic.OPTIONS));
		list.add(PRINT_INTERVAL);
		list.add(PRINTING_RANGE);
		list.add(PRINT_IN_AIR);
		list.add(PRINT_MODE);
		list.add(REPLACE_FLUIDS);

		return ImmutableList.copyOf(list);
	}
	
	// Hotkeys
	public static final ConfigHotkey TOGGLE_PRINTING_MODE = new ConfigHotkey("togglePrintingMode", "M,O", "Allows quickly toggling on/off Printing mode");

	public static List<ConfigHotkey> getHotkeyList() {
		List<ConfigHotkey> list = new java.util.ArrayList<>(List.copyOf(Hotkeys.HOTKEY_LIST));
		list.add(TOGGLE_PRINTING_MODE);

		return ImmutableList.copyOf(list);
	}

	@Override
	public void onInitialize() {
		TOGGLE_PRINTING_MODE.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(PRINT_MODE));
	}
}
