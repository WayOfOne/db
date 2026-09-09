package bot.api;

import net.runelite.api.GameObject;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;

/** Scenery enumeration: walks the scene's object grid directly, so gathering
 * scripts never need typed-in tiles. */
public final class GameObjects {
    private GameObjects() {
    }

    /** Nearest scenery object with any of the given ids, or null. */
    public static GameObject nearest(int... ids) {
        Local me = Game.me();
        if (me == null) {
            return null;
        }
        WorldPoint at = me.location();
        Scene scene = Game.client().getScene();
        Tile[][][] tiles = scene.getTiles();
        int plane = Game.client().getPlane();
        GameObject best = null;
        int bestDist = Integer.MAX_VALUE;
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                Tile t = tiles[plane][x][y];
                if (t == null) {
                    continue;
                }
                for (GameObject o : t.getGameObjects()) {
                    if (o == null) {
                        continue;
                    }
                    for (int id : ids) {
                        if (o.getId() == id) {
                            int d = at.distanceTo(o.getWorldLocation());
                            if (d < bestDist) {
                                bestDist = d;
                                best = o;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return best;
    }
}
