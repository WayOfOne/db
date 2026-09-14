package bot.script.listener;

/** Tick callbacks. `onGameTick` mirrors DreamBot; `onClientTick` is the
 * fork's faster per-frame counterpart (no DreamBot equivalent). DreamBot's
 * onPreTick/onServerTick/preClientCycle have no pinned source and stay out. */
public interface GameTickListener extends java.util.EventListener {
    /** Every server game tick. */
    default void onGameTick() {
    }

    /** Every client frame. */
    default void onClientTick() {
    }
}
