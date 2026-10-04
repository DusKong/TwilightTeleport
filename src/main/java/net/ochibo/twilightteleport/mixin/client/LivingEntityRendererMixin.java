package net.ochibo.twilightteleport.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.ochibo.twilightteleport.client.effect.TeleportEntityEffectManager;
import net.ochibo.twilightteleport.client.render.TeleportDissolveRenderLayer;
import net.ochibo.twilightteleport.client.render.TeleportDissolveRenderState;
import net.ochibo.twilightteleport.client.render.TeleportDissolveVertexConsumerProvider;
import net.ochibo.twilightteleport.client.render.TeleportHeightMeasuringVertexConsumerProvider;
import net.ochibo.twilightteleport.client.render.TeleportRenderedHeightManager;
import net.ochibo.twilightteleport.client.render.TeleportShaderPackCompat;
import net.ochibo.twilightteleport.client.render.TeleportShaderPackFallbackVertexConsumer;
import net.ochibo.twilightteleport.client.render.TeleportShaderPackFallbackVertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<
            T extends LivingEntity,
            S extends LivingEntityRenderState
        > {

    @Inject(
            method = "setupRotations",
            at = @At("HEAD")
    )
    private void twilightTeleport$captureActualBodyYaw(
            S state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo ci
    ) {
        if (state.)
    }

    @Inject(
            method =
                    "render"
                            + "(Lnet/minecraft/entity/LivingEntity;FF"
                            + "Lnet/minecraft/client/util/math/MatrixStack;"
                            + "Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void twilightTeleport$beginRenderedHeightMeasurement(
            LivingEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (entity instanceof AbstractClientPlayerEntity player) {
            TeleportRenderedHeightManager.beginPlayerRender(
                    player,
                    tickDelta,
                    player.getBodyYaw()
            );
        }
    }

    @Inject(
            method =
                    "setupTransforms"
                            + "(Lnet/minecraft/entity/LivingEntity;"
                            + "Lnet/minecraft/client/util/math/MatrixStack;"
                            + "FFFF)V",
            at = @At("HEAD")
    )
    private void twilightTeleport$captureActualBodyYaw(
            LivingEntity entity,
            MatrixStack matrices,
            float animationProgress,
            float bodyYaw,
            float tickDelta,
            float scale,
            CallbackInfo ci
    ) {
        if (entity instanceof AbstractClientPlayer player
                && TeleportRenderedHeightManager
                .isMeasurementActive(player.getUUID())) {
            TeleportRenderedHeightManager.updateMeasurementRenderYaw(
                    player.getUUID(),
                    bodyYaw
            );
        }
    }

    @Inject(
            method =
                    "render"
                            + "(Lnet/minecraft/entity/LivingEntity;FF"
                            + "Lnet/minecraft/client/util/math/MatrixStack;"
                            + "Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    private void twilightTeleport$finishRenderedHeightMeasurement(
            LivingEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (entity instanceof AbstractClientPlayer player) {
            TeleportRenderedHeightManager.endPlayerRender(player);
        }
    }

    
    @Inject(
            method =
                    "getRenderLayer"
                            + "(Lnet/minecraft/entity/LivingEntity;ZZZ)"
                            + "Lnet/minecraft/client/render/RenderLayer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$useDissolveLayer(
            LivingEntity entity,
            boolean showBody,
            boolean translucent,
            boolean showOutline,
            CallbackInfoReturnable<RenderLayer> cir
    ) {
        if (!(entity
                instanceof AbstractClientPlayer player)) {
            return;
        }

        if (TeleportShaderPackCompat.isShadowPass()) {
            if (TeleportEntityEffectManager
                    .shouldRenderDissolve(player.getUUID())) {
                cir.setReturnValue(null);
            }

            return;
        }

        if (!TeleportEntityEffectManager
                .shouldRenderDissolve(player.getUUID())) {
            return;
        }

        if (!TeleportRenderedHeightManager
                .ensureEffectSnapshotReady(player)) {
            return;
        }

        if (!((Object) this
                instanceof PlayerEntityRenderer playerRenderer)) {
            return;
        }

        Minecraft client =
                Minecraft.getInstance();

        float tickDelta =
                client.getRenderTickCounter()
                        .getTickDelta(false);

        TeleportDissolveRenderState.prepare(
                player,
                tickDelta
        );

        if (TeleportShaderPackCompat
                .isShaderPackInUse()) {
            
            return;
        }

        cir.setReturnValue(
                TeleportDissolveRenderLayer.get(
                        player.getUUID(),
                        playerRenderer.getTexture(player)
                )
        );
    }

    
    @Redirect(
            method =
                    "render"
                            + "(Lnet/minecraft/entity/LivingEntity;FF"
                            + "Lnet/minecraft/client/util/math/MatrixStack;"
                            + "Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/render/"
                                    + "VertexConsumerProvider;getBuffer"
                                    + "(Lnet/minecraft/client/render/RenderLayer;)"
                                    + "Lnet/minecraft/client/render/VertexConsumer;"
            )
    )
    private VertexConsumer twilightTeleport$applyShaderPackFallbackToBody(
            VertexConsumerProvider provider,
            RenderLayer layer,
            LivingEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {
        VertexConsumer originalConsumer =
                provider.getBuffer(layer);

        if (!(entity
                instanceof AbstractClientPlayer player)) {
            return originalConsumer;
        }

        if (TeleportShaderPackCompat.isShadowPass()) {
            return originalConsumer;
        }

        
        VertexConsumer measuringConsumer =
                TeleportRenderedHeightManager
                        .isMeasurementActive(
                                player.getUUID()
                        )
                        ? TeleportHeightMeasuringVertexConsumerProvider.wrap(
                        originalConsumer,
                        player.getUUID()
                )
                        : originalConsumer;

        if (!TeleportShaderPackCompat
                .isShaderPackInUse()) {
            return measuringConsumer;
        }

        if (!TeleportEntityEffectManager
                .shouldRenderDissolve(
                        player.getUUID()
                )) {
            return measuringConsumer;
        }

        if (!TeleportRenderedHeightManager
                .ensureEffectSnapshotReady(player)) {
            
            return measuringConsumer;
        }

        TeleportDissolveRenderState.prepare(
                player,
                tickDelta
        );

        
        VertexConsumer shaderPackFallback =
                new TeleportShaderPackFallbackVertexConsumer(
                        originalConsumer,
                        player.getUUID()
                );

        return TeleportRenderedHeightManager
                .isMeasurementActive(player.getUUID())
                ? TeleportHeightMeasuringVertexConsumerProvider.wrap(
                shaderPackFallback,
                player.getUUID()
        )
                : shaderPackFallback;
    }

    
    @Redirect(
            method =
                    "render"
                            + "(Lnet/minecraft/entity/LivingEntity;FF"
                            + "Lnet/minecraft/client/util/math/MatrixStack;"
                            + "Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/render/entity/feature/"
                                    + "FeatureRenderer;render"
                                    + "(Lnet/minecraft/client/util/math/MatrixStack;"
                                    + "Lnet/minecraft/client/render/VertexConsumerProvider;"
                                    + "ILnet/minecraft/entity/Entity;FFFFFF)V"
            )
    )
    @SuppressWarnings({
            "rawtypes",
            "unchecked"
    })
    private void twilightTeleport$renderDissolvingFeatures(
            FeatureRenderer featureRenderer,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            Entity entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch
    ) {
        VertexConsumerProvider selectedProvider =
                vertexConsumers;

        if (entity instanceof AbstractClientPlayer player
                && TeleportShaderPackCompat.isShadowPass()
                && TeleportEntityEffectManager
                .shouldRenderDissolve(player.getUUID())) {
            return;
        }

        if (entity instanceof AbstractClientPlayer player
                && !TeleportShaderPackCompat.isShadowPass()) {
            boolean wantsDissolve =
                    TeleportEntityEffectManager
                            .shouldRenderDissolve(
                                    player.getUUID()
                            );

            boolean snapshotReady =
                    !wantsDissolve
                            || TeleportRenderedHeightManager
                            .ensureEffectSnapshotReady(player);

            if (wantsDissolve && snapshotReady) {
                TeleportDissolveRenderState.prepare(
                        player,
                        tickDelta
                );

                if (TeleportShaderPackCompat
                        .isShaderPackInUse()) {
                    selectedProvider =
                            new TeleportShaderPackFallbackVertexConsumerProvider(
                                    selectedProvider,
                                    player.getUUID()
                            );
                } else {
                    selectedProvider =
                            new TeleportDissolveVertexConsumerProvider(
                                    selectedProvider,
                                    player.getUUID()
                            );
                }
            }

            if (TeleportRenderedHeightManager
                    .isMeasurementActive(player.getUUID())) {
                selectedProvider =
                        new TeleportHeightMeasuringVertexConsumerProvider(
                                selectedProvider,
                                player.getUUID()
                        );
            }
        }

        featureRenderer.render(
                matrices,
                selectedProvider,
                light,
                entity,
                limbAngle,
                limbDistance,
                tickDelta,
                animationProgress,
                headYaw,
                headPitch
        );
    }

    
    @Inject(
            method =
                    "getShadowRadius"
                            + "(Lnet/minecraft/entity/LivingEntity;)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private void twilightTeleport$hideShadow(
            LivingEntity entity,
            CallbackInfoReturnable<Float> cir
    ) {
        if (entity instanceof AbstractClientPlayer player
                && TeleportEntityEffectManager
                .shouldRenderDissolve(player.getUUID())) {
            cir.setReturnValue(0.0F);
        }
    }
}
