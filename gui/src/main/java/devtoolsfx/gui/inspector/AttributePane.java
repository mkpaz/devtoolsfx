package devtoolsfx.gui.inspector;

import devtoolsfx.gui.ToolPane;
import devtoolsfx.gui.controls.FilterField;
import devtoolsfx.scenegraph.attributes.Attribute;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
final class AttributePane extends SplitPane {

    private static final int MIN_FILTER_LENGTH = 3;

    private final ToolPane toolPane;
    private final AttributeTreeTable table = new AttributeTreeTable();
    private final FilterField filterField = new FilterField();
    private final AttributeDetailsPane details = new AttributeDetailsPane(this);

    AttributePane(ToolPane toolPane) {
        super();

        this.toolPane = toolPane;

        createLayout();
        initListeners();
    }

    void setAttributes(AttributeCategory category, List<Attribute<?>> attributes) {
        table.setAttributes(category, attributes);
    }

    void updateAttribute(AttributeCategory category, Attribute<?> attribute) {
        table.updateAttribute(category, attribute);
    }

    void clearAttributes() {
        table.clear();
    }

    void setFilter(@Nullable String filter) {
        table.setFilter(filter);
        if (filter == null || filter.isBlank()) {
            filterField.setText("");
        }
    }

    ToolPane getToolPane() {
        return toolPane;
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        filterField.setPromptText("property name");
        HBox.setHgrow(filterField, Priority.ALWAYS);
        // prevents field from changing height on SplitPane resize
        filterField.setMinHeight(Region.USE_PREF_SIZE);
        filterField.setMaxHeight(Region.USE_PREF_SIZE);

        var filterBox = new HBox(filterField);
        filterBox.getStyleClass().add("filter");
        filterBox.setFillHeight(true);
        VBox.setVgrow(filterBox, Priority.NEVER);

        var tableBox = new VBox(table, filterBox);
        tableBox.getStyleClass().add("table-box");
        VBox.setVgrow(table, Priority.ALWAYS);

        setId("attribute-pane");
        getItems().setAll(tableBox);
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(1);
        setResizableWithParent(details, false);
    }

    private void initListeners() {
        filterField.setOnKeyReleased(event -> {
            var text = filterField.getText();
            if (text.isEmpty() || text.length() >= MIN_FILTER_LENGTH) {
                table.setFilter(text);
            }
        });

        filterField.setOnClearButtonClick(() -> setFilter(null));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            details.setContent(val != null ? val.getValue() : null);
            if (val != null) {
                if (!getItems().contains(details)) {
                    getItems().add(details);
                    setDividerPositions(0.8);
                }
            } else {
                getItems().remove(details);
                setDividerPositions(1.0);
            }
        });

        table.setRefreshHandler(() -> {
            String filter = table.getFilter();

            toolPane.getConnector().reloadSelectedAttributes(null, null);

            if (filter != null) {
                table.setFilter(filter);
            }
        });
    }
}
