package dev.halcyon.visuals.effects;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Эффекты попаданий: неоновая вспышка частиц, искры и расходящаяся волна.
 * Только клиентские частицы, видимые вам. Атаку не изменяет и не отменяет (всегда PASS).
 */
public final class HitEffects {
    private static long lastHitMs = Long.MIN_VALUE / 2;
    private static final List<Wave> WAVES = new ArrayList<>();
    private static final Random RANDOM = Random.create();

    private static final class Wave {
        final double x, y, z;
        final Vector3f color;
        int age;

        Wave(double x, double y, double z, Vector3f color) { this.x = x; this.y = y; this.z = z; this.color = color; }
    }

    private HitEffects() {}

    public static long lastHitMs() { return lastHitMs; }

    private static Vector3f rgb(int c) {
        return new Vector3f(((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f);
    }

    public static ActionResult onAttack(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hit) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!world.isClient || player != mc.player) return ActionResult.PASS;
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return ActionResult.PASS;

        lastHitMs = Util.getMeasuringTimeMs();
        HalcyonConfig cfg = HalcyonConfig.get();
        HalcyonConfig.Hits h = cfg.hitEffects;
        boolean crit = player.fallDistance > 0 && !player.isOnGround() && !player.isClimbing()
                && !player.isTouchingWater() && player.getAttackCooldownProgress(0.5f) > 0.9f;
        DamageNumbers.track(living, crit);
        if (!h.enabled) return ActionResult.PASS;

        Vec3d c = entity.getPos().add(0, entity.getHeight() * 0.6, 0);
        if (hit != null && hit.getEntity() == entity) c = hit.getPos();
        else if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() == entity) c = ehr.getPos();

        Vector3f main = rgb(cfg.accentRgb());
        Vector3f alt = crit ? rgb(0x20D6C8) : new Vector3f(1f, 1f, 1f);
        int count = crit ? h.density * 2 : h.density;

        for (int i = 0; i < count; i++) {
            Vec3d d = randomDir();
            double r = 0.12 + RANDOM.nextDouble() * 0.38;
            Vector3f col = i % 4 == 0 ? alt : main;
            world.addParticle(new DustParticleEffect(col, 0.55f + RANDOM.nextFloat() * 0.6f),
                    c.x + d.x * r, c.y + d.y * r, c.z + d.z * r, d.x * 0.04, d.y * 0.04, d.z * 0.04);
        }
        if (h.sparks) {
            for (int i = 0; i < Math.max(2, count / 4); i++) {
                Vec3d d = randomDir();
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, d.x * 0.35, d.y * 0.35, d.z * 0.35);
            }
        }
        if (h.shockwave) {
            synchronized (WAVES) { WAVES.add(new Wave(entity.getX(), entity.getY() + 0.08, entity.getZ(), crit ? alt : main)); }
        }
        return ActionResult.PASS;
    }

    private static Vec3d randomDir() {
        double u = RANDOM.nextDouble() * 2 - 1, th = RANDOM.nextDouble() * Math.PI * 2, s = Math.sqrt(1 - u * u);
        return new Vec3d(s * Math.cos(th), u, s * Math.sin(th));
    }

    /** Волна расходится кольцом за 6 тиков. */
    public static void tick(MinecraftClient mc) {
        synchronized (WAVES) {
            if (mc.world == null || mc.isPaused()) { if (mc.world == null) WAVES.clear(); return; }
            Iterator<Wave> it = WAVES.iterator();
            while (it.hasNext()) {
                Wave w = it.next();
                double radius = 0.35 + w.age * 0.22;
                int points = (int) Math.round(radius * 22);
                float size = 0.7f - w.age * 0.08f;
                for (int i = 0; i < points; i++) {
                    double a = i * Math.PI * 2 / points;
                    mc.world.addParticle(new DustParticleEffect(w.color, Math.max(0.25f, size)),
                            w.x + Math.cos(a) * radius, w.y, w.z + Math.sin(a) * radius, 0, 0, 0);
                }
                if (++w.age > 5) it.remove();
            }
        }
    }
}
