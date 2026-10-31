import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Генератор текстур скинов мечей Halcyon (16×16, пиксель-арт в стиле ванильных предметов).
 * Запуск: java tools/SwordGen.java src/main/resources/assets/halcyon
 * Создаёт textures/item/sword_*.png (+ .mcmeta для анимированных) и models/item/sword_*.json.
 */
public class SwordGen {
    record Skin(String id, int outline, int edge, int[] blade, int dark, int guard, int guardLight, int handle, int handle2, int pommel, String anim) {}

    static final Skin[] SKINS = {
            new Skin("halcyon", 0x0b2a22, 0xeafff6, new int[]{0x3dffa0, 0x20d6c8}, 0x138a6e, 0x2a2f3a, 0x5d6676, 0x1a1d24, 0x2b303b, 0x3dffa0, "shimmer"),
            new Skin("frost", 0x10263a, 0xffffff, new int[]{0xd6f6ff, 0x86d4ff}, 0x3a8fd0, 0xc9d3e0, 0xf2f6fb, 0x1f2f52, 0x2c4170, 0x9fe4ff, null),
            new Skin("void", 0x140a24, 0xeadcff, new int[]{0xb18cff, 0x5b2bd6}, 0x3a1a8c, 0x1b1426, 0x4a3366, 0x120d1c, 0x241a33, 0xff5cf0, "pulse"),
            new Skin("inferno", 0x2a0c06, 0xfff2b0, new int[]{0xffd46b, 0xff7a2f, 0xd93a2a}, 0x8f1f12, 0x3a3533, 0x6b6460, 0x2a1a14, 0x40281e, 0xffb13b, "flicker"),
            new Skin("obsidian", 0x07050c, 0x9b6bff, new int[]{0x3a2c56, 0x231a36}, 0x140f20, 0xe8b84a, 0xfff0b8, 0x2a1a10, 0x45301e, 0xe8b84a, null),
            new Skin("sakura", 0x3a1424, 0xffffff, new int[]{0xffd3e6, 0xff6fa8}, 0xc8467c, 0xf3eadc, 0xffffff, 0x4a2a1c, 0x6b4028, 0xff8fc0, null),
            new Skin("toxic", 0x0d2408, 0xf4ffd0, new int[]{0xc6ff4a, 0x3ddc4a}, 0x1f8a2a, 0x2f3a2a, 0x56664e, 0x1c2418, 0x2c3826, 0xc6ff4a, null),
            new Skin("royal", 0x2e1e05, 0xfffbe0, new int[]{0xffe08a, 0xd9a530}, 0x8a6414, 0x3b6bff, 0x9fb8ff, 0x23224a, 0x34336b, 0x3b6bff, null)
    };

