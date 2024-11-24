package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.guides.Guide;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;

public class BlockReplacementGuide extends PlacementGuide {
    private static final HashMap<IntProperty, Item> increasingProperties = new HashMap<>();

    static {
        increasingProperties.put(SnowBlock.LAYERS, null);
        increasingProperties.put(SeaPickleBlock.PICKLES, null);
        increasingProperties.put(CandleBlock.CANDLES, null);
    }

    private Integer currentLevel = null;
    private Integer targetLevel = null;
    private IntProperty increasingProperty = null;

    public BlockReplacementGuide(SchematicBlockState state) {
        super(state);

        for (IntProperty property : increasingProperties.keySet()) {
            if (targetState.contains(property) && currentState.contains(property)) {
                currentLevel = currentState.get(property);
                targetLevel = targetState.get(property);
                increasingProperty = property;
                break;
            }
        }
    }

    @Override
    protected boolean getUseShift(SchematicBlockState state) {
        return false;
    }

    @Override
    public @Nullable PrinterPlacementContext getPlacementContext(ClientPlayerEntity player) {
        Optional<ItemStack> requiredItem = getRequiredItem(player);
        int slot = getRequiredItemStackSlot(player);
        if (requiredItem.isEmpty() || slot == -1) return null;

        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(state.blockPos), Direction.UP, state.blockPos, false);
        return new PrinterPlacementContext(player, hitResult, requiredItem.get(), slot);
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (Guide.getProperty(targetState, SlabBlock.TYPE).orElse(null) == SlabType.DOUBLE && Guide.getProperty(currentState, SlabBlock.TYPE).orElse(SlabType.DOUBLE) != SlabType.DOUBLE) {
            return super.canExecute(player);
        }

        if (currentLevel == null || targetLevel == null || increasingProperty == null) return false;
        if (!statesEqualIgnoreProperties(currentState, targetState, CandleBlock.LIT, increasingProperty)) return false;
        if (currentLevel >= targetLevel) return false;

        return super.canExecute(player);
    }
}
