package dev.halcyon.visuals.skins;

import net.minecraft.util.Identifier;

import java.util.List;

/** Скины удочки (клиентские, с отдельной моделью заброса). Текстуры — tools/ExtraGen.java. */
public final class RodSkins {
    public record Rod(String id, String name) {}

    public static final String NONE = "none";
    public static final List<Rod> ALL = List.of(new Rod("halcyon", "Halcyon"), new Rod("frost", "Frostbite"), new Rod("ember", "Ember"));

    private RodSkins() {}

    public static Identifier model(String id) { return Identifier.of("halcyon", "item/rod_" + id); }
    public static Identifier castModel(String id) { return Identifier.of("halcyon", "item/rod_" + id + "_cast"); }
    public static Identifier texture(String id) { return Identifier.of("halcyon", "textures/item/rod_" + id + ".png"); }

    public static Rod get(String id) {
        for (Rod r : ALL) if (r.id().equals(id)) return r;
        return null;
    }

    public static boolean valid(String id) { return NONE.equals(id) || get(id) != null; }
}
