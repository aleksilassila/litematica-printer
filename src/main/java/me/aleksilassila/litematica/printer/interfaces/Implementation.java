package me.aleksilassila.litematica.printer.interfaces;

import me.aleksilassila.litematica.printer.printer.Printer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Direction;

import java.lang.reflect.Field;

/**
 * Dirty class that contains anything and everything that is
 * required to access variables and functions that are inconsistent
 * across different minecraft versions. In other words, this should
 * be the only file that has to be changed in every printer branch.
 */
public class Implementation {
    public static PlayerInventory getInventory(ClientPlayerEntity playerEntity) {
        return playerEntity.inventory;
    }

    public static PlayerAbilities getAbilities(ClientPlayerEntity playerEntity) {
        return playerEntity.abilities;
    }

    public static float getYaw(ClientPlayerEntity playerEntity) {
        return playerEntity.yaw;
    }

    public static float getPitch(ClientPlayerEntity playerEntity) {
        return playerEntity.pitch;
    }

    public static void sendLookPacket(ClientPlayerEntity playerEntity, Direction playerShouldBeFacing) {
        playerEntity.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(
                Implementation.getRequiredYaw(playerEntity, playerShouldBeFacing),
                Implementation.getRequiredPitch(playerEntity, playerShouldBeFacing),
                playerEntity.isOnGround()));
    }

    public static boolean isLookPacket(Packet<?> packet) {
        return packet instanceof PlayerMoveC2SPacket && !(packet instanceof PlayerMoveC2SPacket.PositionOnly);
    }

    public static Packet<?> getMoveOnlyPacket(ClientPlayerEntity playerEntity, Packet<?> packet) {
        if (Printer.Queue.playerShouldBeFacing == null) return packet;
        try {
            Field yawField = PlayerMoveC2SPacket.class.getDeclaredField("yaw");
            Field pitchField = PlayerMoveC2SPacket.class.getDeclaredField("pitch");

            yawField.setAccessible(true);
            pitchField.setAccessible(true);

            yawField.setFloat(packet, Implementation.getRequiredYaw(playerEntity, Printer.Queue.playerShouldBeFacing));
            pitchField.setFloat(packet, Implementation.getRequiredPitch(playerEntity, Printer.Queue.playerShouldBeFacing));

            return packet;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return packet;
        }
    }

    protected static float getRequiredYaw(ClientPlayerEntity playerEntity, Direction playerShouldBeFacing) {
        if (playerShouldBeFacing.getAxis().isHorizontal()) {
            return playerShouldBeFacing.asRotation();
        } else {
            return Implementation.getYaw(playerEntity);
        }
    }

    protected static float getRequiredPitch(ClientPlayerEntity playerEntity, Direction playerShouldBeFacing) {
        if (playerShouldBeFacing.getAxis().isVertical()) {
            return playerShouldBeFacing == Direction.DOWN ? 90 : -90; // FIXME make this less sus too
        } else {
            float pitch = Implementation.getPitch(playerEntity);
            return Math.abs(pitch) < 40 ? pitch : pitch / Math.abs(pitch) * 40;
        }
    }

    public enum NewBlocks {
        LICHEN(null),
        ROD(null),
        CANDLES(null),
        AMETHYST(null);

        public Class<?> clazz;

        NewBlocks(Class<?> clazz) {
            this.clazz = clazz;
        }
    }
}
