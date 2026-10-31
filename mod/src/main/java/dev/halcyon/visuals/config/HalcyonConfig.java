package dev.halcyon.visuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.halcyon.visuals.HalcyonClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Конфигурация совместима с экспортом HUD Configurator на сайте Halcyon:
 * {"format":"halcyon-hud","version":2,"config":{...}}.
 * Файл: config/halcyon/hud.json. Меню в игре — правый Shift, перезагрузка файла — H.
 *
 * Позиции в игре хранятся как привязка (anchor) + отступ в пикселях интерфейса: так HUD не «расползается»
 * при другом масштабе интерфейса или соотношении сторон. Проценты x/y с сайта переводятся в привязку при загрузке.
 */
public final class HalcyonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("halcyon").resolve("hud.json");
    public static final Set<String> STYLES_SET = Set.of("glass", "solid", "outline", "minimal");
    public static final String[] STYLES = {"glass", "solid", "outline", "minimal"};
    public static final Set<String> ANCHORS = Set.of("tl", "tc", "tr", "ml", "mc", "mr", "bl", "bc", "br");
    public static final String[] ORDER = {"watermark", "features", "coords", "clock", "fps", "potions", "durability", "keys", "armor", "health", "food", "xp", "hotbar", "cross"};
    /** Версия раскладки по умолчанию. Файлы мода со старой версией получают новую раскладку. */
    private static final int LAYOUT_VERSION = 4;

    /** Номинальные размеры панелей (px интерфейса) для перевода процентов сайта в привязки. */
    private static final Map<String, int[]> NOMINAL = Map.ofEntries(
            Map.entry("health", new int[]{92, 23}), Map.entry("armor", new int[]{92, 23}), Map.entry("food", new int[]{92, 23}),
            Map.entry("xp", new int[]{190, 22}), Map.entry("hotbar", new int[]{204, 28}), Map.entry("coords", new int[]{130, 27}),
            Map.entry("fps", new int[]{56, 27}), Map.entry("keys", new int[]{51, 36}), Map.entry("cross", new int[]{16, 16}),
            Map.entry("potions", new int[]{120, 48}), Map.entry("durability", new int[]{62, 92}), Map.entry("clock", new int[]{80, 17}),
            Map.entry("watermark", new int[]{170, 17}), Map.entry("features", new int[]{80, 70}));

    public static final Map<String, String[]> MODES = new LinkedHashMap<>();
    static {
        MODES.put("health", new String[]{"bar", "hearts", "num"});
        MODES.put("armor", new String[]{"bar", "icons", "num"});
        MODES.put("food", new String[]{"bar", "icons", "num"});
        MODES.put("xp", new String[]{"bar", "level"});
        MODES.put("hotbar", new String[]{"slots", "compact"});
        MODES.put("coords", new String[]{"full", "short"});
        MODES.put("fps", new String[]{"both", "fps"});
        MODES.put("keys", new String[]{"wasd", "mouse"});
        MODES.put("potions", new String[]{"list", "icons"});
        MODES.put("durability", new String[]{"percent", "bars"});
        MODES.put("clock", new String[]{"real", "game"});
        MODES.put("watermark", new String[]{"full", "compact"});
        MODES.put("features", new String[]{"gradient", "accent"});
        MODES.put("cross", new String[]{"plus", "dot", "ring"});
    }

    public static final class Element {
        public boolean on = true;
        public String mode = "";
        public double size = 1;
        /** Проценты экрана — формат сайта. Обновляются при перетаскивании в игре. */
        public double x;
        public double y;
        /** Привязка: t/m/b + l/c/r, например "bc" — снизу по центру. */
        public String anchor;
        /** Отступ от привязки в пикселях интерфейса (внутрь экрана; для центра — смещение центра). */
        public int ox;
        public int oy;

        public Element() {}

        Element(boolean on, String mode, String anchor, int ox, int oy, double x, double y) {
            this.on = on; this.mode = mode; this.anchor = anchor; this.ox = ox; this.oy = oy; this.x = x; this.y = y;
        }
    }

    public static final class Crosshair {
        public int gap = 2;
        public int length = 4;
        public int thickness = 1;
        public boolean outline = true;
        public boolean dynamic = true;
        public boolean hitMarker = true;
        public boolean cooldown = true;
    }

    public static final class Hits {
        public boolean enabled = true;
        public int density = 18;
        public boolean shockwave = true;
        public boolean sparks = true;
    }

    public static final class Gui {
        public boolean enabled = true;
        public boolean animation = true;
        public boolean tooltips = true;
        public boolean glassButtons = true;
        public boolean halcyonMenu = true;
    }

    public static final class Optimization {
        /** Ограничивать FPS, когда окно игры не в фокусе (экономит видеокарту и батарею). */
        public boolean backgroundFps = true;
        public int backgroundLimit = 30;
        /** Огонь на экране ниже, меньше закрывает обзор. */
        public boolean lowFire = false;
        public String activePreset = "none";
        /** Настройки графики игрока до применения пресета — для кнопки «Вернуть». */
        public Map<String, String> backup;
    }

    // Поля совпадают с конфигом сайта
    public String accent = "#3dffa0";
    public String style = "glass";
    public double opacity = 0.55;
    public double radius = 10;
    public double scale = 1;
    public Map<String, Element> el = defaults();

    // Оформление
    public boolean accentLine = true;
    public boolean smoothBars = true;
    public boolean notifications = true;

    // Дополнительные секции мода
    public boolean enabled = true;
    public Crosshair crosshair = new Crosshair();
    public Hits hitEffects = new Hits();
    public Gui gui = new Gui();
    public Optimization optimization = new Optimization();
    public Swords swords = new Swords();

    public static final class Swords {
        /** Выбранный скин мечей, "none" — ванильные мечи. */
        public String skin = "halcyon";
    }

    /** Предмет в руке от первого лица. */
    public static final class Hand {
        public String swing = "smooth"; // vanilla, smooth, spin, chop
        public double scale = 1.0;
        public double x = 0, y = 0, z = 0;
        public boolean sway = true;
    }

    public static final class Cosmetics {
        public String cape = "halcyon";
        public boolean trail = true;
        public boolean ring = false;
        public boolean jumpBurst = true;
        public int density = 2;
    }

    public static final class Outline {
        public boolean enabled = true;
        public String color = "gradient"; // accent, gradient
        public boolean fill = true;
        public double fillAlpha = 0.12;
    }

    public static final class Combat {
        public boolean damageTint = true;
        public boolean damageNumbers = true;
        public boolean killEffect = true;
        public boolean killSound = true;
    }

    /** Небо и туман в обычном мире. */
    public static final class Atmosphere {
        public String sky = "halcyon"; // vanilla, halcyon, sunset, aurora, night
        public double strength = 0.35;
    }

    public static final String[] SKIES = {"vanilla", "halcyon", "sunset", "aurora", "night"};

    /** Визуалы для PvE: подсветка редкого лута, уведомления о подборе, скин удочки. */
    public static final class Loot {
        public boolean rareGlow = true;
        public boolean pickups = true;
        public String rod = "halcyon";
    }

    public Loot loot = new Loot();
    public Atmosphere atmosphere = new Atmosphere();

    public static final String[] SWINGS = {"vanilla", "smooth", "spin", "chop"};

    public Hand hand = new Hand();
    public Cosmetics cosmetics = new Cosmetics();
    public Outline outline = new Outline();
    public Combat combat = new Combat();

    private transient int accentRgb = 0x3dffa0;

    private static HalcyonConfig instance = fresh();

    public static HalcyonConfig get() { return instance; }

    public int accentRgb() { return accentRgb; }

    public Element element(String key) { return el.get(key); }

    public void setAccent(int rgb) {
        accentRgb = rgb & 0xFFFFFF;
        accent = String.format("#%06x", accentRgb);
    }

    /** Ровный блок снизу по центру: хотбар → опыт → здоровье | голод → броня. */
    private static Map<String, Element> defaults() {
        Map<String, Element> m = new LinkedHashMap<>();
        m.put("hotbar", new Element(true, "slots", "bc", 0, 2, 29.5, 87));
        m.put("xp", new Element(true, "bar", "bc", 0, 32, 30.2, 80));
        m.put("health", new Element(true, "bar", "bc", -49, 56, 30.2, 71));
        m.put("food", new Element(true, "bar", "bc", 49, 56, 50.6, 71));
        m.put("armor", new Element(true, "bar", "bc", -49, 81, 30.2, 62));
        m.put("watermark", new Element(true, "full", "tl", 4, 4, 1, 1.5));
        m.put("coords", new Element(true, "full", "tl", 4, 24, 1, 9));
        m.put("features", new Element(true, "gradient", "tr", 4, 4, 82, 1.5));
        m.put("fps", new Element(false, "both", "tr", 4, 4, 87.5, 1.5));
        m.put("keys", new Element(false, "wasd", "mr", 4, 0, 88.5, 43));
        m.put("potions", new Element(true, "list", "ml", 4, 0, 1, 40));
        m.put("durability", new Element(true, "percent", "bl", 4, 4, 1, 64));
        m.put("clock", new Element(false, "real", "tc", 0, 4, 46, 1.5));
        m.put("cross", new Element(true, "plus", "mc", 0, 0, 50, 50));
        return m;
    }

    private static HalcyonConfig fresh() {
        HalcyonConfig c = new HalcyonConfig();
        c.apply();
        return c;
    }

    public static void reset() { instance = fresh(); }

    /** Возвращает элементы на места по умолчанию, не трогая остальные настройки. */
    public void resetLayout() {
        for (Map.Entry<String, Element> d : defaults().entrySet()) {
            Element e = el.get(d.getKey());
            if (e == null) continue;
            Element def = d.getValue();
            e.anchor = def.anchor; e.ox = def.ox; e.oy = def.oy; e.x = def.x; e.y = def.y;
        }
    }

    /** Загружает конфиг. Возвращает текст ошибки или null. */
    public static String load() {
        try {
            if (!Files.exists(FILE)) {
                instance = fresh();
                save();
                return null;
            }
            String raw = Files.readString(FILE, StandardCharsets.UTF_8);
            if (raw.length() > 200_000) throw new IOException("file too large");
            JsonElement root = JsonParser.parseString(raw);
            if (!root.isJsonObject()) throw new IOException("root is not an object");
            JsonObject obj = root.getAsJsonObject();
            JsonObject body = obj.has("config") && obj.get("config").isJsonObject() ? obj.getAsJsonObject("config") : obj;
            HalcyonConfig parsed = GSON.fromJson(body, HalcyonConfig.class);
            if (parsed == null) throw new IOException("empty config");
            parsed.apply();
            // Файл, записанный старой версией мода: раскладка по умолчанию изменилась — ставим новую,
            // остальные настройки (цвета, виды, размеры) сохраняются. Файлы с сайта не трогаем.
            int ver = obj.has("version") && obj.get("version").isJsonPrimitive() ? obj.get("version").getAsInt() : 0;
            if (body.has("crosshair") && ver < LAYOUT_VERSION) {
                parsed.resetLayout();
                parsed.el.get("fps").on = false; // FPS теперь в вотермарке
            }
            instance = parsed;
            return null;
        } catch (Exception e) {
            HalcyonClient.LOG.warn("Failed to read {}: {}", FILE, e.getMessage());
            instance = fresh();
            return e.getClass().getSimpleName();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            JsonObject wrap = new JsonObject();
            wrap.addProperty("format", "halcyon-hud");
            wrap.addProperty("version", LAYOUT_VERSION);
            wrap.add("config", GSON.toJsonTree(instance));
            Files.writeString(FILE, GSON.toJson(wrap), StandardCharsets.UTF_8);
        } catch (IOException e) {
            HalcyonClient.LOG.warn("Failed to write {}: {}", FILE, e.getMessage());
        }
    }

    private static double clamp(double v, double min, double max, double def) {
        return Double.isFinite(v) ? Math.max(min, Math.min(max, v)) : def;
    }

    private static boolean validMode(String key, String mode) {
        for (String m : MODES.get(key)) if (m.equals(mode)) return true;
        return false;
    }

    /** Проверяет значения и пересчитывает служебные поля. Вызывать после любых изменений. */
    public void apply() {
        if (accent == null || !accent.matches("#[0-9a-fA-F]{6}")) accent = "#3dffa0";
        accentRgb = Integer.parseInt(accent.substring(1), 16);
        if (style == null || !STYLES_SET.contains(style)) style = "glass";
        opacity = clamp(opacity, 0.15, 1, 0.55);
        radius = clamp(radius, 0, 18, 10);
        scale = clamp(scale, 0.7, 1.3, 1);

        Map<String, Element> def = defaults();
        Map<String, Element> merged = new LinkedHashMap<>();
        for (Map.Entry<String, Element> d : def.entrySet()) {
            String key = d.getKey();
            Element e = el != null ? el.get(key) : null;
            if (e == null) e = d.getValue();
            if (e.mode == null || !validMode(key, e.mode)) e.mode = d.getValue().mode;
            e.size = clamp(e.size, 0.6, 1.6, 1);
            e.x = clamp(e.x, 0, 100, d.getValue().x);
            e.y = clamp(e.y, 0, 100, d.getValue().y);
            if (key.equals("cross")) { e.anchor = "mc"; e.ox = 0; e.oy = 0; }
            else if (e.anchor == null || !ANCHORS.contains(e.anchor)) fromPercent(key, e);
            e.ox = Math.max(-2000, Math.min(2000, e.ox));
            e.oy = Math.max(-2000, Math.min(2000, e.oy));
            merged.put(key, e);
        }
        el = merged;

        if (crosshair == null) crosshair = new Crosshair();
        crosshair.gap = Math.max(0, Math.min(8, crosshair.gap));
        crosshair.length = Math.max(1, Math.min(10, crosshair.length));
        crosshair.thickness = Math.max(1, Math.min(3, crosshair.thickness));
        if (hitEffects == null) hitEffects = new Hits();
        hitEffects.density = Math.max(0, Math.min(48, hitEffects.density));
        if (gui == null) gui = new Gui();
        if (optimization == null) optimization = new Optimization();
        optimization.backgroundLimit = Math.max(10, Math.min(120, optimization.backgroundLimit));
        if (optimization.activePreset == null) optimization.activePreset = "none";
        if (swords == null) swords = new Swords();
        if (!dev.halcyon.visuals.skins.SwordSkins.valid(swords.skin)) swords.skin = "none";
        if (hand == null) hand = new Hand();
        if (!java.util.Arrays.asList(SWINGS).contains(hand.swing)) hand.swing = "smooth";
        hand.scale = clamp(hand.scale, 0.5, 1.5, 1);
        hand.x = clamp(hand.x, -1, 1, 0); hand.y = clamp(hand.y, -1, 1, 0); hand.z = clamp(hand.z, -1, 1, 0);
        if (cosmetics == null) cosmetics = new Cosmetics();
        if (!dev.halcyon.visuals.skins.Capes.valid(cosmetics.cape)) cosmetics.cape = "none";
        cosmetics.density = Math.max(1, Math.min(5, cosmetics.density));
        if (outline == null) outline = new Outline();
        if (!"accent".equals(outline.color) && !"gradient".equals(outline.color)) outline.color = "gradient";
        outline.fillAlpha = clamp(outline.fillAlpha, 0, 0.5, 0.12);
        if (combat == null) combat = new Combat();
        if (atmosphere == null) atmosphere = new Atmosphere();
        if (!java.util.Arrays.asList(SKIES).contains(atmosphere.sky)) atmosphere.sky = "vanilla";
        atmosphere.strength = clamp(atmosphere.strength, 0.05, 1, 0.35);
        if (loot == null) loot = new Loot();
        if (!dev.halcyon.visuals.skins.RodSkins.valid(loot.rod)) loot.rod = "none";
    }

    /** Перевод процентов (сайт) в привязку для эталонного экрана 480×270. */
    private static void fromPercent(String key, Element e) {
        final int W = 480, H = 270;
        int[] n = NOMINAL.get(key);
        double left = e.x / 100 * W, top = e.y / 100 * H;
        double cx = left + n[0] / 2.0, cy = top + n[1] / 2.0;
        char h = cx < W / 3.0 ? 'l' : cx > W * 2 / 3.0 ? 'r' : 'c';
        char v = cy < H / 3.0 ? 't' : cy > H * 2 / 3.0 ? 'b' : 'm';
        e.anchor = "" + (v == 't' ? 't' : v == 'm' ? 'm' : 'b') + h;
        e.ox = (int) Math.round(h == 'l' ? left : h == 'r' ? W - left - n[0] : cx - W / 2.0);
        e.oy = (int) Math.round(v == 't' ? top : v == 'b' ? H - top - n[1] : cy - H / 2.0);
    }

    /** Ставит элемент так, чтобы его левый верхний угол оказался в (x, y); привязка выбирается по положению. */
    public static void placeAt(Element e, int x, int y, int w, int h, int sw, int sh) {
        double cx = x + w / 2.0, cy = y + h / 2.0;
        char hz = Math.abs(cx - sw / 2.0) <= 6 ? 'c' : cx < sw / 3.0 ? 'l' : cx > sw * 2 / 3.0 ? 'r' : 'c';
        char vt = cy < sh / 3.0 ? 't' : cy > sh * 2 / 3.0 ? 'b' : 'm';
        if (hz == 'c' && Math.abs(cx - sw / 2.0) <= 6) cx = sw / 2.0; // прилипание к центру
        e.anchor = "" + vt + hz;
        e.ox = (int) Math.round(hz == 'l' ? x : hz == 'r' ? sw - x - w : cx - sw / 2.0);
        e.oy = (int) Math.round(vt == 't' ? y : vt == 'b' ? sh - y - h : cy - sh / 2.0);
        e.x = Math.max(0, Math.min(100, x * 100.0 / sw));
        e.y = Math.max(0, Math.min(100, y * 100.0 / sh));
    }

    /** Левый верхний угол панели по привязке. */
    public static int[] resolve(Element e, int w, int h, int sw, int sh) {
        char v = e.anchor.charAt(0), hz = e.anchor.charAt(1);
        int x = hz == 'l' ? e.ox : hz == 'r' ? sw - e.ox - w : Math.round(sw / 2f + e.ox - w / 2f);
        int y = v == 't' ? e.oy : v == 'b' ? sh - e.oy - h : Math.round(sh / 2f + e.oy - h / 2f);
        x = Math.max(0, Math.min(x, sw - w));
        y = Math.max(0, Math.min(y, sh - h));
        return new int[]{x, y};
    }
}
