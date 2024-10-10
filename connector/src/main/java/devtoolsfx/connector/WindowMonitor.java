package devtoolsfx.connector;

import devtoolsfx.event.*;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.WindowProperties;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import devtoolsfx.util.SceneUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import javafx.util.Subscription;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
final class WindowMonitor {

    private final Window window;
    private final ConnectorOptions connectorOpts;
    private final EventBus eventBus;
    private final EventSource eventSource;

    // highlighting
    private final BoundsPane boundsPane;
    private final InspectPane inspectPane;
    private @Nullable Node hoveredNode;
    private @Nullable Node selectedNode;
    private HighlightOptions highlightOpts = HighlightOptions.defaults();

    // attributes
    private final AttributeListener attributeListener;

    private boolean started;
    private final Map<Integer, Subscription> stylesClassSubs = new HashMap<>();

    /**
     * Creates a new WindowMonitor instance. Monitors are not reusable; each instance must
     * be connected to a different {@link Window}.
     *
     * @param window        the monitored window
     * @param connectorOpts options for {@link LocalConnector}
     * @param eventBus      the event bus instance to track monitor events
     * @param eventSource   the event source to be included in all emitted events
     */
    public WindowMonitor(Window window,
                         ConnectorOptions connectorOpts,
                         EventBus eventBus,
                         EventSource eventSource) {
        this.window = window;
        this.connectorOpts = connectorOpts;
        this.eventBus = eventBus;
        this.eventSource = eventSource;

        this.boundsPane = new BoundsPane();
        this.inspectPane = new InspectPane();

        this.attributeListener = new AttributeListener(eventBus, eventSource);

        connectorOpts.inspectModeProperty().addListener((obs, old, val) -> refreshRoot());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public API                                                            //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Starts the monitor.
     */
    public void start() {
        started = true;

        window.xProperty().addListener(windowPropertyReportListener);
        window.yProperty().addListener(windowPropertyReportListener);
        window.widthProperty().addListener(windowPropertyReportListener);
        window.heightProperty().addListener(windowPropertyReportListener);
        window.focusedProperty().addListener(windowPropertyReportListener);
        window.sceneProperty().addListener(sceneChangeListener);

        changeScene(null, window.getScene());
        fire(WindowPropertiesEvent.of(eventSource, window));
    }

    /**
     * Stops the monitor.
     */
    public void stop() {
        started = false;

        window.xProperty().removeListener(windowPropertyReportListener);
        window.yProperty().removeListener(windowPropertyReportListener);
        window.widthProperty().removeListener(windowPropertyReportListener);
        window.heightProperty().removeListener(windowPropertyReportListener);
        window.focusedProperty().removeListener(windowPropertyReportListener);
        window.sceneProperty().removeListener(sceneChangeListener);

        changeScene(getScene(), null);

        // cleanup resources
        clearSelection();
        inspectPane.hide();
    }

    /**
     * Returns the {@link EventBus} to react to the monitor events.
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Returns the event source that is included in all emitted events by the monitor.
     */
    public EventSource getEventSource() {
        return eventSource;
    }

    /**
     * See {@link Connector#selectWindow(int)}.
     */
    public void selectWindow() {
        if (selectedNode != null) {
            clearSelection();
        }
        attributeListener.setTarget(window);
    }

    /**
     * See {@link Connector#selectNode(int, Element, HighlightOptions)}.
     */
    public void selectNode(@Nullable Node node, @Nullable HighlightOptions opts) {
        Node prevNode = selectedNode;
        if (prevNode != null) {
            prevNode.boundsInParentProperty().removeListener(selectedNodeBoundsListener);
            prevNode.layoutBoundsProperty().removeListener(selectedNodeBoundsListener);
        }

        if (node == null) {
            clearSelection();
            return;
        }

        highlightOpts = Objects.requireNonNullElse(opts, HighlightOptions.defaults());
        selectedNode = node;

        selectedNode.boundsInParentProperty().addListener(selectedNodeBoundsListener);
        selectedNode.layoutBoundsProperty().addListener(selectedNodeBoundsListener);

        boundsPane.toggleLayoutBoundsDisplay(highlightOpts.showLayoutBounds() ? selectedNode : null);
        boundsPane.toggleBoundsInParentDisplay(highlightOpts.showBoundsInParent() ? selectedNode : null);
        boundsPane.toggleBaselineDisplay(highlightOpts.showBaseline() ? selectedNode : null);

        attributeListener.setTarget(selectedNode);
    }

    /**
     * The opposite of {@link #selectNode(Node, HighlightOptions)}.
     */
    public void clearSelection() {
        selectedNode = null;
        attributeListener.setTarget(null);
        highlightOpts = HighlightOptions.defaults();
        boundsPane.detach();
    }

    /**
     * Returns a scene graph node with the given hash code.
     */
    public @Nullable Node findNode(int hashCode) {
        if (getRoot() == null) {
            return null;
        }

        return SceneUtils.findNode(getRoot(), hashCode);
    }

    /**
     * Sets the inspect mode on the monitored object to on or off.
     */
    public void setInspectMode(boolean enabled) {
        if (!enabled) {
            inspectPane.hide();
        }
    }

    /**
     * Hides the monitored window.
     */
    public void hideWindow() {
        window.hide();
    }

    /**
     * See {@link Connector#getStyledElements(int)}.
     */
    public Map.@Nullable Entry<WindowProperties, List<Element>> getStyledElements() {
        List<Element> result = new ArrayList<>();
        if (getRoot() != null) {
            SceneUtils.collectNodesWithStyleSheets(getRoot(), result);
        }

        WindowProperties props = null;
        if (getScene() != null) {
            props = WindowProperties.of(window, eventSource.isPrimaryStage());
        }

        return props != null ? new AbstractMap.SimpleEntry<>(props, result) : null;
    }

    /**
     * Ensures that at least one scene graph node references the given stylesheet URI.
     */
    public boolean containsStylesheet(String uri) {
        if (getScene() != null && (
            Objects.equals(getScene().getUserAgentStylesheet(), uri) || getScene().getStylesheets().contains(uri))
        ) {
            return true;
        }

        return getRoot() != null && SceneUtils.containsStylesheet(getRoot(), uri);
    }

    /**
     * See {@link Connector#reloadSelectedAttributes(int, AttributeCategory, String)}.
     */
    public void reloadSelectedAttributes(@Nullable AttributeCategory category, @Nullable String property) {
        if (category != null && property != null) {
            attributeListener.reloadAttribute(category, property);
            return;
        }

        if (category != null) {
            attributeListener.reloadCategory(category);
            return;
        }

        attributeListener.reload();
    }

    /**
     * Retrieves the scene of the monitored window.
     */
    public @Nullable Scene getScene() {
        return window.getScene();
    }

    /**
     * Retrieves the scene root of the monitored window.
     */
    public @Nullable Parent getRoot() {
        return getScene() != null ? getScene().getRoot() : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listeners                                                             //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Called when the scene's root {@link Parent} node changes.
     */
    private final ChangeListener<Parent> sceneRootChangeListener = (obs, old, val) -> changeRoot(old, val);

    /**
     * Called when the window's Scene value changes.
     */
    private final ChangeListener<Scene> sceneChangeListener = (obs, old, val) -> changeScene(old, val);

    /**
     * Called when any of the window's properties change.
     */
    private final InvalidationListener windowPropertyReportListener = obs -> onWindowPropertyChanged();

    private void onWindowPropertyChanged() {
        fire(WindowPropertiesEvent.of(eventSource, window));
    }

    /**
     * Handles mouse move events to highlight a hovered node.
     */
    private final EventHandler<? super MouseEvent> mouseMoveHighlightFilter = this::onMouseHover;

    private void onMouseHover(MouseEvent event) {
        highlightHoveredNode(event);
    }

    /**
     * Handles mouse move events to select a clicked node.
     */
    private final EventHandler<? super MouseEvent> mousePressSelectFilter = this::onMousePressed;

    private void onMousePressed(MouseEvent event) {
        Node node = getHoveredNode(event);
        if (node != null && connectorOpts.isInspectMode()) {
            fire(new NodeSelectedEvent(eventSource, LocalElement.of(node)));
        }
    }

    /**
     * Reports mouse coordinates via the event bus.
     */
    private final EventHandler<? super MouseEvent> mousePosReportFilter = this::onMouseMoved;

    private void onMouseMoved(MouseEvent event) {
        fire(MousePosEvent.of(eventSource, event));
    }

    /**
     * Listens to the node's children list and handles changes accordingly.
     */
    private final ListChangeListener<Node> nodeChildrenListener = this::onNodeChildrenChanged;

    private void onNodeChildrenChanged(ListChangeListener.Change<? extends Node> change) {
        while (change.next()) {
            for (var dead : change.getRemoved()) {
                removeNodeBranchListenersAndNotify(dead);
            }
            for (var alive : change.getAddedSubList()) {
                addNodeBranchListenersAndNotify(alive);
            }
        }
    }

    /**
     * Tracks the node's visibility state.
     */
    private final ChangeListener<Boolean> nodeVisibilityChangeListener = this::onNodeVisibilityChanged;

    @SuppressWarnings("unchecked")
    private void onNodeVisibilityChanged(Observable obs, Boolean wasVisible, Boolean nowVisible) {
        var node = (Node) ((Property<Boolean>) obs).getBean();
        fire(new NodeVisibilityEvent(eventSource, LocalElement.of(node), nowVisible));
    }

    /**
     * Reports all {@link Event#ANY} events for the node via the event bus.
     */
    private final EventHandler<? super Event> nodeEventLogFilter = this::onNodeAnyEvent;

    private void onNodeAnyEvent(Event event) {
        fire(new JavaFXEvent(
            eventSource, LocalElement.of((Node) event.getSource()), event.getEventType(), String.valueOf(event)
        ));
    }

    /**
     * Updates the selected node highlighting when its bounds change due to resizing.
     */
    private final InvalidationListener selectedNodeBoundsListener = new InvalidationListener() {
        private boolean recursive; // prevent stack overflow

        @Override
        public void invalidated(Observable obs) {
            if (!recursive) {
                recursive = true;
                boundsPane.toggleLayoutBoundsDisplay(highlightOpts.showLayoutBounds() ? selectedNode : null);
                boundsPane.toggleBoundsInParentDisplay(highlightOpts.showBoundsInParent() ? selectedNode : null);
                recursive = false;
            }
        }
    };

    /**
     * Changes the monitored {@link Scene}.
     */
    private void changeScene(@Nullable Scene oldScene, @Nullable Scene newScene) {
        // unsubscribe
        Parent oldRoot = null;
        if (oldScene != null) {
            SceneUtils.removeListener(oldScene, Scene::rootProperty, sceneRootChangeListener);
            SceneUtils.removeEventFilter(oldScene, MouseEvent.MOUSE_MOVED, mouseMoveHighlightFilter);
            oldRoot = oldScene.getRoot();
        }

        // subscribe
        Parent newRoot = null;
        if (newScene != null) {
            SceneUtils.addListener(newScene, Scene::rootProperty, sceneRootChangeListener);
            SceneUtils.addEventFilter(newScene, MouseEvent.MOUSE_MOVED, mouseMoveHighlightFilter);
            newRoot = newScene.getRoot();
        }

        changeRoot(oldRoot, newRoot);
    }

    /**
     * Changes the monitored {@link Parent} root node.
     */
    private void changeRoot(@Nullable Parent oldRoot, @Nullable Parent newRoot, boolean force) {
        // For alerts when we close the dialog, JavaFX caches the stage for reuse and changes
        // the dialog pane to an empty root node. Thus, despite being stopped by the connector,
        // the stage can continue to generate events, which can break the UI. That's why we first
        // have to check that the monitor is still active.
        if (!started) {
            return;
        }

        if (!force && oldRoot == newRoot) {
            return;
        }

        if (oldRoot != null) {
            removeNodeBranchListeners(oldRoot);
            SceneUtils.removeEventFilter(oldRoot, MouseEvent.MOUSE_MOVED, mouseMoveHighlightFilter);
            SceneUtils.removeEventFilter(oldRoot, MouseEvent.MOUSE_MOVED, mousePosReportFilter);
            SceneUtils.removeEventFilter(oldRoot, MouseEvent.MOUSE_PRESSED, mousePressSelectFilter);
            SceneUtils.removeFromNode(oldRoot, inspectPane);
        }

        if (newRoot != null) {
            addNodeBranchListeners(newRoot);
            SceneUtils.addEventFilter(newRoot, MouseEvent.MOUSE_MOVED, mouseMoveHighlightFilter);
            SceneUtils.addEventFilter(newRoot, MouseEvent.MOUSE_MOVED, mousePosReportFilter);
            SceneUtils.addEventFilter(newRoot, MouseEvent.MOUSE_PRESSED, mousePressSelectFilter);
            SceneUtils.addToNode(newRoot, inspectPane);
        }

        boundsPane.attach(newRoot);
        notifyRootChanged(newRoot);
    }

    /**
     * See {@link #changeRoot(Parent, Parent, boolean)}.
     */
    private void changeRoot(@Nullable Parent oldRoot, @Nullable Parent newRoot) {
        changeRoot(oldRoot, newRoot, false);
    }

    /**
     * See {@link #changeRoot(Parent, Parent, boolean)}.
     */
    private void refreshRoot() {
        changeRoot(getRoot(), getRoot(), true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Operations with tracked scene graph node                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Notifies the {@link LocalConnector} client that this scene's root node
     * has been changed, so they can update the UI accordingly.
     */
    public void notifyRootChanged(@Nullable Parent root) {
        var windowElement = LocalElement.of(window, eventSource, root != null ? LocalElement.of(root) : null);
        fire(new RootChangedEvent(eventSource, windowElement));
    }

    /**
     * Adds a set of listeners to the entire branch starting from the
     * specified node to respond to state changes.
     */
    private void addNodeBranchListeners(Node node) {
        if (SceneUtils.isAuxiliaryNode(node)) {
            return;
        }

        node.visibleProperty().removeListener(nodeVisibilityChangeListener);
        node.visibleProperty().addListener(nodeVisibilityChangeListener);

        node.removeEventFilter(Event.ANY, nodeEventLogFilter);
        node.addEventFilter(Event.ANY, nodeEventLogFilter);

        stylesClassSubs.put(node.hashCode(), node.getStyleClass().subscribe(() -> fire(new NodeStyleClassEvent(
            eventSource, LocalElement.of(node), Collections.unmodifiableList(node.getStyleClass())
        ))));

        ObservableList<Node> children = SceneUtils.getChildren(node);
        children.removeListener(nodeChildrenListener);
        children.addListener(nodeChildrenListener);

        for (var child : children) {
            addNodeBranchListeners(child);
        }
    }

    /**
     * The opposite of {@link #addNodeBranchListeners(Node)}.
     * <p>
     * When we are removing a node:
     * - if it's a real node removal removeVisibilityListener is true
     * - if it's a visibility remove we should remove the visibility listeners
     * of its children because the visibility is reduced by their parent
     */
    private void removeNodeBranchListeners(Node node) {
        ObservableList<Node> children = SceneUtils.getChildren(node);
        for (var child : children) {
            removeNodeBranchListeners(child);
        }
        children.removeListener(nodeChildrenListener);

        node.visibleProperty().removeListener(nodeVisibilityChangeListener);

        node.removeEventFilter(Event.ANY, nodeEventLogFilter);

        var subscription = stylesClassSubs.get(node.hashCode());
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    /**
     * Adds a set of listeners to respond to node state changes and emits a {@code node added} event.
     */
    private void addNodeBranchListenersAndNotify(Node node) {
        if (!SceneUtils.isAuxiliaryNode(node)) {
            addNodeBranchListeners(node);
            fire(NodeAddedEvent.of(eventSource, LocalElement.of(node)));
        }
    }

    /**
     * Removes the set of listeners that was added to respond to node state changes and emits
     * a {@code node removed} event.
     */
    private void removeNodeBranchListenersAndNotify(Node node) {
        if (!SceneUtils.isAuxiliaryNode(node)) {
            removeNodeBranchListeners(node);
            fire(new NodeRemovedEvent(eventSource, LocalElement.of(node)));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Highlighting                                                          //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the hovered node inside the scene based on the {@link MouseEvent} coordinates.
     */
    private @Nullable Node getHoveredNode(MouseEvent event) {
        if (getRoot() == null) {
            return null;
        }
        return SceneUtils.findHoveredNode(
            getRoot(), event.getX(), event.getY(), connectorOpts.isIgnoreMouseTransparent()
        );
    }

    /**
     * Highlights a hovered node based on the {@link MouseEvent} coordinates.
     */
    private void highlightHoveredNode(MouseEvent event) {
        if (!connectorOpts.isInspectMode()) {
            return;
        }

        Node node = getHoveredNode(event);
        if (node != null && hoveredNode == node) {
            return;
        }

        hoveredNode = node;
        if (hoveredNode != null) {
            if (SceneUtils.isAuxiliaryNode(hoveredNode)) {
                return;
            }

            if (SceneUtils.getWindow(hoveredNode) instanceof Tooltip tooltip
                && SceneUtils.isAuxiliaryNode(tooltip)) {
                return;
            }

            var nodeBounds = boundsPane.calcRelativeBounds(hoveredNode, false);
            if (nodeBounds != null) {
                inspectPane.show(hoveredNode, nodeBounds, window.getWidth(), window.getHeight());
            }
            return;
        }

        inspectPane.hide();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility Methods                                                       //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Fires the given event via the event bus when the monitor is started.
     */
    private <T extends ConnectorEvent> void fire(T event) {
        if (started) {
            eventBus.fire(event);
        }
    }
}
