package voiidstudios.dynamic.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ConfigManager {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    private String language;
    private boolean updateNotification;
    private boolean autoUpdate;
    private boolean bstatsMetrics;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
        readValues();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        readValues();
    }

    private void readValues() {
        language = config.getString("Config.language", "en_US");
        updateNotification = config.getBoolean("Config.update_notification", true);
        autoUpdate = config.getBoolean("Config.auto_update", true);
        bstatsMetrics = config.getBoolean("Config.bstats_metrics", true);
    }

    public String getLanguage() { return language; }
    public boolean isUpdateNotification() { return updateNotification; }
    public boolean isAutoUpdate() { return autoUpdate; }
    public boolean isBstatsMetrics() { return bstatsMetrics; }
    public FileConfiguration getConfig() { return config; }
}