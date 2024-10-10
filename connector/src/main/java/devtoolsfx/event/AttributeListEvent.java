package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.attributes.Attribute;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Notifies about detected attribute changes in a specific {@link AttributeCategory}.
 *
 * @param eventSource the event source
 * @param element     the element whose attributes have been changed
 * @param category    the attribute category
 * @param attributes  the list of changed attributes
 */
@NullMarked
public record AttributeListEvent(EventSource eventSource,
                                 Element element,
                                 AttributeCategory category,
                                 List<Attribute<?>> attributes) implements ConnectorEvent, ElementEvent {

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString()
            + " | class=" + element.getSimpleClassName()
            + " | category=" + category
            + " | attributes=["
            + attributes.stream().map(Attribute::toLogString).collect(Collectors.joining("; "))
            + "]";
    }
}
