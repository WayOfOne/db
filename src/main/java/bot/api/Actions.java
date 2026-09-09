package bot.api;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Shape;

import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/** Doing things. Builders construct menu entries exactly the way the client
 * builds them for the same entity (same option/target/identifier/type), so a
 * bot never hand-rolls opcodes. Dispatch is a real left-click at the entity's
 * clickbox via {@link Robot}: no packets, no client internals.
 *
 * <p>Dispatch needs the live game (a visible client window). The builders are
 * pure and covered by the offline smoke test; identifier/param conventions
 * were taken from the client's own menu handling and still want one live
 * confirmation pass (see Phase 9 notes in docs/fork-plan.md).
 */
public final class Actions {
    private Actions() {
    }

    /** First-option entry on an NPC (Attack on a cow, Talk-to on a shopkeeper...). */
    public static MenuEntry npcMenu(NPC npc, String option) {
        return npcMenu(npc, option, 0);
    }

    /** Nth-option entry on an NPC (0-based: 0 = first option). */
    public static MenuEntry npcMenu(NPC npc, String option, int actionIndex) {
        MenuAction type;
        switch (actionIndex) {
            case 1: type = MenuAction.NPC_SECOND_OPTION; break;
            case 2: type = MenuAction.NPC_THIRD_OPTION; break;
            case 3: type = MenuAction.NPC_FOURTH_OPTION; break;
            case 4: type = MenuAction.NPC_FIFTH_OPTION; break;
            default: type = MenuAction.NPC_FIRST_OPTION; break;
        }
        return Game.client().createMenuEntry(actionIndex)
            .setOption(option)
            .setTarget(npc.getName())
            .setIdentifier(npc.getIndex())
            .setType(type)
            .setForceLeftClick(true);
    }

    /** First-option entry on scenery (Chop down / Mine / Open ...). */
    public static MenuEntry objectMenu(GameObject object, String option) {
        return Game.client().createMenuEntry(0)
            .setOption(option)
            .setTarget(object.getWorldLocation().toString())
            .setIdentifier(object.getId())
            .setType(MenuAction.GAME_OBJECT_FIRST_OPTION)
            .setForceLeftClick(true);
    }

    /** Nth-option entry on a ground item (0-based; Take is usually third). */
    public static MenuEntry groundItemMenu(TileItem item, String option, int actionIndex) {
        MenuAction type;
        switch (actionIndex) {
            case 1: type = MenuAction.GROUND_ITEM_SECOND_OPTION; break;
            case 2: type = MenuAction.GROUND_ITEM_THIRD_OPTION; break;
            case 3: type = MenuAction.GROUND_ITEM_FOURTH_OPTION; break;
            case 4: type = MenuAction.GROUND_ITEM_FIFTH_OPTION; break;
            default: type = MenuAction.GROUND_ITEM_FIRST_OPTION; break;
        }
        String name = Game.client().getItemDefinition(item.getId()).getName();
        return Game.client().createMenuEntry(actionIndex)
            .setOption(option)
            .setTarget(name)
            .setIdentifier(item.getId())
            .setType(type)
            .setForceLeftClick(true);
    }

    /** Install an entry as the entire menu (left-click does it). */
    public static void install(MenuEntry entry) {
        Game.client().setMenuEntries(new MenuEntry[] { entry });
    }

    /** Screen point at an NPC's clickbox center, or null when off screen. */
    public static Point npcScreen(NPC npc) {
        LocalPoint local = npc.getLocalLocation();
        Shape box = Perspective.getClickbox(
            Game.client(), Game.client().getTopLevelWorldView(), npc.getModel(),
            npc.getOrientation(), local.getX(), local.getY(), Game.client().getPlane());
        if (box == null) {
            return null;
        }
        Rectangle r = box.getBounds();
        return new Point(r.x + r.width / 2, r.y + r.height / 2);
    }

    /** Screen point on the minimap for a world tile (walking), or null. */
    public static Point minimapScreen(WorldPoint world) {
        LocalPoint local = LocalPoint.fromWorld(Game.client(), world);
        if (local == null) {
            return null;
        }
        net.runelite.api.Point p = Perspective.localToMinimap(Game.client(), local);
        return p == null ? null : new Point(p.getX(), p.getY());
    }

    /** Left-click a screen point with the real mouse. Game-only. */
    public static void click(Point at) throws Exception {
        Robot robot = new Robot();
        robot.mouseMove(at.x, at.y);
        robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
    }

    /** First option on the nearest NPC of the given ids. Game-only. */
    public static boolean npc(NPC npc, String option) throws Exception {
        if (npc == null) {
            return false;
        }
        install(npcMenu(npc, option));
        Point at = npcScreen(npc);
        if (at == null) {
            return false;
        }
        click(at);
        return true;
    }

    /** First option on scenery at its clickbox. Game-only. */
    public static boolean object(GameObject object, String option) throws Exception {
        if (object == null) {
            return false;
        }
        install(objectMenu(object, option));
        java.awt.Shape box = object.getClickbox();
        if (box == null) {
            return false;
        }
        Rectangle r = box.getBounds();
        click(new Point(r.x + r.width / 2, r.y + r.height / 2));
        return true;
    }

    /** Nth option on a ground item at its tile. Game-only. */
    public static boolean take(GroundItems.Loot loot, String option, int actionIndex) throws Exception {
        if (loot == null) {
            return false;
        }
        install(groundItemMenu(loot.item(), option, actionIndex));
        LocalPoint local = LocalPoint.fromWorld(Game.client(), loot.location());
        if (local == null) {
            return false;
        }
        java.awt.Polygon poly = Perspective.getCanvasTilePoly(Game.client(), local);
        if (poly == null) {
            return false;
        }
        Rectangle r = poly.getBounds();
        click(new Point(r.x + r.width / 2, r.y + r.height / 2));
        return true;
    }

    /** Walk toward a world tile via the minimap. Game-only. */
    public static boolean walkTo(WorldPoint world) throws Exception {
        Point at = minimapScreen(world);
        if (at == null) {
            return false;
        }
        click(at);
        return true;
    }
}
