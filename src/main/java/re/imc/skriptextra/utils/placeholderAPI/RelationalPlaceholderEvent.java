package re.imc.skriptextra.utils.placeholderAPI;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Event for relational placeholder evaluation context.
 */
public class RelationalPlaceholderEvent extends PlaceholderEvent {

    private final Player other;

    public RelationalPlaceholderEvent(String placeholder, Player player, Player other) {
        super(placeholder, player);
        this.other = other;
    }

    @Override
    public @NotNull Player getPlayer() {
        //noinspection ConstantConditions
        return super.getPlayer().getPlayer();
    }

    public Player getOther() {
        return other;
    }

}
