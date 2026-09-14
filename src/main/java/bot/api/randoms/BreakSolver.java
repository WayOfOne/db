package bot.api.randoms;

import bot.api.Login;
import bot.util.Sleep;

/** Scheduled breaks: log out after {@code workMs}, rest {@code breakMs},
 * re-login with the held session credentials. Idle when no session
 * credentials are held (a break without re-login would strand the script
 * logged out). Configure once via `schedule`; `clear` disables. */
public final class BreakSolver extends BaseSolver {
    private static long workMs;
    private static long breakMs;
    private static long nextBreakAt;

    public BreakSolver() {
        super("BREAK");
    }

    /** Break {@code breakMinutes} every {@code workMinutes} of runtime. */
    public static void schedule(long workMinutes, long breakMinutes) {
        workMs = workMinutes * 60_000L;
        breakMs = breakMinutes * 60_000L;
        nextBreakAt = System.currentTimeMillis() + workMs;
    }

    /** Disable scheduled breaks. */
    public static void clear() {
        workMs = 0;
        breakMs = 0;
        nextBreakAt = 0;
    }

    static boolean due() {
        return workMs > 0 && breakMs > 0 && System.currentTimeMillis() >= nextBreakAt;
    }

    @Override
    public boolean shouldExecute() throws Exception {
        return Login.loggedIn() && Login.hasSessionCredentials() && due();
    }

    @Override
    public int onLoop() throws Exception {
        Login.logout();
        long remaining = breakMs;
        while (remaining > 0) {
            int chunk = (int) Math.min(60_000L, remaining);
            Sleep.sleep(chunk, chunk);
            remaining -= chunk;
        }
        Login.relogin(120_000L);
        nextBreakAt = System.currentTimeMillis() + workMs;
        return 1000;
    }
}
