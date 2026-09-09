package bot.api;

import java.util.ArrayList;
import java.util.List;

import bot.util.Calculations;
import net.runelite.api.coords.WorldPoint;

/** A rectangular zone on one plane. Pure geometry — no game reads, so fully
 * covered by the smoke test. */
public final class Area {
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private final int plane;

    public Area(int x1, int y1, int x2, int y2, int plane) {
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
        this.plane = plane;
    }

    public boolean contains(WorldPoint p) {
        return p != null && p.getPlane() == plane
            && p.getX() >= x1 && p.getX() <= x2
            && p.getY() >= y1 && p.getY() <= y2;
    }

    public WorldPoint getCenter() {
        return new WorldPoint((x1 + x2) / 2, (y1 + y2) / 2, plane);
    }

    public WorldPoint randomTile() {
        return new WorldPoint(Calculations.random(x1, x2), Calculations.random(y1, y2), plane);
    }

    /** All tiles, for scans that need them. */
    public List<WorldPoint> tiles() {
        List<WorldPoint> out = new ArrayList<>();
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                out.add(new WorldPoint(x, y, plane));
            }
        }
        return out;
    }
}
