package re.imc.skriptextra.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.skript.reflect.sections.SectionEvent;
import com.btk5h.skriptmirror.util.SkriptUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Registers Vessel's map-variable syntax when it has no conflicting provider. */
public final class MapVariablesFeature {
    private MapVariablesFeature() {
    }

    public static void register() {
        if (!PluginConfig.enabled("features.map-variables.enabled")) {
            return;
        }
        if (PluginConfig.bool("features.map-variables.disable-when-vessel-present", true)
                && Bukkit.getPluginManager().getPlugin("Vessel") != null) {
            LogUtils.info("检测到 Vessel，已禁用 map 变量功能以避免语法冲突");
            return;
        }
        boolean embeddedSections = PluginConfig.bool("features.map-variables.embedded-sections", true);
        if (embeddedSections && !Bukkit.getPluginManager().isPluginEnabled("skript-reflect")) {
            LogUtils.warn("未检测到 skript-reflect，map 变量功能未启用");
            return;
        }

        if (embeddedSections) {
            Skript.registerEffect(MapSectionEffect.class,
                    PluginConfig.string("features.map-variables.embedded-section-pattern", "sec[tion] <.+> [with [arguments] [variables] %-objects%]"));
        }
        Skript.registerSection(MapVariablesSection.class,
                PluginConfig.string("features.map-variables.pattern", "(set var[iable]s|map) %objects%"));
        LogUtils.info("已启用 map 变量功能");
    }

    public static final class MapVariablesSection extends Section {
        private final Set<MapSectionEffect> sections = new HashSet<>();
        private final Map<StringKey, Expression<?>> expressions = new HashMap<>();
        private boolean local;
        private Variable<?> variable;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean delay, SkriptParser.ParseResult parseResult,
                            SectionNode node, List<TriggerItem> triggerItems) {
            if (!(expressions[0] instanceof Variable<?> parsedVariable) || !parsedVariable.isList()) {
                return false;
            }
            variable = parsedVariable;
            local = variable.isLocal();
            return parseNodes(new StringKey(), node);
        }

        private boolean parseNodes(StringKey path, SectionNode sectionNode) {
            EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
            Set<String> entries = new HashSet<>();
            Set<String> sectionsToRemove = new HashSet<>();
            boolean array = false;
            int index = PluginConfig.integer("features.map-variables.array-start-index", 1);
            for (Node node : sectionNode) {
                String nodeText = node.getKey();
                if (nodeText == null) {
                    continue;
                }
                if (node instanceof SectionNode child) {
                    if (PluginConfig.bool("features.map-variables.embedded-sections", true)
                            && (child.getKey().startsWith("section ") || child.getKey().startsWith("sec "))) {
                        String effectText = child.getKey().endsWith(":") ? child.getKey().substring(0, child.getKey().length() - 1) : child.getKey();
                        MapSectionEffect effect = (MapSectionEffect) Effect.parse(effectText, "Can't understand this effect: " + effectText);
                        if (effect != null) {
                            effect.trigger = loadCode(child, "section", SectionEvent.class);
                            effect.parentKey = path.copy();
                            effect.parent = this;
                            effect.local = local;
                            sections.add(effect);
                            sectionsToRemove.add(nodeText);
                            continue;
                        }
                        Skript.warning("Invalid section effect: " + effectText + ", it's a key?");
                    }
                    builder.addSection(child.getKey(), true);
                    if (!parseNodes(path.copy().append(parseKey(child.getKey())).append(Variable.SEPARATOR), child)) {
                        return false;
                    }
                    continue;
                }
                if (nodeText.startsWith("- ")) {
                    expressions.put(path.copy().append(String.valueOf(index++)), parseValue(nodeText.substring(2)));
                    array = true;
                    continue;
                }
                String entry = nodeText.split(": ", 2)[0];
                entries.add(entry);
                builder.addEntryData(new FixedExpressionEntryData(entry));
            }
            if (array) {
                return true;
            }
            for (String section : sectionsToRemove) {
                sectionNode.remove(section);
            }
            EntryContainer container = builder.build().validate(sectionNode);
            if (container == null) {
                return false;
            }
            for (String entry : entries) {
                expressions.put(path.copy().append(parseKey(entry)), container.getOptional(entry, Expression.class, true));
            }
            return true;
        }

