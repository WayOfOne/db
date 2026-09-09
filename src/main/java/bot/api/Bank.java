package bot.api;

import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Bank reads and actions. Opening uses the same object/NPC interaction as
 * everything else; deposit buttons are widget clicks. Widget dispatch wants
 * one live confirmation pass (see {@link Actions}). */
public final class Bank {
    private Bank() {
    }

    /** True while the bank interface is visible. */
    public static boolean isOpen() {
        Widget w = Game.client().getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        return w != null && !w.isHidden();
    }

    /** Count of an item id in the bank (0 when closed — the container is empty then). */
    public static int count(int id) {
        ItemContainer c = Game.client().getItemContainer(InventoryID.BANK);
        if (c == null) {
            return 0;
        }
        int n = 0;
        for (Item i : c.getItems()) {
            if (i != null && i.getId() == id) {
                n += Math.max(i.getQuantity(), 1);
            }
        }
        return n;
    }

    public static boolean contains(int id) {
        return count(id) > 0;
    }

    /** Open the nearest bank booth from the given ids ("Bank" first option). Game-only. */
    public static boolean openBooth(int... boothIds) throws Exception {
        GameObject booth = GameObjects.nearest(boothIds);
        return Actions.object(booth, "Bank");
    }

    /** Open via the nearest banker NPC ("Bank" first option). Game-only. */
    public static boolean openBanker(int... bankerIds) throws Exception {
        NPC banker = Npcs.nearestWithin(10, bankerIds);
        return Actions.npc(banker, "Bank");
    }

    /** Click the Deposit-inventory button. Game-only. */
    public static boolean depositInventory() throws Exception {
        Widget w = Game.client().getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
        return Actions.widget(w);
    }

    /** Click the Deposit-worn-equipment button. Game-only. */
    public static boolean depositEquipment() throws Exception {
        Widget w = Game.client().getWidget(WidgetInfo.BANK_DEPOSIT_EQUIPMENT);
        return Actions.widget(w);
    }

    /** Close any open interface (escape). Game-only. */
    public static boolean close() throws Exception {
        Keyboard.pressEscape();
        return true;
    }
}
