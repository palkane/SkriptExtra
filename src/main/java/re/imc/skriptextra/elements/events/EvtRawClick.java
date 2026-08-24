package re.imc.skriptextra.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public class EvtRawClick extends SkriptEvent {
    static {
        Class<? extends PlayerEvent>[] eventTypes = CollectionUtils.array(
                PlayerInteractEvent.class, PlayerInteractEntityEvent.class, PlayerInteractAtEntityEvent.class
        );
        Skript.registerEvent("Raw Click", EvtRawClick.class, eventTypes,
                        "raw click")
                .description("Purely click event")
                .examples("on raw click:")
                .since("1.0");
    }


    @Override
    public boolean init(Literal<?>[] literals, int i, SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(Event event) {
        return true;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "raw click";
    }
}
