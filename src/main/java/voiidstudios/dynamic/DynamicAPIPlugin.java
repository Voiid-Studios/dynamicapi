package voiidstudios.dynamic;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import voiidstudios.dynamic.commands.*;
import voiidstudios.dynamic.config.ConfigManager;
import voiidstudios.dynamic.config.PlaceholdersFolderManager;
import voiidstudios.dynamic.listeners.PlayerListener;
import voiidstudios.dynamic.log.DAPILogger;
import voiidstudios.dynamic.log.JavaLoggerImpl;
import voiidstudios.dynamic.managers.MessagesManager;
import voiidstudios.dynamic.update.UpdateChecker;
import voiidstudios.dynamic.update.UpdateDownloaderGithub;
import voiidstudios.dynamic.utils.ServerCompatibility;
import voiidstudios.dynamic.update.UpdateCheckerResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class DynamicAPIPlugin extends JavaPlugin {
    public String version = getDescription().getVersion();
    public boolean isVerifiedVersion = false;

    private static final String DAPI_LOADED_PROPERTY = "dapi.jvm.loaded";

    private final String serverName = Bukkit.getServer().getName();
    private final String bukkitVersion = Bukkit.getBukkitVersion();
    private final String cleanVersion = bukkitVersion.split("-")[0];
    private final String serverId = Bukkit.getVersion();
    private final String cleanId = serverId.split("-", 2)[1].split(" ")[0];

    private final List<DynamicExpansion>      registeredExpansions = new ArrayList<DynamicExpansion>();
    private final Map<String, CommandHandler> commandHandlers      = new LinkedHashMap<String, CommandHandler>();

    private ConfigManager configManager;
    private PlaceholdersFolderManager placeholdersFolderManager;
    private MessagesManager  messagesManager;
    private UpdateChecker updateChecker;
    private UpdateDownloaderGithub updateDownloader;
    private DAPILogger dapiLogger;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();

        configManager = new ConfigManager(this);
        configManager.load();

        dapiLogger = new DAPILogger(new JavaLoggerImpl(getLogger()), true);

        messagesManager = new MessagesManager(this, configManager.getLanguage(), dapiLogger);

        messagesManager.console("&a  ____&2    _    ____ ___ ");
        messagesManager.console("&a |  _ \\&2  / \\  |  _ \\_ _|");
        messagesManager.console("&a | | | |&2/ _ \\ | |_) | |    &aDynamic&2API &bv" + version);
        messagesManager.console("&a | |_| &2/ ___ \\|  __/| |    &8Running on &f" + serverName + " (ID: " + cleanId + ", MC: " + cleanVersion + ")");
        messagesManager.console("&a |____&2/_/   \\_\\_|  |___|");
        messagesManager.console("");

        dateText();

        dapiLogger.process("Loading expansions...");

        placeholdersFolderManager = new PlaceholdersFolderManager(this, dapiLogger);
        placeholdersFolderManager.setup();

        registerCommandHandlers();
        loadAndRegisterExpansions();

        if (configManager.isBstatsMetrics()) {
            Metrics metrics = new Metrics(this, 30270);

            metrics.addCustomChart(new Metrics.SimplePie("pAPIVersion", new java.util.concurrent.Callable<String>() {
                public String call() {
                    org.bukkit.plugin.Plugin papi = getServer().getPluginManager().getPlugin("PlaceholderAPI");
                    return papi != null ? papi.getDescription().getVersion() : "unknown";
                }
            }));
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        if (getCommand("dynamicapi") != null) {
            getCommand("dynamicapi").setExecutor(this);
            getCommand("dynamicapi").setTabCompleter(this);
        }

        dapiLogger.success("§aDynamicAPI is ready!");

        updateChecker = new UpdateChecker(getDescription().getVersion(), dapiLogger);
        updateDownloader = new UpdateDownloaderGithub(dapiLogger, updateChecker);

        dapiLogger.process("Checking for updates...");

        if (ServerCompatibility.isFolia()) {
            try {
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                asyncScheduler.getClass().getMethod("runNow", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class).invoke(asyncScheduler, this, (java.util.function.Consumer<Object>) task -> {
                    checkUpdates(updateChecker.check());
                });
            } catch (Exception e) {
                dapiLogger.warning("Failed to schedule async task on Folia: " + e.getMessage());
            }
        } else {
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                public void run() {
                    checkUpdates(updateChecker.check());
                }
            });
        }

        if (Boolean.getBoolean(DAPI_LOADED_PROPERTY)) {
            sendConsoleUnstableReloadMessage();
        } else {
            System.setProperty(DAPI_LOADED_PROPERTY, "true");
        }
    }

    @Override
    public void onDisable() {
        dapiLogger.process("Unregistering the placeholders...");
        unregisterAll();
        dapiLogger.success("DynamicAPI disabled! Have a nice day ;)");
    }

    public void reload() {
        configManager.reload();
        messagesManager.reload(configManager.getLanguage());
        loadAndRegisterExpansions();
    }

    public void sendConsoleUnstableReloadMessage(){
        final String[] RELOAD_PREFIXES = {
            "WHAT ARE YOU DOING?!",
            "OH HELL NO.",
            "...seriously?",
            "bro.",
            "nope. nope. nope.",
            "have you tried NOT doing that?",
            "the council does not approve.",
            "skill issue.",
            "i am so tired of you.",
            "do you feel powerful? does this make you feel powerful?",
            "i will not stand for this.",
            "i will not tolerate such ingratitude.",
            "i love you very much, but this time you've really gone too far."
        };

        String prefix = RELOAD_PREFIXES[new Random().nextInt(RELOAD_PREFIXES.length)];
        dapiLogger.warning("│ ⚠  " + prefix);
        dapiLogger.warning("│ ");
        dapiLogger.warning("│ Server reload detected by DynamicAPI.");
        dapiLogger.warning("│ This action IS NOT SUPPORTED and may therefore BREAK YOUR PLACEHOLDERS!!!");
        dapiLogger.warning("│ ");
        dapiLogger.warning("│ YOU WILL GET NO SUPPORT FOR THE PLUGIN FOR ANY ISSUES YOU ENCOUNTER AFTER");
        dapiLogger.warning("│ THE SERVER RELOAD!");
        dapiLogger.warning("│ #RestartYourServerAndNeverReloadIt");
    }

    private void checkUpdates(UpdateCheckerResult result) {
        if (result.isError()) {
            if (configManager.isUpdateNotification()) {
                dapiLogger.failure("Failed to check for updates: " + result.getErrorMessage());
            }
            return;
        }

        String latest = result.getLatestVersion();
        

        if (latest == null) return;

        dapiLogger.pasiveWarning("Latest version found: §9v" + latest);
        dapiLogger.pasiveWarning("Current version of DAPI: §6v" + version);

        int comparison = compareVersions(version, latest);

        if (version.contains("+")) {
            dapiLogger.pasiveSevere("Using internal / testing version, skipping...");
            return;
        }

        if (comparison > 0) {
            dapiLogger.pasiveQuestion("...wait, you're running a version newer than the latest stable release?");
            dapiLogger.pasiveSevere("Either you're a time traveler, or something went very wrong. Skipping...");
            return;
        }

        isVerifiedVersion = true;

        if (configManager.isUpdateNotification() && !configManager.isAutoUpdate()) {
            dapiLogger.info("§b│ ");
            dapiLogger.info("§b│ ⚠  A stable update for DynamicAPI is available.");
            dapiLogger.info("§b│ ");
            dapiLogger.info("§b│ Latest version:  §f" + latest);
            dapiLogger.info("§b│ Current version: §f" + version);
            dapiLogger.info("§b│ ");
            dapiLogger.info("§b│ You can download it at:");
            dapiLogger.info("§b│ §fhttps://modrinth.com/plugin/dynamicapi");
            dapiLogger.info("§b│ ");
        }

        if (configManager.isAutoUpdate()) {
            dapiLogger.process("Auto-update enabled. §bDownloading " + latest + "...");
            updateDownloader.downloadUpdate();
        }
    }

    private void registerCommandHandlers() {
        register(new ReloadCommand(this));
        register(new ListCommand(this));
        register(new EnableCommand(this));
        register(new DisableCommand(this));
        register(new HelpCommand(this, commandHandlers));
    }

    private void register(CommandHandler handler) {
        commandHandlers.put(handler.getName(), handler);
    }

    public void loadAndRegisterExpansions() {
        unregisterAll();

        Map<String, List<PlaceholderEntry>> byPrefix = new LinkedHashMap<String, List<PlaceholderEntry>>();
        for (PlaceholderEntry entry : placeholdersFolderManager.loadAllEntries()) {
            if (!byPrefix.containsKey(entry.prefix))
                byPrefix.put(entry.prefix, new ArrayList<PlaceholderEntry>());
            byPrefix.get(entry.prefix).add(entry);
        }

        int totalEnabled = 0;
        String version = getDescription().getVersion();

        for (Map.Entry<String, List<PlaceholderEntry>> e : byPrefix.entrySet()) {
            List<PlaceholderEntry> entries = e.getValue();
            int enabledCount = 0;
            for (PlaceholderEntry en : entries) if (en.enabled) enabledCount++;
            if (enabledCount == 0) continue;

            DynamicExpansion expansion = new DynamicExpansion(version, e.getKey(), entries);
            expansion.register();
            registeredExpansions.add(expansion);
            totalEnabled += enabledCount;
            dapiLogger.success("Registered expansion '%" + e.getKey() + "%' with " + enabledCount + " placeholder(s).");
        }

        dapiLogger.success("§b" + totalEnabled + " placeholder(s) active across " + registeredExpansions.size() + " expansion(s).");
    }

    private void unregisterAll() {
        dapiLogger.success("Unregistered " + registeredExpansions.size() + " expansion(s).");
        for (PlaceholderExpansion exp : registeredExpansions) exp.unregister();
        registeredExpansions.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = (args.length == 0) ? "help" : args[0].toLowerCase();
        String[] subArgs = (args.length > 1) ? shiftArgs(args) : new String[0];

        CommandHandler handler = commandHandlers.get(sub);
        if (handler == null) {
            messagesManager.send(sender, "command.unknown");
            return true;
        }

        String perm = handler.getPermission();
        if (!perm.isEmpty() && !sender.hasPermission(perm)) {
            messagesManager.send(sender, "command.no_permissions");
            return true;
        }

        handler.execute(sender, subArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<String>();
            for (String name : commandHandlers.keySet()) {
                if (name.startsWith(args[0].toLowerCase())) result.add(name);
            }
            return result;
        }
        if (args.length > 1) {
            CommandHandler handler = commandHandlers.get(args[0].toLowerCase());
            if (handler != null) return handler.tabComplete(sender, shiftArgs(args));
        }
        return Collections.emptyList();
    }

    public List<DynamicExpansion> getRegisteredExpansions() { return registeredExpansions; }
    public MessagesManager getMessagesManager() { return messagesManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public UpdateChecker getUpdateChecker() { return updateChecker; }
    public DAPILogger getDapiLogger() { return dapiLogger; }
    public PlaceholdersFolderManager getPlaceholdersFolderManager() { return placeholdersFolderManager; }

    private static String[] shiftArgs(String[] args) {
        String[] shifted = new String[args.length - 1];
        System.arraycopy(args, 1, shifted, 0, args.length - 1);
        return shifted;
    }

    private int compareVersions(String v1, String v2) {
        try {
            String[] parts1 = v1.split("[.+\\-]");
            String[] parts2 = v2.split("[.+\\-]");
            int len = Math.max(parts1.length, parts2.length);
            for (int i = 0; i < len; i++) {
                int a = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
                int b = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
                if (a != b) return a - b;
            }
            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void dateText() { // totally useless, but cute :3
        LocalDate date = LocalDate.now();

        switch (date.getMonth()) {
            case JANUARY:
                if (date.getDayOfMonth() == 1) {
                    messagesManager.console("&eHappy New Year! <3");
                    messagesManager.console(""); return;
                }
                break;
            case FEBRUARY:
                if (date.getDayOfMonth() == 29) {
                    messagesManager.console("&eA leap year? owo");
                    messagesManager.console(""); return;
                }
                break;
            case MARCH:
                if (date.getDayOfMonth() == 13) {
                    messagesManager.console("&eHappy Birthday MaxxVoiid!");
                    messagesManager.console(""); return;
                }
                break;
            case APRIL:
                if (date.getDayOfMonth() == 1) {
                    messagesManager.console("&eApril Fools! Don't trust anything today >:3");
                    messagesManager.console(""); return;
                }
                break;
            case JUNE:
                messagesManager.console("&eHappy Pride Month!");
                messagesManager.console(""); return;
            case OCTOBER:
                if (date.getDayOfMonth() == 31) {
                    messagesManager.console("&eOoOohh, it's Halloween today >:3");
                    messagesManager.console(""); return;
                }
                break;
            case DECEMBER:
                if (date.getDayOfMonth() > 23 && date.getDayOfMonth() < 27) {
                    messagesManager.console("&eHo ho ho, Merry Christmas!");
                    messagesManager.console(""); return;
                }
                break;
            default:
                break;
        }

        int hour = LocalTime.now().getHour();
        if (hour >= 6 && hour < 12) {
            messagesManager.console("&eGood morning! Hope your server has a great day :)");
        } else if (hour >= 12 && hour < 19) {
            messagesManager.console("&eGood afternoon! Keep up the good work :D");
        } else if (hour >= 19 && hour < 22) {
            messagesManager.console("&eGood evening! Wrapping up for the day? :b");
        } else {
            messagesManager.console("&eLate night gaming session? Don't forget to sleep! :p");
        }
        messagesManager.console("");
    }
}