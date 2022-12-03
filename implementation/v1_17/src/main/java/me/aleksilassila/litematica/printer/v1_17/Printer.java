package me.aleksilassila.litematica.printer.v1_17;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.v1_17.actions.AbstractAction;
import me.aleksilassila.litematica.printer.v1_17.guides.AbstractGuide;
import me.aleksilassila.litematica.printer.v1_17.implementation.GuidesImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Printer {
    @NotNull
    public final ClientPlayerEntity player;

    public final PacketHandler packetHandler;

    private final Guides interactionGuides = new GuidesImpl();

    public Printer(@NotNull MinecraftClient client, @NotNull ClientPlayerEntity player) {
        this.player = player;

        this.packetHandler = new PacketHandler(client, player);
    }

    public void onGameTick() {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null) return;

        if (!LitematicaMixinMod.PRINT_MODE.getBooleanValue() && !LitematicaMixinMod.PRINT.getKeybind().isPressed())
            return;

        PlayerAbilities abilities = player.getAbilities();
        if (!abilities.allowModifyWorld)
            return;

        if (packetHandler.acceptsActions()) {
            ArrayList<BlockPos> positions = getReachablePositions();
            findBlock:
            for (BlockPos position : positions) {
                SchematicBlockState state = new SchematicBlockState(player.world, worldSchematic, position);
                if (state.targetState.equals(state.currentState)) continue;

                AbstractGuide[] guides = interactionGuides.getInteractionGuides(state);

                boolean isCurrentlyLooking = ((BlockHitResult) player.raycast(20, 1, false)).getBlockPos().equals(position);

                for (AbstractGuide guide : guides) {
                    if (guide.shouldSkip()) continue findBlock;
                    if (guide.canExecute(player)) {
                        System.out.println("Executing " + guide + " for " + state);
                        // interactionGuides.getInteractionGuides(state);
                        List<AbstractAction> actions = guide.execute(player);
                        packetHandler.addActions(actions.toArray(AbstractAction[]::new));
                        break findBlock;
                    }
                }
            }
        }
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

        return output;
    }
}
