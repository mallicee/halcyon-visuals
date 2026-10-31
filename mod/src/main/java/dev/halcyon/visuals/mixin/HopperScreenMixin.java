package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.gui.GlassGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperScreen.class)
public abstract class HopperScreenMixin {
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void halcyon$glass(DrawContext ctx, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        if (!GlassGui.themed(self)) return;
        GlassGui.drawBackground(self, ctx);
        ci.cancel();
    }
}
