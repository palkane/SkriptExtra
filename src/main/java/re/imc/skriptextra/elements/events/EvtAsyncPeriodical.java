package re.imc.skriptextra.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ScheduledNoWorldEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

public class EvtAsyncPeriodical extends SkriptEvent {

    static {
        Skript.registerEvent(
                "*AsyncPeriodical",
                EvtAsyncPeriodical.class,
                ScheduledNoWorldEvent.class,
                "on async every %timespan%",
                "every %timespan% async"
        ).description("调用于异步执行的周期性事件。")
         .examples("on async every 5 seconds: # 在异步线程运行")
         .since("1.0");
    }

    private Timespan period;
    private BukkitTask task;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        period = ((Literal<Timespan>) args[0]).getSingle();
        return period != null;
    }

    @Override
    public boolean postLoad() {
        long ticks = Math.max(1L, period.getAs(Timespan.TimePeriod.TICK));
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(
                Skript.getInstance(),
                this::execute,
                ticks,
                ticks
        );
        return true;
    }

    @Override
    public void unload() {
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public boolean check(Event event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEventPrioritySupported() {
        return false;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "on async every " + period;
    }

    private void execute() {
        ScheduledNoWorldEvent event = new ScheduledNoWorldEvent();
        SkriptEventHandler.logEventStart(event);
        SkriptEventHandler.logTriggerStart(trigger);
        trigger.execute(event);
        SkriptEventHandler.logTriggerEnd(trigger);
        SkriptEventHandler.logEventEnd();
    }
}