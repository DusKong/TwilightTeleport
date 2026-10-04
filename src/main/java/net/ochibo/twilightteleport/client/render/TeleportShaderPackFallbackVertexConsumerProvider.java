package net.ochibo.twilightteleport.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.UUID;


public final class TeleportShaderPackFallbackVertexConsumerProvider
        implements VertexConsumerProvider {

    private static final VertexConsumer DISCARDING_CONSUMER =
            TeleportDiscardingVertexConsumer.INSTANCE;

    private final VertexConsumerProvider delegate;
    private final UUID playerUuid;

    public TeleportShaderPackFallbackVertexConsumerProvider(
            VertexConsumerProvider delegate,
            UUID playerUuid
    ) {
        this.delegate = delegate;
        this.playerUuid = playerUuid;
    }

    @Override
    public VertexConsumer getBuffer(RenderType layer) {
        
        if (!layer.format().equals(
                DefaultVertexFormat
                        .POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        )) {
            return DISCARDING_CONSUMER;
        }

        
        return new TeleportShaderPackFallbackVertexConsumer(
                delegate.getBuffer(layer),
                playerUuid
        );
    }
}
