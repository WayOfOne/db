package bot.api;

import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;

/** Inventory reads. Slots with no item are skipped. */
public final class Inventory {
    private Inventory() {
    }

    public static Item[] items() {
        ItemContainer c = Game.client().getItemContainer(InventoryID.INVENTORY);
        return c == null ? new Item[0] : c.getItems();
    }

    public static int count(int id) {
        int n = 0;
        for (Item i : items()) {
            if (i != null && i.getId() == id) {
                n += Math.max(i.getQuantity(), 1);
            }
        }
        return n;
    }

    public static boolean contains(int id) {
        return count(id) > 0;
    }

    /** Full when no empty slot remains (28 usable slots). */
    public static boolean full() {
        int used = 0;
        for (Item i : items()) {
            if (i != null && i.getId() > 0) {
                used++;
            }
        }
        return used >= 28;
    }

    /** Current hitpoints, for "should I eat" checks. */
    public static int health() {
        return Skills.level(Skill.HITPOINTS);
    }
}
