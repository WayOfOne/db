package bot.api;

import java.awt.Point;

import net.runelite.api.widgets.Widget;

/** Player-to-player trade. Screen detection reads the title widgets both
 * trade stages show; accept/decline click the same buttons DreamBot's own
 * acceptTrade/declineTrade use (see {@link WidgetIds}). Game-only unless
 * noted. */
public final class Trade {
    private Trade() {
    }

    private static boolean stageVisible(int group, int titleChild) {
        Widget t = Widgets.child(group, titleChild);
        return t != null;
    }

    /** True while either trade screen is showing. */
    public static boolean isOpen() {
        return stageVisible(WidgetIds.TRADE_MAIN, WidgetIds.TRADE_TITLE_MAIN)
            || stageVisible(WidgetIds.TRADE_CONFIRM, WidgetIds.TRADE_TITLE_CONFIRM);
    }

    /** Accept on whichever stage is open (mirrors acceptTrade()'s fallback). */
    public static boolean accept() throws Exception {
        Widget main = Widgets.child(WidgetIds.TRADE_MAIN, WidgetIds.TRADE_ACCEPT_MAIN);
        if (Actions.widget(main)) {
            return true;
        }
        Widget confirm = Widgets.child(WidgetIds.TRADE_CONFIRM, WidgetIds.TRADE_ACCEPT_CONFIRM);
        return Actions.widget(confirm);
    }

    /** Decline on whichever stage is open. */
    public static boolean decline() throws Exception {
        Widget main = Widgets.child(WidgetIds.TRADE_MAIN, WidgetIds.TRADE_DECLINE);
        if (Actions.widget(main)) {
            return true;
        }
        Widget confirm = Widgets.child(WidgetIds.TRADE_CONFIRM, WidgetIds.TRADE_DECLINE);
        return Actions.widget(confirm);
    }

    /** Trade with the nearest player by name. Game-only. */
    public static boolean tradeWith(String name) throws Exception {
        if (Game.me() == null) {
            return false;
        }
        net.runelite.api.Player target = null;
        for (net.runelite.api.Player p : Players.all()) {
            if (p != null && name.equalsIgnoreCase(p.getName())) {
                target = p;
                break;
            }
        }
        if (target == null) {
            return false;
        }
        Actions.install(Actions.playerMenu(target, "Trade with"));
        Point at = Actions.actorScreen(target);
        if (at == null) {
            return false;
        }
        Actions.click(at);
        return true;
    }
}
