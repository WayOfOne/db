package bot.api;

import java.util.List;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

/** NPC search helpers. Ids are NPC type ids (what kind of creature it is). */
public final class Npcs {
    private Npcs() {
    }

    public static List<NPC> all() {
        return Game.client().getNpcs();
    }

    public static List<NPC> withId(int... ids) {
        List<NPC> out = new java.util.ArrayList<>();
        for (NPC n : all()) {
            if (n == null) {
                continue;
            }
            for (int id : ids) {
                if (n.getId() == id) {
                    out.add(n);
                    break;
                }
            }
        }
        return out;
    }

    /** Nearest NPC of any of the given ids, or null. */
    public static NPC nearest(int... ids) {
        Local me = Game.me();
        NPC best = null;
        int bestDist = Integer.MAX_VALUE;
        for (NPC n : withId(ids)) {
            int d = me == null ? 0 : me.location().distanceTo(n.getWorldLocation());
            if (d < bestDist) {
                bestDist = d;
                best = n;
            }
        }
        return best;
    }

    /** Nearest NPC of any of the given ids within {@code range} tiles, or null. */
    public static NPC nearestWithin(int range, int... ids) {
        Local me = Game.me();
        if (me == null) {
            return null;
        }
        WorldPoint at = me.location();
        NPC best = null;
        int bestDist = range + 1;
        for (NPC n : withId(ids)) {
            int d = at.distanceTo(n.getWorldLocation());
            if (d <= range && d < bestDist) {
                bestDist = d;
                best = n;
            }
        }
        return best;
    }
}
