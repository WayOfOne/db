package bot.api;

import net.runelite.api.widgets.WidgetInfo;

/** Top-level tab buttons. Pass an explicit {@link WidgetInfo} for layouts the
 * named helpers don't cover; every constant is compiler-checked against the
 * pinned API, so a renamed widget fails the build instead of the run. */
public final class Tabs {
    private Tabs() {
    }

    public static boolean open(WidgetInfo tab) throws Exception {
        return Actions.widget(Game.client().getWidget(tab));
    }

    public static boolean combat() throws Exception {
        return Actions.widget(Widgets.first(WidgetInfo.FIXED_VIEWPORT_COMBAT_TAB));
    }

    public static boolean stats() throws Exception {
        return Actions.widget(Widgets.first(WidgetInfo.FIXED_VIEWPORT_STATS_TAB));
    }

    public static boolean quests() throws Exception {
        return Actions.widget(Widgets.first(WidgetInfo.FIXED_VIEWPORT_QUESTS_TAB));
    }

    public static boolean inventory() throws Exception {
        WidgetInfo tab = Widgets.first(WidgetInfo.FIXED_VIEWPORT_INVENTORY_TAB) != null
            ? WidgetInfo.FIXED_VIEWPORT_INVENTORY_TAB
            : WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB;
        return open(tab);
    }

    public static boolean equipment() throws Exception {
        return Actions.widget(Widgets.first(WidgetInfo.FIXED_VIEWPORT_EQUIPMENT_TAB));
    }

    public static boolean prayer() throws Exception {
        WidgetInfo tab = Widgets.first(WidgetInfo.FIXED_VIEWPORT_PRAYER_TAB) != null
            ? WidgetInfo.FIXED_VIEWPORT_PRAYER_TAB
            : WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_PRAYER_TAB;
        return open(tab);
    }

    public static boolean magic() throws Exception {
        return Actions.widget(Widgets.first(WidgetInfo.FIXED_VIEWPORT_MAGIC_TAB));
    }
}
