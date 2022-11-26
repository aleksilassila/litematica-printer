package me.aleksilassila.litematica.printer.v1_19.printer;

import me.aleksilassila.litematica.printer.v1_19.printer.guide.*;
import net.minecraft.block.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InteractionGuideHooker {
    static Map<InteractionGuide, Class<? extends Block>[]> guides = new HashMap<>();

    static {
        guides.put(new AnvilGuide(), new Class[]{AnvilBlock.class});
        guides.put(new SlabGuide(), new Class[]{SlabBlock.class});
        guides.put(new WallTorchGuide(), new Class[]{WallTorchBlock.class});
        guides.put(new GuesserGuide(), new Class[]{});
    }

    public Map<InteractionGuide, Class<? extends Block>[]> getGuides() {
        return guides;

    }

    public InteractionGuide[] getInteractionGuides(SchematicBlockState state) {
        Map<InteractionGuide, Class<? extends Block>[]> guides = getGuides();

        ArrayList<InteractionGuide> applicableGuides = new ArrayList<>();
        for (InteractionGuide guide : getGuides().keySet()) {
            if (guides.get(guide).length == 0) {
                applicableGuides.add(guide);
                continue;
            }

            for (Class<? extends Block> clazz : guides.get(guide)) {
                if (clazz.isInstance(state.targetState.getBlock())) {
                    applicableGuides.add(guide);
                }
            }
        }

        return applicableGuides.toArray(InteractionGuide[]::new);
    }
}
