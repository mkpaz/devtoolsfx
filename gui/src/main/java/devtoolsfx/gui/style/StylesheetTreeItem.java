package devtoolsfx.gui.style;

import javafx.scene.control.TreeItem;
import org.jspecify.annotations.Nullable;

final class StylesheetTreeItem extends TreeItem<String> {

    private final int uid;
    private final @Nullable Stylesheet stylesheet;

    public StylesheetTreeItem(int uid, String value, @Nullable Stylesheet stylesheet) {
        super(value);
        this.uid = uid;
        this.stylesheet = stylesheet;
    }

    int getUid() {
        return uid;
    }

    @Nullable Stylesheet getStylesheet() {
        return stylesheet;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static StylesheetTreeItem of(int uid, String name) {
        return new StylesheetTreeItem(uid, name, null);
    }

    public static StylesheetTreeItem of(int uid, String uri, boolean isUserAgentStylesheet) {
        return new StylesheetTreeItem(uid, uri, new Stylesheet(uri, isUserAgentStylesheet));
    }
}
