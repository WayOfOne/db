package bot.api;

import java.util.ArrayList;
import java.util.List;

import bot.util.Sleep;
import bot.util.Timing;
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

    /** Offer-slot root widget (group 465, child 7+slot). Game-only. */
    public static Widget slotWidget(int slot) {
        return Widgets.child(WidgetIds.GE_GROUP, WidgetIds.GE_SLOT_BASE + slot);
    }

    /** Open the buy screen for a slot. Game-only. */
    public static boolean openBuyScreen(int slot) throws Exception {
        Widget root = slotWidget(slot);
        if (root == null) {
            return false;
        }
        Widget buy = root.getChild(WidgetIds.GE_BUY_CHILD);
        return Actions.widget(buy);
    }

    /** Open the sell screen for a slot. Game-only. */
    public static boolean openSellScreen(int slot) throws Exception {
        Widget root = slotWidget(slot);
        if (root == null) {
            return false;
        }
        Widget sell = root.getChild(WidgetIds.GE_SELL_CHILD);
        return Actions.widget(sell);
    }

    /** Search box on the buy screen (chatbox input). Game-only. */
    public static Widget searchBox() {
        return Widgets.child(WidgetIds.SEARCH_GROUP, WidgetIds.SEARCH_BOX);
    }

    /** Type an item name into the open search box and pick the first result
     * whose text contains it. Game-only. */
    public static boolean searchItem(String name) throws Exception {
        Widget box = searchBox();
        if (box == null || !Actions.widget(box)) {
            return false;
        }
        Sleep.sleep(400, 800);
        Keyboard.type(name);
        Keyboard.pressEnter();
        boolean found = Timing.waitCondition(
            () -> Widgets.child(WidgetIds.SEARCH_GROUP, WidgetIds.SEARCH_RESULTS) != null, 5000);
        if (!found) {
            return false;
        }
        Widget[] results = Widgets.childrenOf(WidgetIds.SEARCH_GROUP, WidgetIds.SEARCH_RESULTS);
        for (Widget r : results) {
            String text = r.getText();
            if (text != null && text.toLowerCase(java.util.Locale.ROOT)
                    .contains(name.toLowerCase(java.util.Locale.ROOT))) {
                return Actions.widget(r);
            }
        }
        return false;
    }

    private static boolean clickQtyPriceChild(int child) throws Exception {
        Widget root = Widgets.child(WidgetIds.GE_GROUP, WidgetIds.GE_QTY_PRICE);
        if (root == null) {
            return false;
        }
        return Actions.widget(root.getChild(child));
    }

    /** Set quantity via the enter-quantity button (qty <= 0 clicks All).
     * Types into the chatbox prompt. Game-only. */
    public static boolean setQuantity(int qty) throws Exception {
        boolean ok = qty <= 0
            ? clickQtyPriceChild(WidgetIds.GE_ALL_CHILD)
            : clickQtyPriceChild(WidgetIds.GE_QTY_CHILD);
        if (!ok) {
            return false;
        }
        Sleep.sleep(400, 800);
        if (qty > 0) {
            Keyboard.type(String.valueOf(qty));
            Keyboard.pressEnter();
        }
        return true;
    }

    /** Set price via the enter-price button (price <= 0 clicks guide price).
     * Game-only. */
    public static boolean setPrice(int price) throws Exception {
        boolean ok = price <= 0
            ? clickQtyPriceChild(WidgetIds.GE_GUIDE_CHILD)
            : clickQtyPriceChild(WidgetIds.GE_PRICE_CHILD);
        if (!ok) {
            return false;
        }
        Sleep.sleep(400, 800);
        if (price > 0) {
            Keyboard.type(String.valueOf(price));
            Keyboard.pressEnter();
        }
        return true;
    }

    /** Confirm, trying the buy-screen button then the sell-screen one —
     * the same fallback order DreamBot's own confirm() uses. Game-only. */
    public static boolean confirm() throws Exception {
        Widget buy = Widgets.child(WidgetIds.GE_GROUP, WidgetIds.GE_CONFIRM_A);
        if (Actions.widget(buy)) {
            return true;
        }
        Widget sell = Widgets.child(WidgetIds.GE_GROUP, WidgetIds.GE_CONFIRM_B);
        return Actions.widget(sell);
    }

    /** Abort the open offer. Game-only. */
    public static boolean abortOffer() throws Exception {
        return Actions.widget(Widgets.child(WidgetIds.GE_GROUP, WidgetIds.GE_ABORT));
    }

    /** Full buy flow: screen, item, quantity, price, confirm. qty <= 0 means
     * All, price <= 0 means guide price. Game-only. */
    public static boolean buyOffer(int slot, String itemName, int qty, int price) throws Exception {
        return openBuyScreen(slot)
            && searchItem(itemName)
            && setQuantity(qty)
            && setPrice(price)
            && confirm();
    }

    /** Full sell flow: screen, inventory item, quantity, price, confirm.
     * Game-only. */
    public static boolean sellOffer(int slot, int itemId, int qty, int price) throws Exception {
        if (!openSellScreen(slot)) {
            return false;
        }
        Widget inv = Widgets.first(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER);
        if (inv == null || inv.getChildren() == null) {
            return false;
        }
        boolean picked = false;
        for (Widget w : inv.getChildren()) {
            if (w != null && !w.isHidden() && w.getItemId() == itemId) {
                picked = Actions.widget(w);
                break;
            }
        }
        if (!picked) {
            return false;
        }
        return setQuantity(qty) && setPrice(price) && confirm();
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

    /** Collect finished offers straight to the bank, using whatever bank
     * action the game currently exposes (never a hardcoded label).
     * Game-only. */
    public static boolean collectToBank() throws Exception {
        Widget w = Widgets.findByActionContaining("bank");
        return Actions.widget(w);
    }
}
