package bot.api;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Fairy-ring travel. Dial mechanics mined from DreamBot's own `FairyRings`
 * bytecode (`results/fairy-ring-table.txt`): dial widgets (398, 19/21/23),
 * letters from varp 816 bit-pairs, confirm button (398, 26 — the pinned
 * `FAIRY_RING_TELEPORT_BUTTON` agrees), travel log (381, 7). Destination
 * codes ride in DreamBot's decrypted string tables, so scripts pass codes
 * as plain letters (DreamBot's own `travel(String[])` shape) — no code
 * table is vendored here. Game-only unless noted. */
public final class FairyRings {
    private FairyRings() {
    }

    /** Dial widgets, slot 0-2. */
    public static final int[] DIAL_CHILDREN = {19, 21, 23};
    /** Varp-816 masks per slot. */
    public static final int[] DIAL_MASKS = {3, 12, 48};
    /** Dial letters per slot (index = (varp816 & mask) >> (slot * 2)). */
    public static final String[][] DIAL_LETTERS = {
        {"a", "d", "c", "b"},
        {"i", "l", "k", "j"},
        {"p", "s", "r", "q"},
    };

    /** Current dial letter for a slot 0-2 (varp 816). Pure read. */
    public static String dialLetter(int slot) {
        if (slot < 0 || slot > 2) {
            throw new IllegalArgumentException("slot " + slot + " out of 0-2");
        }
        int idx = (Vars.varp(WidgetIds.FAIRY_VARP) & DIAL_MASKS[slot]) >> (slot * 2);
        return DIAL_LETTERS[slot][idx];
    }

    /** Current 3-letter code on the dials. Pure read. */
    public static String[] currentCode() {
        return new String[] {dialLetter(0), dialLetter(1), dialLetter(2)};
    }

    /** True while the travel interface (confirm button) is showing. */
    public static boolean isOpen() {
        return Widgets.child(WidgetIds.FAIRY_GROUP, WidgetIds.FAIRY_CONFIRM) != null;
    }

    /** Rotate one dial clockwise (DreamBot `rotateSlotClockwise` parity:
     * click the dial; the letter advances). Game-only. */
    public static boolean rotateSlot(int slot) throws Exception {
        if (slot < 0 || slot > 2) {
            return false;
        }
        return Actions.widget(Widgets.child(WidgetIds.FAIRY_GROUP, DIAL_CHILDREN[slot]));
    }

    /** Dial one slot to a letter (up to 4 rotations). Game-only. */
    public static boolean dialTo(int slot, String letter) throws Exception {
        if (slot < 0 || slot > 2 || letter == null) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (letter.equalsIgnoreCase(dialLetter(slot))) {
                return true;
            }
            if (!rotateSlot(slot)) {
                return false;
            }
            bot.util.Sleep.sleep(300, 600);
        }
        return letter.equalsIgnoreCase(dialLetter(slot));
    }

    /** Dial a full code, e.g. {"a", "i", "q"}. Game-only. */
    public static boolean enterCode(String[] code) throws Exception {
        if (code == null || code.length != 3) {
            return false;
        }
        for (int slot = 0; slot < 3; slot++) {
            if (!dialTo(slot, code[slot])) {
                return false;
            }
        }
        return true;
    }

    /** Dial a code and confirm (DreamBot `travel(String[])` parity). Game-only. */
    public static boolean travel(String[] code) throws Exception {
        return enterCode(code)
            && Actions.widget(Game.client().getWidget(WidgetInfo.FAIRY_RING_TELEPORT_BUTTON));
    }
}
