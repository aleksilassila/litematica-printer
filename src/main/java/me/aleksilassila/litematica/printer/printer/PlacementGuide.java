package me.aleksilassila.litematica.printer.printer;

import me.aleksilassila.litematica.printer.interfaces.Implementation;
import net.minecraft.block.*;
import net.minecraft.block.enums.*;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public enum PlacementGuide {
    ROD(Implementation.NewBlocks.ROD.clazz),
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
    AMETHYST(Implementation.NewBlocks.AMETHYST.clazz),
    DOOR(DoorBlock.class),
    COCOA(CocoaBlock.class),
    OBSERVER(ObserverBlock.class),
    WALLSKULL(WallSkullBlock.class),
    SKIP(SkullBlock.class, GrindstoneBlock.class, SignBlock.class, Implementation.NewBlocks.LICHEN.clazz, VineBlock.class),
    DEFAULT;

    private final Class<?>[] matchClasses;

    PlacementGuide(Class<?> ... classes) {
        matchClasses = classes;
    }

    private static PlacementGuide getGuide(BlockState requiredState) {
        for (PlacementGuide guide : PlacementGuide.values()) {
            for (Class<?> clazz : guide.matchClasses) {
                if (clazz != null && clazz.isInstance(requiredState.getBlock())) {
                    return guide;
                }
            }
        }

        return DEFAULT;
    }

    public static Placement getPlacement(BlockState requiredState) {
        switch (getGuide(requiredState)) {
            case WALLTORCH:
            case ROD:
            case AMETHYST:
            case SHULKER: {
                return new Placement(((Direction) PrinterUtils.getPropertyByName(requiredState, "FACING")).getOpposite(),
                        null,
                        null);
            }
            case SLAB: {
                Direction half = requiredState.get(SlabBlock.TYPE) == SlabType.BOTTOM ? Direction.DOWN : Direction.UP;
                return new Placement(half,
                        null,
                        null);
            }
            case STAIR: {
                return new Placement(requiredState.get(StairsBlock.FACING), // FIXME before shipping
                        Vec3d.of(PrinterUtils.getHalf(requiredState.get(StairsBlock.HALF)).getVector()).multiply(0.25), //getHalf(requiredState.get(StairsBlock.HALF)),
                        requiredState.get(StairsBlock.FACING));
            }
            case TRAPDOOR: {
                return new Placement(PrinterUtils.getHalf(requiredState.get(TrapdoorBlock.HALF)),
                        null, //getHalf(requiredState.get(TrapdoorBlock.HALF)),
                        requiredState.get(StairsBlock.FACING).getOpposite());
            }
            case PILLAR: {
                return new Placement(PrinterUtils.axisToDirection(requiredState.get(PillarBlock.AXIS)),
                        null,
                        null, true);
            }
            case ANVIL: {
                return new Placement(null,
                        null,
                        requiredState.get(AnvilBlock.FACING).rotateYCounterclockwise());
            }
            case HOPPER:
            case COCOA: {
                return new Placement((Direction) PrinterUtils.getPropertyByName(requiredState, "FACING"),
                        null,
                        null);
            }
            case WALLMOUNTED: {
                Direction side;
                switch ((WallMountLocation) PrinterUtils.getPropertyByName(requiredState, "FACE")) {
                    case FLOOR: {
                        side = Direction.DOWN;
                        break;
                    }
                    case CEILING: {
                        side = Direction.UP;
                        break;
                    }
                    default: {
                        side = ((Direction) PrinterUtils.getPropertyByName(requiredState, "FACING")).getOpposite();
                        break;
                    }
                }

                Direction look = PrinterUtils.getPropertyByName(requiredState, "FACE") == WallMountLocation.WALL ?
                        null : (Direction) PrinterUtils.getPropertyByName(requiredState, "FACING");

                return new Placement(side,
                        null,
                        look).setCantPlaceInAir(true);
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
            case GATE:
            case CAMPFIRE: {
                return new Placement(null,
                        null,
                        (Direction) PrinterUtils.getPropertyByName(requiredState, "FACING"));
            }
            case BED: {
                if (requiredState.get(BedBlock.PART) != BedPart.FOOT) {
                    return new Placement();
                } else {
                    return new Placement(null, null, requiredState.get(BedBlock.FACING));
                }
            }
            case BELL: {
                Direction side;
                switch (requiredState.get(BellBlock.ATTACHMENT)) {
                    case FLOOR: {
                        side = Direction.DOWN;
                        break;
                    }
                    case CEILING: {
                        side = Direction.UP;
                        break;
                    }
                    default: {
                        side = requiredState.get(BellBlock.FACING);
                        break;
                    }
                }

                Direction look = requiredState.get(BellBlock.ATTACHMENT) != Attachment.SINGLE_WALL &&
                        requiredState.get(BellBlock.ATTACHMENT) != Attachment.DOUBLE_WALL ?
                        requiredState.get(BellBlock.FACING) : null;

                return new Placement(side,
                        null,
                        look);
            }
            case DOOR: {
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
            case WALLSKULL: {
                return new Placement(requiredState.get(WallSkullBlock.FACING).getOpposite(), null, null);
            }
            case SKIP: {
                return new Placement();
            }
            default: { // Try to guess how the rest of the blocks are placed.
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

    public static class Placement {
        @Nullable
        public final Direction side;
        @Nullable
        public final Vec3d hitModifier;
        @Nullable
        public final Direction look;

        boolean sideIsAxis = false;

        boolean cantPlaceInAir = false;
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

        public Placement setSideIsAxis(boolean sideIsAxis) {
            this.sideIsAxis = sideIsAxis;

            return this;
        }

        public Placement setCantPlaceInAir(boolean cantPlaceInAir) {
            this.cantPlaceInAir = cantPlaceInAir;
            return this;
        }
    }
}
