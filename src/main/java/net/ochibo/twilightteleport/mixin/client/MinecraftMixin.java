package net.ochibo.twilightteleport.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screens.Screen;
import net.ochibo.twilightteleport.TeleportCameraController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public abstract class MinecraftMixin {

    
    @Inject(
            method = "setScreen",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$hideDimensionLoadingScreen(
            Screen screen,
            CallbackInfo ci
    ) {
        if (screen instanceof DownloadingTerrainScreen
                && TeleportCameraController.isWaitingForTeleport()) {
            ci.cancel();
        }
    }

    
    @Inject(
            method = "handleInputEvents",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$disableInputEvents(
            CallbackInfo ci
    ) {
        if (TeleportCameraController.shouldBlockInput()) {
            ci.cancel();
        }
    }

    
    @Inject(
            method = "handleBlockBreaking",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$disableBlockBreaking(
            boolean breaking,
            CallbackInfo ci
    ) {
        if (TeleportCameraController.shouldBlockInput()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "doAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$disableAttack(
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (TeleportCameraController.shouldBlockInput()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "doItemUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$disableItemUse(
            CallbackInfo ci
    ) {
        if (TeleportCameraController.shouldBlockInput()) {
            ci.cancel();
        }
    }
}