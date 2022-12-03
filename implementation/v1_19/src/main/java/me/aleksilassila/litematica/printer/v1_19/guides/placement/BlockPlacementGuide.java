package me.aleksilassila.litematica.printer.v1_19.guides.placement;

import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.SchematicBlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BlockPlacementGuide extends PlacementGuide {
    public BlockPlacementGuide(SchematicBlockState state) {
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

//    @Override
//    protected int getStackSlot(ClientPlayerEntity player) {
//        List<ItemStack> requiredItems = getRequiredItems();
//        if (requiredItems.isEmpty() || requiredItems.get(0) == ItemStack.EMPTY) return -1;
//
//        return super.getStackSlot(player);
//    }

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
            if (!isInteractive(state.getNeighbor(validSide).currentState.getBlock())) {
                return validSide;
            }
        }

        return validSides.isEmpty() ? null : validSides.get(0);
    }

    protected boolean getRequiresShift(SchematicBlockState state) {
        if (getRequiresExplicitShift()) return true;
//        if (interactionDir == null) return false;
        Direction clickSide = getValidSide(state);
        if (clickSide == null) return false;
        return isInteractive(state.getNeighbor(clickSide).currentState.getBlock());
    }

    @Nullable
    private Vec3d getHitVector(SchematicBlockState state, Direction validSide) {
        Direction side = getValidSide(state);
        if (side == null) return null;
        return Vec3d.ofCenter(state.blockPos)
                .add(Vec3d.of(side.getVector()).multiply(0.5))
                .add(getHitModifier(validSide));
    }

    @Nullable
    public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player) {
        Direction validSide = getValidSide(state);
        Vec3d hitVec = getHitVector(state, validSide);
        Optional<ItemStack> requiredItem = getRequiredItem(player);

        if (validSide == null || hitVec == null || requiredItem.isEmpty()) return null;

        Direction lookDirection = getLookDirection();
        boolean requiresShift = getRequiresShift(state);

        BlockHitResult blockHitResult = new BlockHitResult(hitVec, validSide.getOpposite(), state.blockPos.offset(validSide), false);

        return new PrinterPlacementContext(player, blockHitResult, requiredItem.get(), getSlotWithItem(player, requiredItem.get()), lookDirection, requiresShift);
    }
}
