package voiidstudios.dynamic.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import voiidstudios.dynamic.DynamicAPIPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerListener implements Listener {
    private final DynamicAPIPlugin plugin;

    public PlayerListener(DynamicAPIPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getConfigManager().isUpdateNotification()) return;
        if (!plugin.isVerifiedVersion) return;
        if (!player.isOp() && !player.hasPermission("dynamicapi.admin")) return;

        String latestVersion = plugin.getUpdateChecker().getLatestVersion();
        if (latestVersion == null) return;
        if (latestVersion.equalsIgnoreCase(plugin.getDescription().getVersion())) return;

        Map<String, String> repl = new HashMap<String, String>();
        repl.put("%LATEST%", latestVersion);
        repl.put("%CURRENT%", plugin.version);
        repl.put("%UPDATELINK%", "https://modrinth.com/plugin/dynamicapi");

        plugin.getMessagesManager().sendListPrefixed(player, "system.update.available", repl);
    }
}