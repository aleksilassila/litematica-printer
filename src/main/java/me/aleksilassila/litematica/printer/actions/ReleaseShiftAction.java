package me.aleksilassila.litematica.printer.actions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.PlayerInput;

public class ReleaseShiftAction extends Action {
    @Override
    public void send(MinecraftClient client, ClientPlayerEntity player) {
        player.input.playerInput = new PlayerInput(player.input.playerInput.forward(), player.input.playerInput.backward(), player.input.playerInput.left(), player.input.playerInput.right(), player.input.playerInput.jump(), false, player.input.playerInput.sprint());
        player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
    }
}
