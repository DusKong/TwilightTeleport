package net.ochibo.twilightteleport.mixin.client;

import net.minecraft.client.MouseHandler;
import net.ochibo.twilightteleport.TeleportCameraController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(
            method = "onScroll",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$disableMouseScroll(
            long handle,
            double xoffset,
            double yoffset,
            CallbackInfo ci
    ) {
        if (TeleportCameraController.shouldBlockInput()) {
            ci.cancel();
        }
    }
}