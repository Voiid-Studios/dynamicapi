package voiidstudios.dynamic.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import voiidstudios.dynamic.commands.interfaces.BukkitCmdSender;
import voiidstudios.dynamic.commands.interfaces.CmdSender;
import voiidstudios.dynamic.log.DAPILogger;

import java.util.List;
import java.util.Map;

public class MessagesManager {
    private final TranslationManager translations;

    public MessagesManager(JavaPlugin plugin, String language, DAPILogger log) {
        this.translations = new TranslationManager(plugin, log);
        this.translations.loadLanguage(language);
    }

    public void reload(String language) {
        translations.loadLanguage(language);
    }


    public String color(String msg) {
        if (msg == null) return "§cMissing message";
        return msg.replace("&", "§");
    }


    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, Map<String, String> repl) {
        String msg = translations.formatKey(key, repl);
        if (msg == null) msg = "§cMissing message: " + key;
        return color(msg);
    }


    public void send(CommandSender sender, String key) {
        send(sender, key, null);
    }

    public void send(CommandSender sender, String key, Map<String, String> repl) {
        sender.sendMessage(get(key, repl));
    }

    public void sendPrefixed(CommandSender sender, String key) {
        sendPrefixed(sender, key, null);
    }

    public void sendPrefixed(CommandSender sender, String key, Map<String, String> repl) {
        String msg = get(key, repl);
        new BukkitCmdSender(sender).sendPrefixedMsg(msg);
    }


    public void sendList(CommandSender sender, String key, Map<String, String> repl) {
        List<String> lines = translations.getStringList(key);
        if (lines.isEmpty()) return;
        for (String line : lines) {
            sender.sendMessage(color(translations.formatRaw(line, repl)));
        }
    }

    public void sendListPrefixed(CommandSender sender, String key, Map<String, String> repl) {
        List<String> lines = translations.getStringList(key);
        if (lines.isEmpty()) return;
        for (String line : lines) {
            String formatted = color(translations.formatRaw(line, repl));
            sender.sendMessage(color(CmdSender.prefix) + formatted);
        }
    }


    public void sendSection(CommandSender sender, String baseKey, Map<String, String> repl) {
        String headerRaw = translations.get(baseKey + ".header");
        if (headerRaw != null) {
            sender.sendMessage(color(translations.formatRaw(headerRaw, repl)));
        }
        List<String> lines = translations.getStringList(baseKey + ".lines");
        for (String line : lines) {
            sender.sendMessage(color(translations.formatRaw(line, repl)));
        }
    }


    public void console(String msg) {
        Bukkit.getConsoleSender().sendMessage(color(msg));
    }



    public String getListAsSingleString(String key, Map<String, String> repl) {
        List<String> lines = translations.getStringList(key);
        if (lines.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String line : lines) {
            String formatted = translations.formatRaw(line, repl);
            if (!first) sb.append("\n");
            sb.append(formatted);
            first = false;
        }
        return color(sb.toString());
    }

    public List<String> getList(String key) {
        return translations.getStringList(key);
    }

    public TranslationManager getTranslations() {
        return translations;
    }
}