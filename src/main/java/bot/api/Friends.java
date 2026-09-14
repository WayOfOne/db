package bot.api;

import java.util.ArrayList;
import java.util.List;

import net.runelite.api.Friend;
import net.runelite.api.Ignore;
import net.runelite.api.NameableContainer;

/** Friends/ignore reads and mutations. Reads come off the maintained
 * containers; mutations follow DreamBot's own `Friends` flow
 * (`results/javap-c-db-friends.txt`): friends tab, entry button
 * ([429,14] add / [429,16] delete / [429,11] message), chatbox typing,
 * verification through the list reads. Ignore-list mutations have no
 * DreamBot counterpart and stay out. Game-only unless noted. */
public final class Friends {
    private Friends() {
    }

    private static NameableContainer<Friend> friends() {
        return Game.client().getFriendContainer();
    }

    /** All friends on the list, never null. */
    public static List<Friend> all() {
        NameableContainer<Friend> c = friends();
        if (c == null || c.getMembers() == null) {
            return List.of();
        }
        List<Friend> out = new ArrayList<>();
        for (Friend f : c.getMembers()) {
            if (f != null) {
                out.add(f);
            }
        }
        return out;
    }

    /** Friend count (DreamBot `getSize` parity). */
    public static int size() {
        NameableContainer<Friend> c = friends();
        return c == null ? 0 : c.getCount();
    }

    /** True when the name is on the friends list (case-insensitive). */
    public static boolean haveFriend(String name) {
        if (name == null) {
            return false;
        }
        NameableContainer<Friend> c = friends();
        return c != null && c.findByName(name) != null;
    }

    /** All ignored names, never null. */
    public static List<Ignore> ignores() {
        NameableContainer<Ignore> c = Game.client().getIgnoreContainer();
        if (c == null || c.getMembers() == null) {
            return List.of();
        }
        List<Ignore> out = new ArrayList<>();
        for (Ignore i : c.getMembers()) {
            if (i != null) {
                out.add(i);
            }
        }
        return out;
    }

    /** True when the name is ignored (case-insensitive). */
    public static boolean isIgnored(String name) {
        if (name == null) {
            return false;
        }
        NameableContainer<Ignore> c = Game.client().getIgnoreContainer();
        return c != null && c.findByName(name) != null;
    }

    /** Open the friends tab (mined DreamBot `swapToFriends` clicks [432, 1];
     * the pinned tab constant is the maintained equivalent). Game-only. */
    public static boolean openTab() throws Exception {
        return Tabs.friends();
    }

    /** True when the friend is online (world != 0). */
    public static boolean isOnline(String name) {
        if (name == null) {
            return false;
        }
        NameableContainer<Friend> c = friends();
        if (c == null) {
            return false;
        }
        Friend f = c.findByName(name);
        return f != null && f.getWorld() != 0;
    }

    private static boolean promptAndType(int buttonChild, String name) throws Exception {
        openTab();
        if (!Actions.widget(Widgets.child(WidgetIds.FRIENDS_GROUP, buttonChild))) {
            return false;
        }
        bot.util.Sleep.sleep(600, 1000);
        Keyboard.type(name);
        Keyboard.pressEnter();
        return true;
    }

    /** Add a friend (tab, entry button, chatbox typing, list verify).
     * Game-only. */
    public static boolean addFriend(String name) throws Exception {
        if (name == null || haveFriend(name)) {
            return haveFriend(name);
        }
        if (!promptAndType(WidgetIds.FRIENDS_ADD, name)) {
            return false;
        }
        bot.util.Timing.waitCondition(() -> haveFriend(name), 5000);
        return haveFriend(name);
    }

    /** Delete a friend (same flow through the delete entry). Game-only. */
    public static boolean deleteFriend(String name) throws Exception {
        if (name == null || !haveFriend(name)) {
            return !haveFriend(name);
        }
        if (!promptAndType(WidgetIds.FRIENDS_DELETE, name)) {
            return false;
        }
        bot.util.Timing.waitCondition(() -> !haveFriend(name), 5000);
        return !haveFriend(name);
    }

    /** Private-message an online friend (tab, message entry, chatbox
     * typing). Game-only. */
    public static boolean sendMessage(String name, String text) throws Exception {
        if (name == null || text == null || !isOnline(name)) {
            return false;
        }
        openTab();
        if (!Actions.widget(Widgets.child(WidgetIds.FRIENDS_GROUP, WidgetIds.FRIENDS_MESSAGE))) {
            return false;
        }
        bot.util.Sleep.sleep(600, 1000);
        Keyboard.type(text);
        Keyboard.pressEnter();
        return true;
    }
}
