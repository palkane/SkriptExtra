package re.imc.skriptextra.utils.placeholderAPI;

/**
 * Interface for managing placeholder listeners.
 */
interface PlaceholderListener {

    /**
     * Register this listener with the placeholder plugin.
     */
    void registerListener();

    /**
     * Unregister this listener from the placeholder plugin.
     */
    void unregisterListener();

    /**
     * Add an evaluator to this listener.
     * @param evaluator The evaluator to add.
     */
    void addEvaluator(PlaceholderEvaluator evaluator);

    /**
     * Remove an evaluator from this listener.
     * @param evaluator The evaluator to remove.
     */
    void removeEvaluator(PlaceholderEvaluator evaluator);

    /**
     * Check if this listener has any evaluators.
     * @return True if evaluators exist, false otherwise.
     */
    boolean hasEvaluators();

}
