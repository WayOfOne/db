package bot.api;

/** Random-event dismissals — and nothing else. This is deliberately NOT a
 * solver: no event is identified, no puzzle is touched, no reward is
 * claimed. All it does is space-bar through Continue-style dialogue boxes
 * (the same input `Dialogs` already exposes) so a script is never wedged
 * on a modal. Option picks are decisions, not dismissals, so choosing
 * stops the loop — anything still open afterwards needs a human or a
 * solver this fork will never ship. Game-only unless noted. */
public final class RandomEvents {
    private RandomEvents() {
    }

    /** Space through up to 5 Continue-style dialogue boxes.
     * @return true when at least one box was dismissed. Game-only. */
    public static boolean dismiss() throws Exception {
        boolean did = false;
        for (int i = 0; i < 5 && Dialogs.isOpen(); i++) {
            if (Dialogs.choosing()) {
                break;
            }
            Dialogs.continueDialogue();
            did = true;
        }
        return did;
    }
}
