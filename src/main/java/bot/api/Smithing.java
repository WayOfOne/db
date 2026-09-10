package bot.api;

import net.runelite.api.GameObject;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Smithing reads and item selection. The smithing screen is group 312
 * (probe-verified `SMITHING_INVENTORY_ITEMS_CONTAINER`); DreamBot ships no
 * smithing class, so this follows the `Shop`/`Bank` widget pattern instead
 * of mined ids. Game-only unless noted. */
public final class Smithing {
    private Smithing() {
    }

    /** True while the smithing item list is showing. */
    public static boolean isOpen() {
        return Widgets.first(WidgetInfo.SMITHING_INVENTORY_ITEMS_CONTAINER) != null;
    }

    /** Smithable item widgets currently listed, never null. */
    public static java.util.List<Widget> items() {
        Widget root = Widgets.first(WidgetInfo.SMITHING_INVENTORY_ITEMS_CONTAINER);
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

    /** True while the item is listed. */
    public static boolean hasItem(int id) {
        for (Widget w : items()) {
            if (w.getItemId() == id) {
                return true;
            }
        }
        return false;
    }

    /** Click a listed item to smith it. Game-only. */
    public static boolean smith(int id) throws Exception {
        for (Widget w : items()) {
            if (w.getItemId() == id) {
                return Actions.widget(w);
            }
        }
        return false;
    }

    /** Open the smithing screen via an anvil. Game-only. */
    public static boolean openAnvil(int... anvilIds) throws Exception {
        GameObject anvil = GameObjects.nearest(anvilIds);
        return Actions.object(anvil, "Smith");
    }
}
