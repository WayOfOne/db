package bot.api;

import java.util.Locale;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Minigame teleports through the clan interface (group 76). Flow mined
 * from DreamBot's own `MinigameTeleports` (`results/javap-c-db-minigame-tp.txt`):
 * the clan tab shows the list [76, 22], the current pick reads off [76, 11],
 * confirm clicks [76, 32] then continues the dialogue. Rows match by
 * visible name at runtime (the same text-matching the GE search uses), so
 * no per-minigame table is needed. The clan-tab opener itself resolves
 * through DreamBot runtime-decrypted holders with no pinned-API constant,
 * so `teleport` works when the list is open and fails clean otherwise —
 * identifying the opener wants one live pass. Game-only unless noted. */
public final class Minigames {
    private Minigames() {
    }

    /** True while the minigame list is showing. */
    public static boolean isOpen() {
        return Widgets.child(WidgetIds.MINIGAME_GROUP, WidgetIds.MINIGAME_LIST) != null;
    }

    /** Currently selected entry text, or null. Pure read. */
    public static String selected() {
        Widget w = Widgets.child(WidgetIds.MINIGAME_GROUP, WidgetIds.MINIGAME_SELECTED);
        return w == null ? null : w.getText();
    }

    /** Teleport to the entry whose visible name contains {@code name}.
     * Game-only. */
    public static boolean teleport(String name) throws Exception {
        if (name == null) {
            return false;
        }
        String want = name.toLowerCase(Locale.ROOT);
        Widget list = Widgets.child(WidgetIds.MINIGAME_GROUP, WidgetIds.MINIGAME_LIST);
        if (list == null || list.getChildren() == null) {
            return false;
        }
        for (Widget row : list.getChildren()) {
            if (row == null || row.isHidden() || row.getText() == null
                || !row.getText().toLowerCase(Locale.ROOT).contains(want)) {
                continue;
            }
            if (!Actions.widget(row)) {
                return false;
            }
            bot.util.Sleep.sleep(400, 800);
            if (!Actions.widget(Widgets.child(WidgetIds.MINIGAME_GROUP, WidgetIds.MINIGAME_CONFIRM))) {
                return false;
            }
            bot.util.Sleep.sleep(600, 1200);
            if (Dialogs.isOpen() && !Dialogs.choosing()) {
                Dialogs.continueDialogue();
            }
            return true;
        }
        return false;
    }
}
