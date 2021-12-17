package me.aleksilassila.litematica.printer.printer;

import me.aleksilassila.litematica.printer.interfaces.Implementation;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum ClickGuide {
    SNOW(SnowBlock.class),
    CANDLES(Implementation.NewBlocks.CANDLES.clazz),
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
                if (clazz != null &&
                        clazz.isInstance(requiredState.getBlock()) &&
                        clazz.isInstance(currentState.getBlock())) {
                    return guide;
                }
            }
        }

        return DEFAULT;
    }

    public static Click shouldClickBlock(BlockState requiredState, BlockState currentState) {
        switch(getGuide(requiredState, currentState)) {
            case SNOW: {
                if (currentState.get(SnowBlock.LAYERS) < requiredState.get(SnowBlock.LAYERS)) {
                    return new Click(true, Items.SNOW);
                }

                break;
            }
            case DOOR: {
                if (requiredState.get(DoorBlock.OPEN) != currentState.get(DoorBlock.OPEN))
                    return new Click(true);

                break;
            }
            case LEVER: {
                if (requiredState.get(LeverBlock.POWERED) != currentState.get(LeverBlock.POWERED))
                    return new Click(true);

                break;
            }
            case CANDLES: {
                if ((Integer) PrinterUtils.getPropertyByName(currentState, "CANDLES") < (Integer) PrinterUtils.getPropertyByName(requiredState, "CANDLES"))
                    return new Click(true, requiredState.getBlock().asItem());

                break;
            }
            case PICKLES: {
                if (currentState.get(SeaPickleBlock.PICKLES) < requiredState.get(SeaPickleBlock.PICKLES))
                    return new Click(true, Items.SEA_PICKLE);

                break;
            }
            case REPEATER: {
                if (!Objects.equals(requiredState.get(RepeaterBlock.DELAY), currentState.get(RepeaterBlock.DELAY)))
                    return new Click(true);

                break;
            }
            case COMPARATOR: {
                if (requiredState.get(ComparatorBlock.MODE) != currentState.get(ComparatorBlock.MODE))
                    return new Click(true);

                break;
            }
            case TRAPDOOR: {
                if (requiredState.get(TrapdoorBlock.OPEN) != currentState.get(TrapdoorBlock.OPEN))
                    return new Click(true);

                break;
            }
            case FENCE: {
                if (requiredState.get(FenceGateBlock.OPEN) != currentState.get(FenceGateBlock.OPEN))
                    return new Click(true);

                break;
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
