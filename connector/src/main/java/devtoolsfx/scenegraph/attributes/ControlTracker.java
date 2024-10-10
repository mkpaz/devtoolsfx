package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.scene.control.Control;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The {@link Tracker} implementation for the {@link Control} class.
 */
@NullMarked
public final class ControlTracker extends Tracker {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "skin", "minWidth", "minHeight", "prefWidth", "prefHeight", "maxWidth", "maxHeight",
        "stylesheets", "userAgentStylesheet"
    );

    public ControlTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.CONTROL);
    }

    @Override
    public void reload(String... properties) {
        Control control = (Control) getTarget();
        if (control == null) {
            return;
        }

        reload(property -> read(control, property), SUPPORTED_PROPERTIES, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof Control;
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(Control control, String property) {
        return switch (property) {
            case "skin" -> new Attribute<>(
                "skin",
                control.getSkin().getClass().getCanonicalName(),
                "skin",
                "-fx-skin",
                ObservableType.of(control.skinProperty()),
                DisplayHint.TEXT,
                ValueState.AUTO
            );
            case "minWidth" -> new Attribute<>(
                "minWidth",
                control.getMinWidth(),
                "minWidthProperty",
                ObservableType.of(control.minWidthProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(control.getMinWidth() == Control.USE_COMPUTED_SIZE)
            );
            case "minHeight" -> new Attribute<>(
                "minHeight",
                control.getMinHeight(),
                "minHeightProperty",
                ObservableType.of(control.minHeightProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(control.getMinHeight() == Control.USE_COMPUTED_SIZE)
            );
            case "prefWidth" -> new Attribute<>(
                "prefWidth",
                control.getPrefWidth(),
                "prefWidthProperty",
                ObservableType.of(control.prefWidthProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(control.getPrefWidth() == Control.USE_COMPUTED_SIZE)
            );
            case "prefHeight" -> new Attribute<>(
                "prefHeight",
                control.getPrefHeight(),
                "prefHeightProperty",
                ObservableType.of(control.prefHeightProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(control.getPrefHeight() == Control.USE_COMPUTED_SIZE)
            );
            case "maxWidth" -> new Attribute<>(
                "maxWidth",
                control.getMaxWidth(),
                "maxWidthProperty",
                ObservableType.of(control.maxWidthProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(control.getMaxWidth() == Control.USE_COMPUTED_SIZE)
            );
            case "maxHeight" -> new Attribute<>(
                "maxHeight",
                control.getMaxHeight(),
                "maxHeightProperty",
                ObservableType.of(control.maxHeightProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(control.getMaxHeight() == Control.USE_COMPUTED_SIZE)
            );
            case "stylesheets" -> {
                String stylesheets = String.join("\n", control.getStylesheets());
                yield new Attribute<>(
                    "stylesheets",
                    stylesheets,
                    "getStylesheets",
                    ObservableType.LIST,
                    DisplayHint.TEXT,
                    ValueState.defaultIf(control.getStylesheets().isEmpty())
                );
            }
            case "userAgentStylesheet" -> new Attribute<>(
                "userAgentStylesheet",
                control.getUserAgentStylesheet(),
                "userAgentStylesheet",
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.TEXT,
                ValueState.defaultIf(control.getUserAgentStylesheet() == null)
            );
            default -> null;
        };
    }
}
