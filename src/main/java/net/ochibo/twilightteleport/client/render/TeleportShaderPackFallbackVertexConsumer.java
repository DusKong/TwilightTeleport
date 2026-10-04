package net.ochibo.twilightteleport.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.ochibo.twilightteleport.config.TwilightTeleportConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public final class TeleportShaderPackFallbackVertexConsumer
        implements VertexConsumer {

    private final VertexConsumer delegate;
    private final UUID playerUuid;
    private final int quadSubdivisions;

    private final List<VertexData> quadVertices =
            new ArrayList<>(4);

    private MutableVertex pendingVertex;

    public TeleportShaderPackFallbackVertexConsumer(
            VertexConsumer delegate,
            UUID playerUuid
    ) {
        this.delegate = delegate;
        this.playerUuid = playerUuid;
        this.quadSubdivisions = Math.max(
                1,
                TwilightTeleportConfigManager
                        .get()
                        .getShaderPackEffectQuality()
                        .subdivisions()
        );
    }

    @Override
    public VertexConsumer addVertex(
            float x,
            float y,
            float z
    ) {
        finishPendingVertex();

        pendingVertex =
                new MutableVertex(
                        x,
                        y,
                        z
                );

        return this;
    }

    @Override
    public VertexConsumer setColor(
            int red,
            int green,
            int blue,
            int alpha
    ) {
        ensurePendingVertex();

        pendingVertex.color =
                alpha << 24
                        | red << 16
                        | green << 8
                        | blue;

        return this;
    }

    @Override
    public VertexConsumer setColor(int i) {
        return this.setColor(ARGB.red(i), ARGB.green(i), ARGB.blue(i), ARGB.alpha(i));
    }

    @Override
    public VertexConsumer setUv(
            float u,
            float v
    ) {
        ensurePendingVertex();

        pendingVertex.u = u;
        pendingVertex.v = v;

        return this;
    }

    @Override
    public VertexConsumer setUv1(
            int u,
            int v
    ) {
        ensurePendingVertex();

        pendingVertex.overlay =
                u | v << 16;

        return this;
    }

    @Override
    public VertexConsumer setUv2(
            int u,
            int v
    ) {
        ensurePendingVertex();

        pendingVertex.light =
                u | v << 16;

        return this;
    }

    @Override
    public VertexConsumer setNormal(
            float x,
            float y,
            float z
    ) {
        ensurePendingVertex();

        pendingVertex.normalX = x;
        pendingVertex.normalY = y;
        pendingVertex.normalZ = z;

        finishPendingVertex();
        return this;
    }

    public VertexConsumer setLineWidth(float width) {
        return this;
    }

    @Override
    public void addVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int overlay,
            int light,
            float normalX,
            float normalY,
            float normalZ
    ) {
        finishPendingVertex();

        acceptVertex(
                new VertexData(
                        x,
                        y,
                        z,
                        color,
                        u,
                        v,
                        overlay,
                        light,
                        normalX,
                        normalY,
                        normalZ
                )
        );
    }

    private void ensurePendingVertex() {
        if (pendingVertex == null) {
            pendingVertex =
                    new MutableVertex(
                            0.0F,
                            0.0F,
                            0.0F
                    );
        }
    }

    private void finishPendingVertex() {
        if (pendingVertex == null) {
            return;
        }

        acceptVertex(
                pendingVertex.toImmutable()
        );

        pendingVertex = null;
    }

    private void acceptVertex(VertexData vertex) {
        quadVertices.add(vertex);

        if (quadVertices.size() < 4) {
            return;
        }

        emitSubdividedQuad(
                quadVertices.get(0),
                quadVertices.get(1),
                quadVertices.get(2),
                quadVertices.get(3)
        );

        quadVertices.clear();
    }

    private void emitSubdividedQuad(
            VertexData v00,
            VertexData v10,
            VertexData v11,
            VertexData v01
    ) {
        for (int y = 0;
             y < quadSubdivisions;
             y++) {

            float y0 =
                    y / (float) quadSubdivisions;

            float y1 =
                    (y + 1)
                            / (float) quadSubdivisions;

            for (int x = 0;
                 x < quadSubdivisions;
                 x++) {

                float x0 =
                        x / (float) quadSubdivisions;

                float x1 =
                        (x + 1)
                                / (float) quadSubdivisions;

                float centerX = (x0 + x1) * 0.5F;
                float centerY = (y0 + y1) * 0.5F;

                TeleportDissolveRenderState.FallbackSample sample =
                        TeleportDissolveRenderState
                                .sampleShaderPackFallback(
                                        playerUuid,
                                        bilerp(
                                                v00.x(),
                                                v10.x(),
                                                v11.x(),
                                                v01.x(),
                                                centerX,
                                                centerY
                                        ),
                                        bilerp(
                                                v00.y(),
                                                v10.y(),
                                                v11.y(),
                                                v01.y(),
                                                centerX,
                                                centerY
                                        ),
                                        bilerp(
                                                v00.z(),
                                                v10.z(),
                                                v11.z(),
                                                v01.z(),
                                                centerX,
                                                centerY
                                        )
                                );

                
                if (sample.alpha() <= 0.001F) {
                    continue;
                }

                emitInterpolated(
                        v00, v10, v11, v01, x0, y0, sample
                );
                emitInterpolated(
                        v00, v10, v11, v01, x1, y0, sample
                );
                emitInterpolated(
                        v00, v10, v11, v01, x1, y1, sample
                );
                emitInterpolated(
                        v00, v10, v11, v01, x0, y1, sample
                );
            }
        }
    }

    private void emitInterpolated(
            VertexData v00,
            VertexData v10,
            VertexData v11,
            VertexData v01,
            float x,
            float y,
            TeleportDissolveRenderState.FallbackSample sample
    ) {
        int sourceColor = bilerpColor(
                v00.color(),
                v10.color(),
                v11.color(),
                v01.color(),
                x,
                y
        );

        int alpha =
                sourceColor >>> 24;

        int red =
                sourceColor >>> 16
                        & 0xFF;

        int green =
                sourceColor >>> 8
                        & 0xFF;

        int blue =
                sourceColor
                        & 0xFF;

        int modifiedColor =
                
                alpha << 24
                        | multiplyColor(
                        red,
                        sample.colorMultiplier()
                ) << 16
                        | multiplyColor(
                        green,
                        sample.colorMultiplier()
                ) << 8
                        | multiplyColor(
                        blue,
                        sample.colorMultiplier()
                );

        VertexData nearest = nearestVertex(
                v00,
                v10,
                v11,
                v01,
                x,
                y
        );

        delegate.addVertex(
                bilerp(
                        v00.x(), v10.x(), v11.x(), v01.x(), x, y
                ),
                bilerp(
                        v00.y(), v10.y(), v11.y(), v01.y(), x, y
                ),
                bilerp(
                        v00.z(), v10.z(), v11.z(), v01.z(), x, y
                ),
                modifiedColor,
                bilerp(
                        v00.u(), v10.u(), v11.u(), v01.u(), x, y
                ),
                bilerp(
                        v00.v(), v10.v(), v11.v(), v01.v(), x, y
                ),
                nearest.overlay(),
                nearest.light(),
                bilerp(
                        v00.normalX(),
                        v10.normalX(),
                        v11.normalX(),
                        v01.normalX(),
                        x,
                        y
                ),
                bilerp(
                        v00.normalY(),
                        v10.normalY(),
                        v11.normalY(),
                        v01.normalY(),
                        x,
                        y
                ),
                bilerp(
                        v00.normalZ(),
                        v10.normalZ(),
                        v11.normalZ(),
                        v01.normalZ(),
                        x,
                        y
                )
        );
    }

    private static float bilerp(
            float v00,
            float v10,
            float v11,
            float v01,
            float x,
            float y
    ) {
        float top = Mth.lerp(x, v00, v10);
        float bottom = Mth.lerp(x, v01, v11);

        return Mth.lerp(y, top, bottom);
    }

    private static VertexData nearestVertex(
            VertexData v00,
            VertexData v10,
            VertexData v11,
            VertexData v01,
            float x,
            float y
    ) {
        if (y < 0.5F) {
            return x < 0.5F ? v00 : v10;
        }

        return x < 0.5F ? v01 : v11;
    }

    private static int bilerpColor(
            int v00,
            int v10,
            int v11,
            int v01,
            float x,
            float y
    ) {
        return bilerpColorComponent(
                v00, v10, v11, v01, 24, x, y
        ) << 24
                | bilerpColorComponent(
                v00, v10, v11, v01, 16, x, y
        ) << 16
                | bilerpColorComponent(
                v00, v10, v11, v01, 8, x, y
        ) << 8
                | bilerpColorComponent(
                v00, v10, v11, v01, 0, x, y
        );
    }

    private static int bilerpColorComponent(
            int v00,
            int v10,
            int v11,
            int v01,
            int shift,
            float x,
            float y
    ) {
        return Mth.clamp(
                Math.round(
                        bilerp(
                                v00 >>> shift & 0xFF,
                                v10 >>> shift & 0xFF,
                                v11 >>> shift & 0xFF,
                                v01 >>> shift & 0xFF,
                                x,
                                y
                        )
                ),
                0,
                255
        );
    }

    private static int multiplyColor(
            int component,
            float multiplier
    ) {
        return Mth.clamp(
                Math.round(
                        component
                                * Mth.clamp(
                                multiplier,
                                0.0F,
                                1.0F
                        )
                ),
                0,
                255
        );
    }

    private record VertexData(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int overlay,
            int light,
            float normalX,
            float normalY,
            float normalZ
    ) {
    }

    private static final class MutableVertex {
        private final float x;
        private final float y;
        private final float z;

        private int color = 0xFFFFFFFF;
        private float u;
        private float v;
        private int overlay;
        private int light;
        private float normalX;
        private float normalY = 1.0F;
        private float normalZ;

        private MutableVertex(
                float x,
                float y,
                float z
        ) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private VertexData toImmutable() {
            return new VertexData(
                    x,
                    y,
                    z,
                    color,
                    u,
                    v,
                    overlay,
                    light,
                    normalX,
                    normalY,
                    normalZ
            );
        }
    }
}
