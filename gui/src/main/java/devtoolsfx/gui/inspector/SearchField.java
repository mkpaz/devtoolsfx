package devtoolsfx.gui.inspector;

import devtoolsfx.gui.controls.TextInputContextMenu;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@NullMarked
final class SearchField<T> extends HBox {

    private final TextField textField = new TextField();
    private final HBox controlsBox = new HBox();
    private final Text hintText = new Text();
    private final Button clearButton = new Button("clear");
    private final Button upButton = new Button();
    private final Button downButton = new Button();

    private List<T> navigableResult = List.of();
    private int position = -1;
    private @Nullable BiConsumer<Integer, T> navigationHandler;

    SearchField() {
        super();

        setId("scene-graph-search-field");

        createLayout();
        initListeners();
    }

    String getText() {
        return textField.getText() != null ? textField.getText().trim() : "";
    }

    @SuppressWarnings("SameParameterValue")
    void setText(@Nullable String text) {
        textField.setText(text != null ? text.trim() : "");
    }

    void setOnTextChange(@Nullable Runnable handler) {
        if (handler != null) {
            textField.setOnKeyReleased(event -> handler.run());
        }
    }

    void setOnClearButtonClick(@Nullable Runnable handler) {
        if (handler != null) {
            clearButton.setOnMousePressed(event -> handler.run());
        }
    }

    void setNavigableResult(@Nullable List<T> result) {
        this.navigableResult = Objects.requireNonNullElse(result, List.of());

        controlsBox.setVisible(!isFieldClear());
        controlsBox.setManaged(!isFieldClear());

        position = -1; // ready to be incremented
        upButton.setDisable(navigableResult.size() <= 1);
        downButton.setDisable(navigableResult.size() <= 1);

        updateHintText();
    }

    void setNavigationHandler(@Nullable BiConsumer<Integer, T> navigationHandler) {
        this.navigationHandler = navigationHandler;
    }

    void navigatePrevious() {
        if (navigationHandler != null) {
            position = position - 1;
            if (position < 0) {
                position = navigableResult.size() - 1;
            }

            navigationHandler.accept(position, navigableResult.get(position));
            updateHintText();
        }
    }

    void navigateNext() {
        if (navigationHandler != null) {
            position = position + 1;
            if (position > navigableResult.size() - 1) {
                position = 0;
            }

            navigationHandler.accept(position, navigableResult.get(position));
            updateHintText();
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        Supplier<Pane> iconGenerator = () -> {
            var pane = new StackPane();
            pane.getStyleClass().add("icon");
            return pane;
        };

        textField.setPromptText("id or styleClass or nodeName");
        textField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setContextMenu(new TextInputContextMenu(textField));

        clearButton.setGraphic(iconGenerator.get());
        clearButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        clearButton.getStyleClass().add("clear-button");

        hintText.getStyleClass().add("hint");

        upButton.setGraphic(iconGenerator.get());
        upButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        upButton.getStyleClass().addAll("arrow-button", "up-button");

        downButton.setGraphic(iconGenerator.get());
        downButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        downButton.getStyleClass().addAll("arrow-button", "down-button");

        controlsBox.getStyleClass().add("controls");
        controlsBox.getChildren().setAll(clearButton, hintText, upButton, downButton);
        controlsBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(controlsBox, Priority.NEVER);
        controlsBox.setVisible(false);
        controlsBox.setManaged(false);

        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(textField, controlsBox);
    }

    private void initListeners() {
        upButton.setOnAction(e -> navigatePrevious());
        downButton.setOnAction(e -> navigateNext());
        clearButton.disableProperty().bind(textField.textProperty().isEmpty());
    }

    private void updateHintText() {
        if (isFieldClear()) {
            hintText.setText("");
            return;
        }

        hintText.setText(String.format("%d of %d", position + 1, navigableResult.size()));
    }

    private boolean isFieldClear() {
        return (textField.getText() == null || textField.getText().isBlank()) && navigableResult.isEmpty();
    }
}
