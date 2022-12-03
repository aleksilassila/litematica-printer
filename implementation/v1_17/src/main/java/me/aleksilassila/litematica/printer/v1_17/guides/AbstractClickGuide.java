package me.aleksilassila.litematica.printer.v1_17.guides;

import me.aleksilassila.litematica.printer.v1_17.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_17.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_17.actions.AbstractAction;
import me.aleksilassila.litematica.printer.v1_17.actions.PrepareAction;
import me.aleksilassila.litematica.printer.v1_17.actions.ReleaseShiftAction;
import me.aleksilassila.litematica.printer.v1_17.implementation.actions.InteractActionImpl;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Guide that clicks the current block to change its state.
 */

public abstract class AbstractClickGuide extends AbstractGuide {
    public AbstractClickGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public List<AbstractAction> execute(ClientPlayerEntity player) {
        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(state.blockPos), Direction.UP, state.blockPos, false);
        ItemStack itemStack = getRequiredItem(player).orElse(ItemStack.EMPTY);
        PrinterPlacementContext ctx = new PrinterPlacementContext(player, hitResult, itemStack, getSlotWithItem(player, itemStack));

        List<AbstractAction> actions = new ArrayList<>();
        actions.add(new ReleaseShiftAction());
        actions.add(new PrepareAction(ctx));
        actions.add(new InteractActionImpl(ctx));

        return actions;
    }

    @Override
    abstract protected @NotNull List<ItemStack> getRequiredItems();
}
