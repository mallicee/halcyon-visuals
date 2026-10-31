package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.gui.GlassGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Стеклянные печь, коптильня и плавильная печь. Огонь и стрелка прогресса рисуются игрой поверх. */
@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin {
    @Redirect(method = "drawBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
    private void halcyon$glass(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int w, int h) {
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        if (!GlassGui.themed(self)) { ctx.drawTexture(tex, x, y, u, v, w, h); return; }
        GlassGui.drawBackground(self, ctx);
        GlassGui.track(ctx, x + 79, y + 34, 24, 17);   // дорожка стрелки прогресса
        GlassGui.track(ctx, x + 56, y + 36, 14, 14);   // место для огня
        GlassGui.arrow(ctx, MinecraftClient.getInstance().textRenderer, x + 87, y + 39);
    }
}
