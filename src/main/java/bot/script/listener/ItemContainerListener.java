package bot.script.listener;

import net.runelite.api.ItemContainer;

/** Container callbacks off `ItemContainerChanged`. Added/removed deltas
 * come from dispatcher snapshots (inventory + equipment only). */
public interface ItemContainerListener extends java.util.EventListener {
    /** Inventory contents changed (post-change container). */
    default void onInventoryChanged(ItemContainer container) {
    }

    /** Equipment contents changed (post-change container). */
    default void onEquipmentChanged(ItemContainer container) {
    }

    /** Items arrived in inventory (id, quantity). */
    default void onInventoryItemAdded(int id, int quantity) {
    }

    /** Items left inventory (id, quantity). */
    default void onInventoryItemRemoved(int id, int quantity) {
    }

    /** Items arrived in equipment (id, quantity). */
    default void onEquipmentItemAdded(int id, int quantity) {
    }

    /** Items left equipment (id, quantity). */
    default void onEquipmentItemRemoved(int id, int quantity) {
    }
}
