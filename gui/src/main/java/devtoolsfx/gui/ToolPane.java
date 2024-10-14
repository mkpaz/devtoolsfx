package devtoolsfx.gui;

import devtoolsfx.connector.Connector;
import devtoolsfx.connector.ConnectorOptions;
import devtoolsfx.connector.Env;
import devtoolsfx.connector.HighlightOptions;
import devtoolsfx.event.*;
import devtoolsfx.gui.controls.Dialog;
import devtoolsfx.gui.controls.TabLine;
import devtoolsfx.gui.env.EnvironmentTab;
import devtoolsfx.gui.eventlog.EventLogTab;
import devtoolsfx.gui.inspector.InspectorTab;
import devtoolsfx.gui.preferences.PreferencesTab;
import devtoolsfx.gui.style.StylesheetTab;
import devtoolsfx.gui.util.Formatters;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.WindowProperties;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;
import javafx.util.Duration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;

/**
 * The embeddable development tools root node.
 * It is also responsible for interacting with the {@link Connector}.
 */
@NullMarked
public final class ToolPane extends BorderPane {

    private static final Logger LOGGER = System.getLogger(ToolPane.class.getName());
    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");
    private static final PseudoClass DARK_MODE = PseudoClass.getPseudoClass("dark");

    // we can't use close() because we are not in FXThread
    private final Connector connector;
    private final ConnectorAdapter connectorAdapter = new ConnectorAdapter();
    private final ConnectorOptions connectorOpts;
    private final Preferences preferences;
    private final Queue<ConnectorEvent> eventQueue = new ArrayDeque<>();

    private final ChangeListener<Boolean> ignoreMouseTransparentListener;
    private final ChangeListener<Boolean> preventPopupAutoHideListener;
    private final Runnable refreshSelectionHandler;

    // tabs
    private final TabLine tabLine = new TabLine(
        InspectorTab.TAB_NAME,
        EventLogTab.TAB_NAME,
        StylesheetTab.TAB_NAME,
        EnvironmentTab.TAB_NAME,
        PreferencesTab.TAB_NAME
    );
    private final StackPane tabs = new StackPane();
    private final InspectorTab inspectorTab;
    private final EventLogTab eventLogTab;
    private final StylesheetTab stylesheetTab;
    private final EnvironmentTab environmentTab;
    private final PreferencesTab preferencesTab;
    private final Button inspectButton = new Button();

    private long lastMousePos;

    public ToolPane(Connector connector, Preferences preferences) {
        this.connector = Objects.requireNonNull(connector, "connector must not be null");
        this.preferences = Objects.requireNonNull(preferences, "preferences must not be null");
        this.connectorOpts = connector.getOptions();

        // init after preferences, but before layout
        inspectorTab = new InspectorTab(this);
        eventLogTab = new EventLogTab(this);
        stylesheetTab = new StylesheetTab(this);
        environmentTab = new EnvironmentTab(this);
        preferencesTab = new PreferencesTab(this);

        ignoreMouseTransparentListener = (obs, old, val) -> connectorOpts.setIgnoreMouseTransparent(val);
        preventPopupAutoHideListener = (obs, old, val) -> connectorOpts.setPreventPopupAutoHide(val);
        refreshSelectionHandler = () -> getConnector().refreshSelection();

        createLayout();
        initListeners();

        toggleDarkMode(preferences.getDarkMode());

        tabLine.selectTab(InspectorTab.TAB_NAME);
        startListenToEvents(false);
    }

    /**
     * Returns the GUI preferences.
     */
    public Preferences getPreferences() {
        return preferences;
    }

    /**
     * Returns the selected scene graph tree element.
     */
    public @Nullable Element getSelectedElement() {
        return inspectorTab.getSelectedTreeElement();
    }

    /**
     * Returns the wrapper that groups connector methods to avoid name conflicts
     * and encapsulates the logic in a single location.
     */
    public ConnectorAdapter getConnector() {
        return connectorAdapter;
    }

    public class ConnectorAdapter {

