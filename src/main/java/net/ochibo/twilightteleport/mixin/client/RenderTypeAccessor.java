package net.ochibo.twilightteleport.mixin.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.class)
public interface RenderTypeAccessor {

    @Accessor("sortOnUpload")
    boolean twilightTeleport$sortOnUpload();
}
