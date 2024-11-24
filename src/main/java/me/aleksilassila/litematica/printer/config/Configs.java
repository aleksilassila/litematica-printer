package me.aleksilassila.litematica.printer.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import me.aleksilassila.litematica.printer.PrinterReference;

import java.util.List;

public class Configs {
    private static final String GENERIC_KEY = PrinterReference.MOD_KEY + ".config.generic";

    // Configs settings
    public static final ConfigInteger PRINTING_INTERVAL = new ConfigInteger("printingInterval", 12, 1, 40).apply(GENERIC_KEY);
    public static final ConfigDouble PRINTING_RANGE = new ConfigDouble("printingRange", 5, 2.5, 5).apply(GENERIC_KEY);
    public static final ConfigBoolean PRINT_MODE = new ConfigBoolean("printingMode", false).apply(GENERIC_KEY);
    public static final ConfigBoolean PRINT_DEBUG = new ConfigBoolean("printingDebug", false).apply(GENERIC_KEY);
    public static final ConfigBoolean REPLACE_FLUIDS_SOURCE_BLOCKS = new ConfigBoolean("replaceFluidSourceBlocks", true).apply(GENERIC_KEY);
    public static final ConfigBoolean STRIP_LOGS = new ConfigBoolean("stripLogs", true).apply(GENERIC_KEY);
    public static final ConfigBoolean INTERACT_BLOCKS = new ConfigBoolean("interactBlocks", true).apply(GENERIC_KEY);

    public static ImmutableList<IConfigBase> getConfigList() {
        List<IConfigBase> list = new java.util.ArrayList<>(fi.dy.masa.litematica.config.Configs.Generic.OPTIONS);
        list.add(PRINT_MODE);
        list.add(PRINT_DEBUG);
        list.add(PRINTING_INTERVAL);
        list.add(PRINTING_RANGE);
        list.add(REPLACE_FLUIDS_SOURCE_BLOCKS);
        list.add(STRIP_LOGS);
        list.add(INTERACT_BLOCKS);

        return ImmutableList.copyOf(list);
    }
}
