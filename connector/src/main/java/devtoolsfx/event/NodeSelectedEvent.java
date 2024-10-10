package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;
import org.jspecify.annotations.NullMarked;

/**
 * Notifies about the selected scene graph node element.
 * Selection is not something that is supported by the scene graph API, but an
 * abstraction for the currently inspected node.
 *
 * @param eventSource the event source
 * @param element     the element used to access the properties of the selected node
 */
@NullMarked
public record NodeSelectedEvent(EventSource eventSource,
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
