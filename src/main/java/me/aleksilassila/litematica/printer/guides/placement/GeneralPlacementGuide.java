package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.Printer;
import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An old school guide where there are defined specific conditions
 * for player state depending on the block being placed.
 */
public class GeneralPlacementGuide extends PlacementGuide {
    public GeneralPlacementGuide(SchematicBlockState state) {
        super(state);
    }

    protected List<Direction> getPossibleSides() {
        return Arrays.asList(Direction.values());
    }

    protected Optional<Direction> getLookDirection() {
        return Optional.empty();
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

    private Optional<Direction> getValidSide(SchematicBlockState state) {
        boolean printInAir = false; // LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();

        List<Direction> sides = getPossibleSides();

        if (sides.isEmpty()) {
            return Optional.empty();
        }

        List<Direction> validSides = new ArrayList<>();
        for (Direction side : sides) {
            if (printInAir && !getRequiresSupport()) {
                return Optional.of(side);
            } else {
                SchematicBlockState neighborState = state.offset(side);

                if (getProperty(neighborState.currentState, SlabBlock.TYPE).orElse(null) == SlabType.DOUBLE) {
                    validSides.add(side);
                    continue;
                }

                if (canBeClicked(neighborState.world, neighborState.blockPos) && // Handle unclickable grass for example
                        !neighborState.currentState.isReplaceable())
                    validSides.add(side);
            }
        }

        for (Direction validSide : validSides) {
            if (!isInteractive(state.offset(validSide).currentState.getBlock())) {
                return Optional.of(validSide);
            }
        }

        return validSides.isEmpty() ? Optional.empty() : Optional.of(validSides.getFirst());
    }

    protected boolean getUseShift(SchematicBlockState state) {
        if (getRequiresExplicitShift())
            return true;

        Direction clickSide = getValidSide(state).orElse(null);
        if (clickSide == null)
            return false;
        return isInteractive(state.offset(clickSide).currentState.getBlock());
    }

    private Optional<Vec3d> getHitVector(SchematicBlockState state) {
        return getValidSide(state).map(side -> Vec3d.ofCenter(state.blockPos)
                .add(Vec3d.of(side.getVector()).multiply(0.5))
                .add(getHitModifier(side)));
    }

    @Nullable
    public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player) {
        try {
            Optional<Direction> validSide = getValidSide(state);
            Optional<Vec3d> hitVec = getHitVector(state);
            Optional<ItemStack> requiredItem = getRequiredItem(player);
            int requiredSlot = getRequiredItemStackSlot(player);

            if (validSide.isEmpty() || hitVec.isEmpty() || requiredItem.isEmpty() || requiredSlot == -1)
                return null;

            Optional<Direction> lookDirection = getLookDirection();
            boolean requiresShift = getUseShift(state);

            BlockHitResult blockHitResult = new BlockHitResult(hitVec.get(), validSide.get().getOpposite(),
                    state.blockPos.offset(validSide.get()), false);

            return new PrinterPlacementContext(player, blockHitResult, requiredItem.get(), requiredSlot,
                    lookDirection.orElse(null), requiresShift);
        } catch (Exception e) {
            Printer.logger.error("getPlacementContext(): Exception caught: {}", e.getMessage());
            //e.printStackTrace();
            return null;
        }
    }
}
