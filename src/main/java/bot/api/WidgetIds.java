package bot.api;

/** Interface widget ids, read out of DreamBot's own API bytecode
 * ({@code results/widget-id-sites.txt}; extraction: {@code tools/ExtractWidgetIds.py}).
 * Each constant cites the DreamBot method that uses it. Group ids are game
 * content ids (same category as item ids) — far stabler than obfuscated names —
 * but every flow built on them still wants one live pass, noted per method. */
public final class WidgetIds {
    private WidgetIds() {
    }

    // -- Grand Exchange (group 465) --
    /** Offer slots 0-7 live at child 7+slot; child 3 = buy button, child 4 = sell button. */
    public static final int GE_GROUP = 465;
    public static final int GE_SLOT_BASE = 7;
    public static final int GE_BUY_CHILD = 3;
    public static final int GE_SELL_CHILD = 4;
    /** General-open check widget (DreamBot isGeneralOpen). */
    public static final int GE_OPEN_CHECK = 5;
    /** Abort/cancel button (DreamBot cancelOffer via helper 2O). */
    public static final int GE_ABORT = 4;
    /** Collect screen root (helper 3); child 0 is the Collect button. */
    public static final int GE_COLLECT = 6;
    public static final int GE_COLLECT_BUTTON = 0;
    /** Confirm buttons: 30 then 1, mirroring DreamBot confirm()'s fallback order. */
    public static final int GE_CONFIRM_A = 30;
    public static final int GE_CONFIRM_B = 1;
    /** Offer-item widgets (DreamBot getOfferFirst/SecondItemWidget). */
    public static final int GE_OFFER_ITEMS = 24;
    /** Quantity/price panel (children below). */
    public static final int GE_QTY_PRICE = 26;
    /** Children of GE_QTY_PRICE: quantity, all, price, guide (DreamBot getters). */
    public static final int GE_QTY_CHILD = 7;
    public static final int GE_ALL_CHILD = 6;
    public static final int GE_PRICE_CHILD = 12;
    public static final int GE_GUIDE_CHILD = 11;

    // -- Chatbox input (group 162): item search box + results --
    public static final int SEARCH_GROUP = 162;
    public static final int SEARCH_BOX = 44;
    public static final int SEARCH_RESULTS = 52;

    // -- Trade: main 335, confirm 334 (DreamBot acceptTrade/declineTrade) --
    public static final int TRADE_MAIN = 335;
    public static final int TRADE_CONFIRM = 334;
    public static final int TRADE_ACCEPT_MAIN = 10;
    public static final int TRADE_ACCEPT_CONFIRM = 13;
    public static final int TRADE_DECLINE = 13;
    public static final int TRADE_TITLE_MAIN = 31;
    public static final int TRADE_TITLE_CONFIRM = 30;

    // -- Deposit box (group 192; DreamBot widgetParentId/widgetChildId) --
    public static final int DEPOSIT_GROUP = 192;
    /** Items container; slots are its children, slot-indexed. */
    public static final int DEPOSIT_ITEMS = 24;
    /** Close button root; child 11 is the button (Escape is primary). */
    public static final int DEPOSIT_CLOSE = 1;
    public static final int DEPOSIT_CLOSE_BUTTON = 11;
    /** Deposit-all-items / equipment / loot buttons. */
    public static final int DEPOSIT_ALL_ITEMS = 31;
    public static final int DEPOSIT_ALL_EQUIPMENT = 30;
    public static final int DEPOSIT_ALL_LOOT = 32;

    // -- Bank (group 12; used via WidgetInfo constants, listed for reference) --
    public static final int BANK_GROUP = 12;

    // -- Combat tab (group 593) --
    /** Attack-style buttons, index 0-3 (DreamBot Combat field "0";
     * identical to WidgetInfo COMBAT_STYLE_ONE..FOUR, probe-verified). */
    public static final int COMBAT_GROUP = 593;
    public static final int COMBAT_STYLE_CHILDREN = 6; // +4 per style: 6, 10, 14, 18
    /** Special-attack button on the combat tab (DreamBot Combat "8"). */
    public static final int COMBAT_SPEC_CHILD = 38;
    /** Auto-retaliate box (== WidgetInfo COMBAT_AUTO_RETALIATE, probe-verified). */
    public static final int COMBAT_RETALIATE_CHILD = 32;

    // -- Special-attack orb by the minimap (group 160) --
    /** Spec orb widget (DreamBot Combat "7"; orb cluster, cf. orb at 160,20). */
    public static final int SPEC_ORB_GROUP = 160;
    public static final int SPEC_ORB_CHILD = 36;

    // -- Prayer book (group 541) + quick prayers (group 77) --
    /** Standard prayer book; per-prayer children in results/prayer-widget-table.txt
     * (DreamBot Prayer.getWidgetIndex/getChildIndex). */
    public static final int PRAYER_BOOK_GROUP = 541;
    /** Quick-prayer setup root (== WidgetInfo QUICK_PRAYER_PRAYERS, probe-verified);
     * per-prayer grandchildren in results/prayer-widget-table.txt. */
    public static final int QUICK_PRAYER_GROUP = 77;
    public static final int QUICK_PRAYER_CHILD = 4;

