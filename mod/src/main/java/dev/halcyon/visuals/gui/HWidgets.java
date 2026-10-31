package dev.halcyon.visuals.gui;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import static dev.halcyon.visuals.render.Render2D.*;

/** Виджеты меню в стиле Halcyon: стекло, тонкие рамки, акцент. */
public final class HWidgets {
    private HWidgets() {}

    static int accent() { return 0xFF000000 | HalcyonConfig.get().accentRgb(); }
    static TextRenderer tr() { return MinecraftClient.getInstance().textRenderer; }

    abstract static class Base extends ClickableWidget {
        Base(int x, int y, int w, int h, Text label) { super(x, y, w, h, label); }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) { appendDefaultNarrations(builder); }
    }

    /** Кнопка: основная (градиент акцента), обычная или вкладка. */
    public static final class Button extends Base {
        public enum Kind { PRIMARY, GHOST, TAB }
        private final Runnable onPress;
        private final Kind kind;
        private final BooleanSupplier active;

        public Button(int x, int y, int w, int h, String label, Kind kind, BooleanSupplier active, Runnable onPress) {
            super(x, y, w, h, Text.literal(label));
            this.kind = kind; this.active = active; this.onPress = onPress;
        }

        @Override public void onClick(double mx, double my) { onPress.run(); }

        @Override
        protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean on = active != null && active.getAsBoolean();
            boolean hov = isHovered();
            int textColor = TEXT;
            switch (kind) {
                case PRIMARY -> {
                    ctx.fillGradient(x, y, x + w, y + h, accent(), 0xFF20D6C8);
                    roundBorder(ctx, x, y, w, h, 4, hov ? 0xFFFFFFFF : 0x40FFFFFF);
                    textColor = 0xFF04120B;
                }
                case TAB -> {
                    if (on) roundRect(ctx, x, y, w, h, 4, argb(HalcyonConfig.get().accentRgb(), 0.14));
                    else if (hov) roundRect(ctx, x, y, w, h, 4, 0x0FFFFFFF);
                    if (on) ctx.fill(x + 4, y + h - 1, x + w - 4, y + h, accent());
                    textColor = on ? TEXT : hov ? TEXT : MUTED;
                }
                default -> {
                    roundRect(ctx, x, y, w, h, 4, hov ? argb(HalcyonConfig.get().accentRgb(), 0.10) : 0x0CFFFFFF);
                    roundBorder(ctx, x, y, w, h, 4, hov ? accent() : 0x26FFFFFF);
                }
            }
            String s = getMessage().getString();
            ctx.drawText(tr(), s, x + (w - tr().getWidth(s)) / 2, y + (h - 8) / 2 + 1, textColor, false);
        }
    }

    /** Подпись слева, анимированный переключатель справа. */
    public static final class Toggle extends Base {
        private final BooleanSupplier get;
        private final Consumer<Boolean> set;
        private float knob;

        public Toggle(int x, int y, int w, int h, String label, BooleanSupplier get, Consumer<Boolean> set) {
            super(x, y, w, h, Text.literal(label));
            this.get = get; this.set = set; this.knob = get.getAsBoolean() ? 1 : 0;
        }

        @Override public void onClick(double mx, double my) { set.accept(!get.getAsBoolean()); }

        @Override
        protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean on = get.getAsBoolean();
            knob += ((on ? 1 : 0) - knob) * 0.35f;
            if (isHovered()) roundRect(ctx, x, y, w, h, 4, 0x0AFFFFFF);
            ctx.drawText(tr(), getMessage(), x + 4, y + (h - 8) / 2 + 1, on ? TEXT : MUTED, false);
            int sw = 20, sh = 10, sx = x + w - sw - 4, sy = y + (h - sh) / 2;
            roundRect(ctx, sx, sy, sw, sh, 5, on ? argb(HalcyonConfig.get().accentRgb(), 0.30) : 0x26FFFFFF);
            roundBorder(ctx, sx, sy, sw, sh, 5, on ? accent() : 0x33FFFFFF);
            int kx = sx + 1 + Math.round(knob * (sw - sh));
            roundRect(ctx, kx, sy + 1, sh - 2, sh - 2, 4, on ? accent() : MUTED);
        }
    }

    /** Переключение значений по кругу: «Стиль: Glass ›». ПКМ — назад. */
    public static final class Cycle extends Base {
        private final Supplier<String> value;
        private final Runnable next;

        public Cycle(int x, int y, int w, int h, String label, Supplier<String> value, Runnable next) {
            super(x, y, w, h, Text.literal(label));
            this.value = value; this.next = next;
        }

        @Override public void onClick(double mx, double my) { next.run(); }

        @Override
        protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            roundRect(ctx, x, y, w, h, 4, isHovered() ? argb(HalcyonConfig.get().accentRgb(), 0.10) : 0x0CFFFFFF);
            roundBorder(ctx, x, y, w, h, 4, isHovered() ? accent() : 0x26FFFFFF);
            String label = getMessage().getString();
            int ty = y + (h - 8) / 2 + 1;
            if (!label.isEmpty()) ctx.drawText(tr(), label, x + 5, ty, MUTED, false);
            String v = value.get() + " ›";
            ctx.drawText(tr(), v, label.isEmpty() ? x + (w - tr().getWidth(v)) / 2 : x + w - tr().getWidth(v) - 5, ty, accent(), false);
        }
    }

    /** Ползунок с подписью и значением. */
    public static final class Slider extends Base {
        private final double min, max, step;
        private final DoubleSupplier get;
        private final DoubleConsumer set;
        private final DoubleFunction<String> fmt;

        public Slider(int x, int y, int w, int h, String label, double min, double max, double step,
                      DoubleSupplier get, DoubleConsumer set, DoubleFunction<String> fmt) {
            super(x, y, w, h, Text.literal(label));
            this.min = min; this.max = max; this.step = step; this.get = get; this.set = set; this.fmt = fmt;
        }

        private void setFrom(double mx) {
            int tx = trackX(), tw = trackW();
            double t = Math.max(0, Math.min(1, (mx - tx) / tw));
            double v = min + t * (max - min);
            v = Math.round(v / step) * step;
            set.accept(Math.max(min, Math.min(max, v)));
        }

        private boolean compact() { return getMessage().getString().isEmpty(); }
        private int trackX() { return compact() ? getX() + 2 : getX() + 4; }
        private int trackW() { return compact() ? getWidth() - 4 - tr().getWidth("160%") - 4 : getWidth() - 8; }

        @Override public void onClick(double mx, double my) { setFrom(mx); }
        @Override protected void onDrag(double mx, double my, double dx, double dy) { setFrom(mx); }

        @Override
        protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            double v = get.getAsDouble();
            float t = (float) ((v - min) / (max - min));
            String val = fmt.apply(v);
            int trackY;
            if (compact()) {
                trackY = y + h / 2 - 1;
                ctx.drawText(tr(), val, x + w - tr().getWidth(val), y + (h - 8) / 2 + 1, TEXT, false);
            } else {
                ctx.drawText(tr(), getMessage(), x + 4, y + 1, MUTED, false);
                ctx.drawText(tr(), val, x + w - 4 - tr().getWidth(val), y + 1, TEXT, false);
                trackY = y + h - 5;
            }
            int tx = trackX(), tw = trackW();
            bar(ctx, tx, trackY, tw, 3, t, 0x26FFFFFF, accent());
            int kx = tx + Math.round(t * tw) - 3;
            roundRect(ctx, kx, trackY - 2, 7, 7, 3, isHovered() || isFocused() ? 0xFFFFFFFF : accent());
        }
    }

    /** Рисует иконку скина меча 16×16; у анимированных — текущий кадр (8 кадров по 3 тика). */
    public static void drawSwordIcon(DrawContext ctx, String skinId, int x, int y) {
        if (dev.halcyon.visuals.skins.SwordSkins.NONE.equals(skinId)) {
            ctx.drawTexture(net.minecraft.util.Identifier.ofVanilla("textures/item/diamond_sword.png"), x, y, 0, 0, 16, 16, 16, 16);
            return;
        }
        var skin = dev.halcyon.visuals.skins.SwordSkins.get(skinId);
        boolean anim = skin != null && skin.animated();
        int frame = anim ? (int) ((net.minecraft.util.Util.getMeasuringTimeMs() / 150) % 8) : 0;
        ctx.drawTexture(dev.halcyon.visuals.skins.SwordSkins.texture(skinId), x, y, 0, frame * 16, 16, 16, 16, anim ? 128 : 16);
    }

    /** Карточка выбора скина меча. */
    public static final class SkinCard extends Base {
        private final String skinId;
        private final boolean animated;
        private final Runnable onPress;

        public SkinCard(int x, int y, int w, int h, String skinId, String name, boolean animated, Runnable onPress) {
            super(x, y, w, h, Text.literal(name));
            this.skinId = skinId; this.animated = animated; this.onPress = onPress;
        }

        @Override public void onClick(double mx, double my) { onPress.run(); }

        @Override
        protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean sel = HalcyonConfig.get().swords.skin.equals(skinId);
            int a = HalcyonConfig.get().accentRgb();
            roundRect(ctx, x, y, w, h, 5, sel ? argb(a, 0.14) : isHovered() ? 0x14FFFFFF : 0x0AFFFFFF);
            roundBorder(ctx, x, y, w, h, 5, sel ? accent() : isHovered() ? argb(a, 0.5) : 0x1FFFFFFF);
            drawSwordIcon(ctx, skinId, x + 5, y + (h - 16) / 2);
            ctx.drawText(tr(), getMessage(), x + 26, y + (animated ? 4 : (h - 8) / 2 + 1), sel ? TEXT : MUTED, false);
            if (animated) ctx.drawText(tr(), "анимация", x + 26, y + 14, argb(a, 0.8), false);
        }
    }

    /** Карточка скина удочки. */
    public static final class RodCard extends Base {
        private final String rodId;
        private final Runnable onPress;

        public RodCard(int x, int y, int w, int h, String rodId, String name, Runnable onPress) {
            super(x, y, w, h, Text.literal(name));
            this.rodId = rodId; this.onPress = onPress;
        }

        @Override public void onClick(double mx, double my) { onPress.run(); }

        @Override
        protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean sel = HalcyonConfig.get().loot.rod.equals(rodId);
            int a = HalcyonConfig.get().accentRgb();
            roundRect(ctx, x, y, w, h, 5, sel ? argb(a, 0.14) : isHovered() ? 0x14FFFFFF : 0x0AFFFFFF);
            roundBorder(ctx, x, y, w, h, 5, sel ? accent() : isHovered() ? argb(a, 0.5) : 0x1FFFFFFF);
            var tex = dev.halcyon.visuals.skins.RodSkins.NONE.equals(rodId)
                    ? net.minecraft.util.Identifier.ofVanilla("textures/item/fishing_rod.png")
                    : dev.halcyon.visuals.skins.RodSkins.texture(rodId);
            ctx.drawTexture(tex, x + (w - 16) / 2, y + 3, 0, 0, 16, 16, 16, 16);
            String name = getMessage().getString();
            if (tr().getWidth(name) > w - 4) name = tr().trimToWidth(name, w - 6) + "…";
            ctx.drawText(tr(), name, x + (w - tr().getWidth(name)) / 2, y + h - 11, sel ? TEXT : MUTED, false);
        }
    }

    /** Карточка плаща: превью лицевой стороны (у анимированных — живое). */
    public static final class CapeCard extends Base {
        private final String capeId;
        private final boolean animated;
        private final Runnable onPress;

        public CapeCard(int x, int y, int w, int h, String capeId, String name, boolean animated, Runnable onPress) {
            super(x, y, w, h, Text.literal(name));
            this.capeId = capeId; this.animated = animated; this.onPress = onPress;
        }

        @Override public void onClick(double mx, double my) { onPress.run(); }

        @Override
        protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean sel = HalcyonConfig.get().cosmetics.cape.equals(capeId);
            int a = HalcyonConfig.get().accentRgb();
            roundRect(ctx, x, y, w, h, 5, sel ? argb(a, 0.14) : isHovered() ? 0x14FFFFFF : 0x0AFFFFFF);
            roundBorder(ctx, x, y, w, h, 5, sel ? accent() : isHovered() ? argb(a, 0.5) : 0x1FFFFFFF);
            var tex = dev.halcyon.visuals.skins.Capes.texture(capeId);
            int pw = 20, ph = 32, px = x + (w - pw) / 2, py = y + 4;
            if (tex == null) {
                roundBorder(ctx, px, py, pw, ph, 3, 0x33FFFFFF);
                ctx.fill(px + 4, py + ph / 2, px + pw - 4, py + ph / 2 + 1, 0x33FFFFFF);
            } else {
                // Лицевая сторона в развёртке 256×128: x 4..44, y 4..68 — рисуем в 2 раза меньше
                ctx.drawTexture(tex, px, py, pw, ph, 4f, 4f, 40, 64, 256, 128);
            }
            String name = getMessage().getString();
            ctx.drawText(tr(), name, x + (w - tr().getWidth(name)) / 2, y + h - 11, sel ? TEXT : MUTED, false);
            if (animated) roundRect(ctx, x + w - 7, y + 4, 4, 4, 2, accent());
        }
    }

    /** Цветной кружок выбора акцента. */
    public static final class Swatch extends Base {
        private final int rgb;
        private final Runnable onPress;

        public Swatch(int x, int y, int size, int rgb, String name, Runnable onPress) {
            super(x, y, size, size, Text.literal(name));
            this.rgb = rgb; this.onPress = onPress;
        }

        @Override public void onClick(double mx, double my) { onPress.run(); }

        @Override
        protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), s = getWidth();
            boolean sel = HalcyonConfig.get().accentRgb() == rgb;
            if (sel || isHovered()) roundBorder(ctx, x - 2, y - 2, s + 4, s + 4, (s + 4) / 2, sel ? 0xFFFFFFFF : 0x66FFFFFF);
            roundRect(ctx, x, y, s, s, s / 2, 0xFF000000 | rgb);
        }
    }
}
