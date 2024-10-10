package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The {@link Tracker} implementation for the {@link Region} class.
 */
@NullMarked
public final class RegionTracker extends Tracker {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "padding", "insets", "snapToPixel", "shape", "scaleShape", "centerShape",
        "userAgentStylesheet", "minWidth", "minHeight", "prefWidth", "prefHeight", "maxWidth", "maxHeight"
    );

    public RegionTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.REGION);
    }

    @Override
    public void reload(String... properties) {
        Region region = (Region) getTarget();
        if (region == null) {
            return;
        }

        reload(property -> read(region, property), SUPPORTED_PROPERTIES, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof Region;
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(Region region, String property) {
        return switch (property) {
            case "insets" -> new Attribute<>(
                "insets",
                region.getInsets(),
                "insetsProperty",
                ObservableType.READ_ONLY,
                DisplayHint.INSETS,
                ValueState.defaultIf(region.getInsets() == null || Insets.EMPTY.equals(region.getInsets()))
            );
            case "padding" -> new Attribute<>(
                "padding",
                region.getPadding(),
                "paddingProperty",
                "-fx-padding",
                ObservableType.of(region.paddingProperty()),
                DisplayHint.INSETS,
                ValueState.defaultIf(region.getPadding() == null || Insets.EMPTY.equals(region.getPadding()))
            );
            case "snapToPixel" -> new Attribute<>(
                "snapToPixel",
                region.isSnapToPixel(),
                "snapToPixelProperty",
                "-fx-snap-to-pixel",
                ObservableType.of(region.snapToPixelProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(region.isSnapToPixel())
            );
            case "shape" -> new Attribute<>(
                "shape",
                region.getShape() != null ? String.valueOf(region.getShape()) : null,
                "shapeProperty",
                "-fx-shape",
                ObservableType.of(region.shapeProperty()),
                DisplayHint.TEXT,
                ValueState.defaultIf(region.getShape() == null)
            );
            case "scaleShape" -> new Attribute<>(
                "scaleShape",
                region.isScaleShape(),
                "scaleShapeProperty",
                "-fx-scale-shape",
                ObservableType.of(region.scaleShapeProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(region.isScaleShape())
            );
            case "centerShape" -> new Attribute<>(
                "centerShape",
                region.isCenterShape(),
                "centerShapeProperty",
                "-fx-position-shape",
                ObservableType.of(region.centerShapeProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(region.isCenterShape())
            );
            case "userAgentStylesheet" -> new Attribute<>(
                "userAgentStylesheet",
                region.getUserAgentStylesheet(),
                "userAgentStylesheet",
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.TEXT,
                ValueState.defaultIf(region.getUserAgentStylesheet() == null)
            );
            case "minWidth" -> new Attribute<>(
                "minWidth",
                region.getMinWidth(),
                "minWidthProperty",
                "-fx-min-width",
                ObservableType.of(region.minWidthProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(region.getMinWidth() == Control.USE_COMPUTED_SIZE)
            );
            case "minHeight" -> new Attribute<>(
                "minHeight",
                region.getMinHeight(),
                "minHeightProperty",
                "-fx-min-height",
                ObservableType.of(region.minHeightProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(region.getMinHeight() == Control.USE_COMPUTED_SIZE)
            );
            case "prefWidth" -> new Attribute<>(
                "prefWidth",
                region.getPrefWidth(),
                "prefWidthProperty",
                "-fx-pref-width",
                ObservableType.of(region.prefWidthProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(region.getPrefWidth() == Control.USE_COMPUTED_SIZE)
            );
            case "prefHeight" -> new Attribute<>(
                "prefHeight",
                region.getPrefHeight(),
                "prefHeightProperty",
                "-fx-pref-height",
                ObservableType.of(region.prefHeightProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(region.getPrefHeight() == Control.USE_COMPUTED_SIZE)
            );
            case "maxWidth" -> new Attribute<>(
                "maxWidth",
                region.getMaxWidth(),
                "maxWidthProperty",
                "-fx-max-width",
                ObservableType.of(region.maxWidthProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(region.getMaxWidth() == Control.USE_COMPUTED_SIZE)
            );
            case "maxHeight" -> new Attribute<>(
                "maxHeight",
                region.getMaxHeight(),
                "maxHeightProperty",
                "-fx-max-height",
                ObservableType.of(region.maxHeightProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(region.getMaxHeight() == Control.USE_COMPUTED_SIZE)
            );
            default -> null;
        };
    }
}
