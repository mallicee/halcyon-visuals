package dev.halcyon.visuals.effects;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

import java.util.Set;

/**
 * Искры и луч света над редкими предметами на земле. Обычные частицы с проверкой глубины:
 * видны только в прямой видимости, сквозь стены не просвечивают.
 */
public final class LootGlow {
    private static final Random RANDOM = Random.create();
    private static final Set<Item> VALUABLE = Set.of(Items.DIAMOND, Items.DIAMOND_BLOCK, Items.EMERALD, Items.EMERALD_BLOCK,
            Items.NETHERITE_INGOT, Items.NETHERITE_SCRAP, Items.NETHERITE_BLOCK, Items.ANCIENT_DEBRIS, Items.GOLD_BLOCK,
            Items.TOTEM_OF_UNDYING, Items.ENCHANTED_GOLDEN_APPLE, Items.NETHER_STAR, Items.ELYTRA, Items.TRIDENT, Items.HEART_OF_THE_SEA);
    private static int tick;

    private LootGlow() {}

    /** Цвет подсветки или -1, если предмет обычный. */
    public static int colorOf(ItemStack s) {
        Rarity r = s.getRarity();
        if (r != Rarity.COMMON) {
            Integer c = r.getFormatting().getColorValue();
            return c != null ? c : HalcyonConfig.get().accentRgb();
        }
        if (s.hasGlint()) return 0xb18cff;
        if (VALUABLE.contains(s.getItem())) {
            if (s.isOf(Items.DIAMOND) || s.isOf(Items.DIAMOND_BLOCK)) return Formatting.AQUA.getColorValue();
            if (s.isOf(Items.EMERALD) || s.isOf(Items.EMERALD_BLOCK)) return 0x3dffa0;
            return 0xffc861;
        }
        return -1;
    }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || mc.isPaused() || !HalcyonConfig.get().loot.rareGlow) return;
        if (++tick % 4 != 0) return;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof ItemEntity item) || e.squaredDistanceTo(p) > 32 * 32) continue;
            int c = colorOf(item.getStack());
            if (c < 0) continue;
            Vector3f col = new Vector3f(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f);
            double ang = RANDOM.nextDouble() * Math.PI * 2;
            mc.world.addParticle(new DustParticleEffect(col, 0.7f),
                    e.getX() + Math.cos(ang) * 0.3, e.getY() + 0.25 + RANDOM.nextDouble() * 0.2, e.getZ() + Math.sin(ang) * 0.3, 0, 0.02, 0);
            if (tick % 12 == 0) { // луч
                for (int i = 0; i < 6; i++) {
                    mc.world.addParticle(new DustParticleEffect(col, 0.45f - i * 0.04f),
                            e.getX(), e.getY() + 0.45 + i * 0.28, e.getZ(), 0, 0, 0);
                }
            }
        }
    }
}
