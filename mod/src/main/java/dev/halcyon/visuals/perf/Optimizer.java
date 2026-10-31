package dev.halcyon.visuals.perf;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.option.SimpleOption;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * «Буст FPS»: пресеты настроек графики игры. Перед первым применением сохраняет настройки игрока,
 * кнопка «Вернуть мои настройки» восстанавливает их полностью.
 * Самый большой прирост дают моды оптимизации (Sodium, Lithium, FerriteCore) — их ставит Halcyon Loader.
 */
public final class Optimizer {
    public enum Preset {
        MAX("Максимум FPS"), BALANCE("Баланс");
        public final String title;
        Preset(String title) { this.title = title; }
    }

    public record ModStatus(String name, boolean loaded, String what) {}

    private Optimizer() {}

    public static ModStatus[] mods() {
        FabricLoader fl = FabricLoader.getInstance();
        return new ModStatus[]{
                new ModStatus("Sodium", fl.isModLoaded("sodium"), "рендер, главный прирост FPS"),
                new ModStatus("Lithium", fl.isModLoaded("lithium"), "игровая логика и физика"),
                new ModStatus("FerriteCore", fl.isModLoaded("ferritecore"), "меньше расход памяти")};
    }

    private static Map<String, String> snapshot(GameOptions o) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("view", String.valueOf(o.getViewDistance().getValue()));
        m.put("sim", String.valueOf(o.getSimulationDistance().getValue()));
        m.put("graphics", o.getGraphicsMode().getValue().name());
        m.put("ao", String.valueOf(o.getAo().getValue()));
        m.put("clouds", o.getCloudRenderMode().getValue().name());
        m.put("particles", o.getParticles().getValue().name());
        m.put("shadows", String.valueOf(o.getEntityShadows().getValue()));
        m.put("biome", String.valueOf(o.getBiomeBlendRadius().getValue()));
        m.put("entityDist", String.valueOf(o.getEntityDistanceScaling().getValue()));
        m.put("maxFps", String.valueOf(o.getMaxFps().getValue()));
        m.put("vsync", String.valueOf(o.getEnableVsync().getValue()));
        return m;
    }

    private static <T> void set(SimpleOption<T> opt, Map<String, String> m, String key, Function<String, T> parse) {
        String raw = m.get(key);
        if (raw == null) return;
        try { opt.setValue(parse.apply(raw)); } catch (RuntimeException ignored) { }
    }

    private static void restoreFrom(GameOptions o, Map<String, String> m) {
        set(o.getViewDistance(), m, "view", Integer::valueOf);
        set(o.getSimulationDistance(), m, "sim", Integer::valueOf);
        set(o.getGraphicsMode(), m, "graphics", GraphicsMode::valueOf);
        set(o.getAo(), m, "ao", Boolean::valueOf);
        set(o.getCloudRenderMode(), m, "clouds", CloudRenderMode::valueOf);
        set(o.getParticles(), m, "particles", ParticlesMode::valueOf);
        set(o.getEntityShadows(), m, "shadows", Boolean::valueOf);
        set(o.getBiomeBlendRadius(), m, "biome", Integer::valueOf);
        set(o.getEntityDistanceScaling(), m, "entityDist", Double::valueOf);
        set(o.getMaxFps(), m, "maxFps", Integer::valueOf);
        set(o.getEnableVsync(), m, "vsync", Boolean::valueOf);
    }

    public static void apply(Preset preset) {
        MinecraftClient mc = MinecraftClient.getInstance();
        GameOptions o = mc.options;
        HalcyonConfig cfg = HalcyonConfig.get();
        if (cfg.optimization.backup == null) cfg.optimization.backup = snapshot(o);

        Map<String, String> v = new LinkedHashMap<>();
        if (preset == Preset.MAX) {
            v.put("view", "8"); v.put("sim", "6"); v.put("graphics", "FAST"); v.put("ao", "false");
            v.put("clouds", "OFF"); v.put("particles", "MINIMAL"); v.put("shadows", "false"); v.put("biome", "0");
            v.put("entityDist", "0.75"); v.put("maxFps", "260"); v.put("vsync", "false");
            cfg.hitEffects.density = Math.min(cfg.hitEffects.density, 10);
        } else {
            v.put("view", "12"); v.put("sim", "8"); v.put("graphics", "FANCY"); v.put("ao", "true");
            v.put("clouds", "FAST"); v.put("particles", "DECREASED"); v.put("shadows", "true"); v.put("biome", "2");
            v.put("entityDist", "1.0"); v.put("maxFps", "260"); v.put("vsync", "false");
        }
        restoreFrom(o, v);
        finish(mc);
        cfg.optimization.activePreset = preset.name();
        HalcyonConfig.save();
        dev.halcyon.visuals.hud.Notifications.push("Буст FPS", "Применён пресет «" + preset.title + "»");
    }

    /** Возвращает настройки игрока. false — если возвращать нечего. */
    public static boolean restore() {
        HalcyonConfig cfg = HalcyonConfig.get();
        if (cfg.optimization.backup == null) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        restoreFrom(mc.options, cfg.optimization.backup);
        finish(mc);
        cfg.optimization.backup = null;
        cfg.optimization.activePreset = "none";
        HalcyonConfig.save();
        dev.halcyon.visuals.hud.Notifications.push("Буст FPS", "Ваши настройки графики возвращены");
        return true;
    }

    private static void finish(MinecraftClient mc) {
        mc.options.write();
        if (mc.worldRenderer != null) mc.worldRenderer.reload();
    }

    public static String activeTitle() {
        String a = HalcyonConfig.get().optimization.activePreset;
        for (Preset p : Preset.values()) if (p.name().equals(a)) return p.title;
        return "ваши настройки";
    }
}
