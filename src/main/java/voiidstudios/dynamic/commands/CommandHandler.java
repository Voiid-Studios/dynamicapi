package voiidstudios.dynamic.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandHandler {
    /**
     * @param sender
     * @param args
     */
    void execute(CommandSender sender, String[] args);

    /**
     * @param args
     */
    List<String> tabComplete(CommandSender sender, String[] args);

    String getPermission();
    String getName();
    String getDescription();
}