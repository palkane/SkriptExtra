package re.imc.skriptextra.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import re.imc.skriptextra.adapt.EnumAdapter;

public class ExprInteractSlot extends SimpleExpression<EquipmentSlot> {
    static {
        Skript.registerExpression(ExprInteractSlot.class, EquipmentSlot.class, EnumAdapter.EVENT,
                "interact slot [type]");
    }

    @Override
    protected EquipmentSlot @Nullable [] get(Event event) {
        if (event instanceof PlayerInteractEvent playerInteractEvent) {
            return new EquipmentSlot[]{playerInteractEvent.getHand()};
        }
        if (event instanceof PlayerInteractEntityEvent playerInteractEvent) {
            return new EquipmentSlot[]{playerInteractEvent.getHand()};
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends EquipmentSlot> getReturnType() {
        return EquipmentSlot.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "interact slot type";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        return true;
    }
}
