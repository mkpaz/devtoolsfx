package devtoolsfx.event;

import devtoolsfx.connector.LocalElement;
import devtoolsfx.scenegraph.Element;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.jspecify.annotations.NullMarked;

/**
 * Notifies about changes in the mouse position.
 * The position should (and must) be given in the scene's coordinates (not in local node coordinates).
 *
 * @param eventSource the event source
 * @param element     the element whose mouse position has changed
 * @param x           the x coordinate
 * @param y           the y coordinate
 */
@NullMarked
public record MousePosEvent(EventSource eventSource,
                            Element element,
                            double x,
                            double y) implements ConnectorEvent, ElementEvent {

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString()
            + " | class=" + element.getSimpleClassName()
            + " | x=" + x + " y=" + y;
    }

    public static MousePosEvent of(EventSource eventSource, MouseEvent mouseEvent) {
        return new MousePosEvent(eventSource,
            LocalElement.of((Node) mouseEvent.getSource()),
            mouseEvent.getSceneX(),
            mouseEvent.getSceneY()
        );
    }
}
