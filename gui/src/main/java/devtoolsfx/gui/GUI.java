package devtoolsfx.gui;

import devtoolsfx.connector.LocalConnector;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Objects;

/**
 * The entry point for launching the dev tools GUI application.
 */
@NullMarked
public final class GUI {

    public static final String USER_AGENT_STYLESHEET = getResource("/index.css").toString();

    public static final double DEFAULT_STAGE_WIDTH = 1024;
    public static final double DEFAULT_STAGE_HEIGHT = 768;

    /**
     * See @{@link #openToolStage(Stage, Preferences, String)}.
     */
    public static void openToolStage(Stage primaryStage, HostServices hostServices) {
        openToolStage(primaryStage, hostServices, null);
    }

    /**
     * See @{@link #openToolStage(Stage, Preferences, String)}.
     */
    public static void openToolStage(Stage primaryStage,
                                     HostServices hostServices,
                                     @Nullable String applicationName) {
        openToolStage(primaryStage, new Preferences(hostServices), applicationName);
    }

    /**
     * Starts the GUI in a separate window.
     *
     * @param primaryStage    the primary stage of the monitored application
     * @param preferences     the initial GUI preferences
     * @param applicationName the name of the monitored application
     */
    public static void openToolStage(Stage primaryStage,
                                     Preferences preferences,
                                     @Nullable String applicationName) {
        Objects.requireNonNull(primaryStage, "primaryStage can not be null");
        Objects.requireNonNull(preferences, "hostServices can not be null");

        var toolPane = createToolPane(primaryStage, preferences, applicationName);
        var scene = new Scene(toolPane, DEFAULT_STAGE_WIDTH, DEFAULT_STAGE_HEIGHT);
        scene.setUserAgentStylesheet(USER_AGENT_STYLESHEET);

        var toolStage = new Stage();
        toolStage.setScene(scene);
        toolStage.setTitle("devtoolsfx");
        toolStage.setOnShown(e -> toolPane.getConnector().start());

        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> toolStage.close());
        toolStage.show();
    }

    /**
     * See @{@link #createToolPane(Stage, Preferences, String)}.
     */
    public static ToolPane createToolPane(Stage primaryStage, HostServices hostServices) {
        return createToolPane(primaryStage, hostServices, null);
    }

    /**
     * See @{@link #createToolPane(Stage, Preferences, String)}.
     */
    public static ToolPane createToolPane(Stage primaryStage,
                                          HostServices hostServices,
                                          @Nullable String applicationName) {
        return createToolPane(primaryStage, new Preferences(hostServices), applicationName);
    }

    /**
     * Creates the GUI tool pane to be displayed as embedded within the application window.
     * <p>
     * The ToolPane requires its own independent user agent stylesheet. Use a Scene or SubScene to
     * display it correctly.
     *
     * @param primaryStage    the primary stage of the monitored application
     * @param preferences     the initial GUI preferences
     * @param applicationName the name of the monitored application
     */
    public static ToolPane createToolPane(Stage primaryStage,
                                          Preferences preferences,
                                          @Nullable String applicationName) {
        Objects.requireNonNull(primaryStage, "primaryStage can not be null");
        Objects.requireNonNull(preferences, "preferences can not be null");

        var connector = new LocalConnector(primaryStage, applicationName);
        return new ToolPane(connector, preferences);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static URL getResource(String path) {
        return Objects.requireNonNull(GUI.class.getResource(path));
    }
}
