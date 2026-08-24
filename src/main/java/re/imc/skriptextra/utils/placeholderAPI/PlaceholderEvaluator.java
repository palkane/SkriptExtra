package re.imc.skriptextra.utils.placeholderAPI;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Placeholder evaluator interface for evaluating custom placeholders.
 */
public interface PlaceholderEvaluator {

    /**
     * Evaluate a placeholder with optional player context.
     * @param placeholder The placeholder to evaluate.
     * @param player The player context (can be null).
     * @return The evaluated value, or null if evaluation failed.
     */
    @Nullable
    String evaluate(String placeholder, @Nullable OfflinePlayer player);

    /**
     * Evaluate a relational placeholder between two players.
     * @param placeholder The placeholder to evaluate.
     * @param one First player.
     * @param two Second player.
     * @return The evaluated value, or null if evaluation failed.
     */
    @Nullable
    String evaluateRelational(String placeholder, Player one, Player two);

}
