package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Экран смерти в стиле Halcyon: тёмное стекло с красной кромкой вместо сплошной красной заливки. */
@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void halcyon$bg(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!HalcyonConfig.get().gui.halcyonMenu) return;
        Screen self = (Screen) (Object) this;
        int w = self.width, h = self.height;
        ctx.fillGradient(0, 0, w, h, 0xB80D0D11, 0xE0140A0E);
        ctx.fillGradient(0, 0, w, h / 3, 0x40FF5C7A, 0x00FF5C7A);
        ctx.fill(0, 0, w, 1, 0xCCFF5C7A);
        ci.cancel();
    }
}
