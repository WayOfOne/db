package bot.api;

import java.util.List;

import net.runelite.api.FriendsChatManager;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.widgets.Widget;

/** Friends-chat + clan reads and chat join/leave. Reads come off the
 * maintained managers. Join/leave follow DreamBot's own `ClanChat` flow
 * (`results/javap-c-db-clanchat.txt`): chat tab, entry button, chatbox
 * typing for join, entry click + wait for leave. DreamBot's entry button
 * resolves through a runtime-decrypted holder, so the fork clicks the
 * visible join/leave action found at runtime instead (the same discovery
 * the GE collector uses) — NOT VERIFIED labels, want one live pass.
 * Game-only unless noted. */
public final class ClanChat {
    private ClanChat() {
    }

    private static FriendsChatManager chat() {
        return Game.client().getFriendsChatManager();
    }

    /** True while inside a friends chat. */
    public static boolean inChat() {
        return chat() != null;
    }

    /** True while inside the named friends chat. */
    public static boolean inChat(String name) {
        FriendsChatManager c = chat();
        return c != null && name != null && name.equalsIgnoreCase(c.getName());
    }

    /** Friends-chat name, or null when not in one. */
    public static String getName() {
        FriendsChatManager c = chat();
        return c == null ? null : c.getName();
    }

    /** Friends-chat owner, or null when not in one. */
    public static String getOwner() {
        FriendsChatManager c = chat();
        return c == null ? null : c.getOwner();
    }

    /** Friends-chat members, never null. */
    public static List<? extends net.runelite.api.Nameable> getMembers() {
        FriendsChatManager c = chat();
        return c == null || c.getMembers() == null ? List.of() : List.of(c.getMembers());
    }

    /** Friends-chat size. */
    public static int getSize() {
        FriendsChatManager c = chat();
        return c == null ? 0 : c.getCount();
    }

    /** Guild clan channel, or null when clanless. */
    public static ClanChannel guild() {
        return Game.client().getClanChannel();
    }

    /** Guild clan name, or null. */
    public static String guildName() {
        ClanChannel c = guild();
        return c == null ? null : c.getName();
    }

    /** Guild clan members, never null. */
    public static List<ClanChannelMember> guildMembers() {
        ClanChannel c = guild();
        return c == null || c.getMembers() == null ? List.of() : c.getMembers();
    }

    /** Open the friends-chat tab. Game-only. */
    public static boolean openTab() throws Exception {
        return Tabs.friendsChat();
    }

    /** Join a friends chat (tab, join action, chatbox typing, verify).
     * Already there (or elsewhere — leaves first, like DreamBot).
     * Game-only. */
    public static boolean join(String name) throws Exception {
        if (name == null) {
            return false;
        }
        if (inChat(name)) {
            return true;
        }
        if (inChat() && !leave()) {
            return false;
        }
        openTab();
        Widget join = Widgets.findByActionContaining("join");
        if (!Actions.widget(join)) {
            return false;
        }
        bot.util.Sleep.sleep(600, 1000);
        Keyboard.type(name);
        Keyboard.pressEnter();
        bot.util.Timing.waitCondition(() -> inChat(name), 5000);
        return inChat(name);
    }

    /** Leave the friends chat (tab, leave action, verify). Game-only. */
    public static boolean leave() throws Exception {
        if (!inChat()) {
            return true;
        }
        openTab();
        Widget leave = Widgets.findByActionContaining("leave");
        if (!Actions.widget(leave)) {
            return false;
        }
        bot.util.Timing.waitCondition(() -> !inChat(), 5000);
        return !inChat();
    }
}
