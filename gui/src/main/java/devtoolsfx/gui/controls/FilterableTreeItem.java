package devtoolsfx.gui.controls;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@NullMarked
public final class FilterableTreeItem<T> extends TreeItem<T> {

    private final ObservableList<TreeItem<T>> sourceList = FXCollections.observableArrayList();
    private final FilteredList<TreeItem<T>> filteredList = new FilteredList<>(sourceList);

    public FilterableTreeItem() {
        this(null);
    }

    public FilterableTreeItem(@Nullable T value) {
        super(value);
        Bindings.bindContent(getChildren(), filteredList);
    }

    public void setItems(List<TreeItem<T>> items) {
        sourceList.setAll(items);
    }

    public List<TreeItem<T>> getChildrenUnmodifiable() {
        return Collections.unmodifiableList(sourceList);
    }

    public boolean isEmpty() {
        return getChildren().isEmpty();
    }

    public void clear() {
        sourceList.clear();
    }

    public void setFilterPredicate(@Nullable Predicate<? super TreeItem<T>> predicate) {
        filteredList.setPredicate(predicate);
    }
}
