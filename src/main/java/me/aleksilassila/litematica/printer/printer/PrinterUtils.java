package me.aleksilassila.litematica.printer.printer;

import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;

public class PrinterUtils {
    protected static int getBlockHalf(BlockState requiredState, BlockState currentState) {
    	if (requiredState.contains(SlabBlock.TYPE)) {
    		if (requiredState.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
    			if (currentState.contains(SlabBlock.TYPE))
    				return currentState.get(SlabBlock.TYPE) == SlabType.TOP ? 0 : 1;
    			else
    				return 0;
			} else {
    			return requiredState.get(SlabBlock.TYPE) == SlabType.TOP ? 1 : 0;
			}
		} else if (requiredState.contains(StairsBlock.HALF)) {
			return requiredState.get(StairsBlock.HALF) == BlockHalf.TOP ? 1 : 0;
		} else if (requiredState.contains(TrapdoorBlock.HALF)) {
    		return requiredState.get(TrapdoorBlock.HALF) == BlockHalf.TOP ? 1 : 0;
		}

    	return -1;
	}

	protected static Direction getFacingDirection(BlockState state) {
		Direction dir = null;

		if (state.getBlock() instanceof PillarBlock) {
			return null;
		}

		if (!containsProperty(state, "facing")) return null;

    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase("facing")) {
				dir = (Direction)state.get(prop);
			}
		}

		if (!shouldntGetOpposite(state)) {
			dir = dir.getOpposite();
		}

		if (shouldRotate(state)) {
			dir = dir.rotateYCounterclockwise();
		}

    	return dir;
	}

	protected static boolean isWaterLogged(BlockState state) {
    	if (!(state.getBlock() instanceof Waterloggable)) return false;
    	if (!containsProperty(state, "waterlogged")) return false;

		for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase("waterlogged")) {
				return (Boolean) state.get(prop);
			}
		}

		return false;
	}

	protected static boolean isTorchOnWall(BlockState state) {
    	if (state.getBlock() instanceof WallTorchBlock) return true;
    	if (!(state.getBlock() instanceof TorchBlock)) return false;

		for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase("facing")) {
				return true;
			}
		}

    	return false;
	}

	protected static boolean isLeverOnWall(BlockState state) {
    	if (!(state.getBlock() instanceof LeverBlock) && !(state.getBlock() instanceof AbstractButtonBlock)) {
    		return false;
		}

    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase("face")) {
				if (state.get(prop).toString().equalsIgnoreCase("wall")) {
					return true;
				}

				break;
			}
		}

    	return false;
	}

	private static boolean containsProperty(BlockState state, String name) {
    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}

    	return false;
	}

	protected static boolean shouldntGetOpposite(BlockState state) {
		return state.getBlock() instanceof StairsBlock
				|| state.getBlock() instanceof GrindstoneBlock
				|| state.getBlock() instanceof LeverBlock
				|| state.getBlock() instanceof AbstractButtonBlock
				|| state.getBlock() instanceof BedBlock
				|| state.getBlock() instanceof DoorBlock
				|| state.getBlock() instanceof ObserverBlock;
	}

	protected static boolean shouldClickBlock(BlockState state, BlockState targetState) {
    	Block block = state.getBlock();

    	if (!(block instanceof RepeaterBlock) && !(block instanceof ComparatorBlock)) return false;
    	if (block.getClass() != targetState.getBlock().getClass()) return false;

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
    	if (state.getBlock() instanceof PillarBlock && state.contains(PillarBlock.AXIS)) {
    		return state.get(PillarBlock.AXIS);
		}

    	return null;
	}

	protected static boolean isFlowingBlock(BlockState state) {
    	return state.contains(FlowableFluid.LEVEL) && state.get(FlowableFluid.LEVEL) > 0;
	}

    protected static boolean isDoubleSlab(BlockState state) {
    	return state.contains(SlabBlock.TYPE) && state.get(SlabBlock.TYPE) == SlabType.DOUBLE;
    }

	protected static boolean isHalfSlab(BlockState state) {
    	return state.contains(SlabBlock.TYPE) && state.get(SlabBlock.TYPE) != SlabType.DOUBLE;
	}
}
