package me.aleksilassila.litematica.printer.v1_19.actions;

import me.aleksilassila.litematica.printer.v1_19.PrinterPlacementContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class PrepareAction extends AbstractAction {
//    public final Direction lookDirection;
//    public final boolean requireSneaking;
//    public final Item item;

//    public PrepareAction(Direction lookDirection, boolean requireSneaking, Item item) {
//        this.lookDirection = lookDirection;
//        this.requireSneaking = requireSneaking;
//        this.item = item;
//    }
//
//    public PrepareAction(Direction lookDirection, boolean requireSneaking, BlockState requiredState) {
//        this(lookDirection, requireSneaking, requiredState.getBlock().asItem());
//    }

    public final PrinterPlacementContext context;

    public PrepareAction(PrinterPlacementContext context) {
        this.context = context;
    }

    @Override
    public Direction lockedLookDirection() {
        return context.lookDirection;
    }

    @Override
    public void send(MinecraftClient client, ClientPlayerEntity player) {
        ItemStack itemStack = context.getStack();
        int slot = context.requiredItemSlot;

        if (itemStack != null) {
            PlayerInventory inventory = player.getInventory();

            // This thing is straight from MinecraftClient#doItemPick()
            if (player.getAbilities().creativeMode) {
                inventory.addPickBlock(itemStack);
                client.interactionManager.clickCreativeStack(player.getStackInHand(Hand.MAIN_HAND), 36 + inventory.selectedSlot);
            } else if (slot != -1) {
                if (PlayerInventory.isValidHotbarIndex(slot)) {
                    inventory.selectedSlot = slot;
                } else {
                    client.interactionManager.pickFromInventory(slot);
                }
            }
        }

        if (context.lookDirection != null) {
//            Implementation.sendLookPacket(player, context.lookDirection);
            float yaw = player.getYaw();
            float pitch = player.getPitch();

            if (context.lookDirection.getAxis().isHorizontal()) {
                yaw = context.lookDirection.asRotation();
            } else {
                pitch = context.lookDirection == Direction.UP ? -90 : 90;
            }

            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), yaw, pitch, player.isOnGround());

            player.networkHandler.sendPacket(packet);
        }

        if (context.requiresSneaking) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }

    @Override
    public String toString() {
        return "PrepareAction{" +
                "context=" + context +
                '}';
    }
}
