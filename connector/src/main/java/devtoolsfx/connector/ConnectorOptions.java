package devtoolsfx.connector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.PopupWindow;
import org.jspecify.annotations.NullMarked;

/**
 * Contains all the supported {@link Connector} options.
 * Every option is observable, so they are applied at runtime immediately.
 */
@NullMarked
public final class ConnectorOptions {

    public static final String AUX_NODE_ID_PREFIX = "devtoolsfx.";

    private final BooleanProperty ignoreMouseTransparent = new SimpleBooleanProperty(false);
    private final BooleanProperty inspectMode = new SimpleBooleanProperty(false);
    private final BooleanProperty preventPopupAutoHide = new SimpleBooleanProperty(false);

    public ConnectorOptions() {
        // pass
    }

    /**
     * Enables the option to ignore mouse-transparent nodes when hovering,
     * e.g., in inspect mode.
     */
    BooleanProperty ignoreMouseTransparentProperty() {
        return ignoreMouseTransparent;
    }

    public boolean isIgnoreMouseTransparent() {
        return ignoreMouseTransparent.get();
    }

    public void setIgnoreMouseTransparent(boolean ignoreMouseTransparent) {
        this.ignoreMouseTransparent.set(ignoreMouseTransparent);
    }

    /**
     * Toggles the connector inspect mode. When enabled, the connector will display
     * the {@link InspectPane} containing short information above any hovered node.
     */
    BooleanProperty inspectModeProperty() {
        return inspectMode;
    }

    public boolean isInspectMode() {
        return inspectMode.get();
    }

    public void setInspectMode(boolean inspectMode) {
        this.inspectMode.set(inspectMode);
    }

    /**
     * Disables the {@link PopupWindow#autoHideProperty()} when the popup window appears.
     * This allows inspection of the popup window content without accidentally hiding the window.
     */
    BooleanProperty preventPopupAutoHideProperty() {
        return preventPopupAutoHide;
    }

    public boolean isPreventPopupAutoHide() {
        return preventPopupAutoHide.get();
    }

    public void setPreventPopupAutoHide(boolean preventPopupAutoHide) {
        this.preventPopupAutoHide.set(preventPopupAutoHide);
    }
}
