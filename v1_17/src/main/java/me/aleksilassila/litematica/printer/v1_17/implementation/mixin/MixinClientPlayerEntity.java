package me.aleksilassila.litematica.printer.v1_17.implementation.mixin;

import com.mojang.authlib.GameProfile;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.v1_17.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_17.Printer;
import me.aleksilassila.litematica.printer.v1_17.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_17.UpdateChecker;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
    private static boolean didCheckForUpdates = false;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    protected MinecraftClient client;
    @Shadow
    public ClientPlayNetworkHandler networkHandler;

    @Inject(at = @At("TAIL"), method = "tick")
    public void tick(CallbackInfo ci) {
        ClientPlayerEntity clientPlayer = (ClientPlayerEntity) (Object) this;
        if (!didCheckForUpdates) {
            didCheckForUpdates = true;

            checkForUpdates();
        }

        if (LitematicaMixinMod.printer == null || LitematicaMixinMod.printer.player != clientPlayer) {
            System.out.println("Initializing printer, player: " + clientPlayer + ", client: " + client);
            LitematicaMixinMod.printer = new Printer(client, clientPlayer);
        }

        // Dirty optimization
        boolean didFindPlacement = true;
        for (int i = 0; i < 10; i++) {
            if (didFindPlacement) {
                didFindPlacement = LitematicaMixinMod.printer.onGameTick();
            }
            LitematicaMixinMod.printer.actionHandler.onGameTick();
        }
    }

    public void checkForUpdates() {
        new Thread(() -> {
            String version = UpdateChecker.version;
            String newVersion = UpdateChecker.getPrinterVersion();

            if (!version.equals(newVersion)) {
                client.inGameHud.addChatMessage(MessageType.SYSTEM,
                        new LiteralText("New version of Litematica Printer available in https://github.com/aleksilassila/litematica-printer/releases"),
                        null);
            }
        }).start();
    }

    @Inject(method = "openEditSignScreen", at = @At("HEAD"), cancellable = true)
    public void openEditSignScreen(SignBlockEntity sign, CallbackInfo ci) {
        getTargetSignEntity(sign).ifPresent(signBlockEntity -> {
            UpdateSignC2SPacket packet = new UpdateSignC2SPacket(sign.getPos(),
                    signBlockEntity.getTextOnRow(0, false).getString(),
                    signBlockEntity.getTextOnRow(1, false).getString(),
                    signBlockEntity.getTextOnRow(2, false).getString(),
                    signBlockEntity.getTextOnRow(3, false).getString());
            this.networkHandler.sendPacket(packet);
            ci.cancel();
        });
    }

    private Optional<SignBlockEntity> getTargetSignEntity(SignBlockEntity sign) {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
        SchematicBlockState state = new SchematicBlockState(sign.getWorld(), worldSchematic, sign.getPos());

        BlockEntity targetBlockEntity = worldSchematic.getBlockEntity(state.blockPos);

        if (targetBlockEntity instanceof SignBlockEntity targetSignEntity) {
            return Optional.of(targetSignEntity);
        }

        return Optional.empty();
    }
}
