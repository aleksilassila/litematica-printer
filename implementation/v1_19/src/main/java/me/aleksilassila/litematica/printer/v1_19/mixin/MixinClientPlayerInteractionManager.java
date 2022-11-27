package me.aleksilassila.litematica.printer.v1_19.mixin;

import me.aleksilassila.litematica.printer.v1_19.interfaces.IClientPlayerInteractionManager;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterPlacementContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager implements IClientPlayerInteractionManager {
    @Shadow
    private MinecraftClient client;


    @Override
    public void rightClickBlock(BlockPos pos, Direction side, Vec3d hitVec) {
        interactBlock(client.player, Hand.MAIN_HAND,
                new BlockHitResult(hitVec, side, pos, false));
        interactItem(client.player, Hand.MAIN_HAND);
//		System.out.println("Printer interactBlock: pos: (" + pos.toShortString() + "), side: " + side.getName() + ", vector: " + hitVec.toString());
    }

    @Override
    public void rightClickBlock(PrinterPlacementContext context) {
        interactBlock(client.player, Hand.MAIN_HAND,
                context.hitResult);
        interactItem(client.player, Hand.MAIN_HAND);
    }

    @Shadow
    public abstract ActionResult interactBlock(ClientPlayerEntity clientPlayerEntity_1, Hand hand_1, BlockHitResult blockHitResult_1);

    @Shadow
    public abstract ActionResult interactItem(PlayerEntity playerEntity_1, Hand hand_1);

    @Inject(at = @At("HEAD"), method = "interactBlock")
    public void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        String out = "Interaction:" +
                "\n - Hand: " + hand.toString() +
                "\n - HitResult: " +
                "\n   - Pos: " + hitResult.getBlockPos().toShortString() +
                "\n   - Side: " + hitResult.getSide().getName() +
                "\n   - Vector: " + hitResult.getPos().toString() +
                "\n   - IsInside: " + hitResult.isInsideBlock();
        System.out.println(out);
    }
}
