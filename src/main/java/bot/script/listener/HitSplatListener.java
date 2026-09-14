package bot.script.listener;

import net.runelite.api.Actor;

/** Hitsplat callbacks off `HitsplatApplied`. */
public interface HitSplatListener extends java.util.EventListener {
    /** A hitsplat landed (target actor, damage amount). */
    default void onHitSplat(Actor target, int damage) {
    }
}
