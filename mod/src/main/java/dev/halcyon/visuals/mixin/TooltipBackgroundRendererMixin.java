package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.halcyon.visuals.render.Render2D.argb;

/** Подсказки к предметам в стиле Halcyon: тёмное стекло и градиентная рамка. */
@Mixin(TooltipBackgroundRenderer.class)
public abstract class TooltipBackgroundRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private static void halcyon$tooltip(DrawContext ctx, int x, int y, int w, int h, int z, CallbackInfo ci) {
        HalcyonConfig cfg = HalcyonConfig.get();
        if (!cfg.gui.tooltips) return;
        int bg = 0xF20D0D11, accent = 0xFF000000 | cfg.accentRgb(), teal = 0xFF20D6C8;
        // Тело со «срезанными» углами, как у ванильной подсказки
        ctx.fill(x - 3, y - 4, x + w + 3, y - 3, z, bg);
        ctx.fill(x - 3, y + h + 3, x + w + 3, y + h + 4, z, bg);
        ctx.fill(x - 3, y - 3, x + w + 3, y + h + 3, z, bg);
        ctx.fill(x - 4, y - 3, x - 3, y + h + 3, z, bg);
        ctx.fill(x + w + 3, y - 3, x + w + 4, y + h + 3, z, bg);
        // Рамка: сверху акцент, по бокам градиент в бирюзу, снизу приглушённая
        ctx.fill(x - 3, y - 3, x + w + 3, y - 2, z, accent);
        ctx.fillGradient(x - 3, y - 2, x - 2, y + h + 2, z, argb(cfg.accentRgb(), 0.8), argb(0x20D6C8, 0.35));
        ctx.fillGradient(x + w + 2, y - 2, x + w + 3, y + h + 2, z, argb(cfg.accentRgb(), 0.8), argb(0x20D6C8, 0.35));
        ctx.fill(x - 3, y + h + 2, x + w + 3, y + h + 3, z, argb(0x20D6C8, 0.35));
        ci.cancel();
    }
}
