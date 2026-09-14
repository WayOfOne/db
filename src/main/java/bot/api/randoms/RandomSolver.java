package bot.api.randoms;

/** One random-event handler. Mirrors DreamBot's own `RandomSolver` shape
 * (`shouldExecute` gate + `onLoop` work returning the next delay), minus
 * the paint/listener baggage scripts don't need. */
public interface RandomSolver {
    /** Event name (matches DreamBot's `RandomEvent` entries). */
    String name();

    /** True when this solver applies right now. */
    boolean shouldExecute() throws Exception;

    /** Do one unit of work; returns ms to wait before re-checking. */
    int onLoop() throws Exception;

    /** Whether the manager may run this solver. */
    boolean isEnabled();

    /** Enable/disable without unregistering. */
    void setEnabled(boolean on);
}
