package devtoolsfx.gui.inspector;

import devtoolsfx.connector.Connector;
import devtoolsfx.connector.ConnectorOptions;
import devtoolsfx.gui.ToolPane;
import devtoolsfx.gui.Preferences;
import devtoolsfx.gui.util.DummyElement;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.WindowProperties.WindowType;
import javafx.scene.control.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.*;
import java.util.stream.Collectors;

@NullMarked
final class SceneGraphTree extends TreeView<Element> {

    private static final Logger LOGGER = System.getLogger(SceneGraphTree.class.getName());

    private final ToolPane toolPane;
    private final TreeItem<Element> treeRoot;
    private final Map<Element, TreeItem<Element>> treeIndex = new HashMap<>();

    private final ExpandCollapse<Integer> forcedNodes = new ExpandCollapse<>(
        item -> item.getValue().hashCode()
    );
    private final ExpandCollapse<String> forcedTypes = new ExpandCollapse<>(
        item -> item.getValue().getSimpleClassName()
    );

    private @Nullable ContextMenu contextMenu;
    private boolean blockSelection;

    SceneGraphTree(ToolPane toolPane) {
        this.toolPane = toolPane;

        treeRoot = new SceneGraphTreeItem(new DummyElement(""));
        treeRoot.setExpanded(true);

        setId("scene-graph-tree");
        setRoot(treeRoot);
        setShowRoot(false);
        setCellFactory(c -> new SceneGraphTreeCell());

        initListeners();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public API                                                            //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds a new window element or updates the existing one with a new root.
     */
    void addOrUpdateWindow(Element element) {
        if (!element.isWindowElement()) {
            LOGGER.log(Level.WARNING, "Adding or updating a window is only possible with a window type of element");
            return;
        }

        // clear selection if the currently selected item belongs to the replaced window branch
        boolean clearSelection = false;
        if (!getSelectionModel().isEmpty()) {
            var window = findParentWindowItem(getSelectionModel().getSelectedItem());
            if (window != null && Objects.equals(window.getValue(), element)) {
                clearSelection = true;
            }
        }

        blockSelection = true;
        treeRoot.getChildren().removeIf(item ->
            item.getValue().isWindowElement() && Objects.equals(item.getValue(), element)
        );
        treeRoot.getChildren().add(createTreeBranch(element));
        blockSelection = false;

        if (clearSelection) {
            clearConnectorSelection();
        }
    }

    /**
     * Removes the window element (the entire branch) from the tree.
     */
    void removeWindow(int uid) {
        blockSelection = true;
        treeRoot.getChildren().removeIf(
            item -> item.getValue().isWindowElement() && item.getValue().getUID() == uid
        );
        blockSelection = false;
    }

    /**
     * Returns the UID of the window containing the specified element, or zero if not found.
     */
    int getWindow(Element element) {
        var item = treeIndex.get(element);
        if (item == null) {
            return 0;
        }

        TreeItem<Element> window = findParentWindowItem(item);
        return window != null ? window.getValue().getUID() : 0;
    }

    /**
     * Selects the specified element in the tree and scrolls to it.
     */
    void selectElement(Element element) {
        if (treeIndex.containsKey(element)) {
            getSelectionModel().select(treeIndex.get(element));
            scrollTo(getSelectionModel().getSelectedIndex());
        }
    }

    /**
     * Returns the value of the selected tree item.
     */
    @Nullable
    Element getSelectedElement() {
        var item = getSelectionModel().getSelectedItem();
        return item != null ? item.getValue() : null;
    }

    /**
     * Adds the given element to the tree.
     */
    void addElement(Element element) {
        blockSelection = true;
        doAddElement(element);
        blockSelection = false;
    }

    /**
     * Removes the given element to the tree.
     */
    void removeElement(Element element) {
        blockSelection = true;
        doRemoveElement(element);
        blockSelection = false;
    }

    /**
     * Updates the specified element.
     */
    void updateTreeElement(Element element) {
        var item = treeIndex.get(element);
        if (item != null) {
            item.setValue(element);
        }
    }

    /**
     * Searches the tree items for the specified string.
     */
    List<TreeItem<Element>> search(@Nullable String filter) {
        // reset previous filter
        treeIndex.forEach((element, item) -> {
            if (item instanceof SceneGraphTreeItem sg) {
                sg.setFiltered(false);
            }
        });

        if (filter == null) {
            return List.of();
        }

        return treeIndex.entrySet().stream()
            .filter(e -> isMatchFilter(e.getKey(), filter))
            .map(e -> (SceneGraphTreeItem) e.getValue())
            .peek(item -> item.setFiltered(true))
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Navigates the tree to the specified element, expanding the branch if needed
     * and scrolling to the element's position.
     */
    void navigate(TreeItem<Element> item) {
        // getRow() returns _visible_ row index, so we have to expand branch first
        expandBranchUpward(item);

        int index = getRow(item);
        if (index > 0) {
            scrollTo(index);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Internal                                                              //
    ///////////////////////////////////////////////////////////////////////////

    private void initListeners() {
        getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (blockSelection) {
                return;
            }

            if (val != null) {
                selectConnectorElement(val);
            } else {
                clearConnectorSelection();
            }
        });

        setOnMousePressed(event -> {
            if (contextMenu != null) {
                contextMenu.hide();
                contextMenu = null;
            }
            if (event.isSecondaryButtonDown() && getSelectionModel().getSelectedItem() != null) {
                contextMenu = createContextMenu(getSelectionModel().getSelectedItem());
                contextMenu.show(SceneGraphTree.this, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private ContextMenu createContextMenu(TreeItem<Element> item) {
        boolean isLeaf = item.getChildren().isEmpty();
        Element element = item.getValue();

        var contextMenu = new ContextMenu();
        contextMenu.setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "sceneGraphTreeContextMenu");

        var clearSelectionMenu = new MenuItem("Clear selection");
        clearSelectionMenu.setOnAction(e -> clearConnectorSelection());

        var hidePopupsMenu = new MenuItem("Close popup windows");
        hidePopupsMenu.setOnAction(e -> closePopupWindows());

        var expandNodeMenu = new CheckMenuItem("Keep expanded");
        expandNodeMenu.setOnAction(e -> forcedNodes.expand(item));
        expandNodeMenu.setSelected(forcedNodes.isExpanded(item));
        expandNodeMenu.setDisable(isLeaf);

        var collapseNodeMenu = new CheckMenuItem("Keep collapsed");
        collapseNodeMenu.setOnAction(e -> forcedNodes.collapse(item));
        collapseNodeMenu.setSelected(forcedNodes.isCollapsed(item));
        collapseNodeMenu.setDisable(isLeaf);

        var expandTypeMenu = new CheckMenuItem("Always expand " + element.getSimpleClassName());
        expandTypeMenu.setOnAction(e -> forcedTypes.expand(item));
        expandTypeMenu.setSelected(forcedTypes.isExpanded(item));

        var collapseTypeMenu = new CheckMenuItem("Always collapse " + element.getSimpleClassName());
        collapseTypeMenu.setOnAction(e -> forcedTypes.collapse(item));
        collapseTypeMenu.setSelected(forcedTypes.isCollapsed(item));

        contextMenu.getItems().addAll(
            expandNodeMenu,
            collapseNodeMenu,
            expandTypeMenu,
            collapseTypeMenu,
            new SeparatorMenuItem(),
            clearSelectionMenu,
            hidePopupsMenu
        );

        return contextMenu;
    }

    /**
     * Notifies the {@link Connector} that the selected {@link Element} in the UI has been changed.
     */
    private void selectConnectorElement(TreeItem<Element> item) {
        TreeItem<Element> window = findParentWindowItem(item);
        if (window != null) {
            toolPane.getConnector().selectElement(window.getValue().getUID(), item.getValue());
        }
    }

    /**
     * See {@link #selectConnectorElement(TreeItem)}.
     */
    private void clearConnectorSelection() {
        var window = findParentWindowItem(getSelectionModel().getSelectedItem());
        if (window != null) {
            getSelectionModel().clearSelection();
            toolPane.getConnector().clearSelection(window.getValue().getUID());
        }
    }

    /**
     * Creates a tree branch starting from the given {@link Element} and traversing
     * deep down to its latest descendant. During this path, it sets the expanded flag
     * for every tree item along the way.
     */
    private TreeItem<Element> createTreeBranch(Element element) {
        var branch = new SceneGraphTreeItem(element);
        treeIndex.put(element, branch);

        // recursively create child tree items for the given node
        var children = new ArrayList<TreeItem<Element>>(element.getChildren().size());
        for (var child : element.getChildren()) {
            if (!child.isAuxiliaryElement()) {
                children.add(createTreeBranch(child));
            }
        }

        for (TreeItem<Element> child : children) {
            branch.getChildren().add(child);
        }

        // determine the tree item's expanded state, it's collapsed by default
        if (!forcedNodes.isCollapsed(branch) && !forcedTypes.isCollapsed(branch)) {
            boolean forceState = forcedNodes.isExpanded(branch) || forcedTypes.isExpanded(branch);
            branch.setExpanded(isElementPreferToBeExpanded(element) || forceState);
        }

        return branch;
    }

    /**
     * Finds and returns the closest window item for the given {@link TreeItem<Element>}.
     * Returns {@code null} if not found.
     */
    private @Nullable TreeItem<Element> findParentWindowItem(@Nullable TreeItem<Element> item) {
        if (item == null) {
            return null;
        }
        return item.getValue().isWindowElement() ? item : findParentWindowItem(item.getParent());
    }

    /**
     * See {@link #addElement(Element)}.
     */
    private void doAddElement(Element elementToAdd) {
        if (elementToAdd.isAuxiliaryElement()) {
            return;
        }

        // get the parent node and if not, it's impossible to determine new node position
        Element parentElement = elementToAdd.getParent();
        if (parentElement == null) {
            LOGGER.log(Level.WARNING, "Unable to determine element position as it has no parent");
            return;
        }
        TreeItem<Element> parentItem = treeIndex.get(parentElement);
        if (parentItem == null) {
            LOGGER.log(Level.WARNING, "Unable to determine tree item position as it has no parent");
            return;
        }

        // guard block, should never happen
        if (containsElement(parentItem, elementToAdd)) {
            LOGGER.log(Level.WARNING, "Unable to add element as it is already present");
            return;
        }

        TreeItem<Element> selectedItem = getSelectionModel().getSelectedItem();
        TreeItem<Element> itemToAdd = createTreeBranch(elementToAdd);
        List<Element> siblingElements = parentElement.getChildren();
        List<TreeItem<Element>> siblingItems = parentItem.getChildren();

        // where we want to be
        int targetPos = siblingElements.indexOf(elementToAdd);

        boolean posFound = false;
        int prevPos = -1;

        // try to insert the tree item in its real position
        for (int index = 0; index < siblingItems.size(); index++) {
            var currentElement = siblingItems.get(index).getValue();
            int currentPos = siblingElements.indexOf(currentElement);

            // guard block, should never happen
            if (prevPos > currentPos) {
                LOGGER.log(Level.WARNING, String.format(
                    "Unexpected condition encountered while adding tree node: parent %s=%d, child %s=%d",
                    parentElement.getSimpleClassName(), prevPos,
                    currentElement.getSimpleClassName(), currentPos
                ));
            }

            if (targetPos > prevPos && targetPos < currentPos) {
                parentItem.getChildren().add(index, itemToAdd);
                posFound = true;
                break;
            }

            prevPos = currentPos;
        }

        // if the target position is not found, add to the end
        if (!posFound) {
            parentItem.getChildren().add(itemToAdd);
        }

        // restore selection
        if (selectedItem != null) {
            getSelectionModel().select(selectedItem);
        }
    }

    /**
     * See {@link #removeElement(Element)}.
     */
    private void doRemoveElement(Element elementToRemove) {
        if (elementToRemove.isAuxiliaryElement()) {
            return;
        }

        var itemToRemove = treeIndex.get(elementToRemove);
        if (itemToRemove == null) {
            var className = elementToRemove.getSimpleClassName();
            LOGGER.log(Level.WARNING, "Trying to remove a non-existent tree item: " + className);
            return;
        }

        // preserve the selected item or clear the selection, if it's the very
        // item that needs to be removed
        TreeItem<Element> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getValue() == elementToRemove) {
            var window = findParentWindowItem(itemToRemove);
            if (window != null) {
                getSelectionModel().clearSelection();
                toolPane.getConnector().clearSelection(window.getValue().getUID());
                selectedItem = null;
            }
        }

        // do not use directly the list as it will suffer concurrent modifications
        List<TreeItem<Element>> childrenToRemove = itemToRemove.getChildren();
        for (var child : new ArrayList<>(childrenToRemove)) {
            // recursively remove the whole branch
            doRemoveElement(child.getValue());
        }

        // finally, remove the target tree item itself
        if (itemToRemove.getParent() != null) {
            itemToRemove.getParent().getChildren().remove(itemToRemove);
        }
        treeIndex.remove(itemToRemove.getValue());

        // restore selection
        if (selectedItem != null) {
            getSelectionModel().select(selectedItem);
        }
    }

    /**
     * Checks whether the given tree item contains the specified element in its children list.
     */
    private boolean containsElement(@Nullable TreeItem<Element> parentItem, Element element) {
        if (parentItem == null) {
            return false;
        }

        for (TreeItem<Element> node : parentItem.getChildren()) {
            if (Objects.equals(node.getValue(), element)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks the preferred expand/collapse state of the specified element.
     */
    private boolean isElementPreferToBeExpanded(Element element) {
        Preferences preferences = toolPane.getPreferences();
        boolean preferToBeExpanded = true;

        if (element.isNodeElement()) {
            var props = element.getNodeProperties();
            if (props != null && props.isControl() && preferences.isCollapseControls()) {
                preferToBeExpanded = false;
            }
            if (props != null && props.isPane() && preferences.getCollapsePanes()) {
                preferToBeExpanded = false;
            }
        }

        return preferToBeExpanded;
    }

    /**
     * Expands the entire branch, starting from the specified node and continuing up to the root.
     */
    private void expandBranchUpward(TreeItem<Element> item) {
        var parent = item.getParent();
        if (parent != null) {
            parent.setExpanded(true);
            expandBranchUpward(parent);
        }
    }

    /**
     * Checks whether the given element matches the filter.
     */
    private boolean isMatchFilter(Element element, String filter) {
        if (element.isWindowElement()) {
            return false;
        }

        var props = element.getNodeProperties();
        if (props == null) {
            return false;
        }

        return containsIgnoreCase(element.getSimpleClassName(), filter)
            || containsIgnoreCase(props.id(), filter)
            || props.styleClass().stream().anyMatch(styleClass -> containsIgnoreCase(styleClass, filter));
    }

    /**
     * Requests the connector to close (hide) all popup windows.
     */
    private void closePopupWindows() {
        List<Element> windows = treeRoot.getChildren().stream()
            .map(TreeItem::getValue)
            .filter(e -> e.getWindowProperties() != null && e.getWindowProperties().windowType() == WindowType.POPUP)
            .toList();

        // prevents ConcurrentModificationException
        windows.forEach(e -> toolPane.getConnector().hideWindow(e.getUID()));
    }

    private boolean containsIgnoreCase(@Nullable String str, @Nullable String subStr) {
        return str != null && (subStr == null || str.toLowerCase().contains(subStr.toLowerCase()));
    }
}
