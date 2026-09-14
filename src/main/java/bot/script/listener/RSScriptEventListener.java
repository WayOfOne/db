package bot.script.listener;

/** Script-callback callbacks off `ScriptCallbackEvent`. */
public interface RSScriptEventListener extends java.util.EventListener {
    /** A script callback fired (event name). */
    default void onScriptCallback(String eventName) {
    }
}
