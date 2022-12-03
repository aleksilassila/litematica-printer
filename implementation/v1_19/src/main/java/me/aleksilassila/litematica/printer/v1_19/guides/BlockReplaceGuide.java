package me.aleksilassila.litematica.printer.v1_19.guides;

import me.aleksilassila.litematica.printer.v1_19.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.SchematicBlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

public class BlockReplaceGuide extends AbstractPlacementGuide {
    private static final HashMap<IntProperty, Item> increasingProperties = new HashMap<>();

    static {
        addProperties();
    }

    private Integer currentLevel = null;
    private Integer targetLevel = null;
    private IntProperty increasingProperty = null;

    protected static void addProperties() {
        increasingProperties.put(SnowBlock.LAYERS, null);
        increasingProperties.put(SeaPickleBlock.PICKLES, null);
        increasingProperties.put(CandleBlock.CANDLES, null);
//            increasingProperties.put(LeveledCauldronBlock.LEVEL, Items.GLASS_BOTTLE);
    }

    public BlockReplaceGuide(SchematicBlockState state) {
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
    protected boolean getRequiresShift(SchematicBlockState state) {
        return false;
    }

    @Override
    public @Nullable PrinterPlacementContext getPlacementContext(ClientPlayerEntity player) {
        Optional<ItemStack> requiredItem = getRequiredItem(player);
        if (requiredItem.isEmpty()) return null;

        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(state.blockPos), Direction.UP, state.blockPos, false);
        return new PrinterPlacementContext(player, hitResult, requiredItem.get(), getSlotWithItem(player, requiredItem.get()));
    }

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (currentLevel == null || targetLevel == null || increasingProperty == null) return false;
        if (!statesEqualIgnoreProperties(currentState, targetState, increasingProperty)) return false;
        if (currentLevel >= targetLevel) return false;

        return super.canExecute(player);
    }
}
