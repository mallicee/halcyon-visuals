package dev.halcyon.visuals.gui;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.config.HalcyonConfig.Element;
import dev.halcyon.visuals.gui.HWidgets.Button;
import dev.halcyon.visuals.gui.HWidgets.Button.Kind;
import dev.halcyon.visuals.perf.Optimizer;
import dev.halcyon.visuals.skins.SwordSkins;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.halcyon.visuals.render.Render2D.*;

/**
 * Меню Halcyon Visuals (правый Shift). Панель слева, HUD остаётся виден справа —
 * каждое изменение сразу отображается в игре. Конфиг сохраняется при закрытии.
 */
public final class HalcyonScreen extends Screen {
    private enum Tab {
        STYLE("Стиль"), ELEMENTS("HUD"), SWORDS("Мечи"), COSMETICS("Косметика"),
        ANIM("Анимации"), CROSS("Прицел"), FX("Эффекты"), WORLD("Мир"), FPS("Буст FPS");
        final String label;
        Tab(String label) { this.label = label; }
    }

    private static Tab tab = Tab.STYLE;
    private static final int[][] ACCENTS = {{0x3dffa0}, {0x20d6c8}, {0x8ab4ff}, {0xb18cff}, {0xff5c7a}, {0xffc861}};
    private static final Map<String, String> EL_NAMES = Map.ofEntries(
            Map.entry("health", "Здоровье"), Map.entry("armor", "Броня"), Map.entry("food", "Голод"), Map.entry("xp", "Опыт"),
            Map.entry("hotbar", "Хотбар"), Map.entry("coords", "Координаты"), Map.entry("fps", "FPS и пинг"),
            Map.entry("keys", "Клавиши"), Map.entry("potions", "Эффекты зелий"), Map.entry("durability", "Прочность"),
            Map.entry("clock", "Часы"), Map.entry("watermark", "Вотермарка"), Map.entry("features", "Список визуалов"));
    private static final Map<String, String> MODE_NAMES = Map.ofEntries(
            Map.entry("bar", "Полоса"), Map.entry("hearts", "Сердца"), Map.entry("num", "Число"),
            Map.entry("icons", "Иконки"), Map.entry("level", "Уровень"), Map.entry("slots", "Слоты"),
            Map.entry("compact", "Компакт"), Map.entry("full", "Полные"), Map.entry("short", "XYZ"),
            Map.entry("both", "FPS+пинг"), Map.entry("fps", "FPS"), Map.entry("wasd", "WASD"),
            Map.entry("mouse", "+ мышь"), Map.entry("plus", "Крест"), Map.entry("dot", "Точка"), Map.entry("ring", "Кольцо"),
            Map.entry("list", "Список"), Map.entry("percent", "Проценты"), Map.entry("bars", "Полосы"),
            Map.entry("real", "Реальное"), Map.entry("game", "Игровое"),
            Map.entry("gradient", "Градиент"), Map.entry("accent", "Акцент"));

    private static final Map<String, String> SKY_NAMES = Map.of(
            "vanilla", "Ванильное", "halcyon", "Halcyon", "sunset", "Закат", "aurora", "Сияние", "night", "Глубокая ночь");
    private static final Map<String, String> SWING_NAMES = Map.of(
            "vanilla", "Ванильный", "smooth", "Плавный", "spin", "С разворота", "chop", "Рубящий");

    private record Label(String text, int x, int y, int color) {}

    @Nullable private final Screen parent;
    private final List<Label> labels = new ArrayList<>();
    private int px, py, pw, ph, contentTop, contentBottom, contentH, scroll, cy, swordPreviewY;
    private long openedAt = -1;

    public HalcyonScreen(@Nullable Screen parent) {
        super(Text.literal("Halcyon Visuals"));
        this.parent = parent;
    }

    private static HalcyonConfig cfg() { return HalcyonConfig.get(); }

    @Override
    protected void init() {
        if (openedAt < 0) openedAt = Util.getMeasuringTimeMs();
        build();
    }

