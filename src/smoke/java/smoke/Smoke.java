package smoke;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bot.api.Actions;
import bot.api.Area;
import bot.api.Bank;
import bot.api.Dialogs;
import bot.api.Equipment;
import bot.api.Game;
import bot.api.GroundItems;
import bot.api.Npcs;
import bot.api.PathFinder;
import bot.api.Vars;
import bot.util.Calculations;
import bot.util.Sleep;
import bot.util.Timing;
import bot.script.Script;
import bot.script.ScriptLoader;
import bot.script.ScriptRunner;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Offline verification: loads the sample script jar, runs it against a stub
 * client, and checks the search + menu builders. Needs no game, no network,
 * no display (never touches {@link java.awt.Robot}). */
public final class Smoke {
    private static int checks;

    public static void main(String[] argv) throws Exception {
        Client client = stubClient();
        Game.install(client, null);
        check(Game.ready(), "client ready");

        try (ScriptLoader loader = new ScriptLoader()) {
            List<ScriptLoader.LoadedScript> found = loader.loadAll(new File(argv[0]));
            check(found.size() == 4, "four sample scripts load (failures=" + loader.failures() + ")");
            ScriptLoader.LoadedScript hello = null;
            for (ScriptLoader.LoadedScript s : found) {
                if ("Hello".equals(s.manifest().name())) {
                    hello = s;
                }
            }
            check(hello != null, "manifest name Hello resolves");
            Script s = hello.newInstance();
            int loops = new ScriptRunner().run(s, client, 10);
            check(loops == 5, "five loops then stop, got " + loops);
        }

        NPC cow = Npcs.nearest(2805);
        check(cow != null && "Cow".equals(cow.getName()), "nearest cow by id");
        check(Npcs.nearestWithin(10, 2805) != null, "cow within 10 tiles");
        check(Npcs.nearestWithin(1, 2805) == null, "cow not within 1 tile");
        check(Npcs.nearest(9999) == null, "unknown id yields null");

        MenuEntry e = Actions.npcMenu(cow, "Attack");
        check("Attack".equals(e.getOption())
            && "Cow".equals(e.getTarget())
            && e.getIdentifier() == 3
            && e.getType() == MenuAction.NPC_FIRST_OPTION
            && e.isForceLeftClick(), "npc menu entry fields");

        TileItem coins = GroundItems.nearest(995).item();
        check(coins != null && coins.getQuantity() == 10, "nearest ground item by id");
        check(GroundItems.nearestWithin(10, 995) != null, "coins within 10 tiles");
        check(GroundItems.nearestWithin(2, 995) == null, "coins not within 2 tiles");
        check(GroundItems.nearest(9999) == null, "unknown ground id yields null");

        MenuEntry g = Actions.groundItemMenu(coins, "Take", 2);
        check("Take".equals(g.getOption())
            && "Coins".equals(g.getTarget())
            && g.getIdentifier() == 995
            && g.getType() == MenuAction.GROUND_ITEM_THIRD_OPTION
            && g.isForceLeftClick(), "ground item menu entry fields");

        check(Bank.isOpen(), "bank reads open");
        check(Bank.count(995) == 100, "bank count by id");
        check(!Bank.contains(9999), "bank misses unknown id");
        check(!Equipment.equipped(995), "nothing equipped");

        Widget deposit = Game.client().getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
        MenuEntry w = Actions.widgetMenu(deposit);
        check("Deposit inventory".equals(w.getOption())
            && w.getType() == MenuAction.CC_OP
            && w.getIdentifier() == 1
            && w.getParam0() == 123456
            && w.isForceLeftClick(), "widget menu entry fields");

        check(!Dialogs.isOpen(), "no dialog open");
        check(!Dialogs.choosing(), "no option choice open");
        check(Vars.varbit(1) == 0, "varbit default");
        check(Vars.varp(1) == 0, "varp default");
        check(Vars.runEnergy() == 0, "energy default");
        check(Vars.healthPercent() == 0, "health percent guards div-zero");

        Area lumby = new Area(3215, 3215, 3230, 3230, 0);
        check(lumby.contains(new WorldPoint(3222, 3218, 0)), "area contains");
        check(!lumby.contains(new WorldPoint(3200, 3200, 0)), "area excludes outside");
        check(!lumby.contains(new WorldPoint(3222, 3218, 1)), "area excludes other plane");
        check(lumby.getCenter().equals(new WorldPoint(3222, 3222, 0)), "area center");
        check(lumby.contains(lumby.randomTile()), "random tile inside");
        check(Calculations.chebyshev(0, 0, 3, 4) == 4, "chebyshev distance");
        int r = Calculations.random(5, 5);
        check(r == 5, "degenerate random range");
        long t0 = System.currentTimeMillis();
        Sleep.sleep(50, 50);
        check(System.currentTimeMillis() - t0 >= 50, "sleep waits");
        check(Timing.waitCondition(() -> true, 1000), "condition already true");
        check(!Timing.waitCondition(() -> false, 150), "condition times out");

        int[][] open = new int[10][10];
        List<java.awt.Point> path = PathFinder.find(open, 0, 0, 9, 9);
        check(path != null && path.get(0).equals(new java.awt.Point(0, 0))
            && path.get(path.size() - 1).equals(new java.awt.Point(9, 9)),
            "open-field path endpoints");
        int[][] wall = new int[10][10];
        for (int y = 0; y < 10; y++) {
            wall[4][y] = PathFinder.BLOCK_FULL;
        }
        wall[4][5] = 0; // one gap
        List<java.awt.Point> around = PathFinder.find(wall, 0, 5, 9, 5);
        check(around != null && around.contains(new java.awt.Point(4, 5)), "wall detour via gap");
        List<java.awt.Point> blocked = PathFinder.find(wall, 0, 0, 9, 0);
        check(blocked != null, "wall detour around the end");
        int[][] shut = new int[5][5];
        shut[2][2] = PathFinder.BLOCK_FULL;
        shut[1][2] = PathFinder.BLOCK_FULL;
        shut[3][2] = PathFinder.BLOCK_FULL;
        shut[2][1] = PathFinder.BLOCK_FULL;
        shut[2][3] = PathFinder.BLOCK_FULL;
        check(PathFinder.find(shut, 0, 0, 2, 2) == null, "enclosed goal unreachable");
        check(PathFinder.find(open, 3, 3, 3, 3).size() == 1, "start equals goal");

        System.out.println("SMOKE PASS (" + checks + " checks)");
    }

