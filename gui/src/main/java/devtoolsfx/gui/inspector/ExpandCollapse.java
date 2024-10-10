package devtoolsfx.gui.inspector;

import devtoolsfx.scenegraph.Element;
import javafx.scene.control.TreeItem;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Accumulates expand/collapse logic for the tree items.
 */
@NullMarked
final class ExpandCollapse<T> {

    private final Set<T> expandItems = new HashSet<>();
    private final Set<T> collapseItems = new HashSet<>();
    private final Function<TreeItem<Element>, T> fun;

    public ExpandCollapse(Function<TreeItem<Element>, T> fun) {
        this.fun = fun;
    }

    /**
     * Toggles the expand/collapse state of the given item. If the item is already
     * present, it will be removed; otherwise, it will be added to either the expanded
     * or collapsed items, depending on the toggle flag.
     */
    void toggle(TreeItem<Element> item, boolean expand) {
        var t = fun.apply(item);
        if (expand) {
            if (expandItems.contains(t)) {
                expandItems.remove(t);
            } else {
                expandItems.add(t);
            }
        } else {
            if (collapseItems.contains(t)) {
                collapseItems.remove(t);
            } else {
                collapseItems.add(t);
            }
        }
    }

    /**
     * Shortcut for {@link #toggle(TreeItem, boolean)}.
     */
    void expand(TreeItem<Element> item) {
        toggle(item, true);
    }

    /**
     * Shortcut for {@link #toggle(TreeItem, boolean)}.
     */
    void collapse(TreeItem<Element> item) {
        toggle(item, false);
    }

    /**
     * Checks whether the given item is present in the "expanded list".
     */
    boolean isExpanded(TreeItem<Element> item) {
        return expandItems.contains(fun.apply(item));
    }

    /**
     * Checks whether the given item is present in the "collapsed list".
     */
    boolean isCollapsed(TreeItem<Element> item) {
        return collapseItems.contains(fun.apply(item));
    }
}
