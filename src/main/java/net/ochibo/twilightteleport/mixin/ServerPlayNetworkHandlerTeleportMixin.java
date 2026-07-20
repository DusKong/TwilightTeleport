package net.ochibo.twilightteleport.mixin;

import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.ochibo.twilightteleport.server.PendingTeleportManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerTeleportMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(
            method =
                    "requestTeleport"
                            + "(DDDFFLjava/util/Set;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$interceptNetworkTeleport(
            double destinationX,
            double destinationY,
            double destinationZ,
            float yaw,
            float pitch,
            Set<PositionFlag> flags,
            CallbackInfo ci
    ) {
        ServerPlayNetworkHandler handler =
                (ServerPlayNetworkHandler) (Object) this;

        Set<PositionFlag> copiedFlags = Set.copyOf(flags);

        double resolvedX = resolveCoordinate(
                player.getX(),
                destinationX,
                copiedFlags,
                PositionFlag.X
        );
        double resolvedY = resolveCoordinate(
                player.getY(),
                destinationY,
                copiedFlags,
                PositionFlag.Y
        );
        double resolvedZ = resolveCoordinate(
                player.getZ(),
                destinationZ,
                copiedFlags,
                PositionFlag.Z
        );

        boolean intercepted =
                PendingTeleportManager
                        .interceptExternalTeleport(
                                player,
                                player.getServerWorld(),
                                resolvedX,
                                resolvedY,
                                resolvedZ,
                                () -> {
                                    handler.requestTeleport(
                                            destinationX,
                                            destinationY,
                                            destinationZ,
                                            yaw,
                                            pitch,
                                            copiedFlags
                                    );
                                    return true;
                                }
                        );

        if (intercepted) {
            ci.cancel();
        }
    }

    private static double resolveCoordinate(
            double current,
            double requested,
            Set<PositionFlag> flags,
            PositionFlag coordinateFlag
    ) {
        return flags.contains(coordinateFlag)
                ? current + requested
                : requested;
    }
}
