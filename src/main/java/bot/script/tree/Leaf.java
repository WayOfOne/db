package bot.script.tree;

import java.util.ArrayList;
import java.util.List;

/** A behavior-tree leaf: runs when valid. Mirrors DreamBot's `Leaf`
 * (isValid/onLoop plus tree/parent links) for ported tree scripts. */
public abstract class Leaf {
    private Branch parent;
    private TreeScript tree;

    /** True when this leaf applies right now. */
    public abstract boolean isValid();

    /** Do the work; returns ms to wait. */
    public abstract int onLoop() throws Exception;

    public TreeScript getTree() {
        return tree;
    }

    void setTree(TreeScript tree) {
        this.tree = tree;
    }

    public Branch getParent() {
        return parent;
    }

    public void setParent(Branch parent) {
        this.parent = parent;
    }
}
