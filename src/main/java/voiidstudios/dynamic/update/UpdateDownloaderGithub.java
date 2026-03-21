package voiidstudios.dynamic.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import voiidstudios.dynamic.log.DAPILogger;

import org.bukkit.Bukkit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class UpdateDownloaderGithub {
    private static final String API_URL = "https://api.github.com/repos/Voiid-Studios/dynamicapi/releases/latest";
    private static final String USER_AGENT = "DynamicAPI-Updater";

    private final DAPILogger log;
    private final UpdateChecker updateChecker;

    public UpdateDownloaderGithub(DAPILogger log, UpdateChecker updateChecker) {
        this.log = log;
        this.updateChecker = updateChecker;
    }

    public boolean downloadUpdate() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) {
                log.warning("GitHub API responded with code " + conn.getResponseCode());
                return false;
            }

            JsonObject release = new JsonParser().parse(
                new InputStreamReader(conn.getInputStream())
            ).getAsJsonObject();

            if (release.get("prerelease").getAsBoolean()) return false;

            String downloadUrl = null;
            String assetName = null;
            for (JsonElement e : release.getAsJsonArray("assets")) {
                JsonObject asset = e.getAsJsonObject();
                String name = asset.get("name").getAsString();
                if (name.toLowerCase().contains("dynamicapi") && name.endsWith(".jar")) {
                    downloadUrl = asset.get("browser_download_url").getAsString();
                    assetName = name;
                    break;
                }
            }

            if (downloadUrl == null || assetName == null) {
                log.warning("Could not find a DynamicAPI jar in the latest release assets.");
                return false;
            }

            long start = System.currentTimeMillis();

            Path updateFile = Bukkit.getUpdateFolderFile().toPath().resolve(assetName);
            Files.createDirectories(updateFile.getParent());

            HttpURLConnection dlConn = (HttpURLConnection) new URL(downloadUrl).openConnection();
            dlConn.setRequestProperty("User-Agent", USER_AGENT);

            try (InputStream in = dlConn.getInputStream()) {
                Files.copy(in, updateFile, StandardCopyOption.REPLACE_EXISTING);
            }

            long elapsed = System.currentTimeMillis() - start;
            log.success("Downloaded update in " + elapsed + "ms!");
            log.pasiveWarning("DynamicAPI will update from §6" + updateChecker.getCurrentVersion() + "§r to §9" + updateChecker.getLatestVersion() + "§r on the next server restart!");
            return true;
        } catch (Exception ex) {
            log.failure("Failed to download update: " + ex.getMessage());
            return false;
        }
    }
}