package bot.api;

import net.runelite.api.Client;
import net.runelite.api.Player;

/** The world, as the client sees it. The client is installed once at startup;
 * everything else reads through here. Pure reads only — acting lives in
 * {@link Actions}. */
public final class Game {
    private static volatile Client client;

    private Game() {
    }

    /** Called once by the launcher. Not for scripts to call. */
    public static void install(Client c) {
        client = c;
    }

    public static Client client() {
        return client;
    }

    public static boolean ready() {
        return client != null && client.getLocalPlayer() != null;
    }

    public static Local me() {
        Player p = client.getLocalPlayer();
        return p == null ? null : new Local(p);
    }
}
