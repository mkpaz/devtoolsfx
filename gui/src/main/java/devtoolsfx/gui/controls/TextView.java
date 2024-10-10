package devtoolsfx.gui.controls;

import devtoolsfx.connector.ConnectorOptions;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A component for displaying text (monospace by default).
 */
@NullMarked
public class TextView extends VBox {

    private final TextArea textArea = new TextArea();

    public TextView() {
        super();

        createLayout();
    }

    public void setText(@Nullable String text) {
        textArea.setText(text);
    }

    private void createLayout() {
        textArea.setEditable(false);
        textArea.setWrapText(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        getChildren().setAll(textArea);
        setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "textView");
        getStyleClass().add("text-view");
    }
}
