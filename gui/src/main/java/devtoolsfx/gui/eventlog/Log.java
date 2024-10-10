package devtoolsfx.gui.eventlog;

import devtoolsfx.event.ConnectorEvent;
import devtoolsfx.event.ElementEvent;
import devtoolsfx.scenegraph.Element;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Predicate;

@NullMarked
final class Log {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final int UNLIMITED = -1;

    private final ObservableList<Entry> sourceList = FXCollections.observableList(new LinkedList<>());
    private final FilteredList<Entry> filteredList = new FilteredList<>(sourceList);
    private final BooleanBinding emptyProperty = Bindings.size(sourceList).isEqualTo(0);

    private int maxSize = UNLIMITED;

    public Log(int maxSize) {
        setMaxSize(maxSize);
    }

    void add(Entry entry) {
        if (sourceList.size() == maxSize) {
            sourceList.removeLast();
        }
        sourceList.addFirst(entry);
    }

    ObservableList<Entry> getEntries() {
        return sourceList;
    }

    ObservableList<Entry> getFilteredEntries() {
        return filteredList;
    }

    void setMaxSize(int maxSize) {
        this.maxSize = maxSize <= 0 ? UNLIMITED : maxSize;
    }

    BooleanBinding emptyProperty() {
        return emptyProperty;
    }

    void clear() {
        sourceList.clear();
    }

    void setFilterPredicate(@Nullable Predicate<Entry> predicate) {
        filteredList.setPredicate(predicate);
    }

    ///////////////////////////////////////////////////////////////////////////

    public record Entry(LocalDateTime timestamp, ConnectorEvent event) {

        public boolean matches(Element element) {
            return event instanceof ElementEvent elementEvent && Objects.equals(elementEvent.getElement(), element);
        }

        public boolean matches(String text) {
            return toLogString().contains(text);
        }

        public String toLogString() {
            String date = DATE_FORMAT.format(timestamp());
            String eventClass = String.format("%-24s", event().getClass().getSimpleName() + ":");
            return date + "  " + eventClass + event().toLogString();
        }

        public static Entry of(ConnectorEvent event) {
            return new Entry(LocalDateTime.now(), event);
        }
    }
}
