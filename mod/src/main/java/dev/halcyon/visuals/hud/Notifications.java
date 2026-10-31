package dev.halcyon.visuals.hud;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

import static dev.halcyon.visuals.render.Render2D.*;

/** Уведомления Halcyon: выезжают справа снизу, показывают таймер и исчезают. Подборы предметов складываются. */
public final class Notifications {
    private static final long LIFE = 3200;

    private static final class Note {
        String title, text;
        long start;
        final boolean warn;
        final ItemStack icon;
        int count;
        Note(String title, String text, boolean warn, ItemStack icon, int count) {
            this.title = title; this.text = text; this.warn = warn; this.icon = icon; this.count = count;
            this.start = Util.getMeasuringTimeMs();
        }
    }

    private static final List<Note> NOTES = new ArrayList<>();

    private Notifications() {}

    public static void push(String title, String text) { add(new Note(title, text, false, null, 0)); }
    public static void warn(String title, String text) { add(new Note(title, text, true, null, 0)); }

    /** «+N Предмет». Повторный подбор того же предмета в течение 2,5 с увеличивает счётчик. */
    public static void pickup(ItemStack stack, int count) {
        HalcyonConfig cfg = HalcyonConfig.get();
        if (!cfg.notifications || !cfg.loot.pickups || stack.isEmpty() || count <= 0) return;
        long now = Util.getMeasuringTimeMs();
        synchronized (NOTES) {
            for (Note n : NOTES) {
                if (n.icon != null && now - n.start < 2500 && ItemStack.areItemsAndComponentsEqual(n.icon, stack)) {
                    n.count += count;
                    n.title = "+" + n.count;
                    n.start = now - 220; // без повторного «выезда»
                    return;
                }
            }
        }
        add(new Note("+" + count, stack.getName().getString(), false, stack.copyWithCount(1), count));
    }

    private static void add(Note n) {
        if (!HalcyonConfig.get().notifications) return;
        synchronized (NOTES) {
            NOTES.add(n);
            while (NOTES.size() > 5) NOTES.remove(0);
        }
    }

    public static void render(DrawContext ctx, TextRenderer tr, HalcyonConfig cfg, int sw, int sh) {
        long now = Util.getMeasuringTimeMs();
        synchronized (NOTES) {
            NOTES.removeIf(n -> now - n.start > LIFE);
            int y = sh - 6;
            int accent = cfg.accentRgb();
            for (int i = NOTES.size() - 1; i >= 0; i--) {
                Note n = NOTES.get(i);
                long age = now - n.start;
                float in = Math.min(1f, age / 220f), out = Math.min(1f, (LIFE - age) / 260f);
                float p = Math.min(in, out);
                p = 1f - (1f - p) * (1f - p) * (1f - p);
                int iconW = n.icon != null ? 20 : 0;
                int w = Math.max(tr.getWidth(n.title), tr.getWidth(n.text)) + 20 + iconW, h = 28;
                int x = sw - 6 - w + Math.round((1f - p) * (w + 10));
                y -= h;
                int color = n.warn ? 0xFFC861 : accent;
                roundRect(ctx, x, y, w, h, 5, argb(0x0D0D11, 0.86));
                roundBorder(ctx, x, y, w, h, 5, argb(0xFFFFFF, 0.12));
                ctx.fill(x + 1, y + 5, x + 3, y + h - 5, 0xFF000000 | color);
                if (n.icon != null) ctx.drawItem(n.icon, x + 8, y + 6);
                ctx.drawText(tr, n.title, x + 9 + iconW, y + 5, 0xFF000000 | color, false);
                ctx.drawText(tr, n.text, x + 9 + iconW, y + 15, TEXT, false);
                float left = 1f - (float) age / LIFE;
                ctx.fill(x + 5, y + h - 2, x + 5 + Math.round((w - 10) * Math.max(0, left)), y + h - 1, argb(color, 0.7));
                y -= 4;
            }
        }
    }
}
