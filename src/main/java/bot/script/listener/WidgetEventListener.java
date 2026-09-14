package bot.script.listener;

/** Interface callbacks off `WidgetLoaded`/`WidgetClosed`. DreamBot's
 * decoded-widget variant is client internals and stays out. */
public interface WidgetEventListener extends java.util.EventListener {
    /** An interface group loaded. */
    default void onWidgetLoaded(int groupId) {
    }

    /** An interface group closed. */
    default void onWidgetClosed(int groupId) {
    }
}
