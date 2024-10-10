package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static java.lang.System.Logger.Level;

/**
 * The {@link Tracker} implementation for the {@link Shape} class.
 */
@NullMarked
public final class ShapeTracker extends Tracker {

    private static final System.Logger LOGGER = System.getLogger(ReflectiveTracker.class.getName());

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "fill", "smooth", "stroke", "strokeType", "strokeWidth", "strokeDashArray",
        "strokeDashOffset", "strokeLineCap", "strokeLineJoin", "strokeMiterLimit"
    );

    public ShapeTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.SHAPE);
    }

    @Override
    public void reload(String... properties) {
        Shape shape = (Shape) getTarget();
        if (shape == null) {
            return;
        }

        reload(property -> read(shape, property), SUPPORTED_PROPERTIES, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof Shape;
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(Shape shape, String property) {
        return switch (property) {
            case "fill" -> {
                if (shape.getFill() == null) {
                    LOGGER.log(Level.WARNING, "[Error] null shape fill for node: " + target);
                }

                yield new Attribute<>(
                    "fill",
                    shape.getFill(),
                    "fillProperty",
                    "-fx-fill",
                    ObservableType.of(shape.fillProperty()),
                    DisplayHint.COLOR,
                    ValueState.defaultIf(Color.BLACK.equals(shape.getFill()))
                );
            }
            case "smooth" -> new Attribute<>(
                "smooth",
                shape.isSmooth(),
                "smoothProperty",
                "-fx-smooth",
                ObservableType.of(shape.smoothProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(shape.isSmooth())
            );
            case "stroke" -> new Attribute<>(
                "stroke",
                shape.getStroke(),
                "strokeProperty",
                "-fx-stroke",
                ObservableType.of(shape.strokeProperty()),
                DisplayHint.COLOR,
                ValueState.defaultIf((Color.BLACK.equals(shape.getFill()) &&
                    (shape instanceof Line || shape instanceof Polyline || shape instanceof Path)
                ) || shape.getStroke() == null)
            );
            case "strokeType" -> new Attribute<>(
                "strokeType",
                shape.getStrokeType(),
                "strokeTypeProperty",
                "-fx-stroke-type",
                ObservableType.of(shape.strokeTypeProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(StrokeType.CENTERED.equals(shape.getStrokeType())),
                List.of(StrokeType.values())
            );
            case "strokeWidth" -> new Attribute<>(
                "strokeWidth",
                shape.getStrokeWidth(),
                "strokeWidthProperty",
                "-fx-stroke-width",
                ObservableType.of(shape.strokeWidthProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(shape.getStrokeWidth() == 1)
            );
            case "strokeDashArray" -> new Attribute<>(
                "strokeWidth",
                shape.getStrokeDashArray(),
                "getStrokeDashArray",
                "-fx-stroke-dash-array",
                ObservableType.LIST,
                DisplayHint.OBJECT,
                ValueState.defaultIf(shape.getStrokeDashArray().isEmpty())
            );
            case "strokeDashOffset" -> new Attribute<>(
                "strokeDashOffset",
                shape.getStrokeDashOffset(),
                "strokeDashOffsetProperty",
                "-fx-stroke-dash-offset",
                ObservableType.of(shape.strokeDashOffsetProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(shape.getStrokeDashOffset() == 0)
            );
            case "strokeLineCap" -> new Attribute<>(
                "strokeLineCap",
                shape.getStrokeLineCap(),
                "strokeLineCapProperty",
                "-fx-stroke-line-cap",
                ObservableType.of(shape.strokeLineCapProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(shape.getStrokeLineCap() == StrokeLineCap.SQUARE),
                List.of(StrokeLineCap.values())
            );
            case "strokeLineJoin" -> new Attribute<>(
                "strokeLineJoin",
                shape.getStrokeLineJoin(),
                "strokeLineJoinProperty",
                "-fx-stroke-line-join",
                ObservableType.of(shape.strokeLineJoinProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(shape.getStrokeLineJoin() == StrokeLineJoin.MITER),
                List.of(StrokeLineJoin.values())
            );
            case "strokeMiterLimit" -> new Attribute<>(
                "strokeMiterLimit",
                shape.getStrokeMiterLimit(),
                "strokeMiterLimitProperty",
                "-fx-stroke-miter-limit",
                ObservableType.of(shape.strokeMiterLimitProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(shape.getStrokeMiterLimit() == 10)
            );
            default -> null;
        };
    }
}
