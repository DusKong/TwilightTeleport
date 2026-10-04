package net.ochibo.twilightteleport.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class TeleportShaderPackFallbackRenderLayer
        extends RenderType {

    private static final Map<RenderType, RenderType> CACHE =
            new ConcurrentHashMap<>();

    private TeleportShaderPackFallbackRenderLayer(
            String name,
            RenderType original,
            LayerActions actions
    ) {
        super(
                name,
                original.format(),
                original.primitiveTopology(),
                original.pipeline().getSortKey(),
                original.affectsCrumbling(),
                true,
                actions.startAction(),
                actions.endAction()
        );
    }

    public static RenderType wrap(RenderType original) {
        if (original
                instanceof TeleportShaderPackFallbackRenderLayer) {
            return original;
        }

        return CACHE.computeIfAbsent(
                original,
                TeleportShaderPackFallbackRenderLayer::create
        );
    }

    public static void clear() {
        CACHE.clear();
    }

    private static RenderType create(RenderType original) {
        LayerActions actions =
                createActions(original);

        return new TeleportShaderPackFallbackRenderLayer(
                "twilightteleport_shaderpack_fallback_"
                        + Integer.toHexString(
                        System.identityHashCode(original)
                ),
                original,
                actions
        );
    }

    private static LayerActions createActions(
            RenderType original
    ) {
        Runnable startAction = () -> {
            
            original.pipeline().b();


            RenderStateShard.TRANSLUCENT_TRANSPARENCY
                    .startDrawing();

            
            RenderPhase.COLOR_MASK.startDrawing();
        };

        Runnable endAction = () -> {
            RenderPhase.COLOR_MASK.endDrawing();

            RenderPhase.TRANSLUCENT_TRANSPARENCY
                    .endDrawing();

            original.endDrawing();
        };

        return new LayerActions(
                startAction,
                endAction
        );
    }

    private record LayerActions(
            Runnable startAction,
            Runnable endAction
    ) {
    }
}
