package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.attributes.Attribute;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import org.jspecify.annotations.NullMarked;

/**
 * Notifies about a single attribute change in a specific category.
 * The difference from the {@link AttributeListEvent} is semantic,
 * differentiating category changes from a single attribute update.
 *
 * @param eventSource the event source
 * @param element     the element whose attributes have been changed
 * @param category    the attribute category
 * @param attribute   the changed attribute
 */
@NullMarked
public record AttributeUpdatedEvent(EventSource eventSource,
                                    Element element,
                                    AttributeCategory category,
                                    Attribute<?> attribute) implements ConnectorEvent, ElementEvent {

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString()
            + " | class=" + element.getSimpleClassName()
            + " | category: " + category
            + " | attribute: " + attribute.toLogString();
    }
}
