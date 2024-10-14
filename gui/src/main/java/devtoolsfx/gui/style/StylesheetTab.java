package devtoolsfx.gui.style;

import devtoolsfx.connector.ConnectorOptions;
import devtoolsfx.gui.ToolPane;
import devtoolsfx.gui.controls.Dialog;
import devtoolsfx.gui.controls.TextView;
import devtoolsfx.gui.util.Formatters;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.NodeProperties;
import devtoolsfx.scenegraph.WindowProperties;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

@NullMarked
public final class StylesheetTab extends VBox {

    public static final String TAB_NAME = "Stylesheets";

    static final int ROOT_UID = -1;
    static final String ROOT_ITEM_NAME = "Application";

    private final ToolPane toolPane;
    private final TreeView<String> treeView = new TreeView<>();

    private @Nullable Dialog<TextView> textViewDialog = null;

    public StylesheetTab(ToolPane toolPane) {
        super();

        this.toolPane = toolPane;

        createLayout();
    }

    public void update() {
        var treeRoot = StylesheetTreeItem.of(ROOT_UID, getPlatformUserAgentStylesheet(), true);
        treeRoot.setExpanded(true);

        for (int uid : toolPane.getConnector().getMonitorIdentifiers()) {
            Map.@Nullable Entry<WindowProperties, List<Element>> data = toolPane.getConnector().getStyledElements(uid);
            if (data == null || (data.getKey().sceneStylesheets().isEmpty() && data.getValue().isEmpty())) {
                continue;
            }

            WindowProperties windowProps = data.getKey();
            var window = StylesheetTreeItem.of(uid, Formatters.formatForTreeItem(uid, windowProps));
            window.setExpanded(true);

            if (windowProps.userAgentStylesheet() != null || !windowProps.sceneStylesheets().isEmpty()) {
                window.getChildren().add(createTreeItem(
                    uid,
                    "Scene",
                    windowProps.sceneStylesheets(),
                    windowProps.userAgentStylesheet()
                ));
            }

            for (Element element : data.getValue()) {
                if (!element.isNodeElement() || element.getNodeProperties() == null) {
                    continue;
                }

                NodeProperties nodeProps = element.getNodeProperties();
                window.getChildren().add(createTreeItem(
                    uid,
                    Formatters.formatForTreeItem(element.getSimpleClassName(), nodeProps),
                    nodeProps.stylesheets(),
                    nodeProps.userAgentStylesheet()
                ));
            }

            treeRoot.getChildren().add(window);
        }

        treeView.setRoot(treeRoot);
        treeView.setShowRoot(true);
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        treeView.setContextMenu(createContextMenu());
        treeView.setCellFactory(param ->
            new StylesheetTreeCell(
                this::getOrCreateTextViewDialog,
                this::getSourceCode
            )
        );
        VBox.setVgrow(treeView, Priority.ALWAYS);

        var hintIcon = new StackPane();
        hintIcon.getStyleClass().add("icon");

        var hintLabel = new Label("user agent stylesheet", hintIcon);
        hintLabel.getStyleClass().add("hint");

        setId("stylesheet-tab");
        getStyleClass().setAll("tab");
        getChildren().setAll(treeView, hintLabel);
    }

    private ContextMenu createContextMenu() {
        var refresh = new MenuItem("Refresh");
        refresh.setOnAction(e -> update());

        var contextMenu = new ContextMenu();
        contextMenu.setId(ConnectorOptions.AUX_NODE_ID_PREFIX + "stylesheetOptionsMenu");
        contextMenu.getItems().addAll(refresh);

        return contextMenu;
    }

    private StylesheetTreeItem createTreeItem(int uid,
                                              String name,
                                              List<String> stylesheets,
                                              @Nullable String userAgentStyleSheet) {
        var parent = StylesheetTreeItem.of(uid, name);
        parent.setExpanded(true);

        if (userAgentStyleSheet != null) {
            parent.getChildren().add(StylesheetTreeItem.of(uid, userAgentStyleSheet, true));
        }

        for (String uri : stylesheets) {
            var child = StylesheetTreeItem.of(uid, uri, false);
            parent.getChildren().add(child);
        }

        return parent;
    }

    private String getPlatformUserAgentStylesheet() {
        return toolPane.getConnector().getUserAgentStylesheet();
    }

    private @Nullable String getSourceCode(int uid, Stylesheet stylesheet) {
        if (stylesheet.isDataURI()) {
            return stylesheet.decodeFromDataURI();
        }

        return toolPane.getConnector().getResource(uid, stylesheet.uri());
    }

    private Dialog<TextView> getOrCreateTextViewDialog() {
        if (textViewDialog == null) {
            textViewDialog = new Dialog<>(
                new TextView(),
                "Source Code",
                640,
                480,
                toolPane.getPreferences().getDarkMode()
            );
        }

        return textViewDialog;
    }
}
