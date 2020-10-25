package fi.dy.masa.litematica.printer;

import net.minecraft.block.*;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;

public class PrinterUtils {
    protected static int getBlockHalf(BlockState state) {
    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equals("type") || prop.getName().equals("half")) {
				return state.get(prop).toString().equals("top") ? 1 : 0;
			}
		}

    	return -1;
	}

	protected static Direction getFacingDirection(BlockState state) {
		Direction dir = null;

		if (state.getBlock() instanceof PillarBlock) {
			return null;
		}

    	for (Property<?> prop : state.getProperties()) {
			if (prop instanceof DirectionProperty) {
				dir = (Direction)state.get(prop);
			}
		}

    	if (dir == null) return null;

		if (shouldGetOpposite(state)) {
			dir = dir.getOpposite();
		}

		if (shouldRotate(state)) {
			dir = dir.rotateYCounterclockwise();
		}

    	return dir;
	}

	protected static boolean shouldGetOpposite(BlockState state) {
		return state.getBlock() instanceof AbstractFurnaceBlock
				|| state.getBlock() instanceof PistonBlock
				|| state.getBlock() instanceof BarrelBlock
				|| state.getBlock() instanceof TrapdoorBlock
				|| state.getBlock() instanceof StonecutterBlock
				|| state.getBlock() instanceof WallTorchBlock
				|| state.getBlock() instanceof LecternBlock
				|| state.getBlock() instanceof RepeaterBlock
				|| state.getBlock() instanceof ComparatorBlock
				|| state.getBlock() instanceof DispenserBlock
				|| state.getBlock() instanceof ChestBlock;
	}

	protected static boolean shouldClickBlock(BlockState state, BlockState targetState) {
    	Block block = state.getBlock();

    	if (!(block instanceof RepeaterBlock) && !(block instanceof ComparatorBlock)) return false;
    	if (state.getClass() != targetState.getClass()) return false;

    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equals("delay") || prop.getName().equals("mode")) {
				if (!targetState.get(prop).equals(state.get(prop))) return true;
			}
		}

    	return false;
	}

	protected static boolean shouldRotate(BlockState state) {
		return state.getBlock() instanceof AnvilBlock;
	}

	protected static Direction.Axis availableAxis(BlockState state) {
    	if (state.getBlock() instanceof PillarBlock) {
    		for (Property<?> prop : state.getProperties()) {
    			if (state.get(prop) instanceof Direction.Axis) {
    				return (Direction.Axis) state.get(prop);
				}
			}
		}

    	return null;
	}

	protected static boolean isFlowingBlock(BlockState state) {
		if (state.getMaterial().equals(Material.WATER) || state.getMaterial().equals(Material.LAVA)) {
			for (Property<?> prop : state.getProperties()) {
				if (prop instanceof IntProperty) {
					if ((Integer) state.get(prop) > 0) return true;
				}
			}
		}

		return false;
	}
}
