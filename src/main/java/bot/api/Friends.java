package bot.api;

import java.util.ArrayList;
import java.util.List;

import net.runelite.api.Friend;
import net.runelite.api.Ignore;
import net.runelite.api.NameableContainer;

/** Friends/ignore reads off the maintained containers. Mutating flows
 * (add/delete/message) type into the friends-tab chatbox in DreamBot with
 * no statically minable widget ids, so they stay out until a live pass —
 * reads are the covered surface. Pure reads unless noted. */
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
}