    private void build() {
        clearChildren();
        labels.clear();
        pw = Math.min(296, width - 16);
        ph = height - 16;
        px = 8; py = 8;

        // Вкладки в два ряда; ширина каждой — по длине подписи
        Tab[] tabs = Tab.values();
        int perRow = (tabs.length + 1) / 2;
        for (int row = 0; row * perRow < tabs.length; row++) {
            int from = row * perRow, to = Math.min(tabs.length, from + perRow);
            int total = 0;
            for (int i = from; i < to; i++) total += textRenderer.getWidth(tabs[i].label) + 10;
            float k = (pw - 16f - (to - from - 1) * 2) / total;
            int tx = px + 8;
            for (int i = from; i < to; i++) {
                Tab t = tabs[i];
                int tw = i == to - 1 ? px + pw - 8 - tx : Math.round((textRenderer.getWidth(t.label) + 10) * k);
                addDrawableChild(new Button(tx, py + 28 + row * 16, tw, 14, t.label, Kind.TAB, () -> tab == t,
                        () -> { tab = t; scroll = 0; build(); }));
                tx += tw + 2;
            }
        }

        contentTop = py + 66;
        contentBottom = py + ph - 28;
        cy = contentTop - scroll;
        List<ClickableWidget> content = new ArrayList<>();
        int x = px + 8, w = pw - 16;

        switch (tab) {
            case STYLE -> {
                section("ЦВЕТ ИНТЕРФЕЙСА", x);
                for (int i = 0; i < ACCENTS.length; i++) {
                    int rgb = ACCENTS[i][0];
                    content.add(new HWidgets.Swatch(x + 4 + i * 20, cy, 12, rgb, "#" + Integer.toHexString(rgb), () -> cfg().setAccent(rgb)));
                }
                cy += 20;
                section("ПАНЕЛИ", x);
                content.add(new HWidgets.Cycle(x, cy, w, 16, "Стиль", () -> cap(cfg().style), () -> {
                    String[] s = HalcyonConfig.STYLES;
                    int i = java.util.Arrays.asList(s).indexOf(cfg().style);
                    cfg().style = s[(i + 1) % s.length];
                }));
                cy += 20;
                content.add(slider(x, w, "Прозрачность подложки", 15, 100, 5, () -> cfg().opacity * 100, v -> cfg().opacity = v / 100, v -> (int) v + "%"));
                content.add(slider(x, w, "Скругление", 0, 18, 1, () -> cfg().radius, v -> cfg().radius = v, v -> (int) v + " px"));
                content.add(slider(x, w, "Общий масштаб", 70, 130, 5, () -> cfg().scale * 100, v -> cfg().scale = v / 100, v -> (int) v + "%"));
                section("ОФОРМЛЕНИЕ", x);
                content.add(toggle(x, w, "Переливающаяся полоска на панелях", () -> cfg().accentLine, v -> cfg().accentLine = v));
                content.add(toggle(x, w, "Плавные полоски здоровья и опыта", () -> cfg().smoothBars, v -> cfg().smoothBars = v));
                content.add(toggle(x, w, "Уведомления справа снизу", () -> cfg().notifications, v -> cfg().notifications = v));
                content.add(toggle(x, w, "HUD Halcyon включён", () -> cfg().enabled, v -> cfg().enabled = v));
            }
            case ELEMENTS -> {
                section("ЭЛЕМЕНТ · ВИД · РАЗМЕР", x);
                for (String key : HalcyonConfig.MODES.keySet()) {
                    if (key.equals("cross")) continue;
                    Element e = cfg().element(key);
                    int nameW = Math.min(104, w / 3 + 10), modeW = 62;
                    content.add(new HWidgets.Toggle(x, cy, nameW, 16, EL_NAMES.get(key), () -> e.on, v -> e.on = v));
                    content.add(new HWidgets.Cycle(x + nameW + 4, cy, modeW, 16, "", () -> MODE_NAMES.get(e.mode), () -> e.mode = nextMode(key, e.mode)));
                    content.add(new HWidgets.Slider(x + nameW + modeW + 10, cy, w - nameW - modeW - 10, 16, "", 60, 160, 5,
                            () -> e.size * 100, v -> e.size = v / 100, v -> (int) v + "%"));
                    cy += 19;
                }
                cy += 4;
                note("Расставить элементы мышью — кнопка «Расположение» внизу.", x);
            }
            case SWORDS -> {
                swordPreviewY = cy;
                cy += 58;
                section("СКИНЫ МЕЧЕЙ", x);
                int half = (w - 4) / 2, i = 0;
                java.util.List<String[]> list = new ArrayList<>();
                list.add(new String[]{SwordSkins.NONE, "Ванильный", "0"});
                for (SwordSkins.Skin s : SwordSkins.ALL) list.add(new String[]{s.id(), s.name(), s.animated() ? "1" : "0"});
                for (String[] s : list) {
                    int col = i % 2;
                    content.add(new HWidgets.SkinCard(x + col * (half + 4), cy, col == 0 ? half : w - half - 4, 26, s[0], s[1], s[2].equals("1"),
                            () -> {
                                cfg().swords.skin = s[0];
                                dev.halcyon.visuals.hud.Notifications.push("Скин меча", s[1]);
                            }));
                    if (col == 1) cy += 30;
                    i++;
                }
                if (i % 2 == 1) cy += 30;
                note("Скин меняет вид всех мечей у вас в игре: в руке, в инвентаре и на земле. Урон и характеристики не меняются, другие игроки скин не видят.", x);
                cy += 4;
                section("УДОЧКИ", x);
                java.util.List<String[]> rods = new ArrayList<>();
                rods.add(new String[]{dev.halcyon.visuals.skins.RodSkins.NONE, "Ванильная"});
                for (var r : dev.halcyon.visuals.skins.RodSkins.ALL) rods.add(new String[]{r.id(), r.name()});
                int rw = (w - 12) / 4, ri = 0;
                for (String[] r : rods) {
                    int rx = x + ri * (rw + 4), rwid = ri == 3 ? w - 3 * (rw + 4) : rw;
                    content.add(new HWidgets.RodCard(rx, cy, rwid, 34, r[0], r[1], () -> {
                        cfg().loot.rod = r[0];
                        dev.halcyon.visuals.hud.Notifications.push("Удочка", r[1]);
                    }));
                    ri++;
                }
                cy += 38;
            }
            case CROSS -> {
                Element e = cfg().element("cross");
                HalcyonConfig.Crosshair c = cfg().crosshair;
                content.add(toggle(x, w, "Свой прицел Halcyon", () -> e.on, v -> e.on = v));
                content.add(new HWidgets.Cycle(x, cy, w, 16, "Вид", () -> MODE_NAMES.get(e.mode), () -> e.mode = nextMode("cross", e.mode)));
                cy += 20;
                content.add(slider(x, w, "Размер", 60, 160, 10, () -> e.size * 100, v -> e.size = v / 100, v -> (int) v + "%"));
                content.add(slider(x, w, "Зазор", 0, 8, 1, () -> c.gap, v -> c.gap = (int) v, v -> (int) v + " px"));
                content.add(slider(x, w, "Длина линий", 1, 10, 1, () -> c.length, v -> c.length = (int) v, v -> (int) v + " px"));
                content.add(slider(x, w, "Толщина", 1, 3, 1, () -> c.thickness, v -> c.thickness = (int) v, v -> (int) v + " px"));
                content.add(toggle(x, w, "Тёмная обводка", () -> c.outline, v -> c.outline = v));
                content.add(toggle(x, w, "Разброс при беге и прыжке", () -> c.dynamic, v -> c.dynamic = v));
                content.add(toggle(x, w, "Метка попадания", () -> c.hitMarker, v -> c.hitMarker = v));
                content.add(toggle(x, w, "Полоска перезарядки удара", () -> c.cooldown, v -> c.cooldown = v));
            }
            case FX -> {
                HalcyonConfig.Hits h = cfg().hitEffects;
                content.add(toggle(x, w, "Эффекты попаданий", () -> h.enabled, v -> h.enabled = v));
                content.add(slider(x, w, "Плотность частиц", 0, 48, 2, () -> h.density, v -> h.density = (int) v, v -> String.valueOf((int) v)));
                content.add(toggle(x, w, "Волна по земле", () -> h.shockwave, v -> h.shockwave = v));
                content.add(toggle(x, w, "Искры", () -> h.sparks, v -> h.sparks = v));
                note("Ударьте любое существо, чтобы увидеть эффект. Частицы видите только вы.", x);
                cy += 4;
                HalcyonConfig.Combat cb = cfg().combat;
                section("УРОН", x);
                content.add(toggle(x, w, "Цвет вспышки урона — акцент", () -> cb.damageTint,
                        v -> { cb.damageTint = v; dev.halcyon.visuals.effects.DamageTint.apply(); }));
                content.add(toggle(x, w, "Цифры урона от ваших ударов", () -> cb.damageNumbers, v -> cb.damageNumbers = v));
                content.add(toggle(x, w, "Эффект убийства", () -> cb.killEffect, v -> cb.killEffect = v));
                content.add(toggle(x, w, "Звук убийства", () -> cb.killSound, v -> cb.killSound = v));
                cy += 4;
                HalcyonConfig.Outline ol = cfg().outline;
                section("ОБВОДКА БЛОКА", x);
                content.add(toggle(x, w, "Своя обводка блока", () -> ol.enabled, v -> ol.enabled = v));
                content.add(new HWidgets.Cycle(x, cy, w, 16, "Цвет", () -> ol.color.equals("gradient") ? "Градиент" : "Акцент",
                        () -> ol.color = ol.color.equals("gradient") ? "accent" : "gradient"));
                cy += 20;
                content.add(toggle(x, w, "Заливка блока", () -> ol.fill, v -> ol.fill = v));
                content.add(slider(x, w, "Прозрачность заливки", 0, 50, 2, () -> ol.fillAlpha * 100, v -> ol.fillAlpha = v / 100, v -> (int) v + "%"));
            }
            case COSMETICS -> {
                section("ПЛАЩИ", x);
                java.util.List<String[]> capes = new ArrayList<>();
                capes.add(new String[]{dev.halcyon.visuals.skins.Capes.NONE, "Без плаща", "0"});
                for (var c : dev.halcyon.visuals.skins.Capes.ALL) capes.add(new String[]{c.id(), c.name(), c.animated() ? "1" : "0"});
                int cols = 3, cw = (w - (cols - 1) * 4) / cols, i = 0;
                for (String[] c : capes) {
                    int col = i % cols;
                    content.add(new HWidgets.CapeCard(x + col * (cw + 4), cy, col == cols - 1 ? w - (cols - 1) * (cw + 4) : cw, 50,
                            c[0], c[1], c[2].equals("1"), () -> {
                                cfg().cosmetics.cape = c[0];
                                dev.halcyon.visuals.hud.Notifications.push("Плащ", c[1]);
                            }));
                    if (col == cols - 1) cy += 54;
                    i++;
                }
                if (i % cols != 0) cy += 54;
                note("Плащ виден на вашем персонаже: нажмите F5. Другие игроки его не видят.", x);
                cy += 4;
                HalcyonConfig.Cosmetics c = cfg().cosmetics;
                section("АУРА", x);
                content.add(toggle(x, w, "След из частиц при беге", () -> c.trail, v -> c.trail = v));
                content.add(toggle(x, w, "Кольцо под ногами", () -> c.ring, v -> c.ring = v));
                content.add(toggle(x, w, "Вспышка при прыжке", () -> c.jumpBurst, v -> c.jumpBurst = v));
                content.add(slider(x, w, "Плотность ауры", 1, 5, 1, () -> c.density, v -> c.density = (int) v, v -> String.valueOf((int) v)));
            }
            case ANIM -> {
                HalcyonConfig.Hand h = cfg().hand;
                section("ПРЕДМЕТ В РУКЕ", x);
                content.add(new HWidgets.Cycle(x, cy, w, 16, "Взмах", () -> SWING_NAMES.get(h.swing), () -> {
                    String[] s = HalcyonConfig.SWINGS;
                    h.swing = s[(java.util.Arrays.asList(s).indexOf(h.swing) + 1) % s.length];
                }));
                cy += 20;
                content.add(slider(x, w, "Размер предмета", 50, 150, 5, () -> h.scale * 100, v -> h.scale = v / 100, v -> (int) v + "%"));
                content.add(slider(x, w, "Сдвиг влево / вправо", -10, 10, 1, () -> h.x * 10, v -> h.x = v / 10, v -> String.valueOf((int) v)));
                content.add(slider(x, w, "Сдвиг вниз / вверх", -10, 10, 1, () -> h.y * 10, v -> h.y = v / 10, v -> String.valueOf((int) v)));
                content.add(slider(x, w, "Ближе / дальше", -10, 10, 1, () -> h.z * 10, v -> h.z = v / 10, v -> String.valueOf((int) v)));
                content.add(toggle(x, w, "Покачивание при ходьбе", () -> h.sway, v -> h.sway = v));
                note("Только вид от первого лица. Скорость и механика атаки не меняются.", x);
                cy += 4;
                HalcyonConfig.Gui g = cfg().gui;
                section("ИНТЕРФЕЙС", x);
                content.add(toggle(x, w, "Стеклянные окна: сундуки, инвентарь, печи", () -> g.enabled, v -> g.enabled = v));
                content.add(toggle(x, w, "Анимация открытия окон", () -> g.animation, v -> g.animation = v));
                content.add(toggle(x, w, "Подсказки к предметам Halcyon", () -> g.tooltips, v -> g.tooltips = v));
                content.add(toggle(x, w, "Стеклянные кнопки в меню", () -> g.glassButtons, v -> g.glassButtons = v));
                content.add(toggle(x, w, "Главное меню Halcyon", () -> g.halcyonMenu, v -> g.halcyonMenu = v));
                note("F6 — скриншот без интерфейса (клавиша меняется в «Управление»).", x);
            }
            case WORLD -> {
                HalcyonConfig.Atmosphere at = cfg().atmosphere;
                section("НЕБО И ТУМАН", x);
                content.add(new HWidgets.Cycle(x, cy, w, 16, "Тема", () -> SKY_NAMES.get(at.sky), () -> {
                    String[] s = HalcyonConfig.SKIES;
                    at.sky = s[(java.util.Arrays.asList(s).indexOf(at.sky) + 1) % s.length];
                }));
                cy += 20;
                content.add(slider(x, w, "Сила оттенка", 5, 100, 5, () -> at.strength * 100, v -> at.strength = v / 100, v -> (int) v + "%"));
                note("Меняется оттенок неба и тумана в обычном мире; день и ночь сохраняются.", x);
                cy += 4;
                section("ПОГОДА", x);
                boolean weather = dev.halcyon.visuals.effects.Atmosphere.weatherEnabled();
                content.add(new Button(x, cy, w, 18, weather ? "Выключить погоду Halcyon" : "Включить погоду Halcyon",
                        weather ? Kind.GHOST : Kind.PRIMARY, null, () -> {
                            dev.halcyon.visuals.effects.Atmosphere.setWeather(!weather);
                            build();
                        }));
                cy += 22;
                note("Светящиеся капли дождя и мягкий снег. После нажатия игра перезагрузит ресурсы — это пара секунд.", x);
                cy += 4;
                section("ЗАЧАРОВАНИЯ", x);
                boolean glint = dev.halcyon.visuals.effects.Atmosphere.packEnabled(dev.halcyon.visuals.effects.Atmosphere.GLINT_PACK);
                content.add(new Button(x, cy, w, 18, glint ? "Вернуть фиолетовый блеск" : "Изумрудный блеск зачарований",
                        glint ? Kind.GHOST : Kind.PRIMARY, null, () -> {
                            dev.halcyon.visuals.effects.Atmosphere.setPack(dev.halcyon.visuals.effects.Atmosphere.GLINT_PACK, !glint);
                            build();
                        }));
                cy += 22;
                cy += 4;
                HalcyonConfig.Loot lt = cfg().loot;
                section("ЛУТ", x);
                content.add(toggle(x, w, "Сияние редких предметов на земле", () -> lt.rareGlow, v -> lt.rareGlow = v));
                content.add(toggle(x, w, "Уведомления о подобранных предметах", () -> lt.pickups, v -> lt.pickups = v));
                note("Сияние — обычные частицы: видны только в прямой видимости, сквозь стены не светят.", x);
            }
            case FPS -> {
                HalcyonConfig.Optimization o = cfg().optimization;
                section("ПРЕСЕТЫ ГРАФИКИ", x);
                int half = (w - 4) / 2;
                content.add(new Button(x, cy, half, 18, "Максимум FPS", Kind.PRIMARY, null,
                        () -> { Optimizer.apply(Optimizer.Preset.MAX); build(); }));
                content.add(new Button(x + half + 4, cy, w - half - 4, 18, "Баланс", Kind.GHOST, null,
                        () -> { Optimizer.apply(Optimizer.Preset.BALANCE); build(); }));
                cy += 22;
                if (o.backup != null) {
                    content.add(new Button(x, cy, w, 16, "Вернуть мои настройки", Kind.GHOST, null,
                            () -> { Optimizer.restore(); build(); }));
                    cy += 20;
                }
                labels.add(new Label("Сейчас: " + Optimizer.activeTitle(), x + 2, cy, 0xFF000000 | cfg().accentRgb()));
                cy += 12;
                note("«Максимум FPS»: прорисовка 8, быстрая графика, без облаков и теней, минимум частиц. Всё возвращается одной кнопкой.", x);
                cy += 4;
                section("МОДЫ ОПТИМИЗАЦИИ", x);
                for (Optimizer.ModStatus m : Optimizer.mods()) {
                    labels.add(new Label((m.loaded() ? "✔ " : "✖ ") + m.name() + " — " + m.what(), x + 2, cy,
                            m.loaded() ? 0xFF000000 | cfg().accentRgb() : MUTED));
                    cy += 11;
                }
                note("Дают основной прирост FPS. Ставятся галочкой «Оптимизация» в Halcyon Loader.", x);
                cy += 4;
                section("ДОПОЛНИТЕЛЬНО", x);
                content.add(toggle(x, w, "FPS в фоне: " + o.backgroundLimit, () -> o.backgroundFps, v -> o.backgroundFps = v));
                content.add(toggle(x, w, "Низкий огонь на экране", () -> o.lowFire, v -> o.lowFire = v));
            }
        }
        contentH = cy + scroll - contentTop;

        for (ClickableWidget wdg : content) {
            wdg.visible = wdg.getY() >= contentTop - 1 && wdg.getY() + wdg.getHeight() <= contentBottom + 1;
            addDrawableChild(wdg);
        }

        int fy = py + ph - 22, bw = (pw - 16 - 8) / 3;
        addDrawableChild(new Button(px + 8, fy, bw, 16, "Сбросить", Kind.GHOST, null, () -> { HalcyonConfig.reset(); build(); }));
        addDrawableChild(new Button(px + 8 + bw + 4, fy, bw, 16, "Расположение", Kind.GHOST, null,
                () -> { if (client != null) client.setScreen(new LayoutScreen(this)); }));
        addDrawableChild(new Button(px + 8 + 2 * (bw + 4), fy, bw, 16, "Готово", Kind.PRIMARY, null, this::close));
    }

