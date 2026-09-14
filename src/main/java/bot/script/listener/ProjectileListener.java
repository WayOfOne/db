package bot.script.listener;

import net.runelite.api.Projectile;

/** Projectile callbacks. DreamBot's targeted/pre/post variants have no
 * pinned source; movement is what the bus reports. */
public interface ProjectileListener extends java.util.EventListener {
    /** A projectile moved. */
    default void onProjectileMoved(Projectile projectile) {
    }
}
