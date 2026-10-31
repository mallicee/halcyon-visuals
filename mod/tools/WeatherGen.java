import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Текстуры погоды Halcyon (встроенный набор ресурсов «Halcyon Weather»):
 * тонкие светящиеся капли дождя и мягкие снежинки. Размер как у ванильных — 64×256.
 * Запуск: java tools/WeatherGen.java src/main/resources/resourcepacks/weather
 */
public class WeatherGen {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length > 0 ? args[0] : "src/main/resources/resourcepacks/weather");
        Path env = root.resolve("assets/minecraft/textures/environment");
        Files.createDirectories(env);
        Random r = new Random(42);

        BufferedImage rain = new BufferedImage(64, 256, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < 70; i++) {
            int x = r.nextInt(64), y = r.nextInt(256), len = 7 + r.nextInt(12);
            double tint = r.nextDouble();
            for (int k = 0; k < len; k++) {
                double fade = Math.sin(Math.PI * k / (len - 1));
                int a = (int) (60 + 150 * fade);
                int c = mix(0x9ff7e6, 0xe8fbff, tint);
                rain.setRGB(x, (y + k) % 256, (a << 24) | c);
            }
        }
        ImageIO.write(rain, "png", env.resolve("rain.png").toFile());

        BufferedImage snow = new BufferedImage(64, 256, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < 90; i++) {
            int x = r.nextInt(63), y = r.nextInt(255);
            int c = r.nextInt(5) == 0 ? 0xbff9e8 : 0xffffff;
            put(snow, x, y, c, 230);
            if (r.nextBoolean()) { put(snow, x + 1, y, c, 140); put(snow, x, y + 1, c, 140); put(snow, x + 1, y + 1, c, 90); }
        }
        ImageIO.write(snow, "png", env.resolve("snow.png").toFile());

        Files.writeString(root.resolve("pack.mcmeta"),
                "{\n  \"pack\": {\n    \"pack_format\": 34,\n    \"description\": \"Halcyon Weather — стилизованный дождь и снег\"\n  }\n}\n");
        System.out.println("ok");
    }

    static void put(BufferedImage img, int x, int y, int c, int a) { img.setRGB(x, y, (a << 24) | c); }

    static int mix(int a, int b, double t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) Math.round(ar + (br - ar) * t) << 16) | ((int) Math.round(ag + (bg - ag) * t) << 8) | (int) Math.round(ab + (bb - ab) * t);
    }
}
