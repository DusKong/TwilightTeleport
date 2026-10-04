package net.ochibo.twilightteleport.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.ochibo.twilightteleport.server.PendingTeleportManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityTeleportTargetMixin {

    /*@WrapMethod(
            method =
                    "teleportTo"
                            + "(Lnet/minecraft/world/TeleportTarget;)"
                            + "Lnet/minecraft/entity/Entity;"
    )
    private Entity twilightTeleport$wrapTeleportTargetTransition(
            TeleportTarget teleportTarget,
            Operation<Entity> original
    ) {
        ServerPlayerEntity player =
                (ServerPlayerEntity) (Object) this;

        PendingTeleportManager
                .beginTeleportTargetTransition(
                        player
                );

        try {
            return original.call(teleportTarget);
        } finally {
            PendingTeleportManager
                    .endTeleportTargetTransition(player);
        }
    }*/
}
