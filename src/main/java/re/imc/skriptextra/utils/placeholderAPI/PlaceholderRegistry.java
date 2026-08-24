package re.imc.skriptextra.utils.placeholderAPI;

import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for managing custom placeholders.
 */
public class PlaceholderRegistry {

    private final Plugin plugin;
    private final Map<PlaceholderPlugin, PlaceholderRegister> registers = new HashMap<>();

    public PlaceholderRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a placeholder with an evaluator.
     * @param placeholderPlugin The placeholder plugin to use.
     * @param placeholder The placeholder name.
     * @param evaluator The evaluator for this placeholder.
     */
    public void registerPlaceholder(PlaceholderPlugin placeholderPlugin, String placeholder, PlaceholderEvaluator evaluator) {
        registers.computeIfAbsent(placeholderPlugin, PlaceholderRegister::new)
                .register(placeholder, evaluator);
    }

    /**
     * Unregister a placeholder evaluator.
     * @param placeholderPlugin The placeholder plugin.
     * @param placeholder The placeholder name.
     * @param evaluator The evaluator to remove.
     */
    public void unregisterPlaceholder(PlaceholderPlugin placeholderPlugin, String placeholder, PlaceholderEvaluator evaluator) {
        PlaceholderRegister register = registers.get(placeholderPlugin);
        if (register != null) {
            register.unregister(placeholder, evaluator);
            if (register.isEmpty()) {
                registers.remove(placeholderPlugin);
            }
        }
    }

    private final class PlaceholderRegister {

        private final PlaceholderPlugin placeholderPlugin;
        private final Map<String, PlaceholderListener> listeners = new HashMap<>();

        public PlaceholderRegister(PlaceholderPlugin placeholderPlugin) {
            this.placeholderPlugin = placeholderPlugin;
        }

        public void register(String placeholder, PlaceholderEvaluator evaluator) {
            listeners.computeIfAbsent(placeholder,
                    key -> placeholderPlugin.registerPlaceholder(PlaceholderRegistry.this.plugin, key))
                    .addEvaluator(evaluator);
        }

        public void unregister(String placeholder, PlaceholderEvaluator evaluator) {
            PlaceholderListener listener = listeners.get(placeholder);
            if (listener == null) {
                return;
            }
            listener.removeEvaluator(evaluator);
            if (!listener.hasEvaluators()) {
                listener.unregisterListener();
                listeners.remove(placeholder);
            }
        }

        public boolean isEmpty() {
            return listeners.isEmpty();
        }
    }
}
