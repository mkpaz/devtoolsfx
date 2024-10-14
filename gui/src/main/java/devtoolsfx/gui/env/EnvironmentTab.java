package devtoolsfx.gui.env;

import devtoolsfx.connector.ConnectorOptions;
import devtoolsfx.connector.Env;
import devtoolsfx.connector.KeyValue;
import devtoolsfx.gui.ToolPane;
import devtoolsfx.gui.controls.Dialog;
import devtoolsfx.gui.controls.FilterField;
import devtoolsfx.gui.controls.FilterableTreeItem;
import devtoolsfx.gui.controls.TextView;
import devtoolsfx.gui.util.GUIHelpers;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@NullMarked
public class EnvironmentTab extends VBox {

    public static final String TAB_NAME = "Environment";

    private static final int MIN_FILTER_LENGTH = 3;
    private static final PseudoClass GROUP = PseudoClass.getPseudoClass("group");
    private static final PseudoClass LEAF = PseudoClass.getPseudoClass("leaf");

    private final FilterField filterField = new FilterField();
    private final TreeTableView<KeyValue> kvTable = new TreeTableView<>();
    private final FilterableTreeItem<KeyValue> treeRoot = new FilterableTreeItem<>();
    private final FilterableTreeItem<KeyValue> platformRoot = new FilterableTreeItem<>(
        new KeyValue("Platform", null)
    );
    private final FilterableTreeItem<KeyValue> propertiesRoot = new FilterableTreeItem<>(
        new KeyValue("System Properties", null)
    );
    private final FilterableTreeItem<KeyValue> envVariablesRoot = new FilterableTreeItem<>(
        new KeyValue("Environment Variables", null)
    );
    private @Nullable Dialog<TextView> textViewDialog = null;

    private final ToolPane toolPane;
    private final Env env;

    public EnvironmentTab(ToolPane toolPane) {
        super();

        this.toolPane = toolPane;
        this.env = toolPane.getConnector().getEnv();

        createLayout();
        initListeners();
    }

    public void update() {
        var platformProps = Stream.of(
                env.getConditionalFeatures(),
                env.getPlatformPreferences(),
                env.getOtherPlatformProperties()
            )
            .flatMap(Collection::stream)
            .sorted()
            .map(TreeItem::new)
            .toList();
        platformRoot.setItems(platformProps);

        var systemProps = env.getSystemProperties().stream()
            .sorted()
            .map(TreeItem::new)
            .toList();
        propertiesRoot.setItems(systemProps);

        var envVariables = env.getEnvVariables().stream()
            .sorted()
            .map(TreeItem::new)
            .toList();
        envVariablesRoot.setItems(envVariables);

        setFilter(filterField.getText());
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        filterField.setPromptText("filter");
        HBox.setHgrow(filterField, Priority.ALWAYS);
        filterField.setMinHeight(Region.USE_PREF_SIZE);
        filterField.setMaxHeight(Region.USE_PREF_SIZE);

        var filterBox = new HBox(filterField);
        filterBox.getStyleClass().add("filter");
        filterBox.setFillHeight(true);
        VBox.setVgrow(filterBox, Priority.NEVER);

        platformRoot.setExpanded(true);
        propertiesRoot.setExpanded(true);
        envVariablesRoot.setExpanded(true);

        treeRoot.setItems(List.of(platformRoot, propertiesRoot, envVariablesRoot));
        kvTable.setRoot(treeRoot);
        kvTable.setShowRoot(false);

        createTableColumns();
        kvTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        kvTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        kvTable.setContextMenu(createContextMenu());
        VBox.setVgrow(kvTable, Priority.ALWAYS);

        setId("environment-tab");
        getStyleClass().setAll("tab");
        getChildren().setAll(filterBox, kvTable);
    }

