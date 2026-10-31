package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ограничение FPS, пока окно игры не в фокусе: видеокарта не работает впустую. */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
    private void halcyon$backgroundFps(CallbackInfoReturnable<Integer> cir) {
        HalcyonConfig.Optimization o = HalcyonConfig.get().optimization;
        MinecraftClient self = (MinecraftClient) (Object) this;
        if (o.backgroundFps && !self.isWindowFocused()) {
            cir.setReturnValue(Math.min(cir.getReturnValue(), o.backgroundLimit));
        }
    }
}
