package bot.script.listener;

/** Login callbacks off game-state edges. DreamBot's stage/response
 * variants key off its own login indices (no static source); edges on
 * the maintained state are the covered surface. */
public interface LoginListener extends java.util.EventListener {
    /** We entered the game. */
    default void onLogin() {
    }

    /** We left the game. */
    default void onLogout() {
    }
}
