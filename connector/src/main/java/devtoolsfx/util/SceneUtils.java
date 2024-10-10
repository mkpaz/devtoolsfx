package devtoolsfx.util;

import devtoolsfx.connector.ConnectorOptions;
import devtoolsfx.connector.LocalElement;
import devtoolsfx.scenegraph.Element;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.control.Control;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A set of utility methods to work with scene's nodes.
 */
@NullMarked
public final class SceneUtils {

    private static final System.Logger LOGGER = System.getLogger(SceneUtils.class.getName());

    /**
     * Returns the given node unique ID. Basically, it's just a hash code.
     */
    public static int getUID(Node node) {
        return node.hashCode();
    }

    /**
     * Checks whether the given node is a normal application node or an auxiliary node.
     */
    public static boolean isAuxiliaryNode(@Nullable Node node) {
        return node != null
            && node.getId() != null
            && node.getId().startsWith(ConnectorOptions.AUX_NODE_ID_PREFIX);
    }

    /**
     * See {@link #isAuxiliaryNode(Node)}.
     */
    public static boolean isAuxiliaryNode(@Nullable PopupControl popup) {
        return popup != null
            && popup.getId() != null
            && popup.getId().startsWith(ConnectorOptions.AUX_NODE_ID_PREFIX);
    }

    /**
     * Returns the unmodifiable list of children of the given node or an empty list.
     * This method handles the use case when the provided node is a {@link SubScene}.
     */
    public static ObservableList<Node> getChildren(@Nullable Node node) {
        return switch (node) {
            case Parent parent -> parent.getChildrenUnmodifiable();
            case SubScene subScene -> subScene.getRoot().getChildrenUnmodifiable();
            case null, default -> FXCollections.emptyObservableList();
        };
    }

    /**
     * Returns the count of children for the specified node.
     */
    public static int countChildren(Node node) {
        return (int) getChildren(node).stream().filter(c -> !isAuxiliaryNode(c)).count();
    }

    /**
     * Returns the count of nodes in the branch, starting from and including the given node.
     */
    public static int countNodesInBranch(Node branch) {
        if (isAuxiliaryNode(branch)) {
            return 0;
        }

        int count = 1;
        for (var child : getChildren(branch)) {
            count += countNodesInBranch(child);
        }

        return count;
    }

    /**
     * Searches for a scene graph node with the given hash code, starting from the specified node.
     */
    public static @Nullable Node findNode(Node node, int hashCode) {
        if (node.hashCode() == hashCode) {
            return node;
        }

        for (var child : SceneUtils.getChildren(node)) {
            return findNode(child, hashCode);
        }

        return null;
    }

