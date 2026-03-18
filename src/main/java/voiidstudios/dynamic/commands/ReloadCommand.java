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
        plugin.reload();
        plugin.getMessagesManager().sendPrefixed(sender, "command.reload");
    }

    @Override public List<String> tabComplete(CommandSender sender, String[] args) { return Collections.emptyList(); }
    @Override public String getPermission() { return "dynamicapi.reload"; }
    @Override public String getName() { return "reload"; }
    @Override public String getDescription() { return "Reload config and placeholders"; }
}