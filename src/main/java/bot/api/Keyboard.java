package bot.api;

import java.awt.Robot;
import java.awt.event.KeyEvent;

/** Typed text for chat, search boxes, and quantity prompts. Handles letters
 * (with shift), digits, space, and enter; anything else is rejected rather
 * than mistyped. Game-only. */
public final class Keyboard {
    private Keyboard() {
    }

    public static void type(String text) throws Exception {
        Robot robot = new Robot();
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                tap(robot, KeyEvent.VK_SPACE, false);
            } else if (c == '\n') {
                tap(robot, KeyEvent.VK_ENTER, false);
            } else if (Character.isLetter(c)) {
                tap(robot, KeyEvent.getExtendedKeyCodeForChar(Character.toUpperCase(c)),
                    Character.isUpperCase(c));
            } else if (Character.isDigit(c)) {
                tap(robot, KeyEvent.getExtendedKeyCodeForChar(c), false);
            } else {
                throw new IllegalArgumentException("untypable char: '" + c + "'");
            }
            robot.delay(60);
        }
    }

    public static void pressEnter() throws Exception {
        Robot robot = new Robot();
        tap(robot, KeyEvent.VK_ENTER, false);
    }

    public static void pressEscape() throws Exception {
        Robot robot = new Robot();
        tap(robot, KeyEvent.VK_ESCAPE, false);
    }

    private static void tap(Robot robot, int key, boolean shift) {
        if (shift) {
            robot.keyPress(KeyEvent.VK_SHIFT);
        }
        robot.keyPress(key);
        robot.keyRelease(key);
        if (shift) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
    }
}
