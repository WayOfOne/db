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
}
