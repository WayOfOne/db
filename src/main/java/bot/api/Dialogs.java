package bot.api;

import java.awt.Robot;
import java.awt.event.KeyEvent;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Talking to NPCs: state reads off dialog widgets, input via the same keys a
 * player uses (space continues, number keys pick options). Key dispatch works
 * across every dialog type without fragile child-widget ids. Game-only. */
public final class Dialogs {
    private Dialogs() {
    }

    private static boolean visible(WidgetInfo info) {
        Widget w = Game.client().getWidget(info);
        return w != null && !w.isHidden();
    }

    /** True while any dialog box (NPC, player, option, sprite) is showing. */
    public static boolean isOpen() {
        return visible(WidgetInfo.DIALOG_NPC_TEXT)
            || visible(WidgetInfo.DIALOG_PLAYER_TEXT)
            || visible(WidgetInfo.DIALOG_OPTION)
            || visible(WidgetInfo.DIALOG_SPRITE_TEXT);
    }

    /** True while a player-choice option list is showing. */
    public static boolean choosing() {
        return visible(WidgetInfo.DIALOG_OPTION_OPTIONS);
    }

    private static void press(int key) throws Exception {
        Robot robot = new Robot();
        robot.keyPress(key);
        robot.keyRelease(key);
        Thread.sleep(400);
    }

    /** Advance the dialog (space). Game-only. */
    public static void continueDialogue() throws Exception {
        press(KeyEvent.VK_SPACE);
    }

    /** Pick option 1-9 from a player-choice dialog. Game-only. */
    public static void chooseOption(int n) throws Exception {
        if (n < 1 || n > 9) {
            throw new IllegalArgumentException("option " + n + " out of 1-9");
        }
        press(KeyEvent.VK_0 + n);
    }
}
