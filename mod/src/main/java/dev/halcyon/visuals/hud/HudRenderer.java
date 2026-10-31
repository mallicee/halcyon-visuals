package dev.halcyon.visuals.hud;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.config.HalcyonConfig.Element;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.halcyon.visuals.render.Render2D.*;

/**
 * Кастомный HUD. Каждый элемент привязан к краю или центру экрана с отступом в пикселях интерфейса,
 * координаты округляются до целых — без «съезжания» при другом масштабе интерфейса.
 */
public final class HudRenderer {
    private static final int PAD = 4;

    private static final Identifier HEART_CONTAINER = Identifier.ofVanilla("hud/heart/container");
    private static final Identifier HEART_FULL = Identifier.ofVanilla("hud/heart/full");
    private static final Identifier HEART_HALF = Identifier.ofVanilla("hud/heart/half");
    private static final Identifier ARMOR_FULL = Identifier.ofVanilla("hud/armor_full");
    private static final Identifier ARMOR_HALF = Identifier.ofVanilla("hud/armor_half");
    private static final Identifier ARMOR_EMPTY = Identifier.ofVanilla("hud/armor_empty");
    private static final Identifier FOOD_FULL = Identifier.ofVanilla("hud/food_full");
    private static final Identifier FOOD_HALF = Identifier.ofVanilla("hud/food_half");
    private static final Identifier FOOD_EMPTY = Identifier.ofVanilla("hud/food_empty");
    private static final int FOOD_COLOR = 0xFFE8A85C;

    /** Прямоугольники элементов за последний кадр (x, y, w, h в координатах экрана) — для редактора расположения. */
    private static final Map<String, int[]> RECTS = new LinkedHashMap<>();

    private HudRenderer() {}

    @FunctionalInterface
    private interface Painter { void paint(DrawContext ctx, int x, int y); }

    private record Widget(int w, int h, Painter painter) {}

    public static Map<String, int[]> rects() { return RECTS; }

    /** Используется миксином InGameHud: заменяем ли этот ванильный элемент. */
    public static boolean replaces(String key) {
        HalcyonConfig cfg = HalcyonConfig.get();
        Element e = cfg.element(key);
        return cfg.enabled && e != null && e.on;
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        RECTS.clear();
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        HalcyonConfig cfg = HalcyonConfig.get();
        if (player == null || mc.options.hudHidden || !cfg.enabled) return;

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        boolean statusBars = mc.interactionManager != null && mc.interactionManager.hasStatusBars();

        for (String key : HalcyonConfig.ORDER) {
            Element e = cfg.element(key);
            if (e == null || !e.on || key.equals("cross")) continue;
            if (key.equals("features")) { renderFeatures(ctx, mc.textRenderer, cfg, e, sw, sh); continue; }
            if (!statusBars && (key.equals("health") || key.equals("armor") || key.equals("food") || key.equals("xp"))) continue;
            if (key.equals("hotbar") && player.isSpectator()) continue;

            Widget w = widget(key, e.mode, mc, player, cfg);
            if (w == null) continue;
            float s = (float) (cfg.scale * e.size);
            int boxW = w.w() + PAD * 2, boxH = w.h() + PAD * 2;
            int sbw = Math.round(boxW * s), sbh = Math.round(boxH * s);
            int[] pos = HalcyonConfig.resolve(e, sbw, sbh, sw, sh);
            RECTS.put(key, new int[]{pos[0], pos[1], sbw, sbh});

            MatrixStack m = ctx.getMatrices();
            m.push();
            m.translate(pos[0], pos[1], 0);
            m.scale(s, s, 1);
            panel(ctx, cfg, boxW, boxH);
            w.painter().paint(ctx, PAD, PAD);
            m.pop();
        }

        CrosshairRenderer.render(ctx, mc, cfg, sw, sh);
        Notifications.render(ctx, mc.textRenderer, cfg, sw, sh);
    }

