package devtoolsfx.scenegraph;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Represents a vertex in the scene graph tree.
 */
@NullMarked
public interface Vertex {

    /**
     * Returns the parent element, if any.
     */
    @Nullable
    Element getParent();

    /**
     * Returns the list of child elements.
     */
    List<Element> getChildren();

    /**
     * Returns whether the vertex has any child elements.
     */
    boolean hasChildren();
}
