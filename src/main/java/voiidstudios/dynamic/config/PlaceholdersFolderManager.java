package voiidstudios.dynamic.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import voiidstudios.dynamic.PlaceholderEntry;
import voiidstudios.dynamic.log.DAPILogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PlaceholdersFolderManager {
    private final JavaPlugin plugin;
    private final DAPILogger log;
    private File placeholdersFolder;

    public PlaceholdersFolderManager(JavaPlugin plugin, DAPILogger log) {
        this.plugin = plugin;
        this.log = log;
    }

    public void setup() {
        placeholdersFolder = new File(plugin.getDataFolder(), "placeholders");
        if (!placeholdersFolder.exists()) {
            placeholdersFolder.mkdirs();
            plugin.saveResource("placeholders/placeholders.yml", false);
            plugin.saveResource("placeholders/more_placeholders.yml", false);
        }
    }

    public List<PlaceholderEntry> loadAllEntries() {
        List<PlaceholderEntry> allEntries = new ArrayList<PlaceholderEntry>();

        File[] files = placeholdersFolder.listFiles();
        if (files == null) return allEntries;

        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;

            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = yml.getConfigurationSection("Placeholders");

            if (root == null) continue;

            int count = 0;
            for (String key : root.getKeys(false)) {
                ConfigurationSection sec = root.getConfigurationSection(key);
                if (sec == null) continue;

                boolean enabled = sec.getBoolean("enable", false);
                String prefix = sec.getString("prefix", "").toLowerCase();
                String pattern = sec.getString("pattern", key);
                boolean requiresPlayer = sec.getBoolean("requires_player", false);
                Map<String, String> context   = loadStringMap(sec.getConfigurationSection("context"));
                Map<String, String> variables = loadStringMap(sec.getConfigurationSection("variables"));
                String returns  = sec.getString("returns", "");
                String fallback = sec.getString("fallback", "");

                if (prefix.isEmpty()) {
                    log.warning("[" + file.getName() + "] Placeholder '" + key + "' has no 'prefix'. Skipping.");
                    continue;
                }

                allEntries.add(new PlaceholderEntry(key, enabled, prefix, pattern, requiresPlayer, context, variables, returns, fallback));
                count++;
            }

            if (count > 0) {
                log.success("Loaded " + count + " placeholder definition(s) from " + file.getName());
            }
        }

        return allEntries;
    }

    public boolean setEnabled(String id, boolean enabled) {
        File[] files = placeholdersFolder.listFiles();
        if (files == null) return false;

        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;

            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = yml.getConfigurationSection("Placeholders");
            if (root == null) continue;

            if (!root.contains(id)) continue;

            yml.set("Placeholders." + id + ".enable", enabled);
            try {
                yml.save(file);
                return true;
            } catch (IOException e) {
                log.severe("Failed to save " + file.getName() + " after toggling '" + id + "': " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    public List<String> getAllIds() {
        List<String> ids = new ArrayList<String>();
        File[] files = placeholdersFolder.listFiles();
        if (files == null) return ids;

        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = yml.getConfigurationSection("Placeholders");
            if (root == null) continue;
            ids.addAll(root.getKeys(false));
        }
        return ids;
    }

    private static Map<String, String> loadStringMap(ConfigurationSection sec) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (sec == null) return map;
        for (String k : sec.getKeys(false)) {
            String val = sec.getString(k);
            if (val != null) map.put(k, val);
        }
        return map;
    }
}