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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlacementGuide extends InteractionGuide {
    public PlacementGuide(SchematicBlockState state) {
        super(state);
    }

    protected List<Direction> getPossibleSides() {
        return Arrays.asList(Direction.values());
    }

    protected Direction getLookDirection() {
        return null;
    }

    protected boolean getRequiresSupport() {
        return false;
    }

    protected boolean getRequiresExplicitShift() {
        return false;
    }

    protected Vec3d getHitModifier(Direction validSide) {
        return new Vec3d(0, 0, 0);
    }

    protected ItemStack getBlockItem() {
        return state.targetState.getBlock().getPickStack(state.world, state.blockPos, state.targetState);
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(getBlockItem());
    }

    @Override
    protected int getStackSlot(ClientPlayerEntity player) {
        List<ItemStack> requiredItems = getRequiredItems();
        if (requiredItems.isEmpty() || requiredItems.get(0) == ItemStack.EMPTY) return -1;

        return super.getStackSlot(player);
    }

    private @Nullable Direction getValidSide(SchematicBlockState state) {
        boolean printInAir = LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();

        List<Direction> sides = getPossibleSides();

        if (sides.isEmpty()) {
            return null;
        }

        List<Direction> validSides = new ArrayList<>();
        for (Direction side : sides) {
            if (printInAir && !getRequiresSupport()) {
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
        if (getRequiresExplicitShift()) return true;
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
                .add(getHitModifier(validSide));
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (!super.canExecute(player)) return false;
        ItemPlacementContext ctx = getPlacementContext(player);
        if (ctx == null || !ctx.canPlace()) return false;
//        if (!state.currentState.getMaterial().isReplaceable()) return false;
        if (state.currentState.contains(FluidBlock.LEVEL) && state.currentState.get(FluidBlock.LEVEL) == 0)
            return false;

        BlockState resultState = targetState.getBlock().getPlacementState(ctx);
        if (resultState == null || !resultState.canPlaceAt(state.world, state.blockPos))
            return false;

        return true;
    }

    @Override
    public List<AbstractAction> execute(ClientPlayerEntity player) {
        PrinterPlacementContext ctx = getPlacementContext(player);

        if (ctx == null) return null;

        List<AbstractAction> actions = new ArrayList<>();
        actions.add(new PrepareAction(ctx));
        actions.add(new InteractAction(ctx));
        if (getRequiresShift(state)) actions.add(new ReleaseShiftAction());

        return actions;
    }

    @Nullable
    public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player) {
        Direction validSide = getValidSide(state);
        Vec3d hitVec = getHitVector(state, validSide);

        if (validSide == null || hitVec == null) return null;

        Direction lookDirection = getLookDirection();
        boolean requiresShift = getRequiresShift(state);
        Item requiredItem = state.targetState.getBlock().asItem();

        if (!playerHasAccessToItem(player, requiredItem)) return null;

        BlockHitResult blockHitResult = new BlockHitResult(hitVec, validSide.getOpposite(), state.blockPos.offset(validSide), false);
        ItemStack itemStack = new ItemStack(requiredItem);

        return new PrinterPlacementContext(player, blockHitResult, itemStack, lookDirection, requiresShift);
    }
}
