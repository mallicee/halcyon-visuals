package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.skins.Capes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Плащ Halcyon на вашем персонаже. Только ваш клиент, другие игроки не видят. */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    private static final Identifier VANILLA_ELYTRA = Identifier.ofVanilla("textures/entity/elytra.png");

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void halcyon$cape(CallbackInfoReturnable<SkinTextures> cir) {
        if ((Object) this != MinecraftClient.getInstance().player) return;
        Identifier cape = Capes.texture(HalcyonConfig.get().cosmetics.cape);
        if (cape == null) return;
        SkinTextures o = cir.getReturnValue();
        // Элитры оставляем своими: иначе игра натянула бы на них текстуру плаща
        Identifier elytra = o.elytraTexture() != null ? o.elytraTexture() : VANILLA_ELYTRA;
        cir.setReturnValue(new SkinTextures(o.texture(), o.textureUrl(), cape, elytra, o.model(), o.secure()));
    }
}
