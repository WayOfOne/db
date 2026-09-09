package bot.util;

import java.util.function.BooleanSupplier;

/** Wait-for-condition with timeout. Polls every 100 ms; returns what it saw. */
public final class Timing {
    private Timing() {
    }

    public static boolean waitCondition(BooleanSupplier condition, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Sleep.sleep(100);
        }
        return condition.getAsBoolean();
    }
}
