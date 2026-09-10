package bot.api;

import net.runelite.api.Prayer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Prayer control. Quick-prayer toggles through the minimap orb; individual
 * prayers toggle through the book widget each DreamBot `Prayer` constant
 * addresses as group 541 + its child index
 * (`results/prayer-widget-table.txt`; parsed out of DreamBot's own
 * `Prayer` enum, levels cross-checked). Game-only unless noted. */
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

    /** Prayer-book widget child for a standard-book prayer (group 541),
     * or -1 for null / Ruinous Powers (separate book, not mined). */
    public static int bookChild(Prayer prayer) {
        if (prayer == null) {
            return -1;
        }
        return switch (prayer) {
            case THICK_SKIN -> 9;
            case BURST_OF_STRENGTH -> 10;
            case CLARITY_OF_THOUGHT -> 11;
            case SHARP_EYE -> 27;
            case MYSTIC_WILL -> 30;
            case ROCK_SKIN -> 12;
            case SUPERHUMAN_STRENGTH -> 13;
            case IMPROVED_REFLEXES -> 14;
            case RAPID_RESTORE -> 15;
            case RAPID_HEAL -> 16;
            case PROTECT_ITEM -> 17;
            case HAWK_EYE -> 28;
            case MYSTIC_LORE -> 31;
            case STEEL_SKIN -> 18;
            case ULTIMATE_STRENGTH -> 19;
            case INCREDIBLE_REFLEXES -> 20;
            case PROTECT_FROM_MAGIC -> 21;
            case PROTECT_FROM_MISSILES -> 22;
            case PROTECT_FROM_MELEE -> 23;
            case EAGLE_EYE, DEADEYE -> 29;
            case MYSTIC_MIGHT, MYSTIC_VIGOUR -> 32;
            case RETRIBUTION -> 24;
            case REDEMPTION -> 25;
            case SMITE -> 26;
            case PRESERVE -> 37;
            case CHIVALRY -> 34;
            case PIETY -> 35;
            case RIGOUR -> 33;
            case AUGURY -> 36;
            default -> -1;
        };
    }

    /** Click a prayer in the book (opens the tab first). Game-only. */
    public static boolean toggle(Prayer prayer) throws Exception {
        int child = bookChild(prayer);
        if (child < 0) {
            return false;
        }
        Tabs.prayer();
        return Actions.widget(Widgets.child(WidgetIds.PRAYER_BOOK_GROUP, child));
    }

    /** Ensure a prayer is on (no-op when already active). Game-only. */
    public static boolean activate(Prayer prayer) throws Exception {
        return isActive(prayer) || toggle(prayer);
    }

    /** Quick-prayer setup slot for a prayer (group 77, child 4), or -1.
     * Children mined from DreamBot's `Prayer` enum
     * (`results/prayer-widget-table.txt`); the root matches the pinned
     * `QUICK_PRAYER_PRAYERS`. */
    public static int quickChild(Prayer prayer) {
        if (prayer == null) {
            return -1;
        }
        return switch (prayer) {
            case THICK_SKIN -> 0;
            case BURST_OF_STRENGTH -> 1;
            case CLARITY_OF_THOUGHT -> 2;
            case SHARP_EYE -> 18;
            case MYSTIC_WILL -> 19;
            case ROCK_SKIN -> 3;
            case SUPERHUMAN_STRENGTH -> 4;
            case IMPROVED_REFLEXES -> 5;
            case RAPID_RESTORE -> 6;
            case RAPID_HEAL -> 7;
            case PROTECT_ITEM -> 8;
            case HAWK_EYE -> 20;
            case MYSTIC_LORE -> 21;
            case STEEL_SKIN -> 9;
            case ULTIMATE_STRENGTH -> 10;
            case INCREDIBLE_REFLEXES -> 11;
            case PROTECT_FROM_MAGIC -> 12;
            case PROTECT_FROM_MISSILES -> 13;
            case PROTECT_FROM_MELEE -> 14;
            case EAGLE_EYE, DEADEYE -> 22;
            case MYSTIC_MIGHT, MYSTIC_VIGOUR -> 23;
            case RETRIBUTION -> 15;
            case REDEMPTION -> 16;
            case SMITE -> 17;
            case PRESERVE -> 28;
            case CHIVALRY -> 25;
            case PIETY -> 26;
            case RIGOUR -> 24;
            case AUGURY -> 27;
            default -> -1;
        };
    }

    /** Tick a prayer in the quick-prayer setup interface. Game-only. */
    public static boolean selectQuick(Prayer prayer) throws Exception {
        int quick = quickChild(prayer);
        if (quick < 0) {
            return false;
        }
        Widget root = Widgets.child(WidgetIds.QUICK_PRAYER_GROUP, WidgetIds.QUICK_PRAYER_CHILD);
        return Actions.widget(root == null ? null : root.getChild(quick));
    }
}
