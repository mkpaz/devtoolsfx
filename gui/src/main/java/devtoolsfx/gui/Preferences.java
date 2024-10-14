package devtoolsfx.gui;

import devtoolsfx.connector.Connector;
import devtoolsfx.connector.ConnectorOptions;
import devtoolsfx.connector.HighlightOptions;
import devtoolsfx.scenegraph.Element;
import javafx.application.HostServices;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import javafx.stage.PopupWindow;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class Preferences {

    public static final String JAVADOC_SEARCH_URI = "https://openjfx.io/javadoc/21/search.html";
    public static final String CSS_REFERENCE_BASE_URI = "https://openjfx.io/javadoc/22/javafx.graphics/javafx/scene/doc-files/cssref.html";
    public static final int MIN_EVENT_LOG_SIZE = 10;
    public static final int MAX_EVENT_LOG_SIZE = 10_000_000;
    public static final int DEFAULT_EVENT_LOG_SIZE = 10_000;
    public static final boolean KEEP_ATTRIBUTES_SORT = true;

    protected final BooleanProperty autoRefreshSceneGraph = new SimpleBooleanProperty(true);
    protected final BooleanProperty preventPopupAutoHide = new SimpleBooleanProperty(true);
    protected final BooleanProperty collapseControls = new SimpleBooleanProperty(true);
    protected final BooleanProperty collapsePanes = new SimpleBooleanProperty(false);
    protected final BooleanProperty showLayoutBounds = new SimpleBooleanProperty(true);
    protected final BooleanProperty showBoundsInParent = new SimpleBooleanProperty(true);
    protected final BooleanProperty showBaseline = new SimpleBooleanProperty(true);
    protected final BooleanProperty ignoreMouseTransparent = new SimpleBooleanProperty(false);
    protected final BooleanProperty enableEventLog = new SimpleBooleanProperty(false); // non-UI
    protected final IntegerProperty maxEventLogSize = new SimpleIntegerProperty(DEFAULT_EVENT_LOG_SIZE);
    protected final BooleanProperty darkMode = new SimpleBooleanProperty(false);

    protected final HostServices hostServices;

    public Preferences(HostServices hostServices) {
        this.hostServices = Objects.requireNonNull(hostServices, "hostServices must not be null");
    }

    /**
     * Returns the monitored application {@link HostServices}.
     * This is necessary to display a URI in a web browser while avoiding
     * a dependency on the {@code java.desktop} module.
     */
    public HostServices getHostServices() {
        return hostServices;
    }

    /**
     * Enables scene graph auto-refreshing.
     */
    public BooleanProperty autoRefreshSceneGraphProperty() {
        return autoRefreshSceneGraph;
    }

    public boolean isAutoRefreshSceneGraph() {
        return autoRefreshSceneGraph.get();
    }

    public void setAutoRefreshSceneGraph(boolean autoRefreshSceneGraph) {
        this.autoRefreshSceneGraph.set(autoRefreshSceneGraph);
    }

    /**
     * Disables the {@link PopupWindow#autoHideProperty()} when the popup window appears.
     * This allows inspection of the popup window content without accidentally hiding the window.
     */
    public BooleanProperty preventPopupAutoHideProperty() {
        return preventPopupAutoHide;
    }

    public boolean isPreventPopupAutoHide() {
        return preventPopupAutoHide.get();
    }

    public void setPreventPopupAutoHide(boolean preventPopupAutoHide) {
        this.preventPopupAutoHide.set(preventPopupAutoHide);
    }

    /**
     * Enables collapsing of JavaFX {@link Control} type nodes by default.
     */
    public BooleanProperty collapseControlsProperty() {
        return collapseControls;
    }

    public boolean isCollapseControls() {
        return collapseControls.get();
    }

    public void setCollapseControls(boolean collapseControls) {
        this.collapseControls.set(collapseControls);
    }

    /**
     * Enables collapsing of JavaFX {@link Pane} type nodes by default.
     */
    public BooleanProperty collapsePanesProperty() {
        return collapsePanes;
    }

    public boolean getCollapsePanes() {
        return collapsePanes.get();
    }

    public void setCollapsePanes(boolean collapsePanes) {
        this.collapsePanes.set(collapsePanes);
    }

    /**
     * Enables highlighting the layoutBounds when selecting the element.
     * See {@link Connector#selectNode(int, Element, HighlightOptions)}.
     */
    public BooleanProperty showLayoutBoundsProperty() {
        return showLayoutBounds;
    }

    public boolean isShowLayoutBounds() {
        return showLayoutBounds.get();
    }

    public void setShowLayoutBounds(boolean showLayoutBounds) {
        this.showLayoutBounds.set(showLayoutBounds);
    }

    /**
     * Enables highlighting the boundInParent when selecting the element.
     * See {@link Connector#selectNode(int, Element, HighlightOptions)}.
     */
    public BooleanProperty showBoundsInParentProperty() {
        return showBoundsInParent;
    }

    public boolean isShowBoundsInParent() {
        return showBoundsInParent.get();
    }

    public void setShowBoundsInParent(boolean showBoundsInParent) {
        this.showBoundsInParent.set(showBoundsInParent);
    }

    /**
     * Enables highlighting the baselineOffset when selecting the element.
     * See {@link Connector#selectNode(int, Element, HighlightOptions)}.
     */
    public BooleanProperty showBaselineProperty() {
        return showBaseline;
    }

    public boolean isShowBaseline() {
        return showBaseline.get();
    }

    public void setShowBaseline(boolean showBaseline) {
        this.showBaseline.set(showBaseline);
    }

    /**
     * See {@link ConnectorOptions#isIgnoreMouseTransparent()}.
     */
    public boolean isIgnoreMouseTransparent() {
        return ignoreMouseTransparent.get();
    }

    public BooleanProperty ignoreMouseTransparentProperty() {
        return ignoreMouseTransparent;
    }

    public void setIgnoreMouseTransparent(boolean ignoreMouseTransparent) {
        this.ignoreMouseTransparent.set(ignoreMouseTransparent);
    }

    /**
     * Enables or disables runtime event logging.
     */
    public BooleanProperty enableEventLogProperty() {
        return enableEventLog;
    }

    public boolean isEnableEventLog() {
        return enableEventLog.get();
    }

    public void setEnableEventLog(boolean enableEventLog) {
        this.enableEventLog.set(enableEventLog);
    }

    /**
     * Sets the maximum size of the event log.
     */
    public IntegerProperty maxEventLogSizeProperty() {
        return maxEventLogSize;
    }

    public int getMaxEventLogSize() {
        return maxEventLogSize.get();
    }

    public void setMaxEventLogSize(int size) {
        maxEventLogSize.set(size);
    }

    /**
     * Activates or deactivates dark mode for the dev tools UI.
     */
    public BooleanProperty darkModeProperty() {
        return darkMode;
    }

    public boolean getDarkMode() {
        return darkMode.get();
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode.set(darkMode);
    }

    @Override
    public String toString() {
        return "Preferences{" +
            "autoRefreshSceneGraph=" + autoRefreshSceneGraph +
            ", preventPopupAutoHide=" + preventPopupAutoHide +
            ", collapseControls=" + collapseControls +
            ", collapsePanes=" + collapsePanes +
            ", showLayoutBounds=" + showLayoutBounds +
            ", showBoundsInParent=" + showBoundsInParent +
            ", showBaseline=" + showBaseline +
            ", ignoreMouseTransparent=" + ignoreMouseTransparent +
            ", enableEventLog=" + enableEventLog +
            ", maxEventLogSize=" + maxEventLogSize +
            ", darkMode=" + darkMode +
            ", hostServices=" + hostServices +
            '}';
    }

    ///////////////////////////////////////////////////////////////////////////

    public HighlightOptions getHighlightOptions() {
        return new HighlightOptions(
            showLayoutBounds.get(),
            showBoundsInParent.get(),
            showBaseline.get()
        );
    }
}
