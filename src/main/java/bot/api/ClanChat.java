package bot.api;

import java.util.List;

import net.runelite.api.FriendsChatManager;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;

/** Friends-chat + clan reads off the maintained managers. Join/leave/type
 * flows have no statically minable widget ids in DreamBot's bytecode, so
 * they stay out until a live pass — reads are the covered surface.
 * Pure reads unless noted. */
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
}
