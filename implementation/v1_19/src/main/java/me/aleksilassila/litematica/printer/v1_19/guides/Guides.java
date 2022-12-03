package me.aleksilassila.litematica.printer.v1_19.guides;

import me.aleksilassila.litematica.printer.v1_19.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.guides.interaction.*;
import me.aleksilassila.litematica.printer.v1_19.guides.placement.*;
import net.minecraft.block.*;
import net.minecraft.util.Pair;

import java.util.ArrayList;

public class Guides {
    protected final static ArrayList<Pair<Class<? extends Guide>, Class<? extends Block>[]>> guides = new ArrayList<>();

    @SafeVarargs
    protected static void registerGuide(Class<? extends Guide> guideClass, Class<? extends Block>... blocks) {
        guides.add(new Pair<>(guideClass, blocks));
    }

    static {
        registerGuide(SkipGuide.class, AbstractSignBlock.class, SkullBlock.class);

        registerGuide(SlabGuide.class, SlabBlock.class);
        registerGuide(WallTorchGuide.class, WallTorchBlock.class, WallRedstoneTorchBlock.class);
        registerGuide(FarmlandGuide.class, FarmlandBlock.class);
        registerGuide(TillingGuide.class, FarmlandBlock.class);
        registerGuide(RailGuesserGuide.class, RailBlock.class);

        registerGuide(PropertySpecificGuesserGuide.class,
                RepeaterBlock.class, ComparatorBlock.class, RedstoneWireBlock.class, RedstoneTorchBlock.class,
                BambooBlock.class, CactusBlock.class, SaplingBlock.class, ScaffoldingBlock.class, PointedDripstoneBlock.class,
                HorizontalConnectingBlock.class, DoorBlock.class, TrapdoorBlock.class, FenceGateBlock.class, ChestBlock.class,
                SnowBlock.class, SeaPickleBlock.class, CandleBlock.class, LeverBlock.class, EndPortalFrameBlock.class,
                CandleBlock.class, RedstoneTorchBlock.class, CampfireBlock.class, PoweredRailBlock.class, LeavesBlock.class);
        registerGuide(GuesserGuide.class);

        registerGuide(CampfireExtinguishGuide.class, CampfireBlock.class);
        registerGuide(EnderEyeGuide.class, EndPortalFrameBlock.class);
        registerGuide(CycleStateGuide.class,
                DoorBlock.class, FenceGateBlock.class, TrapdoorBlock.class,
                LeverBlock.class,
                RepeaterBlock.class, ComparatorBlock.class, NoteBlock.class);
        registerGuide(BlockReplacementGuide.class, SnowBlock.class, SeaPickleBlock.class, CandleBlock.class);
        registerGuide(LogGuide.class);
        registerGuide(LogStrippingGuide.class);
    }

    public ArrayList<Pair<Class<? extends Guide>, Class<? extends Block>[]>> getGuides() {
        return guides;
    }

    public Guide[] getInteractionGuides(SchematicBlockState state) {
        ArrayList<Pair<Class<? extends Guide>, Class<? extends Block>[]>> guides = getGuides();

        ArrayList<Guide> applicableGuides = new ArrayList<>();
        for (Pair<Class<? extends Guide>, Class<? extends Block>[]> guidePair : guides) {
            try {
                if (guidePair.getRight().length == 0) {
                    applicableGuides.add(guidePair.getLeft().getConstructor(SchematicBlockState.class).newInstance(state));
                    continue;
                }

                for (Class<? extends Block> clazz : guidePair.getRight()) {
                    if (clazz.isInstance(state.targetState.getBlock())) {
                        applicableGuides.add(guidePair.getLeft().getConstructor(SchematicBlockState.class).newInstance(state));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return applicableGuides.toArray(Guide[]::new);
    }
}
