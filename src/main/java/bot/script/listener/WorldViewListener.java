package bot.script.listener;

/** World-change callbacks off `WorldChanged`. The event carries no id in
 * this build, so the new world is read off the client. */
public interface WorldViewListener extends java.util.EventListener {
    /** The world changed (new world number). */
    default void onWorldChanged(int world) {
    }
}
