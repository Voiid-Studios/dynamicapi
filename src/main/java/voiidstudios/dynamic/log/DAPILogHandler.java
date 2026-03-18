package voiidstudios.dynamic.log;

import org.bukkit.Bukkit;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DAPILogHandler extends Handler {

    public static void install(Logger pluginLogger) {
        for (Handler h : pluginLogger.getHandlers().clone()) {
            pluginLogger.removeHandler(h);
        }
        pluginLogger.setUseParentHandlers(false);
        pluginLogger.addHandler(new DAPILogHandler());
    }

    public static void uninstall(Logger pluginLogger) {
        for (Handler h : pluginLogger.getHandlers().clone()) {
            if (h instanceof DAPILogHandler) {
                pluginLogger.removeHandler(h);
            }
        }
        pluginLogger.setUseParentHandlers(true);
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null || record.getMessage() == null) return;
        Bukkit.getConsoleSender().sendMessage(record.getMessage());
    }

    @Override public void flush() {}
    @Override public void close() throws SecurityException {}
}