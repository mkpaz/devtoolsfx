package devtoolsfx;

import devtoolsfx.gui.GUI;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Base64;
import java.util.Objects;
import java.util.Random;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javafx.scene.control.Alert.AlertType;

public class Launcher extends Application {

    static final String DATA_URI_PREFIX = "data:base64,";

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        var root = new VBox();

        var table = new TableView<>();
        table.getStylesheets().addAll(getResource("/demo.css"));

        var textArea = new TextArea();
        textArea.setVisible(false);
        textArea.getStylesheets().add(toDataURI(
            """
                .foo {
                  -fx-opacity: 1;
                }"""
        ));

        var infoDialogBtn = new Button("Info Dialog");
        infoDialogBtn.setOnAction(e -> {
            Alert alert = new Alert(AlertType.CONFIRMATION, "Test", ButtonType.YES, ButtonType.NO);
            alert.initModality(Modality.NONE);
            alert.showAndWait();
        });

        var testBtn = new Button("Test");
        testBtn.setOnAction(e -> {
            textArea.setVisible(!textArea.isVisible());
            table.getStyleClass().add("foo-" + new Random().nextLong(100));
        });

        root.getChildren().setAll(
            table,
            textArea,
            new HBox(10, infoDialogBtn, testBtn)
        );

        var scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getResource("/demo.css"));
        primaryStage.setScene(scene);
        primaryStage.setTitle("Demo");
        primaryStage.setOnShown(e -> GUI.openToolStage(primaryStage, getHostServices()));
        primaryStage.show();

        CSSFX.start();
    }

    static String getResource(String path) {
        return Objects.requireNonNull(Launcher.class.getResource(path)).toString();
    }

    static String toDataURI(String css) {
        if (css == null) {
            throw new NullPointerException("CSS string cannot be null!");
        }
        return DATA_URI_PREFIX + new String(Base64.getEncoder().encode(css.getBytes(UTF_8)), UTF_8);
    }
}
