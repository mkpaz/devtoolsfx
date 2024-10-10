package devtoolsfx.scenegraph;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Represents a selective set of window properties.
 *
 * @param windowType       the type of the window
 * @param sceneStylesheets the list of stylesheets for the scene
 * @param isPrimaryStage   whether the window is the primary stage
 * @param windowTitle      the title of the window
 * @param ownerClassName   the class name of the window owner's node
 */
@NullMarked
public record WindowProperties(WindowType windowType,
                               List<String> sceneStylesheets,
                               @Nullable String userAgentStylesheet,
                               boolean isPrimaryStage,
                               @Nullable String windowTitle,
                               @Nullable String ownerClassName) {

    public enum WindowType {
        STAGE, MODAL, POPUP, ALERT
    }

    public static WindowProperties of(Window window, boolean isPrimaryStage) {
        var type = WindowType.STAGE;
        String windowTitle = null;
        String ownerClassName = null;
        Scene scene = window.getScene();

        if (window instanceof PopupWindow popup) {
            type = WindowType.POPUP;
            if (popup.getOwnerNode() != null) {
                ownerClassName = popup.getOwnerNode().getClass().getSimpleName();
            }
        }

        if (window instanceof Stage stage) {
            if (stage.getModality() == Modality.WINDOW_MODAL || stage.getModality() == Modality.APPLICATION_MODAL) {
                type = WindowType.MODAL;
            }

            // alert can be modal or not modal
            if (scene != null && scene.getRoot() instanceof DialogPane) {
                type = WindowType.ALERT;
            }

            windowTitle = stage.getTitle();
        }

        List<String> stylesheets = window.getScene() != null
            ? Collections.unmodifiableList(window.getScene().getStylesheets())
            : List.of();

        String uas = null;
        if (scene != null && scene.getUserAgentStylesheet() != null && !scene.getUserAgentStylesheet().isEmpty()) {
            uas = scene.getUserAgentStylesheet();
        }

        return new WindowProperties(type, stylesheets, uas, isPrimaryStage, windowTitle, ownerClassName);
    }
}
