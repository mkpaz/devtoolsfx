package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import devtoolsfx.util.SceneUtils;
import javafx.scene.Parent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The {@link Tracker} implementation for the {@link Parent} class.
 */
@NullMarked
public final class ParentTracker extends Tracker {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "stylesheets", "needsLayout", "childCount", "branchCount"
    );

    public ParentTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.PARENT);
    }

    @Override
    public void reload(String... properties) {
        Parent parent = (Parent) getTarget();
        if (parent == null) {
            return;
        }

        reload(property -> read(parent, property), SUPPORTED_PROPERTIES, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof Parent;
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(Parent parent, String property) {
        return switch (property) {
            case "stylesheets" -> {
                String stylesheets = String.join("\n", parent.getStylesheets());
                yield new Attribute<>(
                    "stylesheets",
                    stylesheets,
                    "getStylesheets",
                    ObservableType.LIST,
                    DisplayHint.TEXT,
                    ValueState.AUTO
                );
            }
            case "needsLayout" -> new Attribute<>(
                "needsLayout",
                parent.isNeedsLayout(),
                "needsLayoutProperty",
                ObservableType.READ_ONLY,
                DisplayHint.BOOLEAN,
                ValueState.AUTO
            );
            case "childCount" -> new Attribute<>(
                "childCount",
                SceneUtils.countChildren(parent),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "branchCount" -> new Attribute<>(
                "branchCount",
                SceneUtils.countNodesInBranch(parent),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            default -> null;
        };
    }
}
