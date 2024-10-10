package devtoolsfx.connector;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.event.ExceptionEvent;
import devtoolsfx.event.WindowClosedEvent;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.WindowProperties;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import devtoolsfx.util.SceneUtils;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ListChangeListener;
import javafx.scene.control.PopupControl;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Implements the {@link Connector} interface for local (this JVM process) nodes.
 * Also see {@link LocalElement}.
 */
@NullMarked
public final class LocalConnector implements Connector {

    private static final Logger LOGGER = System.getLogger(LocalConnector.class.getName());

    private final String application;
    private final ConnectorOptions opts;
    private final EventBus eventBus = new EventBus();
    private final Env env = new LocalEnv();

    private final Map<Integer, WindowMonitor> monitors = new HashMap<>();

    private final ListChangeListener<Window> windowListChangeListener = this::onWindowListChanged;
    private final ReadOnlyBooleanWrapper started = new ReadOnlyBooleanWrapper();

    /**
     * See {@link LocalConnector#(Stage, ConnectorOptions , String)}.
     */
    public LocalConnector(Stage primaryStage) {
        this(primaryStage, null, null);
    }

    /**
     * See {@link LocalConnector#(Stage, ConnectorOptions , String)}.
     */
    public LocalConnector(Stage primaryStage, @Nullable String application) {
        this(primaryStage, application, null);
    }

    /**
     * Creates a new connector.
     * Only one connector per app should be created.
     *
     * @param primaryStage the target app's primary stage
     * @param application  the target app's name that will be reported in events,
     *                     see {@link EventSource#application()}
     * @param opts         the connector options
     */
    public LocalConnector(Stage primaryStage,
                          @Nullable String application,
                          @Nullable ConnectorOptions opts) {
        Objects.requireNonNull(primaryStage, "primary stage must not be null");

        this.application = Objects.requireNonNullElse(application, "app-" + primaryStage.hashCode());
        this.opts = Objects.requireNonNullElse(opts, new ConnectorOptions());

        monitors.put(uidOf(primaryStage), createMonitor(primaryStage, application, true));

        this.opts.inspectModeProperty().addListener((obs, old, val) -> {
            if (!val) {
                // prevents ConcurrentModificationException
                new ArrayList<>(monitors.values()).forEach(monitor -> monitor.setInspectMode(false));
            }
        });
    }

    @Override
    public void start() {
        started.set(true);

        monitors.forEach((hash, monitor) -> monitor.start());
        Window.getWindows().addListener(windowListChangeListener);
        LOGGER.log(Level.INFO, "LocalConnector started");
    }

    @Override
    public void stop() {
        started.set(false);

        monitors.forEach((hash, monitor) -> monitor.stop());
        Window.getWindows().removeListener(windowListChangeListener);
        LOGGER.log(Level.INFO, "LocalConnector stopped");
    }

