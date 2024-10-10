package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.stage.Window;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public final class WindowTracker extends Tracker {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "width", "height", "x", "y", "opacity", "focused", "showing",
        "outputScaleX", "outputScaleY", "renderScaleX", "renderScaleY", "forceIntegerRenderScale", "userData"
    );

    public WindowTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.WINDOW);
    }

    @Override
    public void reload(String... properties) {
        Window window = (Window) getTarget();
        if (window == null) {
            return;
        }

        reload(property -> read(window, property), SUPPORTED_PROPERTIES, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof Window;
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(Window window, String property) {
        return switch (property) {
            case "width" -> new Attribute<>(
                "width",
                window.getWidth(),
                "widthProperty",
                ObservableType.of(window.widthProperty()),
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "height" -> new Attribute<>(
                "height",
                window.getHeight(),
                "heightProperty",
                ObservableType.of(window.heightProperty()),
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "x" -> new Attribute<>(
                "x",
                window.getX(),
                "xProperty",
                ObservableType.of(window.xProperty()),
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "y" -> new Attribute<>(
                "y",
                window.getY(),
                "yProperty",
                ObservableType.of(window.yProperty()),
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "opacity" -> new Attribute<>(
                "opacity",
                window.getOpacity(),
                "opacityProperty",
                null,
                ObservableType.of(window.opacityProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(window.getOpacity() == 1.0),
                List.of(0.0, 1.0)
            );
            case "focused" -> new Attribute<>(
                "focused",
                window.isFocused(),
                "focusedProperty",
                ObservableType.of(window.focusedProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(!window.isFocused())
            );
            case "showing" -> new Attribute<>(
                "showing",
                window.isShowing(),
                "showingProperty",
                ObservableType.of(window.showingProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(!window.isShowing())
            );
            case "outputScaleX" -> new Attribute<>(
                "outputScaleX",
                window.getOutputScaleX(),
                "outputScaleXProperty",
                ObservableType.of(window.outputScaleXProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(window.getOutputScaleX() == 1.0)
            );
            case "outputScaleY" -> new Attribute<>(
                "outputScaleY",
                window.getOutputScaleY(),
                "outputScaleYProperty",
                ObservableType.of(window.outputScaleYProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(window.getOutputScaleY() == 1.0)
            );
            case "renderScaleX" -> new Attribute<>(
                "renderScaleX",
                window.getRenderScaleX(),
                "renderScaleXProperty",
                ObservableType.of(window.renderScaleXProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(window.getRenderScaleX() == 1.0)
            );
            case "renderScaleY" -> new Attribute<>(
                "renderScaleY",
                window.getRenderScaleY(),
                "renderScaleYProperty",
                ObservableType.of(window.renderScaleYProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(window.getRenderScaleY() == 1.0)
            );
            case "forceIntegerRenderScale" -> new Attribute<>(
                "forceIntegerRenderScale",
                window.isForceIntegerRenderScale(),
                "forceIntegerRenderScaleProperty",
                ObservableType.of(window.forceIntegerRenderScaleProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(!window.isForceIntegerRenderScale())
            );
            case "userData" -> new Attribute<>(
                "userData",
                String.valueOf(window.getUserData()),
                "userData",
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.TEXT,
                ValueState.defaultIf(window.getUserData() == null)
            );
            default -> null;
        };
    }
}
