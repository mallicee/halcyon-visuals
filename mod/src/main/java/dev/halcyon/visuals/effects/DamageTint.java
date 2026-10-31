package dev.halcyon.visuals.effects;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

/**
 * Цвет вспышки урона у существ. Игра хранит его в маленькой текстуре наложения (верхние 8 строк);
 * мы перекрашиваем эти строки в акцентный цвет, при выключении — возвращаем ванильный красный.
 */
public final class DamageTint {
    private static final int VANILLA_ABGR = -1308622593; // ванильный красный с прозрачностью 0xB2
    private static NativeImageBackedTexture texture;

    private DamageTint() {}

    public static void register(NativeImageBackedTexture overlay) {
        texture = overlay;
        apply();
    }

    public static void apply() {
        if (texture == null) return;
        NativeImage img = texture.getImage();
        if (img == null) return;
        HalcyonConfig cfg = HalcyonConfig.get();
        int abgr = VANILLA_ABGR;
        if (cfg.combat.damageTint) {
            int rgb = cfg.accentRgb();
            int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
            abgr = (0x9E << 24) | (b << 16) | (g << 8) | r;
        }
        for (int y = 0; y < 8; y++) for (int x = 0; x < 16; x++) img.setColor(x, y, abgr);
        texture.upload();
    }
}
