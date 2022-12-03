package me.aleksilassila.litematica.printer.v1_19;

import me.aleksilassila.litematica.printer.v1_19.guides.*;
import net.minecraft.block.*;
import net.minecraft.util.Pair;

import java.util.ArrayList;

public class Guides {
    protected final static ArrayList<Pair<Class<? extends AbstractGuide>, Class<? extends Block>[]>> guides = new ArrayList<>();

    @SafeVarargs
    protected static void registerGuide(Class<? extends AbstractGuide> guideClass, Class<? extends Block>... blocks) {
        guides.add(new Pair<>(guideClass, blocks));
    }

    static {
        registerGuide(SkipGuide.class, AbstractSignBlock.class, SkullBlock.class);

        registerGuide(SlabGuide.class, SlabBlock.class);
        registerGuide(WallTorchGuide.class, WallTorchBlock.class, WallRedstoneTorchBlock.class);
        registerGuide(FarmlandGuide.class, FarmlandBlock.class);
        registerGuide(FarmlandClickGuide.class, FarmlandBlock.class);
        registerGuide(RailGuesserGuide.class, RailBlock.class);

        registerGuide(MultiPropertyGuesserGuide.class,
                RepeaterBlock.class, ComparatorBlock.class, RedstoneWireBlock.class, RedstoneTorchBlock.class,
                BambooBlock.class, CactusBlock.class, SaplingBlock.class, ScaffoldingBlock.class, PointedDripstoneBlock.class,
                HorizontalConnectingBlock.class, DoorBlock.class, TrapdoorBlock.class, FenceGateBlock.class, ChestBlock.class,
                SnowBlock.class, SeaPickleBlock.class, CandleBlock.class, LeverBlock.class, EndPortalFrameBlock.class,
                CandleBlock.class, RedstoneTorchBlock.class, CampfireBlock.class, PoweredRailBlock.class, LeavesBlock.class);
        registerGuide(GuesserGuide.class);

        registerGuide(ExtinguishCampfiresGuide.class, CampfireBlock.class);
        registerGuide(EnderEyeClickGuide.class, EndPortalFrameBlock.class);
        registerGuide(CycleClickGuide.class,
                DoorBlock.class, FenceGateBlock.class, TrapdoorBlock.class,
                LeverBlock.class,
                RepeaterBlock.class, ComparatorBlock.class, NoteBlock.class);
        registerGuide(BlockReplaceGuide.class, SnowBlock.class, SeaPickleBlock.class, CandleBlock.class);
        registerGuide(LogStrippingPlacementGuide.class);
        registerGuide(LogStrippingGuide.class);
    }

    public ArrayList<Pair<Class<? extends AbstractGuide>, Class<? extends Block>[]>> getGuides() {
        return guides;
    }

    public AbstractGuide[] getInteractionGuides(SchematicBlockState state) {
        ArrayList<Pair<Class<? extends AbstractGuide>, Class<? extends Block>[]>> guides = getGuides();

        ArrayList<AbstractGuide> applicableGuides = new ArrayList<>();
        for (Pair<Class<? extends AbstractGuide>, Class<? extends Block>[]> guidePair : guides) {
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

        return applicableGuides.toArray(AbstractGuide[]::new);
    }
}
