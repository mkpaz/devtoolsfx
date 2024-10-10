package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The {@link Tracker} implementation that attempts to reflectively obtain all
 * the properties of the target node.
 */
@NullMarked
public final class ReflectiveTracker extends Tracker {

    private final Map<String, ObservableValue<?>> orderedProperties = new TreeMap<>();
    private final Map<WritableValue<?>, String> styleableProperties = new HashMap<>();

    public ReflectiveTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.REFLECTIVE);
    }

    @Override
    public void reload(String... properties) {
        Object node = getTarget();
        if (node == null) {
            return;
        }

        reload(this::read, orderedProperties.keySet(), properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target != null;
    }

    @Override
    protected void beforeSetTarget(Object target) {
        scan(target);
    }

    @Override
    protected void beforeResetTarget(Object target) {
        orderedProperties.clear();
        styleableProperties.clear();
    }

    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({"rawtypes", "unchecked", "ConstantValue"})
    private void scan(Object target) {
        orderedProperties.clear();
        for (Map.Entry<ObservableValue<?>, String> entry : propertyListener.getProperties().entrySet()) {
            // should never happen, but double check there no null values in observed properties
            if (entry.getKey() != null && entry.getValue() != null) {
                orderedProperties.put(entry.getValue(), entry.getKey());
            }
        }

        styleableProperties.clear();
        if (target instanceof Node node) {
            for (CssMetaData meta : node.getCssMetaData()) {
                StyleableProperty<?> styleable = meta.getStyleableProperty(node);
                String name = meta.getProperty();
                if (styleable != null && name != null) {
                    styleableProperties.put(styleable, name);
                }
            }
        }
    }

    private Attribute<?> read(String property) {
        ObservableValue<?> obs = orderedProperties.get(property);
        Object value = obs.getValue(); // can be null and when it's null we won't get any DisplayHint...

        ObservableType obsType = ObservableType.of(obs);
        String field = property + PropertyListener.PROPERTY_SUFFIX;
        String styleable = obs instanceof WritableValue<?> writable ? styleableProperties.get(writable) : null;

        // ValueState.NOT_APPLICABLE everywhere, because it's not possible to guess
        // whether the observable property has default value or not
        return switch (value) {
            // primitive properties
            case Boolean bool -> new Attribute<>(
                property,
                bool,
                field,
                styleable,
                obsType,
                DisplayHint.BOOLEAN,
                ValueState.AUTO
            );
            case Integer number -> new Attribute<>(
                property,
                number,
                field,
                styleable,
                obsType,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case Double number -> new Attribute<>(
                property,
                number,
                field,
                styleable,
                obsType,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case String str -> new Attribute<>(
                property,
                str,
                field,
                styleable,
                obsType,
                DisplayHint.TEXT,
                ValueState.AUTO
            );
            // object properties
            case Enum<?> enumeration -> new Attribute<>(
                property,
                enumeration,
                field,
                styleable,
                obsType,
                DisplayHint.ENUM,
                ValueState.AUTO,
                List.of(enumeration.getClass().getEnumConstants())
            );
            case Color color -> new Attribute<>(
                property,
                color,
                field,
                styleable,
                obsType,
                DisplayHint.COLOR,
                ValueState.AUTO
            );
            case Image image -> new Attribute<>(
                property,
                image,
                field,
                styleable,
                obsType,
                DisplayHint.IMAGE,
                ValueState.AUTO
            );
            case Background background -> new Attribute<>(
                property,
                background,
                field,
                styleable,
                obsType,
                DisplayHint.BACKGROUND,
                ValueState.AUTO
            );
            case Border border -> new Attribute<>(
                property,
                border,
                field,
                styleable,
                obsType,
                DisplayHint.BORDER,
                ValueState.AUTO
            );
            case Tooltip tooltip -> new Attribute<>(
                property,
                tooltip.getText(),
                field,
                styleable,
                obsType,
                DisplayHint.TEXT,
                ValueState.AUTO
            );
            // the remainder is ObjectProperty<?>
            case null, default -> new Attribute<>(
                property,
                String.valueOf(value),
                field,
                styleable,
                obsType,
                DisplayHint.OBJECT,
                ValueState.AUTO
            );
        };
    }
}
