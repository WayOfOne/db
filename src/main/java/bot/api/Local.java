package bot.api;

import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

/** You. A thin wrapper over the local player. */
public final class Local {
    private final Player p;

    public Local(Player p) {
        this.p = p;
    }

    public WorldPoint location() {
        return p.getWorldLocation();
    }

    public int worldX() {
        return p.getWorldLocation().getX();
    }

    public int worldY() {
        return p.getWorldLocation().getY();
    }

    public int animation() {
        return p.getAnimation();
    }

    public boolean isIdle() {
        return p.getAnimation() == -1;
    }

    public int combatLevel() {
        return p.getCombatLevel();
    }
}
