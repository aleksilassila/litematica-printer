package me.aleksilassila.litematica.printer.printer;

import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.interfaces.Implementation;
import net.fabricmc.fabric.mixin.content.registry.AxeItemAccessor;
import net.minecraft.block.*;
import net.minecraft.block.enums.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlacementGuide {
    @NotNull protected final MinecraftClient client;
    @NotNull protected final ClientWorld world;
    @NotNull protected final WorldSchematic worldSchematic;

    public PlacementGuide(@NotNull MinecraftClient client, @NotNull WorldSchematic worldSchematic) {
        this.client = client;
        this.world = client.world;
        this.worldSchematic = worldSchematic;
    }

    public Action getAction(BlockPos pos) {
        for (ClassHook hook : ClassHook.values()) {
            if (hook.state != getState(pos)) continue;

            for (Class<?> clazz : hook.classes) {
                if (clazz != null && clazz.isInstance(worldSchematic.getBlockState(pos).getBlock())) {
                    return buildAction(pos, hook);
                }
            }
        }

        return null;
    }

//    public static Placement getPlacement(BlockState requiredState, MinecraftClient client) {
//        Placement placement = _getPlacement(requiredState, client);
//        return placement.setItem(placement.item == null ? requiredState.getBlock().asItem() : placement.item);
//    }

    private @Nullable Action buildAction(BlockPos pos, ClassHook hook) {
        BlockState requiredState = worldSchematic.getBlockState(pos);
        BlockState currentState = world.getBlockState(pos);

        switch (hook) {
            case WALLTORCH:
            case ROD:
            case AMETHYST:
            case SHULKER: {
                return new Action(((Direction) PrinterUtils.getPropertyByName(requiredState, "FACING")).getOpposite());
            }
            case SLAB: {
                Direction half = requiredState.get(SlabBlock.TYPE) == SlabType.BOTTOM ? Direction.DOWN : Direction.UP;
                return new Action(half);
            }
            /*
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
                Placement placement = new Placement(PrinterUtils.axisToDirection(requiredState.get(PillarBlock.AXIS)),
                        null,
                        null).setSideIsAxis(true);

                // If is stripped log && should use normal log instead
                if (AxeItemAccessor.getStrippedBlocks().containsValue(requiredState.getBlock()) &&
                        LitematicaMixinMod.STRIP_LOGS.getBooleanValue()) {
                    Block stripped = requiredState.getBlock();

                    for (Block log : AxeItemAccessor.getStrippedBlocks().keySet()) {
                        if (AxeItemAccessor.getStrippedBlocks().get(log) != stripped) continue;

                        if (!PrinterUtils.playerHasAccessToItem(client.player, stripped.asItem()) &&
                                PrinterUtils.playerHasAccessToItem(client.player, log.asItem())) {
                            placement.item = log.asItem();
                        }
                        break;

                    }
                }

                return placement;
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
            case OBSERVER:
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
            case FARMLAND: {
                if (!PrinterUtils.playerHasAccessToItem(client.player, requiredState.getBlock().asItem())) {
                    return new Placement(null, null, null).setItem(Items.DIRT);
                }
                break;
            }
            case FLOWER_POT: {
                return new Placement(null, null, null).setItem(Items.FLOWER_POT);
            }
            case BIG_DRIPLEAF_STEM: {
                return new Placement(null, null, null).setItem(Items.BIG_DRIPLEAF);
            }
            default: { // Try to guess how the rest of the blocks are placed.
                Direction look = null;

                for (Property<?> prop : requiredState.getProperties()) {
                    if (prop instanceof DirectionProperty && prop.getName().equalsIgnoreCase("FACING")) {
                        look = ((Direction) requiredState.get(prop)).getOpposite();
                    }
                }

                Placement placement = new Placement(null, null, look);

                // If required == dirt path place dirt
                if (requiredState.getBlock().equals(Blocks.DIRT_PATH) && !PrinterUtils.playerHasAccessToItem(client.player, requiredState.getBlock().asItem())) {
                    placement.setItem(Items.DIRT);
                }

                return placement;
            }*/
        }

        return null;
    }
//
//    public static class Placement {
//        @NotNull
//        public final Direction side;
//        @Nullable
//        public final Vec3d hitModifier;
//        @Nullable
//        public final Direction look;
//
//        boolean sideIsAxis = false;
//
//        boolean cantPlaceInAir = false;
//        boolean skip;
//
//        Item item = null;
//
//        public Placement(@Nullable Direction side, @Nullable Vec3d hitModifier, @Nullable Direction look) {
//            this.side = side == null ? Direction.DOWN : side;
//            this.hitModifier = hitModifier;
//            this.look = look;
//
//            this.skip = false;
//        }
//
//        public Placement() {
//            this(null, null, null);
//            this.skip = true;
//        }
//
//        public Placement setSideIsAxis(boolean sideIsAxis) {
//            this.sideIsAxis = sideIsAxis;
//
//            return this;
//        }
//
//        public Placement setCantPlaceInAir(boolean cantPlaceInAir) {
//            this.cantPlaceInAir = cantPlaceInAir;
//            return this;
//        }
//
//        public Placement setItem(Item item) {
//            this.item = item;
//            return this;
//        }
//    }
//
//    public static class Click {
//        public final boolean click;
//        @Nullable
//        public final Item[] items;
//
//        public Click(boolean click, @Nullable Item ...item) {
//            this.click = click;
//            this.items = item;
//        }
//
//        public Click(boolean click) {
//            this(click, null);
//        }
//
//        public Click() {
//            this(false, null);
//        }
//    }

    public class Action {
//        BlockPos neighbor;
        private Direction[] neighbors;
        private Direction lookDirection;
        private Vec3d hitModifier;
        @Nullable private Item requiredItem;

//        private boolean cantPlaceInAir = false;

        public Action(Direction... neighbors) {
            this.setValidNeighbors(neighbors);
        }

        public Direction[] getValidTargets() {
            if (neighbors == null) return new Direction[]{};
            return neighbors;
        }

        public Vec3d getHitVector() {
            return hitModifier;
        }

        public Direction getLookDirection() {
            return lookDirection;
        }

        public @Nullable Item getRequiredItem(BlockState requiredState) {
            return requiredItem == null ? requiredState.getBlock().asItem() : requiredItem;
        }



        public Action setValidNeighbors(Direction... neighbors) {
            this.neighbors = neighbors;
            return this;
        }

        public Action setValidNeighbors(Direction.Axis... axis) {
            List<Direction> neighbors = new ArrayList<>();

            for (Direction.Axis a : axis) {
                for (Direction d : Direction.values()) {
                    if (d.getAxis() == a) {
                        neighbors.add(d);
                    }
                }
            }
            
            this.neighbors = neighbors.toArray(Direction[]::new);
            return this;
        }

        public Action setInvalidNeighbors(Direction... neighbors) {
            List<Direction> dirs = Arrays.asList(Direction.values());
            dirs.removeAll(Arrays.asList(neighbors));
            this.neighbors = dirs.toArray(Direction[]::new);
            return this;
        }

        public Action setLookDirection(Direction lookDirection) {
            this.lookDirection = lookDirection;
            return this;
        }
    }

    private State getState(BlockPos pos) {
        if (!worldSchematic.getBlockState(pos).isAir() &&
                client.world.getBlockState(pos).isAir())
            return State.MISSING_BLOCK;
        else if (!worldSchematic.getBlockState(pos).getBlock()
                .equals(client.world.getBlockState(pos).getBlock()))
            return State.WRONG_STATE;
        else if (!worldSchematic.getBlockState(pos)
                .equals(client.world.getBlockState(pos).isAir()))
            return State.WRONG_STATE;

        return State.CORRECT;
    }

    enum State {
        MISSING_BLOCK,
        WRONG_STATE,
        CORRECT;
    }

    enum ClassHook {
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
        FARMLAND(FarmlandBlock.class),
        FLOWER_POT(FlowerPotBlock.class),
        BIG_DRIPLEAF_STEM(BigDripleafStemBlock.class),
        DEFAULT_MISSING(State.MISSING_BLOCK),
        DEFAULT_CLICKABLE(State.WRONG_STATE);

        private final Class<?>[] classes;
        private final State state;

        ClassHook(Class<?>... classes) {
            this(State.MISSING_BLOCK, classes);
        }

        ClassHook(State state, Class<?>... classes) {
            this.state = state;
            this.classes = classes;
        }
    }
}
