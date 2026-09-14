package bot.api.randoms;

import java.util.List;

import bot.api.Actions;
import bot.api.Game;
import bot.api.Login;
import bot.api.WidgetIds;
import bot.util.Sleep;
import bot.util.Timing;
import net.runelite.api.widgets.Widget;

/** Bank-PIN entry. Mechanics mined from DreamBot's own `BankPinSolver`
 * (`results/javap-c-db-randoms.txt`): the pin interface is group 213;
 * digit slots sit at root children +3..+6 (first showing "?" needs entry);
 * the ten digit buttons sit at +16+2i, matched by their child(1) label
 * (empty labels accepted as fallback, mirroring the mined matcher). The
 * PIN itself is session-memory only via `Randoms.setBankPin` — never
 * persisted, never logged. Game-only unless noted. */
public final class BankPinSolver extends BaseSolver {
    public BankPinSolver() {
        super("BANK_PIN");
    }

    /** Pin-interface root (group 213), or null. */
    public static Widget root() {
        Widget[] roots = Game.client().getWidgetRoots();
        if (roots == null) {
            return null;
        }
        for (Widget r : roots) {
            if (r != null && !r.isHidden() && (r.getId() >>> 16) == WidgetIds.BANK_PIN_GROUP) {
                return r;
            }
        }
        return null;
    }

    /** True while the pin interface is showing. */
    public static boolean isOpen() {
        return root() != null;
    }

    private static Widget[] children() {
        Widget root = root();
        if (root == null || root.getChildren() == null) {
            return new Widget[0];
        }
        return root.getChildren();
    }

    private static Widget childAt(int index) {
        Widget[] kids = children();
        return index < 0 || index >= kids.length ? null : kids[index];
    }

    /** Index of the first slot showing "?", or -1 when none (mined `0()`). */
    public static int pendingSlot() {
        Widget[] kids = children();
        for (int i = 0; i < 4; i++) {
            Widget slot = i + 3 < kids.length ? kids[i + 3] : null;
            if (slot != null && !slot.isHidden() && "?".equals(slot.getText())) {
                return i;
            }
        }
        return -1;
    }

    /** Button for a digit: first whose child(1) label matches, else the
     * first empty-labelled button (mined `4(String)` fallback). */
    public static Widget digitButton(char digit) {
        Widget[] kids = children();
        Widget fallback = null;
        for (int i = 0; i < 10; i++) {
            int idx = 16 + 2 * i;
            Widget button = idx < kids.length ? kids[idx] : null;
            if (button == null || button.isHidden()) {
                continue;
            }
            Widget label = button.getChild(1);
            String text = label == null ? null : label.getText();
            if (text == null || text.isEmpty()) {
                if (fallback == null) {
                    fallback = button;
                }
                continue;
            }
            if (text.length() == 1 && text.charAt(0) == digit) {
                return button;
            }
        }
        return fallback;
    }

    /** Enter a 4-digit PIN (runtime string only). Game-only. */
    public static boolean enterPin(String pin) throws Exception {
        if (pin == null || pin.length() != 4 || !isOpen()) {
            return false;
        }
        for (int d = 0; d < 4; d++) {
            final char want = pin.charAt(d);
            Widget button = digitButton(want);
            if (button == null || !Actions.widget(button)) {
                return false;
            }
            final int slot = d;
            if (!Timing.waitCondition(() -> {
                Widget[] kids = children();
                Widget s = slot + 3 < kids.length ? kids[slot + 3] : null;
                return s == null || !"?".equals(s.getText());
            }, 3000)) {
                return false;
            }
            Sleep.sleep(300, 600);
        }
        return pendingSlot() == -1;
    }

    @Override
    public boolean shouldExecute() throws Exception {
        String pin = Randoms.bankPin();
        return Login.loggedIn() && pin != null && pin.length() == 4 && pendingSlot() != -1;
    }

    @Override
    public int onLoop() throws Exception {
        return enterPin(Randoms.bankPin()) ? 600 : 1000;
    }
}
