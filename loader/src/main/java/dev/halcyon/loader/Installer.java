package dev.halcyon.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Установка Halcyon Visuals:
 * Fabric для 1.21.1 → Fabric API → мод Halcyon → (конфиг HUD) → профиль в официальном Minecraft Launcher.
 * Вход в аккаунт и запуск игры выполняет официальный лаунчер; загрузчик не трогает учётные данные.
 */
public final class Installer {
    public static final String MC_VERSION = "1.21.1";
    public static final String LOADER_VERSION = "0.16.14";
    public static final String FABRIC_API = "0.116.15+1.21.1";
    public static final String PROFILE_KEY = "halcyon-visuals";
    public static final String PROFILE_NAME = "Halcyon Visuals";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20)).followRedirects(HttpClient.Redirect.NORMAL).build();

    public record Step(int index, int total, String text) {}

    private final Path mcDir;
    private final boolean separateDir;
    private final String hudConfig;
    private final Consumer<Step> progress;
    private final boolean optimize;

    /** Моды оптимизации с Modrinth: slug → что даёт. */
    public static final String[][] OPTIMIZATION_MODS = {
            {"sodium", "Sodium"}, {"lithium", "Lithium"}, {"ferrite-core", "FerriteCore"}};

    public Installer(Path mcDir, boolean separateDir, boolean optimize, String hudConfig, Consumer<Step> progress) {
        this.mcDir = mcDir;
        this.separateDir = separateDir;
        this.optimize = optimize;
        this.hudConfig = hudConfig == null ? "" : hudConfig.trim();
        this.progress = progress;
    }

    public static Path defaultMinecraftDir() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            return Paths.get(appData != null ? appData : home, ".minecraft");
        }
        if (os.contains("mac")) return Paths.get(home, "Library", "Application Support", "minecraft");
        return Paths.get(home, ".minecraft");
    }

    public Path gameDir() { return separateDir ? mcDir.resolve("halcyon-" + MC_VERSION) : mcDir; }

    /** Проверяет ввод заранее, чтобы не начинать установку зря. Возвращает текст ошибки или null. */
    public String validate() {
        if (!Files.isDirectory(mcDir)) return "Папка Minecraft не найдена: " + mcDir + "\nУстановите и запустите официальный Minecraft Launcher хотя бы один раз.";
        if (!Files.exists(mcDir.resolve("launcher_profiles.json"))) return "В папке нет launcher_profiles.json.\nЗапустите официальный Minecraft Launcher хотя бы один раз и закройте его.";
        if (!hudConfig.isEmpty()) {
            try {
                JsonObject o = JsonParser.parseString(hudConfig).getAsJsonObject();
                if (!o.has("config") && !o.has("el")) return "Вставленный текст не похож на конфиг Halcyon HUD.\nСкопируйте JSON кнопкой «JSON» в конструкторе на сайте.";
            } catch (Exception e) {
                return "Конфиг HUD не читается как JSON. Проверьте, что текст скопирован целиком.";
            }
        }
        return null;
    }

    public void install() throws Exception {
        int total = (hudConfig.isEmpty() ? 5 : 6) + (optimize ? 1 : 0), i = 0;

        step(++i, total, "Устанавливаем Fabric " + LOADER_VERSION + " для " + MC_VERSION);
        String versionId = installFabricProfile();

        step(++i, total, "Готовим папку игры");
        Path game = gameDir();
        Path mods = game.resolve("mods");
        Files.createDirectories(mods);
        cleanOld(mods);

        step(++i, total, "Скачиваем Fabric API " + FABRIC_API);
        downloadFabricApi(mods);

        step(++i, total, "Устанавливаем Halcyon Visuals");
        try (InputStream in = Installer.class.getResourceAsStream("/bundle/halcyon-visuals.jar")) {
            if (in == null) throw new IOException("Файл мода не встроен в загрузчик (bundle/halcyon-visuals.jar)");
            Files.copy(in, mods.resolve("halcyon-visuals.jar"), StandardCopyOption.REPLACE_EXISTING);
        }

        if (optimize) {
            step(++i, total, "Скачиваем оптимизацию: Sodium, Lithium, FerriteCore");
            for (String[] m : OPTIMIZATION_MODS) installFromModrinth(m[0], m[1], mods);
        }

        if (!hudConfig.isEmpty()) {
            step(++i, total, "Записываем конфиг HUD");
            Path cfg = game.resolve("config").resolve("halcyon").resolve("hud.json");
            Files.createDirectories(cfg.getParent());
            Files.writeString(cfg, hudConfig, StandardCharsets.UTF_8);
        }

        step(++i, total, "Добавляем профиль «" + PROFILE_NAME + "» в Minecraft Launcher");
        writeProfile(versionId, game);
    }

    private void step(int i, int total, String text) { progress.accept(new Step(i, total, text)); }

    private String installFabricProfile() throws Exception {
        String url = "https://meta.fabricmc.net/v2/versions/loader/" + MC_VERSION + "/" + LOADER_VERSION + "/profile/json";
        String json = getString(url);
        JsonObject profile = JsonParser.parseString(json).getAsJsonObject();
        String id = profile.get("id").getAsString();
        if (!id.matches("[A-Za-z0-9._+-]+")) throw new IOException("Неожиданный ответ сервера Fabric");
        Path dir = mcDir.resolve("versions").resolve(id);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(id + ".json"), json, StandardCharsets.UTF_8);
        Path jar = dir.resolve(id + ".jar");
        if (!Files.exists(jar)) Files.write(jar, new byte[0]); // заглушка, как у официального установщика Fabric
        return id;
    }

    /** Удаляет прошлые версии наших файлов; чужие моды не трогает. */
    private void cleanOld(Path mods) throws IOException {
        try (Stream<Path> s = Files.list(mods)) {
            for (Path p : s.toList()) {
                String n = p.getFileName().toString().toLowerCase();
                boolean ours = n.startsWith("halcyon-visuals");
                boolean oldApi = separateDir && n.startsWith("fabric-api-") && n.endsWith(".jar");
                boolean oldOpt = separateDir && optimize && n.endsWith(".jar")
                        && (n.startsWith("sodium-") || n.startsWith("lithium-") || n.startsWith("ferritecore-"));
                if (ours || oldApi || oldOpt) Files.deleteIfExists(p);
            }
        }
    }

    private void downloadFabricApi(Path mods) throws Exception {
        String base = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/" + FABRIC_API + "/fabric-api-" + FABRIC_API + ".jar";
        String expected = getString(base + ".sha1").trim().split("\\s+")[0].toLowerCase();
        Path target = mods.resolve("fabric-api-" + FABRIC_API + ".jar");
        if (Files.exists(target) && sha1(target).equals(expected)) return;
        Path tmp = Files.createTempFile("fabric-api", ".jar");
        try {
            HttpResponse<Path> r = HTTP.send(req(base), HttpResponse.BodyHandlers.ofFile(tmp));
            if (r.statusCode() != 200) throw new IOException("Fabric API: сервер ответил " + r.statusCode());
            String got = sha1(tmp);
            if (!got.equals(expected)) throw new IOException("Fabric API скачался с ошибкой (контрольная сумма не совпала). Попробуйте ещё раз.");
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    /** Последний релиз мода под Fabric 1.21.1 с Modrinth, с проверкой SHA-1. */
    private void installFromModrinth(String slug, String name, Path mods) throws Exception {
        String q = "loaders=" + java.net.URLEncoder.encode("[\"fabric\"]", StandardCharsets.UTF_8)
                + "&game_versions=" + java.net.URLEncoder.encode("[\"" + MC_VERSION + "\"]", StandardCharsets.UTF_8);
        com.google.gson.JsonArray versions = JsonParser.parseString(
                getString("https://api.modrinth.com/v2/project/" + slug + "/version?" + q)).getAsJsonArray();
        JsonObject chosen = null;
        for (var v : versions) {
            JsonObject o = v.getAsJsonObject();
            if (chosen == null) chosen = o;
            if ("release".equals(o.get("version_type").getAsString())) { chosen = o; break; }
        }
        if (chosen == null) throw new IOException(name + ": нет версии для " + MC_VERSION);
        JsonObject file = null;
        for (var f : chosen.getAsJsonArray("files")) {
            JsonObject o = f.getAsJsonObject();
            if (file == null || (o.has("primary") && o.get("primary").getAsBoolean())) file = o;
        }
        if (file == null) throw new IOException(name + ": файл не найден");
        String url = file.get("url").getAsString();
        String filename = file.get("filename").getAsString();
        String expected = file.getAsJsonObject("hashes").get("sha1").getAsString().toLowerCase();
        if (!filename.matches("[A-Za-z0-9._+-]+\\.jar") || !URI.create(url).getHost().endsWith("modrinth.com"))
            throw new IOException(name + ": неожиданный ответ Modrinth");

        Path target = mods.resolve(filename);
        if (Files.exists(target) && sha1(target).equals(expected)) return;
        Path tmp = Files.createTempFile(slug, ".jar");
        try {
            HttpResponse<Path> r = HTTP.send(req(url), HttpResponse.BodyHandlers.ofFile(tmp));
            if (r.statusCode() != 200) throw new IOException(name + ": сервер ответил " + r.statusCode());
            if (!sha1(tmp).equals(expected)) throw new IOException(name + " скачался с ошибкой (контрольная сумма не совпала).");
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private void writeProfile(String versionId, Path game) throws IOException {
        Path file = mcDir.resolve("launcher_profiles.json");
        String raw = Files.readString(file, StandardCharsets.UTF_8);
        Path backup = mcDir.resolve("launcher_profiles.json.halcyon-backup");
        if (!Files.exists(backup)) Files.writeString(backup, raw, StandardCharsets.UTF_8);

        JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
        JsonObject profiles = root.has("profiles") ? root.getAsJsonObject("profiles") : new JsonObject();
        String now = Instant.now().toString();
        JsonObject p = new JsonObject();
        p.addProperty("name", PROFILE_NAME);
        p.addProperty("type", "custom");
        p.addProperty("lastVersionId", versionId);
        p.addProperty("created", profiles.has(PROFILE_KEY) && profiles.getAsJsonObject(PROFILE_KEY).has("created")
                ? profiles.getAsJsonObject(PROFILE_KEY).get("created").getAsString() : now);
        p.addProperty("lastUsed", now);
        p.addProperty("icon", "Emerald_Block");
        if (separateDir) p.addProperty("gameDir", game.toString());
        profiles.add(PROFILE_KEY, p);
        root.add("profiles", profiles);
        Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
    }

    /** Убирает профиль и (если выбрано) отдельную папку игры. Возвращает описание сделанного. */
    public static String uninstall(Path mcDir, boolean deleteGameDir) throws IOException {
        StringBuilder done = new StringBuilder();
        Path file = mcDir.resolve("launcher_profiles.json");
        if (Files.exists(file)) {
            JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("profiles") && root.getAsJsonObject("profiles").remove(PROFILE_KEY) != null) {
                Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
                done.append("Профиль удалён из Minecraft Launcher.\n");
            }
        }
        Path main = mcDir.resolve("mods").resolve("halcyon-visuals.jar");
        if (Files.deleteIfExists(main)) done.append("Мод удалён из .minecraft/mods.\n");
        Path game = mcDir.resolve("halcyon-" + MC_VERSION);
        if (deleteGameDir && Files.isDirectory(game)) {
            try (Stream<Path> walk = Files.walk(game)) {
                for (Path p : walk.sorted((a, b) -> b.getNameCount() - a.getNameCount()).toList()) Files.deleteIfExists(p);
            }
            done.append("Папка ").append(game.getFileName()).append(" удалена.\n");
        }
        return done.isEmpty() ? "Halcyon не был установлен." : done.toString().trim();
    }

    /** Пытается открыть официальный Minecraft Launcher. */
    public static boolean openLauncher() {
        String pf86 = System.getenv("ProgramFiles(x86)"), pf = System.getenv("ProgramFiles"), local = System.getenv("LOCALAPPDATA");
        List<String> candidates = Stream.of(
                pf86 == null ? null : pf86 + "\\Minecraft Launcher\\MinecraftLauncher.exe",
                pf == null ? null : pf + "\\Minecraft Launcher\\MinecraftLauncher.exe",
                "C:\\XboxGames\\Minecraft Launcher\\Content\\Minecraft.exe",
                local == null ? null : local + "\\Programs\\Minecraft Launcher\\MinecraftLauncher.exe"
        ).filter(s -> s != null).toList();
        for (String c : candidates) {
            if (Files.isRegularFile(Paths.get(c))) {
                try { new ProcessBuilder(c).start(); return true; } catch (IOException ignored) { }
            }
        }
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            try { // версия из Microsoft Store / Xbox App
                new ProcessBuilder("explorer.exe", "shell:AppsFolder\\Microsoft.4297127D64EC6_8wekyb3d8bbwe!Minecraft").start();
                return true;
            } catch (IOException ignored) { }
        }
        return false;
    }

    private static HttpRequest req(String url) {
        return HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(60))
                .header("User-Agent", "HalcyonLoader/0.3").GET().build();
    }

    private static String getString(String url) throws Exception {
        HttpResponse<String> r = HTTP.send(req(url), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new IOException("Сервер " + URI.create(url).getHost() + " ответил " + r.statusCode());
        return r.body();
    }

    private static String sha1(Path p) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        try (InputStream in = Files.newInputStream(p)) {
            byte[] buf = new byte[1 << 16];
            for (int n; (n = in.read(buf)) > 0; ) md.update(buf, 0, n);
        }
        return HexFormat.of().formatHex(md.digest());
    }
}
