package dev.halcyon.visuals.mixin;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Анимации предмета в руке от первого лица: стиль взмаха, размер и положение, покачивание при ходьбе.
 * Только отображение: скорость и механика атаки не меняются.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void halcyon$begin(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress,
                               ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        matrices.push();
        HalcyonConfig.Hand h = HalcyonConfig.get().hand;
        if (h.sway) {
            Vec3d v = player.getVelocity();
            float amp = (float) Math.min(1.0, Math.sqrt(v.x * v.x + v.z * v.z) * 8.0);
            float dist = player.horizontalSpeed + (player.horizontalSpeed - player.prevHorizontalSpeed) * tickDelta;
            float phase = dist * (float) Math.PI;
            matrices.translate(MathHelper.sin(phase) * 0.022f * amp, -Math.abs(MathHelper.cos(phase)) * 0.016f * amp, 0f);
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At("TAIL"))
    private void halcyon$end(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress,
                             ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        matrices.pop();
    }

    /** После того как игра поставила руку на место — сдвиг и масштаб предмета вокруг кисти. */
    @Inject(method = "applyEquipOffset", at = @At("TAIL"))
    private void halcyon$offset(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        HalcyonConfig.Hand h = HalcyonConfig.get().hand;
        int side = arm == Arm.RIGHT ? 1 : -1;
        if (h.x != 0 || h.y != 0 || h.z != 0) matrices.translate(side * h.x * 0.3, h.y * 0.3, h.z * 0.3);
        if (h.scale != 1.0) {
            float s = (float) h.scale;
            matrices.scale(s, s, s);
        }
    }

    /** Свои стили взмаха вместо ванильного. */
    @Inject(method = "applySwingOffset", at = @At("HEAD"), cancellable = true)
    private void halcyon$swing(MatrixStack matrices, Arm arm, float swingProgress, CallbackInfo ci) {
        String style = HalcyonConfig.get().hand.swing;
        if (style.equals("vanilla")) return;
        int i = arm == Arm.RIGHT ? 1 : -1;
        float p = swingProgress;
        switch (style) {
            case "smooth" -> { // плавная кривая без рывка в начале
                float e = MathHelper.sin(p * (float) Math.PI * 0.5f);
                float f = MathHelper.sin(e * (float) Math.PI);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * (45f + f * -25f)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * f * -18f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f * -65f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * -45f));
            }
            case "spin" -> { // замах «с разворота»: широкая дуга по горизонтали
                float f = MathHelper.sin(MathHelper.sqrt(p) * (float) Math.PI);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * (45f + f * -75f)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * f * -35f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f * -40f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * -45f));
            }
            case "chop" -> { // рубящий удар сверху вниз
                float f = MathHelper.sin(MathHelper.sqrt(p) * (float) Math.PI);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * 45f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * f * -8f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f * -115f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * -45f));
            }
            default -> { return; }
        }
        ci.cancel();
    }
}