        /**
         * See {@link Connector#start()}.
         */
        public void start() {
            connector.start();
        }

        /**
         * See {@link Connector#stop()}}.
         */
        public void stop() {
            connector.start();
        }

        /**
         * See {@link Connector#getEnv()}}.
         */
        public Env getEnv() {
            return connector.getEnv();
        }

        /**
         * See {@link Connector#selectNode(int, Element, HighlightOptions)}}.
         */
        public void selectElement(int uid, Element element) {
            inspectorTab.clearAttributes();

            if (element.isWindowElement()) {
                connector.selectWindow(uid);
            }

            if (element.isNodeElement()) {
                connector.selectNode(uid, element, preferences.getHighlightOptions());
            }
        }

        /**
         * See {@link Connector#reloadSelectedAttributes(int, AttributeCategory, String)}}.
         */
        public void reloadSelectedAttributes(@Nullable AttributeCategory category, @Nullable String property) {
            Element selected = inspectorTab.getSelectedTreeElement();
            if (selected == null) {
                return;
            }

            int uid = inspectorTab.getWindow(selected);
            if (uid == 0) {
                return;
            }

            connector.reloadSelectedAttributes(uid, category, property);
        }

        /**
         * See {@link Connector#clearSelection(int)}}.
         */
        public void clearSelection(int uid) {
            connector.clearSelection(uid);
        }

        public void refreshSelection() {
            Element selected = inspectorTab.getSelectedTreeElement();
            if (selected == null) {
                return;
            }

            int uid = inspectorTab.getWindow(selected);
            if (uid == 0) {
                return;
            }

            selectElement(uid, selected);
        }

        /**
         * See {@link Connector#hideWindow(int)}}.
         */
        public void hideWindow(int uid) {
            connector.hideWindow(uid);
        }

        /**
         * Returns the identifiers of the currently monitored windows.
         */
        public List<Integer> getMonitorIdentifiers() {
            return connector.getEventSources().stream().map(EventSource::uid).toList();
        }

        /**
         * See {@link Connector#getStyledElements(int)}}.
         */
        public Map.@Nullable Entry<WindowProperties, List<Element>> getStyledElements(int uid) {
            return connector.getStyledElements(uid);
        }

        /**
         * See {@link Connector#getUserAgentStylesheet()}}.
         */
        public String getUserAgentStylesheet() {
            return connector.getUserAgentStylesheet();
        }

        /**
         * See {@link Connector#getResource(int, String)}}.
         */
        public @Nullable String getResource(int uid, String uri) {
            return connector.getResource(uid, uri);
        }

        /**
         * See {@link Connector#getDeclaringClass(String, String)}}.
         */
        public @Nullable String getDeclaringClass(String className, String property) {
            return connector.getDeclaringClass(className, property);
        }
    }

    /**
     * Handles the GUI exceptions.
     * <p>
     * Note: We can't use the {@link UncaughtExceptionHandler} because the embedded GUI operates
     * in the FXThread and the target application may or want to set its own exception handler.
     */
    public void handleException(Exception e) {
        LOGGER.log(Level.WARNING, Formatters.exceptionToString(e));
    }

    @Override
    public String getUserAgentStylesheet() {
        return GUI.USER_AGENT_STYLESHEET;
    }

///////////////////////////////////////////////////////////////////////////
    // UI construction                                                       //
    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        var icon = new StackPane();
        icon.getStyleClass().add("icon");
        inspectButton.setGraphic(icon);

        inspectButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        inspectButton.setId("inspect-button");

        tabLine.getChildren().addFirst(inspectButton);
        tabs.getChildren().setAll(
            inspectorTab,
            eventLogTab,
            stylesheetTab,
            environmentTab,
            preferencesTab
        );

