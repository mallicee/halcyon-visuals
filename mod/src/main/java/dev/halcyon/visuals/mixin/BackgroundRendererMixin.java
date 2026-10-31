package dev.halcyon.visuals.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.halcyon.visuals.effects.Atmosphere;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Оттенок тумана по теме (не под водой и не в лаве). */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    @Shadow private static float red;
    @Shadow private static float green;
    @Shadow private static float blue;

    @Inject(method = "render", at = @At("TAIL"))
    private static void halcyon$fog(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        if (!Atmosphere.active(world) || camera.getSubmersionType() != CameraSubmersionType.NONE) return;
        float[] c = Atmosphere.tint(red, green, blue, true);
        red = c[0]; green = c[1]; blue = c[2];
        RenderSystem.clearColor(red, green, blue, 0.0f);
    }
}
