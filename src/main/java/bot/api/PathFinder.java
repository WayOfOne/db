package bot.api;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/** A* over scene collision flags. Movement-bit values were read out of
 * RuneLite's own client ({@code results/movementflag-clinit.txt}):
 * directional wall bits on the current tile block leaving that way; OBJECT,
 * FLOOR and FULL on the target tile block entry (decoration never blocks).
 * Single plane only. Pure logic — covered by the smoke test on synthetic maps.
 */
public final class PathFinder {
    public static final int BLOCK_NW = 1;
    public static final int BLOCK_N = 2;
    public static final int BLOCK_NE = 4;
    public static final int BLOCK_E = 8;
    public static final int BLOCK_SE = 16;
    public static final int BLOCK_S = 32;
    public static final int BLOCK_SW = 64;
    public static final int BLOCK_W = 128;
    public static final int BLOCK_OBJECT = 256;
    public static final int BLOCK_FLOOR = 2097152;
    public static final int BLOCK_FULL = 2359552;

    /** Bit mask on the target tile that forbids entry. */
    public static final int ENTRY_BLOCKED = BLOCK_OBJECT | BLOCK_FLOOR | BLOCK_FULL;

    private static final int[] DX = { 0, 1, 1, 1, 0, -1, -1, -1 };
    private static final int[] DY = { -1, -1, 0, 1, 1, 1, 0, -1 };
    /** Wall bit on the current tile blocking each of the 8 directions above. */
    private static final int[] WALL = {
        BLOCK_N, BLOCK_NE, BLOCK_E, BLOCK_SE,
        BLOCK_S, BLOCK_SW, BLOCK_W, BLOCK_NW,
    };

    private PathFinder() {
    }

    private static final class Node implements Comparable<Node> {
        final int x;
        final int y;
        final double g;
        final double f;
        final Node parent;

        Node(int x, int y, double g, double f, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(f, o.f);
        }
    }

    private static double heuristic(int x, int y, int gx, int gy) {
        int dx = Math.abs(x - gx);
        int dy = Math.abs(y - gy);
        return Math.max(dx, dy) + 0.4142 * Math.min(dx, dy);
    }

    private static boolean stepBlocked(int[][] flags, int w, int h, int x, int y, int dir) {
        int nx = x + DX[dir];
        int ny = y + DY[dir];
        if (nx < 0 || ny < 0 || nx >= w || ny >= h) {
            return true;
        }
        if ((flags[x][y] & WALL[dir]) != 0) {
            return true;
        }
        if ((flags[nx][ny] & ENTRY_BLOCKED) != 0) {
            return true;
        }
        if (DX[dir] != 0 && DY[dir] != 0) {
            // No corner cutting: both orthogonal exits must be legal.
            int ox = x + DX[dir];
            int oy = y;
            int px = x;
            int py = y + DY[dir];
            int wallO = DX[dir] > 0 ? BLOCK_E : BLOCK_W;
            int wallP = DY[dir] > 0 ? BLOCK_S : BLOCK_N;
            if ((flags[x][y] & wallO) != 0 || (flags[ox][oy] & ENTRY_BLOCKED) != 0) {
                return true;
            }
            if ((flags[x][y] & wallP) != 0 || (flags[px][py] & ENTRY_BLOCKED) != 0) {
                return true;
            }
        }
        return false;
    }

    /** Scene-coordinate path from (sx,sy) to (gx,gy), inclusive, or null when
     * unreachable. {@code flags} is the plane's collision grid. */
    public static List<Point> find(int[][] flags, int sx, int sy, int gx, int gy) {
        int w = flags.length;
        int h = flags[0].length;
        if (sx < 0 || sy < 0 || gx < 0 || gy < 0 || sx >= w || sy >= h || gx >= w || gy >= h) {
            return null;
        }
        if ((flags[gx][gy] & ENTRY_BLOCKED) != 0) {
            return null;
        }
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<Long, Double> best = new HashMap<>();
        open.add(new Node(sx, sy, 0, heuristic(sx, sy, gx, gy), null));
        best.put(((long) sx << 32) | (sy & 0xffffffffL), 0.0);
        while (!open.isEmpty()) {
            Node cur = open.poll();
            if (cur.x == gx && cur.y == gy) {
                List<Point> path = new ArrayList<>();
                for (Node n = cur; n != null; n = n.parent) {
                    path.add(new Point(n.x, n.y));
                }
                Collections.reverse(path);
                return path;
            }
            for (int dir = 0; dir < 8; dir++) {
                if (stepBlocked(flags, w, h, cur.x, cur.y, dir)) {
                    continue;
                }
                int nx = cur.x + DX[dir];
                int ny = cur.y + DY[dir];
                double step = (DX[dir] != 0 && DY[dir] != 0) ? 1.4142 : 1.0;
                double g = cur.g + step;
                long key = ((long) nx << 32) | (ny & 0xffffffffL);
                if (g < best.getOrDefault(key, Double.POSITIVE_INFINITY)) {
                    best.put(key, g);
                    open.add(new Node(nx, ny, g, g + heuristic(nx, ny, gx, gy), cur));
                }
            }
        }
        return null;
    }
}
