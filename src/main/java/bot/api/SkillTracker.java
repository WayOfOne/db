package bot.api;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

import net.runelite.api.Skill;

/** XP-over-time tracking (DreamBot `SkillTracker` counterpart, which keeps
 * per-skill start XP and exposes gained rates). Pure logic — the clock and
 * XP source inject so offline tests drive it without the game. */
public final class SkillTracker {
    private final LongSupplier clockMs;
    private final IntSupplier xpNow;
    private long startMs;
    private int startXp;

    /** Track a live skill (game time + game XP). */
    public SkillTracker(Skill skill) {
        this(System::currentTimeMillis, () -> Skills.experience(skill));
    }

    /** Injectable clock/XP source (test seam; production uses the game). */
    public SkillTracker(LongSupplier clockMs, IntSupplier xpNow) {
        this.clockMs = clockMs;
        this.xpNow = xpNow;
        this.startMs = clockMs.getAsLong();
        this.startXp = xpNow.getAsInt();
    }

    /** Restart the window at current XP/now. */
    public void reset() {
        startMs = clockMs.getAsLong();
        startXp = xpNow.getAsInt();
    }

    /** XP gained since start (never negative across level resets). */
    public int gained() {
        return Math.max(0, xpNow.getAsInt() - startXp);
    }

    /** Elapsed tracking time in ms. */
    public long elapsedMs() {
        return Math.max(0, clockMs.getAsLong() - startMs);
    }

    /** Gained XP per hour (0 when no time has passed). */
    public double perHour() {
        long elapsed = elapsedMs();
        return elapsed <= 0 ? 0.0 : gained() * 3_600_000.0 / elapsed;
    }

    /** Millis until {@code targetXp} total at the current rate, or -1 when
     * no XP is being gained. */
    public long millisTo(int targetXp) {
        int remaining = targetXp - xpNow.getAsInt();
        if (remaining <= 0) {
            return 0;
        }
        double rate = perHour();
        return rate <= 0 ? -1 : (long) (remaining * 3_600_000.0 / rate);
    }
}
