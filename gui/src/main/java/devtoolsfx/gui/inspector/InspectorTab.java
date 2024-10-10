package devtoolsfx.gui.inspector;

import devtoolsfx.gui.ToolPane;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.attributes.Attribute;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import javafx.scene.control.SplitPane;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public final class InspectorTab extends SplitPane {

    public static final String TAB_NAME = "Inspector";

    private final SceneGraphPane sceneGraphPane;
    private final AttributePane attributePane;

    public InspectorTab(ToolPane toolPane) {
        super();

        sceneGraphPane = new SceneGraphPane(toolPane);
        attributePane = new AttributePane(toolPane);

        createLayout();
    }

    /**
     * Adds or updates a window element in the scenegraph tree.
     */
    public void addOrUpdateWindow(Element element) {
        sceneGraphPane.addOrUpdateWindow(element);
    }

    /**
     * Removes window element from the scenegraph tree.
     */
    public void removeWindow(int uid) {
        sceneGraphPane.removeWindow(uid);
    }

    /**
     * Returns the UID of the window containing the specified element, or zero if not found.
     */
    public int getWindow(Element element) {
        return sceneGraphPane.getWindow(element);
    }

    /**
     * Adds a new node element to the scenegraph tree.
     */
    public void addTreeElement(Element element) {
        sceneGraphPane.addTreeElement(element);
    }

    /**
     * Adds node element from the scenegraph tree.
     */
    public void removeTreeElement(Element element) {
        sceneGraphPane.removeTreeElement(element);
    }

    /**
     * Returns the selected element from the scenegraph tree.
     */
    @Nullable
    public Element getSelectedTreeElement() {
        return sceneGraphPane.getSelectedTreeElement();
    }

    /**
     * Selects the specified element in the scenegraph tree.
     */
    public void selectTreeElement(Element element) {
        sceneGraphPane.selectTreeElement(element);
    }

    /**
     * Clears the scenegraph tree selection.
     */
    @SuppressWarnings("unused")
    public void clearTreeSelection() {
        sceneGraphPane.clearTreeSelection();
    }

    /**
     * Updates the specified element visibility state in the scenegraph tree.
     */
    public void updateTreeElementVisibilityState(Element element, boolean visibility) {
        sceneGraphPane.updateTreeElementVisibilityState(element, visibility);
    }

    /**
     * Updates the specified element style class list in the scenegraph tree.
     */
    public void updateTreeElementStyleClass(Element element, List<String> styleClass) {
        sceneGraphPane.updateTreeElementStyleClass(element, styleClass);
    }

    /**
     * Sets (replaces) the list of displayed attributes.
     */
    public void setAttributes(AttributeCategory category, List<Attribute<?>> attributes) {
        attributePane.setAttributes(category, attributes);
    }

    /**
     * Updates a single attribute value.
     */
    public void updateAttribute(AttributeCategory category, Attribute<?> attribute) {
        attributePane.updateAttribute(category, attribute);
    }

    /**
     * Clears the list of displayed attributes.
     */
    public void clearAttributes() {
        attributePane.clearAttributes();
        attributePane.setFilter("");
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        setId("inspector-tab");
        getStyleClass().add("tab");
        setDividerPositions(0.4);
        SplitPane.setResizableWithParent(sceneGraphPane, false);

        getItems().addAll(sceneGraphPane, attributePane);
    }
}
