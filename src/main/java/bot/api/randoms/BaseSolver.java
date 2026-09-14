package bot.api.randoms;

/** Shared enable/disable bookkeeping for solvers. */
public abstract class BaseSolver implements RandomSolver {
    private final String name;
    private boolean enabled = true;

    protected BaseSolver(String name) {
        this.name = name;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(boolean on) {
        this.enabled = on;
    }
}
