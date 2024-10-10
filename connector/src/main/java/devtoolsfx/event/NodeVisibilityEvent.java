package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;
import org.jspecify.annotations.NullMarked;

/**
 * Notifies about node visibility state changes.
 *
 * @param eventSource the source of the event
 * @param element     the element whose visibility state has changed
 * @param visible     the new visibility state
 */
@NullMarked
public record NodeVisibilityEvent(EventSource eventSource,
                                  Element element,
                                  boolean visible) implements ConnectorEvent {

    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString()
            + " | class=" + element.getSimpleClassName()
            + " | visible=" + visible;
    }
}
