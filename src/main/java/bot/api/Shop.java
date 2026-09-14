package bot.api;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Shop reads and trading. The stock container is group 300
 * (probe-verified `SHOP_INVENTORY_ITEMS_CONTAINER`; DreamBot's own `Shop`
 * resolves its parent through a runtime-decrypted holder, so no
 * DreamBot-side group id exists to mine). Buy/sell click the game's own
 * quantity actions — the same labels the shop menu shows. Game-only
 * unless noted. */
public final class Shop {
    private Shop() {
    }

    /** True while a shop stock list is showing. */
    public static boolean isOpen() {
        return Widgets.first(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER) != null;
    }

    /** Visible stock widgets carrying an item, never null. */
    public static java.util.List<Widget> stock() {
        Widget root = Widgets.first(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER);
        java.util.List<Widget> out = new java.util.ArrayList<>();
        if (root == null || root.getChildren() == null) {
            return out;
        }
        for (Widget w : root.getChildren()) {
            if (w != null && !w.isHidden() && w.getItemId() > 0) {
                out.add(w);
            }
        }
        return out;
    }

    /** Stock count for an item id. */
    public static int stockCount(int id) {
        int n = 0;
        for (Widget w : stock()) {
            if (w.getItemId() == id) {
                n += Math.max(1, w.getItemQuantity());
            }
        }
        return n;
    }

    /** True while the shop stocks the item. */
    public static boolean hasStock(int id) {
        return stockCount(id) > 0;
    }

    private static boolean clickStock(int id, String option) throws Exception {
        Widget root = Widgets.first(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER);
        if (root == null || root.getChildren() == null) {
            return false;
        }
        for (Widget w : root.getChildren()) {
            if (w != null && !w.isHidden() && w.getItemId() == id) {
                return Actions.widget(w, option);
            }
        }
        return false;
    }

    /** Buy one/five/ten/fifty of a stocked item. Game-only. */
    public static boolean buyOne(int id) throws Exception {
        return clickStock(id, "Buy 1");
    }

    /** Buy one/five/ten/fifty of a stocked item. Game-only. */
    public static boolean buyFive(int id) throws Exception {
        return clickStock(id, "Buy 5");
    }

    /** Buy one/five/ten/fifty of a stocked item. Game-only. */
    public static boolean buyTen(int id) throws Exception {
        return clickStock(id, "Buy 10");
    }

    /** Buy one/five/ten/fifty of a stocked item. Game-only. */
    public static boolean buyFifty(int id) throws Exception {
        return clickStock(id, "Buy 50");
    }

    private static boolean clickInventory(int id, String option) throws Exception {
        Widget root = Widgets.first(WidgetInfo.INVENTORY);
        if (root == null || root.getChildren() == null) {
            return false;
        }
        for (Widget w : root.getChildren()) {
            if (w != null && !w.isHidden() && w.getItemId() == id) {
                return Actions.widget(w, option);
            }
        }
        return false;
    }

    /** Sell one/five/ten/fifty of a carried item. Game-only. */
    public static boolean sellOne(int id) throws Exception {
        return clickInventory(id, "Sell 1");
    }

    /** Sell one/five/ten/fifty of a carried item. Game-only. */
    public static boolean sellFive(int id) throws Exception {
        return clickInventory(id, "Sell 5");
    }

    /** Sell one/five/ten/fifty of a carried item. Game-only. */
    public static boolean sellTen(int id) throws Exception {
        return clickInventory(id, "Sell 10");
    }

    /** Sell one/five/ten/fifty of a carried item. Game-only. */
    public static boolean sellFifty(int id) throws Exception {
        return clickInventory(id, "Sell 50");
    }

    /** Open a shop via a keeper ("Trade" first option). Game-only. */
    public static boolean open(int... keeperIds) throws Exception {
        return Actions.npc(Npcs.nearestWithin(10, keeperIds), "Trade");
    }

    /** Close via Escape (Bank.close precedent). Game-only. */
    public static boolean close() throws Exception {
        Keyboard.pressEscape();
        return !isOpen();
    }
}
