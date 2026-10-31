import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Генератор плащей Halcyon. Формат — стандартная развёртка плаща Minecraft (64×32) в 4× разрешении: 256×128.
 * Анимированные плащи — отдельные кадры cape_<id>_<n>.png, мод переключает их по времени.
 * Запуск: java tools/CapeGen.java src/main/resources/assets/halcyon
 */
public class CapeGen {
    static final int K = 4, W = 64 * K, H = 32 * K, FRAMES = 8;
    // Лицевая сторона плаща в развёртке: x 1..11, y 1..17 (обратная: x 12..22)
    static final int FW = 10 * K, FH = 16 * K;

    interface Painter { int color(double u, double v, int frame); } // u,v ∈ [0,1] по лицевой стороне

    record Cape(String id, boolean animated, int edge, Painter painter, boolean logo) {}

    public static void main(String[] args) throws Exception {
        Path dir = Path.of(args.length > 0 ? args[0] : "src/main/resources/assets/halcyon").resolve("textures/cape");
        Files.createDirectories(dir);
        Random rnd = new Random(7);
        double[][] stars = new double[26][3];
        for (double[] s : stars) { s[0] = rnd.nextDouble(); s[1] = rnd.nextDouble(); s[2] = rnd.nextDouble(); }

        Cape[] capes = {
                new Cape("halcyon", true, 0x0b2a22, (u, v, f) -> {
                    int base = grad(new int[]{0x3dffa0, 0x20d6c8, 0x0e3b40}, v);
                    double band = ((u * 0.6 + v) - f / (double) FRAMES * 1.6 + 0.3);
                    double k = Math.max(0, 1 - Math.abs(band) / 0.12);
                    return mix(base, 0xffffff, 0.45 * k);
                }, true),
                new Cape("aurora", true, 0x0a0f24, (u, v, f) -> {
                    double ph = f / (double) FRAMES * Math.PI * 2;
                    double w1 = Math.sin(u * 6 + v * 3 + ph) * 0.5 + 0.5, w2 = Math.sin(u * 4 - v * 5 - ph) * 0.5 + 0.5;
                    int c = mix(0x0c1230, 0x3dffa0, Math.pow(w1, 3) * (1 - v) * 0.9);
                    return mix(c, 0xb18cff, Math.pow(w2, 3) * 0.7);
                }, false),
                new Cape("midnight", false, 0x05060f, (u, v, f) -> {
                    int c = grad(new int[]{0x1a2250, 0x0a0c1e}, v);
                    for (double[] s : stars) {
                        double d = Math.hypot((u - s[0]) * FW, (v - s[1]) * FH);
                        if (d < 0.9 + s[2] * 1.2) return mix(c, 0xffffff, 0.6 + 0.4 * s[2]);
                    }
                    return c;
                }, true),
                new Cape("ember", false, 0x200805, (u, v, f) -> {
                    int c = grad(new int[]{0x1a0a08, 0x8f1f12, 0xff7a2f}, v);
                    double n = Math.sin(u * 23 + v * 7) * Math.sin(u * 11 - v * 19);
                    return n > 0.82 && v > 0.45 ? mix(c, 0xffd46b, 0.8) : c;
                }, false),
                new Cape("glacier", false, 0x10263a, (u, v, f) -> {
                    int c = grad(new int[]{0xf2fbff, 0x86d4ff, 0x2c4170}, v);
                    double line = Math.abs(((u + v * 0.7) * 5) % 1.0 - 0.5);
                    return line < 0.04 ? mix(c, 0xffffff, 0.7) : c;
                }, false),
                new Cape("sakura", false, 0x3a1424, (u, v, f) -> {
                    int c = grad(new int[]{0xffe3ef, 0xff8fc0, 0xc8467c}, v);
                    double n = Math.sin(u * 31 + v * 13) * Math.sin(u * 17 - v * 29);
                    return n > 0.86 ? mix(c, 0xffffff, 0.85) : c;
                }, false)
        };

        for (Cape c : capes) {
            int frames = c.animated() ? FRAMES : 1;
            for (int f = 0; f < frames; f++) {
                BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
                paintCape(img, c, f);
                String name = c.animated() ? "cape_" + c.id() + "_" + f + ".png" : "cape_" + c.id() + ".png";
                ImageIO.write(img, "png", dir.resolve(name).toFile());
            }
            System.out.println("cape_" + c.id() + (c.animated() ? " (" + frames + ")" : ""));
        }
    }

    static void paintCape(BufferedImage img, Cape c, int frame) {
        // Лицевая и обратная стороны
        for (int face = 0; face < 2; face++) {
            int ox = (face == 0 ? 1 : 12) * K, oy = K;
            for (int y = 0; y < FH; y++)
                for (int x = 0; x < FW; x++) {
                    double u = (x + 0.5) / FW, v = (y + 0.5) / FH;
                    double uu = face == 0 ? u : 1 - u;
                    int col = c.painter().color(uu, v, frame);
                    if (x < 2 || x >= FW - 2 || y >= FH - 2) col = mix(col, c.edge(), 0.6); // окантовка
                    if (c.logo() && logo(uu, v)) col = mix(col, 0xffffff, 0.92);
                    img.setRGB(ox + x, oy + y, 0xFF000000 | col);
                }
        }
        // Торцы (тонкие грани) — цвет окантовки
        fill(img, 0, K, K, FH, c.edge());
        fill(img, 11 * K, K, K, FH, c.edge());
        fill(img, K, 0, FW, K, c.edge());
        fill(img, 11 * K, 0, FW, K, c.edge());
    }

    /** Знак Halcyon: две дуги и точка, в верхней трети плаща. */
    static boolean logo(double u, double v) {
        double cx = 0.5, cy = 0.36, x = (u - cx) * 10, y = (v - cy) * 16; // в «пикселях» плаща
        double r = Math.hypot(x, y);
        boolean upper = y <= 0.15;
        if (upper && Math.abs(r - 3.0) < 0.42) return true;
        if (upper && Math.abs(r - 1.7) < 0.42) return true;
        return Math.hypot(x, y - 1.3) < 0.55;
    }

    static void fill(BufferedImage img, int x0, int y0, int w, int h, int c) {
        for (int y = y0; y < y0 + h; y++) for (int x = x0; x < x0 + w; x++) img.setRGB(x, y, 0xFF000000 | c);
    }

    static int grad(int[] stops, double t) {
        double p = Math.max(0, Math.min(1, t)) * (stops.length - 1);
        int i = Math.min(stops.length - 2, (int) Math.floor(p));
        return mix(stops[i], stops[i + 1], p - i);
    }

    static int mix(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) Math.round(ar + (br - ar) * t) << 16) | ((int) Math.round(ag + (bg - ag) * t) << 8) | (int) Math.round(ab + (bb - ab) * t);
    }
}
