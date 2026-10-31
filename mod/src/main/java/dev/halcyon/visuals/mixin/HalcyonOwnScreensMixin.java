package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.gui.MenuBackground;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Экраны «Загрузка мира…» и «Сохранение мира…» со своим renderBackground. */
@Mixin({DownloadingTerrainScreen.class, MessageScreen.class})
public abstract class HalcyonOwnScreensMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void halcyon$bg(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!HalcyonConfig.get().gui.halcyonMenu) return;
        Screen self = (Screen) (Object) this;
        MenuBackground.render(ctx, self.width, self.height);
        ci.cancel();
    }
}