    /**
     * Returns the nearest instance of the {@link Pane} class starting from the
     * given parent no and down to its descendants.
     */
    public static @Nullable Parent findNearestPane(@Nullable Parent parent) {
        if (parent == null) {
            return null;
        }

        Parent fertile = (parent instanceof Group || parent instanceof Pane) ? parent : null;
        if (fertile == null) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (child instanceof Parent) {
                    fertile = findNearestPane((Parent) child);
                }
            }
        }

        return fertile;
    }

    /**
     * Finds the hovered node in the target's children using the given coordinates.
     */
    public static @Nullable Node findHoveredNode(@Nullable Node target,
                                                 double x,
                                                 double y,
                                                 boolean ignoreMouseTransparent) {

        if (target == null || SceneUtils.isAuxiliaryNode(target)) {
            return null;
        }

        List<Node> children = getChildren(target);
        for (int i = children.size() - 1; i >= 0; i--) {
            Node maybeHovered = findHoveredNode(children.get(i), x, y, ignoreMouseTransparent);
            if (maybeHovered != null) {
                return maybeHovered;
            }
        }

        Point2D localPoint = target.sceneToLocal(x, y);
        boolean isNotMouseTransparent = ignoreMouseTransparent || !target.isMouseTransparent();

        if (target.contains(localPoint) && isNotMouseTransparent && isBranchVisible(target)) {
            return target;
        }

        return null;
    }

    /**
     * Adds the given node to the given parent. If the parent is not of a container type,
     * then adds it to the nearest pane in the parent's descendants.
     */
    public static void addToNode(Parent parent, Node node) {
        if (parent instanceof Group group) {
            group.getChildren().add(node);
        } else if (parent instanceof Pane pane) {
            pane.getChildren().add(node);
        } else {
            var pane = findNearestPane(parent);
            if (pane != null) {
                addToNode(pane, node);
            }
        }
    }

    /**
     * The opposite of the {@link #addToNode(Parent, Node)}.
     */
    public static void removeFromNode(Parent parent, Node node) {
        if (parent instanceof Group group) {
            group.getChildren().remove(node);
        } else if (parent instanceof Pane pane) {
            pane.getChildren().remove(node);
        } else {
            var pane = findNearestPane(parent);
            if (pane != null) {
                removeFromNode(pane, node);
            }
        }
    }

    /**
     * Returns the list of stylesheets for the specified node.
     */
    public static List<String> getStylesheets(Node node) {
        return switch (node) {
            case Control control -> Collections.unmodifiableList(control.getStylesheets());
            case Parent parent -> Collections.unmodifiableList(parent.getStylesheets());
            default -> List.of();
        };
    }

    /**
     * Returns the user agent stylesheet for the specified node.
     */
    public static @Nullable String getUserAgentStylesheet(Node node) {
        String uas = switch (node) {
            case Control control -> control.getUserAgentStylesheet();
            case Region region -> region.getUserAgentStylesheet();
            case SubScene subScene -> subScene.getUserAgentStylesheet();
            default -> null;
        };

        return uas != null && uas.isEmpty() ? null : uas;
    }

    /**
     * Checks that both the given node and all it ancestors are visible.
     */
    public static boolean isBranchVisible(@Nullable Node node) {
        // node is visible if it's null, because it means that all descendant are visible
        // up to the common ancestor, which doesn't have a parent, so the recursion ended with null
        return node == null || (node.isVisible() && isBranchVisible(node.getParent()));
    }

    /**
     * Returns the window to which the given node belongs, if any.
     */
    public static @Nullable Window getWindow(Node node) {
        if (node.getScene() != null && node.getScene().getWindow() != null) {
            return node.getScene().getWindow();
        }
        return null;
    }

    /**
     * Recursively collects the list of nodes for which {@link Parent#getStylesheets()}
     * or {@link Control#getStylesheets()} is not empty.
     */
    public static void collectNodesWithStyleSheets(Node node, List<Element> accumulator) {
        var stylesheets = getStylesheets(node);
        if (!stylesheets.isEmpty()) {
            accumulator.add(LocalElement.of(node));
        }

        getChildren(node).forEach(child -> collectNodesWithStyleSheets(child, accumulator));
    }

    /**
     * Determines if the specified node or any of its descendants contains
     * the given stylesheet URI.
     */
    public static boolean containsStylesheet(Node node, String uri) {
        if (Objects.equals(getUserAgentStylesheet(node), uri)) {
            return true;
        }

        if (getStylesheets(node).contains(uri)) {
            return true;
        }

        for (Node child : getChildren(node)) {
            return containsStylesheet(child, uri);
        }

        return false;
    }

    /**
     * Null safe wrapper for calling the {@code addListener()} on a generic node.
     */
    public static <N> void addListener(@Nullable N node,
                                       Function<N, ObservableValue<?>> obs,
                                       InvalidationListener listener) {
        if (node != null) {
            obs.apply(node).addListener(listener);
        } else {
            LOGGER.log(Level.INFO, "node is null, this behavior is probably not expected");
        }
    }

    /**
     * Null safe wrapper for calling the {@code removeListener()} on a generic node.
     */
    public static <N> void removeListener(@Nullable N node,
                                          Function<N, ObservableValue<?>> obs,
                                          InvalidationListener listener) {
        if (node != null) {
            obs.apply(node).removeListener(listener);
        } else {
            LOGGER.log(Level.INFO, "node is null, this behavior is probably not expected");
        }
    }

    /**
     * Null safe wrapper for calling the {@code addListener()} on a generic node.
     */
    public static <N, V> void addListener(@Nullable N node,
                                          Function<N, ObservableValue<V>> obs,
                                          ChangeListener<V> listener) {
        if (node != null) {
            obs.apply(node).addListener(listener);
        } else {
            LOGGER.log(Level.INFO, "node is null, this behavior is probably not expected");
        }
    }

    /**
     * Null safe wrapper for calling the {@code removeListener()} on a generic node.
     */
    public static <N, V> void removeListener(@Nullable N node,
                                             Function<N, ObservableValue<V>> obs,
                                             ChangeListener<V> listener) {
        if (node != null) {
            obs.apply(node).addListener(listener);
        } else {
            LOGGER.log(Level.INFO, "node is null, this behavior is probably not expected");
        }
    }

    /**
     * Null safe wrapper for calling the {@code addEventFilter()} on a generic scene.
     */
    public static <E extends Event> void addEventFilter(@Nullable Scene scene,
                                                        EventType<E> eventType,
                                                        EventHandler<? super E> eventFilter) {
        if (scene != null) {
            scene.addEventFilter(eventType, eventFilter);
        } else {
            LOGGER.log(Level.INFO, "scene is null, this behavior is probably not expected");
        }
    }

    /**
     * Null safe wrapper for calling the {@code removeEventFilter()} on a generic scene.
     */
    public static <E extends Event> void removeEventFilter(@Nullable Scene scene,
                                                           EventType<E> eventType,
                                                           EventHandler<? super E> eventFilter) {
        if (scene != null) {
            scene.removeEventFilter(eventType, eventFilter);
        } else {
            LOGGER.log(Level.INFO, "scene is null, this behavior is probably not expected");
        }
    }

    /**
     * Null safe wrapper for calling the {@code addEventFilter()} on a generic parent.
     */
    public static <E extends Event> void addEventFilter(@Nullable Parent parent,
                                                        EventType<E> eventType,
                                                        EventHandler<? super E> eventFilter) {
        if (parent != null) {
            parent.addEventFilter(eventType, eventFilter);
        } else {
            LOGGER.log(Level.INFO, "parent is null, this behavior is probably not expected");
        }
    }

    /**
     * Null safe wrapper for calling the {@code removeEventFilter()} on a generic parent.
     */
    public static <E extends Event> void removeEventFilter(@Nullable Parent parent,
                                                           EventType<E> eventType,
                                                           EventHandler<? super E> eventFilter) {
        if (parent != null) {
            parent.removeEventFilter(eventType, eventFilter);
        } else {
            LOGGER.log(Level.INFO, "parent is null, this behavior is probably not expected");
        }
    }
}
