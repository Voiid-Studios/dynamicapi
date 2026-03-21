package voiidstudios.dynamic.commands;

import org.bukkit.command.CommandSender;
import voiidstudios.dynamic.DynamicAPIPlugin;
import voiidstudios.dynamic.commands.interfaces.BukkitCmdSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HelpCommand implements CommandHandler {
    private final DynamicAPIPlugin plugin;
    private final Map<String, CommandHandler> handlers;

    public HelpCommand(DynamicAPIPlugin plugin, Map<String, CommandHandler> handlers) {
        this.plugin = plugin;
        this.handlers = handlers;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Map<String, String> repl = new HashMap<String, String>();
        repl.put("%VERSION%", plugin.getDescription().getVersion());

        new BukkitCmdSender(sender).sendPrefixedMsg(plugin.getMessagesManager().get("command.help.header", repl));
        plugin.getMessagesManager().sendList(sender, "command.help.lines", repl);
    }

    @Override public List<String> tabComplete(CommandSender sender, String[] args) { return Collections.emptyList(); }
    @Override public String getPermission() { return "dynamicapi.help"; }
    @Override public String getName() { return "help"; }
    @Override public String getDescription() { return "Show this help message"; }
}