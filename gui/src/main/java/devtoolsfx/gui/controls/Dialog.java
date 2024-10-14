package devtoolsfx.gui.controls;

import devtoolsfx.gui.GUI;
import javafx.css.PseudoClass;
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

    private static final PseudoClass DARK_MODE = PseudoClass.getPseudoClass("dark");
    private static final double DEFAULT_WIDTH = 640;
    private static final double DEFAULT_HEIGHT = 480;

    private final P root;

    public Dialog(P root, boolean darkMode) {
        this(root, "", DEFAULT_WIDTH, DEFAULT_HEIGHT, darkMode);
    }

    public Dialog(P root,
                  String title,
                  double width,
                  double height,
                  boolean darkMode) {
        super();

        this.root = Objects.requireNonNull(root, "parent node cannot be null");
        createLayout(root, title, width, height, darkMode);
    }

    public P getRoot() {
        return root;
    }

    public void toggleDarkMode(boolean darkMode) {
        getRoot().pseudoClassStateChanged(DARK_MODE, darkMode);
    }

    private void createLayout(P parent, String title, double width, double height, boolean darkMode) {
        setTitle(title);
        initModality(Modality.NONE);

        var scene = new Scene(parent);
        scene.setUserAgentStylesheet(GUI.USER_AGENT_STYLESHEET);
        toggleDarkMode(darkMode);

        setWidth(width);
        setHeight(height);
        setScene(scene);
    }
}
