package dev.halcyon.visuals.hud;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.config.HalcyonConfig.Element;
import dev.halcyon.visuals.effects.HitEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

import static dev.halcyon.visuals.render.Render2D.argb;

/**
 * Кастомный прицел: крест, точка или кольцо. Динамический разброс — только визуальный:
 * точность и механика стрельбы не меняются.
 */
public final class CrosshairRenderer {
    private CrosshairRenderer() {}

    public static void render(DrawContext ctx, MinecraftClient mc, HalcyonConfig cfg, int sw, int sh) {
        Element e = cfg.element("cross");
        ClientPlayerEntity p = mc.player;
        if (e == null || !e.on || p == null) return;
        if (!mc.options.getPerspective().isFirstPerson() || p.isSpectator()) return;

        HalcyonConfig.Crosshair c = cfg.crosshair;
        int color = 0xFF000000 | cfg.accentRgb();
        int outline = 0x8C000000;

        int gap = c.gap;
        if (c.dynamic) {
            Vec3d v = p.getVelocity();
            gap += (int) Math.min(4, Math.round(Math.sqrt(v.x * v.x + v.z * v.z) * 12));
            if (!p.isOnGround()) gap += 2;
        }
        int len = c.length, t = c.thickness;

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(sw / 2f, sh / 2f, 0);
        m.scale((float) e.size, (float) e.size, 1);

        switch (e.mode) {
            case "dot" -> {
                int d = t + 1, a = -(d - 1) / 2;
                if (c.outline) ctx.fill(a - 1, a - 1, a + d + 1, a + d + 1, outline);
                ctx.fill(a, a, a + d, a + d, color);
            }
            case "ring" -> {
                int r = gap + len / 2 + 2;
                int steps = Math.max(24, r * 10);
                for (int i = 0; i < steps; i++) {
                    double ang = i * Math.PI * 2 / steps;
                    int px = (int) Math.round(Math.cos(ang) * r), py = (int) Math.round(Math.sin(ang) * r);
                    ctx.fill(px, py, px + t, py + t, color);
                }
                ctx.fill(0, 0, 1, 1, color);
            }
            default -> { // plus
                int a = -(t - 1) / 2;
                int[][] arms = {
                        {a, -gap - len, a + t, -gap},
                        {a, 1 + gap, a + t, 1 + gap + len},
                        {-gap - len, a, -gap, a + t},
                        {1 + gap, a, 1 + gap + len, a + t}
                };
                if (c.outline) for (int[] r : arms) ctx.fill(r[0] - 1, r[1] - 1, r[2] + 1, r[3] + 1, outline);
                for (int[] r : arms) ctx.fill(r[0], r[1], r[2], r[3], color);
            }
        }

        // Метка попадания (короткий крестик по диагонали)
        long age = Util.getMeasuringTimeMs() - HitEffects.lastHitMs();
        if (c.hitMarker && age < 220) {
            int mc2 = argb(0xFFFFFF, 1 - age / 220.0);
            for (int k = 4; k <= 7; k++) {
                ctx.fill(k, k, k + 1, k + 1, mc2);
                ctx.fill(-k, k, -k + 1, k + 1, mc2);
                ctx.fill(k, -k, k + 1, -k + 1, mc2);
                ctx.fill(-k, -k, -k + 1, -k + 1, mc2);
            }
        }

        // Индикатор перезарядки атаки (если в настройках игры выбран «под прицелом»)
        float cd = p.getAttackCooldownProgress(0f);
        if (c.cooldown && cd < 1f && mc.options.getAttackIndicator().getValue() == AttackIndicator.CROSSHAIR) {
            int y = 1 + gap + len + 5, w = 16;
            ctx.fill(-w / 2, y, w / 2, y + 2, 0x40FFFFFF);
            ctx.fill(-w / 2, y, -w / 2 + Math.round(w * cd), y + 2, color);
        }
        m.pop();
    }
}
