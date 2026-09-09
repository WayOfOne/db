package bot.api;

import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;

/** Combat state reads. */
public final class Combat {
    private Combat() {
    }

    /** True while fighting something or something is fighting you. */
    public static boolean isInCombat() {
        Player me = Game.client().getLocalPlayer();
        if (me == null) {
            return false;
        }
        Actor target = me.getInteracting();
        if (target instanceof NPC || target instanceof Player) {
            return true;
        }
        for (NPC n : Game.client().getNpcs()) {
            if (n != null && n.getInteracting() == me) {
                return true;
            }
        }
        return false;
    }
}
