package devtoolsfx.connector;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import devtoolsfx.scenegraph.attributes.Tracker;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.stream.Collectors;

/**
 * Listens for all types of attributes for the given target.
 */
@NullMarked
final class AttributeListener {

    private static final Logger LOGGER = System.getLogger(AttributeListener.class.getName());

    private final EnumMap<AttributeCategory, Tracker> trackers;
    private @Nullable Object target;

    public AttributeListener(EventBus eventBus,
                             EventSource eventSource) {
        trackers = Arrays.stream(AttributeCategory.values())
            .map(category -> AttributeCategory.createTracker(category, eventBus, eventSource))
            .collect(Collectors.toMap(
                Tracker::getCategory,
                tracker -> tracker,
                (l, r) -> {
                    LOGGER.log(Level.WARNING, "duplicate keys " + l.getCategory() + " and " + r.getCategory());
                    return l;
                },
                () -> new EnumMap<>(AttributeCategory.class)
            ));
    }

    /**
     * Replaces the currently tracked target with a new one.
     */
    public void setTarget(@Nullable Object candidate) {
        if (candidate != null && candidate == target) {
            return;
        }

        target = candidate;
        setTargetToAllTrackers();
    }

    /**
     * Reloads (reads again) all attributes.
     */
    public void reload() {
        trackers.values().forEach(Tracker::reload);
    }

    /**
     * Reloads (reads again) all attributes in the specified category.
     */
    public void reloadCategory(AttributeCategory category) {
        trackers.get(category).reload();
    }

    /**
     * Reloads (reads again) the specified attribute from the given category.
     */
    public void reloadAttribute(AttributeCategory category, String property) {
        trackers.get(category).reload(property);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Reloads (reads again) all attributes in all trackers (categories).
     */
    private void setTargetToAllTrackers() {
        for (var tracker : trackers.values()) {
            if (tracker.accepts(target)) {
                tracker.setTarget(target);
            } else {
                tracker.reset();
            }
        }
    }
}
