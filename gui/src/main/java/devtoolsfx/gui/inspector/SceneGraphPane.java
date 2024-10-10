package devtoolsfx.gui.inspector;

import devtoolsfx.gui.ToolPane;
import devtoolsfx.scenegraph.Element;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
final class SceneGraphPane extends VBox {

    private final SearchField<TreeItem<Element>> searchField = new SearchField<>();
    private final SceneGraphTree tree;

    SceneGraphPane(ToolPane toolPane) {
        super();

        tree = new SceneGraphTree(toolPane);

        createLayout();
        initListeners();
    }

    void addOrUpdateWindow(Element element) {
        tree.addOrUpdateWindow(element);
    }

    void removeWindow(int uid) {
        tree.removeWindow(uid);
    }

    int getWindow(Element element) {
        return tree.getWindow(element);
    }

    void addTreeElement(Element element) {
        tree.addElement(element);
    }

    void removeTreeElement(Element element) {
        tree.removeElement(element);
    }

    @Nullable
    Element getSelectedTreeElement() {
        return tree.getSelectedElement();
    }

    void selectTreeElement(Element element) {
        tree.selectElement(element);
    }

    void clearTreeSelection() {
        tree.getSelectionModel().clearSelection();
    }

    @SuppressWarnings("unused")
    void updateTreeElementVisibilityState(Element element, boolean visibility) {
        tree.updateTreeElement(element);
        tree.refresh();
    }

    @SuppressWarnings("unused")
    void updateTreeElementStyleClass(Element element, List<String> styleClass) {
        tree.updateTreeElement(element);
        tree.refresh();
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        HBox.setHgrow(searchField, Priority.ALWAYS);

        var filterBox = new HBox(searchField);
        filterBox.getStyleClass().add("filter");

        VBox.setVgrow(tree, Priority.ALWAYS);

        setId("scenegraph-pane");
        getChildren().setAll(tree, filterBox);
    }

    private void initListeners() {
        searchField.setOnTextChange(() -> {
            var filter = searchField.getText();

            if (filter.length() <= 3) {
                clearSearchResult();
                return;
            }

            List<TreeItem<Element>> result = tree.search(filter);
            searchField.setNavigableResult(result);

            if (!result.isEmpty()) {
                searchField.navigateNext();
            }

            tree.refresh();
        });

        searchField.setNavigationHandler((position, item) -> tree.navigate(item));

        searchField.setOnClearButtonClick(() -> {
            searchField.setText(null);
            clearSearchResult();
        });
    }

    private void clearSearchResult() {
        tree.search(null);
        searchField.setNavigableResult(null);
        tree.refresh();
    }
}
