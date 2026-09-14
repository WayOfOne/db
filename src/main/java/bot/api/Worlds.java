package bot.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.runelite.api.GameState;
import net.runelite.api.World;
import net.runelite.api.WorldType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** World list reads and switcher hopping. List data comes straight from the
 * maintained `Client.getWorldList` (id, players, activity, types — the
 * client populates it, no server calls of ours). The hop flow is mined from
 * DreamBot's own `WorldHopper` (`results/javap-c-db-hop.txt`): the switcher
 * list lives at (69, 18) — the pinned `WORLD_SWITCHER_LIST` agrees — rows
 * match by visible number at runtime and hop through the row's own switch
 * action (the same runtime-discovery the GE collector uses for Collect).
 * No `JSocket`-style server comms exists here by design. Game-only unless
 * noted. */
public final class Worlds {
    private Worlds() {
    }

    /** Current world number. Pure read. */
    public static int current() {
        return Game.client().getWorld();
    }

    /** All known worlds, never null (empty when the list hasn't loaded). */
    public static List<World> all() {
        World[] worlds = Game.client().getWorldList();
        if (worlds == null) {
            return List.of();
        }
        List<World> out = new ArrayList<>();
        for (World w : worlds) {
            if (w != null) {
                out.add(w);
            }
        }
        return out;
    }

    /** World by id, or null. Pure read. */
    public static World get(int id) {
        for (World w : all()) {
            if (w.getId() == id) {
                return w;
            }
        }
        return null;
    }

    /** Members worlds / free worlds. Pure reads. */
    public static List<World> members() {
        return filter(w -> w.getTypes().contains(WorldType.MEMBERS));
    }

    /** Members worlds / free worlds. Pure reads. */
    public static List<World> f2p() {
        return filter(w -> !w.getTypes().contains(WorldType.MEMBERS));
    }

    /** PvP / high-risk worlds. Pure reads. */
    public static List<World> pvp() {
        return filter(w -> w.getTypes().contains(WorldType.PVP));
    }

    /** PvP / high-risk worlds. Pure reads. */
    public static List<World> highRisk() {
        return filter(w -> w.getTypes().contains(WorldType.HIGH_RISK));
    }

    /** Worlds whose activity text contains {@code text} (case-insensitive).
     * Pure read. */
    public static List<World> byActivity(String text) {
        if (text == null) {
            return List.of();
        }
        String want = text.toLowerCase(Locale.ROOT);
        return filter(w -> w.getActivity() != null
            && w.getActivity().toLowerCase(Locale.ROOT).contains(want));
    }

    /** Emptiest world, or null when the list is empty. Pure read. */
    public static World leastPopulated() {
        return all().stream()
            .min(Comparator.comparingInt(World::getPlayerCount))
            .orElse(null);
    }

    private static List<World> filter(java.util.function.Predicate<World> keep) {
        List<World> out = new ArrayList<>();
        for (World w : all()) {
            if (keep.test(w)) {
                out.add(w);
            }
        }
        return out;
    }

    /** True while the world switcher list is showing. */
    public static boolean isOpen() {
        return Widgets.child(WidgetIds.WORLD_SWITCHER_GROUP, WidgetIds.WORLD_SWITCHER_LIST)
            != null;
    }

    /** Open the switcher via the logout-tab button
     * (probe-verified 182,3). Game-only. */
    public static boolean open() throws Exception {
        Tabs.logout();
        return Actions.widget(Game.client().getWidget(WidgetInfo.WORLD_SWITCHER_BUTTON));
    }

    /** Hop to a world: open the switcher, click the row's own switch action,
     * wait for the world to change. Fails clean when the list is closed or
     * the row is missing. Game-only. */
    public static boolean hop(int id) throws Exception {
        if (current() == id) {
            return true;
        }
        if (!isOpen() && !open()) {
            return false;
        }
        Widget list = Widgets.child(WidgetIds.WORLD_SWITCHER_GROUP, WidgetIds.WORLD_SWITCHER_LIST);
        if (list == null || list.getChildren() == null) {
            return false;
        }
        for (Widget row : list.getChildren()) {
            if (row == null || row.isHidden() || !mentions(row.getText(), id)) {
                continue;
            }
            String option = switchOption(row.getActions());
            if (option == null) {
                return false;
            }
            if (!Actions.widget(row, option)) {
                return false;
            }
            return bot.util.Timing.waitCondition(() -> current() == id, 15_000)
                && Game.client().getGameState() == GameState.LOGGED_IN;
        }
        return false;
    }

    /** True when the stripped text names the world as a standalone number. */
    static boolean mentions(String text, int id) {
        if (text == null) {
            return false;
        }
        String[] parts = text.replaceAll("<[^>]*>", " ").trim().split("[^0-9]+");
        for (String p : parts) {
            if (!p.isEmpty() && Integer.parseInt(p) == id) {
                return true;
            }
        }
        return false;
    }

    private static String switchOption(String[] actions) {
        if (actions == null) {
            return null;
        }
        for (String a : actions) {
            if (a != null && a.toLowerCase(Locale.ROOT).contains("switch")) {
                return a;
            }
        }
        return null;
    }
}
