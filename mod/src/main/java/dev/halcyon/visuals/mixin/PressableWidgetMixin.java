package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.halcyon.visuals.render.Render2D.*;

/** Стеклянные кнопки во всех меню игры (главное меню, пауза, настройки). */
@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin extends ClickableWidget {
    public PressableWidgetMixin(int x, int y, int w, int h, Text message) { super(x, y, w, h, message); }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void halcyon$glass(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HalcyonConfig cfg = HalcyonConfig.get();
        if (!cfg.gui.glassButtons) return;
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        int accent = cfg.accentRgb();
        boolean hot = this.active && (isHovered() || isFocused());
        double a = this.alpha;
        roundRect(ctx, x, y, w, h, 4, hot ? argb(accent, 0.18 * a) : argb(0x141420, (this.active ? 0.72 : 0.45) * a));
        roundBorder(ctx, x, y, w, h, 4, hot ? argb(accent, a) : argb(0xFFFFFF, (this.active ? 0.16 : 0.08) * a));
        if (hot) ctx.fill(x + 4, y + h - 1, x + w - 4, y + h, argb(accent, 0.9 * a));
        int color = (this.active ? 0xFFFFFF : 0x8B90A5) | MathHelper.ceil(a * 255.0) << 24;
        ((PressableWidget) (Object) this).drawMessage(ctx, MinecraftClient.getInstance().textRenderer, color);
        ci.cancel();
    }
}
