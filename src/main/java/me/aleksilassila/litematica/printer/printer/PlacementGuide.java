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
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlacementGuide {
    @NotNull
    protected final MinecraftClient client;
    @NotNull
    protected final ClientWorld world;
    @NotNull
    protected final WorldSchematic worldSchematic;

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

    @SuppressWarnings("EnhancedSwitchMigration")
    private @Nullable Action buildAction(BlockPos pos, ClassHook type) {
        BlockState requiredState = worldSchematic.getBlockState(pos);
        BlockState currentState = world.getBlockState(pos);

        switch (type) {
            case WALLTORCH:
            case ROD:
            case AMETHYST:
            case SHULKER: {
                return new Placement().setSides(
                        ((Direction) PrinterUtils.getPropertyByName(requiredState, "FACING"))
                                .getOpposite());
            }
            case SLAB: {
                Direction half = requiredState.get(SlabBlock.TYPE) == SlabType.BOTTOM ? Direction.DOWN : Direction.UP;
                return new Placement().setSides(half);
            }
            case STAIR: {
                Direction half = PrinterUtils.getHalf(requiredState.get(StairsBlock.HALF));

                Map<Direction, Vec3d> sides = new HashMap<>();
                sides.put(half, new Vec3d(0, 0, 0));

                for (Direction d : PrinterUtils.horizontalDirections) {
                    sides.put(d, Vec3d.of(half.getVector()).multiply(0.25));
                }

                return new Placement()
                        .setSides(sides)
                        .setLookDirection(requiredState.get(StairsBlock.FACING));
            }
            case TRAPDOOR: {
                Direction half = PrinterUtils.getHalf(requiredState.get(TrapdoorBlock.HALF));

                Map<Direction, Vec3d> sides = new HashMap<>();
                sides.put(half, new Vec3d(0, 0, 0));

                for (Direction d : PrinterUtils.horizontalDirections) {
                    sides.put(d, Vec3d.of(half.getVector()).multiply(0.25));
                }

                return new Placement()
                        .setSides(sides)
                        .setLookDirection(requiredState.get(StairsBlock.FACING).getOpposite());
            }
            case PILLAR: {
                Action action = new Placement().setSides(requiredState.get(PillarBlock.AXIS));

                // If is stripped log && should use normal log instead
                if (AxeItemAccessor.getStrippedBlocks().containsValue(requiredState.getBlock()) &&
                        LitematicaMixinMod.STRIP_LOGS.getBooleanValue()) {
                    Block stripped = requiredState.getBlock();

                    for (Block log : AxeItemAccessor.getStrippedBlocks().keySet()) {
                        if (AxeItemAccessor.getStrippedBlocks().get(log) != stripped) continue;

                        if (!PrinterUtils.playerHasAccessToItem(client.player, stripped.asItem()) &&
                                PrinterUtils.playerHasAccessToItem(client.player, log.asItem())) {
                            action.setItem(log.asItem());
                        }
                        break;

                    }
                }

                return action;
            }
            case ANVIL: {
                return new Placement().setLookDirection(requiredState.get(AnvilBlock.FACING).rotateYCounterclockwise());
            }
            case HOPPER: // FIXME add all sides
            case COCOA: {
                return new Placement().setSides((Direction) PrinterUtils.getPropertyByName(requiredState, "FACING"));
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

                return new Placement().setSides(side).setLookDirection(look).setRequiresSupport();
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
                return new Placement()
                        .setLookDirection((Direction) PrinterUtils.getPropertyByName(requiredState, "FACING"));
            }
            case BED: {
                if (requiredState.get(BedBlock.PART) != BedPart.FOOT) {
                    break;
                } else {
                    return new Placement().setLookDirection(requiredState.get(BedBlock.FACING));
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

                return new Placement().setSides(side).setLookDirection(look);
            }
            // Fixme
//            case DOOR: {
//                Direction hinge = requiredState.get(DoorBlock.FACING);
//                if (requiredState.get(DoorBlock.HINGE) == DoorHinge.RIGHT) {
//                    hinge = hinge.rotateYClockwise();
//                } else {
//                    hinge = hinge.rotateYCounterclockwise();
//                }
//
//                Vec3d hitModifier = Vec3d.of(hinge.getVector()).multiply(0.25);
//                return new Placement(Direction.DOWN,
//                        hitModifier,
//                        requiredState.get(DoorBlock.FACING));
//            }
            case WALLSKULL: {
                return new Placement().setSides(requiredState.get(WallSkullBlock.FACING).getOpposite());
            }
            case FARMLAND: {
                if (!PrinterUtils.playerHasAccessToItem(client.player, requiredState.getBlock().asItem())) {
                    return new Placement().setItem(Items.DIRT);
                }
                break;
            }
            case FLOWER_POT: { // Fixme these
                return new Placement().setItem(Items.FLOWER_POT);
            }
            case BIG_DRIPLEAF_STEM: {
                return new Placement().setItem(Items.BIG_DRIPLEAF);
            }
            case SKIP: {
                break;
            }
            default: { // Try to guess how the rest of the blocks are placed.
                Direction look = null;

                for (Property<?> prop : requiredState.getProperties()) {
                    if (prop instanceof DirectionProperty && prop.getName().equalsIgnoreCase("FACING")) {
                        look = ((Direction) requiredState.get(prop)).getOpposite();
                    }
                }

                Action placement = new Placement().setLookDirection(look);

                // If required == dirt path place dirt
                if (requiredState.getBlock().equals(Blocks.DIRT_PATH) && !PrinterUtils.playerHasAccessToItem(client.player, requiredState.getBlock().asItem())) {
                    placement.setItem(Items.DIRT);
                }

                return placement;
            }
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

    public abstract class Action {
        private Map<Direction, Vec3d> sides;
        private Direction lookDirection;
        @Nullable
        private Item clickItem; // null == any

        private boolean crouch = false;
        private boolean requiresSupport = false;

        // If true, click target block, not neighbor

        public Action() {
            this.sides = new HashMap<>();
            for (Direction direction : Direction.values()) {
                sides.put(direction, new Vec3d(0, 0, 0));
            }
        }

        public Action(Direction side) {
            this(side, new Vec3d(0, 0, 0));
        }

        public Action(Map<Direction, Vec3d> sides) {
            this.sides = sides;
        }

        public Action(Direction side, Vec3d modifier) {
            this.sides = new HashMap<>();
            this.sides.put(side, modifier);
        }

        @SafeVarargs
        public Action(Pair<Direction, Vec3d>... sides) {
            this.sides = new HashMap<>();
            for (Pair<Direction, Vec3d> side : sides) {
                this.sides.put(side.getLeft(), side.getRight());
            }
        }

        public Action(Direction.Axis axis) {
            this.sides = new HashMap<>();

            for (Direction d : Direction.values()) {
                if (d.getAxis() == axis) {
                    sides.put(d, new Vec3d(0, 0, 0));
                }
            }
        }

        public @Nullable Direction getLookDirection() {
            return lookDirection;
        }

        public @Nullable Item getRequiredItem(BlockPos pos) {
            return clickItem == null ? worldSchematic.getBlockState(pos).getBlock().asItem() : clickItem;
        }

        public @NotNull Map<Direction, Vec3d> getSides() {
            if (this.sides == null) {
                this.sides = new HashMap<>();
                for (Direction d : Direction.values()) {
                    this.sides.put(d, new Vec3d(0, 0, 0));
                }
            }

            return this.sides;
        }

        public @Nullable Direction getValidSide() {
            Map<Direction, Vec3d> sides = getSides();

            for (Direction d : Direction.values()) {
                if (sides.containsKey(d)) {
                    return d;
                }
            }

            return null;
        }

        public Action setSides(Direction.Axis... axis) {
            Map<Direction, Vec3d> sides = new HashMap<>();

            for (Direction.Axis a : axis) {
                for (Direction d : Direction.values()) {
                    if (d.getAxis() == a) {
                        sides.put(d, new Vec3d(0, 0, 0));
                    }
                }
            }

            this.sides = sides;
            return this;
        }

//        public Action setInvalidNeighbors(Direction... neighbors) {
//            List<Direction> dirs = Arrays.asList(Direction.values());
//            dirs.removeAll(Arrays.asList(neighbors));
//            this.neighbors = dirs.toArray(Direction[]::new);
//            return this;
//        }

        public Action setLookDirection(Direction lookDirection) {
            this.lookDirection = lookDirection;
            return this;
        }

        public Action setSides(Map<Direction, Vec3d> sides) {
            this.sides = sides;
            return this;
        }

        public abstract BlockPos getTargetPos(BlockPos pos);

        public Action setSides(Direction... directions) {
            Map<Direction, Vec3d> sides = new HashMap<>();

            for (Direction d : directions) {
                sides.put(d, new Vec3d(0, 0, 0));
            }

            this.sides = sides;
            return this;
        }

        public Action setItem(Item item) {
            this.clickItem = item;
            return this;
        }

        public Action setRequiresSupport(boolean requiresSupport) {
            this.requiresSupport = requiresSupport;
            return this;
        }

        public Action setRequiresSupport() {
            return this.setRequiresSupport(true);
        }
    }

    public class Placement extends Action {
        public @Nullable Pair<Direction, Vec3d> getSupportedSide(BlockPos pos) {
            Map<Direction, Vec3d> sides = this.getSides();

            for (Direction d : sides.keySet()) {
                if (world.getBlockState(pos.offset(d)).getMaterial().isSolid()) {
                    return new Pair<>(d, sides.get(d));
                }
            }

            return null;
        }

        @Override
        public BlockPos getTargetPos(BlockPos pos) {
            return null;
        }
    }

    public class Click extends Action {
        @Override
        public BlockPos getTargetPos(BlockPos pos) {
            return pos;
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
        CORRECT
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
