package bot.api.randoms;

import java.util.ArrayList;
import java.util.List;

/** Random-event manager. Holds the default dismiss-shaped solver set
 * (DreamBot's own `randoms/` package is dismiss-shaped too — no puzzle
 * solvers exist to mirror) and runs the first ready solver each call.
 * The bank PIN and re-login credentials are session-memory only, set
 * explicitly by the script author, never persisted or logged. */
public final class Randoms {
    private Randoms() {
    }

    private static final List<RandomSolver> SOLVERS = new ArrayList<>();
    private static String bankPin;

    static {
        SOLVERS.add(new DismissSolver());
        SOLVERS.add(new GenieSolver());
        SOLVERS.add(new WelcomeSolver());
        SOLVERS.add(new BankPinSolver());
        SOLVERS.add(new LoginSolver());
        SOLVERS.add(new BreakSolver());
    }

    /** Registered solvers (live list — enable/disable in place). */
    public static List<RandomSolver> solvers() {
        return SOLVERS;
    }

    /** Enable/disable a solver by name. */
    public static void setEnabled(String name, boolean on) {
        for (RandomSolver s : SOLVERS) {
            if (s.name().equalsIgnoreCase(name)) {
                s.setEnabled(on);
            }
        }
    }

    /** Hold the bank PIN for this session only (never persisted, never
     * logged). Null clears it. */
    public static void setBankPin(String pin) {
        bankPin = pin;
    }

    static String bankPin() {
        return bankPin;
    }

    /** Run the first ready, enabled solver; returns its requested delay,
     * or -1 when nothing applied (caller keeps its own loop pace). */
    public static int runOnce() throws Exception {
        for (RandomSolver s : SOLVERS) {
            if (s.isEnabled() && s.shouldExecute()) {
                return s.onLoop();
            }
        }
        return -1;
    }
}
