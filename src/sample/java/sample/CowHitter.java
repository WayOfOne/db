package sample;

import bot.api.Actions;
import bot.api.Game;
import bot.api.Npcs;
import bot.script.Script;
import bot.script.ScriptManifest;
import net.runelite.api.NPC;

/** Worked example: attack the nearest cow (id 2805) when idle.
 * The full walkthrough lives in docs/writing-scripts.md. */
@ScriptManifest(name = "CowHitter", version = "1.0",
    description = "Attacks the nearest cow.", author = "fork")
public final class CowHitter extends Script {

    /** Cow type id. Lumbridge cows are 2805; verify yours in-game. */
    private static final int COW = 2805;

    @Override
    public int onLoop() {
        if (!Game.ready() || !Game.me().isIdle()) {
            return 600;
        }
        NPC cow = Npcs.nearestWithin(10, COW);
        if (cow == null) {
            return 600;
        }
        try {
            Actions.npc(cow, "Attack");
        } catch (Exception e) {
            return 2000;
        }
        return 1200;
    }
}
