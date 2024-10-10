package devtoolsfx.gui.inspector;

import devtoolsfx.scenegraph.attributes.Attribute;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TreeItem;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@NullMarked
@SuppressWarnings("FieldCanBeLocal")
final class AttributeTreeItem extends TreeItem<AttributeCellContent> {

    private final ObservableList<AttributeTreeItem> sourceList = FXCollections.observableArrayList();
    private final FilteredList<AttributeTreeItem> filteredList = new FilteredList<>(sourceList);
    private final SortedList<AttributeTreeItem> sortedList = new SortedList<>(
        filteredList, Comparator.comparing(TreeItem::getValue)
    );

    AttributeTreeItem(AttributeCellContent value) {
        super(value);
        Bindings.bindContent(getChildren(), sortedList);
    }

    void addGroup(AttributeTreeItem group) {
        sourceList.add(group);
    }

    boolean isGroupOf(AttributeCategory category) {
        return getValue().getCategory() != null && getValue().getCategory() == category;
    }

    void setAttributes(List<Attribute<?>> attributes) {
        List<AttributeTreeItem> items = attributes.stream()
            .map(AttributeCellContent::forValue)
            .map(AttributeTreeItem::new)
            .toList();
        sourceList.setAll(items);
    }

    void updateAttribute(Attribute<?> attribute) {
        int index = -1;
        for (int i = 0; i < sourceList.size(); i++) {
            AttributeCellContent content = sourceList.get(i).getValue();
            if (content.getAttribute() != null && Objects.equals(content.getAttribute().name(), attribute.name())) {
                index = i;
                break;
            }
        }

        if (index > 0) {
            sourceList.set(index, new AttributeTreeItem(AttributeCellContent.forValue(attribute)));
        }
    }

    List<AttributeTreeItem> getChildrenUnmodifiable() {
        return Collections.unmodifiableList(sourceList);
    }

    void clear() {
        sourceList.clear();
    }

    boolean isEmpty() {
        return getChildren().isEmpty();
    }

    void setFilterText(@Nullable String filter) {
        if (filter == null) {
            setFilterPredicate(null);
        } else {
            setFilterPredicate(item -> item.getValue().matches(filter));
        }
    }

    void setFilterPredicate(@Nullable Predicate<? super AttributeTreeItem> predicate) {
        filteredList.setPredicate(predicate);
    }
}
