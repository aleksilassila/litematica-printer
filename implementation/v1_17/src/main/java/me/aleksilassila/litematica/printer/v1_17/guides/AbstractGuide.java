package me.aleksilassila.litematica.printer.v1_17.guides;

import me.aleksilassila.litematica.printer.v1_17.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_17.actions.AbstractAction;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

abstract public class AbstractGuide {
    protected final SchematicBlockState state;
    protected final BlockState currentState;
    protected final BlockState targetState;

    public AbstractGuide(SchematicBlockState state) {
        this.state = state;

        this.currentState = state.currentState;
        this.targetState = state.targetState;
    }

    protected boolean playerHasRightItem(ClientPlayerEntity player) {
        return getStackSlot(player) != -1;
    }

    protected int getSlotWithItem(ClientPlayerEntity player, ItemStack itemStack) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.main.size(); ++i) {
            if (!inventory.main.get(i).isEmpty() && inventory.main.get(i).isItemEqual(itemStack)) {
                return i;
            }
        }

        return -1;
    }

    protected int getStackSlot(ClientPlayerEntity player) {
        if (player.getAbilities().creativeMode) {
            return player.getInventory().selectedSlot;
        }

        Optional<ItemStack> requiredItem = getRequiredItem(player);
        if (requiredItem.isEmpty()) return -1;

        return getSlotWithItem(player, requiredItem.get());
    }

    public boolean canExecute(ClientPlayerEntity player) {
        if (!playerHasRightItem(player)) return false;

        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        return !targetState.equals(currentState);
    }

    abstract public List<AbstractAction> execute(ClientPlayerEntity player);

    abstract protected @NotNull List<ItemStack> getRequiredItems();

    /**
     * Returns the first required item that player has access to,
     * or empty if the items are inaccessible.
     */
    protected Optional<ItemStack> getRequiredItem(ClientPlayerEntity player) {
        List<ItemStack> requiredItems = getRequiredItems();

        for (ItemStack requiredItem : requiredItems) {
            if (player.getAbilities().creativeMode) return Optional.of(requiredItem);

            int slot = getSlotWithItem(player, requiredItem);
            if (slot > -1 && !requiredItem.isOf(Items.AIR))
                return Optional.of(requiredItem);
        }

        return Optional.empty();
    }

    protected boolean statesEqualIgnoreProperties(BlockState state1, BlockState state2, Property<?>... propertiesToIgnore) {
        if (state1.getBlock() != state2.getBlock()) return false;

        loop:
        for (Property<?> property : state1.getProperties()) {
            for (Property<?> ignoredProperty : propertiesToIgnore) {
                if (property == ignoredProperty) continue loop;
            }

            try {
                if (state1.get(property) != state2.get(property)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if
     */
    protected boolean statesEqual(BlockState state1, BlockState state2) {
        return statesEqualIgnoreProperties(state1, state2);
    }

    public boolean shouldSkip() {
        return false;
    }
}
