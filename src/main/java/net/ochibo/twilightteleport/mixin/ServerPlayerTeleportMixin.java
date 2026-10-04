package net.ochibo.twilightteleport.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.ochibo.twilightteleport.server.PendingTeleportManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerTeleportMixin {

    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$interceptExternalTeleport(
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

        Set<Relative> copiedRelatives = Set.copyOf(relatives);

        double resolvedX = resolveCoordinate(
                player.getX(),
                x,
                copiedRelatives,
                Relative.X
        );
        double resolvedY = resolveCoordinate(
                player.getY(),
                y,
                copiedRelatives,
                Relative.Y
        );
        double resolvedZ = resolveCoordinate(
                player.getZ(),
                z,
                copiedRelatives,
                Relative.Z
        );

        boolean intercepted =
                PendingTeleportManager
                        .interceptExternalTeleport(
                                player,
                                level,
                                resolvedX,
                                resolvedY,
                                resolvedZ,
                                () -> player.teleportTo(
                                        level,
                                        x,
                                        y,
                                        z,
                                        copiedRelatives,
                                        newYRot,
                                        newXRot,
                                        resetCamera
                                )
                        );
    }

    @Unique
    private static double resolveCoordinate(
            double current,
            double requested,
            Set<Relative> relatives,
            Relative relative
    ) {
        return relatives.contains(relative)
                ? current + requested
                : requested;
    }
}
