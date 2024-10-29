package me.aleksilassila.litematica.printer.mixin;

import java.util.List;
import me.aleksilassila.litematica.printer.config.Hotkeys;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.litematica.event.InputHandler;

@Mixin(value = InputHandler.class, remap = false)
public class InputHandlerMixin
{
    @Redirect(method = "addHotkeys", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Hotkeys;HOTKEY_LIST:Ljava/util/List;"))
    private List<ConfigHotkey> moreHotkeys()
    {
        return Hotkeys.getHotkeyList();
    }

    @Redirect(method = "addKeysToMap", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Hotkeys;HOTKEY_LIST:Ljava/util/List;"))
    private List<ConfigHotkey> moreeHotkeys()
    {
        return Hotkeys.getHotkeyList();
    }
}
