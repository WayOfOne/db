package sample;

import bot.api.Bank;
import bot.api.Game;
import bot.script.Script;
import bot.script.ScriptManifest;

/** Worked example: open the nearest bank booth and deposit everything.
 * Booth ids vary by location — verify yours in-game. */
@ScriptManifest(name = "BankRunner", version = "1.0",
    description = "Banks everything at the nearest booth.", author = "fork")
public final class BankRunner extends Script {

    private static final int[] BOOTHS = { 10517 };

    @Override
    public int onLoop() {
        if (!Game.ready() || !Game.me().isIdle()) {
            return 600;
        }
        try {
            if (!Bank.isOpen()) {
                return Bank.openBooth(BOOTHS) ? 2000 : 3000;
            }
            Bank.depositInventory();
            return -1;
        } catch (Exception e) {
            return 2000;
        }
    }
}
