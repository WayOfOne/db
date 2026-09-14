package bot.script.listener;

/** Varbit callbacks off `VarbitChanged`. Varp variants have no pinned
 * event source in this build and stay out. */
public interface VarListener extends java.util.EventListener {
    /** A varbit changed (id, new value). */
    default void onVarbitUpdate(int varbitId, int value) {
    }
}
