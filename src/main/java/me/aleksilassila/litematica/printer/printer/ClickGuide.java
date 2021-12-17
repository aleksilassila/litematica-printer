package me.aleksilassila.litematica.printer.printer;

import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum ClickGuide {
    SNOW(SnowBlock.class),
    CANDLES(AbstractCandleBlock.class),
    LEVER(LeverBlock.class),
    REPEATER(RepeaterBlock.class),
    COMPARATOR(ComparatorBlock.class),
    TRAPDOOR(TrapdoorBlock.class),
    DOOR(DoorBlock.class),
    PICKLES(SeaPickleBlock.class),
    FENCE(FenceGateBlock.class),
    DEFAULT;

    private final Class<?>[] matchClasses;

    ClickGuide(Class<?> ... classes) {
        matchClasses = classes;
    }

    private static ClickGuide getGuide(BlockState requiredState, BlockState currentState) {
        for (ClickGuide guide : ClickGuide.values()) {
            for (Class<?> clazz : guide.matchClasses) {
                if (clazz.isInstance(requiredState.getBlock()) &&
                        clazz.isInstance(currentState.getBlock())) {
                    return guide;
                }
            }
        }

        return DEFAULT;
    }

    public static Click shouldClickBlock(BlockState requiredState, BlockState currentState) {
        switch(getGuide(requiredState, currentState)) {
            case SNOW -> {
                if (currentState.get(SnowBlock.LAYERS) < requiredState.get(SnowBlock.LAYERS)) {
                    return new Click(true, Items.SNOW);
                }
            }
            case DOOR -> {
                if (requiredState.get(DoorBlock.OPEN) != currentState.get(DoorBlock.OPEN))
                    return new Click(true);
            }
            case LEVER -> {
                System.out.println("Caught lever, required: " + requiredState.get(LeverBlock.POWERED) + ", current: " + currentState.get(LeverBlock.POWERED));
                if (requiredState.get(LeverBlock.POWERED) != currentState.get(LeverBlock.POWERED))
                    return new Click(true);
            }
            case CANDLES -> {
                if (currentState.get(CandleBlock.CANDLES) < requiredState.get(CandleBlock.CANDLES))
                    return new Click(true, requiredState.getBlock().asItem());
            }
            case PICKLES -> {
                if (currentState.get(SeaPickleBlock.PICKLES) < requiredState.get(SeaPickleBlock.PICKLES))
                    return new Click(true, Items.SEA_PICKLE);
            }
            case REPEATER -> {
                if (!Objects.equals(requiredState.get(RepeaterBlock.DELAY), currentState.get(RepeaterBlock.DELAY)))
                    return new Click(true);
            }
            case COMPARATOR -> {
                if (requiredState.get(ComparatorBlock.MODE) != currentState.get(ComparatorBlock.MODE))
                    return new Click(true);
            }
            case TRAPDOOR -> {
                if (requiredState.get(TrapdoorBlock.OPEN) != currentState.get(TrapdoorBlock.OPEN))
                    return new Click(true);
            }
            case FENCE -> {
                if (requiredState.get(FenceGateBlock.OPEN) != currentState.get(FenceGateBlock.OPEN))
                    return new Click(true);
            }
        }

        return new Click();
    }

    public static class Click {
        public final boolean click;
        @Nullable
        public final Item item;

        public Click(boolean click, @Nullable Item item) {
            this.click = click;
            this.item = item;
        }

        public Click(boolean click) {
            this(click, null);
        }

        public Click() {
            this(false, null);
        }
    }
}
