package net.ochibo.twilightteleport.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ochibo.twilightteleport.TwilightTeleport;

import java.util.UUID;

public record TeleportEffectPayload(
        UUID sessionId,
        UUID playerUuid,
        TeleportEffectAction action,
        int delayTicks,
        int durationTicks,
        int elapsedTicks
) implements CustomPayload {

    public static final Id<TeleportEffectPayload> ID =
            new Id<>(Identifier.of(
                    TwilightTeleport.MOD_ID,
                    "teleport_effect"
            ));

    public static final PacketCodec<RegistryByteBuf, TeleportEffectPayload> CODEC =
            PacketCodec.of(
                    TeleportEffectPayload::write,
                    TeleportEffectPayload::new
            );

    private TeleportEffectPayload(RegistryByteBuf buf) {
        this(
                buf.readUuid(),
                buf.readUuid(),
                readAction(buf),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    private void write(RegistryByteBuf buf) {
        buf.writeUuid(sessionId);
        buf.writeUuid(playerUuid);
        buf.writeVarInt(action.wireId());
        buf.writeVarInt(Math.max(0, delayTicks));
        buf.writeVarInt(Math.max(0, durationTicks));
        buf.writeVarInt(Math.max(0, elapsedTicks));
    }

    private static TeleportEffectAction readAction(RegistryByteBuf buf) {
        return TeleportEffectAction.fromWireId(buf.readVarInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
