package devtoolsfx.gui.inspector;

import devtoolsfx.connector.ConnectorOptions;
import devtoolsfx.gui.controls.ColorIndicator;
import devtoolsfx.gui.util.Formatters;
import devtoolsfx.gui.util.GUIHelpers;
import devtoolsfx.scenegraph.attributes.Attribute;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

@NullMarked
final class AttributeTreeTable extends TreeTableView<AttributeCellContent> {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    private static final PseudoClass GROUP = PseudoClass.getPseudoClass("group");
    private static final PseudoClass DEFAULT = PseudoClass.getPseudoClass("default");
    private static final PseudoClass LEAF = PseudoClass.getPseudoClass("leaf");

    private final AttributeTreeItem root = new AttributeTreeItem(AttributeCellContent.forRoot());

    private @Nullable Runnable refreshHandler;
    private @Nullable String filter;

    AttributeTreeTable() {
        super();

        createTableColumns();
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setContextMenu(createContextMenu());

        setId("attribute-tree-table");
        setRoot(root);
        setShowRoot(false);
        root.setExpanded(true);

        setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                GUIHelpers.copySelectedRowsToClipboard(this, content ->
                    content.getAttribute() != null ? formatValueByText(content.getAttribute()).value() : ""
                );
            }
        });

        refreshRootFilter();
    }

    void setAttributes(AttributeCategory category,
                       List<Attribute<?>> attributes) {
        AttributeTreeItem group = findGroupByCategory(category);
        if (group == null) {
            group = new AttributeTreeItem(AttributeCellContent.forGroup(category));
            root.addGroup(group);
        }

        group.setAttributes(attributes);
        group.setExpanded(true);

        refreshRootFilter();
    }

    void updateAttribute(AttributeCategory category,
                         Attribute<?> attribute) {
        AttributeTreeItem group = findGroupByCategory(category);
        if (group == null) {
            return;
        }
        group.updateAttribute(attribute);
    }

    void clear() {
        filter = null;
        root.clear();
        refreshRootFilter();
    }

    @Nullable
    String getFilter() {
        return filter;
    }

    void setFilter(@Nullable String filter) {
        this.filter = filter;

        // we have to obtain children from the source list,
        // not from the filtered list
        for (var group : root.getChildrenUnmodifiable()) {
            group.setFilterText(filter);
        }

        refreshRootFilter();
    }

    void setRefreshHandler(Runnable handler) {
        this.refreshHandler = handler;
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createTableColumns() {
        var propertyCol = new TreeTableColumn<AttributeCellContent, AttributeCellContent>("Property");
        var valueCol = new TreeTableColumn<AttributeCellContent, AttributeCellContent>("Value");

        Callback<CellDataFeatures<AttributeCellContent, AttributeCellContent>, ObservableValue<AttributeCellContent>>
            cellValueFactory = cdf -> new SimpleObjectProperty<>(cdf.getValue().getValue());

        propertyCol.setCellValueFactory(cellValueFactory);
        propertyCol.setCellFactory(c -> new TreeTableCell<>() {
            final Label infoLabel = new Label();

            {
                getStyleClass().add("property-cell");
                setContentDisplay(ContentDisplay.RIGHT);
                infoLabel.getStyleClass().add("info");
            }

            @Override
            protected void updateItem(@Nullable AttributeCellContent content, boolean empty) {
                super.updateItem(content, empty);

                if (empty || content == null) {
                    setGraphic(null);
                    infoLabel.setText(null);
                    setText(null);
                    getTableRow().pseudoClassStateChanged(GROUP, false);
                    return;
                }

                String text = null;

                if (content.isGroup()) {
                    switch (content.getCategory()) {
                        case CONTROL -> text = "Control Properties";
                        case GRID_PANE -> text = "GridPane Properties";
                        case LABELED -> text = "Labeled Properties";
                        case IMAGE_VIEW -> text = "Image Properties";
                        case NODE -> text = "Node Properties";
                        case PARENT -> text = "Parent Properties";
                        case REGION -> text = "Region Properties";
                        case SCENE -> text = "Scene Properties";
                        case SHAPE -> text = "Shape Properties";
                        case TEXT -> text = "Text Properties";
                        case WINDOW -> text = "Window Properties";
                        case REFLECTIVE -> text = "Reflective Properties";
                        case null -> {
                        }
                    }
                    setGraphic(null);
                    infoLabel.setText(null);
                } else if (content.getAttribute() != null) {
                    text = content.getAttribute().name();

                    var cssProperty = content.getAttribute().cssProperty();
                    if (cssProperty != null && !cssProperty.isEmpty()) {
                        setGraphic(infoLabel);
                        infoLabel.setText("CSS");
                    } else {
                        setGraphic(null);
                        infoLabel.setText(null);
                    }
                }

                pseudoClassStateChanged(LEAF, getTableRow().getTreeItem().isLeaf());
                getTableRow().pseudoClassStateChanged(GROUP, content.isGroup());
                setText(text);
            }
        });
        propertyCol.setSortable(false);
        propertyCol.setReorderable(false);
        propertyCol.setMinWidth(200);
        propertyCol.setPrefWidth(200);
        propertyCol.setMaxWidth(300);

        valueCol.setCellValueFactory(cellValueFactory);
        valueCol.setCellFactory(c -> new TreeTableCell<>() {
            @Override
            protected void updateItem(@Nullable AttributeCellContent content, boolean empty) {
                super.updateItem(content, empty);

                if (empty || content == null || content.getAttribute() == null) {
                    setText(null);
                    setGraphic(null);
                    getTableRow().pseudoClassStateChanged(DEFAULT, false);
                } else {
                    var cv = formatValueByText(content.getAttribute());
                    setText(cv.value());
                    getTableRow().pseudoClassStateChanged(DEFAULT, cv.isDefault());

                    setGraphic(getOptionalValueGraphic(content.getAttribute()));
                }
            }
        });
        valueCol.setSortable(false);
        valueCol.setReorderable(false);

        getColumns().add(propertyCol);
        getColumns().add(valueCol);
    }

    private ContextMenu createContextMenu() {
        var refresh = new MenuItem("Refresh");
        refresh.setOnAction(e -> {
            if (refreshHandler != null) {
                refreshHandler.run();
            }
        });

        var contextMenu = new ContextMenu();
        contextMenu.setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "attributeTableOptionsMenu");
        contextMenu.getItems().addAll(refresh);

        return contextMenu;
    }

    private @Nullable AttributeTreeItem findGroupByCategory(AttributeCategory category) {
        for (var child : root.getChildren()) {
            if (child instanceof AttributeTreeItem item && item.isGroupOf(category)) {
                return item;
            }
        }
        return null;
    }

    private void refreshRootFilter() {
        // even though we're creating a new predicate instance each time,
        // it won't work without setting it to null first
        root.setFilterText(null);
        root.setFilterPredicate(group -> !group.isEmpty());
    }

    private CellValue formatValueByText(Attribute<?> attribute) {
        var value = attribute.value();
        return switch (attribute.displayHint()) {
            case COLOR -> {
                if (value instanceof Color color) {
                    yield CellValue.of(
                        Formatters.colorToHexString(color) + "; " + Formatters.colorToRgbString(color),
                        attribute
                    );
                }

                yield CellValue.of(String.valueOf(value).toUpperCase(), attribute);
            }
            case INSETS -> {
                if (value instanceof Insets insets && (Objects.equals(insets, Insets.EMPTY)
                    || (insets.getTop() == 0 && insets.getRight() == 0 && insets.getBottom() == 0 && insets.getLeft() == 0))) {
                    yield new CellValue("Insets.EMPTY", true);
                }
                yield CellValue.of(String.valueOf(value), attribute);
            }
            case NUMERIC -> {
                boolean isDefault = attribute.valueState() == ValueState.DEFAULT
                    || (value instanceof Number num && num.doubleValue() == 0);

                if (value instanceof Double num) {
                    if (num == Region.USE_COMPUTED_SIZE) {
                        yield new CellValue("USE_COMPUTED_SIZE", true);
                    }
                    if (num == Region.USE_PREF_SIZE) {
                        yield new CellValue("USE_PREF_SIZE", true);
                    }
                    if (num == Double.MIN_VALUE) {
                        yield new CellValue("MIN_VALUE", true);
                    }
                    if (num == Double.MAX_VALUE) {
                        yield new CellValue("MAX_VALUE", true);
                    }

                    yield new CellValue(DECIMAL_FORMAT.format(num), isDefault);
                }

                yield new CellValue(String.valueOf(value), isDefault);
            }
            default -> {
                if (value instanceof List<?> list && list.isEmpty()) {
                    yield new CellValue("[]", true);
                }

                var s = String.valueOf(value);
                boolean isDefault = attribute.valueState() == ValueState.DEFAULT || s.isEmpty() || "null".equals(s);
                yield new CellValue(s, isDefault);
            }
        };
    }

    private @Nullable Node getOptionalValueGraphic(Attribute<?> attribute) {
        if (attribute.displayHint() == DisplayHint.COLOR && attribute.value() instanceof Color color) {
            return new ColorIndicator(color);
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////////////

    private record CellValue(String value, boolean isDefault) {

        public static CellValue of(String value, Attribute<?> attribute) {
            return new CellValue(value, attribute.valueState() == ValueState.DEFAULT);
        }
    }
}
