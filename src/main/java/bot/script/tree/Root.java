package bot.script.tree;

/** The tree root: always valid, runs the first valid branch. Mirrors
 * DreamBot's `Root` (addBranches). Attaching propagates the tree link to
 * every descendant so leaves added before attachment still resolve it. */
public class Root extends Branch {
    private final TreeScript tree;

    public Root(TreeScript tree) {
        this.tree = tree;
        setTree(tree);
    }

    @Override
    public TreeScript getTree() {
        return tree;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    /** Attach branches (propagates tree links through descendants). */
    public final Root addBranches(Branch... branches) {
        for (Branch branch : branches) {
            if (branch != null) {
                propagate(branch);
                getChildren().add(branch);
                branch.setParent(this);
            }
        }
        return this;
    }

    private void propagate(Leaf leaf) {
        leaf.setTree(tree);
        if (leaf instanceof Branch branch) {
            for (Leaf child : branch.getChildren()) {
                child.setParent(branch);
                propagate(child);
            }
        }
    }
}
