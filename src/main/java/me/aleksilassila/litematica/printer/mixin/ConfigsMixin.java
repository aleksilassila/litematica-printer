package me.aleksilassila.litematica.printer.mixin;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import me.aleksilassila.litematica.printer.config.Hotkeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = Configs.class, remap = false)
public class ConfigsMixin {
    @Redirect(method = "loadFromFile", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Configs$Generic;OPTIONS:Lcom/google/common/collect/ImmutableList;"))
    private static ImmutableList<IConfigBase> moreOptions() {
        return me.aleksilassila.litematica.printer.config.Configs.getConfigList();
    }

    @Redirect(method = "saveToFile", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Configs$Generic;OPTIONS:Lcom/google/common/collect/ImmutableList;"))
    private static ImmutableList<IConfigBase> moreeOptions() {
        return me.aleksilassila.litematica.printer.config.Configs.getConfigList();
    }

    @Redirect(method = "loadFromFile", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Hotkeys;HOTKEY_LIST:Ljava/util/List;"))
    private static List<ConfigHotkey> moreHotkeys() {
        return Hotkeys.getHotkeyList();
    }

    @Redirect(method = "saveToFile", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Hotkeys;HOTKEY_LIST:Ljava/util/List;"))
    private static List<ConfigHotkey> moreeHotkeys() {
        return Hotkeys.getHotkeyList();
    }
}