        @Override
        protected @Nullable TriggerItem walk(Event event) {
            String variableName = variable.getName().toString(event);
            String baseName = variableName.substring(0, variableName.length() - 3);
            if (PluginConfig.bool("features.map-variables.clear-before-map", false)) {
                Variables.setVariable(variableName, null, event, local);
            }
            for (Map.Entry<StringKey, Expression<?>> entry : expressions.entrySet()) {
                Object[] values = entry.getValue().getAll(event);
                String key = entry.getKey().get(event);
                if (values.length == 1) {
                    Variables.setVariable(baseName + Variable.SEPARATOR + key, values[0], event, local);
                } else {
                    for (int index = 0; index < values.length; index++) {
                        Variables.setVariable(baseName + Variable.SEPARATOR + key + Variable.SEPARATOR + (index + 1), values[index], event, local);
                    }
                }
            }
            for (MapSectionEffect section : sections) {
                section.execute(event);
            }
            return getNext();
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "map";
        }
    }

    public static final class MapSectionEffect extends Effect {
        private Trigger trigger;
        private StringKey parentKey;
        private MapVariablesSection parent;
        private boolean local;
        private List<Variable<?>> arguments;
        private StringKey key;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean delay, SkriptParser.ParseResult parseResult) {
            key = parseKey(parseResult.regexes.get(0).group());
            arguments = new ArrayList<>();
            Expression<?> argumentExpression = SkriptUtil.defendExpression(expressions[0]);
            if (argumentExpression == null) {
                return true;
            }
            if (argumentExpression instanceof ExpressionList<?> list) {
                for (Expression<?> expression : list.getExpressions()) {
                    if (!addArgument(expression)) {
                        return false;
                    }
                }
                return true;
            }
            return addArgument(argumentExpression);
        }

        private boolean addArgument(Expression<?> expression) {
            if (!(expression instanceof Variable<?> variable)) {
                Skript.error("The arguments can only contain variables");
                return false;
            }
            arguments.add(variable);
            return true;
        }

        @Override
        protected void execute(Event event) {
            String variableName = parent.variable.getName().toString(event);
            String baseName = variableName.substring(0, variableName.length() - 3);
            String sectionName = parentKey.copy().append(key).get(event);
            Variables.setVariable(baseName + Variable.SEPARATOR + sectionName,
                    new com.btk5h.skriptmirror.skript.reflect.sections.Section(trigger, event, arguments), event, local);
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "section";
        }
    }

    private static Expression<?> parseValue(String text) {
        return new SkriptParser(text, 3, ParseContext.DEFAULT).parseExpression(Number.class, ItemType.class, Object.class);
    }

    private static StringKey parseKey(String text) {
        if (!text.contains("%")) {
            return new StringKey().append(text);
        }
        String expressionText = text.startsWith("\"") ? text : "\"" + text + "\"";
        return new StringKey().append(new SkriptParser(expressionText, 3, ParseContext.DEFAULT)
                .parseExpression(Number.class, ItemType.class, Object.class));
    }

    private static final class FixedExpressionEntryData extends ExpressionEntryData<Object> {
        private FixedExpressionEntryData(String key) {
            super(key, null, true, Object.class);
        }

        @Override
        protected @Nullable Expression<?> getValue(String value) {
            return new SkriptParser(value, 3, ParseContext.DEFAULT).parseExpression(Number.class, Object.class);
        }
    }

    public static final class StringKey {
        private final List<Object> parts = new ArrayList<>();

        public StringKey append(String text) {
            parts.add(text);
            return this;
        }

        public StringKey append(Expression<?> expression) {
            parts.add(expression);
            return this;
        }

        public StringKey append(StringKey key) {
            parts.addAll(key.parts);
            return this;
        }

        public String get(Event event) {
            StringBuilder key = new StringBuilder();
            for (Object part : parts) {
                if (part instanceof String text) {
                    key.append(text);
                } else if (part instanceof Expression<?> expression) {
                    Object value = expression.getSingle(event);
                    if (value != null) {
                        key.append(value);
                    }
                }
            }
            return key.toString();
        }

        public StringKey copy() {
            StringKey copy = new StringKey();
            copy.parts.addAll(parts);
            return copy;
        }
    }
}
