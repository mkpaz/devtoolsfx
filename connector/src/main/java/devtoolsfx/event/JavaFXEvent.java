package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;
import javafx.event.Event;
import javafx.event.EventType;
import org.jspecify.annotations.NullMarked;

/**
 * A wrapper for {@link javafx.event.Event} to dispatch JavaFX events via the connector event bus.
 *
 * @param eventSource the source of the event
 * @param element     the node (element) that triggered the event
 * @param eventType   the type of JavaFX event
 * @param value       the event as a string, or any other payload in a human-readable format
 */
@NullMarked
public record JavaFXEvent(EventSource eventSource,
                          Element element,
                          EventType<? extends Event> eventType,
                          String value) implements ConnectorEvent, ElementEvent {

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString()
            + " | type=" + eventType
            + " | value=" + value;
    }
}
