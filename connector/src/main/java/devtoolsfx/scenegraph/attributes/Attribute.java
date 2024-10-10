package devtoolsfx.scenegraph.attributes;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates summary information about an observable object property or field.
 *
 * @param name           the unique attribute name (label)
 * @param value          the value of a property, field, or any other payload
 * @param field          the observable property or field name, can be null
 * @param cssProperty    the corresponding CSS property name, if styleable
 * @param observableType the type of the observable property
 * @param displayHint    a hint to assist in displaying the property value
 * @param valueState     whether the property value has been changed
 * @param validValues    the set (or range) of valid property values
 */
@NullMarked
public record Attribute<V>(
    String name,
    @Nullable V value,
    @Nullable String field,
    @Nullable String cssProperty,
    ObservableType observableType,
    DisplayHint displayHint,
    ValueState valueState,
    List<V> validValues) {

    public Attribute(String name,
                     @Nullable V value,
                     @Nullable String field,
                     ObservableType observableType,
                     DisplayHint displayHint,
                     ValueState valueState) {
        this(name, value, field, null, observableType, displayHint, valueState, List.of());
    }

    public Attribute(String name,
                     @Nullable V value,
                     @Nullable String field,
                     @Nullable String cssProperty,
                     ObservableType observableType,
                     DisplayHint displayHint,
                     ValueState valueState) {
        this(name, value, field, cssProperty, observableType, displayHint, valueState, List.of());
    }

    @Override
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }
        if (target == null || getClass() != target.getClass()) {
            return false;
        }

        Attribute<?> attribute = (Attribute<?>) target;
        return name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String toLogString() {
        return "Attribute:"
            + " name=" + name
            + " value=" + value
            + " field=" + field
            + " valueState=" + valueState;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides a hint for displaying the attribute value.
     */
    public enum DisplayHint {

        BACKGROUND(Background.class),
        BOOLEAN(Boolean.class),
        BORDER(Border.class),
        BOUNDS(Bounds.class),
        CLIP(Clip.class),
        COLOR(Color.class),
        COLUMN_CONSTRAINTS(ColumnConstraints.class),
        EFFECT(Effect.class),
        ENUM(Enum.class),
        FONT(Font.class),
        IMAGE(Image.class),
        INSETS(Insets.class),
        NUMERIC(Double.class),
        OBJECT(Object.class),
        PROPERTIES(Map.class),
        ROW_CONSTRAINTS(RowConstraints.class),
        TEXT(String.class),
        TRANSFORMS(Transform.class);

        private final Class<?> valueType;

        DisplayHint(Class<?> valueType) {
            this.valueType = valueType;
        }

        public Class<?> valueType() {
            return valueType;
        }
    }

    /**
     * Represents the observable type of property.
     */
    public enum ObservableType {
        READ_WRITE,
        READ_ONLY,
        BOUND,
        LIST,
        SET,
        NOT_OBSERVABLE;

        public static <T extends ObservableValue<?>> ObservableType of(T prop) {
            if (prop instanceof Property<?> p && p.isBound()) {
                return ObservableType.BOUND;
            }

            if (prop.getClass().getSimpleName().startsWith("ReadOnly")) {
                return READ_ONLY;
            }

            return READ_WRITE;
        }
    }

    /**
     * Represents the state of an attribute value.
     * <li>DEFAULT - indicates that the attribute value is in its default state</li>
     * <li>CHANGED - indicates that the attribute value has been modified</li>
     * <li>AUTO - indicates that the attribute value is auto-computed or constant</li>
     */
    public enum ValueState {
        DEFAULT,
        CHANGED,
        AUTO;

        public static ValueState defaultIf(boolean state) {
            return state ? DEFAULT : CHANGED;
        }
    }
}
