package devtoolsfx.gui.inspector;

import devtoolsfx.gui.Preferences;
import devtoolsfx.scenegraph.attributes.Attribute;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static devtoolsfx.scenegraph.attributes.AttributeCategory.REFLECTIVE;

@NullMarked
final class AttributeCellContent implements Comparable<AttributeCellContent> {

    private final @Nullable AttributeCategory category;
    private final @Nullable Attribute<?> attribute;

    private AttributeCellContent(@Nullable AttributeCategory category,
                                 @Nullable Attribute<?> attribute) {
        this.category = category;
        this.attribute = attribute;
    }

    @Nullable
    AttributeCategory getCategory() {
        return category;
    }

    @Nullable
    Attribute<?> getAttribute() {
        return attribute;
    }

    boolean isRoot() {
        return category == null;
    }

    boolean isGroup() {
        return category != null && attribute == null;
    }

    boolean matches(@Nullable String filter) {
        return filter != null
            && attribute != null
            && attribute.name().toLowerCase().contains(filter.toLowerCase());
    }

    ///////////////////////////////////////////////////////////////////////////

    static AttributeCellContent forRoot() {
        return new AttributeCellContent(null, null);
    }

    static AttributeCellContent forGroup(AttributeCategory category) {
        Objects.requireNonNull(category, "category must be specified");
        return new AttributeCellContent(category, null);
    }

    static AttributeCellContent forValue(Attribute<?> attribute) {
        Objects.requireNonNull(attribute, "attribute must be specified");
        return new AttributeCellContent(null, attribute);
    }

    @Override
    public int compareTo(AttributeCellContent other) {
        // reflective/summary attributes group should be the last one
        if (isGroup() && other.isGroup() && other.getCategory() != null) {
            if (getCategory() == REFLECTIVE) {
                return 1;
            }
            return getCategory().compareTo(other.getCategory());
        }

        // all trackers (except reflective) sort attributes semantically,
        // we can either keep this sorting or reset it to alphabetical order
        if (getAttribute() != null && other.getAttribute() != null) {
            return Preferences.KEEP_ATTRIBUTES_SORT
                ? 0
                : getAttribute().name().compareTo(other.getAttribute().name());
        }

        return 0;
    }
}
