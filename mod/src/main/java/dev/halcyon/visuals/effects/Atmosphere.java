package dev.halcyon.visuals.effects;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Цвет неба и тумана в обычном мире + встроенный набор ресурсов погоды. */
public final class Atmosphere {
    public static final String WEATHER_PACK = "halcyon:weather";

    private Atmosphere() {}

    private static int themeColor(HalcyonConfig cfg, boolean fog) {
        return switch (cfg.atmosphere.sky) {
            case "halcyon" -> fog ? 0x7fe8d0 : 0x5fd6e8;
            case "sunset" -> fog ? 0xffb38a : 0xff8f6b;
            case "aurora" -> fog ? 0x8a7fe0 : 0x3fd6b8;
            case "night" -> fog ? 0x5a6ab8 : 0x34489a;
            default -> -1;
        };
    }

    public static boolean active(ClientWorld world) {
        return world != null && world.getRegistryKey() == World.OVERWORLD && !"vanilla".equals(HalcyonConfig.get().atmosphere.sky);
    }

    /**
     * Смешивает ванильный цвет с цветом темы, сохраняя яркость ванильного:
     * ночь остаётся ночью, меняется только оттенок.
     */
    public static float[] tint(float r, float g, float b, boolean fog) {
        HalcyonConfig cfg = HalcyonConfig.get();
        int t = themeColor(cfg, fog);
        if (t < 0) return new float[]{r, g, b};
        float tr = ((t >> 16) & 255) / 255f, tg = ((t >> 8) & 255) / 255f, tb = (t & 255) / 255f;
        float lum = Math.max(r, Math.max(g, b)), tmax = Math.max(tr, Math.max(tg, tb));
        float k = tmax > 0 ? lum / tmax : 0;
        float s = (float) cfg.atmosphere.strength;
        return new float[]{r + (tr * k - r) * s, g + (tg * k - g) * s, b + (tb * k - b) * s};
    }

    public static Vec3d tintSky(Vec3d c) {
        float[] o = tint((float) c.x, (float) c.y, (float) c.z, false);
        return new Vec3d(o[0], o[1], o[2]);
    }

    public static final String GLINT_PACK = "halcyon:glint";

    public static boolean weatherEnabled() { return packEnabled(WEATHER_PACK); }
    public static void setWeather(boolean on) { setPack(WEATHER_PACK, on); }

    public static boolean packEnabled(String id) {
        return MinecraftClient.getInstance().getResourcePackManager().getEnabledIds().contains(id);
    }

    /** Включает или выключает встроенный набор ресурсов Halcyon и перезагружает ресурсы. */
    public static void setPack(String id, boolean on) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ResourcePackManager m = mc.getResourcePackManager();
        if (on) m.enable(id); else m.disable(id);
        mc.options.refreshResourcePacks(m);
    }
}
