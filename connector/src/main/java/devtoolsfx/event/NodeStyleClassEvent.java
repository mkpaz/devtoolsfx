package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Notifies about changes to the node's style class list.
 *
 * @param eventSource the source of the event
 * @param element     the element whose style class list has changed
 * @param styleClass  the new style class list
 */
@NullMarked
public record NodeStyleClassEvent(EventSource eventSource,
                                  Element element,
                                  List<String> styleClass) implements ConnectorEvent {
    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString()
            + " | class=" + element.getSimpleClassName()
            + " | styleClass=" + styleClass;
    }
}
