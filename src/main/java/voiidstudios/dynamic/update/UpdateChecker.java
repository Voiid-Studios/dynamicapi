package voiidstudios.dynamic.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import voiidstudios.dynamic.log.DAPILogger;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private static final String API_URL = "https://api.github.com/repos/Voiid-Studios/dynamicapi/releases/latest";

    private final String currentVersion;
    private final DAPILogger log;
    private String latestVersion;

    public UpdateChecker(String currentVersion, DAPILogger log) {
        this.currentVersion = currentVersion;
        this.log = log;
    }

    public UpdateCheckerResult check() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestProperty("User-Agent", "DynamicAPI-UpdateChecker");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            JsonObject release = new JsonParser().parse(
                new InputStreamReader(connection.getInputStream())
            ).getAsJsonObject();

            if (release.get("prerelease").getAsBoolean()) return UpdateCheckerResult.noErrors(null);

            String tag = release.get("tag_name").getAsString().replace("v", "").trim();
            latestVersion = tag;

            if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                return UpdateCheckerResult.noErrors(latestVersion);
            }

            log.success("You are using the latest version!");
            return UpdateCheckerResult.noErrors(null);

        } catch (Exception ex) {
            return UpdateCheckerResult.error();
        }
    }

    public String getLatestVersion() { return latestVersion; }
    public String getCurrentVersion() { return currentVersion; }
}