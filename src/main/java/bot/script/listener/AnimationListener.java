package bot.script.listener;

import net.runelite.api.NPC;
import net.runelite.api.Player;

/** Animation callbacks off `AnimationChanged`. Spot-animation variants are
 * omitted: the pinned event carries no spot id to forward. */
public interface AnimationListener extends java.util.EventListener {
    /** Player animation changed (current id, -1 idle). */
    default void onPlayerAnimation(Player player, int animation) {
    }

    /** NPC animation changed (current id, -1 idle). */
    default void onNpcAnimation(NPC npc, int animation) {
    }
}
