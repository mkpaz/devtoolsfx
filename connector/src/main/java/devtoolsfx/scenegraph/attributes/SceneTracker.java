package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullMarked
public final class SceneTracker extends Tracker {

    public static final Set<String> NON_REFLECTIVE_PROPERTIES = Set.of("stylesheets", "userData");

    private final Map<String, ObservableValue<?>> orderedProperties = new TreeMap<>();

    // Only one reflective tracker per target is allowed, and we already use one to obtain
    // the window properties. However, the scene contains too many attributes to ignore,
    // so this is the custom partially reflective implementation.
    public SceneTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.SCENE);
    }

    @Override
    public void reload(String... properties) {
        Scene scene = (Scene) getTarget();
        if (scene == null) {
            return;
        }

        List<String> supportedProperties = Stream.concat(
            orderedProperties.keySet().stream(),
            NON_REFLECTIVE_PROPERTIES.stream()
        ).sorted().collect(Collectors.toList());

        reload(property -> read(scene, property), supportedProperties, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof Window;
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void beforeSetTarget(Object target) {
        scan();
    }

    @Override
    protected void beforeResetTarget(Object target) {
        orderedProperties.clear();
    }

    @Override
    protected boolean doSetTarget(@Nullable Object candidate) {
        if (candidate instanceof Window window) {
            candidate = window.getScene();
        }
        return super.doSetTarget(candidate);
    }

    @SuppressWarnings({"ConstantValue"})
    private void scan() {
        orderedProperties.clear();
        for (Map.Entry<ObservableValue<?>, String> entry : propertyListener.getProperties().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                orderedProperties.put(entry.getValue(), entry.getKey());
            }
        }
    }

    private @Nullable Attribute<?> read(Scene scene, String property) {
        if (NON_REFLECTIVE_PROPERTIES.contains(property)) {
            return switch (property) {
                case "stylesheets" -> {
                    String stylesheets = String.join("\n", scene.getStylesheets());
                    yield new Attribute<>(
                        "stylesheets",
                        stylesheets,
                        "getStylesheets",
                        ObservableType.LIST,
                        DisplayHint.TEXT,
                        ValueState.defaultIf(scene.getStylesheets().isEmpty())
                    );
                }
                case "userData" -> new Attribute<>(
                    "userData",
                    String.valueOf(scene.getUserData()),
                    "userData",
                    ObservableType.NOT_OBSERVABLE,
                    DisplayHint.TEXT,
                    ValueState.defaultIf(scene.getUserData() == null)
                );
                default -> null;
            };
        }

        return read(property);
    }

    private Attribute<?> read(String property) {
        ObservableValue<?> obs = orderedProperties.get(property);
        Object value = obs.getValue(); // can be null and when it's null we won't get any DisplayHint...

        ObservableType obsType = ObservableType.of(obs);
        String field = property + PropertyListener.PROPERTY_SUFFIX;

        // ValueState.NOT_APPLICABLE everywhere, because it's not possible to guess
        // whether the observable property has default value or not
        return switch (value) {
            // primitive properties
            case Boolean bool -> new Attribute<>(
                property,
                bool,
                field,
                null,
                obsType,
                DisplayHint.BOOLEAN,
                ValueState.AUTO
            );
            case Integer number -> new Attribute<>(
                property,
                number,
                field,
                null,
                obsType,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case Double number -> new Attribute<>(
                property,
                number,
                field,
                null,
                obsType,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case String str -> new Attribute<>(
                property,
                str,
                field,
                null,
                obsType,
                DisplayHint.TEXT,
                ValueState.AUTO
            );
            // object properties
            case Enum<?> enumeration -> new Attribute<>(
                property,
                enumeration,
                field,
                null,
                obsType,
                DisplayHint.ENUM,
                ValueState.AUTO,
                List.of(enumeration.getClass().getEnumConstants())
            );
            case Color color -> new Attribute<>(
                property,
                color,
                field,
                null,
                obsType,
                DisplayHint.COLOR,
                ValueState.AUTO
            );
            case Image image -> new Attribute<>(
                property,
                image,
                field,
                null,
                obsType,
                DisplayHint.IMAGE,
                ValueState.AUTO
            );
            case Background background -> new Attribute<>(
                property,
                background,
                field,
                null,
                obsType,
                DisplayHint.BACKGROUND,
                ValueState.AUTO
            );
            case Border border -> new Attribute<>(
                property,
                border,
                field,
                null,
                obsType,
                DisplayHint.BORDER,
                ValueState.AUTO
            );
            case Tooltip tooltip -> new Attribute<>(
                property,
                tooltip.getText(),
                field,
                null,
                obsType,
                DisplayHint.TEXT,
                ValueState.AUTO
            );
            // the remainder is ObjectProperty<?>
            case null, default -> new Attribute<>(
                property,
                String.valueOf(value),
                field,
                null,
                obsType,
                DisplayHint.OBJECT,
                ValueState.AUTO
            );
        };
    }
}
