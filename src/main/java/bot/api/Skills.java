package bot.api;

import net.runelite.api.Experience;
import net.runelite.api.Skill;

/** Skill reads. Levels come from XP (real) and boosted values (current),
 * exactly like the in-game skill tab. */
public final class Skills {
    private Skills() {
    }

    /** Current (boosted/drained) level, e.g. current Hitpoints. */
    public static int level(Skill skill) {
        return Game.client().getBoostedSkillLevel(skill);
    }

    /** Real level earned by XP. */
    public static int base(Skill skill) {
        return Experience.getLevelForXp(Game.client().getSkillExperience(skill));
    }

    public static int experience(Skill skill) {
        return Game.client().getSkillExperience(skill);
    }
}
