package re.imc.skriptextra.utils.placeholderAPI;

import ch.njol.skript.Skript;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Enum for supported placeholder plugins.
 */
public enum PlaceholderPlugin {

    PLACEHOLDER_API("PlaceholderAPI", Skript.classExists("me.clip.placeholderapi.expansion.PlaceholderExpansion")) {
        private final char[] illegalCharacters = new char[]{'%', '{', '}', '_'};

        @Override
        public @Nullable String validatePrefix(String prefix) {
            if (StringUtils.isBlank(prefix)) {
                return "A prefix cannot be blank";
            }
            for (char character : prefix.toCharArray()) {
                for (char illegalCharacter : illegalCharacters) {
                    if (character == illegalCharacter) {
                        return getDisplayName() + " prefixes cannot contain the character '" + character + "'";
                    }
                }
            }
            return null;
        }

        @Override
        PlaceholderListener registerPlaceholder(Plugin plugin, String placeholder) {
            PlaceholderListener listener = new PlaceholderAPIListener(plugin, placeholder);
            listener.registerListener();
            return listener;
        }

        @Override
        public @Nullable String parsePlaceholder(String placeholder, @Nullable OfflinePlayer player) {
            if (placeholder.indexOf('%') == -1) {
                placeholder = "%" + placeholder + "%";
            }
            String value = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, placeholder);
            if (value.isEmpty() || value.equalsIgnoreCase(placeholder)) {
                return null;
            }
            return value;
        }

        @Override
        public boolean supportsRelationalPlaceholders() {
            return true;
        }

        @Override
        public @Nullable String parseRelationalPlaceholder(String placeholder, Player one, Player two) {
            if (placeholder.indexOf('%') == -1) {
                placeholder = "%" + placeholder + "%";
            }
            String value = me.clip.placeholderapi.PlaceholderAPI.setRelationalPlaceholders(one, two, placeholder);
            if (value.isEmpty() || value.equalsIgnoreCase(placeholder)) {
                return null;
            }
            return value;
        }
    };

    private static final Collection<PlaceholderPlugin> INSTALLED_PLUGINS = Arrays.stream(values())
            .filter(PlaceholderPlugin::isInstalled)
            .collect(Collectors.toSet());

    /**
     * Get all installed placeholder plugins.
     * @return Collection of installed plugins.
     */
    public static Collection<PlaceholderPlugin> getInstalledPlugins() {
        return INSTALLED_PLUGINS;
    }

    private final String displayName;
    private final boolean installed;

    PlaceholderPlugin(String displayName, boolean installed) {
        this.displayName = displayName;
        this.installed = installed;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final boolean isInstalled() {
        return installed;
    }

    /**
     * Validate a placeholder prefix.
     * @param prefix The prefix to validate.
     * @return Null if valid, error message if invalid.
     */
    public abstract @Nullable String validatePrefix(String prefix);

    /**
     * Register a new placeholder.
     * @param plugin The plugin registering the placeholder.
     * @param placeholder The placeholder name.
     * @return The registered listener.
     */
    abstract PlaceholderListener registerPlaceholder(Plugin plugin, String placeholder);

    /**
     * Parse a placeholder value.
     * @param placeholder The placeholder string.
     * @param player The player context.
     * @return The parsed value, or null.
     */
    public abstract @Nullable String parsePlaceholder(String placeholder, @Nullable OfflinePlayer player);

    /**
     * Check if relational placeholders are supported.
     * @return True if supported, false otherwise.
     */
    public abstract boolean supportsRelationalPlaceholders();

    public @Nullable String parseRelationalPlaceholder(String placeholder, Player one, Player two) {
        if (!supportsRelationalPlaceholders()) {
            throw new UnsupportedOperationException(getDisplayName() + " does not support relational placeholders.");
        }
        throw new RuntimeException(getDisplayName() + " is missing relational placeholder implementation.");
    }

}
