package devtoolsfx.gui.inspector;

import devtoolsfx.gui.Preferences;
import devtoolsfx.scenegraph.Element;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
final class AttributeDetailsPane extends ScrollPane {

    private static final PseudoClass EMPTY = PseudoClass.getPseudoClass("empty");
    private static final String EMPTY_VALUE = "-";
    private static final int MIN_NAME_WIDTH = 100;

    private final AttributePane pane;

    private final Hyperlink propertyTitleLink = new Hyperlink("Property");
    private final Label propertyLabel = new Label();
    private final Tooltip propertyTooltip = new Tooltip();

    private final Hyperlink cssPropertyTitleLink = new Hyperlink("CSS property");
    private final Label cssPropertyLabel = new Label();
    private final Tooltip cssPropertyTooltip = new Tooltip();

    private final Label defaultLabel = new Label();

    AttributeDetailsPane(AttributePane pane) {
        super();

        this.pane = pane;

        setId("attribute-details-pane");
        createLayout();
        initListeners();
    }

    void setContent(@Nullable AttributeCellContent content) {
        if (content != null && content.getAttribute() != null) {
            var attr = content.getAttribute();

            propertyTitleLink.setText(attr.name());
            toggleDocLink(
                propertyTitleLink,
                propertyTooltip,
                createJavadocUri(Objects.requireNonNullElse(attr.field(), attr.name()))
            );
            propertyLabel.setText(defaultIfEmpty(String.valueOf(attr.value())));

            cssPropertyLabel.setText(defaultIfEmpty(attr.cssProperty()));
            toggleDocLink(cssPropertyTitleLink, cssPropertyTooltip, createCSSReferenceUri(attr.cssProperty()));

            defaultLabel.setText(String.valueOf(attr.valueState()));
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private void createLayout() {
        var grid = new GridPane();
        grid.getStyleClass().add("grid");
        grid.getColumnConstraints().setAll(
            new ColumnConstraints(MIN_NAME_WIDTH, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.NEVER, HPos.LEFT, false),
            new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, true)
        );

        propertyLabel.setWrapText(true);
        grid.add(propertyTitleLink, 0, 0);
        grid.add(propertyLabel, 1, 0);

        cssPropertyLabel.setWrapText(true);
        grid.add(cssPropertyTitleLink, 0, 1);
        grid.add(cssPropertyLabel, 1, 1);

        defaultLabel.setWrapText(true);
        grid.add(new Label("State"), 0, 2);
        grid.add(defaultLabel, 1, 2);

        setContent(grid);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setFitToWidth(true);
        setMaxHeight(10_000);
        setPrefHeight(200);
    }

    private void initListeners() {
        propertyTitleLink.setOnAction(e -> openDocLink(propertyTitleLink));
        cssPropertyTitleLink.setOnAction(e -> openDocLink(cssPropertyTitleLink));
    }

    private @Nullable String createJavadocUri(String field) {
        Element element = pane.getToolPane().getSelectedElement();
        if (element == null || !element.getClassInfo().module().startsWith("javafx.")) {
            return null;
        }

        String query = field;
        String declaringClassName = pane.getToolPane().getConnector().getDeclaringClass(
            element.getClassInfo().className(), field
        );

        if (declaringClassName != null) {
            query = declaringClassName + "." + field;
        }

        return Preferences.JAVADOC_SEARCH_URI + "?q=" + query;
    }

    private @Nullable String createCSSReferenceUri(@Nullable String cssProperty) {
        Element element = pane.getToolPane().getSelectedElement();
        if (element == null || cssProperty == null || !element.getClassInfo().module().startsWith("javafx.")) {
            return null;
        }

        return Preferences.CSS_REFERENCE_BASE_URI + "#" + element.getSimpleClassName().toLowerCase();
    }

    private void toggleDocLink(Hyperlink link, Tooltip tooltip, @Nullable String uri) {
        link.setUserData(uri);
        link.pseudoClassStateChanged(EMPTY, uri == null);

        tooltip.setText(uri);
        link.setTooltip(uri != null ? tooltip : null);
    }

    private void openDocLink(Hyperlink link) {
        if (link.getUserData() != null && link.getUserData() instanceof String uri) {
            pane.getToolPane().getPreferences().getHostServices().showDocument(uri);
        }
    }

    private String defaultIfEmpty(@Nullable String s) {
        return s != null && !s.isBlank() ? s : EMPTY_VALUE;
    }
}
