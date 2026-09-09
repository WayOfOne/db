package bot.api;

import java.util.ArrayList;
import java.util.List;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.NPC;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Grand Exchange reads and collection. Slot state comes straight from the
 * client's offer array (8 slots); collecting clicks the game's own Collect
 * actions found by widget search, so no button ids are hardcoded. Creating
 * new buy/sell offers (search text, quantity, price widgets) is deferred —
 * that flow wants a live pass, not guesses. */
public final class GrandExchange {
    private GrandExchange() {
    }

    /** True while the GE interface is visible. */
    public static boolean isOpen() {
        Widget w = Game.client().getWidget(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER);
        return w != null && !w.isHidden();
    }

    /** All 8 offer slots, including empties. */
    public static GrandExchangeOffer[] offers() {
        GrandExchangeOffer[] offers = Game.client().getGrandExchangeOffers();
        return offers == null ? new GrandExchangeOffer[0] : offers;
    }

    /** Slots holding a live or finished offer. */
    public static List<GrandExchangeOffer> active() {
        List<GrandExchangeOffer> out = new ArrayList<>();
        for (GrandExchangeOffer o : offers()) {
            if (o != null && o.getState() != GrandExchangeOfferState.EMPTY) {
                out.add(o);
            }
        }
        return out;
    }

    /** True for finished offers (bought, sold, or cancelled). */
    public static boolean isDone(GrandExchangeOffer offer) {
        switch (offer.getState()) {
            case BOUGHT:
            case SOLD:
            case CANCELLED_BUY:
            case CANCELLED_SELL:
                return true;
            default:
                return false;
        }
    }

    /** Fill fraction 0.0-1.0 of a live offer. */
    public static double progress(GrandExchangeOffer offer) {
        int total = offer.getTotalQuantity();
        return total <= 0 ? 0.0 : (double) offer.getQuantitySold() / total;
    }

    /** Open the exchange via a clerk ("Exchange" first option). Game-only. */
    public static boolean openClerk(int... clerkIds) throws Exception {
        NPC clerk = Npcs.nearestWithin(10, clerkIds);
        return Actions.npc(clerk, "Exchange");
    }

    /** Click every visible Collect action (offer slots, collect screen).
     * @return actions clicked. Game-only. */
    public static int collectAll() throws Exception {
        List<Widget> buttons = Widgets.findAll(w -> {
            if (w.isHidden()) {
                return false;
            }
            String[] actions = w.getActions();
            if (actions == null) {
                return false;
            }
            for (String a : actions) {
                if ("Collect".equals(a)) {
                    return true;
                }
            }
            return false;
        });
        for (Widget w : buttons) {
            Actions.widget(w);
            Thread.sleep(400);
        }
        return buttons.size();
    }
}
