package me.aleksilassila.litematica.printer.v1_19.printer;

import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.printer.action.AbstractAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Direction;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PacketHandler {
    private final MinecraftClient client;
    private final ClientPlayerEntity player;

    private final Queue<AbstractAction> actionQueue = new LinkedList<>();
    public Direction lockedLookDirection = null;

    public PacketHandler(MinecraftClient client, ClientPlayerEntity player) {
        this.client = client;
        this.player = player;
    }

    private int tick = 0;

    public void onGameTick() {
        int tickRate = LitematicaMixinMod.PRINT_INTERVAL.getIntegerValue();

        tick = tick % tickRate == tickRate - 1 ? 0 : tick + 1;
        if (tick % tickRate != 0) {
            return;
        }

        AbstractAction nextAction = actionQueue.poll();
        if (nextAction != null) {
//            if (nextAction.lockedLookDirection() != null)
//                lockedLookDirection = nextAction.lockedLookDirection();

            System.out.println("Sending action " + nextAction.getClass().getSimpleName());
            nextAction.send(client, player);
        } else {
//            lockedLookDirection = null;
        }
    }

    public boolean acceptsActions() {
        return actionQueue.isEmpty();
    }

    public void addActions(AbstractAction... actions) {
        if (!acceptsActions()) return;

        for (AbstractAction action : actions) {
            if (action.lockedLookDirection() != null)
                lockedLookDirection = action.lockedLookDirection();
        }
        actionQueue.addAll(List.of(actions));
    }
}
