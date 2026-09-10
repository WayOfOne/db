package bot.api;

import net.runelite.api.widgets.WidgetInfo;

/** Spellbook home teleports (one per book, no runes or levels). Each widget
 * is compiler-checked against the pinned API; combat/utility spells with
 * rune costs and target selection are deferred, not guessed. */
public final class Magic {
    private Magic() {
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
