package bot.api;

import net.runelite.api.GameState;
import net.runelite.api.widgets.WidgetInfo;

/** Login-screen driver. Credentials are runtime-only: they arrive as method
 * arguments (or the launcher's environment), are never stored anywhere,
 * never logged, and must never be committed — see the credential rule in
 * `docs/writing-scripts.md`. There is no account manager, no switching,
 * and no authenticator bypass: an authenticator prompt fails the attempt
 * and the human completes it. Game-only unless noted. */
public final class Login {
    private Login() {
    }

    /** Current login state (RuneLite's maintained mapping of the raw login
     * index). Pure read. */
    public static GameState state() {
        return GameState.of(Game.client().getLoginIndex());
    }

    /** True while in-game. Pure read. */
    public static boolean loggedIn() {
        return Game.client().getGameState() == GameState.LOGGED_IN;
    }

    /** Attempt login with runtime credentials. Clicks through the
     * click-to-play screen, fills the client fields directly (no widget
     * ids needed), submits with Enter, then waits for in-game up to
     * {@code timeoutMs}. Returns false on timeout or when an
     * authenticator prompt appears (complete it by hand and retry).
     * Game-only. */
    public static boolean login(String username, String password, long timeoutMs) throws Exception {
        if (loggedIn()) {
            return true;
        }
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        Actions.widget(Game.client().getWidget(WidgetInfo.LOGIN_CLICK_TO_PLAY_SCREEN));
        Game.client().setUsername(username);
        Game.client().setPassword(password);
        Keyboard.pressEnter();
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (loggedIn()) {
                return true;
            }
            if (Game.client().getGameState() == GameState.LOGIN_SCREEN_AUTHENTICATOR) {
                return false;
            }
            Thread.sleep(1000);
        }
        return loggedIn();
    }

    /** Log out via the logout tab + logout button
     * (probe-verified 182,6). Game-only. */
    public static boolean logout() throws Exception {
        Tabs.logout();
        return Actions.widget(Game.client().getWidget(WidgetInfo.LOGOUT_BUTTON));
    }
}