    private void section(String text, int x) { labels.add(new Label(text, x + 2, cy + 2, MUTED)); cy += 13; }

    private void note(String text, int x) {
        for (var line : textRenderer.wrapLines(Text.literal(text), pw - 20)) {
            labels.add(new Label(toPlain(line), x + 2, cy, 0xFF6B7085));
            cy += 10;
        }
    }

    private static String toPlain(net.minecraft.text.OrderedText line) {
        StringBuilder sb = new StringBuilder();
        line.accept((index, style, codePoint) -> { sb.appendCodePoint(codePoint); return true; });
        return sb.toString();
    }

    private HWidgets.Toggle toggle(int x, int w, String label, java.util.function.BooleanSupplier get, java.util.function.Consumer<Boolean> set) {
        HWidgets.Toggle t = new HWidgets.Toggle(x, cy, w, 16, label, get, set);
        cy += 19;
        return t;
    }

    private HWidgets.Slider slider(int x, int w, String label, double min, double max, double step,
                                   java.util.function.DoubleSupplier get, java.util.function.DoubleConsumer set,
                                   java.util.function.DoubleFunction<String> fmt) {
        HWidgets.Slider s = new HWidgets.Slider(x, cy, w, 20, label, min, max, step, get, set, fmt);
        cy += 24;
        return s;
    }

