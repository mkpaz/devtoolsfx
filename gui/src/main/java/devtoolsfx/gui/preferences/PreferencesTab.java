package devtoolsfx.gui.preferences;

import devtoolsfx.gui.Preferences;
import devtoolsfx.gui.ToolPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PreferencesTab extends VBox {

    public static final String TAB_NAME = "Preferences";

    private final ToolPane toolPane;

    public PreferencesTab(ToolPane toolPane) {
        super();

        this.toolPane = toolPane;

        createLayout();
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        getChildren().setAll(
            createSceneGraphGroup(),
            createInspectionGroup(),
            createEventLogGroup()
        );

        setId("preferences-tab");
        getStyleClass().setAll("tab");
    }

    private VBox createSceneGraphGroup() {
        var autoRefreshToggle = new CheckBox("Refresh automatically");
        autoRefreshToggle.selectedProperty().bindBidirectional(
            toolPane.getPreferences().autoRefreshSceneGraphProperty()
        );
        autoRefreshToggle.setDisable(true);

        var preventAutoHideToggle = new CheckBox("Prevent auto-hide for popup windows");
        preventAutoHideToggle.selectedProperty().bindBidirectional(
            toolPane.getPreferences().preventPopupAutoHideProperty()
        );

        var collapseControlsToggle = new CheckBox("Collapse controls");
        collapseControlsToggle.selectedProperty().bindBidirectional(
            toolPane.getPreferences().collapseControlsProperty()
        );

        var collapsePanesToggle = new CheckBox("Collapse panes");
        collapsePanesToggle.selectedProperty().bindBidirectional(
            toolPane.getPreferences().collapsePanesProperty()
        );

        var content = new FlowPane(
            autoRefreshToggle,
            preventAutoHideToggle,
            collapseControlsToggle,
            collapsePanesToggle
        );

        return createPreferencesGroup("Scene Graph", content);
    }

    private VBox createInspectionGroup() {
        var layoutBoundsToggle = new CheckBox("Highlight layout bounds");
        layoutBoundsToggle.selectedProperty().bindBidirectional(
            toolPane.getPreferences().showLayoutBoundsProperty()
        );

        var boundsInParentToggle = new CheckBox("Highlight bounds in parent");
        boundsInParentToggle.selectedProperty().bindBidirectional(
            toolPane.getPreferences().showBoundsInParentProperty()
        );

        var baselineToggle = new CheckBox("Highlight baseline offset");
        baselineToggle.selectedProperty().bindBidirectional(
            toolPane.getPreferences().showBaselineProperty()
        );

        var mouseTransparentToggle = new CheckBox("Ignore mouse transparent");
        mouseTransparentToggle.selectedProperty().bindBidirectional(
            toolPane.getPreferences().ignoreMouseTransparentProperty()
        );

        var content = new FlowPane(
            layoutBoundsToggle,
            boundsInParentToggle,
            baselineToggle,
            mouseTransparentToggle
        );

        return createPreferencesGroup("Inspection", content);
    }

    private VBox createEventLogGroup() {
        var maxSizeField = new TextField(String.valueOf(
            toolPane.getPreferences().getMaxEventLogSize()
        ));
        maxSizeField.textProperty().addListener(
            (obs, old, val) -> toolPane.getPreferences().setMaxEventLogSize(parseEventLogSize(val))
        );

        var maxSizeBox = new HBox(new Label("Max log size"), maxSizeField);
        maxSizeBox.setSpacing(8);
        maxSizeBox.setAlignment(Pos.CENTER_LEFT);

        var content = new FlowPane(
            maxSizeBox
        );

        return createPreferencesGroup("Event Log", content);
    }

    private VBox createPreferencesGroup(String name, Node content) {
        var header = new Label(name);
        header.getStyleClass().add("header");

        content.getStyleClass().add("content");

        var group = new VBox(header, content);
        group.getStyleClass().add("group");

        return group;
    }

    private int parseEventLogSize(@Nullable String text) {
        int nextVal = Preferences.DEFAULT_EVENT_LOG_SIZE;

        if (text != null && !text.isBlank()) {
            try {
                nextVal = Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
            }
        }

        if (nextVal < Preferences.MIN_EVENT_LOG_SIZE || nextVal > Preferences.MAX_EVENT_LOG_SIZE) {
            nextVal = Preferences.DEFAULT_EVENT_LOG_SIZE;
        }

        return nextVal;
    }
}
