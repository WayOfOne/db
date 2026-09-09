package bot.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** Finding interface elements by content rather than ids. The recursive walk
 * covers roots and children; text/action matching is exact-after-trim on the
 * widget's own strings. */
public final class Widgets {
    private Widgets() {
    }

    public static List<Widget> findAll(Predicate<Widget> match) {
        List<Widget> out = new ArrayList<>();
        Widget[] roots = Game.client().getWidgetRoots();
        if (roots == null) {
            return out;
        }
        for (Widget r : roots) {
            walk(r, match, out);
        }
        return out;
    }

    private static void walk(Widget w, Predicate<Widget> match, List<Widget> out) {
        if (w == null) {
            return;
        }
        if (match.test(w)) {
            out.add(w);
        }
        Widget[] children = w.getChildren();
        if (children != null) {
            for (Widget c : children) {
                walk(c, match, out);
            }
        }
    }

    /** First visible widget whose text equals {@code text}. */
    public static Widget findByText(String text) {
        List<Widget> found = findAll(w -> !w.isHidden() && text.equals(strip(w.getText())));
        return found.isEmpty() ? null : found.get(0);
    }

    /** First visible widget exposing {@code action} in its menu actions. */
    public static Widget findByAction(String action) {
        List<Widget> found = findAll(w -> {
            if (w.isHidden()) {
                return false;
            }
            String[] actions = w.getActions();
            if (actions == null) {
                return false;
            }
            for (String a : actions) {
                if (action.equals(strip(a))) {
                    return true;
                }
            }
            return false;
        });
        return found.isEmpty() ? null : found.get(0);
    }

    /** Widget by known id, or null when absent/hidden. */
    public static Widget first(WidgetInfo info) {
        Widget w = Game.client().getWidget(info);
        return w != null && !w.isHidden() ? w : null;
    }

    private static String strip(String s) {
        return s == null ? null : s.replaceAll("<[^>]*>", "").trim();
    }
}
