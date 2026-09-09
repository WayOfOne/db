package bot.api;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

/** Worn-equipment reads. Slot layout is the game's own equipment order. */
public final class Equipment {
    private Equipment() {
    }

    public static Item[] items() {
        ItemContainer c = Game.client().getItemContainer(InventoryID.EQUIPMENT);
        return c == null ? new Item[0] : c.getItems();
    }

    public static boolean equipped(int id) {
        for (Item i : items()) {
            if (i != null && i.getId() == id) {
                return true;
            }
        }
        return false;
    }

    /** Item id in a specific slot, or -1 when the slot is empty. */
    public static int inSlot(EquipmentInventorySlot slot) {
        Item[] all = items();
        int idx = slot.getSlotIdx();
        if (idx < 0 || idx >= all.length || all[idx] == null) {
            return -1;
        }
        return all[idx].getId();
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
}