    /** Названия включённых визуалов для списка справа сверху. */
    private static List<String> activeFeatures(HalcyonConfig cfg) {
        List<String> f = new ArrayList<>();
        f.add("HUD · " + cap(cfg.style));
        Element cross = cfg.element("cross");
        if (cross != null && cross.on) f.add("Crosshair");
        if (cfg.hitEffects.enabled) f.add("Hit Effects");
        var sword = dev.halcyon.visuals.skins.SwordSkins.get(cfg.swords.skin);
        if (sword != null) f.add("Sword · " + sword.name());
        var cape = dev.halcyon.visuals.skins.Capes.get(cfg.cosmetics.cape);
        if (cape != null) f.add("Cape · " + cape.name());
        if (cfg.cosmetics.trail || cfg.cosmetics.ring) f.add("Aura");
        if (!cfg.hand.swing.equals("vanilla")) f.add("Swing · " + cap(cfg.hand.swing));
        if (cfg.outline.enabled) f.add("Block Outline");
        if (cfg.combat.damageNumbers) f.add("Damage Numbers");
        if (cfg.combat.killEffect) f.add("Kill Effect");
        if (!cfg.atmosphere.sky.equals("vanilla")) f.add("Sky · " + cap(cfg.atmosphere.sky));
        if (cfg.gui.tooltips) f.add("Tooltips");
        if (cfg.gui.enabled) f.add("Glass GUI");
        if (cfg.gui.animation) f.add("Animations");
        if (cfg.optimization.lowFire) f.add("Low Fire");
        if (cfg.optimization.backgroundFps) f.add("Background FPS");
        switch (cfg.optimization.activePreset) {
            case "MAX" -> f.add("FPS Boost · Max");
            case "BALANCE" -> f.add("FPS Boost · Balance");
            default -> { }
        }
        return f;
    }

