package voiidstudios.dynamic.log;

public class DAPILogger {
    private final EpicPlatformLogger logger;
    private final boolean color;
    private boolean debug;

    public DAPILogger(EpicPlatformLogger logger, boolean color) {
        this.logger = logger;
        this.color  = color;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }


    public void debug(String message) {
        if (!debug) return;
        log(EpicLogLevel.INFO, message);
    }

    public void debug(String message, Throwable thrown) {
        if (!debug) return;
        log(EpicLogLevel.WARNING, message, thrown);
    }

    public void debug(EpicLogLevel level, String message) {
        if (!debug) return;
        log(level, message);
    }


    public void info(String message) {
        log(EpicLogLevel.INFO, message);
    }

    public void info(String message, Throwable thrown) {
        log(EpicLogLevel.INFO, message, thrown);
    }
    
    public void success(String message) {
        log(EpicLogLevel.SUCCESS, message);
    }

    public void success(String message, Throwable thrown) {
        log(EpicLogLevel.SUCCESS, message, thrown);
    }

    public void failure(String message) {
        log(EpicLogLevel.FAILURE, message);
    }

    public void failure(String message, Throwable thrown) {
        log(EpicLogLevel.FAILURE, message, thrown);
    }

    public void warning(String message) {
        log(EpicLogLevel.WARNING, message);
    }

    public void warning(String message, Throwable thrown) {
        log(EpicLogLevel.WARNING, message, thrown);
    }

    public void severe(String message) {
        log(EpicLogLevel.SEVERE, message);
    }

    public void severe(String message, Throwable thrown) {
        log(EpicLogLevel.SEVERE, message, thrown);
    }


    private void log(EpicLogLevel level, String message) {
        logger.log(level, formatMessage(level, message));
    }

    private void log(EpicLogLevel level, String message, Throwable thrown) {
        logger.log(level, formatMessage(level, message), thrown);
    }

    private String formatMessage(EpicLogLevel level, String message) {
        if (color) {
            String levelColor;
            if (level == EpicLogLevel.SUCCESS) {
                levelColor = "[§a✓§r] ";
            } else if (level == EpicLogLevel.FAILURE) {
                levelColor = "[§cx§r] ";
            } else if (level == EpicLogLevel.WARNING) {
                levelColor = "§e";
            } else if (level == EpicLogLevel.SEVERE) {
                levelColor = "§c";
            } else {
                levelColor = "";
            }

            if (level == EpicLogLevel.WARNING || level == EpicLogLevel.SEVERE) {
                message = levelColor + message + "§r";
            } else {
                message = "§6[§aDynamicAPI§6] §r" + levelColor + message + "§r";
            }
        }
        return ANSIConverter.convertToAnsi(message);
    }
}