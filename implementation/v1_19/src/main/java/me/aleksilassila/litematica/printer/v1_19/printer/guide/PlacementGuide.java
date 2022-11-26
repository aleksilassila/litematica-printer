package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.InteractAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.PrepareAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.ReleaseShiftAction;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlacementGuide extends InteractionGuide {
    protected List<Direction> getPossibleSides(SchematicBlockState state, BlockState currentState, BlockState targetState) {
        return Arrays.asList(Direction.values());
    }

    protected Direction getLookDirection(SchematicBlockState state) {
        return null;
    }

    protected boolean getRequiresSupport(SchematicBlockState state) {
        return false;
    }

    protected boolean getRequiresExplicitShift(SchematicBlockState state) {
        return false;
    }

    protected Vec3d getHitModifier(SchematicBlockState state, Direction validSide) {
        return new Vec3d(0, 0, 0);
    }

    @Override
    protected List<ItemStack> getRequiredItems(SchematicBlockState state) {
        return Collections.singletonList(state.targetState.getBlock().asItem().getDefaultStack());
    }

    @Nullable
    private Direction getValidSide(SchematicBlockState state) {
        boolean printInAir = LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();

        List<Direction> sides = getPossibleSides(state, state.currentState, state.targetState);

        if (sides.isEmpty()) {
            return null;
        }

        List<Direction> validSides = new ArrayList<>();
        for (Direction side : sides) {
            if (printInAir && !getRequiresSupport(state)) {
                return side;
            } else {
                SchematicBlockState neighborState = state.getNeighbor(side);

                // If neighbor is half slab
                if (neighborState.currentState.contains(SlabBlock.TYPE)
                        && neighborState.currentState.get(SlabBlock.TYPE) != SlabType.DOUBLE) {
                    continue;
                }

                if (canBeClicked(neighborState.world, neighborState.blockPos) && // Handle unclickable grass for example
                        !neighborState.currentState.getMaterial().isReplaceable())
                    validSides.add(side);
            }
        }

        for (Direction validSide : validSides) {
            if (!Implementation.isInteractive(state.getNeighbor(validSide).currentState.getBlock())) {
                return validSide;
            }
        }

        return validSides.isEmpty() ? null : validSides.get(0);
    }

    private boolean getRequiresShift(SchematicBlockState state) {
        if (getRequiresExplicitShift(state)) return true;
//        if (interactionDir == null) return false;
        Direction clickSide = getValidSide(state);
        if (clickSide == null) return false;
        return Implementation.isInteractive(state.getNeighbor(clickSide).currentState.getBlock());
    }

    @Nullable
    private Vec3d getHitVector(SchematicBlockState state, Direction validSide) {
        Direction side = getValidSide(state);
        if (side == null) return null;
        return Vec3d.ofCenter(state.blockPos)
                .add(Vec3d.of(side.getVector()).multiply(0.5))
                .add(getHitModifier(state, validSide));
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player, SchematicBlockState state) {
        if (!super.canExecute(player, state)) return false;
        ItemPlacementContext ctx = getPlacementContext(player, state);
        if (ctx == null || !ctx.canPlace()) return false;
//        if (!state.currentState.getMaterial().isReplaceable()) return false;
        if (state.currentState.contains(FluidBlock.LEVEL) && state.currentState.get(FluidBlock.LEVEL) == 0)
            return false;

        return true;
    }

    @Override
    public List<AbstractAction> execute(ClientPlayerEntity player, SchematicBlockState state) {
        PrinterPlacementContext ctx = getPlacementContext(player, state);

        if (ctx == null) return null;

        List<AbstractAction> actions = new ArrayList<>();
        actions.add(new PrepareAction(ctx));
        actions.add(new InteractAction(ctx));
        if (getRequiresShift(state)) actions.add(new ReleaseShiftAction());

        return actions;
    }

    @Nullable
    public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player, SchematicBlockState state) {
        Direction validSide = getValidSide(state);
        Vec3d hitVec = getHitVector(state, validSide);

        if (validSide == null || hitVec == null) return null;

        Direction lookDirection = getLookDirection(state);
        boolean requiresShift = getRequiresShift(state);
        Item requiredItem = state.targetState.getBlock().asItem();

        if (!playerHasAccessToItem(player, requiredItem)) return null;

        BlockHitResult blockHitResult = new BlockHitResult(hitVec, validSide.getOpposite(), state.blockPos.offset(validSide), false);
        ItemStack itemStack = new ItemStack(requiredItem);

        return new PrinterPlacementContext(player, blockHitResult, itemStack, lookDirection, requiresShift);
    }
}
