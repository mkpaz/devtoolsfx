package devtoolsfx.connector;

import devtoolsfx.util.ClassInfoCache;

import java.text.DecimalFormat;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.jspecify.annotations.NullMarked;

import static javafx.stage.PopupWindow.AnchorLocation;

/**
 * The pane that is meant to be displayed above the hovered node.
 * It visually highlights the node as well as displays the tooltip with short node information.
 */
@NullMarked
final class InspectPane extends Group {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

    private final Rectangle rootBounds = new Rectangle();
    private final Rectangle viewportBounds = new Rectangle();
    private final Tooltip tooltip = new Tooltip();

    public InspectPane() {
        super();

        setManaged(false);
        setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "inspectPane");

        tooltip.setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "inspectPaneTooltip");
        tooltip.setAnchorLocation(AnchorLocation.CONTENT_BOTTOM_RIGHT);
    }

    /**
     * Shows the pane.
     *
     * @param node           the target node
     * @param boundsInParent the node's bounds relative to the {@link BoundsPane}
     * @param rootWidth      the width of the scene's root
     * @param rootHeight     the height of the scene's root
     */
    public void show(Node node, Bounds boundsInParent, double rootWidth, double rootHeight) {
        if (tooltip.isShowing()) {
            hide();
        }

        rootBounds.setWidth(rootWidth);
        rootBounds.setHeight(rootHeight);

        double nodeWidth = boundsInParent.getMaxX() - boundsInParent.getMinX();
        double nodeHeight = boundsInParent.getMaxY() - boundsInParent.getMinY();

        viewportBounds.setLayoutX(boundsInParent.getMinX());
        viewportBounds.setLayoutY(boundsInParent.getMinY());
        viewportBounds.setWidth(nodeWidth);
        viewportBounds.setHeight(nodeHeight);

        // for some reason stage width may not be equal to the root node width,
        // so we introduce some delta to compensate
        if (rootWidth - nodeWidth < 2 && rootHeight - nodeHeight < 2) {
            viewportBounds.setWidth(0);
            viewportBounds.setHeight(0);
        }

        var curtain = Shape.subtract(rootBounds, viewportBounds);
        curtain.setMouseTransparent(false);
        curtain.setFill(Color.GREEN);
        curtain.setOpacity(0.5);

        getChildren().add(curtain);

        Point2D screenXY = node.localToScreen(
            node.getBoundsInLocal().getMinX(),
            node.getBoundsInLocal().getMinY()
        );

        var header = ClassInfoCache.get(node).simpleClassName();
        if (node.getId() != null) {
            header += " id=\"" + node.getId() + "\"";
        }
        var styleClass = node.getStyleClass();
        if (!styleClass.isEmpty()) {
            header += " class=\"" + String.join(" ", styleClass) + "\"";
        }

        var text = """
            %s
            x: %s y: %s
            width: %s height: %s
            """.formatted(
            header,
            DECIMAL_FORMAT.format(boundsInParent.getMinX()),
            DECIMAL_FORMAT.format(boundsInParent.getMinY()),
            DECIMAL_FORMAT.format(nodeWidth),
            DECIMAL_FORMAT.format(nodeHeight)
        );

        tooltip.setText(text);
        tooltip.show(node.getScene().getWindow(), screenXY.getX(), screenXY.getY());
    }

    /**
     * The opposite of {@link #show(Node, Bounds, double, double)}.
     */
    public void hide() {
        try {
            getChildren().clear();
            tooltip.hide();
        } catch (Exception ignored) {
            // UnsupportedOperationException when closing the monitored
            // window without disabling the inspect mode
        }
    }
}
