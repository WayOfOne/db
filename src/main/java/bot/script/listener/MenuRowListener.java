package bot.script.listener;

import net.runelite.api.MenuEntry;

/** Menu callbacks. DreamBot's `ActionListener.onAction` shares the same
 * click source, so it funnels here too (documented on that interface). */
public interface MenuRowListener extends java.util.EventListener {
    /** A menu entry was built (inspect or modify before it shows). */
    default void onMenuEntryAdded(MenuEntry entry) {
    }

    /** A menu entry was clicked (option, target). */
    default void onMenuOptionClicked(String option, String target) {
    }
}
