package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.gui.GlassGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Анимация открытия всех окон инвентаря и светлые заголовки для стеклянных контейнеров. */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected int titleX;
    @Shadow protected int titleY;
    @Shadow protected int playerInventoryTitleX;
    @Shadow protected int playerInventoryTitleY;
    @Shadow @Final protected Text playerInventoryTitle;

    @Unique private long halcyon$openedAt;
    @Unique private boolean halcyon$pushed;

    protected HandledScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void halcyon$init(CallbackInfo ci) {
        halcyon$openedAt = Util.getMeasuringTimeMs();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void halcyon$renderHead(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        halcyon$pushed = false;
        HalcyonConfig.Gui gui = HalcyonConfig.get().gui;
        if (!gui.animation) return;
        float t = (Util.getMeasuringTimeMs() - halcyon$openedAt) / 170f;
        if (t >= 1f || t < 0f) return;
        float eased = 1f - (1f - t) * (1f - t) * (1f - t);
        float s = 0.94f + 0.06f * eased;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(this.width / 2f, this.height / 2f + (1f - eased) * 6f, 0);
        ctx.getMatrices().scale(s, s, 1f);
        ctx.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);
        halcyon$pushed = true;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void halcyon$renderTail(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (halcyon$pushed) {
            ctx.getMatrices().pop();
            halcyon$pushed = false;
        }
    }

    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void halcyon$drawForeground(DrawContext ctx, int mouseX, int mouseY, CallbackInfo ci) {
        if (!GlassGui.themed(this)) return;
        ctx.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0xFFE9ECF2, false);
        ctx.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0xFF8B90A5, false);
        ci.cancel();
    }
}
