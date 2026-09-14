package bot.api.randoms;

import bot.api.Game;
import bot.api.Login;
import net.runelite.api.GameState;

/** Re-login on disconnect using the held session credentials (see
 * `Login.setSessionCredentials` — memory only, never persisted). Idle
 * when no credentials are held or already in-game. */
public final class LoginSolver extends BaseSolver {
    public LoginSolver() {
        super("LOGIN");
    }

    @Override
    public boolean shouldExecute() throws Exception {
        if (Login.loggedIn() || !Login.hasSessionCredentials()) {
            return false;
        }
        GameState state = Game.client().getGameState();
        return state == GameState.CONNECTION_LOST || state == GameState.LOGIN_SCREEN;
    }

    @Override
    public int onLoop() throws Exception {
        return Login.relogin(120_000L) ? 1000 : 5000;
    }
}
