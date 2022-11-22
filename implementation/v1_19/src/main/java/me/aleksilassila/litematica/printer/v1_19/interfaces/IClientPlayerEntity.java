package me.aleksilassila.litematica.printer.v1_19.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.stat.StatHandler;

public abstract class IClientPlayerEntity extends ClientPlayerEntity {
    public IClientPlayerEntity(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
        super(client, world, networkHandler, stats, recipeBook, lastSneaking, lastSprinting);
    }

    abstract PlayerAbilities getPlayerAbilities();
}
