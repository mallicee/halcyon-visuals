package dev.halcyon.visuals.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;

/** Скриншот без интерфейса: прячем HUD на пару кадров, сохраняем, возвращаем как было. */
public final class ScreenshotMode {
    private static int stage;
    private static boolean wasHidden;

    private ScreenshotMode() {}

    public static void request(MinecraftClient mc) {
        if (stage != 0 || mc.world == null) return;
        wasHidden = mc.options.hudHidden;
        mc.options.hudHidden = true;
        stage = 1;
    }

    public static void tick(MinecraftClient mc) {
        if (stage == 0) return;
        if (stage < 3) { stage++; return; } // ждём, пока кадр без HUD будет отрисован
        ScreenshotRecorder.saveScreenshot(mc.runDirectory, mc.getFramebuffer(),
                msg -> mc.execute(() -> Notifications.push("Скриншот", "Сохранён в папку screenshots")));
        mc.options.hudHidden = wasHidden;
        stage = 0;
    }
}
