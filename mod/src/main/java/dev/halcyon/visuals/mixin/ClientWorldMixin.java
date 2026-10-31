package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.effects.Atmosphere;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Оттенок неба по выбранной теме. */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void halcyon$sky(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (Atmosphere.active((ClientWorld) (Object) this)) cir.setReturnValue(Atmosphere.tintSky(cir.getReturnValue()));
    }
}