    private static String cap(String s) { return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    /** Список визуалов: строки по длине, выезжают справа, текст градиентом. */
    private static void renderFeatures(DrawContext ctx, TextRenderer tr, HalcyonConfig cfg, Element e, int sw, int sh) {
        List<String> items = activeFeatures(cfg);
        items.sort(Comparator.comparingInt((String it) -> tr.getWidth(it)).reversed());
        int lineH = 11, w = 0;
        for (String s : items) w = Math.max(w, tr.getWidth(s) + 9);
        int h = items.size() * lineH;
        float s = (float) (cfg.scale * e.size);
        int sbw = Math.round(w * s), sbh = Math.round(h * s);
        int[] pos = HalcyonConfig.resolve(e, sbw, sbh, sw, sh);
        RECTS.put("features", new int[]{pos[0], pos[1], sbw, sbh});

        int accent = cfg.accentRgb(), accent2 = 0x20D6C8;
        boolean gradient = e.mode.equals("gradient");
        double t = net.minecraft.util.Util.getMeasuringTimeMs() / 2600.0;
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(pos[0], pos[1], 0);
        m.scale(s, s, 1);
        for (int i = 0; i < items.size(); i++) {
            String name = items.get(i);
            float a = Anim.appear("feat:" + name);
            int lw = tr.getWidth(name) + 9;
            int lx = w - lw + Math.round((1f - a) * (lw + 4));
            int ly = i * lineH;
            int color = gradient ? lerpColor(accent, accent2, (float) (0.5 - 0.5 * Math.cos(((i * 0.12) + t) % 1.0 * Math.PI * 2))) : 0xFF000000 | accent;
            if (!cfg.style.equals("minimal")) ctx.fill(lx, ly, w, ly + lineH, argb(0x0D0D11, cfg.opacity * 0.8));
            ctx.fill(w - 1, ly, w, ly + lineH, color);
            ctx.drawText(tr, name, lx + 4, ly + 2, color, cfg.style.equals("minimal"));
        }
        m.pop();
    }

    private static void panel(DrawContext ctx, HalcyonConfig cfg, int w, int h) {
        int r = (int) Math.round(cfg.radius * 0.4);
        double o = cfg.opacity;
        switch (cfg.style) {
            case "solid" -> roundRect(ctx, 0, 0, w, h, r, argb(0x0A0A0E, o));
            case "outline" -> {
                roundRect(ctx, 0, 0, w, h, r, argb(0x0A0A0E, o * 0.3));
                roundBorder(ctx, 0, 0, w, h, r, argb(cfg.accentRgb(), 0.9));
            }
            case "minimal" -> { }
            default -> { // glass
                roundRect(ctx, 0, 0, w, h, r, argb(0x0D0D11, o * 0.75));
                roundBorder(ctx, 0, 0, w, h, r, argb(0xFFFFFF, o * 0.3));
            }
        }
        if (cfg.accentLine && !cfg.style.equals("minimal")) {
            accentLine(ctx, 0, 0, w, Math.max(r, 2), 0xFF000000 | cfg.accentRgb(), 0xFF20D6C8);
        }
    }

    private static void text(DrawContext ctx, TextRenderer tr, HalcyonConfig cfg, String s, int x, int y, int color) {
        ctx.drawText(tr, s, x, y, color, cfg.style.equals("minimal"));
    }

    private static void rightText(DrawContext ctx, TextRenderer tr, HalcyonConfig cfg, String s, int right, int y, int color) {
        text(ctx, tr, cfg, s, right - tr.getWidth(s), y, color);
    }

    /** Полоса с подписью: общий вид для здоровья, брони и голода. */
    private static Widget labeledBar(TextRenderer tr, HalcyonConfig cfg, String label, String value, float progress, int color, float extra, int extraColor) {
        return new Widget(84, 15, (ctx, x, y) -> {
            text(ctx, tr, cfg, label, x, y, MUTED);
            rightText(ctx, tr, cfg, value, x + 84, y, TEXT);
            bar(ctx, x, y + 11, 84, 4, progress, 0x1FFFFFFF, color);
            if (extra > 0) bar(ctx, x, y + 11, 84, 4, extra, 0, extraColor);
        });
    }

    /** Ряд из 10 иконок (половинки — по чётности), как в ванили. */
    private static Widget iconRow(int value, Identifier full, Identifier half, Identifier empty, boolean rightToLeft) {
        return new Widget(81, 9, (ctx, x, y) -> {
            for (int i = 0; i < 10; i++) {
                int ix = rightToLeft ? x + 72 - i * 8 : x + i * 8;
                Identifier id = i * 2 + 1 < value ? full : i * 2 + 1 == value ? half : empty;
                if (empty == null) ctx.drawGuiTexture(HEART_CONTAINER, ix, y, 9, 9);
                if (id != null) ctx.drawGuiTexture(id, ix, y, 9, 9);
            }
        });
    }

    private static Widget iconNumber(TextRenderer tr, HalcyonConfig cfg, Identifier icon, String value) {
        return new Widget(12 + tr.getWidth(value), 9, (ctx, x, y) -> {
            ctx.drawGuiTexture(icon, x, y, 9, 9);
            text(ctx, tr, cfg, value, x + 12, y + 1, TEXT);
        });
    }

    private static Widget widget(String key, String mode, MinecraftClient mc, ClientPlayerEntity p, HalcyonConfig cfg) {
        TextRenderer tr = mc.textRenderer;
        int accent = 0xFF000000 | cfg.accentRgb();

        switch (key) {
            case "health": {
                float hp = p.getHealth(), max = Math.max(1, p.getMaxHealth()), abs = p.getAbsorptionAmount();
                if (mode.equals("hearts")) {
                    int hearts = Math.min(30, (int) Math.ceil(max / 2));
                    int rows = (hearts + 9) / 10;
                    int filled = (int) Math.ceil(hp);
                    return new Widget(81, rows * 10 - 1, (ctx, x, y) -> {
                        for (int i = 0; i < hearts; i++) {
                            int hx = x + (i % 10) * 8, hy = y + (rows - 1 - i / 10) * 10;
                            ctx.drawGuiTexture(HEART_CONTAINER, hx, hy, 9, 9);
                            if (i * 2 + 1 < filled) ctx.drawGuiTexture(HEART_FULL, hx, hy, 9, 9);
                            else if (i * 2 + 1 == filled) ctx.drawGuiTexture(HEART_HALF, hx, hy, 9, 9);
                        }
                    });
                }
                String extra = abs > 0 ? " +" + Math.round(abs) : "";
                if (mode.equals("num")) return iconNumber(tr, cfg, HEART_FULL, Math.round(hp) + extra);
                return labeledBar(tr, cfg, "HP", Math.round(hp) + " / " + Math.round(max) + extra, Anim.smooth("hp", hp / max), RED,
                        Anim.smooth("abs", Math.min(1, abs / max)), GOLD);
            }
            case "armor": {
                int armor = p.getArmor();
                if (mode.equals("icons")) return iconRow(armor, ARMOR_FULL, ARMOR_HALF, ARMOR_EMPTY, false);
                if (mode.equals("num")) return iconNumber(tr, cfg, ARMOR_FULL, String.valueOf(armor));
                return labeledBar(tr, cfg, "ARMOR", armor + " / 20", Anim.smooth("armor", armor / 20f), accent, 0, 0);
            }
            case "food": {
                int food = p.getHungerManager().getFoodLevel();
                float sat = p.getHungerManager().getSaturationLevel();
                if (mode.equals("icons")) return iconRow(food, FOOD_FULL, FOOD_HALF, FOOD_EMPTY, true);
                if (mode.equals("num")) return iconNumber(tr, cfg, FOOD_FULL, String.valueOf(food));
                return labeledBar(tr, cfg, "FOOD", food + " / 20", Anim.smooth("food", food / 20f), FOOD_COLOR,
                        Anim.smooth("sat", Math.min(1, sat / 20f)), 0x66FFFFFF);
            }
            case "xp": {
                int lvl = p.experienceLevel;
                float prog = p.experienceProgress;
                if (mode.equals("level")) {
                    String n = String.valueOf(lvl);
                    int nw = tr.getWidth(n);
                    return new Widget(nw + 4 + tr.getWidth("LVL"), 9, (ctx, x, y) -> {
                        text(ctx, tr, cfg, n, x, y, accent);
                        text(ctx, tr, cfg, "LVL", x + nw + 4, y, MUTED);
                    });
                }
                String right = "LVL " + lvl + " · " + Math.round(prog * 100) + "%";
                float shown = Anim.smooth("xp", prog);
                return new Widget(182, 14, (ctx, x, y) -> {
                    text(ctx, tr, cfg, "XP", x, y, MUTED);
                    rightText(ctx, tr, cfg, right, x + 182, y, accent);
                    bar(ctx, x, y + 11, 182, 3, shown, 0x1FFFFFFF, accent);
                });
            }
            case "watermark": {
                String fps = mc.getCurrentFps() + " fps";
                String sep = "  |  ";
                List<String[]> parts = new ArrayList<>(); // текст, цвет: "a" акцент, "t" текст, "m" приглушённый
                if (mode.equals("full")) {
                    parts.add(new String[]{mc.getSession().getUsername(), "t"});
                    parts.add(new String[]{fps, "a"});
                    parts.add(new String[]{pingOf(mc, p) + " ms", "t"});
                    LocalTime now = LocalTime.now();
                    parts.add(new String[]{String.format("%02d:%02d", now.getHour(), now.getMinute()), "t"});
                } else {
                    parts.add(new String[]{fps, "a"});
                }
                int logoW = tr.getWidth("halcyon.");
                int fpsW = tr.getWidth("9999 fps"); // фиксированная ширина под FPS — без «дыхания»
                int total = logoW;
                for (String[] part : parts) total += tr.getWidth(sep) + (part[0].equals(fps) ? fpsW : tr.getWidth(part[0]));
                return new Widget(total, 9, (ctx, x, y) -> {
                    int cx = x;
                    text(ctx, tr, cfg, "halcyon", cx, y, TEXT);
                    text(ctx, tr, cfg, ".", cx + tr.getWidth("halcyon"), y, accent);
                    cx += logoW;
                    for (String[] part : parts) {
                        text(ctx, tr, cfg, sep, cx, y, 0x59FFFFFF);
                        cx += tr.getWidth(sep);
                        int col = part[1].equals("a") ? accent : TEXT;
                        text(ctx, tr, cfg, part[0], cx, y, col);
                        cx += part[0].equals(fps) ? fpsW : tr.getWidth(part[0]);
                    }
                });
            }
            case "hotbar": {
                boolean compact = mode.equals("compact");
                int slot = compact ? 18 : 20, gap = 2;
                int sel = p.getInventory().selectedSlot;
                int r = Math.min(4, (int) Math.round(cfg.radius * 0.3));
                return new Widget(9 * slot + 8 * gap, slot, (ctx, x, y) -> {
                    for (int i = 0; i < 9; i++) {
                        int sx = x + i * (slot + gap);
                        roundRect(ctx, sx, y, slot, slot, r, i == sel ? argb(cfg.accentRgb(), 0.16) : 0x40000000);
                        roundBorder(ctx, sx, y, slot, slot, r, i == sel ? accent : 0x2EFFFFFF);
                        ItemStack stack = p.getInventory().getStack(i);
                        if (!stack.isEmpty()) {
                            int ix = sx + (slot - 16) / 2, iy = y + (slot - 16) / 2;
                            ctx.drawItem(stack, ix, iy);
                            ctx.drawItemInSlot(tr, stack, ix, iy);
                        }
                    }
                });
            }
            case "coords": {
                BlockPos pos = p.getBlockPos();
                String line1 = pos.getX() + " / " + pos.getY() + " / " + pos.getZ();
                int lw = tr.getWidth("XYZ ");
                if (mode.equals("short")) {
                    return new Widget(lw + tr.getWidth(line1), 9, (ctx, x, y) -> {
                        text(ctx, tr, cfg, "XYZ ", x, y, MUTED);
                        text(ctx, tr, cfg, line1, x + lw, y, accent);
                    });
                }
                String biome = mc.world == null ? "?" : mc.world.getBiome(pos).getKey()
                        .map(k -> pretty(k.getValue().getPath())).orElse("?");
                String line2 = biome + " · " + p.getHorizontalFacing().asString().toUpperCase();
                int w = Math.max(lw + tr.getWidth(line1), tr.getWidth(line2));
                return new Widget(w, 19, (ctx, x, y) -> {
                    text(ctx, tr, cfg, "XYZ ", x, y, MUTED);
                    text(ctx, tr, cfg, line1, x + lw, y, accent);
                    text(ctx, tr, cfg, line2, x, y + 10, MUTED);
                });
            }
            case "fps": {
                String fps = mc.getCurrentFps() + " FPS";
                String ping = pingOf(mc, p) + " ms";
                boolean both = mode.equals("both");
                // Фиксированная ширина: панель не «дышит», когда меняется число
                int w = Math.max(tr.getWidth("9999 FPS"), both ? tr.getWidth("9999 ms") : 0);
                return new Widget(w, both ? 19 : 9, (ctx, x, y) -> {
                    rightText(ctx, tr, cfg, fps, x + w, y, accent);
                    if (both) rightText(ctx, tr, cfg, ping, x + w, y + 10, MUTED);
                });
            }
            case "keys": {
                boolean mouse = mode.equals("mouse");
                int k = 13, g = 2, w = 3 * k + 2 * g;
                int r = Math.min(3, (int) Math.round(cfg.radius * 0.25));
                return new Widget(w, mouse ? 3 * k + 2 * g : 2 * k + g, (ctx, x, y) -> {
                    key(ctx, tr, cfg, "W", mc.options.forwardKey.isPressed(), x + k + g, y, k, k, r);
                    key(ctx, tr, cfg, "A", mc.options.leftKey.isPressed(), x, y + k + g, k, k, r);
                    key(ctx, tr, cfg, "S", mc.options.backKey.isPressed(), x + k + g, y + k + g, k, k, r);
                    key(ctx, tr, cfg, "D", mc.options.rightKey.isPressed(), x + 2 * (k + g), y + k + g, k, k, r);
                    if (mouse) {
                        int half = (w - g) / 2, my = y + 2 * (k + g);
                        key(ctx, tr, cfg, "L " + InputStats.leftCps(), mc.options.attackKey.isPressed(), x, my, half, k, r);
                        key(ctx, tr, cfg, "R " + InputStats.rightCps(), mc.options.useKey.isPressed(), x + half + g, my, w - half - g, k, r);
                    }
                });
            }
            case "potions": {
                List<StatusEffectInstance> effects = new ArrayList<>();
                for (StatusEffectInstance i : p.getStatusEffects()) if (i.shouldShowIcon()) effects.add(i);
                if (effects.isEmpty()) return null;
                effects.sort(Comparator.comparing((StatusEffectInstance i) -> !i.getEffectType().value().isBeneficial())
                        .thenComparingInt(StatusEffectInstance::getDuration));
                if (mode.equals("icons")) {
                    return new Widget(effects.size() * 20 - 2, 28, (ctx, x, y) -> {
                        for (int i = 0; i < effects.size(); i++) {
                            StatusEffectInstance ef = effects.get(i);
                            int ix = x + i * 20;
                            ctx.drawSprite(ix, y, 0, 18, 18, mc.getStatusEffectSpriteManager().getSprite(ef.getEffectType()));
                            String d = shortDuration(ef);
                            text(ctx, tr, cfg, d, ix + (18 - tr.getWidth(d)) / 2, y + 20, durationColor(ef, accent));
                        }
                    });
                }
                List<String> names = new ArrayList<>();
                int w = 0;
                for (StatusEffectInstance ef : effects) {
                    String n = ef.getEffectType().value().getName().getString() + (ef.getAmplifier() > 0 ? " " + roman(ef.getAmplifier() + 1) : "");
                    names.add(n);
                    w = Math.max(w, 22 + tr.getWidth(n) + 8 + tr.getWidth(longDuration(ef)));
                }
                int width = Math.max(100, w);
                return new Widget(width, effects.size() * 20 - 2, (ctx, x, y) -> {
                    for (int i = 0; i < effects.size(); i++) {
                        StatusEffectInstance ef = effects.get(i);
                        int iy = y + i * 20;
                        ctx.drawSprite(x, iy, 0, 18, 18, mc.getStatusEffectSpriteManager().getSprite(ef.getEffectType()));
                        text(ctx, tr, cfg, names.get(i), x + 22, iy + 5, ef.getEffectType().value().isBeneficial() ? TEXT : RED);
                        rightText(ctx, tr, cfg, longDuration(ef), x + width, iy + 5, durationColor(ef, accent));
                    }
                });
            }
            case "durability": {
                List<ItemStack> items = new ArrayList<>();
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    ItemStack s = p.getEquippedStack(slot);
                    if (!s.isEmpty()) items.add(s);
                }
                ItemStack main = p.getMainHandStack();
                if (!main.isEmpty() && main.isDamageable()) items.add(main);
                if (items.isEmpty()) return null;
                boolean bars = mode.equals("bars");
                int w = 20 + (bars ? 32 : tr.getWidth("100%"));
                return new Widget(w, items.size() * 18 - 2, (ctx, x, y) -> {
                    for (int i = 0; i < items.size(); i++) {
                        ItemStack s = items.get(i);
                        int iy = y + i * 18;
                        ctx.drawItem(s, x, iy);
                        if (s.isDamageable() && s.getMaxDamage() > 0) {
                            float f = 1f - (float) s.getDamage() / s.getMaxDamage();
                            int col = f > 0.5f ? accent : f > 0.2f ? GOLD : RED;
                            if (bars) bar(ctx, x + 20, iy + 6, 32, 4, f, 0x1FFFFFFF, col);
                            else text(ctx, tr, cfg, Math.round(f * 100) + "%", x + 20, iy + 4, col);
                        } else {
                            text(ctx, tr, cfg, "×" + s.getCount(), x + 20, iy + 4, TEXT);
                        }
                    }
                });
            }
            case "clock": {
                String value, label;
                if (mode.equals("game") && mc.world != null) {
                    long t = mc.world.getTimeOfDay();
                    long day = t / 24000 + 1, tod = (t + 6000) % 24000;
                    value = String.format("%02d:%02d", tod / 1000, (tod % 1000) * 60 / 1000);
                    label = "ДЕНЬ " + day;
                } else {
                    LocalTime now = LocalTime.now();
                    value = String.format("%02d:%02d", now.getHour(), now.getMinute());
                    label = "ВРЕМЯ";
                }
                int lw = tr.getWidth(label);
                return new Widget(lw + 6 + tr.getWidth("00:00"), 9, (ctx, x, y) -> {
                    text(ctx, tr, cfg, label, x, y, MUTED);
                    text(ctx, tr, cfg, value, x + lw + 6, y, accent);
                });
            }
            default:
                return null;
        }
    }

