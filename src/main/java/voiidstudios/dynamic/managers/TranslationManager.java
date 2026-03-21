package voiidstudios.dynamic.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import voiidstudios.dynamic.log.DAPILogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TranslationManager {
    private final JavaPlugin plugin;
    private final DAPILogger log;

    private FileConfiguration jarLangBase;
    private FileConfiguration langBase;
    private FileConfiguration langSelected;
    private FileConfiguration langCustomOverrides;

    private String currentLang = "en_US";

    public TranslationManager(JavaPlugin plugin, DAPILogger log) {
        this.plugin = plugin;
        this.log = log;
    }

    public void loadLanguage(String langCode) {
        this.currentLang = langCode;

        jarLangBase = loadYamlFromResource("messages/origins/en_US.yml");

        ensureDataFileExists("messages/origins/en_US.yml");
        ensureDataFileExists("messages/custom/custom.yml");

        langBase = loadYaml("messages/origins/en_US.yml");
        langSelected = loadYaml("messages/origins/" + langCode + ".yml");
        langCustomOverrides = loadYaml("messages/custom/custom.yml");

        log.process("Synchronizing language keys... ");

        syncMissingKeys();
    }

    private void ensureDataFileExists(String resourcePath) {
        File dest = new File(plugin.getDataFolder(), resourcePath);
        if (!dest.exists()) {
            InputStream is = plugin.getResource(resourcePath);
            if (is != null) {
                dest.getParentFile().mkdirs();
                try (InputStream in = is; FileOutputStream out = new FileOutputStream(dest)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                } catch (IOException e) {
                    log.warning("Could not copy resource: " + resourcePath);
                }
            }
        }
    }

    private FileConfiguration loadYaml(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) return new YamlConfiguration();
        return YamlConfiguration.loadConfiguration(file);
    }

    private FileConfiguration loadYamlFromResource(String resourcePath) {
        InputStream is = plugin.getResource(resourcePath);
        if (is == null) return new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            log.warning("Failed to load resource: " + resourcePath);
            return new YamlConfiguration();
        }
    }

    public void syncMissingKeys() {
        if (jarLangBase == null) return;
        if (langSelected == null) langSelected = new YamlConfiguration();
        if (langBase == null) langBase = new YamlConfiguration();

        File selectedFile = new File(plugin.getDataFolder(), "messages/origins/" + currentLang + ".yml");
        File baseFile = new File(plugin.getDataFolder(), "messages/origins/en_US.yml");

        boolean changedSelected = false;
        boolean changedBase = false;

        for (String key : jarLangBase.getKeys(true)) {
            Object value = jarLangBase.get(key);
            if (!langSelected.contains(key)) { langSelected.set(key, value); changedSelected = true; }
            if (!langBase.contains(key)) { langBase.set(key, value); changedBase = true; }
        }

        try {
            if (changedSelected) { selectedFile.getParentFile().mkdirs(); langSelected.save(selectedFile); }
            if (changedBase) { baseFile.getParentFile().mkdirs(); langBase.save(baseFile); }

            log.success("Synchronized language keys");
        } catch (IOException e) {
            log.severe("Failed to save language files during sync.");
        }
    }


    public String get(String key) {
        return getFromSources(key, getSources());
    }

    public List<String> getStringList(String key) {
        return asStringList(key, getSources());
    }

    public String formatKey(String key, Map<String, String> placeholders) {
        String raw = get(key);
        if (raw == null) return null;
        return formatRaw(raw, placeholders);
    }

    public String formatRaw(String raw, Map<String, String> placeholders) {
        if (raw == null) return null;
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                raw = raw.replace(e.getKey(), e.getValue());
            }
        }
        return raw;
    }


    private List<FileConfiguration> getSources() {
        List<FileConfiguration> sources = new ArrayList<FileConfiguration>();
        addIfNotEmpty(langCustomOverrides, sources);
        addIfNotEmpty(langSelected, sources);
        addIfNotEmpty(langBase, sources);
        return sources;
    }

    private void addIfNotEmpty(FileConfiguration config, List<FileConfiguration> list) {
        if (config == null || config.getKeys(true).isEmpty()) return;
        list.add(config);
    }

    private String getFromSources(String key, List<FileConfiguration> sources) {
        for (FileConfiguration source : sources) {
            if (source.contains(key)) return source.getString(key);
        }
        return null;
    }

    private List<String> asStringList(String key, List<FileConfiguration> sources) {
        for (FileConfiguration source : sources) {
            if (source.isList(key)) return source.getStringList(key);
        }
        String raw = getFromSources(key, sources);
        if (raw == null) return Collections.emptyList();
        if (!raw.contains("\n")) return Collections.singletonList(raw);
        return Arrays.asList(raw.split("\n"));
    }

    public String getCurrentLanguage() { return currentLang; }
}