package bot.script.listener;

import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;

/** Chat callbacks. Method names mirror DreamBot's `ChatListener`; payloads
 * are the maintained `ChatMessage` (sender + text) with per-type fan-out
 * done by the dispatcher (`bot.script.Events`). */
public interface ChatListener extends java.util.EventListener {
    /** Every chat line, before typed fan-out. */
    default void onMessage(ChatMessage message) {
    }

    /** Game and console lines. */
    default void onGameMessage(String sender, String message) {
    }

    /** Public chat lines. */
    default void onPlayerMessage(String sender, String message) {
    }

    /** Incoming private messages. */
    default void onPrivateInMessage(String sender, String message) {
    }

    /** Outgoing private messages. */
    default void onPrivateOutMessage(String sender, String message) {
    }

    /** Friends-chat and clan lines. */
    default void onClanMessage(String sender, String message) {
    }

    /** Trade lines. */
    default void onTradeMessage(String sender, String message) {
    }

    /** Route one line to its typed callbacks. Pure logic, unit-tested. */
    static void dispatch(ChatListener target, ChatMessage message) {
        target.onMessage(message);
        String sender = message.getName();
        String text = message.getMessage();
        ChatMessageType type = message.getType();
        switch (type) {
            case GAMEMESSAGE, CONSOLE, ENGINE, BROADCAST, WELCOME, ITEM_EXAMINE,
                 NPC_EXAMINE, OBJECT_EXAMINE, SPAM, DIALOG -> target.onGameMessage(sender, text);
            case PUBLICCHAT, MODCHAT, AUTOTYPER, MODAUTOTYPER -> target.onPlayerMessage(sender, text);
            case PRIVATECHAT, MODPRIVATECHAT -> target.onPrivateInMessage(sender, text);
            case PRIVATECHATOUT -> target.onPrivateOutMessage(sender, text);
            case FRIENDSCHAT, FRIENDSCHATNOTIFICATION, CLAN_CHAT, CLAN_MESSAGE,
                 CLAN_GUEST_CHAT, CLAN_GUEST_MESSAGE, CLAN_GIM_CHAT,
                 CLAN_GIM_MESSAGE -> target.onClanMessage(sender, text);
            case TRADE, TRADEREQ, TRADE_SENT -> target.onTradeMessage(sender, text);
            default -> {
            }
        }
    }
}
