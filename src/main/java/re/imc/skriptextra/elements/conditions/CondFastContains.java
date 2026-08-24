package re.imc.skriptextra.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

import java.util.Map;

public class CondFastContains extends Condition {

    static {
        Skript.registerCondition(CondFastContains.class,
                "%objects% fast contain[s] %objects%", "%objects% fast (doesn't|does not|do not|don't) contain[s] %objects%"
        );
    }


    private VariableString var;
    boolean isLocal;
    private Expression<?> containers;
    private Expression<?> items;

    @Override
    public boolean check(Event event) {

        if (this.var != null) {
            String var = this.var.toString(event).toLowerCase();
            Map<String, Object> varMap = (Map<String, Object>) getVariable(event, var, isLocal);
            Object[] all = items.getAll(event);
            boolean result = true;
            if (varMap == null) {
                result = false;
            } else {
                for (Object o : all) {
                    if (!varMap.containsValue(o)) {
                        result = false;
                        break;
                    }
                }
                if (all.length == 0) {
                    result = false;
                }
            }
            return isNegated() ? !result : result;
        }

        if (containers != null){
            Object[] containerValues = containers.getAll(event);
            return items.check(event, o1 -> {
                for (Object o2 : containerValues) {
                    if (Comparators.compare(o1, o2) == Relation.EQUAL)
                        return true;
                }
                return false;
            }, isNegated());
        }

        return false;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "fast contains";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        Expression<?> v = LiteralUtils.defendExpression(expressions[0]);
        if (v instanceof Variable varExpr && varExpr.isList()) {
            var = varExpr.getName();
            isLocal = varExpr.isLocal();
        } else {
            containers = v;
        }

        items = LiteralUtils.defendExpression(expressions[1]);
        this.setNegated(i == 1);
        return true;
    }

    private Object getVariable(Event e, String name, boolean isLocal) {
        final Object val = Variables.getVariable(name, e, isLocal);
        if (val == null) {
            return Variables.getVariable((isLocal ? Variable.LOCAL_VARIABLE_TOKEN : "") + name, e, false);
        }
        return val;
    }
}