    private static void check(boolean cond, String name) {
        if (!cond) {
            throw new AssertionError("FAIL: " + name);
        }
        checks++;
        System.out.println("ok " + name);
    }

    private static Object defaultValue(Class<?> t) {
        if (!t.isPrimitive()) {
            return null;
        }
        if (t == boolean.class) {
            return false;
        }
        if (t == void.class) {
            return null;
        }
        return 0;
    }

    private static NPC stubNpc() {
        InvocationHandler h = (proxy, m, args) -> switch (m.getName()) {
            case "getId" -> 2805;
            case "getIndex" -> 3;
            case "getName" -> "Cow";
            case "getAnimation" -> -1;
            case "getWorldLocation" -> new WorldPoint(3222, 3218, 0);
            default -> defaultValue(m.getReturnType());
        };
        return (NPC) Proxy.newProxyInstance(Smoke.class.getClassLoader(), new Class<?>[] { NPC.class }, h);
    }

    private static Player stubPlayer() {
        InvocationHandler h = (proxy, m, args) -> switch (m.getName()) {
            case "getName" -> "Me";
            case "getAnimation" -> -1;
            case "getWorldLocation" -> new WorldPoint(3220, 3218, 0);
            default -> defaultValue(m.getReturnType());
        };
        return (Player) Proxy.newProxyInstance(Smoke.class.getClassLoader(), new Class<?>[] { Player.class }, h);
    }

    private static MenuEntry stubEntry() {
        Map<String, Object> state = new HashMap<>();
        InvocationHandler h = (proxy, m, args) -> {
            String n = m.getName();
            if (n.startsWith("set") && args != null && args.length == 1) {
                state.put(n, args[0]);
                return proxy;
            }
            if ((n.startsWith("get") || n.startsWith("is")) && (args == null || args.length == 0)) {
                String key = n.startsWith("is") ? "set" + n.substring(2) : "set" + n.substring(3);
                Object v = state.get(key);
                return v != null ? v : defaultValue(m.getReturnType());
            }
            return defaultValue(m.getReturnType());
        };
        return (MenuEntry) Proxy.newProxyInstance(
            Smoke.class.getClassLoader(), new Class<?>[] { MenuEntry.class }, h);
    }

