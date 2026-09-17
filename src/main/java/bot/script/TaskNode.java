package bot.script;

/** One prioritized task: highest-priority accepted node runs each loop.
 * Mirrors DreamBot's `TaskNode` (priority/accept/execute) so ported
 * task scripts keep their shape; sleeping goes through `bot.util`. */
public abstract class TaskNode {
    /** Higher runs first when several accept. */
    public int priority() {
        return 0;
    }

    /** True when this task applies right now. */
    public abstract boolean accept();

    /** Do the work; returns ms to wait before the next loop. */
    public abstract int execute() throws Exception;

    /** Run the best accepted node; -1 when none applied. Pure dispatch. */
    public static int run(java.util.List<TaskNode> nodes) throws Exception {
        TaskNode best = null;
        for (TaskNode n : nodes) {
            if (n != null && n.accept()
                && (best == null || n.priority() > best.priority())) {
                best = n;
            }
        }
        return best == null ? -1 : best.execute();
    }
}
