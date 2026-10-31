package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.gui.GlassGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Стеклянный верстак. */
@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin {
    @Redirect(method = "drawBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
    private void halcyon$glass(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int w, int h) {
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        if (!GlassGui.themed(self)) { ctx.drawTexture(tex, x, y, u, v, w, h); return; }
        GlassGui.drawBackground(self, ctx);
        GlassGui.arrow(ctx, MinecraftClient.getInstance().textRenderer, x + 97, y + 39);
    }
}
