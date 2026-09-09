package bot.util;

/** Sleeping with human-ish variance. The runner already sleeps between loops;
 * use these for extra pauses inside a loop (animation waits, travel waits). */
public final class Sleep {
    private Sleep() {
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Sleep a random amount in [min, max] ms. */
    public static void sleep(int min, int max) {
        sleep(Calculations.random(min, max));
    }

    /** Sleep a gaussian-ish amount around {@code mean} (clamped to [min, max]). */
    public static void sleepHumanized(int mean, int min, int max) {
        double v = mean + (Calculations.random(-1.0, 1.0) + Calculations.random(-1.0, 1.0)) * mean / 4.0;
        sleep((int) Math.min(max, Math.max(min, v)));
    }
}
