package devtoolsfx.connector;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.WindowProperties;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The connector serves as the main entry point for application monitoring. It accepts
 * the target app's primary stage and tracks and reports its state and changes via the
 * {@link EventBus}. The client should subscribe to EventBus events to react to these changes.
 */
@NullMarked
public interface Connector {

    /**
     * Starts the connector, which monitors and reports on all existing and new application windows.
     */
    void start();

    /**
     * The opposite of {@link #start()}.
     */
    void stop();

    /**
     * Returns the start/stop state of the connector.
     */
    ReadOnlyBooleanProperty startedProperty();

    /**
     * Returns the {@link EventBus} to react to the connector events.
     */
    EventBus getEventBus();

    /**
     * Returns the list of event sources for all currently monitored objects.
     */
    List<EventSource> getEventSources();

    /**
     * Returns the connector options.
     */
    ConnectorOptions getOptions();

    /**
     * Returns the interface for accessing system or platform information
     * about the monitored application.
     */
    Env getEnv();

    /**
     * Selects and starts monitoring the attributes of the window and scene.
     * This method is mutually exclusive with {@link #selectNode(int, Element, HighlightOptions)}.
     */
    void selectWindow(int uid);

    /**
     * Selects and starts monitoring the node attributes and visual highlights of the
     * specified element's bounds, if possible. This method is mutually exclusive with
     * {@link #selectWindow(int)}.
     */
    void selectNode(int uid, Element element, @Nullable HighlightOptions opts);

    /**
     * The opposite of {@link #selectNode(int, Element, HighlightOptions)}.
     *
     * @param uid see {@link EventSource#uid()}
     */
    void clearSelection(int uid);

    /**
     * Reloads the attributes of the selected element, if any. If no property is specified,
     * all category attributes will be reloaded. If no category is specified, all element
     * attributes across all categories will be reloaded.
     */
    void reloadSelectedAttributes(int uid, @Nullable AttributeCategory category, @Nullable String property);

    /**
     * Hides the specified window.
     *
     * @param uid see {@link EventSource#uid()}
     */
    void hideWindow(int uid);

    /**
     * Returns the list of nodes (elements) with custom stylesheets, specifically those
     * for which {@link Parent#getStylesheets()} or {@link Control#getStylesheets()} is not empty.
     *
     * @param uid see {@link EventSource#uid()}
     */
    Map.@Nullable Entry<WindowProperties, List<Element>> getStyledElements(int uid);

    /**
     * Returns the {@link Application#getUserAgentStylesheet()} for the monitored application.
     */
    String getUserAgentStylesheet();

    /**
     * Reads and returns the content of the file resource at the specified URI.
     */
    @Nullable
    String getResource(int uid, String uri);

    /**
     * Returns the owner class name for the given property.
     * This method addresses the issue of finding the superclass that owns the property.
     */
    @Nullable
    String getDeclaringClass(String className, String property);
}
