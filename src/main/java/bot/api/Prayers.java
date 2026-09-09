package bot.api;

import net.runelite.api.Prayer;
import net.runelite.api.widgets.WidgetInfo;

/** Prayer control. Quick-prayer toggles through the minimap orb; individual
 * state reads through {@link Vars}. Full per-prayer book clicking is deferred
 * (book widget ids want a live pass). */
public final class Prayers {
    private Prayers() {
    }

    public static boolean isActive(Prayer prayer) {
        return Vars.prayerActive(prayer);
    }

    /** Toggle quick-prayers via the minimap orb. Game-only. */
    public static boolean quickPrayer() throws Exception {
        return Actions.widget(Game.client().getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB));
    }
}