    private static String nextMode(String key, String mode) {
        String[] m = HalcyonConfig.MODES.get(key);
        for (int i = 0; i < m.length; i++) if (m[i].equals(mode)) return m[(i + 1) % m.length];
        return m[0];
    }

    private static String cap(String s) { return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int overflow = contentH - (contentBottom - contentTop);
        if (overflow > 0 && mouseX >= px && mouseX <= px + pw) {
            scroll = (int) Math.max(0, Math.min(overflow, scroll - vertical * 16));
            build();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    /** Без затемнения и размытия: HUD за панелью должен быть виден. */
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Открыто из главного меню (мира нет) — рисуем фон Halcyon, иначе оставляем HUD видимым
        if (client != null && client.world == null) MenuBackground.render(ctx, width, height);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float t = Math.min(1f, (Util.getMeasuringTimeMs() - openedAt) / 180f);
        float e = 1f - (1f - t) * (1f - t) * (1f - t);
        boolean anim = t < 1f && cfg().gui.animation;
        if (anim) { ctx.getMatrices().push(); ctx.getMatrices().translate((e - 1f) * 14f, 0, 0); }

        int accent = cfg().accentRgb();
        roundRect(ctx, px, py, pw, ph, 8, 0xE612121A);
        ctx.fillGradient(px + 2, py + 2, px + pw - 2, py + 26, 0x14FFFFFF, 0x00FFFFFF);
        roundBorder(ctx, px, py, pw, ph, 8, argb(accent, 0.40));

        int hx = ctx.drawText(textRenderer, "halcyon", px + 10, py + 10, TEXT, false);
        ctx.drawText(textRenderer, ".", hx, py + 10, 0xFF000000 | accent, false);
        String tag = (client != null ? client.getCurrentFps() : 0) + " FPS · RSHIFT";
        ctx.drawText(textRenderer, tag, px + pw - 10 - textRenderer.getWidth(tag), py + 10, MUTED, false);
        ctx.fill(px + 8, py + 62, px + pw - 8, py + 63, 0x14FFFFFF);
        ctx.fill(px + 8, py + ph - 27, px + pw - 8, py + ph - 26, 0x14FFFFFF);

        for (Label l : labels) {
            if (l.y() >= contentTop - 2 && l.y() + 8 <= contentBottom + 2) ctx.drawText(textRenderer, l.text(), l.x(), l.y(), l.color(), false);
        }
        if (tab == Tab.SWORDS && swordPreviewY >= contentTop - 2 && swordPreviewY + 52 <= contentBottom + 2) {
            // Большое превью выбранного скина: подсветка + меч ×3
            String skin = cfg().swords.skin;
            int bx = px + 8, by = swordPreviewY, bw = pw - 16, bh = 52;
            roundRect(ctx, bx, by, bw, bh, 6, argb(accent, 0.06));
            roundBorder(ctx, bx, by, bw, bh, 6, argb(accent, 0.25));
            roundRect(ctx, bx + 10, by + 4, 44, 44, 22, argb(accent, 0.10));
            ctx.getMatrices().push();
            ctx.getMatrices().translate(bx + 8, by + 2, 0);
            ctx.getMatrices().scale(3f, 3f, 1f);
            HWidgets.drawSwordIcon(ctx, skin, 0, 0);
            ctx.getMatrices().pop();
            SwordSkins.Skin s = SwordSkins.get(skin);
            String name = s == null ? "Ванильный" : s.name();
            ctx.drawText(textRenderer, name, bx + 64, by + 14, TEXT, false);
            ctx.drawText(textRenderer, s == null ? "обычные мечи" : s.animated() ? "анимированный скин" : "скин меча",
                    bx + 64, by + 26, argb(accent, 0.85), false);
        }

        int overflow = contentH - (contentBottom - contentTop);
        if (overflow > 0) {
            int trackH = contentBottom - contentTop, thumbH = Math.max(12, trackH * trackH / (trackH + overflow));
            int thumbY = contentTop + (trackH - thumbH) * scroll / overflow;
            ctx.fill(px + pw - 5, thumbY, px + pw - 3, thumbY + thumbH, argb(accent, 0.6));
        }

        super.render(ctx, mouseX, mouseY, delta);
        if (anim) ctx.getMatrices().pop();

        String hint = "Изменения видны сразу · Esc — закрыть";
        ctx.drawText(textRenderer, hint, width - textRenderer.getWidth(hint) - 8, height - 14, 0xB3E9ECF2, true);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        HalcyonConfig.get().apply();
        HalcyonConfig.save();
        dev.halcyon.visuals.effects.DamageTint.apply();
        dev.halcyon.visuals.hud.Notifications.push("Halcyon", "Настройки сохранены");
        if (client != null) client.setScreen(parent);
    }
}
