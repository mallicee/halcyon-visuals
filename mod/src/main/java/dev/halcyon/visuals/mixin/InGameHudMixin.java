package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.hud.HudRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Скрывает ванильные элементы, которые Halcyon рисует сам (только если соответствующий элемент включён).
 * Воздух и здоровье маунта остаются ванильными.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void halcyon$hotbar(CallbackInfo ci) {
        if (HudRenderer.replaces("hotbar")) ci.cancel();
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void halcyon$health(CallbackInfo ci) {
        if (HudRenderer.replaces("health")) ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void halcyon$armor(CallbackInfo ci) {
        if (HudRenderer.replaces("armor")) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void halcyon$food(CallbackInfo ci) {
        if (HudRenderer.replaces("food")) ci.cancel();
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void halcyon$xpBar(CallbackInfo ci) {
        if (HudRenderer.replaces("xp")) ci.cancel();
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void halcyon$xpLevel(CallbackInfo ci) {
        if (HudRenderer.replaces("xp")) ci.cancel();
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void halcyon$crosshair(CallbackInfo ci) {
        if (HudRenderer.replaces("cross")) ci.cancel();
    }
}
