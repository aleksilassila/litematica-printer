package me.aleksilassila.litematica.printer.v1_19_4.guides.interaction;

import me.aleksilassila.litematica.printer.v1_19_4.implementation.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19_4.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19_4.actions.Action;
import me.aleksilassila.litematica.printer.v1_19_4.actions.PrepareAction;
import me.aleksilassila.litematica.printer.v1_19_4.actions.ReleaseShiftAction;
import me.aleksilassila.litematica.printer.v1_19_4.guides.Guide;
import me.aleksilassila.litematica.printer.v1_19_4.implementation.actions.InteractActionImpl;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A guide that clicks the current block to change its state.
 */
public abstract class InteractionGuide extends Guide {
    public InteractionGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public @NotNull List<Action> execute(ClientPlayerEntity player) {
        List<Action> actions = new ArrayList<>();

        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(state.blockPos), Direction.UP, state.blockPos, false);
        ItemStack requiredItem = getRequiredItem(player).orElse(ItemStack.EMPTY);
        int requiredSlot = getRequiredItemStackSlot(player);

        if (requiredSlot == -1) return actions;

        PrinterPlacementContext ctx = new PrinterPlacementContext(player, hitResult, requiredItem, requiredSlot);

        actions.add(new ReleaseShiftAction());
        actions.add(new PrepareAction(ctx));
        actions.add(new InteractActionImpl(ctx));

        return actions;
    }

    @Override
    abstract protected @NotNull List<ItemStack> getRequiredItems();
}
