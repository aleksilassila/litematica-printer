package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.InteractAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.PrepareAction;
import me.aleksilassila.litematica.printer.v1_19.printer.action.ReleaseShiftAction;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract public class AbstractPlacementGuide extends AbstractGuide {
    public AbstractPlacementGuide(SchematicBlockState state) {
        super(state);
    }

    protected ItemStack getBlockItem() {
        return state.targetState.getBlock().getPickStack(state.world, state.blockPos, state.targetState);
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(getBlockItem());
    }

    abstract protected boolean getRequiresShift(SchematicBlockState state);

    @Nullable
    abstract public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player);

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
}
