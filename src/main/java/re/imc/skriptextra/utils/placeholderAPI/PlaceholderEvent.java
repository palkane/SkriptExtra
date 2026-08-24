package re.imc.skriptextra.utils.placeholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event for placeholder evaluation context.
 */
public class PlaceholderEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final @Nullable OfflinePlayer player;
    private final String placeholder;
    private final @Nullable String prefix;
    private final @Nullable String identifier;
    private @Nullable String result;

    public PlaceholderEvent(String placeholder, @Nullable OfflinePlayer player) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.placeholder = placeholder;
        
        int underscorePos = placeholder.indexOf("_");
        if (underscorePos != -1) {
            prefix = placeholder.substring(0, underscorePos);
            identifier = placeholder.substring(underscorePos + 1);
        } else {
            prefix = null;
            identifier = null;
        }
        
        this.player = player;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public @Nullable String getPrefix() {
        return prefix;
    }

    public @Nullable String getIdentifier() {
        return identifier;
    }

    public @Nullable OfflinePlayer getPlayer() {
        return player;
    }

    public void setResult(@Nullable String result) {
        this.result = result;
    }

    public @Nullable String getResult() {
        return result;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
