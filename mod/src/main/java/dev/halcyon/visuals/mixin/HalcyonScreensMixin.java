package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.gui.MenuBackground;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Фон Halcyon на экранах загрузки мира, подключения к серверу и прогресса. */
@Mixin(Screen.class)
public abstract class HalcyonScreensMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void halcyon$loading(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!HalcyonConfig.get().gui.halcyonMenu) return;
        if (self instanceof LevelLoadingScreen || self instanceof ConnectScreen || self instanceof ProgressScreen) {
            MenuBackground.render(ctx, self.width, self.height);
            ci.cancel();
        }
    }
}
