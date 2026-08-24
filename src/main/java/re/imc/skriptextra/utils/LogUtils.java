package re.imc.skriptextra.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;


public final class LogUtils {

    private static Logger logger;
    private static final String VERSION = "@VERSION@";

    public static void init(JavaPlugin p) {
        logger = p.getLogger();
    }

    public static void log(String message, Level level) {
        String prefix = "[SkriptExtra] ";
        logger.log(level, prefix + VERSION + " " + message);
    }

    public static void info(String message) {
        log(message, Level.INFO);
    }

    public static void warn(String message) {
        log(message, Level.WARNING);
    }

    public static void severe(String message) {
        log(message, Level.SEVERE);
    }
}