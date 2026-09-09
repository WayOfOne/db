package sample;

import bot.api.Actions;
import bot.api.Game;
import bot.api.GameObjects;
import bot.api.Inventory;
import bot.script.Script;
import bot.script.ScriptManifest;
import net.runelite.api.GameObject;

/** Worked example: chop the nearest normal tree until the inventory is full.
 * Tree ids vary by location — verify yours in-game. */
@ScriptManifest(name = "Woodcutter", version = "1.0",
    description = "Chops normal trees until full.", author = "fork")
public final class Woodcutter extends Script {

    private static final int[] TREES = { 1276, 1278 };

    @Override
    public int onLoop() {
        if (!Game.ready() || !Game.me().isIdle()) {
            return 600;
        }
        if (Inventory.full()) {
            return -1; // full: bank runs are a later script
        }
        GameObject tree = GameObjects.nearest(TREES);
        if (tree == null) {
            return 2000;
        }
        try {
            Actions.object(tree, "Chop down");
        } catch (Exception e) {
            return 2000;
        }
        return 1200;
    }
}
