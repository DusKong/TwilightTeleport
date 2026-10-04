package net.ochibo.twilightteleport.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.ochibo.twilightteleport.TwilightTeleport;

import java.util.UUID;

public record TeleportEffectPayload(
        UUID sessionId,
        UUID playerUuid,
        TeleportEffectAction action,
        int delayTicks,
        int durationTicks,
        int elapsedTicks
) implements CustomPacketPayload {

    public static final Type<TeleportEffectPayload> TYPE =
            new Type<>(TwilightTeleport.id("teleport_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportEffectPayload> CODEC =
            StreamCodec.ofMember(
                    TeleportEffectPayload::write,
                    TeleportEffectPayload::read
            );

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(sessionId);
        buf.writeUUID(playerUuid);
        buf.writeVarInt(action.wireId());
        buf.writeVarInt(Math.max(0, delayTicks));
        buf.writeVarInt(Math.max(0, durationTicks));
        buf.writeVarInt(Math.max(0, elapsedTicks));
    }

    private static TeleportEffectPayload read(RegistryFriendlyByteBuf buf) {
        return new TeleportEffectPayload(
                buf.readUUID(),
                buf.readUUID(),
                readAction(buf),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    private static TeleportEffectAction readAction(RegistryFriendlyByteBuf buf) {
        return TeleportEffectAction.fromWireId(buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
