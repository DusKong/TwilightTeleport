package net.ochibo.twilightteleport.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.renderer.rendertype.RenderType;


import java.util.UUID;


public final class TeleportDissolveVertexConsumerProvider
        implements VertexConsumerProvider {

    private static final VertexConsumer DISCARDING_CONSUMER =
            TeleportDiscardingVertexConsumer.INSTANCE;

    private final VertexConsumerProvider delegate;
    private final UUID playerUuid;

    public TeleportDissolveVertexConsumerProvider(
            VertexConsumerProvider delegate,
            UUID playerUuid
    ) {
        this.delegate = delegate;
        this.playerUuid = playerUuid;
    }

    @Override
    public VertexConsumer getBuffer(RenderType layer) {
        if (!layer.format().equals(
                DefaultVertexFormat.ENTITY
        )) {
            return DISCARDING_CONSUMER;
        }

        return delegate.getBuffer(
                TeleportDissolveRenderLayer.wrap(
                        playerUuid,
                        layer
                )
        );
    }
}
