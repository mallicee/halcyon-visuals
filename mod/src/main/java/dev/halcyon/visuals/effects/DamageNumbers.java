package dev.halcyon.visuals.effects;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Цифры урона над целью. Показывают только урон от ваших ударов: после вашей атаки мод
 * полсекунды смотрит, насколько уменьшилось здоровье цели, и показывает разницу.
 */
public final class DamageNumbers {
    private static final long LIFE = 900;
    private static final Random RANDOM = Random.create();

    private static final class Watch { float lastHealth; int ticksLeft; boolean crit; }
    private record Num(Vec3d pos, String text, int color, long start) {}

    private static final Map<Integer, Watch> WATCH = new HashMap<>();
    private static final List<Num> NUMS = new ArrayList<>();

    private DamageNumbers() {}

    public static void track(LivingEntity target, boolean crit) {
        HalcyonConfig.Combat c = HalcyonConfig.get().combat;
        if (!c.damageNumbers && !c.killEffect) return;
        Watch w = new Watch();
        w.lastHealth = target.getHealth();
        w.ticksLeft = 10;
        w.crit = crit;
        WATCH.put(target.getId(), w);
    }

    public static void tick(MinecraftClient mc) {
        if (mc.world == null) { WATCH.clear(); NUMS.clear(); return; }
        Iterator<Map.Entry<Integer, Watch>> it = WATCH.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Watch> e = it.next();
            Watch w = e.getValue();
            Entity ent = mc.world.getEntityById(e.getKey());
            if (!(ent instanceof LivingEntity le)) { it.remove(); continue; }
            float h = le.getHealth();
            if (h < w.lastHealth - 0.01f && HalcyonConfig.get().combat.damageNumbers) {
                float dmg = w.lastHealth - h;
                String txt = (w.crit ? "✦ " : "") + (dmg >= 10 ? String.valueOf(Math.round(dmg)) : String.format("%.1f", dmg));
                int color = w.crit ? 0xFFC861 : HalcyonConfig.get().accentRgb();
                Vec3d p = ent.getPos().add((RANDOM.nextDouble() - 0.5) * 0.6, ent.getHeight() + 0.2, (RANDOM.nextDouble() - 0.5) * 0.6);
                synchronized (NUMS) { NUMS.add(new Num(p, txt, color, Util.getMeasuringTimeMs())); }
            }
            w.lastHealth = h;
            if (h <= 0 || le.isDead()) { KillEffect.play(le); it.remove(); continue; }
            if (--w.ticksLeft <= 0) it.remove();
        }
    }

    public static void render(WorldRenderContext ctx) {
        MatrixStack m = ctx.matrixStack();
        VertexConsumerProvider consumers = ctx.consumers();
        if (m == null || consumers == null) return;
        Camera cam = ctx.camera();
        Vec3d cp = cam.getPos();
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        long now = Util.getMeasuringTimeMs();
        synchronized (NUMS) {
            NUMS.removeIf(n -> now - n.start() > LIFE);
            for (Num n : NUMS) {
                float t = (now - n.start()) / (float) LIFE;
                float pop = t < 0.15f ? 1f + (0.15f - t) * 2.5f : 1f;
                float alpha = t < 0.6f ? 1f : 1f - (t - 0.6f) / 0.4f;
                int a = Math.max(8, Math.round(alpha * 255));
                float s = 0.028f * pop;
                m.push();
                m.translate(n.pos().x - cp.x, n.pos().y - cp.y + t * 0.7, n.pos().z - cp.z);
                m.multiply(cam.getRotation());
                m.scale(s, -s, s);
                float x = -tr.getWidth(n.text()) / 2f;
                tr.draw(n.text(), x, 0, (a << 24) | n.color(), true, m.peek().getPositionMatrix(), consumers,
                        TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                m.pop();
            }
        }
    }
}
