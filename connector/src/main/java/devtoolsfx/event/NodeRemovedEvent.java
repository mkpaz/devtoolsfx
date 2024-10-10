package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;
import org.jspecify.annotations.NullMarked;

/**
 * Notifies about the removed scene graph node element.
 *
 * @param eventSource the event source
 * @param element     the element used to access the properties of the removed node
 */
@NullMarked
public record NodeRemovedEvent(EventSource eventSource,
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
}
