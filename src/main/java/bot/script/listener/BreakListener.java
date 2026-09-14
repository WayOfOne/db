package bot.script.listener;

/** Break callbacks, fired by `bot.api.randoms.BreakSolver` around the
 * logout/rest/re-login cycle (DreamBot's `BreakEvent` shape, without the
 * payload: start and end only). */
public interface BreakListener extends java.util.EventListener {
    /** A scheduled break started. */
    default void onBreakStart() {
    }

    /** A scheduled break ended. */
    default void onBreakEnd() {
    }
}
