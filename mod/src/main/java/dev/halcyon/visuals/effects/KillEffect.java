package dev.halcyon.visuals.effects;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

/** Эффект убийства цели вашим ударом: спираль света вверх, искры и мягкий звон. Видите и слышите только вы. */
public final class KillEffect {
    private static final Random RANDOM = Random.create();

    private KillEffect() {}

    public static void play(LivingEntity e) {
        MinecraftClient mc = MinecraftClient.getInstance();
        HalcyonConfig cfg = HalcyonConfig.get();
        if (mc.world == null || !cfg.combat.killEffect) return;
        int c = cfg.accentRgb();
        Vector3f a = new Vector3f(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f);
        Vector3f b = new Vector3f(0x20 / 255f, 0xd6 / 255f, 0xc8 / 255f);
        double h = Math.max(1.0, e.getHeight() + 0.6);
        for (int i = 0; i < 40; i++) { // двойная спираль
            double t = i / 40.0, ang = t * Math.PI * 6;
            for (int s = 0; s < 2; s++) {
                double aa = ang + s * Math.PI;
                mc.world.addParticle(new DustParticleEffect(s == 0 ? a : b, 0.9f),
                        e.getX() + Math.cos(aa) * 0.55, e.getY() + t * h, e.getZ() + Math.sin(aa) * 0.55, 0, 0.02, 0);
            }
        }
        for (int i = 0; i < 14; i++) {
            mc.world.addParticle(ParticleTypes.END_ROD, e.getX(), e.getY() + e.getHeight() * 0.6, e.getZ(),
                    (RANDOM.nextDouble() - 0.5) * 0.25, RANDOM.nextDouble() * 0.2, (RANDOM.nextDouble() - 0.5) * 0.25);
        }
        if (cfg.combat.killSound) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.35f, 0.9f));
        }
    }
}
