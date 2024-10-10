package devtoolsfx.event;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.stage.Window;
import org.jspecify.annotations.NullMarked;

/**
 * Notifies about changes to the window properties.
 *
 * @param eventSource the event source
 * @param position    the window's position on the screen
 * @param size        the window's size
 * @param focused     whether the window is focused
 */
@NullMarked
public record WindowPropertiesEvent(EventSource eventSource,
                                    Point2D position,
                                    Dimension2D size,
                                    boolean focused) implements ConnectorEvent {

    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString()
            + " | x=" + position.getX() + " y=" + position.getY()
            + " | width=" + size.getWidth() + " height=" + size.getHeight()
            + " | focused=" + focused;
    }

    public static WindowPropertiesEvent of(EventSource eventSource, Window window) {
        return new WindowPropertiesEvent(
            eventSource,
            new Point2D(window.getX(), window.getY()),
            new Dimension2D(window.getWidth(), window.getHeight()),
            window.isFocused()
        );
    }
}
