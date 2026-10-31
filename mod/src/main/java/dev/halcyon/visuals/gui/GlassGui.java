package dev.halcyon.visuals.gui;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.mixin.HandledScreenAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.screen.slot.Slot;

import static dev.halcyon.visuals.render.Render2D.*;

/**
 * Стеклянное оформление контейнеров: сундуки, шалкеры, раздатчики/выбрасыватели, воронки.
 * У этих экранов фон — только текстура и слоты, поэтому их можно перерисовать целиком без потери информации.
 */
public final class GlassGui {
    private GlassGui() {}

    public static boolean themed(Screen screen) {
        return HalcyonConfig.get().gui.enabled && (screen instanceof GenericContainerScreen
                || screen instanceof ShulkerBoxScreen || screen instanceof Generic3x3ContainerScreen
                || screen instanceof HopperScreen || screen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen
                || screen instanceof net.minecraft.client.gui.screen.ingame.CraftingScreen
                || screen instanceof net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen);
    }

    /** Тёмная ниша под модель игрока в инвентаре. */
    public static void playerArea(DrawContext ctx, int x, int y, int w, int h) {
        roundRect(ctx, x, y, w, h, 5, 0x66000000);
        roundBorder(ctx, x, y, w, h, 5, argb(HalcyonConfig.get().accentRgb(), 0.25));
    }

    /** Подложка под индикатор (огонь, стрелка прогресса). */
    public static void track(DrawContext ctx, int x, int y, int w, int h) {
        roundRect(ctx, x, y, w, h, 3, 0x40000000);
    }

    /** Стрелка «→» между сеткой крафта и результатом. */
    public static void arrow(DrawContext ctx, net.minecraft.client.font.TextRenderer tr, int x, int y) {
        ctx.drawText(tr, "→", x, y, argb(HalcyonConfig.get().accentRgb(), 0.85), false);
    }

    public static void drawBackground(HandledScreen<?> screen, DrawContext ctx) {
        HandledScreenAccessor a = (HandledScreenAccessor) screen;
        int x = a.halcyon$getX(), y = a.halcyon$getY(), w = a.halcyon$getBackgroundWidth(), h = a.halcyon$getBackgroundHeight();
        int accent = HalcyonConfig.get().accentRgb();

        roundRect(ctx, x, y, w, h, 8, 0xD212121A);
        ctx.fillGradient(x + 2, y + 2, x + w - 2, y + 28, 0x16FFFFFF, 0x00FFFFFF);
        roundBorder(ctx, x, y, w, h, 8, argb(accent, 0.45));

        int divY = y + a.halcyon$getPlayerInventoryTitleY() - 4;
        ctx.fill(x + 8, divY, x + w - 8, divY + 1, 0x14FFFFFF);

        for (Slot s : screen.getScreenHandler().slots) {
            int sx = x + s.x - 1, sy = y + s.y - 1;
            roundRect(ctx, sx, sy, 18, 18, 3, 0x5A000000);
            roundBorder(ctx, sx, sy, 18, 18, 3, 0x1FFFFFFF);
        }
    }
}
