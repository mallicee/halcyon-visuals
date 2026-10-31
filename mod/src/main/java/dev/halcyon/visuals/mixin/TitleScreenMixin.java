package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.gui.MenuBackground;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Главное меню в стиле Halcyon вместо панорамы. */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void halcyon$background(DrawContext ctx, float delta, CallbackInfo ci) {
        if (!HalcyonConfig.get().gui.halcyonMenu) return;
        MenuBackground.render(ctx, this.width, this.height);
        ci.cancel();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void halcyon$brand(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!HalcyonConfig.get().gui.halcyonMenu) return;
        int x = ctx.drawText(this.textRenderer, "halcyon", 6, 10, 0xFFE9ECF2, false);
        ctx.drawText(this.textRenderer, ".", x, 10, 0xFF000000 | HalcyonConfig.get().accentRgb(), false);
        ctx.drawText(this.textRenderer, "visuals", x + 6, 10, 0xFF8B90A5, false);
    }
}
