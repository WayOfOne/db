package bot.api;

import net.runelite.api.GameObject;
import net.runelite.api.widgets.Widget;

/** Deposit-box reads and actions. Button ids come from DreamBot's own
 * deposit-box API ({@code results/widget-id-sites.txt}); closing leads with
 * Escape exactly like theirs. Game-only unless noted. */
public final class DepositBox {
    private DepositBox() {
    }

    /** True while the deposit box is showing. */
    public static boolean isOpen() {
        return Widgets.child(WidgetIds.DEPOSIT_GROUP, WidgetIds.DEPOSIT_ITEMS) != null;
    }

    /** Open the nearest deposit box ("Deposit" first option). Game-only. */
    public static boolean open(int... boxIds) throws Exception {
        GameObject box = GameObjects.nearest(boxIds);
        return Actions.object(box, "Deposit");
    }

    /** Slot widget (slot-indexed children of the items container), or null. */
    public static Widget slotWidget(int slot) {
        Widget root = Widgets.child(WidgetIds.DEPOSIT_GROUP, WidgetIds.DEPOSIT_ITEMS);
        if (root == null) {
            return null;
        }
        return root.getChild(slot);
    }

    private static boolean depositButton(int child) throws Exception {
        return Actions.widget(Widgets.child(WidgetIds.DEPOSIT_GROUP, child));
    }

    /** Deposit everything carried. Game-only. */
    public static boolean depositAllItems() throws Exception {
        return depositButton(WidgetIds.DEPOSIT_ALL_ITEMS);
    }

    /** Deposit everything worn. Game-only. */
    public static boolean depositAllEquipment() throws Exception {
        return depositButton(WidgetIds.DEPOSIT_ALL_EQUIPMENT);
    }

    /** Deposit all loot. Game-only. */
    public static boolean depositAllLoot() throws Exception {
        return depositButton(WidgetIds.DEPOSIT_ALL_LOOT);
    }

    /** Close: Escape first, close-button fallback. Game-only. */
    public static boolean close() throws Exception {
        Keyboard.pressEscape();
        if (isOpen()) {
            Widget root = Widgets.child(WidgetIds.DEPOSIT_GROUP, WidgetIds.DEPOSIT_CLOSE);
            if (root != null) {
                return Actions.widget(root.getChild(WidgetIds.DEPOSIT_CLOSE_BUTTON));
            }
        }
        return !isOpen();
    }
}
