import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Скины удочек (обычная и заброшенная) и текстура изумрудного блеска зачарований.
 * Запуск: java tools/ExtraGen.java src/main/resources
 */
public class ExtraGen {
    record Rod(String id, int stick, int stickLight, int grip, int tip, int line) {}

    static final Rod[] RODS = {
            new Rod("halcyon", 0x1b6e5c, 0x3dffa0, 0x2a2f3a, 0x20d6c8, 0xd8fff0),
            new Rod("frost", 0x3a8fd0, 0xd6f6ff, 0x1f2f52, 0xffffff, 0xeaf8ff),
            new Rod("ember", 0x8f1f12, 0xffb13b, 0x2a1a14, 0xffd46b, 0xffe2b0)
    };

    public static void main(String[] args) throws Exception {
        Path res = Path.of(args.length > 0 ? args[0] : "src/main/resources");
        Path tex = res.resolve("assets/halcyon/textures/item"), models = res.resolve("assets/halcyon/models/item");
        Files.createDirectories(tex);
        Files.createDirectories(models);
        for (Rod r : RODS) {
            ImageIO.write(rod(r, false), "png", tex.resolve("rod_" + r.id() + ".png").toFile());
            ImageIO.write(rod(r, true), "png", tex.resolve("rod_" + r.id() + "_cast.png").toFile());
            Files.writeString(models.resolve("rod_" + r.id() + ".json"), """
                    {
                      "parent": "minecraft:item/handheld_rod",
                      "textures": { "layer0": "halcyon:item/rod_%1$s" },
                      "overrides": [ { "predicate": { "cast": 1 }, "model": "halcyon:item/rod_%1$s_cast" } ]
                    }
                    """.formatted(r.id()));
            Files.writeString(models.resolve("rod_" + r.id() + "_cast.json"), """
                    {
                      "parent": "minecraft:item/handheld_rod",
                      "textures": { "layer0": "halcyon:item/rod_%s_cast" }
                    }
                    """.formatted(r.id()));
            System.out.println("rod_" + r.id());
        }

        // Блеск зачарований (набор ресурсов «Halcyon Glint»)
        Path glint = res.resolve("resourcepacks/glint/assets/minecraft/textures/misc");
        Files.createDirectories(glint);
        BufferedImage g = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Random rnd = new Random(3);
        double[][] waves = new double[6][3];
        for (double[] w : waves) { w[0] = rnd.nextDouble() * 6; w[1] = 0.6 + rnd.nextDouble(); w[2] = rnd.nextDouble(); }
        for (int y = 0; y < 128; y++)
            for (int x = 0; x < 128; x++) {
                double v = 0;
                for (double[] w : waves) v += Math.pow(0.5 + 0.5 * Math.sin((x + y * w[1]) / 128.0 * Math.PI * 2 * 2 + w[0]), 12) * (0.4 + w[2]);
                v = Math.min(0.85, v * 0.42);
                int c = mix(0x3dffa0, 0x20d6c8, 0.5 + 0.5 * Math.sin((x - y) / 40.0));
                int r = (int) (((c >> 16) & 255) * v), gg = (int) (((c >> 8) & 255) * v), b = (int) ((c & 255) * v);
                g.setRGB(x, y, 0xFF000000 | (r << 16) | (gg << 8) | b);
            }
        ImageIO.write(g, "png", glint.resolve("enchanted_glint_item.png").toFile());
        ImageIO.write(g, "png", glint.resolve("enchanted_glint_entity.png").toFile());
        Files.writeString(res.resolve("resourcepacks/glint/pack.mcmeta"),
                "{\n  \"pack\": {\n    \"pack_format\": 34,\n    \"description\": \"Halcyon Glint — изумрудный блеск зачарований\"\n  }\n}\n");
        System.out.println("glint");
    }

    /** Удочка по диагонали из левого нижнего угла; в обычном виде с лески свисает крючок. */
    static BufferedImage rod(Rod r, boolean cast) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i <= 12; i++) {
            int x = 1 + i, y = 14 - i;
            int c = i < 3 ? r.grip() : i == 12 ? r.tip() : (i % 3 == 0 ? r.stickLight() : r.stick());
            if (i >= 3 && i < 12) px(img, x, y + 1, mix(r.stick(), 0x000000, 0.45)); // тень, чтобы удочка читалась
            px(img, x, y, c);
            if (i < 3) px(img, x + 1, y, r.grip()); // утолщение рукояти
        }
        px(img, 2, 12, r.stickLight()); // катушка
        if (!cast) {
            for (int y = 3; y <= 11; y++) px(img, 14, y, r.line());
            px(img, 14, 12, r.tip()); px(img, 13, 13, r.tip()); px(img, 14, 13, r.tip());
        }
        return img;
    }

    static void px(BufferedImage img, int x, int y, int c) { img.setRGB(x, y, 0xFF000000 | c); }

    static int mix(int a, int b, double t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) Math.round(ar + (br - ar) * t) << 16) | ((int) Math.round(ag + (bg - ag) * t) << 8) | (int) Math.round(ab + (bb - ab) * t);
    }
}
