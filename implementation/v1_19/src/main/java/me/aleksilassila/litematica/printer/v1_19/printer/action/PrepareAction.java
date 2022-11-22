package me.aleksilassila.litematica.printer.v1_19.printer.action;

import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.util.math.Direction;

public class PrepareAction extends AbstractAction {
    public final Direction lookDirection;
    public final boolean requireSneaking;
    public final Item item;

    public PrepareAction(Direction lookDirection, boolean requireSneaking, Item item) {
        this.lookDirection = lookDirection;
        this.requireSneaking = requireSneaking;
        this.item = item;
    }

    public PrepareAction(Direction lookDirection, boolean requireSneaking, BlockState requiredState) {
        this(lookDirection, requireSneaking, requiredState.getBlock().asItem());
    }

    @Override
    public Direction lockedLookDirection() {
        return lookDirection;
    }

    @Override
    public void send(MinecraftClient client, ClientPlayerEntity player) {
        if (item != null) {
            if (Implementation.getAbilities(player).creativeMode) {
                player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(Implementation.getInventory(player).selectedSlot, item.getDefaultStack()));
            } else {
                int slot = getItemSlot(player, item);
                if (slot >= 0)
                    player.networkHandler.sendPacket(new PickFromInventoryC2SPacket(slot));
            }
        }

        if (lookDirection != null) {
            Implementation.sendLookPacket(player, lookDirection);
        }

        if (requireSneaking) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }
}
