package me.aleksilassila.litematica.printer;

import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SchematicBlockState {
    public final World world;
    public final WorldSchematic schematic;
    public final BlockPos blockPos;
    public final BlockState targetState;
    public final BlockState currentState;

    public SchematicBlockState(World world, WorldSchematic schematic, BlockPos blockPos) {
        this.world = world;
        this.schematic = schematic;
        this.blockPos = blockPos;
        this.targetState = schematic.getBlockState(blockPos);
        this.currentState = world.getBlockState(blockPos);
    }

    public SchematicBlockState offset(Direction direction) {
        return new SchematicBlockState(world, schematic, blockPos.offset(direction));
    }

    @Override
    public String toString() {
        return "SchematicBlockState{" +
                "world=" + world +
                ", schematic=" + schematic +
                ", blockPos=" + blockPos +
                ", targetState=" + targetState +
                ", currentState=" + currentState +
                '}';
    }
}