    private void initListeners() {
        filterField.setOnTextChange(() -> setFilter(filterField.getText()));
        filterField.setOnClearButtonClick(() -> {
            filterField.setText(null);
            setFilter(null);
        });

        kvTable.setOnMouseClicked(event -> {
            if (MouseButton.PRIMARY.equals(event.getButton()) && event.getClickCount() == 2 && !kvTable.getSelectionModel().isEmpty()) {
                var item = kvTable.getSelectionModel().getSelectedItem();
                if (item.getValue().value() == null) {
                    return;
                }

                var dialog = getOrCreateTextViewDialog();
                dialog.getRoot().setText(item.getValue().key() + "=" + item.getValue().value());
                dialog.show();
                dialog.toFront();
            }
        });

        kvTable.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                GUIHelpers.copySelectedRowsToClipboard(kvTable, kv -> kv.key() + "=" + kv.value());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void createTableColumns() {
        var keyCol = new TreeTableColumn<KeyValue, String>("Property");
        keyCol.setCellValueFactory(param -> columnMapper(param, KeyValue::key));
        keyCol.setCellFactory(c -> new TreeTableCell<>() {
            {
                getStyleClass().add("key-cell");
            }

            @Override
            protected void updateItem(@Nullable String key, boolean empty) {
                super.updateItem(key, empty);

                if (empty) {
                    setText(null);
                    return;
                }

                pseudoClassStateChanged(LEAF, getTableRow().getTreeItem().isLeaf());
                getTableRow().pseudoClassStateChanged(GROUP, getTableRow().getTreeItem() instanceof FilterableTreeItem);
                setText(key);
            }
        });
        keyCol.setSortable(false);
        keyCol.setReorderable(false);
        keyCol.setPrefWidth(30);

        var valueCol = new TreeTableColumn<KeyValue, String>("Value");
        valueCol.setCellValueFactory(param -> columnMapper(param, KeyValue::value));
        valueCol.setSortable(false);
        valueCol.setReorderable(false);

        kvTable.getColumns().setAll(keyCol, valueCol);
    }

    private ContextMenu createContextMenu() {
        var refresh = new MenuItem("Refresh");
        refresh.setOnAction(e -> update());

        var contextMenu = new ContextMenu();
        contextMenu.setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "envTableOptionsMenu");
        contextMenu.getItems().addAll(refresh);

        return contextMenu;
    }

    private void setFilter(@Nullable String filter) {
        if (filter != null && filter.length() >= MIN_FILTER_LENGTH) {
            platformRoot.setFilterPredicate(item -> containsIgnoreCase(item.getValue().key(), filter));
            propertiesRoot.setFilterPredicate(item -> containsIgnoreCase(item.getValue().key(), filter));
            envVariablesRoot.setFilterPredicate(item -> containsIgnoreCase(item.getValue().key(), filter));
        } else {
            platformRoot.setFilterPredicate(null);
            propertiesRoot.setFilterPredicate(null);
            envVariablesRoot.setFilterPredicate(null);
        }

        refreshRootFilter();
    }

    private void refreshRootFilter() {
        treeRoot.setFilterPredicate(null);
        treeRoot.setFilterPredicate(
            item -> item instanceof FilterableTreeItem<KeyValue> filterable && !filterable.isEmpty()
        );
    }

    private <T extends TreeTableColumn.CellDataFeatures<@Nullable KeyValue, String>> ObservableValue<@Nullable String> columnMapper(
        T cdf, Function<KeyValue, String> mapper) {

        if (cdf.getValue() == null || cdf.getValue().getValue() == null) {
            return new SimpleStringProperty(null);
        }

        return new SimpleStringProperty(mapper.apply(cdf.getValue().getValue()));
    }

    private Dialog<TextView> getOrCreateTextViewDialog() {
        if (textViewDialog == null) {
            textViewDialog = new Dialog<>(
                new TextView(),
                "Details",
                640,
                480,
                toolPane.getPreferences().getDarkMode()
            );
        }

        return textViewDialog;
    }

    private boolean containsIgnoreCase(@Nullable String str, @Nullable String subStr) {
        return str != null && (subStr == null || str.toLowerCase().contains(subStr.toLowerCase()));
    }
}
