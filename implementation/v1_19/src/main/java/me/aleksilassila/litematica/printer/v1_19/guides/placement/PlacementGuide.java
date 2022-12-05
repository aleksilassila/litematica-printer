package me.aleksilassila.litematica.printer.v1_19.guides.placement;

import me.aleksilassila.litematica.printer.v1_19.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.actions.AbstractAction;
import me.aleksilassila.litematica.printer.v1_19.actions.PrepareAction;
import me.aleksilassila.litematica.printer.v1_19.actions.ReleaseShiftAction;
import me.aleksilassila.litematica.printer.v1_19.guides.Guide;
import me.aleksilassila.litematica.printer.v1_19.implementation.actions.InteractActionImpl;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Guide that clicks its neighbors to create a placement in target position.
 */
abstract public class PlacementGuide extends Guide {
    public PlacementGuide(SchematicBlockState state) {
        super(state);
    }

    protected ItemStack getBlockItem(BlockState state) {
        return state.getBlock().getPickStack(this.state.world, this.state.blockPos, state);
    }

    protected Optional<Block> getRequiredItemAsBlock(ClientPlayerEntity player) {
        Optional<ItemStack> requiredItem = getRequiredItem(player);

        if (requiredItem.isEmpty()) {
            return Optional.empty();
        } else {
            ItemStack itemStack = requiredItem.get();

            if (itemStack.getItem() instanceof BlockItem)
                return Optional.of(((BlockItem) itemStack.getItem()).getBlock());
            else return Optional.empty();
        }
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(getBlockItem(state.targetState));
    }

    abstract protected boolean getUseShift(SchematicBlockState state);

    @Nullable
    abstract public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player);

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (!super.canExecute(player)) return false;

        List<ItemStack> requiredItems = getRequiredItems();
        if (requiredItems.isEmpty() || requiredItems.stream().allMatch(i -> i.isOf(Items.AIR)))
            return false;

        ItemPlacementContext ctx = getPlacementContext(player);
        if (ctx == null || !ctx.canPlace()) return false;
//        if (!state.currentState.getMaterial().isReplaceable()) return false;
        if (state.currentState.contains(FluidBlock.LEVEL) && state.currentState.get(FluidBlock.LEVEL) == 0)
            return false;

        BlockState resultState = getRequiredItemAsBlock(player)
                .orElse(targetState.getBlock())
                .getPlacementState(ctx);

//        if (resultState != null && !canPlaceInWater(resultState)) return false;

        if (resultState != null) {
            if (!resultState.canPlaceAt(state.world, state.blockPos)) return false;
            return !(currentState.getBlock() instanceof FluidBlock) || canPlaceInWater(resultState);
        } else {
            return false;
        }
    }

    @Override
    public List<AbstractAction> execute(ClientPlayerEntity player) {
        PrinterPlacementContext ctx = getPlacementContext(player);

        if (ctx == null) return null;

        List<AbstractAction> actions = new ArrayList<>();
        actions.add(new PrepareAction(ctx));
        actions.add(new InteractActionImpl(ctx));
        if (ctx.shouldSneak) actions.add(new ReleaseShiftAction());

        return actions;
    }

    protected static boolean canBeClicked(World world, BlockPos pos) {
        return getOutlineShape(world, pos) != VoxelShapes.empty();
    }

    private static VoxelShape getOutlineShape(World world, BlockPos pos) {
        return world.getBlockState(pos).getOutlineShape(world, pos);
    }

    public static boolean isInteractive(Block block) {
        for (Class<?> clazz : interactiveBlocks) {
            if (clazz.isInstance(block)) {
                return true;
            }
        }

        return false;
    }

    private boolean canPlaceInWater(BlockState blockState) {
        Block block = blockState.getBlock();
        if (block instanceof FluidFillable) {
            return true;
        } else if (!(block instanceof DoorBlock) && !blockState.isIn(BlockTags.SIGNS) && !blockState.isOf(Blocks.LADDER) && !blockState.isOf(Blocks.SUGAR_CANE) && !blockState.isOf(Blocks.BUBBLE_COLUMN)) {
            Material material = blockState.getMaterial();
            if (material != Material.PORTAL && material != Material.STRUCTURE_VOID && material != Material.UNDERWATER_PLANT && material != Material.REPLACEABLE_UNDERWATER_PLANT) {
                return material.blocksMovement();
            } else {
                return true;
            }
        }

        return true;
    }

    public static Class<?>[] interactiveBlocks = new Class[]{
            AbstractChestBlock.class, AbstractFurnaceBlock.class, CraftingTableBlock.class,
            AbstractButtonBlock.class, LeverBlock.class, DoorBlock.class, TrapdoorBlock.class,
            BedBlock.class, RedstoneWireBlock.class, ScaffoldingBlock.class, HopperBlock.class,
            EnchantingTableBlock.class, NoteBlock.class, JukeboxBlock.class, CakeBlock.class,
            FenceGateBlock.class, BrewingStandBlock.class, DragonEggBlock.class, CommandBlock.class,
            BeaconBlock.class, AnvilBlock.class, ComparatorBlock.class, RepeaterBlock.class,
            DropperBlock.class, DispenserBlock.class, ShulkerBoxBlock.class, LecternBlock.class,
            FlowerPotBlock.class, BarrelBlock.class, BellBlock.class, SmithingTableBlock.class,
            LoomBlock.class, CartographyTableBlock.class, GrindstoneBlock.class,
            StonecutterBlock.class
    };
}