    // -- Emote book (group 216) --
    /** Scroll root (DreamBot Emotes doEmote) and emote container
     * (DreamBot Emotes getEmoteChild: [216, 2, child]); per-emote
     * children in results/emote-widget-table.txt. */
    public static final int EMOTE_GROUP = 216;
    public static final int EMOTE_SCROLL = 1;
    public static final int EMOTE_CONTAINER = 2;

    // -- Friends tab switch (group 432) --
    /** Friends/ignore tab button (DreamBot Friends swapToFriends). */
    public static final int FRIENDS_TAB_GROUP = 432;
    public static final int FRIENDS_TAB_CHILD = 1;

    // -- Friends tab interface (group 429) --
    /** Message / add / delete entry buttons (DreamBot Friends
     * sendMessage/addFriend/deleteFriend). */
    public static final int FRIENDS_GROUP = 429;
    public static final int FRIENDS_MESSAGE = 11;
    public static final int FRIENDS_ADD = 14;
    public static final int FRIENDS_DELETE = 16;

    // -- Shop (group 300) + smithing (group 312) --
    /** Stock list (== WidgetInfo SHOP_INVENTORY_ITEMS_CONTAINER,
     * probe-verified). DreamBot's own Shop resolves its parent through a
     * runtime-decrypted holder — no DreamBot-side id to mine. */
    public static final int SHOP_GROUP = 300;
    /** Smithing item list (== WidgetInfo SMITHING_INVENTORY_ITEMS_CONTAINER,
     * probe-verified). DreamBot ships no smithing class. */
    public static final int SMITHING_GROUP = 312;

    // -- Fairy rings (group 398, log 381, dial varp 816) --
    /** Confirm/teleport button (DreamBot FairyRings "5"; ==
     * WidgetInfo FAIRY_RING_TELEPORT_BUTTON, probe-verified). */
    public static final int FAIRY_GROUP = 398;
    public static final int FAIRY_CONFIRM = 26;
    /** Dial widgets, slot 0-2 (DreamBot FairyRings "2" via table "0f"). */
    public static final int[] FAIRY_DIALS = {19, 21, 23};
    /** Travel-log list (DreamBot FairyRings "0"). */
    public static final int FAIRY_LOG_GROUP = 381;
    public static final int FAIRY_LOG_CHILD = 7;
    /** Dial state varp (DreamBot FairyRings getCode: masks [3,12,48],
     * shift slot*2, letters [a,d,c,b]/[i,l,k,j]/[p,s,r,q]). */
    public static final int FAIRY_VARP = 816;

    // -- Minigame teleports (group 76, clan interface) --
    /** Selected-entry text (DreamBot MinigameTeleports `9`), entry list
     * (`9(Minigame)` picker), confirm button (`8i`). */
    public static final int MINIGAME_GROUP = 76;
    public static final int MINIGAME_SELECTED = 11;
    public static final int MINIGAME_LIST = 22;
    public static final int MINIGAME_CONFIRM = 32;

    /** Quest points varp (DreamBot Quests.getQuestPoints, one-line read). */
    public static final int QUEST_POINTS_VARP = 101;

    // -- Music (group 239) --
    /** Music-tab interface (DreamBot Music player widgets). */
    public static final int MUSIC_GROUP = 239;

    // -- Quest journal (group 399) --
    /** Journal rows (DreamBot Quest.getParent() == 399 default;
     * getChild() == 7 constant in all three books; per-quest rows in
     * results/quest-table.txt). */
    public static final int QUEST_GROUP = 399;
    public static final int QUEST_LIST = 7;

    // -- Spellbooks (group 218, all books) --
    /** Spell widgets (DreamBot per-book getParent() == 218 for Normal,
     * Ancient, Lunar, Arceuus; == pinned home-teleport group).
     * Per-spell children + levels in results/spell-table.txt. */
    public static final int SPELL_GROUP = 218;

    // -- World switcher (group 69; button 182,3) --
    /** List root (DreamBot WorldHopper `0`; == WidgetInfo
     * WORLD_SWITCHER_LIST, probe-verified) and sibling rows; sort headers
     * (DreamBot hopWorld branches). */
    public static final int WORLD_SWITCHER_GROUP = 69;
    public static final int WORLD_SWITCHER_LIST = 18;
    public static final int WORLD_SWITCHER_ROW_ALT = 17;
    public static final int WORLD_SWITCHER_SORT_A = 23;
    public static final int WORLD_SWITCHER_SORT_B = 24;
    /** Switcher button (== WidgetInfo WORLD_SWITCHER_BUTTON,
     * probe-verified). */
    public static final int WORLD_SWITCHER_BUTTON_GROUP = 182;
    public static final int WORLD_SWITCHER_BUTTON_CHILD = 3;

    // -- Bank PIN (group 213) + welcome screen (group 378) --
    /** Pin interface root (DreamBot BankPinSolver `6()`; packed id 213).
     * Digit slots at root children +3..+6 ("?" needs entry); digit
     * buttons at +16+2i matched by child(1) label. */
    public static final int BANK_PIN_GROUP = 213;
    /** Welcome-screen close widget (DreamBot WelcomeScreenSolver `4()`). */
    public static final int WELCOME_GROUP = 378;
    public static final int WELCOME_CLOSE = 72;
}
