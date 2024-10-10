package devtoolsfx.event;

import devtoolsfx.connector.Connector;
import org.jspecify.annotations.NullMarked;

/**
 * The base sealed interface for all {@link Connector} events.
 */
@NullMarked
public sealed interface ConnectorEvent permits
    AttributeListEvent,
    AttributeUpdatedEvent,
    ExceptionEvent,
    JavaFXEvent,
    MousePosEvent,
    NodeAddedEvent,
    NodeRemovedEvent,
    NodeSelectedEvent,
    NodeStyleClassEvent,
    NodeVisibilityEvent,
    RootChangedEvent,
    WindowClosedEvent,
    WindowPropertiesEvent {

    EventSource eventSource();

    String toLogString();
}
