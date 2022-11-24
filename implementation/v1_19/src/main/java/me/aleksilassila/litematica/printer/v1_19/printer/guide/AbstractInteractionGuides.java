package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AbstractInteractionGuides {
    static Map<InteractionGuide, Class<? extends Block>[]> guides = new HashMap<>();

    static {
        guides.put(new AnvilGuide(), new Class[]{AnvilBlock.class});
    }

    public Map<InteractionGuide, Class<? extends Block>[]> getGuides() {
        return guides;

    }

    public InteractionGuide[] getInteractionGuides(SchematicBlockState state) {
        Map<InteractionGuide, Class<? extends Block>[]> guides = getGuides();

        ArrayList<InteractionGuide> applicableGuides = new ArrayList<>();
        for (InteractionGuide guide : getGuides().keySet()) {
            for (Class<? extends Block> clazz : guides.get(guide)) {
                if (clazz.isInstance(state.targetState.getBlock())) {
                    applicableGuides.add(guide);
                }
            }
        }

        return applicableGuides.toArray(InteractionGuide[]::new);
    }
}
