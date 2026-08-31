package re.imc.skriptextra.utils;

import org.bukkit.configuration.file.FileConfiguration;
import re.imc.skriptextra.SkriptExtra;

import java.util.List;

public final class PluginConfig {
    private PluginConfig() {
    }

    private static FileConfiguration config() {
        return SkriptExtra.getInstance().getConfig();
    }

    public static boolean enabled(String path) {
        return config().getBoolean(path, true);
    }

    public static boolean bool(String path, boolean defaultValue) {
        return config().getBoolean(path, defaultValue);
    }

    public static int integer(String path, int defaultValue) {
        return config().getInt(path, defaultValue);
    }

    public static long longValue(String path, long defaultValue) {
        return config().getLong(path, defaultValue);
    }

    public static String string(String path, String defaultValue) {
        return config().getString(path, defaultValue);
    }

    public static List<String> strings(String path) {
        return config().getStringList(path);
    }

}
