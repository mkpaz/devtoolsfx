package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;

/**
 * The attribute category corresponds to {@link Tracker} implementations;
 * each of these is designed to work with one of the categories.
 */
public enum AttributeCategory {
    CONTROL,
    GRID_PANE,
    LABELED,
    IMAGE_VIEW,
    NODE,
    PARENT,
    REFLECTIVE,
    REGION,
    SCENE,
    SHAPE,
    TEXT,
    WINDOW;

    public static Tracker createTracker(AttributeCategory category,
                                        EventBus eventBus,
                                        EventSource eventSource) {
        return switch (category) {
            case CONTROL -> new ControlTracker(eventBus, eventSource);
            case GRID_PANE -> new GridPaneTracker(eventBus, eventSource);
            case LABELED -> new LabeledTracker(eventBus, eventSource);
            case IMAGE_VIEW -> new ImageViewTracker(eventBus, eventSource);
            case NODE -> new NodeTracker(eventBus, eventSource);
            case PARENT -> new ParentTracker(eventBus, eventSource);
            case REFLECTIVE -> new ReflectiveTracker(eventBus, eventSource);
            case REGION -> new RegionTracker(eventBus, eventSource);
            case SCENE -> new SceneTracker(eventBus, eventSource);
            case SHAPE -> new ShapeTracker(eventBus, eventSource);
            case TEXT -> new TextTracker(eventBus, eventSource);
            case WINDOW -> new WindowTracker(eventBus, eventSource);
        };
    }
}
