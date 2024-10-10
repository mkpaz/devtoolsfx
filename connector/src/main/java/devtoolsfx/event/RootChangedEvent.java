package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;
import org.jspecify.annotations.NullMarked;

/**
 * Notifies about changes to the window's scene or the scene's root,
 * including situations when a new window (and thus a new scene and root) is added.
 *
 * @param eventSource the event source
 * @param element     the element used to access the properties of the scene's root node
 */
@NullMarked
public record RootChangedEvent(EventSource eventSource,
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
