package me.aleksilassila.litematica.printer.common;

import me.aleksilassila.litematica.printer.common.actions.InteractAction;

public interface IModImplementation {
    InteractAction createInteractAction(PrinterPlacementContext ctx);
}
