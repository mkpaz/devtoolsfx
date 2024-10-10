package devtoolsfx.event;

import org.jspecify.annotations.NullMarked;

/**
 * Notifies that the window has been closed.
 *
 * @param eventSource the event source
 */
@NullMarked
public record WindowClosedEvent(EventSource eventSource) implements ConnectorEvent {

    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString();
    }
}