    private static String shortDuration(StatusEffectInstance e) {
        if (e.isInfinite()) return "∞";
        int s = e.getDuration() / 20;
        return s >= 60 ? (s / 60) + "m" : s + "s";
    }

    private static String longDuration(StatusEffectInstance e) {
        if (e.isInfinite()) return "∞";
        int s = e.getDuration() / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    /** Последние 10 секунд эффекта подсвечиваются тёплым цветом. */
    private static int durationColor(StatusEffectInstance e, int accent) {
        return !e.isInfinite() && e.getDuration() < 200 ? GOLD : MUTED;
    }

    private static String roman(int n) {
        return switch (n) { case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(n); };
    }

    private static void key(DrawContext ctx, TextRenderer tr, HalcyonConfig cfg, String label, boolean down, int x, int y, int w, int h, int r) {
        if (down) roundRect(ctx, x, y, w, h, r, 0xFF000000 | cfg.accentRgb());
        else roundBorder(ctx, x, y, w, h, r, 0x4DFFFFFF);
        int tx = x + (w - tr.getWidth(label) + 1) / 2, ty = y + (h - 7) / 2;
        ctx.drawText(tr, label, tx, ty, down ? 0xFF04120B : TEXT, false);
    }

    private static int pingOf(MinecraftClient mc, ClientPlayerEntity p) {
        if (mc.getNetworkHandler() == null) return 0;
        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
        return e == null ? 0 : e.getLatency();
    }

    private static String pretty(String path) {
        StringBuilder sb = new StringBuilder();
        for (String part : path.split("_")) {
            if (part.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
