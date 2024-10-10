package devtoolsfx.scenegraph;

import devtoolsfx.connector.ConnectorOptions;
import devtoolsfx.util.SceneUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Represents a selective set of node properties.
 *
 * @param id          the {@link Node#getId()} of the node
 * @param styleClass  the {@link Node#getStyleClass()} of the node
 * @param stylesheets the list of stylesheets for the node
 * @param isControl   whether the node is a JavaFX {@link Control}
 * @param isPane      whether the node is a JavaFX {@link Pane} or {@link Group}
 * @param isVisible   whether the node is visible
 */
@NullMarked
public record NodeProperties(@Nullable String id,
                             List<String> styleClass,
                             List<String> stylesheets,
                             @Nullable String userAgentStylesheet,
                             boolean isControl,
                             boolean isPane,
                             boolean isVisible) {

    /**
     * See {@link Element#isAuxiliaryElement()}.
     */
    public boolean isAuxiliaryElement() {
        // all auxiliary nodes must have an ID that starts with a common prefix,
        // for normal javaFX nodes ID isn't mandatory (and rarely used)
        return id != null && id.startsWith(ConnectorOptions.AUX_NODE_ID_PREFIX);
    }

    public static NodeProperties of(Node node) {
        return new NodeProperties(
            node.getId(),
            Collections.unmodifiableList(node.getStyleClass()),
            SceneUtils.getStylesheets(node),
            SceneUtils.getUserAgentStylesheet(node),
            node instanceof Control,
            node instanceof Pane || node instanceof Group,
            node.isVisible()
        );
    }
}