        setTop(tabLine);
        setCenter(tabs);
    }

    private void initListeners() {
        preferences.ignoreMouseTransparentProperty().addListener(ignoreMouseTransparentListener);
        connectorOpts.setIgnoreMouseTransparent(preferences.isIgnoreMouseTransparent());

        preferences.preventPopupAutoHideProperty().addListener(preventPopupAutoHideListener);
        connectorOpts.setPreventPopupAutoHide(preferences.isPreventPopupAutoHide());

        preferences.showLayoutBoundsProperty().subscribe(refreshSelectionHandler);
        preferences.showBoundsInParentProperty().subscribe(refreshSelectionHandler);
        preferences.showBaselineProperty().subscribe(refreshSelectionHandler);

        preferences.darkModeProperty().addListener((obs, old, val) -> toggleDarkMode(val));

        tabLine.setOnTabSelect(tab -> {
            switch (tab) {
                case InspectorTab.TAB_NAME -> inspectorTab.toFront();
                case EventLogTab.TAB_NAME -> eventLogTab.toFront();
                case StylesheetTab.TAB_NAME -> {
                    stylesheetTab.toFront();
                    stylesheetTab.update();
                }
                case EnvironmentTab.TAB_NAME -> {
                    environmentTab.toFront();
                    environmentTab.update();
                }
                case PreferencesTab.TAB_NAME -> preferencesTab.toFront();
            }
        });

        inspectButton.setOnAction(e -> {
            boolean enabled = !connectorOpts.isInspectMode();
            connectorOpts.setInspectMode(enabled);
            inspectButton.pseudoClassStateChanged(ACTIVE, enabled);
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Event Handling                                                        //
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("SameParameterValue")
    private void startListenToEvents(boolean useQueue) {
        if (useQueue) {
            // Optionally, we can update the GUI on a separate queue, which adds a small delay.
            // This is how it was implemented before and was left as an option.
            Timeline eventDispatcher = new Timeline(new KeyFrame(Duration.millis(60), event -> {
                // no need to synchronize
                while (!eventQueue.isEmpty()) {
                    try {
                        dispatchEvent(eventQueue.poll());
                    } catch (Exception e) {
                        handleException(e);
                    }
                }
            }));

            eventDispatcher.setCycleCount(Animation.INDEFINITE);
            eventDispatcher.play();
        }

        connector.getEventBus().subscribe(ConnectorEvent.class, event -> {
            if (event instanceof MousePosEvent) {
                // traffic protection
                if (System.currentTimeMillis() - lastMousePos < 500) {
                    return;
                }
                lastMousePos = System.currentTimeMillis();
            }

            if (useQueue) {
                eventQueue.offer(event);
            } else {
                dispatchEvent(event);
            }
        });
    }

    private void dispatchEvent(ConnectorEvent connectorEvent) {
        eventLogTab.offer(connectorEvent);

        switch (connectorEvent) {
            case AttributeListEvent event -> inspectorTab.setAttributes(
                event.category(), event.attributes()
            );
            case AttributeUpdatedEvent event -> inspectorTab.updateAttribute(
                event.category(), event.attribute()
            );
            case NodeAddedEvent event -> inspectorTab.addTreeElement(event.element());
            case NodeRemovedEvent event -> inspectorTab.removeTreeElement(event.element());
            case NodeSelectedEvent event -> {
                connectorOpts.setInspectMode(false);
                inspectButton.pseudoClassStateChanged(ACTIVE, false);
                inspectorTab.selectTreeElement(event.element());
            }
            case NodeStyleClassEvent event -> inspectorTab.updateTreeElementStyleClass(
                event.element(), event.styleClass()
            );
            case NodeVisibilityEvent event -> inspectorTab.updateTreeElementVisibilityState(
                event.element(), event.visible()
            );
            case RootChangedEvent event -> inspectorTab.addOrUpdateWindow(event.element());
            case WindowClosedEvent event -> inspectorTab.removeWindow(event.eventSource().uid());
            default -> {
                // if there's no specific event here, then it's just for logging
            }
        }
    }

    private void toggleDarkMode(boolean darkMode) {
        pseudoClassStateChanged(DARK_MODE, darkMode);
        Window.getWindows().stream()
            .filter(w -> w instanceof Dialog<?>)
            .forEach(dialog -> ((Dialog<?>) dialog).toggleDarkMode(darkMode));
    }
}
