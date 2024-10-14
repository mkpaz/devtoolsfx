package devtoolsfx.gui.eventlog;

import devtoolsfx.event.ConnectorEvent;
import devtoolsfx.gui.ToolPane;
import devtoolsfx.gui.controls.Dialog;
import devtoolsfx.gui.controls.FilterField;
import devtoolsfx.gui.controls.TextView;
import devtoolsfx.gui.util.Formatters;
import devtoolsfx.gui.util.GUIHelpers;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Supplier;

@NullMarked
public final class EventLogTab extends VBox {

    public static final String TAB_NAME = "Events";
    private static final PseudoClass STARTED = PseudoClass.getPseudoClass("started");
    private static final int MIN_FILTER_LENGTH = 3;
    private static final int MAX_NUMBER_OF_LINES = 3;

    private final ToolPane toolPane;
    private final Log log;

    private final ListView<Log.Entry> logView = new ListView<>();
    private final Button startStopButton = new Button();
    private final Button clearButton = new Button();
    private final Button exportButton = new Button();
    private final FilterField filterField = new FilterField();
    private final OptionsMenuButton optionsMenu = new OptionsMenuButton(e -> {
        updateFilter();
        updateStatusLabel();
    });
    private final Label statusLabel = new Label();
    private @Nullable Dialog<TextView> textViewDialog = null;

    public EventLogTab(ToolPane toolPane) {
        super();

        this.toolPane = toolPane;
        this.log = new Log(toolPane.getPreferences().getMaxEventLogSize());

        createLayout();
        initListeners();

        updateFilter();
        updateStatusLabel();
    }

    public void offer(ConnectorEvent event) {
        if (toolPane.getPreferences().isEnableEventLog()) {
            log.add(Log.Entry.of(event));
            updateStatusLabel();
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        Supplier<Pane> iconGenerator = () -> {
            var pane = new StackPane();
            pane.getStyleClass().add("icon");
            return pane;
        };

        startStopButton.setGraphic(iconGenerator.get());
        startStopButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        startStopButton.getStyleClass().add("start-stop-button");

        clearButton.setGraphic(iconGenerator.get());
        clearButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        clearButton.getStyleClass().add("clear-button");

        exportButton.setGraphic(iconGenerator.get());
        exportButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        exportButton.getStyleClass().add("export-button");

        filterField.setPromptText("filter");
        HBox.setHgrow(filterField, Priority.ALWAYS);
        filterField.setMinHeight(Region.USE_PREF_SIZE);
        filterField.setMaxHeight(Region.USE_PREF_SIZE);

        var controlsBox = new HBox();
        controlsBox.getStyleClass().add("controls");
        controlsBox.getChildren().setAll(startStopButton, clearButton, exportButton, filterField, optionsMenu);
        VBox.setVgrow(controlsBox, Priority.NEVER);

        logView.getStyleClass().add("log-view");
        logView.setItems(log.getFilteredEntries());
        logView.setCellFactory(c -> new ListCell<>() {
            @Override
            protected void updateItem(Log.Entry entry, boolean empty) {
                super.updateItem(entry, empty);
                setText(!empty
                    ? Formatters.limitNumberOfLines(entry.toLogString(), MAX_NUMBER_OF_LINES, "\n...")
                    : null
                );
            }
        });
        VBox.setVgrow(logView, Priority.ALWAYS);

        var statusBar = new HBox(statusLabel);
        statusBar.getStyleClass().add("status-bar");
        VBox.setVgrow(statusBar, Priority.NEVER);
        updateStatusLabel();

        setId("event-log-tab");
        getStyleClass().setAll("tab");
        getChildren().setAll(controlsBox, logView, statusBar);
    }

    private void initListeners() {
        startStopButton.setOnAction(e -> {
            boolean nextState = !toolPane.getPreferences().isEnableEventLog();
            toolPane.getPreferences().setEnableEventLog(nextState);
            startStopButton.pseudoClassStateChanged(STARTED, nextState);

            updateFilter();
            updateStatusLabel();
        });

        clearButton.setOnAction(e -> {
            log.clear();

            updateFilter();
            updateStatusLabel();
        });
        clearButton.disableProperty().bind(log.emptyProperty());

        exportButton.disableProperty().bind(log.emptyProperty());
        exportButton.setOnAction(event -> {
            var dialog = new FileChooser();
            dialog.setTitle("Save File");
            dialog.setInitialFileName("event-log.txt");
            dialog.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
            dialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            var file = dialog.showSaveDialog(exportButton.getScene().getWindow());
            if (file != null) {
                exportLog(file);
            }
        });

        filterField.setOnTextChange(() -> {
            updateFilter();
            updateStatusLabel();
        });
        filterField.setOnClearButtonClick(() -> filterField.setText(""));

        logView.setOnMouseClicked(event -> {
            if (MouseButton.PRIMARY.equals(event.getButton()) && event.getClickCount() == 2 && !logView.getSelectionModel().isEmpty()) {
                var entry = logView.getSelectionModel().getSelectedItem();
                var dialog = getOrCreateTextViewDialog();
                dialog.getRoot().setText(String.valueOf(entry));
                dialog.show();
                dialog.toFront();
            }
        });
        logView.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                GUIHelpers.copySelectedRowsToClipboard(logView, Log.Entry::toLogString);
            }
        });

        toolPane.getPreferences().maxEventLogSizeProperty().addListener(
            (obs, old, val) -> log.setMaxSize((int) val)
        );
    }

    @SuppressWarnings("RedundantIfStatement")
    private void updateFilter() {
        log.setFilterPredicate(entry -> {
            if (!optionsMenu.isEventEnabled(entry.event())) {
                return false;
            }

            if (optionsMenu.isFilterSelectedOnly() && (toolPane.getSelectedElement() == null
                || !entry.matches(toolPane.getSelectedElement()))) {
                return false;
            }

            var text = filterField.getText();
            if ((text.length() >= MIN_FILTER_LENGTH && !entry.matches(text))) {
                return false;
            }

            return true;
        });
    }

    private void updateStatusLabel() {
        int totalSize = log.getEntries().size();
        int filteredSize = log.getFilteredEntries().size();
        statusLabel.setText(filteredSize == totalSize
            ? totalSize + " entries"
            : filteredSize + "/" + totalSize + " entries"
        );
    }

    private void exportLog(File file) {
        var sb = new StringBuilder();
        for (var entry : new ArrayList<>(log.getFilteredEntries())) {
            sb.append(entry.toLogString());
            sb.append("\n");
        }

        try {
            Files.writeString(file.toPath(), sb.toString());
        } catch (IOException e) {
            toolPane.handleException(e);
        }
    }

    private Dialog<TextView> getOrCreateTextViewDialog() {
        if (textViewDialog == null) {
            textViewDialog = new Dialog<>(
                new TextView(),
                "Log Entry",
                640,
                480,
                toolPane.getPreferences().getDarkMode()
            );
        }

        return textViewDialog;
    }
}
