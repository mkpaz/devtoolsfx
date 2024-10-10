package devtoolsfx.event;

import devtoolsfx.connector.LocalConnector;
import javafx.application.Platform;
import org.jspecify.annotations.NullMarked;

import java.lang.System.Logger;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import static java.lang.System.Logger.Level;

/**
 * A straightforward event bus implementation. Events are published in channels
 * distinguished by event type. It must only be called from the FXThread.
 */
@NullMarked
public final class EventBus {

    private static final Logger LOGGER = System.getLogger(LocalConnector.class.getName());

    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    /**
     * Creates new {@link EventBus} instance.
     * If you want to use global event bus go with singleton method instead.
     */
    public EventBus() {
        // pass
    }

    /**
     * Subscribe to an event type.
     */
    public <E extends ConnectorEvent> void subscribe(Class<? extends E> eventType, Consumer<E> subscriber) {
        Set<Consumer<?>> eventSubscribers = getOrCreateSubscribers(eventType);
        eventSubscribers.add(subscriber);
    }

    /**
     * Unsubscribe from all event types.
     */
    public <E extends ConnectorEvent> void unsubscribe(Consumer<E> subscriber) {
        subscribers.values().forEach(eventSubscribers -> eventSubscribers.remove(subscriber));
    }

    /**
     * Publish an event to all subscribers. The event is published to all consumers
     * which subscribed to this event type or any super class.
     */
    @SuppressWarnings("unchecked")
    public <E extends ConnectorEvent> void fire(E event) {
        Class<?> eventType = event.getClass();
        subscribers.keySet().stream()
            .filter(type -> type.isAssignableFrom(eventType))
            .flatMap(type -> subscribers.get(type).stream())
            .forEach(subscriber -> fire(event, (Consumer<E>) subscriber));
    }

    ///////////////////////////////////////////////////////////////////////////

    private <E> Set<Consumer<?>> getOrCreateSubscribers(Class<E> eventType) {
        return subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>());
    }

    private <E extends ConnectorEvent> void fire(E event, Consumer<E> subscriber) {
        try {
            if (Platform.isFxApplicationThread()) {
                subscriber.accept(event);
            } else {
                LOGGER.log(Level.WARNING, "Calling the event bus not from the FX thread");
                Platform.runLater(() -> subscriber.accept(event));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e.getMessage());
        }
    }
}
