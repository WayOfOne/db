package bot.api.randoms;

import bot.api.Actions;
import bot.api.Game;
import bot.api.Login;
import bot.api.WidgetIds;
import bot.api.Widgets;
import net.runelite.api.widgets.Widget;

/** Welcome-screen closer. Fires when logged in with the welcome widget
 * (378, 72 — DreamBot's own `WelcomeScreenSolver` target) visible.
 * Game-only unless noted. */
public final class WelcomeSolver extends BaseSolver {
    public WelcomeSolver() {
        super("WELCOME_SCREEN");
    }

    /** True while the welcome widget is showing. */
    public static boolean isOpen() {
        return Widgets.child(WidgetIds.WELCOME_GROUP, WidgetIds.WELCOME_CLOSE) != null;
    }

    /** Click the welcome widget away. Game-only. */
    public static boolean close() throws Exception {
        Widget w = Widgets.child(WidgetIds.WELCOME_GROUP, WidgetIds.WELCOME_CLOSE);
        return Actions.widget(w);
    }

    @Override
    public boolean shouldExecute() throws Exception {
        return Login.loggedIn() && isOpen();
    }

    @Override
    public int onLoop() throws Exception {
        return close() ? 500 : 300;
    }
}
