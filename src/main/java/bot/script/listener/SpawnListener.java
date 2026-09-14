package bot.script.listener;

import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.TileItem;

/** Spawn/despawn callbacks. First sightings come from the spawn events;
 * projectile first-sightings are tracked by the dispatcher (the pinned
 * bus only reports movement). */
public interface SpawnListener extends java.util.EventListener {
    /** An NPC appeared. */
    default void onNpcSpawn(NPC npc) {
    }

    /** An NPC disappeared. */
    default void onNpcDespawn(NPC npc) {
    }

    /** A player appeared (not the local player logging in). */
    default void onPlayerSpawn(Player player) {
    }

    /** A player disappeared. */
    default void onPlayerDespawn(Player player) {
    }

    /** A game object appeared. */
    default void onGameObjectSpawn(GameObject object) {
    }

    /** A game object disappeared. */
    default void onGameObjectDespawn(GameObject object) {
    }

    /** Ground loot appeared. */
    default void onGroundItemSpawn(TileItem item) {
    }

    /** Ground loot disappeared. */
    default void onGroundItemDespawn(TileItem item) {
    }

    /** Ground loot stack size changed (old, new). */
    default void onGroundItemUpdate(TileItem item, int oldQuantity, int newQuantity) {
    }

    /** A projectile was first seen. */
    default void onProjectileSpawn(Projectile projectile) {
    }
}
