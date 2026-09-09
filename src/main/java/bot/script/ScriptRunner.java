package bot.script;

import net.runelite.api.Client;

/** Runs one script: start, loop, exit. Loop sleeps are capped so a script can
 * never hang the runner for more than a minute per iteration. */
public final class ScriptRunner {
    public static final int MAX_SLEEP_MS = 60_000;

    private volatile boolean stop;

    public void stop() {
        stop = true;
    }

    /** @return number of {@code onLoop} iterations executed. */
    public int run(Script script, Client client, int maxIterations) {
        script.init(client);
        int loops = 0;
        script.onStart();
        try {
            while (!stop && loops < maxIterations) {
                int sleep = script.onLoop();
                loops++;
                if (sleep <= 0) {
                    break;
                }
                try {
                    Thread.sleep(Math.min(sleep, MAX_SLEEP_MS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            script.onExit();
        }
        return loops;
    }
}
