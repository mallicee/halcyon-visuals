package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.gui.GlassGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Стеклянный инвентарь игрока: заменяем только текстуру фона, модель игрока рисуется как обычно. */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    protected InventoryScreenMixin(PlayerScreenHandler h, PlayerInventory inv, Text title) { super(h, inv, title); }

    @Redirect(method = "drawBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
    private void halcyon$glass(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int w, int h) {
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        if (!GlassGui.themed(self)) { ctx.drawTexture(tex, x, y, u, v, w, h); return; }
        GlassGui.drawBackground(self, ctx);
        GlassGui.playerArea(ctx, x + 26, y + 8, 49, 70);
        GlassGui.arrow(ctx, this.textRenderer, x + 137, y + 32);
    }

    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void halcyon$title(DrawContext ctx, int mouseX, int mouseY, CallbackInfo ci) {
        if (!GlassGui.themed(this)) return;
        ctx.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0xFFE9ECF2, false);
        ci.cancel();
    }
}
