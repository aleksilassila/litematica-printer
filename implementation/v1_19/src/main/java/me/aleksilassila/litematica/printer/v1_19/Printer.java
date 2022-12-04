package me.aleksilassila.litematica.printer.v1_19;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.v1_19.actions.AbstractAction;
import me.aleksilassila.litematica.printer.v1_19.guides.Guide;
import me.aleksilassila.litematica.printer.v1_19.guides.Guides;
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

    public final ActionHandler packetHandler;

    private final Guides interactionGuides = new Guides();

    public Printer(@NotNull MinecraftClient client, @NotNull ClientPlayerEntity player) {
        this.player = player;

        this.packetHandler = new ActionHandler(client, player);
    }

    public boolean onGameTick() {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (!packetHandler.acceptsActions()) return false;

        if (worldSchematic == null) return false;

        if (!LitematicaMixinMod.PRINT_MODE.getBooleanValue() && !LitematicaMixinMod.PRINT.getKeybind().isPressed())
            return false;

        PlayerAbilities abilities = player.getAbilities();
        if (!abilities.allowModifyWorld)
            return false;

        List<BlockPos> positions = getReachablePositions();
        findBlock:
        for (BlockPos position : positions) {
            SchematicBlockState state = new SchematicBlockState(player.world, worldSchematic, position);
            if (state.targetState.equals(state.currentState) || state.targetState.isAir()) continue;

            Guide[] guides = interactionGuides.getInteractionGuides(state);

            boolean isCurrentlyLooking = ((BlockHitResult) player.raycast(20, 1, false)).getBlockPos().equals(position);

            for (Guide guide : guides) {
                if (guide.shouldSkip()) continue findBlock;
                if (guide.canExecute(player)) {
                    System.out.println("Executing " + guide + " for " + state);
                    // interactionGuides.getInteractionGuides(state);
                    List<AbstractAction> actions = guide.execute(player);
                    packetHandler.addActions(actions.toArray(AbstractAction[]::new));
                    return true;
                }
            }
        }

        return false;
    }

    private List<BlockPos> getReachablePositions() {
        int maxReach = (int) Math.ceil(LitematicaMixinMod.PRINTING_RANGE.getDoubleValue());
        int maxReachSquared = (int) MathHelper.square(LitematicaMixinMod.PRINTING_RANGE.getDoubleValue());

        ArrayList<BlockPos> positions = new ArrayList<>();

        for (int y = -maxReach; y < maxReach + 1; y++) {
            for (int x = -maxReach; x < maxReach + 1; x++) {
                for (int z = -maxReach; z < maxReach + 1; z++) {
                    BlockPos blockPos = player.getBlockPos().north(x).west(z).up(y);

                    if (!DataManager.getRenderLayerRange().isPositionWithinRange(blockPos)) continue;
                    if (this.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(blockPos)) > maxReachSquared) {
                        continue;
                    }

                    positions.add(blockPos);
                }
            }
        }

        return positions.stream()
                .filter(p -> {
                    Vec3d vec = Vec3d.ofCenter(p);
                    return this.player.getPos().squaredDistanceTo(vec) > 1 && this.player.getEyePos().squaredDistanceTo(vec) > 1;
                })
                .sorted((a, b) -> {
                    double aDistance = this.player.getPos().squaredDistanceTo(Vec3d.ofCenter(a));
                    double bDistance = this.player.getPos().squaredDistanceTo(Vec3d.ofCenter(b));
                    return Double.compare(aDistance, bDistance);
                }).toList();
    }
}
