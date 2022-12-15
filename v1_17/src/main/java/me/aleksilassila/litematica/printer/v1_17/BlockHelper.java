package me.aleksilassila.litematica.printer.v1_17;

import net.minecraft.block.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract public class BlockHelper {
    public static List<Class<?>> interactiveBlocks = new ArrayList<>(Arrays.asList(
            AbstractChestBlock.class, AbstractFurnaceBlock.class, CraftingTableBlock.class, LeverBlock.class,
            DoorBlock.class, TrapdoorBlock.class, BedBlock.class, RedstoneWireBlock.class, ScaffoldingBlock.class,
            HopperBlock.class, EnchantingTableBlock.class, NoteBlock.class, JukeboxBlock.class, CakeBlock.class,
            FenceGateBlock.class, BrewingStandBlock.class, DragonEggBlock.class, CommandBlock.class,
            BeaconBlock.class, AnvilBlock.class, ComparatorBlock.class, RepeaterBlock.class,
            DropperBlock.class, DispenserBlock.class, ShulkerBoxBlock.class, LecternBlock.class,
            FlowerPotBlock.class, BarrelBlock.class, BellBlock.class, SmithingTableBlock.class,
            LoomBlock.class, CartographyTableBlock.class, GrindstoneBlock.class,
            StonecutterBlock.class, AbstractSignBlock.class));
}
