package me.aleksilassila.litematica.printer.guides.interaction;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class CycleStateGuide extends InteractionGuide {
    private static final Property<?>[] propertiesToIgnore = new Property[]{
            Properties.POWERED,
            Properties.LIT
    };

    public CycleStateGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (!super.canExecute(player))
            return false;

        return targetState.getBlock() == currentState.getBlock();
    }

    @Override
    protected @Nonnull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(ItemStack.EMPTY);
    }

    @Override
    protected boolean statesEqual(BlockState state1, BlockState state2) {
        if (state2.getBlock() instanceof LeverBlock) {
            return super.statesEqual(state1, state2);
        }

        return statesEqualIgnoreProperties(state1, state2, propertiesToIgnore);
    }
}
