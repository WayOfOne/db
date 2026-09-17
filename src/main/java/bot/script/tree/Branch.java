package bot.script.tree;

import java.util.ArrayList;
import java.util.List;

/** A branch: runs its first valid child leaf each loop. Mirrors DreamBot's
 * `Branch` (addLeaves/clear/getChildren/onLoop). */
public abstract class Branch extends Leaf {
    private final List<Leaf> children = new ArrayList<>();

    /** A branch is valid when any child is. */
    @Override
    public boolean isValid() {
        for (Leaf child : children) {
            if (child.isValid()) {
                return true;
            }
        }
        return false;
    }

    /** Run the first valid child. */
    @Override
    public int onLoop() throws Exception {
        for (Leaf child : children) {
            if (child.isValid()) {
                return child.onLoop();
            }
        }
        return 600;
    }

    /** Attach leaves (sets their parent/tree links). */
    public final Branch addLeaves(Leaf... leaves) {
        for (Leaf leaf : leaves) {
            if (leaf != null) {
                leaf.setParent(this);
                leaf.setTree(getTree());
                children.add(leaf);
            }
        }
        return this;
    }

    /** Detach all children. */
    public final void clear() {
        children.clear();
    }

    public List<Leaf> getChildren() {
        return children;
    }
}
