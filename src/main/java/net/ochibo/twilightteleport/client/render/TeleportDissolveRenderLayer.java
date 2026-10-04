package net.ochibo.twilightteleport.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class TeleportDissolveRenderLayer
        extends RenderType {

    private static final Map<SkinLayerKey, RenderType> SKIN_CACHE =
            new ConcurrentHashMap<>();

    private static final Map<WrappedLayerKey, RenderType> WRAPPED_CACHE =
            new ConcurrentHashMap<>();

    private TeleportDissolveRenderLayer(
            String name,
            VertexFormat vertexFormat,
            VertexFormat drawMode,
            int expectedBufferSize,
            boolean hasCrumbling,
            boolean translucent,
            LayerActions actions
    ) {
        super(
                name,
                vertexFormat,
                drawMode,
                expectedBufferSize,
                hasCrumbling,
                translucent,
                actions.startAction(),
                actions.endAction()
        );
    }

    
    public static RenderType get(
            UUID playerUuid,
            Identifier texture
    ) {
        SkinLayerKey key =
                new SkinLayerKey(playerUuid, texture);

        return SKIN_CACHE.computeIfAbsent(
                key,
                TeleportDissolveRenderLayer::createSkinLayer
        );
    }

    
    public static RenderType wrap(
            UUID playerUuid,
            RenderType originalLayer
    ) {
        if (originalLayer instanceof TeleportDissolveRenderLayer) {
            return originalLayer;
        }

        WrappedLayerKey key =
                new WrappedLayerKey(
                        playerUuid,
                        originalLayer
                );

        return WRAPPED_CACHE.computeIfAbsent(
                key,
                TeleportDissolveRenderLayer::createWrappedLayer
        );
    }

    public static void clear(UUID playerUuid) {
        SKIN_CACHE.keySet().removeIf(
                key -> key.playerUuid().equals(playerUuid)
        );

        WRAPPED_CACHE.keySet().removeIf(
                key -> key.playerUuid().equals(playerUuid)
        );
    }

    private static RenderType createSkinLayer(
            SkinLayerKey key
    ) {
        LayerActions actions =
                createSkinActions(key);

        return new TeleportDissolveRenderLayer(
                "twilightteleport_dissolve_skin_"
                        + key.playerUuid()
                        + "_"
                        + key.texture(),
                DefaultVertexFormat.ENTITY,
                VertexFormat.DrawMode.QUADS,
                TRANSIENT_BUFFER_SIZE,
                false,
                false,
                actions
        );
    }

    private static RenderType createWrappedLayer(
            WrappedLayerKey key
    ) {
        RenderType original = key.originalLayer();

        LayerActions actions =
                createWrappedActions(key);

        return new TeleportDissolveRenderLayer(
                "twilightteleport_dissolve_feature_"
                        + key.playerUuid()
                        + "_"
                        + Integer.toHexString(
                        System.identityHashCode(original)
                ),
                original.format(),
                original.getDrawMode(),
                original.getExpectedBufferSize(),
                original.affectsCrumbling(),
                original.sortOnUpload(),
                actions
        );
    }

    private static LayerActions createSkinActions(
            SkinLayerKey key
    ) {
        Texture texturePhase =
                new Texture(key.texture(), false, false);

        ShaderProgram shaderPhase =
                new ShaderProgram(
                        TeleportDissolveShaders::getProgram
                );

        Texturing uniformPhase =
                createUniformPhase(key.playerUuid());

        RenderPhase[] phases = {
                texturePhase,
                shaderPhase,
                NO_TRANSPARENCY,
                LEQUAL_DEPTH_TEST,
                DISABLE_CULLING,
                ENABLE_LIGHTMAP,
                ENABLE_OVERLAY_COLOR,
                NO_LAYERING,
                MAIN_TARGET,
                uniformPhase,
                ALL_MASK,
                NO_COLOR_LOGIC
        };

        Runnable startAction = () -> {
            for (RenderPhase phase : phases) {
                phase.startDrawing();
            }
        };

        Runnable endAction = () -> {
            for (int i = phases.length - 1; i >= 0; i--) {
                phases[i].endDrawing();
            }
        };

        return new LayerActions(
                startAction,
                endAction
        );
    }

    private static LayerActions createWrappedActions(
            WrappedLayerKey key
    ) {
        RenderType original = key.originalLayer();

        ShaderProgram shaderPhase =
                new ShaderProgram(
                        TeleportDissolveShaders::getProgram
                );

        Texturing uniformPhase =
                createUniformPhase(key.playerUuid());

        Runnable startAction = () -> {
            
            original.startDrawing();

            
            shaderPhase.startDrawing();
            uniformPhase.startDrawing();
        };

        Runnable endAction = () -> {
            uniformPhase.endDrawing();
            shaderPhase.endDrawing();
            original.endDrawing();
        };

        return new LayerActions(
                startAction,
                endAction
        );
    }

    private static Texturing createUniformPhase(
            UUID playerUuid
    ) {
        return new Texturing(
                "twilightteleport_dissolve_uniforms_"
                        + playerUuid,
                () -> TeleportDissolveRenderState
                        .uploadUniforms(playerUuid),
                () -> {
                }
        );
    }

    private record SkinLayerKey(
            UUID playerUuid,
            Identifier texture
    ) {
    }

    
    private record WrappedLayerKey(
            UUID playerUuid,
            RenderType originalLayer
    ) {
    }

    private record LayerActions(
            Runnable startAction,
            Runnable endAction
    ) {
    }
}
