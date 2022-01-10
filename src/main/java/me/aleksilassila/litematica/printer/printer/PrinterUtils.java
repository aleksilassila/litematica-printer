package me.aleksilassila.litematica.printer.printer;

import me.aleksilassila.litematica.printer.interfaces.Implementation;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;

public class PrinterUtils {
	public static boolean playerHasAccessToItem(ClientPlayerEntity playerEntity, Item item) {
		return playerHasAccessToItems(playerEntity, new Item[]{item});
	}

	public static boolean playerHasAccessToItems(ClientPlayerEntity playerEntity, Item[] items) {
		if (items == null) return false;
		if (Implementation.getAbilities(playerEntity).creativeMode) return true;
		else {
			Inventory inv = Implementation.getInventory(playerEntity);

			for (Item item : items) {
				for (int i = 0; i < inv.size(); i++) {
					if (inv.getStack(i).getItem() == item && inv.getStack(i).getCount() > 0)
						return true;
				}
			}
		}

		return false;
	}

    protected static boolean isDoubleSlab(BlockState state) {
    	return state.contains(SlabBlock.TYPE) && state.get(SlabBlock.TYPE) == SlabType.DOUBLE;
    }

	protected static boolean isHalfSlab(BlockState state) {
    	return state.contains(SlabBlock.TYPE) && state.get(SlabBlock.TYPE) != SlabType.DOUBLE;
	}

    public static Direction getHalf(BlockHalf half) {
        return half == BlockHalf.TOP ? Direction.UP : Direction.DOWN;
    }

    public static Direction axisToDirection(Direction.Axis axis) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == axis) return direction;
        }

        return Direction.DOWN;
    }

    public static Comparable<?> getPropertyByName(BlockState state, String name) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equalsIgnoreCase(name)) {
                return state.get(prop);
            }
        }

        return null;
    }
}
