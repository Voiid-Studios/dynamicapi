package voiidstudios.dynamic.commands;

import org.bukkit.command.CommandSender;
import voiidstudios.dynamic.DynamicAPIPlugin;

import java.util.ArrayList;
import java.util.List;

public final class EnableCommand implements CommandHandler {
    private final DynamicAPIPlugin plugin;

    public EnableCommand(DynamicAPIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.getMessagesManager().sendPrefixed(sender, "command.enable.usage");
            return;
        }

        String id = args[0];
        boolean found = plugin.getPlaceholdersFolderManager().setEnabled(id, true);

        if (!found) {
            plugin.getMessagesManager().sendPrefixed(sender, "command.enable.not_found", java.util.Collections.singletonMap("%ID%", id));
            return;
        }

        plugin.loadAndRegisterExpansions();
        plugin.getMessagesManager().sendPrefixed(sender, "command.enable.success", java.util.Collections.singletonMap("%ID%", id));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> ids = plugin.getPlaceholdersFolderManager().getAllIds();
            List<String> result = new ArrayList<String>();
            for (String id : ids) {
                if (id.startsWith(args[0].toLowerCase())) result.add(id);
            }
            return result;
        }
        return java.util.Collections.emptyList();
    }

    @Override public String getPermission() { return "dynamicapi.enable"; }
    @Override public String getName() { return "enable"; }
    @Override public String getDescription() { return "Enable a placeholder by ID"; }
}