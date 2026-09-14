package bot.api;

import net.runelite.api.widgets.Widget;

/** Quest reads. Quest points are a one-line varp-101 read in DreamBot's own
 * `Quests` (`results/javap-c-db-niche2.txt`:198). Per-quest progress comes
 * from the mined `Quest` table (`results/quest-table.txt`): raw varp/varbit
 * values plus journal row colors at (399, 7, grandchild). Interpreting
 * values as started/finished needs one live calibration (see the table
 * doc) — until then this exposes the reads, not the verdicts. Pure reads
 * unless noted. */
public final class Quests {
    private Quests() {
    }

    /** Quest points (varp 101). */
    public static int questPoints() {
        return Vars.varp(WidgetIds.QUEST_POINTS_VARP);
    }

    /** Raw progress value: varbit when the quest has one, else its varp,
     * else -1. */
    public static int settingValue(Quest quest) {
        if (quest == null) {
            return -1;
        }
        if (quest.varbitId >= 0) {
            return Vars.varbit(quest.varbitId);
        }
        if (quest.configId >= 0) {
            return Vars.varp(quest.configId);
        }
        return -1;
    }

    /** Journal row widget, or null when the quest tab is closed. */
    public static Widget rowWidget(Quest quest) {
        if (quest == null) {
            return null;
        }
        Widget list = Widgets.child(WidgetIds.QUEST_GROUP, WidgetIds.QUEST_LIST);
        return list == null ? null : list.getChild(quest.grandchild);
    }

    /** Journal row text color, or -1 when unavailable. Calibrate once live:
     * note the colors of red/yellow/green rows, then map them to states. */
    public static int rowColor(Quest quest) {
        Widget row = rowWidget(quest);
        return row == null ? -1 : row.getTextColor();
    }

    /** Open the quest tab. Game-only. */
    public static boolean openTab() throws Exception {
        return Tabs.quests();
    }
}
