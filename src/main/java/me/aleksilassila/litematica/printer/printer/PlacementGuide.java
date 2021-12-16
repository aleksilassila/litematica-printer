package me.aleksilassila.litematica.printer.printer;

import net.minecraft.block.*;
import net.minecraft.block.enums.*;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
    WALLMOUNTED(LeverBlock.class, AbstractButtonBlock.class),
//    GRINDSTONE(GrindstoneBlock.class),
    GATE(FenceGateBlock.class),
    CAMPFIRE(CampfireBlock.class),
    SHULKER(ShulkerBoxBlock.class),
    BED(BedBlock.class),
    BELL(BellBlock.class),
    AMETHYST(AmethystClusterBlock.class),
    DOOR(DoorBlock.class),
    COCOA(CocoaBlock.class),
    SKIP(SkullBlock.class, GrindstoneBlock.class, SignBlock.class, AbstractLichenBlock.class, VineBlock.class),
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
            case WALLTORCH, ROD, AMETHYST -> { // FIXME check if the wall exists?
                return new Placement(((Direction) getPropertyByName(requiredState, "FACING")).getOpposite(),
                        null,
                        null);
            }
            case SLAB -> {
                Direction half = requiredState.get(SlabBlock.TYPE) == SlabType.BOTTOM ? Direction.DOWN : Direction.UP;
                return new Placement(half,
                        null,
                        null);
            }
            case STAIR -> {
                return new Placement(requiredState.get(StairsBlock.FACING), // FIXME before shipping
                        Vec3d.of(getHalf(requiredState.get(StairsBlock.HALF)).getVector()).multiply(0.25), //getHalf(requiredState.get(StairsBlock.HALF)),
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
                        requiredState.get(AnvilBlock.FACING).rotateYCounterclockwise());
            }
            case HOPPER, COCOA -> {
                return new Placement((Direction) getPropertyByName(requiredState, "FACING"),
                        null,
                        null);
            }
            case WALLMOUNTED -> {
                Direction side = switch ((WallMountLocation) getPropertyByName(requiredState, "FACE")) {
                    case FLOOR -> Direction.DOWN;
                    case CEILING -> Direction.UP;
                    default -> (Direction) getPropertyByName(requiredState, "FACING");
                };

                Direction look = getPropertyByName(requiredState, "FACE") == WallMountLocation.WALL ?
                        null : (Direction) getPropertyByName(requiredState, "FACING");

                return new Placement(side,
                        null,
                        look);
            }
//            case GRINDSTONE -> { // Tese are broken
//                Direction side = switch ((WallMountLocation) getPropertyByName(requiredState, "FACE")) {
//                    case FLOOR -> Direction.DOWN;
//                    case CEILING -> Direction.UP;
//                    default -> (Direction) getPropertyByName(requiredState, "FACING");
//                };
//
//                Direction look = getPropertyByName(requiredState, "FACE") == WallMountLocation.WALL ?
//                        null : (Direction) getPropertyByName(requiredState, "FACING");
//
//                return new Placement(Direction.DOWN, // FIXME test
//                        Vec3d.of(side.getVector()).multiply(0.5),
//                        look);
//            }
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
            case BELL -> {
                Direction side = switch (requiredState.get(BellBlock.ATTACHMENT)) {
                    case FLOOR -> Direction.DOWN;
                    case CEILING -> Direction.UP;
                    default -> requiredState.get(BellBlock.FACING);
                };

                Direction look = requiredState.get(BellBlock.ATTACHMENT) != Attachment.SINGLE_WALL &&
                        requiredState.get(BellBlock.ATTACHMENT) != Attachment.DOUBLE_WALL ?
                        requiredState.get(BellBlock.FACING) : null;

                return new Placement(side,
                        null,
                        look);
            }
            case DOOR -> {
                Direction hinge = requiredState.get(DoorBlock.FACING);
                if (requiredState.get(DoorBlock.HINGE) == DoorHinge.RIGHT) {
                    hinge = hinge.rotateYClockwise();
                } else {
                    hinge = hinge.rotateYCounterclockwise();
                }

                Vec3d hitModifier = Vec3d.of(hinge.getVector()).multiply(0.25);
                return new Placement(Direction.DOWN,
                        hitModifier,
                        requiredState.get(DoorBlock.FACING));
            }
            case SKIP -> {
                return new Placement();
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
        public final Vec3d hitModifier;
        @Nullable
        public final Direction look;

        final boolean sideIsAxis;
        boolean skip;

        public Placement(@Nullable Direction side, @Nullable Vec3d hitModifier, @Nullable Direction look, boolean sideIsAxis) {
            this.side = side;
            this.hitModifier = hitModifier;
            this.look = look;

            this.sideIsAxis = sideIsAxis;
            this.skip = false;
        }

        public Placement(@Nullable Direction side, @Nullable Vec3d hitModifier, @Nullable Direction look) {
            this(side, hitModifier, look, false);
        }

        public Placement() {
            this(null, null, null, false);
            this.skip = true;
        }
    }
}
