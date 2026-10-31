package dev.halcyon.visuals.render;

import net.minecraft.client.gui.DrawContext;

/** Примитивы: скруглённые прямоугольники и рамки без наложения полупрозрачных пикселей. */
public final class Render2D {
    public static final int TEXT = 0xFFEEF1F6;
    public static final int MUTED = 0xFF9AA0B4;
    public static final int RED = 0xFFFF5C7A;
    public static final int GOLD = 0xFFFFC861;

    private Render2D() {}

    public static int argb(int rgb, double alpha) {
        int a = (int) Math.round(Math.max(0, Math.min(1, alpha)) * 255);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static int inset(int r, int row) {
        double dy = r - row - 0.5;
        return (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
    }

    public static void roundRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) { ctx.fill(x, y, x + w, y + h, color); return; }
        ctx.fill(x + r, y, x + w - r, y + h, color);
        ctx.fill(x, y + r, x + r, y + h - r, color);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            int in = inset(r, i);
            ctx.fill(x + in, y + i, x + r, y + i + 1, color);
            ctx.fill(x + in, y + h - 1 - i, x + r, y + h - i, color);
            ctx.fill(x + w - r, y + i, x + w - in, y + i + 1, color);
            ctx.fill(x + w - r, y + h - 1 - i, x + w - in, y + h - i, color);
        }
    }

    public static void roundBorder(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 1 || h <= 1) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        ctx.fill(x + r, y, x + w - r, y + 1, color);
        ctx.fill(x + r, y + h - 1, x + w - r, y + h, color);
        ctx.fill(x, y + r, x + 1, y + h - r, color);
        ctx.fill(x + w - 1, y + r, x + w, y + h - r, color);
        int prev = r;
        for (int i = 0; i < r; i++) {
            int in = inset(r, i);
            int end = i == 0 ? r : Math.max(in + 1, prev);
            ctx.fill(x + in, y + i, x + end, y + i + 1, color);
            ctx.fill(x + in, y + h - 1 - i, x + end, y + h - i, color);
            ctx.fill(x + w - end, y + i, x + w - in, y + i + 1, color);
            ctx.fill(x + w - end, y + h - 1 - i, x + w - in, y + h - i, color);
            prev = in;
        }
    }

    public static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return 0xFF000000 | (Math.round(ar + (br - ar) * t) << 16) | (Math.round(ag + (bg - ag) * t) << 8) | Math.round(ab + (bb - ab) * t);
    }

    /** Переливающаяся градиентная линия (акцент → бирюза → акцент), движется со временем. */
    public static void accentLine(DrawContext ctx, int x, int y, int w, int inset, int c1, int c2) {
        double t = net.minecraft.util.Util.getMeasuringTimeMs() / 2600.0;
        for (int i = inset; i < w - inset; i += 2) {
            double f = (i / (double) w + t) % 1.0;
            float m = (float) (0.5 - 0.5 * Math.cos(f * Math.PI * 2));
            ctx.fill(x + i, y, Math.min(x + i + 2, x + w - inset), y + 1, lerpColor(c1, c2, m));
        }
    }

    /** Горизонтальная полоса прогресса со скруглением. */
    public static void bar(DrawContext ctx, int x, int y, int w, int h, float progress, int bg, int fg) {
        roundRect(ctx, x, y, w, h, h / 2, bg);
        int fw = Math.round(w * Math.max(0, Math.min(1, progress)));
        if (fw > 0) roundRect(ctx, x, y, Math.max(fw, h), h, h / 2, fg);
    }
}
