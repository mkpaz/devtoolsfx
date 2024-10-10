package devtoolsfx.gui.eventlog;

import devtoolsfx.connector.ConnectorOptions;
import devtoolsfx.event.ConnectorEvent;
import devtoolsfx.event.JavaFXEvent;
import devtoolsfx.event.MousePosEvent;
import devtoolsfx.event.WindowPropertiesEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jspecify.annotations.NullMarked;

import java.util.*;

@NullMarked
final class OptionsMenuButton extends MenuButton {

    private static final Set<Class<? extends ConnectorEvent>> PREDISABLED_EVENTS = Set.of(
        JavaFXEvent.class,
        MousePosEvent.class,
        WindowPropertiesEvent.class
    );

    private final CheckMenuItem selectedOnlyItem = new CheckMenuItem("For selected node only");
    private final Map<Class<?>, CheckMenuItem> eventItems = new HashMap<>();

    OptionsMenuButton(EventHandler<ActionEvent> actionHandler) {
        super("Options");

        Objects.requireNonNull(actionHandler, "action handler must not be null");

        setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "eventLogOptionsMenu");
        createMenuItems(actionHandler);
    }

    boolean isFilterSelectedOnly() {
        return selectedOnlyItem.isSelected();
    }

    <T extends ConnectorEvent> boolean isEventEnabled(T event) {
        var item = eventItems.get(event.getClass());
        return item != null && item.isSelected();
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createMenuItems(EventHandler<ActionEvent> actionHandler) {
        selectedOnlyItem.setOnAction(actionHandler);

        getItems().addAll(
            selectedOnlyItem,
            new SeparatorMenuItem()
        );

        Arrays.stream(ConnectorEvent.class.getPermittedSubclasses())
            .sorted(Comparator.comparing(Class::getSimpleName))
            .forEach(cls -> getItems().add(createEventMenuItem(cls, actionHandler, eventItems)));

        var selectAll = new MenuItem("Select all events");
        selectAll.setOnAction(e -> {
            eventItems.values().forEach(item -> item.setSelected(true));
            actionHandler.handle(e);
        });

        var deselectAll = new MenuItem("Deselect all events");
        deselectAll.setOnAction(e -> {
            eventItems.values().forEach(item -> item.setSelected(false));
            actionHandler.handle(e);
        });

        getItems().addAll(
            new SeparatorMenuItem(),
            selectAll,
            deselectAll
        );
    }

    private MenuItem createEventMenuItem(Class<?> cls,
                                         EventHandler<ActionEvent> actionHandler,
                                         Map<Class<?>, CheckMenuItem> registry) {
        var item = new CheckMenuItem(cls.getSimpleName());
        item.setUserData(cls);
        item.setOnAction(actionHandler);
        item.setSelected(!PREDISABLED_EVENTS.contains(cls));

        registry.put(cls, item);

        return item;
    }
}
