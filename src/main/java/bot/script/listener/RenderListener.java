package bot.script.listener;

/** Frame callbacks off `BeforeRender`. */
public interface RenderListener extends java.util.EventListener {
    /** Before the scene renders. */
    default void onRender() {
    }
}
