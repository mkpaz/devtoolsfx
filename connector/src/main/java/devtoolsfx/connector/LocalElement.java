package devtoolsfx.connector;

import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.*;
import devtoolsfx.util.ClassInfoCache;
import devtoolsfx.util.SceneUtils;
import javafx.scene.Node;
import javafx.stage.Window;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The {@link Element} implementation that directly wraps a link to the scene graph
 * node and must not leak the JVM process (e.g. by transferring it via the network).
 */
@NullMarked
public final class LocalElement implements Element {

    private final int uid;
    private final ClassInfo classInfo;
    private final Vertex vertex;
    private final @Nullable NodeProperties nodeProperties;
    private final @Nullable WindowProperties windowProperties;
    private final @Nullable Node node;

    private LocalElement(int uid,
                         ClassInfo classInfo,
                         Vertex vertex,
                         @Nullable NodeProperties nodeProperties,
                         @Nullable WindowProperties windowProperties,
                         @Nullable Node node) {
        this.uid = uid;
        this.classInfo = Objects.requireNonNull(classInfo, "class info must not be null");
        this.vertex = Objects.requireNonNull(vertex, "vertex must not be null");

        if (nodeProperties != null && windowProperties != null) {
            throw new IllegalArgumentException(
                "Either nodeProperties or windowProperties must be null, as it signifies the type of the element"
            );
        }

        if (nodeProperties == null && windowProperties == null) {
            throw new IllegalArgumentException(
                "Either nodeProperties or windowProperties must be specified, as it signifies the type of the element"
            );
        }

        this.nodeProperties = nodeProperties;
        this.windowProperties = windowProperties;
        this.node = node;
    }

    @Override
    public int getUID() {
        return uid;
    }

    @Override
    public ClassInfo getClassInfo() {
        return classInfo;
    }

    @Override
    public @Nullable Element getParent() {
        return vertex.getParent();
    }

    @Override
    public List<Element> getChildren() {
        return vertex.getChildren();
    }

    @Override
    public boolean hasChildren() {
        return vertex.hasChildren();
    }

    @Override
    public @Nullable NodeProperties getNodeProperties() {
        return nodeProperties;
    }

    @Override
    public @Nullable WindowProperties getWindowProperties() {
        return windowProperties;
    }

    @Override
    public boolean isWindowElement() {
        return windowProperties != null;
    }

    @Override
    public boolean isNodeElement() {
        return nodeProperties != null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LocalElement that)) {
            return false;
        }

        return uid == that.uid;
    }

    @Override
    public int hashCode() {
        return uid;
    }

    @Override
    public String toString() {
        return "LocalElement{" +
            "uid=" + uid +
            ", classInfo=" + classInfo +
            ", vertex=" + vertex +
            ", nodeProperties=" + nodeProperties +
            ", windowProperties=" + windowProperties +
            ", node=" + node +
            '}';
    }

    /**
     * If the element is a wrapper around {@link Node}, unwraps the target node.
     */
    public @Nullable Node unwrap() {
        return node;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new Element from the target JavaFX node.
     */
    public static Element of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");

        return new LocalElement(
            node.hashCode(),
            ClassInfoCache.get(node),
            new NodeVertex(node),
            NodeProperties.of(node),
            null,
            node
        );
    }

    /**
     * Creates a new Element for the given window.
     */
    public static Element of(Window window, EventSource eventSource, @Nullable Element root) {
        Objects.requireNonNull(window, "window cannot be null");

        return new LocalElement(
            eventSource.uid(),
            ClassInfoCache.get(window),
            new WindowVertex(root),
            null,
            WindowProperties.of(window, eventSource.isPrimaryStage()),
            null
        );
    }

    /**
     * See {@link LocalElement#of(Window, EventSource, Element)}.
     */
    public static Element of(Window window, EventSource eventSource) {
        Objects.requireNonNull(window, "window cannot be null");

        Element root = null;
        if (window.getScene() != null && window.getScene().getRoot() != null) {
            root = LocalElement.of(window.getScene().getRoot());
        }

        return LocalElement.of(window, eventSource, root);
    }

    ///////////////////////////////////////////////////////////////////////////

    @NullMarked
    static final class NodeVertex implements Vertex {

        private final Node node;

        public NodeVertex(Node node) {
            this.node = node;
        }

        @Override
        public @Nullable Element getParent() {
            if (node.getParent() != null) {
                return of(node.getParent());
            }
            return null;
        }

        @Override
        public List<Element> getChildren() {
            return SceneUtils.getChildren(node)
                .stream()
                .map(LocalElement::of)
                .collect(Collectors.toList());
        }

        @Override
        public boolean hasChildren() {
            return SceneUtils.getChildren(node).isEmpty();
        }
    }

    @NullMarked
    static final class WindowVertex implements Vertex {

        private final @Nullable Element root;

        public WindowVertex(@Nullable Element root) {
            this.root = root;
        }

        @Override
        public @Nullable Element getParent() {
            return null;
        }

        @Override
        public List<Element> getChildren() {
            return root != null ? List.of(root) : List.of();
        }

        @Override
        public boolean hasChildren() {
            return root != null;
        }
    }
}
