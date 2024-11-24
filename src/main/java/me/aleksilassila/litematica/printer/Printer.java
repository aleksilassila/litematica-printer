package me.aleksilassila.litematica.printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.actions.Action;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.config.Hotkeys;
import me.aleksilassila.litematica.printer.guides.Guide;
import me.aleksilassila.litematica.printer.guides.Guides;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Printer {
    public static final Logger logger = LogManager.getLogger(PrinterReference.MOD_ID);
    @Nonnull
    public final ClientPlayerEntity player;
    public final ActionHandler actionHandler;
    private final Guides interactionGuides = new Guides();

    public Printer(@Nonnull MinecraftClient client, @Nonnull ClientPlayerEntity player) {
        this.player = player;
        this.actionHandler = new ActionHandler(client, player);
    }

    public boolean onGameTick() {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (!actionHandler.acceptsActions()) {
            return false;
        }

        if (worldSchematic == null) {
            return false;
        }

        if (!Configs.PRINT_MODE.getBooleanValue() && !Hotkeys.PRINT.getKeybind().isPressed()) {
            return false;
        }

        PlayerAbilities abilities = player.getAbilities();
        if (!abilities.allowModifyWorld) {
            return false;
        }

        List<BlockPos> positions = getReachablePositions();
        findBlock:
        for (BlockPos position : positions) {
            SchematicBlockState state = new SchematicBlockState(player.getWorld(), worldSchematic, position);
            if (state.targetState.equals(state.currentState) || state.targetState.isAir()) {
                continue;
            }

            Guide[] guides = interactionGuides.getInteractionGuides(state);

            BlockHitResult result = RayTraceUtils.traceToSchematicWorld(player, 10, true, true);
            boolean isCurrentlyLookingSchematic = result != null && result.getBlockPos().equals(position);

            for (Guide guide : guides) {
                // Add INTERACT_BLOCKS pull by DarkReaper231
                if (guide.canExecute(player) && Configs.INTERACT_BLOCKS.getBooleanValue()) {
                    printDebug("Executing {} for {}", guide, state);
                    List<Action> actions = guide.execute(player);
                    actionHandler.addActions(actions.toArray(Action[]::new));
                    return true;
                }
                if (guide.skipOtherGuides()) {
                    continue findBlock;
                }
            }
        }

        return false;
    }

    private List<BlockPos> getReachablePositions() {
        int maxReach = (int) Math.ceil(Configs.PRINTING_RANGE.getDoubleValue());
        double maxReachSquared = MathHelper.square(Configs.PRINTING_RANGE.getDoubleValue());

        ArrayList<BlockPos> positions = new ArrayList<>();

        for (int y = -maxReach; y < maxReach + 1; y++) {
            for (int x = -maxReach; x < maxReach + 1; x++) {
                for (int z = -maxReach; z < maxReach + 1; z++) {
                    BlockPos blockPos = player.getBlockPos().north(x).west(z).up(y);

                    if (!DataManager.getRenderLayerRange().isPositionWithinRange(blockPos)) {
                        continue;
                    }
                    if (this.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(blockPos)) > maxReachSquared) {
                        continue;
                    }

                    positions.add(blockPos);
                }
            }
        }

        return positions.stream()
                .filter(p ->
                {
                    Vec3d vec = Vec3d.ofCenter(p);
                    return this.player.getPos().squaredDistanceTo(vec) > 1
                            && this.player.getEyePos().squaredDistanceTo(vec) > 1;
                })
                .sorted((a, b) ->
                {
                    double aDistance = this.player.getPos().squaredDistanceTo(Vec3d.ofCenter(a));
                    double bDistance = this.player.getPos().squaredDistanceTo(Vec3d.ofCenter(b));
                    return Double.compare(aDistance, bDistance);
                }).toList();
    }

    public static void printDebug(String key, Object... args) {
        if (Configs.PRINT_DEBUG.getBooleanValue()) {
            logger.info(key, args);
        }
    }
}
