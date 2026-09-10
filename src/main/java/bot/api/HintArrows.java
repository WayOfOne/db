package bot.api;

import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

/** Hint-arrow reads (DreamBot `HintArrow` parity: exists/type/point/target).
 * Reads only — the fork never plants arrows. Pure reads unless noted. */
public final class HintArrows {
    private HintArrows() {
    }

    /** True while a hint arrow is showing. */
    public static boolean exists() {
        return Game.client().hasHintArrow();
    }

    /** Arrow type code (1 = NPC, 2 = player, 3 = world point, roughly). */
    public static int type() {
        return Game.client().getHintArrowType();
    }

    /** Arrow world point, or null for entity arrows. */
    public static WorldPoint point() {
        return Game.client().getHintArrowPoint();
    }

    /** Arrow target player, or null. */
    public static Player player() {
        return Game.client().getHintArrowPlayer();
    }

    /** Arrow target NPC, or null. */
    public static NPC npc() {
        return Game.client().getHintArrowNpc();
    }

    /** Clear our own arrow (e.g. after arriving). Game-only. */
    public static void clear() {
        Game.client().clearHintArrow();
    }
}
