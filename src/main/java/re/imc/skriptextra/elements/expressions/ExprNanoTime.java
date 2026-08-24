package re.imc.skriptextra.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import java.lang.System;


public class ExprNanoTime extends SimpleExpression<Long> {
    static {
        Skript.registerExpression(ExprNanoTime.class, Long.class, ExpressionType.EVENT,
                "nano[]time");
    }

    @Override
    protected Long @Nullable [] get(Event event) {
        return new Long[]{System.nanoTime()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "nano time";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        return true;
    }
}