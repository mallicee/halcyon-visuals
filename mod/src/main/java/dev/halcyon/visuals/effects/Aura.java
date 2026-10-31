package dev.halcyon.visuals.effects;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

/** Аура вокруг вашего персонажа: след при беге, кольцо под ногами, вспышка при прыжке. Частицы видите только вы. */
public final class Aura {
    private static final Random RANDOM = Random.create();
    private static boolean wasOnGround = true;
    private static int tick;

    private Aura() {}

    private static Vector3f rgb(int c) {
        return new Vector3f(((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f);
    }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || mc.isPaused() || p.isSpectator() || p.isInvisible()) return;
        HalcyonConfig cfg = HalcyonConfig.get();
        HalcyonConfig.Cosmetics c = cfg.cosmetics;
        tick++;
        Vector3f a = rgb(cfg.accentRgb()), b = rgb(0x20D6C8);
        Vec3d v = p.getVelocity();
        double speed = Math.sqrt(v.x * v.x + v.z * v.z);

        if (c.trail && speed > 0.08) {
            for (int i = 0; i < c.density; i++) {
                double ox = (RANDOM.nextDouble() - 0.5) * 0.4, oz = (RANDOM.nextDouble() - 0.5) * 0.4;
                mc.world.addParticle(new DustParticleEffect(i % 2 == 0 ? a : b, 0.6f + RANDOM.nextFloat() * 0.4f),
                        p.getX() - v.x * 2 + ox, p.getY() + 0.1 + RANDOM.nextDouble() * 0.3, p.getZ() - v.z * 2 + oz, 0, 0, 0);
            }
        }

        if (c.ring && tick % 3 == 0 && p.isOnGround()) {
            int points = 4 + c.density * 2;
            double base = tick * 0.08;
            for (int i = 0; i < points; i++) {
                double ang = base + i * Math.PI * 2 / points;
                mc.world.addParticle(new DustParticleEffect(i % 2 == 0 ? a : b, 0.5f),
                        p.getX() + Math.cos(ang) * 0.65, p.getY() + 0.05, p.getZ() + Math.sin(ang) * 0.65, 0, 0, 0);
            }
        }

        boolean onGround = p.isOnGround();
        if (c.jumpBurst && wasOnGround && !onGround && v.y > 0.2) {
            for (int i = 0; i < 18; i++) {
                double ang = i * Math.PI * 2 / 18;
                mc.world.addParticle(new DustParticleEffect(i % 3 == 0 ? b : a, 0.8f),
                        p.getX() + Math.cos(ang) * 0.45, p.getY() + 0.05, p.getZ() + Math.sin(ang) * 0.45, 0, 0, 0);
            }
        }
        wasOnGround = onGround;
    }
}
