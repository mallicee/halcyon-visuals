package dev.halcyon.visuals.hud;

import net.minecraft.util.Util;

import java.util.ArrayDeque;

/** Счётчик кликов в секунду для виджета клавиш. Только подсчёт для отображения. */
public final class InputStats {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();

    private InputStats() {}

    public static void click(int button) {
        long now = Util.getMeasuringTimeMs();
        if (button == 0) LEFT.addLast(now);
        else if (button == 1) RIGHT.addLast(now);
    }

    private static int count(ArrayDeque<Long> q) {
        long cutoff = Util.getMeasuringTimeMs() - 1000;
        while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
        return q.size();
    }

    public static int leftCps() { return count(LEFT); }
    public static int rightCps() { return count(RIGHT); }
}
