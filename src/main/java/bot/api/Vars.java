package bot.api;

import net.runelite.api.Prayer;
import net.runelite.api.Skill;

/** Raw game-state reads: varbits/varps (quest stages, settings, toggles),
 * run energy, prayer. Ids are the game's own varbit/varp numbers. */
public final class Vars {
    private Vars() {
    }

    public static int varbit(int id) {
        return Game.client().getVarbitValue(id);
    }

    public static int varp(int id) {
        return Game.client().getVarpValue(id);
    }

    /** Run energy 0-100. */
    public static int runEnergy() {
        return Game.client().getEnergy() / 100;
    }

    public static boolean prayerActive(Prayer prayer) {
        return Game.client().isPrayerActive(prayer);
    }

    /** Hitpoints percentage, for "should I eat" checks. */
    public static int healthPercent() {
        int max = Skills.base(Skill.HITPOINTS);
        return max == 0 ? 0 : Skills.level(Skill.HITPOINTS) * 100 / max;
    }
}
