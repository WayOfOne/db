package bot.util;

import bot.api.Combat;
import bot.api.Favour;
import bot.api.Quests;
import bot.api.Skills;
import net.runelite.api.Skill;

/** Script prerequisites (DreamBot's `data/requirements` shape, minus what
 * needs unknown verdicts: quest completion stays out because finished-state
 * interpretation is uncalibrated). Pure logic, fully offline-testable. */
public final class Requirements {
    private Requirements() {
    }

    /** A prerequisite a script checks before acting. */
    public interface Requirement {
        boolean meets();
    }

    /** Base skill level requirement. */
    public static Requirement skill(Skill skill, int level) {
        return () -> Skills.base(skill) >= level;
    }

    /** Combat-level requirement. */
    public static Requirement combat(int level) {
        return () -> Combat.combatLevel() >= level;
    }

    /** Quest-point requirement. */
    public static Requirement questPoints(int qp) {
        return () -> Quests.questPoints() >= qp;
    }

    /** Arceuus favour requirement (percent). */
    public static Requirement favour(Favour.House house, double percent) {
        return () -> Favour.percent(house) >= percent;
    }

    /** All sub-requirements must hold. */
    public static Requirement all(Requirement... requirements) {
        return () -> {
            if (requirements == null) {
                return false;
            }
            for (Requirement r : requirements) {
                if (r == null || !r.meets()) {
                    return false;
                }
            }
            return true;
        };
    }

    /** Any sub-requirement must hold. */
    public static Requirement any(Requirement... requirements) {
        return () -> {
            if (requirements == null) {
                return false;
            }
            for (Requirement r : requirements) {
                if (r != null && r.meets()) {
                    return true;
                }
            }
            return false;
        };
    }
}
