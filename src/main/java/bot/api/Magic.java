package bot.api;

import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.Skill;

/** Spellbook home teleports (one per book, no runes or levels). Each widget
 * is compiler-checked against the pinned API; combat/utility spells with
 * rune costs and target selection are deferred, not guessed. */
public final class Magic {
    private Magic() {
    }

    /** Cast a spell by its mined book slot (DreamBot `castSpell` parity:
     * click (218, child)). Rune costs ride DreamBot decrypted tables, so
     * casting never checks runes — use `canCast` for the level gate.
     * Targeted spells need a post-click target tap by the script.
     * Game-only. */
    public static boolean cast(Spell spell) throws Exception {
        if (spell == null) {
            return false;
        }
        Tabs.magic();
        return Actions.widget(Widgets.child(WidgetIds.SPELL_GROUP, spell.child));
    }

    /** True when our Magic level meets the spell's requirement. Level gate
     * only — rune costs are not statically minable (see `Spell`). */
    public static boolean canCast(Spell spell) {
        return spell != null && Skills.level(Skill.MAGIC) >= spell.level;
    }

    /** Cast a home teleport by its verified spellbook widget. Game-only. */
    public static boolean homeTeleport(WidgetInfo spellWidget) throws Exception {
        return Actions.widget(Game.client().getWidget(spellWidget));
    }

    /** Standard spellbook home teleport (Lumbridge). Game-only. */
    public static boolean lumbridgeHome() throws Exception {
        return homeTeleport(WidgetInfo.SPELL_LUMBRIDGE_HOME_TELEPORT);
    }

    /** Ancient spellbook home teleport (Edgeville). Game-only. */
    public static boolean ancientHome() throws Exception {
        return homeTeleport(WidgetInfo.SPELL_EDGEVILLE_HOME_TELEPORT);
    }

    /** Lunar spellbook home teleport. Game-only. */
    public static boolean lunarHome() throws Exception {
        return homeTeleport(WidgetInfo.SPELL_LUNAR_HOME_TELEPORT);
    }

    /** Arceuus spellbook home teleport. Game-only. */
    public static boolean arceuusHome() throws Exception {
        return homeTeleport(WidgetInfo.SPELL_ARCEUUS_HOME_TELEPORT);
    }

    /** Kourend spellbook home teleport. Game-only. */
    public static boolean kourendHome() throws Exception {
        return homeTeleport(WidgetInfo.SPELL_KOUREND_HOME_TELEPORT);
    }
}
