package re.imc.skriptextra.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.data.BukkitClasses;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public class ExtraClasses {
    public static void load() {
        try {
            Classes.registerClass(new ClassInfo<>(Action.class, "interactaction")
                    .user("interact ?actions?")
                    .name("Interact Action")
                    .description("Used in raw click event")
                    .parser(new Parser<>() {

                        @Override
                        public @Nullable Action parse(String s, ParseContext context) {
                            try {
                                return Action.valueOf(s.replace(" ", "_").toUpperCase());
                            } catch (Throwable t) {
                                return null;
                            }
                        }

                        @Override
                        public String toString(Action action, int i) {
                            return action.name().toLowerCase().replace("_", " ");
                        }

                        @Override
                        public String toVariableNameString(Action action) {
                            return action.name();
                        }
                    })
            );
        } catch (Throwable t) {}
    }
}
