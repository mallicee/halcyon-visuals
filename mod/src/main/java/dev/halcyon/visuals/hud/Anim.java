package dev.halcyon.visuals.hud;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

/** Плавное приближение значений к цели, не зависящее от частоты кадров. */
public final class Anim {
    private static final class State { float value; long time; }
    private static final Map<String, State> STATES = new HashMap<>();

    private Anim() {}

    private static float step(String key, float target, float start, float speed) {
        long now = Util.getMeasuringTimeMs();
        State s = STATES.get(key);
        if (s == null) { s = new State(); s.value = start; s.time = now; STATES.put(key, s); }
        float dt = Math.min(0.25f, (now - s.time) / 1000f);
        s.time = now;
        s.value += (target - s.value) * (1f - (float) Math.exp(-dt * speed));
        if (Math.abs(target - s.value) < 0.001f) s.value = target;
        return s.value;
    }

    /** Полоски HUD: плавно догоняют реальное значение. */
    public static float smooth(String key, float target) {
        if (!HalcyonConfig.get().smoothBars) return target;
        return step(key, target, target, 10f);
    }

    /** Появление: от 0 до 1 при первом показе. */
    public static float appear(String key) {
        return step(key, 1f, 0f, 9f);
    }

    public static void forget(String key) { STATES.remove(key); }
}
