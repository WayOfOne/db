package bot.api;

import net.runelite.api.widgets.WidgetInfo;

/** Spellbook teleports that need no runes or levels. Each home-teleport widget
 * is compiler-checked against the pinned API; fuller spellbooks (levels,
 * rune costs, target selection) are deferred, not guessed. */
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
}
