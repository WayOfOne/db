package bot.script.listener;

/** Click callbacks. Same `MenuOptionClicked` source as `MenuRowListener`
 * (DreamBot kept both interfaces; the fork keeps both names). */
public interface ActionListener extends java.util.EventListener {
    /** A menu entry was clicked (option, target). */
    default void onAction(String option, String target) {
    }
}
