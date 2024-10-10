package devtoolsfx.gui.controls;

import devtoolsfx.connector.ConnectorOptions;
import javafx.scene.control.*;

import java.util.function.Consumer;

/**
 * There is no API to modify the default text input context menu, so this serves as a replacement.
 * However, it still lacks internationalization (i18n) support because it is not public.
 */
public class TextInputContextMenu extends ContextMenu {

    public TextInputContextMenu(TextInputControl control) {
        super();

        setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "textInputContextMenu");
        createMenu(control);
    }

    private void createMenu(TextInputControl control) {
        var undo = menuItem("Undo", control, TextInputControl::undo);
        undo.setDisable(true);

        var redo = menuItem("Redo", control, TextInputControl::redo);
        redo.setDisable(true);

        var cut = menuItem("Cut", control, TextInputControl::cut);
        cut.setDisable(true);

        var copy = menuItem("Copy", control, TextInputControl::copy);
        copy.setDisable(true);

        var paste = menuItem("Paste", control, TextInputControl::paste);

        var selectAll = menuItem("Select All", control, TextInputControl::selectAll);
        selectAll.setDisable(true);

        var delete = menuItem("Delete", control, this::deleteSelectedText);
        delete.setDisable(true);

        control.undoableProperty().addListener((obs, old, val) -> undo.setDisable(!val));
        control.redoableProperty().addListener((obs, old, val) -> redo.setDisable(!val));
        control.selectionProperty().addListener((obs, old, val) -> {
            cut.setDisable(val.getLength() == 0);
            copy.setDisable(val.getLength() == 0);
            delete.setDisable(val.getLength() == 0);
            selectAll.setDisable(val.getLength() == val.getEnd());
        });

        getItems().setAll(undo, redo, cut, copy, paste, delete, new SeparatorMenuItem(), selectAll);
    }

    protected MenuItem menuItem(String text, TextInputControl control, Consumer<TextInputControl> action) {
        var item = new MenuItem(text);
        item.setOnAction(e -> action.accept(control));
        return item;
    }

    protected void deleteSelectedText(TextInputControl control) {
        IndexRange range = control.getSelection();
        if (range.getLength() == 0) {
            return;
        }

        String text = control.getText();
        String newText = text.substring(0, range.getStart()) + text.substring(range.getEnd());

        control.setText(newText);
        control.positionCaret(range.getStart());
    }
}
