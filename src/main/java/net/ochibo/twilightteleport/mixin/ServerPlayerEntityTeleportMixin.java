package net.ochibo.twilightteleport.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.ochibo.twilightteleport.server.PendingTeleportManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityTeleportMixin {

    
    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$interceptWaystonesDimensionTeleport(
            ServerLevel level,
            double x,
            double y,
            double z,
            Set<Relative> relatives,
            float newYRot,
            float newXRot,
            boolean resetCamera,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ServerPlayer player =
                (ServerPlayer) (Object) this;

        boolean intercepted =
                PendingTeleportManager
                        .interceptExternalTeleport(
                                player,
                                level,
                                x,
                                y,
                                z,
                                () -> player.teleportTo(
                                        level,
                                        x,
                                        y,
                                        z,
                                        relatives,
                                        newYRot,
                                        newXRot,
                                        resetCamera
                                )
                        );

        if (intercepted) {
            cir.cancel();
        }
    }
}
