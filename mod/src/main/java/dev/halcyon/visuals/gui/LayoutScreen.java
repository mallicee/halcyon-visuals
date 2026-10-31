package dev.halcyon.visuals.gui;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.config.HalcyonConfig.Element;
import dev.halcyon.visuals.hud.HudRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

import static dev.halcyon.visuals.render.Render2D.*;

/**
 * Редактор расположения: элементы HUD перетаскиваются мышью прямо по экрану.
 * ПКМ по элементу — выровнять по центру, R — вернуть раскладку по умолчанию.
 */
public final class LayoutScreen extends Screen {
    private final Screen parent;
    private String drag;
    private int grabX, grabY, dragW, dragH;
    private boolean snappedCenter;

    public LayoutScreen(Screen parent) {
        super(Text.literal("Halcyon Layout"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x22000000);
    }

    private String hit(double mx, double my) {
        String found = null;
        for (Map.Entry<String, int[]> e : HudRenderer.rects().entrySet()) {
            int[] r = e.getValue();
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) found = e.getKey();
        }
        return found;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int accent = HalcyonConfig.get().accentRgb();
        String hover = drag != null ? drag : hit(mouseX, mouseY);

        if (drag != null && snappedCenter) ctx.fill(width / 2, 0, width / 2 + 1, height, argb(accent, 0.5));

        for (Map.Entry<String, int[]> e : HudRenderer.rects().entrySet()) {
            int[] r = e.getValue();
            boolean on = e.getKey().equals(hover);
            if (on) roundRect(ctx, r[0] - 2, r[1] - 2, r[2] + 4, r[3] + 4, 4, argb(accent, 0.12));
            roundBorder(ctx, r[0] - 2, r[1] - 2, r[2] + 4, r[3] + 4, 4, on ? (0xFF000000 | accent) : argb(accent, 0.45));
        }

        String hint = "Перетаскивайте элементы · ПКМ — по центру · R — сбросить · Esc — назад";
        int hw = textRenderer.getWidth(hint) + 16, hx = (width - hw) / 2, hy = height / 2 - 40;
        roundRect(ctx, hx, hy, hw, 16, 6, 0xD912121A);
        roundBorder(ctx, hx, hy, hw, 16, 6, argb(accent, 0.45));
        ctx.drawText(textRenderer, hint, hx + 8, hy + 4, TEXT, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        String key = hit(mx, my);
        if (key == null) return super.mouseClicked(mx, my, button);
        int[] r = HudRenderer.rects().get(key);
        Element e = HalcyonConfig.get().element(key);
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            HalcyonConfig.placeAt(e, (width - r[2]) / 2, r[1], r[2], r[3], width, height);
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            drag = key;
            grabX = (int) mx - r[0]; grabY = (int) my - r[1];
            dragW = r[2]; dragH = r[3];
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (drag == null) return super.mouseDragged(mx, my, button, dx, dy);
        int x = Math.max(0, Math.min(width - dragW, (int) mx - grabX));
        int y = Math.max(0, Math.min(height - dragH, (int) my - grabY));
        snappedCenter = Math.abs(x + dragW / 2 - width / 2) <= 6;
        HalcyonConfig.placeAt(HalcyonConfig.get().element(drag), x, y, dragW, dragH, width, height);
        return true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        drag = null; snappedCenter = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            HalcyonConfig.get().resetLayout();
            dev.halcyon.visuals.hud.Notifications.push("Расположение", "Возвращена раскладка по умолчанию");
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        HalcyonConfig.save();
        if (client != null) client.setScreen(parent);
    }
}
