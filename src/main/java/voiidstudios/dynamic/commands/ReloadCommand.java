package voiidstudios.dynamic.commands;

import org.bukkit.command.CommandSender;
import voiidstudios.dynamic.DynamicAPIPlugin;

import java.util.Collections;
import java.util.List;

public final class ReloadCommand implements CommandHandler {
    private final DynamicAPIPlugin plugin;

    public ReloadCommand(DynamicAPIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.getMessagesManager().sendPrefixed(sender, "command.reload.process");

        plugin.getConfigManager().reload();
        plugin.getMessagesManager().sendPrefixed(sender, "command.reload.success_config");

        plugin.getMessagesManager().reload(plugin.getConfigManager().getLanguage());
        plugin.getMessagesManager().sendPrefixed(sender, "command.reload.success_messages");

        plugin.loadAndRegisterExpansions();

        int totalEnabled = 0;
        for (voiidstudios.dynamic.DynamicExpansion exp : plugin.getRegisteredExpansions()) {
            for (voiidstudios.dynamic.PlaceholderEntry entry : exp.getEntries()) {
                if (entry.enabled) totalEnabled++;
            }
        }

        java.util.Map<String, String> repl = new java.util.HashMap<String, String>();
        repl.put("%PLACEHOLDERS%", String.valueOf(totalEnabled));
        repl.put("%EXPANSIONS%", String.valueOf(plugin.getRegisteredExpansions().size()));
        plugin.getMessagesManager().sendPrefixed(sender, "command.reload.success_placeholders", repl);
    }

    @Override public List<String> tabComplete(CommandSender sender, String[] args) { return Collections.emptyList(); }
    @Override public String getPermission() { return "dynamicapi.reload"; }
    @Override public String getName() { return "reload"; }
    @Override public String getDescription() { return "Reload config and placeholders"; }
}