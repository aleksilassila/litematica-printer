package me.aleksilassila.litematica.printer.guides;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.actions.Action;
import me.aleksilassila.litematica.printer.implementation.BlockHelperImpl;
import net.minecraft.block.BlockState;
import net.minecraft.block.CoralBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

abstract public class Guide extends BlockHelperImpl {
    protected final SchematicBlockState state;
    protected final BlockState currentState;
    protected final BlockState targetState;

    public Guide(SchematicBlockState state) {
        this.state = state;
        this.currentState = state.currentState;
        this.targetState = state.targetState;
    }

    protected boolean playerHasRightItem(ClientPlayerEntity player) {
        return getRequiredItemStackSlot(player) != -1;
    }

    protected int getSlotWithItem(ClientPlayerEntity player, ItemStack itemStack) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.main.size(); ++i) {
            if (itemStack.isEmpty() && inventory.main.get(i).isOf(itemStack.getItem())) {
                return i;
            }
            if (!inventory.main.get(i).isEmpty() && ItemStack.areItemsEqual(inventory.main.get(i), itemStack)) {
                return i;
            }
        }

        return -1;
    }

    protected int getRequiredItemStackSlot(ClientPlayerEntity player) {
        if (player.getAbilities().creativeMode) {
            return player.getInventory().selectedSlot;
        }

        Optional<ItemStack> requiredItem = getRequiredItem(player);
        return requiredItem.map(itemStack -> getSlotWithItem(player, itemStack)).orElse(-1);
    }

    public boolean canExecute(ClientPlayerEntity player) {
        if (!playerHasRightItem(player)) {
            return false;
        }

        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        return !statesEqual(targetState, currentState);
    }

    abstract public @Nonnull List<Action> execute(ClientPlayerEntity player);

    abstract protected @Nonnull List<ItemStack> getRequiredItems();

    /**
     * Returns the first required item that the player has access to,
     * or empty if the items are inaccessible.
     */
    protected Optional<ItemStack> getRequiredItem(ClientPlayerEntity player) {
        List<ItemStack> requiredItems = getRequiredItems();

        for (ItemStack requiredItem : requiredItems) {
            if (player.getAbilities().creativeMode) {
                return Optional.of(requiredItem);
            }

            int slot = getSlotWithItem(player, requiredItem);
            if (slot > -1) {
                return Optional.of(requiredItem);
            }
        }

        return Optional.empty();
    }

    protected boolean statesEqualIgnoreProperties(BlockState state1, BlockState state2,
                                                  Property<?>... propertiesToIgnore) {
        if (state1.getBlock() != state2.getBlock()) {
            return false;
        }

        loop:
        for (Property<?> property : state1.getProperties()) {
            if (property == Properties.WATERLOGGED && !(state1.getBlock() instanceof CoralBlock)) {
                continue;
            }

            for (Property<?> ignoredProperty : propertiesToIgnore) {
                if (property == ignoredProperty) {
                    continue loop;
                }
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

    protected static <T extends Comparable<T>> Optional<T> getProperty(BlockState blockState, Property<T> property) {
        if (blockState.contains(property)) {
            return Optional.of(blockState.get(property));
        }
        return Optional.empty();
    }

    /**
     * Returns true if
     */
    protected boolean statesEqual(BlockState state1, BlockState state2) {
        return statesEqualIgnoreProperties(state1, state2);
    }

    public boolean skipOtherGuides() {
        return false;
    }
}
