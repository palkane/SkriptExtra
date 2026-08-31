package re.imc.skriptextra.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/** Registers Vessel's symbolic multiple-condition syntax when Vessel is absent. */
public final class MultipleConditionsFeature {
    private MultipleConditionsFeature() {
    }

    public static void register() {
        if (!PluginConfig.enabled("features.conditions.multiple.enabled")) {
            return;
        }
        if (PluginConfig.bool("features.conditions.multiple.disable-when-vessel-present", true)
                && Bukkit.getPluginManager().getPlugin("Vessel") != null) {
            LogUtils.info("检测到 Vessel，已禁用多条件符号语法以避免冲突");
            return;
        }

        Skript.registerCondition(MultipleCondition.class,
                PluginConfig.string("features.conditions.multiple.and-pattern", "<.+> && <.+>"),
                PluginConfig.string("features.conditions.multiple.or-pattern", "<.+> \\|\\| <.+>"));
        LogUtils.info("已启用 && 和 || 多条件语法");
    }

    public static final class MultipleCondition extends Condition {
        private Condition first;
        private Condition second;
        private boolean and;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean delay,
                            SkriptParser.ParseResult parseResult) {
            String firstText = parseResult.regexes.get(0).group();
            String secondText = parseResult.regexes.get(1).group();
            first = Condition.parse(firstText, "Can't understand this condition: " + firstText);
            second = Condition.parse(secondText, "Can't understand this condition: " + secondText);
            and = matchedPattern == 0;
            return first != null && second != null;
        }

        @Override
        public boolean check(Event event) {
            return and ? first.check(event) && second.check(event) : first.check(event) || second.check(event);
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return first.toString(event, debug) + (and ? " && " : " || ") + second.toString(event, debug);
        }
    }
}
