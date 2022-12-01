package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.client.network.ClientPlayerEntity;

public class SkipGuide extends BlockPlacementGuide {
    public SkipGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        return false;
    }
}
