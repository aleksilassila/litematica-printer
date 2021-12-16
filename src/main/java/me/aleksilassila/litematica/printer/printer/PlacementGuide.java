package me.aleksilassila.litematica.printer.printer;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum PlacementGuide {
    ROD(RodBlock.class),
    WALLTORCH(WallTorchBlock.class),
    TORCH(TorchBlock.class),
    SLAB(SlabBlock.class),
    STAIR(StairsBlock.class),
    TRAPDOOR(TrapdoorBlock.class),
    PILLAR(PillarBlock.class),
    ANVIL(AnvilBlock.class),
    HOPPER(HopperBlock.class),
    GRINDSTONE(GrindstoneBlock.class),
    GATE(FenceGateBlock.class),
    CAMPFIRE(CampfireBlock.class),
    SHULKER(ShulkerBoxBlock.class),
    BED(BedBlock.class),
    DEFAULT;

    private final Class<?>[] matchClasses;

    PlacementGuide(Class<?> ... classes) {
        matchClasses = classes;
    }

    private static PlacementGuide getGuide(BlockState requiredState) {
        for (PlacementGuide guide : PlacementGuide.values()) {
            for (Class<?> clazz : guide.matchClasses) {
                if (clazz.isInstance(requiredState.getBlock())) {
                    return guide;
                }
            }
        }

        return DEFAULT;
    }

    public static Placement getPlacement(BlockState requiredState) {
        switch (getGuide(requiredState)) {
            case ROD -> {
                return new Placement(requiredState.get(RodBlock.FACING),
                        null,
                        null);
            }
            case WALLTORCH -> { // FIXME check if the wall exists?
                return new Placement(requiredState.get(WallTorchBlock.FACING).getOpposite(),
                        null,
                        null);
            }
            case SLAB -> {
                Direction half = requiredState.get(SlabBlock.TYPE) == SlabType.BOTTOM ? Direction.DOWN : Direction.UP;
                return new Placement(half,
                        half,
                        null);
            }
            case STAIR -> {
                return new Placement(requiredState.get(StairsBlock.FACING), // FIXME before shipping
                        getHalf(requiredState.get(StairsBlock.HALF)),
                        requiredState.get(StairsBlock.FACING));
            }
            case TRAPDOOR -> {
                return new Placement(getHalf(requiredState.get(TrapdoorBlock.HALF)),
                        null, //getHalf(requiredState.get(TrapdoorBlock.HALF)),
                        requiredState.get(StairsBlock.FACING).getOpposite());
            }
            case PILLAR -> {
                return new Placement(axisToDirection(requiredState.get(PillarBlock.AXIS)),
                        null,
                        null, true);
            }
            case ANVIL -> {
                return new Placement(null,
                        null,
                        requiredState.get(AnvilBlock.FACING).rotateCounterclockwise(Direction.Axis.X)); // FIXME test
            }
            case HOPPER -> {
                return new Placement(requiredState.get(HopperBlock.FACING),
                        null,
                        null);
            }
            case GRINDSTONE -> {
                Direction look = requiredState.get(GrindstoneBlock.FACING);
                Direction side = switch (requiredState.get(GrindstoneBlock.FACE)) {
                    case FLOOR -> Direction.DOWN;
                    case WALL -> requiredState.get(GrindstoneBlock.FACING).getOpposite();
                    case CEILING -> Direction.UP;
                };

                if (requiredState.get(GrindstoneBlock.FACE) == WallMountLocation.WALL) {
                    look = look.getOpposite();
                }

                return new Placement(side, // FIXME test
                        null,
                        look);
            }
            case GATE, CAMPFIRE -> {
                return new Placement(null,
                        null,
                        (Direction) getPropertyByName(requiredState, "FACING"));
            }
            case SHULKER -> {
                return new Placement(requiredState.get(ShulkerBoxBlock.FACING).getOpposite(),
                        null,
                        null);
            }
            case BED -> {
                if (requiredState.get(BedBlock.PART) != BedPart.FOOT) {
                    return new Placement();
                } else {
                    return new Placement(null, null, requiredState.get(BedBlock.FACING));
                }
            }
            default -> { // Try to guess how the rest of the blocks are placed.
                Direction look = null;

                for (Property<?> prop : requiredState.getProperties()) {
                    if (prop instanceof DirectionProperty && prop.getName().equalsIgnoreCase("FACING")) {
                        look = ((Direction) requiredState.get(prop)).getOpposite();
                    }
                }

                return new Placement(null, null, look);
            }
        }
    }

    private static Direction getHalf(BlockHalf half) {
        return half == BlockHalf.TOP ? Direction.UP : Direction.DOWN;
    }

    private static Direction axisToDirection(Direction.Axis axis) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == axis) return direction;
        }

        return Direction.DOWN;
    }

    private static Comparable<?> getPropertyByName(BlockState state, String name) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equalsIgnoreCase(name)) {
                return state.get(prop);
            }
        }

        return null;
    }

    public static class Placement {
        @Nullable
        public final Direction side;
        @Nullable
        public final Direction half;
        @Nullable
        public final Direction look;

        final boolean sideIsAxis;
        boolean skip;

        public Placement(@Nullable Direction side, @Nullable Direction half, @Nullable Direction look, boolean sideIsAxis) {
            this.side = side;
            this.half = half;
            this.look = look;

            this.sideIsAxis = sideIsAxis;
            this.skip = false;
        }

        public Placement(@Nullable Direction side, @Nullable Direction half, @Nullable Direction look) {
            this(side, half, look, false);
        }

        public Placement() {
            this(null, null, null, false);
            this.skip = true;
        }
    }
//
//    public enum PlacementSide {
//        FORWARD,
//        BACKWARDS,
//        RIGHT,
//        LEFT,
//        UP,
//        DOWN,
//        UNDEFINED;
//    }
//
//    private enum PlacementHalf {
//        TOP,
//        BOTTOM,
//        UNDEFINED;
//    }
//
//    public enum PlacementLookDirection {
//        FORWARD,
//        BACKWARDS,
//        RIGHT,
//        LEFT,
//        UP,
//        DOWN,
//        UNDEFINED;
//    }
}
/*

PlacementGuide.getPlacement(requiredState)

-> requiredState
Block class, State
return where to click

 */