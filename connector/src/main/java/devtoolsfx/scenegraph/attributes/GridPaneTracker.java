package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * The {@link Tracker} implementation for the {@link GridPane} class.
 */
@NullMarked
public final class GridPaneTracker extends Tracker {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "hgap", "vgap", "alignment", "gridLinesVisible", "rowConstrains", "columnConstraints"
    );

    private final ListChangeListener<RowConstraints> rowListener = c -> reload("rowConstrains");
    private final ListChangeListener<ColumnConstraints> colListener = c -> reload("columnConstraints");

    public GridPaneTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.GRID_PANE);
    }

    @Override
    public void reload(String... properties) {
        GridPane gridpane = (GridPane) getTarget();
        if (gridpane == null) {
            return;
        }

        reload(property -> read(gridpane, property), SUPPORTED_PROPERTIES, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof GridPane;
    }

    @Override
    public void setTarget(@Nullable Object target) {
        if (getTarget() != null) {
            GridPane old = (GridPane) getTarget();
            old.getRowConstraints().removeListener(rowListener);
            old.getColumnConstraints().removeListener(colListener);
        }
        super.setTarget(target);

        GridPane grid = (GridPane) target;
        if (grid != null) {
            grid.getRowConstraints().addListener(rowListener);
            grid.getColumnConstraints().addListener(colListener);
        }
    }

    @Override
    protected void beforeResetTarget(Object target) {
        GridPane grid = (GridPane) target;
        grid.getRowConstraints().removeListener(rowListener);
        grid.getColumnConstraints().removeListener(colListener);
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(GridPane gridpane, String property) {
        return switch (property) {
            case "hgap" -> new Attribute<>(
                "hgap",
                gridpane.getHgap(),
                "hgapProperty",
                "-fx-hgap",
                ObservableType.of(gridpane.hgapProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(gridpane.getHgap() == 0)
            );
            case "vgap" -> new Attribute<>(
                "vgap",
                gridpane.getVgap(),
                "vgapProperty",
                "-fx-vgap",
                ObservableType.of(gridpane.vgapProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(gridpane.getVgap() == 0)
            );
            case "alignment" -> new Attribute<>(
                "alignment",
                gridpane.getAlignment(),
                "alignmentProperty",
                "-fx-alignment",
                ObservableType.of(gridpane.alignmentProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(gridpane.getAlignment() == null || gridpane.getAlignment() == Pos.TOP_LEFT)
            );
            case "gridLinesVisible" -> new Attribute<>(
                "gridLinesVisible",
                gridpane.isGridLinesVisible(),
                "gridLinesVisibleProperty",
                "-fx-grid-lines-visible",
                ObservableType.of(gridpane.gridLinesVisibleProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(!gridpane.isGridLinesVisible())
            );
            case "rowConstrains" -> new Attribute<>(
                "rowConstrains",
                Collections.unmodifiableList(gridpane.getRowConstraints()),
                "getRowConstraints",
                ObservableType.LIST,
                DisplayHint.ROW_CONSTRAINTS,
                ValueState.defaultIf(gridpane.getRowConstraints().isEmpty())
            );
            case "columnConstraints" -> new Attribute<>(
                "columnConstraints",
                Collections.unmodifiableList(gridpane.getColumnConstraints()),
                "getColumnConstraints",
                ObservableType.LIST,
                DisplayHint.COLUMN_CONSTRAINTS,
                ValueState.defaultIf(gridpane.getColumnConstraints().isEmpty())
            );
            default -> null;
        };
    }
}
