package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** «Низкий огонь»: пламя на экране опускается ниже и меньше закрывает обзор. */
@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"))
    private static void halcyon$lowFireStart(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        matrices.push();
        if (HalcyonConfig.get().optimization.lowFire) matrices.translate(0f, -0.3f, 0f);
    }

    @Inject(method = "renderFireOverlay", at = @At("TAIL"))
    private static void halcyon$lowFireEnd(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        matrices.pop();
    }
}
