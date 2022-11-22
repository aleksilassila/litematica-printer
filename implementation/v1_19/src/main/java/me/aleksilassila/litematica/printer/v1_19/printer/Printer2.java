package me.aleksilassila.litematica.printer.v1_19.printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.implementations.InteractionGuidesImpl;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import me.aleksilassila.litematica.printer.v1_19.printer.guide.AbstractInteractionGuides;
import me.aleksilassila.litematica.printer.v1_19.printer.guide.InteractionGuide;
import net.minecraft.block.AirBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Printer2 extends PrinterUtils {
    @NotNull
    private final MinecraftClient client;
    @NotNull
    private final ClientPlayerEntity player;

    public final PacketHandler packetHandler;

    private final AbstractInteractionGuides interactionGuides = new InteractionGuidesImpl();

    public Printer2(@NotNull MinecraftClient client, @NotNull ClientPlayerEntity player) {
        this.client = client;
        this.player = player;

        this.packetHandler = new PacketHandler(client, player);
    }

    public void onGameTick() {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null) return;

        ArrayList<BlockPos> positions = getReachablePositions();
        for (BlockPos position : positions) {
            SchematicBlockState state = new SchematicBlockState(player.world, worldSchematic, position);
            InteractionGuide guide = interactionGuides.getInteractionGuide(state);

            if (guide != null) {
                if (!guide.canExecute(player, state)) continue;

                List<AbstractAction> actions = guide.getActions(player, state);

                if (actions == null) {
                    System.out.println("Skipping actions for " + state.targetState.getBlock().getName().getString());
                    continue;
                }
                System.out.println("Adding actions for " + state.targetState.getBlock().getClass().getSimpleName());

                packetHandler.addActions(actions.toArray(AbstractAction[]::new));
                break;
            }
        }

        packetHandler.onGameTick();
    }

    private ArrayList<BlockPos> getReachablePositions() {
        int maxReach = (int) MathHelper.square(LitematicaMixinMod.PRINTING_RANGE.getDoubleValue());

        ArrayList<BlockPos> positions = new ArrayList<>();

        for (int y = -maxReach; y < maxReach + 1; y++) {
            for (int x = -maxReach; x < maxReach + 1; x++) {
                for (int z = -maxReach; z < maxReach + 1; z++) {
                    BlockPos blockPos = player.getBlockPos().north(x).west(z).up(y);

                    if (!DataManager.getRenderLayerRange().isPositionWithinRange(blockPos)) continue;
                    if (this.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(blockPos)) > maxReach) {
                        continue;
                    }

                    positions.add(blockPos);
                }
            }
        }

        Map<Integer, ArrayList<BlockPos>> printingLayers = new HashMap<>();

        positions.forEach(blockPos -> {
            int layer = blockPos.getY();

            if (!printingLayers.containsKey(layer)) {
                printingLayers.put(layer, new ArrayList<>());
            }

            printingLayers.get(layer).add(blockPos);
        });

        printingLayers.values().forEach(list -> list.sort((a, b) -> {
            double aDistance = this.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(a));
            double bDistance = this.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(b));

            return Double.compare(aDistance, bDistance);
        }));

        ArrayList<BlockPos> output = new ArrayList<>();

        for (int layer : printingLayers.keySet()) {
            output.addAll(printingLayers.get(layer));
        }

//        output.forEach(p -> {
//            if (((int) player.getX()) == p.getX() && ((int) player.getZ()) == p.getZ()) {
//                SchematicBlockState state = new SchematicBlockState(player.world, SchematicWorldHandler.getSchematicWorld(), p);
//                System.out.println(state);
//            }
//        });

        return output;
    }
}
