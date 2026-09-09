package bot.api;

import java.util.List;

import net.runelite.api.Player;

/** Player search helpers. */
public final class Players {
    private Players() {
    }

    public static List<Player> all() {
        return Game.client().getPlayers();
    }

    /** Nearest other player, or null when alone. */
    public static Player nearest() {
        Local me = Game.me();
        if (me == null) {
            return null;
        }
        Player self = Game.client().getLocalPlayer();
        Player best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Player p : all()) {
            if (p == null || p == self) {
                continue;
            }
            int d = me.location().distanceTo(p.getWorldLocation());
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
