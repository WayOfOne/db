package bot.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bot.api.Game;
import bot.script.Script;
import bot.script.ScriptLoader;
import bot.script.ScriptRunner;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;

/** Boots the pinned RuneLite client, then runs one local script from the
 * scripts directory until it finishes.
 *
 * <p>Usage: {@code Main <scripts-dir> <script-name> [client args...]}.
 * Needs the live game (network + login happen in the client itself); the
 * offline suite ({@code gradlew smokeTest}) covers everything short of that.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] argv) throws Exception {
        if (argv.length < 2) {
            System.out.println("usage: Main <scripts-dir> <script-name> [client args...]");
            System.exit(2);
        }
        File scriptsDir = new File(argv[0]);
        String wanted = argv[1];

        // The real client owns the game state; scripts only read through Game.
        Client client = bootClient(argv);
        Game.install(client);

        Script script;
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
            System.out.println("running " + match.manifest().name()
                + " v" + match.manifest().version());
            int loops = new ScriptRunner().run(script, client, Integer.MAX_VALUE);
            System.out.println("finished after " + loops + " loops");
        }
    }

    /** Starts the RuneLite client in-process and returns its API handle.
     * The client publishes a Guice injector; the fork reads the one
     * framework object it needs (the {@link Client}) from it. */
    static Client bootClient(String[] argv) throws Exception {
        String[] clientArgs = new String[Math.max(0, argv.length - 2)];
        System.arraycopy(argv, 2, clientArgs, 0, clientArgs.length);
        RuneLite.main(clientArgs);
        return RuneLite.getInjector().getInstance(Client.class);
    }
}
