package bot.script.listener;

import net.runelite.api.Skill;

/** XP callbacks. The dispatcher tracks last XP/level per skill off
 * `StatChanged` and derives gains and level-ups; DreamBot's
 * onClientEvent (fake drops) has no source and stays out. */
public interface ExperienceListener extends java.util.EventListener {
    /** XP gained in a skill (delta since the last event). */
    default void onGained(Skill skill, int gained, int total) {
    }

    /** Level-up in a skill. */
    default void onLevelUp(Skill skill, int newLevel) {
    }

    /** Any level change (up or down, e.g. boosts wearing off). */
    default void onLevelChange(Skill skill, int oldLevel, int newLevel) {
    }
}
