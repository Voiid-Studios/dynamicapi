package voiidstudios.dynamic.commands;

import org.bukkit.command.CommandSender;
import voiidstudios.dynamic.DynamicAPIPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DisableCommand implements CommandHandler {
    private final DynamicAPIPlugin plugin;

    public DisableCommand(DynamicAPIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.getMessagesManager().sendPrefixed(sender, "command.disable.usage");
            return;
        }

        String id = args[0];
        boolean found = plugin.getPlaceholdersFolderManager().setEnabled(id, false);

        if (!found) {
            plugin.getMessagesManager().sendPrefixed(sender, "command.disable.not_found", Collections.singletonMap("%ID%", id));
            return;
        }

        plugin.loadAndRegisterExpansions();
        plugin.getMessagesManager().sendPrefixed(sender, "command.disable.success", Collections.singletonMap("%ID%", id));
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
        return Collections.emptyList();
    }

    @Override public String getPermission() { return "dynamicapi.disable"; }
    @Override public String getName() { return "disable"; }
    @Override public String getDescription() { return "Disable a placeholder by ID"; }
}