package bot.api;

import java.awt.Point;
import java.util.List;

import bot.util.Timing;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/** Getting around. Single-tile clicks go through {@link Actions#walkTo};
 * longer trips compute an A* path over the scene collision grid
 * ({@link PathFinder}) and walk it waypoint by waypoint. Game-only. */
public final class Walking {
    private Walking() {
    }

    /** Scene base derived from the player (world minus scene coords), so no
     * scene-layout constants are needed. */
    static int[] sceneBase() {
        Local me = Game.me();
        LocalPoint local = Game.client().getLocalPlayer().getLocalLocation();
        return new int[] { me.worldX() - local.getSceneX(), me.worldY() - local.getSceneY() };
    }

    /** A* path in world tiles, or null when unreachable / off-plane. */
    public static List<WorldPoint> findPath(WorldPoint goal) {
        Local me = Game.me();
        if (me == null || goal.getPlane() != me.location().getPlane()) {
            return null;
        }
        int plane = me.location().getPlane();
        int[][] flags = Game.client().getCollisionMaps()[plane].getFlags();
        LocalPoint local = Game.client().getLocalPlayer().getLocalLocation();
        int[] base = sceneBase();
        List<Point> steps = PathFinder.find(
            flags, local.getSceneX(), local.getSceneY(),
            goal.getX() - base[0], goal.getY() - base[1]);
        if (steps == null) {
            return null;
        }
        List<WorldPoint> out = new java.util.ArrayList<>(steps.size());
        for (Point s : steps) {
            out.add(new WorldPoint(base[0] + s.x, base[1] + s.y, plane));
        }
        return out;
    }

    /** Walk the A* path, stepping to waypoints ahead and waiting for arrival.
     * Game-only. */
    public static boolean walkPath(WorldPoint goal) throws Exception {
        List<WorldPoint> path = findPath(goal);
        if (path == null || path.isEmpty()) {
            return false;
        }
        for (WorldPoint step : path) {
            Local me = Game.me();
            if (me == null) {
                return false;
            }
            if (me.location().distanceTo(step) <= 2) {
                continue;
            }
            if (me.location().distanceTo(goal) <= 2) {
                return true;
            }
            Actions.walkTo(step);
            Timing.waitCondition(
                () -> {
                    Local m = Game.me();
                    return m != null && m.location().distanceTo(step) <= 2;
                },
                8000);
        }
        Local me = Game.me();
        return me != null && me.location().distanceTo(goal) <= 2;
    }
}
