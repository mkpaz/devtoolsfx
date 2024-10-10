package devtoolsfx.gui.inspector;

import devtoolsfx.scenegraph.Element;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TreeItem;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
final class SceneGraphTreeItem extends TreeItem<Element> implements Comparable<TreeItem<Element>> {

    private final BooleanProperty filtered = new SimpleBooleanProperty();

    SceneGraphTreeItem(Element value) {
        super(value);
    }

    boolean getFiltered() {
        return filtered.get();
    }

    void setFiltered(boolean filtered) {
        this.filtered.set(filtered);
    }

    @Override
    public int compareTo(TreeItem<Element> other) {
        var thisPath = getPathIndices(this, new ArrayList<>()).reversed();
        var otherPath = getPathIndices(other, new ArrayList<>()).reversed();

        // a guard block, should not happen if not comparing with root
        if (thisPath.isEmpty() && !otherPath.isEmpty()) {
            return -1;
        }
        if (otherPath.isEmpty() && !thisPath.isEmpty()) {
            return 1;
        }

        for (int i = 0; i <= Math.min(thisPath.size(), otherPath.size()) - 1; i++) {
            // lesser number wins, because it's closer to the root
            int result = Integer.compare(thisPath.get(i), otherPath.get(i));
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    /**
     * Returns a list of indices that represent the position of the given item in the tree.
     * The list is in reverse order, from leaf to root.
     */
    private List<Integer> getPathIndices(TreeItem<Element> item, List<Integer> accumulator) {
        var parent = item.getParent();
        if (parent == null) {
            return accumulator;
        }

        accumulator.add(parent.getChildren().indexOf(item));
        getPathIndices(parent, accumulator);

        return accumulator;
    }
}