    @Override
    public ReadOnlyBooleanProperty startedProperty() {
        return started.getReadOnlyProperty();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public List<EventSource> getEventSources() {
        return monitors.values().stream().map(WindowMonitor::getEventSource).toList();
    }

    @Override
    public ConnectorOptions getOptions() {
        return opts;
    }

    @Override
    public Env getEnv() {
        return env;
    }

    @Override
    public void selectWindow(int uid) {
        var monitor = monitors.get(uid);
        if (monitor != null) {
            monitor.selectWindow();
        } else {
            LOGGER.log(Level.WARNING, "Unable to select window: unknown window UID");
        }
    }

    @Override
    public void selectNode(int uid, Element element, @Nullable HighlightOptions opts) {
        var monitor = monitors.get(uid);
        if (monitor != null && element.isNodeElement()) {
            var node = element instanceof LocalElement local ? local.unwrap() : monitor.findNode(element.getUID());
            if (node != null) {
                monitor.selectNode(node, Objects.requireNonNullElse(opts, HighlightOptions.defaults()));
            } else {
                LOGGER.log(Level.WARNING, "Unable to select element: unknown node");
            }
        } else {
            LOGGER.log(Level.WARNING, "Unable to select element: unknown window UID");
        }
    }

    @Override
    public void clearSelection(int uid) {
        var monitor = monitors.get(uid);
        if (monitor != null) {
            monitor.clearSelection();
        } else {
            LOGGER.log(Level.WARNING, "Unable to clear selection: unknown window UID");
        }
    }

    @Override
    public void reloadSelectedAttributes(int uid,
                                         @Nullable AttributeCategory category,
                                         @Nullable String property) {
        var monitor = monitors.get(uid);
        if (monitor != null) {
            monitor.reloadSelectedAttributes(category, property);
        } else {
            LOGGER.log(Level.WARNING, "Unable to reload attributes: unknown window UID");
        }
    }

    @Override
    public void hideWindow(int uid) {
        var monitor = monitors.get(uid);
        if (monitor != null) {
            monitor.hideWindow();
        }
    }

    @Override
    public Map.@Nullable Entry<WindowProperties, List<Element>> getStyledElements(int uid) {
        var monitor = monitors.get(uid);
        if (monitor != null) {
            return monitor.getStyledElements();
        }
        return null;
    }

    @Override
    public String getUserAgentStylesheet() {
        var uas = Application.getUserAgentStylesheet();
        // not optimal, but there's no API to obtain platform's UA stylesheets URLs,
        // for the reference, they're in the StyleManager#platformUserAgentStylesheetContainers
        return Objects.requireNonNullElse(uas, Application.STYLESHEET_MODENA);
    }

    @Override
    public @Nullable String getResource(int uid, String uri) {
        // security check to avoid reading an arbitrary file
        var monitor = monitors.get(uid);
        if (monitor == null || !monitor.containsStylesheet(uri)) {
            return null;
        }

        String content = null;
        try {
            content = Files.readString(
                Paths.get(URI.create(uri).getPath())
            );
        } catch (Exception e) {
            eventBus.fire(ExceptionEvent.of(monitor.getEventSource(), e));
        }

        return content;
    }

    @Override
    public @Nullable String getDeclaringClass(String canonicalName, String property) {
        try {
            Class<?> cls = getDeclaringClass(Class.forName(canonicalName), property);
            return cls != null ? cls.getCanonicalName() : null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private @Nullable Class<?> getDeclaringClass(Class<?> cls, String method) {
        try {
            cls.getDeclaredMethod(method);
            return cls;
        } catch (NoSuchMethodException e) {
            Class<?> superClass = cls.getSuperclass();
            if (superClass != null) {
                return getDeclaringClass(superClass, method);
            }
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Instantiates a new {@link WindowMonitor}.
     */
    private WindowMonitor createMonitor(Window window,
                                        @Nullable String application,
                                        boolean isPrimaryStage) {

        int uid = uidOf(window);

        var app = application;
        if (app == null && window instanceof Stage stage) {
            app = stage.getTitle();
        }
        if (app == null) {
            app = "unknown#" + uid;
        }

        var eventSource = new EventSource(app, uid, isPrimaryStage);
        return new WindowMonitor(window, opts, eventBus, eventSource);
    }

    /**
     * Handles reported {@link Window#getWindows()} list changes.
     */
    private void onWindowListChanged(ListChangeListener.Change<? extends Window> change) {
        while (change.next()) {
            new ArrayList<>(change.getAddedSubList()).forEach(this::handleWindowAdd);
            new ArrayList<>(change.getRemoved()).forEach(this::handleWindowRemove);
        }
    }

    // package-private for unit tests
    void handleWindowAdd(Window window) {
        int uid = uidOf(window);

        // ignore auxiliary tooltips (inspect mode) and context menus
        if (window instanceof PopupControl popup && (
            // context menu with ID || menu button with ID
            SceneUtils.isAuxiliaryNode(popup) || SceneUtils.isAuxiliaryNode(popup.getOwnerNode()))
        ) {
            return;
        }

        // ignore any other auxiliary windows
        if (window.getScene() != null && SceneUtils.isAuxiliaryNode(window.getScene().getRoot())) {
            return;
        }

        // guard block, should never happen
        if (monitors.containsKey(uid)) {
            LOGGER.log(Level.ERROR, "Attempting to add a new window that already has a bound monitor object");
            monitors.remove(uid);
            handleWindowAdd(window);
        }

        var monitor = createMonitor(window, application, false);
        monitor.setInspectMode(opts.isInspectMode());

        if (window instanceof PopupWindow popup) {
            try {
                if (opts.isPreventPopupAutoHide()) {
                    popup.setAutoHide(false);
                }
            } catch (Exception ignored) {
                // some resource contention when autoHide=true
            }
        }

        monitors.put(uid, monitor);
        monitor.start();
    }

    // package-private for unit tests
    void handleWindowRemove(Window window) {
        var uid = uidOf(window);
        var monitor = monitors.get(uid);
        if (monitor != null) {
            monitor.stop();
            monitors.remove(uid);
            eventBus.fire(new WindowClosedEvent(monitor.getEventSource()));
        }
    }

    /**
     * Returns unique window ID.
     */
    private int uidOf(Window window) {
        return window.hashCode();
    }
}
