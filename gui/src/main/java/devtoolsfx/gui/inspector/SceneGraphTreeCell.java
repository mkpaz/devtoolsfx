package devtoolsfx.gui.inspector;

import devtoolsfx.gui.util.Formatters;
import devtoolsfx.scenegraph.Element;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class SceneGraphTreeCell extends TreeCell<Element> {

    static final PseudoClass HIDDEN = PseudoClass.getPseudoClass("hidden");
    static final PseudoClass FILTERED = PseudoClass.getPseudoClass("filtered");

    private final Label label = new Label();

    public SceneGraphTreeCell() {
        super();
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void updateItem(Element element, boolean empty) {
        super.updateItem(element, empty);

        if (empty || element == null) {
            pseudoClassStateChanged(HIDDEN, false);
            pseudoClassStateChanged(FILTERED, false);

            label.setText(null);
            setText(null);
            setGraphic(null);

            return;
        }

        label.setText(Formatters.formatForTreeItem(element));
        setGraphic(label);

        pseudoClassStateChanged(HIDDEN, isHidden(element));
        pseudoClassStateChanged(FILTERED, isFiltered(getTreeItem()));
    }

    private boolean isHidden(@Nullable Element element) {
        if (element == null || !element.isNodeElement()) {
            return false;
        }

        var props = element.getNodeProperties();
        if (props != null && !props.isVisible()) {
            return true;
        }

        return isHidden(element.getParent());
    }

    private boolean isFiltered(@Nullable TreeItem<?> item) {
        return item instanceof SceneGraphTreeItem sg && sg.getFiltered();
    }
}
