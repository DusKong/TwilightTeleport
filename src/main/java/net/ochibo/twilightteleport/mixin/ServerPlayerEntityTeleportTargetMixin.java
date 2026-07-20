package net.ochibo.twilightteleport.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import net.ochibo.twilightteleport.server.PendingTeleportManager;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityTeleportTargetMixin {

    @WrapMethod(
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
    }
}
