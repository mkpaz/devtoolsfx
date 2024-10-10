package devtoolsfx.connector;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;

/**
 * Represents a simple key-value pair of strings.
 */
@NullMarked
public record KeyValue(String key, @Nullable String value) implements Comparable<KeyValue> {

    public static final Comparator<KeyValue> COMPARATOR = Comparator.comparing(KeyValue::key);

    @Override
    public boolean equals(Object target) {
        if (this == target) return true;
        if (!(target instanceof KeyValue keyValue)) return false;

        return key.equals(keyValue.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public static KeyValue of(Map.Entry<?, ?> entry) {
        return new KeyValue(
            String.valueOf(entry.getKey()),
            String.valueOf(entry.getValue())
        );
    }

    @Override
    public int compareTo(KeyValue other) {
        return COMPARATOR.compare(this, other);
    }
}
