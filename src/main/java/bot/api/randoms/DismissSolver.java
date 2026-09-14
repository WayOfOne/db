package bot.api.randoms;

import bot.api.Dialogs;
import bot.api.RandomEvents;

/** Continue-box dismissal (see `RandomEvents` for the boundary). */
public final class DismissSolver extends BaseSolver {
    public DismissSolver() {
        super("DISMISS");
    }

    @Override
    public boolean shouldExecute() throws Exception {
        return Dialogs.isOpen() && !Dialogs.choosing();
    }

    @Override
    public int onLoop() throws Exception {
        return RandomEvents.dismiss() ? 600 : 300;
    }
}
