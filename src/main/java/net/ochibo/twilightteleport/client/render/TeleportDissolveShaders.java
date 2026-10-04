package net.ochibo.twilightteleport.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.ochibo.twilightteleport.TwilightTeleport;
import org.jetbrains.annotations.Nullable;

public final class TeleportDissolveShaders {
    private static final Identifier PROGRAM_ID =
            TwilightTeleport.id("teleport_dissolve");

    @Nullable
    private static ShaderManager program;

    private TeleportDissolveShaders() {
    }

    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context ->
                context.register(
                        PROGRAM_ID,
                        DefaultVertexFormat.ENTITY,
                        loadedProgram -> program = loadedProgram
                )
        );
    }

    @Nullable
    public static ShaderManager getProgram() {
        return program;
    }
}
