package devtoolsfx.gui.style;

import devtoolsfx.gui.controls.Dialog;
import devtoolsfx.gui.controls.TextView;
import devtoolsfx.gui.util.Formatters;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@NullMarked
final class StylesheetTreeCell extends TreeCell<String> {

    private static final PseudoClass UA_STYLESHEET = PseudoClass.getPseudoClass("user-agent");
    private static final int MAX_NUMBER_OF_LINES = 3;

    private final StackPane icon = new StackPane();

    public StylesheetTreeCell(Supplier<Dialog<TextView>> textViewDialog,
                              BiFunction<Integer, Stylesheet, @Nullable String> sourceCodeProvider) {
        super();

        icon.getStyleClass().add("icon");

        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY
                && e.getClickCount() == 2
                && getTreeItem() instanceof StylesheetTreeItem item
                && item.isLeaf()
                && item.getStylesheet() != null) {

                Dialog<TextView> dialog = textViewDialog.get();
                dialog.getRoot().setText(Objects.requireNonNullElse(
                    sourceCodeProvider.apply(item.getUid(), item.getStylesheet()),
                    "Unable to obtain the source code."
                ));
                dialog.show();
                dialog.toFront();
            }
        });
    }

    @Override
    protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
            pseudoClassStateChanged(UA_STYLESHEET, false);
            return;
        }

        String text = value;
        boolean isUserAgentStylesheet = false;

        if (getTreeItem() instanceof StylesheetTreeItem item && item.getStylesheet() != null) {
            Stylesheet stylesheet = item.getStylesheet();
            text = stylesheet.uri();

            if (item.getUid() == StylesheetTab.ROOT_UID) {
                text = StylesheetTab.ROOT_ITEM_NAME + " [" + text + "]";
            }

            if (stylesheet.isDataURI()) {
                text = Formatters.limitNumberOfLines(stylesheet.decodeFromDataURI(), MAX_NUMBER_OF_LINES, "\n...");
            }

            if (stylesheet.isUserAgentStylesheet()) {
                isUserAgentStylesheet = true;
                setGraphic(icon);
            } else {
                setGraphic(null);
            }
        }

        setText(text);
        pseudoClassStateChanged(UA_STYLESHEET, isUserAgentStylesheet);
    }
}
