package bot.api;

import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.widgets.WidgetInfo;

/** Combat state reads and tab actions. Varp/varbit ids come from DreamBot's
 * own Combat bytecode (`results/javap-c-db-combat-prayer-tracker.txt`):
 * special % = varp 300 / 10, special active = varp 301, auto-retaliate =
 * varp 172 bit 0, style = varp 43 (+ varbit 2668 when 43 == 4), poisoned =
 * varp 102. Style/auto-retaliate widgets are the pinned
 * `WidgetInfo.COMBAT_*` constants — probe-verified identical to DreamBot's
 * mined ids (`results/widgetinfo-probe.txt`). Game-only unless noted. */
public final class Combat {
    private Combat() {
    }

    /** True while fighting something or something is fighting you. */
    public static boolean isInCombat() {
        Player me = Game.client().getLocalPlayer();
        if (me == null) {
            return false;
        }
        Actor target = me.getInteracting();
        if (target instanceof NPC || target instanceof Player) {
            return true;
        }
        for (NPC n : Game.client().getNpcs()) {
            if (n != null && n.getInteracting() == me) {
                return true;
            }
        }
        return false;
    }

    /** Our combat level, -1 when logged out (mirrors DreamBot getCombatLevel). */
    public static int combatLevel() {
        Player me = Game.client().getLocalPlayer();
        return me == null ? -1 : me.getCombatLevel();
    }

    /** Special-attack energy 0-100 (DreamBot getSpecialPercentage). Pure read. */
    public static int specialPercentage() {
        return Vars.varp(300) / 10;
    }

    /** True while special attack is toggled on (DreamBot isSpecialActive). */
    public static boolean specialActive() {
        return Vars.varp(301) == 1;
    }

    /** Auto-retaliate on/off (DreamBot isAutoRetaliateOn). Pure read. */
    public static boolean autoRetaliate() {
        return (Vars.varp(172) & 1) != 0;
    }

    /** Set auto-retaliate via the combat-tab box. Game-only. */
    public static boolean setAutoRetaliate(boolean on) throws Exception {
        if (autoRetaliate() == on) {
            return true;
        }
        Tabs.combat();
        return Actions.widget(Game.client().getWidget(WidgetInfo.COMBAT_AUTO_RETALIATE));
    }

    /** Toggle the special-attack orb; falls back to the combat-tab button
     * after opening the tab (mirrors DreamBot toggleSpecialAttack's
     * orb-then-tab order). Game-only. */
    public static boolean toggleSpecial(boolean on) throws Exception {
        if (specialActive() == on) {
            return true;
        }
        if (Actions.widget(Widgets.child(WidgetIds.SPEC_ORB_GROUP, WidgetIds.SPEC_ORB_CHILD))) {
            return true;
        }
        Tabs.combat();
        return Actions.widget(Widgets.child(WidgetIds.COMBAT_GROUP, WidgetIds.COMBAT_SPEC_CHILD));
    }

    /** Attack-style index 0-3 (DreamBot getCombatModeIndex; varbit 2668
     * offsets defensive autocast). Pure read. */
    public static int styleIndex() {
        int mode = Vars.varp(43);
        return mode == 4 ? mode + Vars.varbit(2668) : mode;
    }

    /** Select attack style 0-3 via the combat tab (DreamBot
     * setCombatModeIndex range). Game-only. */
    public static boolean setStyle(int index) throws Exception {
        if (index < 0 || index > 3 || styleIndex() == index) {
            return styleIndex() == index;
        }
        Tabs.combat();
        WidgetInfo style = switch (index) {
            case 0 -> WidgetInfo.COMBAT_STYLE_ONE;
            case 1 -> WidgetInfo.COMBAT_STYLE_TWO;
            case 2 -> WidgetInfo.COMBAT_STYLE_THREE;
            default -> WidgetInfo.COMBAT_STYLE_FOUR;
        };
        return Actions.widget(Game.client().getWidget(style));
    }

    /** True while poisoned (DreamBot isPoisoned: varp 102). Envenom magnitude
     * needs a runtime-decrypted threshold DreamBot never exposes statically,
     * so it stays out — poisoned vs clean is the supported read. */
    public static boolean isPoisoned() {
        return Vars.varp(102) > 0;
    }
}
