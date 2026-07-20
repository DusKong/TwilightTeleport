package net.ochibo.twilightteleport.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ochibo.twilightteleport.TwilightTeleport;

import java.util.UUID;

public record TeleportClientStatePayload(
        UUID sessionId,
        TeleportClientState state
) implements CustomPayload {

    public static final Id<TeleportClientStatePayload> ID =
            new Id<>(Identifier.of(
                    TwilightTeleport.MOD_ID,
                    "teleport_client_state"
            ));

    public static final PacketCodec<RegistryByteBuf, TeleportClientStatePayload> CODEC =
            PacketCodec.of(
                    TeleportClientStatePayload::write,
                    TeleportClientStatePayload::new
            );

    private TeleportClientStatePayload(RegistryByteBuf buf) {
        this(
                buf.readUuid(),
                readState(buf)
        );
    }

    private void write(RegistryByteBuf buf) {
        buf.writeUuid(sessionId);
        buf.writeVarInt(state.wireId());
    }

    private static TeleportClientState readState(RegistryByteBuf buf) {
        return TeleportClientState.fromWireId(buf.readVarInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
