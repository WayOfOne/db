package sample;

import bot.api.Game;
import bot.api.GrandExchange;
import bot.script.Script;
import bot.script.ScriptManifest;

/** Worked example: open the exchange and collect everything finished.
 * Clerk ids vary — verify yours in-game. */
@ScriptManifest(name = "GeCollector", version = "1.0",
    description = "Collects finished exchange offers.", author = "fork")
public final class GeCollector extends Script {

    private static final int[] CLERKS = { 2148 };

    @Override
    public int onLoop() {
        if (!Game.ready() || !Game.me().isIdle()) {
            return 600;
        }
        try {
            if (!GrandExchange.isOpen()) {
                return GrandExchange.openClerk(CLERKS) ? 2000 : 3000;
            }
            GrandExchange.collectAll();
            return -1;
        } catch (Exception e) {
            return 2000;
        }
    }
}
