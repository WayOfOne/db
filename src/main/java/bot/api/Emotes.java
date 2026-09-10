package bot.api;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Emote book: open the tab, click an emote by its mined slot
 * (`results/emote-widget-table.txt`: container [216, 2] + per-emote child,
 * matching DreamBot's own `getEmoteChild`). Game-only unless noted. */
public final class Emotes {
    private Emotes() {
    }

    /** Open the emotes tab. Game-only. */
    public static boolean openTab() throws Exception {
        return Tabs.emotes();
    }

    /** The clickable widget for an emote (scroll-aware container lookup),
     * or null when the book is closed. */
    public static Widget emoteWidget(Emote emote) {
        if (emote == null) {
            return null;
        }
        Widget root = Widgets.child(WidgetIds.EMOTE_GROUP, WidgetIds.EMOTE_CONTAINER);
        return root == null ? null : root.getChild(emote.child);
    }

    /** Perform an emote (opens the tab first). Game-only. */
    public static boolean perform(Emote emote) throws Exception {
        openTab();
        return Actions.widget(emoteWidget(emote));
    }
}
