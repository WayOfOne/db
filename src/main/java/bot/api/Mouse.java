package bot.api;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/** Raw input. Mouse movement plus typed text; menu clicks live in
 * {@link Actions}. Game-only. */
public final class Mouse {
    private Mouse() {
    }

    public static void move(Point at) throws Exception {
        new Robot().mouseMove(at.x, at.y);
    }
}
