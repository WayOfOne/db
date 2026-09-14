package bot.script.listener;

/** Client-script callbacks off `ScriptPreFired`/`ScriptPostFired`. */
public interface RuneScriptListener extends java.util.EventListener {
    /** A client script started (script id). */
    default void onScriptPreFired(int scriptId) {
    }

    /** A client script finished (script id). */
    default void onScriptPostFired(int scriptId) {
    }
}
