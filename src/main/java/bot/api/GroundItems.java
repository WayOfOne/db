package bot.api;

import java.util.ArrayList;
import java.util.List;

import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;

/** Ground-item enumeration for looters: scans the scene's tiles, the same grid
 * {@link GameObjects} walks for scenery.
 *
 * <p>{@link TileItem} carries no position in this API version, so every method
 * returns a {@link Loot} pairing the item with the tile it was found on. */
public final class GroundItems {
    private GroundItems() {
    }

    /** One ground item plus where it lies. */
    public static final class Loot {
        private final TileItem item;
        private final WorldPoint location;

        Loot(TileItem item, WorldPoint location) {
            this.item = item;
            this.location = location;
        }

        public TileItem item() {
            return item;
        }

        public WorldPoint location() {
            return location;
        }
    }

    public static List<Loot> withId(int... ids) {
        List<Loot> out = new ArrayList<>();
        Local me = Game.me();
        if (me == null) {
            return out;
        }
        Scene scene = Game.client().getScene();
        Tile[][][] tiles = scene.getTiles();
        int plane = Game.client().getPlane();
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                Tile t = tiles[plane][x][y];
                if (t == null) {
                    continue;
                }
                WorldPoint at = t.getWorldLocation();
                for (TileItem i : t.getGroundItems()) {
                    if (i == null) {
                        continue;
                    }
                    for (int id : ids) {
                        if (i.getId() == id) {
                            out.add(new Loot(i, at));
                            break;
                        }
                    }
                }
            }
        }
        return out;
    }

    /** Nearest ground item with any of the given ids, or null. */
    public static Loot nearest(int... ids) {
        Local me = Game.me();
        if (me == null) {
            return null;
        }
        Loot best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Loot l : withId(ids)) {
            int d = me.location().distanceTo(l.location());
            if (d < bestDist) {
                bestDist = d;
                best = l;
            }
        }
        return best;
    }

    /** Nearest ground item with any of the given ids within range, or null. */
    public static Loot nearestWithin(int range, int... ids) {
        Local me = Game.me();
        if (me == null) {
            return null;
        }
        WorldPoint at = me.location();
        Loot best = null;
        int bestDist = range + 1;
        for (Loot l : withId(ids)) {
            int d = at.distanceTo(l.location());
            if (d <= range && d < bestDist) {
                bestDist = d;
                best = l;
            }
        }
        return best;
    }
}
