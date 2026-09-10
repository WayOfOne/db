package bot.api;

/** Quest reads. Quest points are a one-line varp-101 read in DreamBot's own
 * `Quests` (`results/javap-c-db-niche2.txt`:198). Per-quest states are NOT
 * implemented: DreamBot derives them from quest-tab text colors through a
 * runtime-decrypted color map (`Quest$State.getForID`), so no static
 * mapping exists to mine — identifying the colors needs one live pass.
 * Pure reads. */
public final class Quests {
    private Quests() {
    }

    /** Quest points (varp 101). */
    public static int questPoints() {
        return Vars.varp(WidgetIds.QUEST_POINTS_VARP);
    }

    /** Open the quest tab. Game-only. */
    public static boolean openTab() throws Exception {
        return Tabs.quests();
    }
}
