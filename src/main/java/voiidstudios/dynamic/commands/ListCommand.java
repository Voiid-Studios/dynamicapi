package voiidstudios.dynamic.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import voiidstudios.dynamic.DynamicAPIPlugin;
import voiidstudios.dynamic.PlaceholderEntry;
import voiidstudios.dynamic.commands.interfaces.CmdSender;
import voiidstudios.dynamic.managers.MessagesManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ListCommand implements CommandHandler {
    private static final String DOCS_URL = "https://github.com/Voiid-Studios/dynamicapi/wiki";

    private final DynamicAPIPlugin plugin;

    public ListCommand(DynamicAPIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessagesManager msg = plugin.getMessagesManager();
        List<PlaceholderEntry> allEntries = plugin.getPlaceholdersFolderManager().loadAllEntries();

        int totalEnabled = 0;
        for (PlaceholderEntry entry : allEntries) {
            if (entry.enabled) totalEnabled++;
        }

        String hoverText = msg.getListAsSingleString("list.hint", null);

        TextComponent icon = new TextComponent("ℹ ");
        icon.setColor(ChatColor.AQUA);
        icon.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hoverText).create()
        ));
        icon.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,
                DOCS_URL
        ));

        Map<String, String> titleRepl = new HashMap<String, String>();
        titleRepl.put("%PLACEHOLDERS%", String.valueOf(totalEnabled));
        String titleColored = ChatColor.translateAlternateColorCodes('&', msg.get("list.title", titleRepl));
        String prefixColored = ChatColor.translateAlternateColorCodes('&', CmdSender.prefix);

        TextComponent line = new TextComponent(prefixColored);
        line.addExtra(icon);
        line.addExtra(new TextComponent(titleColored));

        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(line);
        } else {
            sender.sendMessage(prefixColored + titleColored);
        }

        String enabledLabel = msg.get("list.enabled");
        String disabledLabel = msg.get("list.disabled");

        if (allEntries.isEmpty()) {
            msg.send(sender, "list.no_placeholders");
        } else {
            for (PlaceholderEntry entry : allEntries) {
                Map<String, String> repl = new HashMap<String, String>();
                repl.put("%PREFIX%", entry.prefix);
                repl.put("%PATTERN%", entry.pattern);
                repl.put("%ID%", entry.id);
                repl.put("%STATUSCOLOR%", entry.enabled ? "\u00A7a" : "\u00A7c");
                repl.put("%STATUS%", entry.enabled ? enabledLabel : disabledLabel);
                msg.send(sender, "list.entry", repl);
            }
        }
    }

    @Override public List<String> tabComplete(CommandSender sender, String[] args) { return Collections.emptyList(); }
    @Override public String getPermission() { return "dynamicapi.list"; }
    @Override public String getName() { return "list"; }
    @Override public String getDescription() { return "List all active placeholders"; }
}