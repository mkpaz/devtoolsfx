package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;
import org.jspecify.annotations.NullMarked;

/**
 * Notifies about the added scene graph node element.
 *
 * @param eventSource the event source
 * @param element     the element used to access the properties of the added node
 */
@NullMarked
public record NodeAddedEvent(EventSource eventSource,
                             Element element) implements ConnectorEvent, ElementEvent {

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString()
            + " | class=" + element.getSimpleClassName()
            + " | properties=" + (element.isWindowElement() ? element.getWindowProperties() : element.getNodeProperties());
    }

    public static NodeAddedEvent of(EventSource eventSource, Element element) {
        return new NodeAddedEvent(eventSource, element);
    }
}
