package dev.halcyon.visuals.skins;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Скины мечей Halcyon. Чисто клиентские: меняют вид всех мечей у вас в игре
 * (в руке, в инвентаре, на земле). Другие игроки их не видят, характеристики оружия не меняются.
 * Текстуры генерируются tools/SwordGen.java.
 */
public final class SwordSkins {
    public record Skin(String id, String name, boolean animated) {}

    public static final String NONE = "none";

    public static final List<Skin> ALL = List.of(
            new Skin("halcyon", "Halcyon", true),
            new Skin("void", "Void", true),
            new Skin("inferno", "Inferno", true),
            new Skin("frost", "Frostbite", false),
            new Skin("obsidian", "Obsidian", false),
            new Skin("sakura", "Sakura", false),
            new Skin("toxic", "Toxic", false),
            new Skin("royal", "Royal", false));

    private SwordSkins() {}

    public static Identifier model(String id) { return Identifier.of("halcyon", "item/sword_" + id); }

    public static Identifier texture(String id) { return Identifier.of("halcyon", "textures/item/sword_" + id + ".png"); }

    public static boolean valid(String id) {
        if (NONE.equals(id)) return true;
        for (Skin s : ALL) if (s.id().equals(id)) return true;
        return false;
    }

    public static Skin get(String id) {
        for (Skin s : ALL) if (s.id().equals(id)) return s;
        return null;
    }

    /** Регистрирует модели скинов, чтобы игра запекла их вместе с остальными. */
    public static void init() {
        ModelLoadingPlugin.register(ctx -> {
            ctx.addModels(ALL.stream().map(s -> model(s.id())).toList());
            for (RodSkins.Rod r : RodSkins.ALL) ctx.addModels(RodSkins.model(r.id()), RodSkins.castModel(r.id()));
        });
    }
}
