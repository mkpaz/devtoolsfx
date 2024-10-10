package devtoolsfx.gui.controls;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FilterField extends HBox {

    private final TextField textField = new TextField();
    private final Button clearButton = new Button();

    public FilterField() {
        super();

        createLayout();
        initListeners();
    }

    public String getText() {
        return textField.getText() != null ? textField.getText().trim() : "";
    }

    public void setText(@Nullable String text) {
        textField.setText(text != null ? text.trim() : "");
    }

    public void setPromptText(String text) {
        textField.setPromptText(text);
    }

    public void setOnTextChange(@Nullable Runnable handler) {
        if (handler != null) {
            textField.setOnKeyReleased(event -> handler.run());
        }
    }

    public void setOnClearButtonClick(@Nullable Runnable handler) {
        if (handler != null) {
            clearButton.setOnMousePressed(event -> handler.run());
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        textField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setContextMenu(new TextInputContextMenu(textField));

        var icon = new StackPane();
        icon.getStyleClass().add("icon");
        clearButton.setGraphic(icon);

        clearButton.getStyleClass().add("clear-button");
        clearButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        HBox.setHgrow(clearButton, Priority.NEVER);
        clearButton.setVisible(false);
        clearButton.setManaged(false);

        getStyleClass().add("filter-field");
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(textField, clearButton);
    }

    private void initListeners() {
        clearButton.visibleProperty().bind(textField.textProperty().isEmpty().not());
        clearButton.managedProperty().bind(textField.textProperty().isEmpty());
    }
}
