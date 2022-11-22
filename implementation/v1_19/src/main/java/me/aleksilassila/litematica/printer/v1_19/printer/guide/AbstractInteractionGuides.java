package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.block.AnvilBlock;

import javax.annotation.Nullable;

public abstract class AbstractInteractionGuides {
    public InteractionGuide[] getGuides() {
        return new InteractionGuide[]{
                new AnvilGuide().hookClasses(AnvilBlock.class),
        };
    }

    @Nullable
    public InteractionGuide getInteractionGuide(SchematicBlockState state) {
        for (InteractionGuide guide : getGuides()) {
            if (guide.isApplicable(state)) return guide;
        }

        return null;
    }
}
