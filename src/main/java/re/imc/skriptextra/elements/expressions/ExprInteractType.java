package re.imc.skriptextra.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import re.imc.skriptextra.adapt.EnumAdapter;

public class ExprInteractType extends SimpleExpression<Action> {
    static {
        Skript.registerExpression(ExprInteractType.class, Action.class, EnumAdapter.EVENT,
                "[event] interact type");
    }

    @Override
    protected Action @Nullable [] get(Event event) {
        if (event instanceof PlayerInteractEvent playerInteractEvent) {
            return new Action[]{playerInteractEvent.getAction()};
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Action> getReturnType() {
        return Action.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "interact type";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        return true;
    }
}
