package devtoolsfx.gui.util;

import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@NullMarked
public final class GUIHelpers {

    /**
     * Sets system clipboard content.
     * Null value is ignored.
     */
    public static void setClipboard(@Nullable String s) {
        if (s == null) {
            return;
        }

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(s);
        clipboard.setContent(content);
    }

    /**
     * Copies selected items from the TreeTableView to the system clipboard.
     */
    public static <S> void copySelectedRowsToClipboard(TreeTableView<S> table,
                                                       Function<S, String> stringConverter) {
        if (table.getSelectionModel().isEmpty()) {
            return;
        }

        var sb = new StringBuilder();
        for (TreeItem<S> item : table.getSelectionModel().getSelectedItems()) {
            sb.append(stringConverter.apply(item.getValue()));
            sb.append('\n');
        }

        setClipboard(sb.toString());
    }

    /**
     * Copies selected items from the ListView to the system clipboard.
     */
    public static <S> void copySelectedRowsToClipboard(ListView<S> table,
                                                       Function<S, String> stringConverter) {
        if (table.getSelectionModel().isEmpty()) {
            return;
        }

        var sb = new StringBuilder();
        for (S value : table.getSelectionModel().getSelectedItems()) {
            sb.append(stringConverter.apply(value));
            sb.append('\n');
        }

        setClipboard(sb.toString());
    }
}
