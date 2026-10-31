package dev.halcyon.visuals;

import dev.halcyon.visuals.config.HalcyonConfig;
import dev.halcyon.visuals.effects.HitEffects;
import dev.halcyon.visuals.gui.HalcyonScreen;
import dev.halcyon.visuals.hud.Notifications;
import dev.halcyon.visuals.hud.ScreenshotMode;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import dev.halcyon.visuals.hud.HudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Halcyon Visuals — клиентский мод с чисто визуальными изменениями.
 * Ничего не автоматизирует и не даёт игровых преимуществ: только отрисовка.
 */
public final class HalcyonClient implements ClientModInitializer {
    public static final String MOD_ID = "halcyon";
    public static final Logger LOG = LoggerFactory.getLogger("Halcyon");

    private static KeyBinding reloadKey;
    private static KeyBinding toggleKey;
    private static KeyBinding menuKey;
    private static KeyBinding screenshotKey;

    @Override
    public void onInitializeClient() {
        HalcyonConfig.load();
        dev.halcyon.visuals.skins.SwordSkins.init();

        reloadKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.halcyon.reload", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.halcyon"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.halcyon.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.halcyon"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.halcyon.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.halcyon"));
        screenshotKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.halcyon.screenshot", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F6, "key.categories.halcyon"));

        // Встроенный набор ресурсов погоды: включается в меню Halcyon (вкладка «Мир») или в «Пакетах ресурсов»
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(mod -> {
            ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of(MOD_ID, "weather"), mod,
                    Text.literal("Halcyon Weather"), ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of(MOD_ID, "glint"), mod,
                    Text.literal("Halcyon Glint"), ResourcePackActivationType.NORMAL);
        });

        // Кнопка Halcyon в главном меню и меню паузы
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen || screen instanceof GameMenuScreen) {
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Halcyon"),
                        b -> client.setScreen(new HalcyonScreen(screen))).dimensions(w - 74, 6, 68, 20).build());
            }
        });

        HudRenderCallback.EVENT.register(HudRenderer::render);
        AttackEntityCallback.EVENT.register(HitEffects::onAttack);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                Notifications.push("Halcyon Visuals", "Правый Shift — меню визуалов"));

        WorldRenderEvents.BLOCK_OUTLINE.register(dev.halcyon.visuals.effects.BlockOutline::render);
        WorldRenderEvents.AFTER_ENTITIES.register(dev.halcyon.visuals.effects.DamageNumbers::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HitEffects.tick(client);
            dev.halcyon.visuals.effects.Aura.tick(client);
            dev.halcyon.visuals.effects.DamageNumbers.tick(client);
            ScreenshotMode.tick(client);
            dev.halcyon.visuals.effects.LootGlow.tick(client);
            if (client.player == null) return;
            while (screenshotKey.wasPressed()) ScreenshotMode.request(client);
            while (reloadKey.wasPressed()) {
                String error = HalcyonConfig.load();
                dev.halcyon.visuals.effects.DamageTint.apply();
                if (error == null) Notifications.push("Halcyon", Text.translatable("halcyon.msg.reloaded").getString());
                else Notifications.warn("Ошибка конфига", "Загружены настройки по умолчанию");
            }
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new HalcyonScreen(null));
            }
            while (toggleKey.wasPressed()) {
                HalcyonConfig cfg = HalcyonConfig.get();
                cfg.enabled = !cfg.enabled;
                client.player.sendMessage(Text.translatable(cfg.enabled ? "halcyon.msg.hud_on" : "halcyon.msg.hud_off"), true);
            }
            checkDurability(client.player);
        });

        LOG.info("Halcyon Visuals loaded");
    }

    private static final EquipmentSlot[] WATCHED = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
    private static final Map<EquipmentSlot, Boolean> WARNED = new EnumMap<>(EquipmentSlot.class);

    /** Одно уведомление, когда прочность брони или предмета в руке падает ниже 10%. */
    private static void checkDurability(PlayerEntity player) {
        for (EquipmentSlot slot : WATCHED) {
            ItemStack s = player.getEquippedStack(slot);
            boolean low = !s.isEmpty() && s.isDamageable() && s.getMaxDamage() > 0
                    && (s.getMaxDamage() - s.getDamage()) * 10 < s.getMaxDamage();
            if (low && !WARNED.getOrDefault(slot, false)) {
                int pct = Math.round(100f * (s.getMaxDamage() - s.getDamage()) / s.getMaxDamage());
                Notifications.warn("Низкая прочность", s.getName().getString() + ": " + pct + "%");
            }
            WARNED.put(slot, low);
        }
    }
}
