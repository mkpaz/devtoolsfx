package devtoolsfx.connector;

import devtoolsfx.util.SceneUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static java.lang.System.Logger;
import static java.lang.System.Logger.Level;

/**
 * Contains the logic to implement highlighting of arbitrary nodes within the given parent node.
 * See {@link #attach(Parent)}. There are three types of nodes: a colored rectangle to
 * highlight layoutBounds, a stroked rectangle to highlight boundsInParent, and a line
 * to highlight the baselineOffset.
 */
@NullMarked
final class BoundsPane {

    private static final Logger LOGGER = System.getLogger(BoundsPane.class.getName());

    private final Rectangle boundsInParentRect = createBoundsInParentRect();
    private final Rectangle layoutBoundsRect = createLayoutBoundsRect();
    private final Line baselineStroke = createBaselineStroke();
    private @Nullable Parent parent;

    public BoundsPane() {
        // pass
    }

    /**
     * Attaches the overlay to the given parent node. When attached, the BoundsPane is able
     * to display highlighting nodes above the parent node in the parent coordinate system.
     * Use {@link #toggleLayoutBoundsDisplay(Node)}, {@link #toggleBoundsInParentDisplay(Node)}
     * and {@link #toggleBaselineDisplay} to highlight any descendant nodes of the parent.
     */
    public void attach(@Nullable Parent candidate) {
        if (parent != null) {
            detach();
        }

        // find parent we can use to hang bounds rectangles
        parent = SceneUtils.findNearestPane(candidate);

        if (parent == null) {
            if (candidate != null) {
                LOGGER.log(Level.WARNING, "Could not find writable parent to add overlay nodes, overlay is disabled");
            }

            toggleLayoutBoundsDisplay(null);
            toggleBoundsInParentDisplay(null);
            toggleBaselineDisplay(null);
        } else {
            SceneUtils.addToNode(parent, boundsInParentRect);
            SceneUtils.addToNode(parent, layoutBoundsRect);
            SceneUtils.addToNode(parent, baselineStroke);
        }
    }

    /**
     * Removes the overlay highlighting nodes.
     */
    public void detach() {
        if (parent != null) {
            SceneUtils.removeFromNode(parent, boundsInParentRect);
            SceneUtils.removeFromNode(parent, layoutBoundsRect);
            SceneUtils.removeFromNode(parent, baselineStroke);
            parent = null;
        }
    }

    /**
     * Updates the properties of the layoutBounds rectangle to set or remove the highlight
     * for the specified target node. If the target node is null the current selection will
     * be removed.
     */
    public void toggleLayoutBoundsDisplay(@Nullable Node target) {
        if (target == null || parent == null) {
            hideRect(layoutBoundsRect);
            return;
        }

        Bounds bounds = calcRelativeBounds(target, true);
        if (bounds == null || !isFinite(bounds)) {
            hideRect(layoutBoundsRect);
        } else {
            resizeRelocateRect(layoutBoundsRect, bounds);
            layoutBoundsRect.setVisible(true);
        }
    }

    /**
     * Updates the properties of the boundsInParent rectangle to set or remove the highlight
     * for the specified target node. If the target node is null the current selection will
     * be removed.
     */
    public void toggleBoundsInParentDisplay(@Nullable Node target) {
        if (target == null || parent == null) {
            hideRect(boundsInParentRect);
            return;
        }

        Bounds bounds = calcRelativeBounds(target, false);
        if (bounds == null || !isFinite(bounds)) {
            hideRect(boundsInParentRect);
        } else {
            resizeRelocateRect(boundsInParentRect, bounds);
            boundsInParentRect.setVisible(true);
        }
    }

    /**
     * Updates the baseline stroke properties to set or remove the baseline offset highlighting
     * for the specified target node. If the target node is null the current selection will be removed.
     */
    public void toggleBaselineDisplay(@Nullable Node target) {
        // protect from the Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN
        if (parent == null || target == null || target.getBaselineOffset() == Node.BASELINE_OFFSET_SAME_AS_HEIGHT) {
            hideStroke(baselineStroke);
            return;
        }

        Bounds b = target.getLayoutBounds();
        Point2D scenePos = target.localToScene(b.getMinX(), b.getMinY() + target.getBaselineOffset());
        Point2D localPos = parent.sceneToLocal(scenePos);

        // No jokes: if one of the coordinates is not finite, it will (silently) break the rendering.
        // The overlay rectangle will be "frozen" and will only "unfreeze" after a window resize.
        if (!isFinite(localPos)) {
            hideStroke(baselineStroke);
            return;
        }

        baselineStroke.setStartX(localPos.getX());
        baselineStroke.setEndX(localPos.getX() + b.getWidth());
        baselineStroke.setStartY(localPos.getY());
        baselineStroke.setEndY(localPos.getY());
        baselineStroke.setVisible(true);
    }

