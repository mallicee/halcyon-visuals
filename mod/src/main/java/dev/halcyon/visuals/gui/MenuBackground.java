package dev.halcyon.visuals.gui;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import static dev.halcyon.visuals.render.Render2D.*;

/** Фон главного меню Halcyon: тёмный градиент, мягкие ауры, сетка и плывущие частицы. */
public final class MenuBackground {
    private static final int PARTICLES = 70;
    private static final float[][] P = new float[PARTICLES][4];
    static {
        java.util.Random r = new java.util.Random(11);
        for (float[] p : P) { p[0] = r.nextFloat(); p[1] = r.nextFloat(); p[2] = 0.3f + r.nextFloat(); p[3] = r.nextFloat(); }
    }

    private MenuBackground() {}

    public static void render(DrawContext ctx, int w, int h) {
        double t = Util.getMeasuringTimeMs() / 1000.0;
        int accent = HalcyonConfig.get().accentRgb();
        ctx.fillGradient(0, 0, w, h, 0xFF0D0D11, 0xFF12121A);

        aura(ctx, (int) (w * 0.22 + Math.sin(t * 0.21) * 40), (int) (h * 0.18 + Math.cos(t * 0.17) * 25), (int) (h * 0.55), accent);
        aura(ctx, (int) (w * 0.82 + Math.cos(t * 0.19) * 40), (int) (h * 0.62 + Math.sin(t * 0.23) * 25), (int) (h * 0.5), 0x20D6C8);

        for (int x = 0; x < w; x += 28) ctx.fill(x, 0, x + 1, h, 0x07FFFFFF);
        for (int y = 0; y < h; y += 28) ctx.fill(0, y, w, y + 1, 0x07FFFFFF);

        for (float[] p : P) {
            double y = (p[1] - t * 0.02 * p[2]) % 1.0;
            if (y < 0) y += 1;
            int px = (int) (p[0] * w + Math.sin(t * 0.5 + p[3] * 6) * 6), py = (int) (y * h);
            float tw = (float) (0.5 + 0.5 * Math.sin(t * 2 + p[3] * 12));
            int col = p[3] > 0.5 ? accent : 0x20D6C8;
            int s = p[2] > 1.0f ? 2 : 1;
            ctx.fill(px, py, px + s, py + s, argb(col, 0.25 + 0.55 * tw));
        }
        ctx.fillGradient(0, (int) (h * 0.7), w, h, 0x000D0D11, 0xCC0D0D11);
    }

    /** Мягкое пятно света из концентрических полупрозрачных кругов. */
    private static void aura(DrawContext ctx, int cx, int cy, int r, int rgb) {
        for (int i = 12; i >= 1; i--) {
            int rr = r * i / 12;
            roundRect(ctx, cx - rr, cy - rr, rr * 2, rr * 2, rr, argb(rgb, 0.018));
        }
    }
}