    static final int FRAMES = 8;

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length > 0 ? args[0] : "src/main/resources/assets/halcyon");
        Path tex = root.resolve("textures/item"), models = root.resolve("models/item");
        Files.createDirectories(tex);
        Files.createDirectories(models);
        for (Skin s : SKINS) {
            int frames = s.anim() == null ? 1 : FRAMES;
            BufferedImage img = new BufferedImage(16, 16 * frames, BufferedImage.TYPE_INT_ARGB);
            for (int f = 0; f < frames; f++) draw(img, s, f, f * 16);
            ImageIO.write(img, "png", tex.resolve("sword_" + s.id() + ".png").toFile());
            if (frames > 1) {
                int ft = s.anim().equals("flicker") ? 2 : 3;
                Files.writeString(tex.resolve("sword_" + s.id() + ".png.mcmeta"),
                        "{\n  \"animation\": { \"frametime\": " + ft + ", \"interpolate\": " + !s.anim().equals("flicker") + " }\n}\n");
            }
            Files.writeString(models.resolve("sword_" + s.id() + ".json"),
                    "{\n  \"parent\": \"minecraft:item/handheld\",\n  \"textures\": { \"layer0\": \"halcyon:item/sword_" + s.id() + "\" }\n}\n");
            System.out.println("sword_" + s.id() + (frames > 1 ? " (" + frames + " кадров)" : ""));
        }
    }

    // Области: 0 пусто, 1 лезвие, 2 гарда, 3 рукоять, 4 навершие
    static int region(double s, double c) {
        if (s >= 1.2 && s <= 14.6) {
            double half = 1.15 * Math.min(1.0, (14.6 - s) / 2.2);
            if (Math.abs(c) <= Math.max(0.35, half)) return 1;
        }
        if (s >= -0.3 && s < 1.2 && Math.abs(c) <= 3.0) return 2;
        if (s >= -3.7 && s < -0.3 && Math.abs(c) <= 0.72) return 3;
        if (s >= -5.0 && s < -3.7 && Math.abs(c) <= 1.15) return 4;
        return 0;
    }

    static void draw(BufferedImage img, Skin sk, int frame, int oy) {
        double hx = 4.5, hy = 11.5, r2 = Math.sqrt(0.5);
        int[][] reg = new int[16][16];
        double[][] ss = new double[16][16], cc = new double[16][16];
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++) {
                double px = x + 0.5 - hx, py = y + 0.5 - hy;
                double s = (px - py) * r2, c = (px + py) * r2;
                ss[y][x] = s; cc[y][x] = c;
                reg[y][x] = region(s, c);
            }
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++) {
                int r = reg[y][x];
                int color;
                if (r == 0) {
                    boolean near = (x > 0 && reg[y][x - 1] != 0) || (x < 15 && reg[y][x + 1] != 0)
                            || (y > 0 && reg[y - 1][x] != 0) || (y < 15 && reg[y + 1][x] != 0);
                    if (!near) continue;
                    color = sk.outline();
                } else {
                    double s = ss[y][x], c = cc[y][x];
                    color = switch (r) {
                        case 1 -> blade(sk, s, c, frame);
                        case 2 -> c < -0.5 ? sk.guardLight() : sk.guard();
                        case 3 -> ((int) Math.floor((s + 4) * 1.4) % 2 == 0) ? sk.handle() : sk.handle2();
                        default -> sk.pommel();
                    };
                }
                img.setRGB(x, oy + y, 0xFF000000 | color);
            }
    }

    static int blade(Skin sk, double s, double c, int frame) {
        double t = Math.max(0, Math.min(1, (s - 1.2) / 13.4));
        int base = gradient(sk.blade(), t);
        int color = c < -0.4 ? mix(base, sk.edge(), 0.55) : c > 0.4 ? mix(base, sk.dark(), 0.45) : base;
        if (sk.anim() == null) return color;
        double ph = frame / (double) FRAMES;
        return switch (sk.anim()) {
            case "shimmer" -> { // светлая полоса бежит от гарды к острию
                double pos = -1 + ph * 18;
                double k = Math.max(0, 1 - Math.abs(s - pos) / 1.6);
                yield mix(color, 0xffffff, 0.6 * k);
            }
            case "pulse" -> mix(color, 0xff9cf6, 0.25 * (0.5 - 0.5 * Math.cos(ph * Math.PI * 2)));
            case "flicker" -> { // «пламя»: яркость пятнами по кадрам
                double n = Math.sin(s * 2.1 + frame * 1.7) * 0.5 + 0.5;
                yield mix(color, 0xfff2b0, 0.35 * n * t);
            }
            default -> color;
        };
    }

    static int gradient(int[] stops, double t) {
        if (stops.length == 1) return stops[0];
        double p = t * (stops.length - 1);
        int i = Math.min(stops.length - 2, (int) Math.floor(p));
        return mix(stops[i], stops[i + 1], p - i);
    }

    static int mix(int a, int b, double t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) Math.round(ar + (br - ar) * t) << 16) | ((int) Math.round(ag + (bg - ag) * t) << 8) | (int) Math.round(ab + (bb - ab) * t);
    }
}
