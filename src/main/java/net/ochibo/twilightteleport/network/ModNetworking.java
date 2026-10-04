package net.ochibo.twilightteleport.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ochibo.twilightteleport.server.PendingTeleportManager;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(
                TeleportEffectPayload.TYPE,
                TeleportEffectPayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                TeleportClientStatePayload.TYPE,
                TeleportClientStatePayload.CODEC
        );


        ServerPlayNetworking.registerGlobalReceiver(
                TeleportClientStatePayload.TYPE,
                (payload, context) ->
                        PendingTeleportManager.handleClientState(
                                context.player(),
                                payload
                        )
        );

    }
}
