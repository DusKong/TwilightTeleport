package net.ochibo.twilightteleport.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.ochibo.twilightteleport.server.PendingTeleportManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerTeleportMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(
            method = "teleport(DDDFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$interceptNetworkTeleport(
            double x, double y, double z, float yRot, float xRot, CallbackInfo ci
    ) {
        ServerGamePacketListenerImpl handler =
                (ServerGamePacketListenerImpl) (Object) this;

        boolean intercepted =
                PendingTeleportManager
                        .interceptExternalTeleport(
                                player,
                                player.level(),
                                x,
                                y,
                                z,
                                () -> {
                                    handler.teleport(
                                            x,
                                            y,
                                            z,
                                            yRot,
                                            xRot
                                    );
                                    return true;
                                }
                        );

        if (intercepted) {
            ci.cancel();
        }
    }
}
