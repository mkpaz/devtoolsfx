package devtoolsfx.scenegraph;

import java.util.List;

import javafx.scene.Node;
import javafx.stage.Window;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An element represents an arbitrary item in the scene graph hierarchy
 * and essentially serves as a local or remote wrapper around {@link Node}
 * or {@link Window}.
 * <p>
 * While it is not directly enforced, an implementation must override
 * {@code hashCode()} and {@code equals()} to function properly.
 */
@NullMarked
public interface Element {

    /**
     * Returns the unique element ID (generally, its hash code).
     */
    int getUID();

    /**
     * Returns full information about the type of the wrapped scene graph node.
     */
    ClassInfo getClassInfo();

    /**
     * Returns the parent element, or null if this element is the root.
     */
    @Nullable
    Element getParent();

    /**
     * Returns a list of the children of this element.
     */
    List<Element> getChildren();

    /**
     * Checks whether this element has any children.
     */
    boolean hasChildren();

    /**
     * Returns the properties of the wrapped node. If the element does not wrap
     * a {@link Node} but a {@link Window}, it returns null.
     */
    @Nullable
    NodeProperties getNodeProperties();

    /**
     * Returns the properties of the wrapped window. If the element does not wrap
     * a {@link Window} but a {@link Node}, it returns null.
     */
    @Nullable
    WindowProperties getWindowProperties();

    /**
     * Checks whether the element is a wrapper around {@link Node}.
     */
    boolean isNodeElement();

    /**
     * Checks whether the element is a wrapper around {@link Window}.
     */
    boolean isWindowElement();

    /**
     * Returns whether the element wraps an auxiliary node.
     */
    default boolean isAuxiliaryElement() {
        // all auxiliary nodes must have an ID that starts with a common prefix,
        // for normal javaFX nodes ID isn't mandatory (and rarely used)
        var props = getNodeProperties();
        return props != null && props.isAuxiliaryElement();
    }

    /**
     * Returns the simple class name of the wrapped scene graph node.
     */
    default String getSimpleClassName() {
        return getClassInfo().simpleClassName();
    }
}