    private static TileItem stubGroundItem(Tile tile) {
        InvocationHandler h = (proxy, m, args) -> switch (m.getName()) {
            case "getId" -> 995;
            case "getQuantity" -> 10;
            case "getTile" -> tile;
            default -> defaultValue(m.getReturnType());
        };
        return (TileItem) Proxy.newProxyInstance(
            Smoke.class.getClassLoader(), new Class<?>[] { TileItem.class }, h);
    }

    private static Tile stubTile(TileItem[] box) {
        InvocationHandler h = (proxy, m, args) -> switch (m.getName()) {
            case "getWorldLocation" -> new WorldPoint(3225, 3218, 0);
            case "getGameObjects" -> new GameObject[0];
            case "getGroundItems" -> List.of(box[0]);
            default -> defaultValue(m.getReturnType());
        };
        return (Tile) Proxy.newProxyInstance(
            Smoke.class.getClassLoader(), new Class<?>[] { Tile.class }, h);
    }

    private static Scene stubScene() {
        TileItem[] box = new TileItem[1];
        Tile tile = stubTile(box);
        box[0] = stubGroundItem(tile);
        Tile[][][] grid = new Tile[1][104][104];
        grid[0][50][50] = tile;
        InvocationHandler h = (proxy, m, args) -> switch (m.getName()) {
            case "getTiles" -> grid;
            default -> defaultValue(m.getReturnType());
        };
        return (Scene) Proxy.newProxyInstance(
            Smoke.class.getClassLoader(), new Class<?>[] { Scene.class }, h);
    }

    private static ItemComposition stubItemComp() {
        InvocationHandler h = (proxy, m, args) -> switch (m.getName()) {
            case "getName" -> "Coins";
            default -> defaultValue(m.getReturnType());
        };
        return (ItemComposition) Proxy.newProxyInstance(
            Smoke.class.getClassLoader(), new Class<?>[] { ItemComposition.class }, h);
    }

    private static ItemContainer stubContainer(Item[] items) {
        InvocationHandler h = (proxy, m, args) -> switch (m.getName()) {
            case "getItems" -> items;
            case "count" -> items.length;
            default -> defaultValue(m.getReturnType());
        };
        return (ItemContainer) Proxy.newProxyInstance(
            Smoke.class.getClassLoader(), new Class<?>[] { ItemContainer.class }, h);
    }

    private static Widget stubWidget() {
        InvocationHandler h = (proxy, m, args) -> switch (m.getName()) {
            case "getId" -> 123456;
            case "getIndex" -> 1;
            case "getName" -> "";
            case "getActions" -> new String[] { "Deposit inventory" };
            case "isHidden" -> false;
            default -> defaultValue(m.getReturnType());
        };
        return (Widget) Proxy.newProxyInstance(
            Smoke.class.getClassLoader(), new Class<?>[] { Widget.class }, h);
    }

    private static Client stubClient() {
        NPC cow = stubNpc();
        Player me = stubPlayer();
        Scene scene = stubScene();
        ItemComposition coins = stubItemComp();
        Widget widget = stubWidget();
        ItemContainer bank = stubContainer(new Item[] { new Item(995, 100) });
        ItemContainer empty = stubContainer(new Item[0]);
        InvocationHandler h = (proxy, m, args) -> switch (m.getName()) {
            case "getNpcs" -> List.of(cow);
            case "getPlayers" -> List.of();
            case "getLocalPlayer" -> me;
            case "getScene" -> scene;
            case "getPlane" -> 0;
            case "getItemDefinition" -> coins;
            case "createMenuEntry" -> stubEntry();
            case "getItemContainer" -> args[0] == InventoryID.BANK ? bank : empty;
            case "getWidget" -> args[0] == WidgetInfo.BANK_ITEM_CONTAINER
                || args[0] == WidgetInfo.BANK_DEPOSIT_INVENTORY
                || args[0] == WidgetInfo.BANK_DEPOSIT_EQUIPMENT ? widget : null;
            default -> defaultValue(m.getReturnType());
        };
        return (Client) Proxy.newProxyInstance(
            Smoke.class.getClassLoader(), new Class<?>[] { Client.class }, h);
    }
}
