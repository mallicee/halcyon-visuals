package dev.halcyon.visuals.skins;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.List;

/**
 * Плащи Halcyon (клиентская косметика): видны вам на вашем персонаже — в виде от третьего лица (F5),
 * в инвентаре и на экране настроек скина. Другие игроки их не видят.
 * Текстуры — tools/CapeGen.java, развёртка стандартного плаща в 4× разрешении.
 */
public final class Capes {
    public record Cape(String id, String name, boolean animated) {}

    public static final String NONE = "none";
    private static final int FRAMES = 8;
    private static final long FRAME_MS = 120;

    public static final List<Cape> ALL = List.of(
            new Cape("halcyon", "Halcyon", true),
            new Cape("aurora", "Aurora", true),
            new Cape("midnight", "Midnight", false),
            new Cape("ember", "Ember", false),
            new Cape("glacier", "Glacier", false),
            new Cape("sakura", "Sakura", false));

    private Capes() {}

    public static Cape get(String id) {
        for (Cape c : ALL) if (c.id().equals(id)) return c;
        return null;
    }

    public static boolean valid(String id) { return NONE.equals(id) || get(id) != null; }

    /** Текстура плаща; у анимированных — текущий кадр. */
    public static Identifier texture(String id) {
        Cape c = get(id);
        if (c == null) return null;
        if (!c.animated()) return Identifier.of("halcyon", "textures/cape/cape_" + id + ".png");
        int frame = (int) ((Util.getMeasuringTimeMs() / FRAME_MS) % FRAMES);
        return Identifier.of("halcyon", "textures/cape/cape_" + id + "_" + frame + ".png");
    }
}
