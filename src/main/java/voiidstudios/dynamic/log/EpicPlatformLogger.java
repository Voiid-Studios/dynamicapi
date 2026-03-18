package voiidstudios.dynamic.log;

public interface EpicPlatformLogger {
    void log(EpicLogLevel level, String message);
    void log(EpicLogLevel level, String message, Throwable throwable);
}