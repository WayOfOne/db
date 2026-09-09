package bot.util;

import java.util.concurrent.ThreadLocalRandom;

/** Small math helpers. Pure, covered by the smoke test. */
public final class Calculations {
    private Calculations() {
    }

    public static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double random(double min, double max) {
        return min + ThreadLocalRandom.current().nextDouble() * (max - min);
    }

    public static int chebyshev(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }
}
