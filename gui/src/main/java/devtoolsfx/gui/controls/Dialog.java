package devtoolsfx.gui.controls;

import devtoolsfx.gui.GUI;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * A utility wrapper for modal dialogs.
 */
@NullMarked
public final class Dialog<P extends Parent> extends Stage {

    private static final double DEFAULT_WIDTH = 640;
    private static final double DEFAULT_HEIGHT = 480;

    private final P root;

    public Dialog(P root) {
        this(root, "", DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Dialog(P root, String title, double width, double height) {
        super();

        this.root = Objects.requireNonNull(root, "parent node cannot be null");
        createLayout(root, title, width, height);
    }

    public P getRoot() {
        return root;
    }

    private void createLayout(P parent, String title, double width, double height) {
        setTitle(title);
        initModality(Modality.NONE);

        var scene = new Scene(parent);
        scene.getStylesheets().add(GUI.USER_AGENT_STYLESHEET);

        setWidth(width);
        setHeight(height);
        setScene(scene);
    }
}
