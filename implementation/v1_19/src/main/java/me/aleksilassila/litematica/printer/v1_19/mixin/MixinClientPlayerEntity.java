package me.aleksilassila.litematica.printer.v1_19.mixin;

import com.mojang.authlib.GameProfile;
import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.printer.Printer;
import me.aleksilassila.litematica.printer.v1_19.printer.Printer2;
import me.aleksilassila.litematica.printer.v1_19.printer.UpdateChecker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
    private static boolean didCheckForUpdates = false;

    @Shadow
    protected MinecraftClient client;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile, @Nullable PlayerPublicKey publicKey) {
        super(world, profile, publicKey);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void tick(CallbackInfo ci) {
        ClientPlayerEntity clientPlayer = (ClientPlayerEntity) (Object) this;
        if (!didCheckForUpdates) {
            didCheckForUpdates = true;

            checkForUpdates();
        }

        if (LitematicaMixinMod.printer == null || LitematicaMixinMod.printer.player != clientPlayer) {
            System.out.println("Initializing printer, player: " + clientPlayer + ", client: " + client);
            LitematicaMixinMod.printer = new Printer2(client, clientPlayer);
        }

        LitematicaMixinMod.printer.onGameTick();
        LitematicaMixinMod.printer.packetHandler.onGameTick();
//
//        if (Printer.getPrinter() == null) {
//            Printer.init(client);
//            return;
//        }

//        Printer.getPrinter().tick();
    }

    public void checkForUpdates() {
        new Thread(() -> {
            String version = UpdateChecker.version;
            String newVersion = UpdateChecker.getPrinterVersion();

            if (!version.equals(newVersion)) {
                client.inGameHud.getChatHud().addMessage(Text.literal("New version of Litematica Printer available in https://github.com/aleksilassila/litematica-printer/releases"));
            }
        }).start();
    }
}