    /**
     * Calculates the bounds of the node relative to the current parent node.
     *
     * @param layoutBounds calculate for the layoutBounds, if false boundsInParent will be used
     */
    public @Nullable Bounds calcRelativeBounds(Node node, boolean layoutBounds) {
        if (parent == null) {
            return null;
        }

        var bounds = node.getBoundsInParent();
        double layoutX = 0, layoutY = 0;
        if (layoutBounds) {
            bounds = node.getLayoutBounds();
            layoutX = node.getLayoutX();
            layoutY = node.getLayoutY();
        }

        if (node.getParent() == null) {
            // no parent, so the node is root
            return new BoundingBox(
                bounds.getMinX() + layoutX + 1, bounds.getMinY() + layoutY + 1,
                bounds.getWidth() - 2, bounds.getHeight() - 2
            );
        } else {
            Point2D scenePos = node.getParent().localToScene(bounds.getMinX(), bounds.getMinY());
            Point2D parentPos = parent.sceneToLocal(scenePos);
            return new BoundingBox(
                parentPos.getX() + layoutX, parentPos.getY() + layoutY,
                bounds.getWidth(), bounds.getHeight()
            );
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a {@link Rectangle} that will be used in the overlay to highlight
     * the {@link Node#boundsInParentProperty()} of a selected node.
     */
    private Rectangle createBoundsInParentRect() {
        var r = new Rectangle();
        r.setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "layoutBoundsRect");
        r.setFill(null);
        r.setStroke(Color.GREEN);
        r.setStrokeType(StrokeType.INSIDE);
        r.setOpacity(0.8);
        r.getStrokeDashArray().addAll(3.0, 3.0);
        r.setStrokeWidth(1);
        r.setManaged(false);
        r.setMouseTransparent(true);
        return r;
    }

    /**
     * Creates a {@link Rectangle} that will be used in the overlay to highlight
     * the {@link Node#layoutBoundsProperty()} of a selected node.
     */
    private Rectangle createLayoutBoundsRect() {
        var r = new Rectangle();
        r.setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "boundsInParentRect");
        r.setFill(Color.YELLOW);
        r.setOpacity(0.5);
        r.setManaged(false);
        r.setMouseTransparent(true);
        return r;
    }

    /**
     * Creates a {@link Line} that will be used in the overlay to highlight
     * the {@link Node#getBaselineOffset()} of a selected node.
     */
    private Line createBaselineStroke() {
        var l = new Line();
        l.setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "baselineLine");
        l.setStroke(Color.RED);
        l.setOpacity(.75);
        l.setStrokeWidth(1);
        l.setManaged(false);
        return l;
    }

    /**
     * Hides the given line.
     */
    private void hideStroke(Line line) {
        line.setStartX(0);
        line.setEndX(0);
        line.setStartY(0);
        line.setEndY(0);
        line.setVisible(false);
    }

    /**
     * Hides the given rectangle.
     */
    private void hideRect(Rectangle rect) {
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(0);
        rect.setHeight(0);
        rect.setVisible(false);
    }

    /**
     * Resizes and relocates the specified rectangle according to the provided bounds.
     */
    private void resizeRelocateRect(Rectangle rect, Bounds bounds) {
        rect.setX(bounds.getMinX());
        rect.setY(bounds.getMinY());
        rect.setWidth(bounds.getWidth());
        rect.setHeight(bounds.getHeight());
    }

    /**
     * Ensures that all point coordinates are finite double values.
     */
    private boolean isFinite(Point2D point) {
        return isFinite(point.getX()) && isFinite(point.getY());
    }

    /**
     * Ensures that all bounds values are finite double values.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isFinite(Bounds bounds) {
        return isFinite(bounds.getMinX())
            && isFinite(bounds.getMinY())
            && isFinite(bounds.getWidth())
            && isFinite(bounds.getHeight());
    }

    /**
     * Ensures that double is finite (not Infinity or NaN).
     */
    private boolean isFinite(double d) {
        return Double.isFinite(d) && !Double.isNaN(d);
    }
}
