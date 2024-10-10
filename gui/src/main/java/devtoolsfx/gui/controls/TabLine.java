package devtoolsfx.gui.controls;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

@NullMarked
public final class TabLine extends HBox {

    private @Nullable Consumer<String> selectionHandler;

    private final ToggleGroup group = new ToggleGroup();
    private final ChangeListener<@Nullable Toggle> selectionListener = (obs, old, val) -> {
        if (val == null && old != null) {
            old.setSelected(true);
            return;
        }

        if (selectionHandler != null && val != null && val.getUserData() instanceof String tab) {
            selectionHandler.accept(tab);
        }
    };

    public TabLine(String... tabs) {
        super();

        getStyleClass().add("tab-line");
        createLayout(tabs);
    }

    public void selectTab(String tab) {
        group.getToggles().stream()
            .filter(t -> Objects.equals(t.getUserData(), tab) && !t.isSelected())
            .findFirst()
            .ifPresent(t -> t.setSelected(true));
    }

    public void setOnTabSelect(@Nullable Consumer<String> selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout(String... tabs) {
        var buttons = new ArrayList<ToggleButton>(tabs.length);
        for (var tab : tabs) {
            var button = new ToggleButton(tab);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setUserData(tab);
            HBox.setHgrow(button, Priority.ALWAYS);

            buttons.add(button);
        }

        group.getToggles().setAll(buttons);
        group.selectedToggleProperty().addListener(selectionListener);

        getChildren().setAll(buttons);
    }
}
