package bot.api.randoms;

import bot.api.Dialogs;
import bot.api.Npcs;
import bot.api.RandomEvents;

/** Talk through the Genie's dialogue (game-content NPC name). Lamp claiming
 * (an XP choice) is intentionally left to the script. */
public final class GenieSolver extends BaseSolver {
    public GenieSolver() {
        super("GENIE");
    }

    @Override
    public boolean shouldExecute() throws Exception {
        if (!Dialogs.isOpen() || Dialogs.choosing()) {
            return false;
        }
        return Npcs.nearestNameWithin(15, "Genie") != null;
    }

    @Override
    public int onLoop() throws Exception {
        return RandomEvents.dismiss() ? 600 : 300;
    }
}
