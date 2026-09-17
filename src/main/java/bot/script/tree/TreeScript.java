package bot.script.tree;

import bot.script.Script;
import bot.script.ScriptManifest;

/** A script structured as a behavior tree. Mirrors DreamBot's `TreeScript`
 * (addBranches/clear/getRoot/getCurrentBranchName/onLoop): each loop runs
 * the root, which runs the first valid branch. Plain `Script` subclasses
 * keep working unchanged — this is purely additive for ported tree scripts. */
public abstract class TreeScript extends Script {
    private final Root root = new Root(this);
    private String currentBranch = "";

    /** Attach branches to the root. */
    public final Root addBranches(Leaf... branches) {
        for (Leaf leaf : branches) {
            if (leaf instanceof Branch branch) {
                root.addBranches(branch);
            } else if (leaf != null) {
                leaf.setTree(this);
                root.getChildren().add(leaf);
                leaf.setParent(root);
            }
        }
        return root;
    }

    /** Detach everything. */
    public final void clear() {
        root.clear();
        currentBranch = "";
    }

    public Root getRoot() {
        return root;
    }

    public String getCurrentBranchName() {
        return currentBranch;
    }

    @Override
    public int onLoop() {
        try {
            for (Leaf child : root.getChildren()) {
                if (child.isValid()) {
                    currentBranch = child.getClass().getSimpleName();
                    return child.onLoop();
                }
            }
        } catch (Exception e) {
            return 600;
        }
        return 600;
    }

    /** Manifest lookup helper for samples (annotations live on subclasses). */
    protected ScriptManifest manifest() {
        return getClass().getAnnotation(ScriptManifest.class);
    }
}
