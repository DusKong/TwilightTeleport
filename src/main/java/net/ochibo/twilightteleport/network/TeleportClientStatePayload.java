package net.ochibo.twilightteleport.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ochibo.twilightteleport.TwilightTeleport;

import java.util.UUID;

public record TeleportClientStatePayload(
        UUID sessionId,
        TeleportClientState state
) implements CustomPacketPayload {

    public static final Type<TeleportClientStatePayload> TYPE =
            new Type<>(TwilightTeleport.id("teleport_client_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportClientStatePayload> CODEC =
            StreamCodec.ofMember(
                    TeleportClientStatePayload::write,
                    TeleportClientStatePayload::read
            );

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(sessionId);
        buf.writeVarInt(state.wireId());
    }

    public static TeleportClientStatePayload read(RegistryFriendlyByteBuf buf) {
        return new TeleportClientStatePayload(
                buf.readUUID(),
                readState(buf)
        );
    }

    private static TeleportClientState readState(RegistryFriendlyByteBuf buf) {
        return TeleportClientState.fromWireId(buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
