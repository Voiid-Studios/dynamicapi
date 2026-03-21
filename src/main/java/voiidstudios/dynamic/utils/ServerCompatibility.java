package voiidstudios.dynamic.utils;

import org.bukkit.Bukkit;

public class ServerCompatibility {
    private static Boolean folia = null;

    public static boolean isFolia() {
        if (folia != null) return folia;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = Bukkit.getServer().getName().equalsIgnoreCase("Folia");
        }

        return folia;
    }
}