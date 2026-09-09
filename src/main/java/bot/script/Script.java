package bot.script;

import bot.ui.ScriptOverlay;
import net.runelite.api.Client;

/** Base class for local scripts. The runner calls {@link #onStart} once, then
 * {@link #onLoop} until it returns {@code <= 0}, then {@link #onExit}.
 * Neither method may block the client thread: return a sleep and exit. */
public abstract class Script {
    private Client client;

    /** Called by the runner before {@link #onStart}. Not for scripts to call. */
    public final void init(Client client) {
        this.client = client;
    }

    protected final Client client() {
        return client;
    }

    public void onStart() {
    }

    /** @return milliseconds to sleep before the next loop; {@code <= 0} stops. */
    public abstract int onLoop();

    public void onExit() {
    }

    /** Optional overlay, registered for the script's lifetime. Null by default. */
    public ScriptOverlay overlay() {
        return null;
    }
}
