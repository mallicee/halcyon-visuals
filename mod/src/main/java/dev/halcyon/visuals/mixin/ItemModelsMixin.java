package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.skins.SwordSkins;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Подменяет модель любого меча на выбранный скин Halcyon (только отображение у вас). */
@Mixin(ItemModels.class)
public abstract class ItemModelsMixin {
    @Shadow public abstract BakedModelManager getModelManager();

    @Inject(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At("HEAD"), cancellable = true)
    private void halcyon$swordSkin(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
        net.minecraft.util.Identifier id = null;
        if (stack.getItem() instanceof SwordItem) {
            String skin = HalcyonConfig.get().swords.skin;
            if (!SwordSkins.NONE.equals(skin)) id = SwordSkins.model(skin);
        } else if (stack.getItem() instanceof net.minecraft.item.FishingRodItem) {
            // Модель удочки с переопределением «cast»: игра сама переключит её при забросе
            String rod = HalcyonConfig.get().loot.rod;
            if (!dev.halcyon.visuals.skins.RodSkins.NONE.equals(rod)) id = dev.halcyon.visuals.skins.RodSkins.model(rod);
        }
        if (id == null) return;
        BakedModelManager mgr = getModelManager();
        BakedModel model = mgr.getModel(id);
        if (model != null && model != mgr.getMissingModel()) cir.setReturnValue(model);
    }
}
