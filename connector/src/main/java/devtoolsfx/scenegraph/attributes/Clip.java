package devtoolsfx.scenegraph.attributes;

import javafx.geometry.Bounds;
import javafx.scene.Node;

/**
 * A wrapper to transfer {@link Node#clipProperty()} info.
 */
public record Clip(String className, Bounds bounds) {
}
