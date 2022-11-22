package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.InteractAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.PrepareAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.ReleaseShiftAction;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlacementGuide extends InteractionGuide {
    protected List<Direction> getValidSides(SchematicBlockState state) {
        return Arrays.asList(Direction.values());
    }

    protected Direction getLookDirection(SchematicBlockState state) {
        return null;
    }

    protected boolean getRequiresSupport(SchematicBlockState state) {
        return false;
    }

    @Nullable
    protected Direction getValidSide(SchematicBlockState state) {
        boolean printInAir = LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();

        List<Direction> sides = getValidSides(state);

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

    protected boolean getRequiresShift(SchematicBlockState state, Direction interactionDir) {
        if (getRequiresShift(state)) return true;
        if (interactionDir == null) return false;
        return Implementation.isInteractive(state.getNeighbor(interactionDir).currentState.getBlock());
    }

    protected boolean getRequiresShift(SchematicBlockState state) {
        return false;
    }

    @Nullable
    protected Vec3d getHitVector(SchematicBlockState state) {
        Direction side = getValidSide(state);
        if (side == null) return null;
        return Vec3d.ofCenter(state.blockPos)
                .add(Vec3d.of(side.getVector()).multiply(0.5));
    }

    @Override
    public List<AbstractAction> getActions(ClientPlayerEntity player, SchematicBlockState state) {
        Direction validSide = getValidSide(state);
        Vec3d hitVec = getHitVector(state);

        if (validSide == null || hitVec == null) return null;

        Direction lookDirection = getLookDirection(state);
        boolean requiresShift = getRequiresShift(state, validSide);
        Item requiredItem = state.targetState.getBlock().asItem();

        if (!playerHasAccessToItem(player, requiredItem)) return null;

        List<AbstractAction> actions = new ArrayList<>();
        actions.add(new PrepareAction(lookDirection, requiresShift, requiredItem));
        actions.add(new InteractAction(state.blockPos.offset(validSide), validSide.getOpposite(), hitVec));
        if (requiresShift) actions.add(new ReleaseShiftAction());

        return actions;
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player, SchematicBlockState state) {
        if (!state.currentState.getMaterial().isReplaceable()) return false;
        if (state.currentState.contains(FluidBlock.LEVEL) && state.currentState.get(FluidBlock.LEVEL) == 0)
            return false;

        return true;
    }
}
