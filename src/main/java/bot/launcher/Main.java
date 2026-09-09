package bot.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Injector;

import bot.api.Game;
import bot.script.Script;
import bot.script.ScriptLoader;
import bot.script.ScriptRunner;
import bot.ui.ScriptOverlay;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.ui.overlay.OverlayManager;

/** Boots the pinned RuneLite client, waits for login, then runs one local
 * script from the scripts directory until it finishes.
 *
 * <p>Usage: {@code Main <scripts-dir> <script-name> [client args...]}.
 * Needs the live game (network + login happen in the client itself); the
 * offline suite ({@code gradlew smokeTest}) covers everything short of that.
 */
public final class Main {
    /** How long to wait for a logged-in player before giving up. */
    private static final long LOGIN_TIMEOUT_MS = 10 * 60 * 1000L;

    private Main() {
    }

    public static void main(String[] argv) throws Exception {
        if (argv.length < 2) {
            System.out.println("usage: Main <scripts-dir> <script-name> [client args...]");
            System.exit(2);
        }
        System.out.println("dreambot-local " + forkVersion());
        File scriptsDir = new File(argv[0]);
        String wanted = argv[1];

        Injector injector = bootClient(argv);
        Client client = injector.getInstance(Client.class);
        Game.install(client, injector);

        System.out.println("waiting for login...");
        long deadline = System.currentTimeMillis() + LOGIN_TIMEOUT_MS;
        while (!Game.ready() && System.currentTimeMillis() < deadline) {
            Thread.sleep(1000);
        }
        if (!Game.ready()) {
            System.out.println("no logged-in player after "
                + (LOGIN_TIMEOUT_MS / 1000) + "s; exiting without running anything");
            System.exit(3);
        }

        Script script;
        ScriptOverlay overlay = null;
        try (ScriptLoader loader = new ScriptLoader()) {
            List<ScriptLoader.LoadedScript> found = loader.loadAll(scriptsDir);
            ScriptLoader.LoadedScript match = null;
            for (ScriptLoader.LoadedScript s : found) {
                if (s.manifest().name().equalsIgnoreCase(wanted)) {
                    match = s;
                    break;
                }
            }
            if (match == null) {
                List<String> names = new ArrayList<>();
                for (ScriptLoader.LoadedScript s : found) {
                    names.add(s.manifest().name());
                }
                throw new IllegalArgumentException(
                    "no script named '" + wanted + "' in " + scriptsDir
                    + " (found " + names + ", failures " + loader.failures() + ")");
            }
            script = match.newInstance();
            overlay = script.overlay();
            OverlayManager overlays = null;
            if (overlay != null) {
                overlays = injector.getInstance(OverlayManager.class);
                overlays.add(overlay);
            }
            try {
                System.out.println("running " + match.manifest().name()
                    + " v" + match.manifest().version());
                int loops = new ScriptRunner().run(script, client, Integer.MAX_VALUE);
                System.out.println("finished after " + loops + " loops");
            } finally {
                if (overlays != null) {
                    overlays.remove(overlay);
                }
            }
        }
    }

    /** Fork version bundled in {@code src/main/resources/fork.properties}. */
    static String forkVersion() {
        try (java.io.InputStream in = Main.class.getResourceAsStream("/fork.properties")) {
            if (in == null) {
                return "unknown";
            }
            java.util.Properties p = new java.util.Properties();
            p.load(in);
            return p.getProperty("fork.version", "unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }

    /** Starts the RuneLite client in-process and returns its injector, which
     * publishes the {@link Client} and framework services. */
    static Injector bootClient(String[] argv) throws Exception {
        String[] clientArgs = new String[Math.max(0, argv.length - 2)];
        System.arraycopy(argv, 2, clientArgs, 0, clientArgs.length);
        RuneLite.main(clientArgs);
        return RuneLite.getInjector();
    }
}
