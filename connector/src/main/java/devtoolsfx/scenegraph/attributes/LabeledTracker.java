package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The {@link Tracker} implementation for the {@link Labeled} class.
 */
@NullMarked
public final class LabeledTracker extends Tracker {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "text", "font", "textFill", "graphic", "graphicTextGap", "labelPadding",
        "contentDisplay", "alignment", "textAlignment",
        "textOverrun", "wrapText", "underline", "ellipsisString"
    ); // label padding, text fill, ellipsis-string

    public LabeledTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.LABELED);
    }

    @Override
    public void reload(String... properties) {
        Labeled labeled = (Labeled) getTarget();
        if (labeled == null) {
            return;
        }

        reload(property -> read(labeled, property), SUPPORTED_PROPERTIES, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof Labeled;
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(Labeled label, String property) {
        return switch (property) {
            case "text" -> new Attribute<>(
                "text",
                label.getText(),
                "textProperty",
                ObservableType.of(label.textProperty()),
                DisplayHint.TEXT,
                ValueState.defaultIf(label.getText() == null || label.getText().isEmpty())
            );
            case "font" -> new Attribute<>(
                "font",
                label.getFont(),
                "fontProperty",
                "-fx-font",
                ObservableType.of(label.fontProperty()),
                DisplayHint.FONT,
                ValueState.defaultIf(label.getFont() == null)
            );
            case "textFill" -> new Attribute<>(
                "textFill",
                label.getTextFill(),
                "textFillProperty",
                "-fx-text-fill",
                ObservableType.of(label.textFillProperty()),
                DisplayHint.COLOR,
                ValueState.defaultIf(Color.BLACK.equals(label.getTextFill()))
            );
            case "graphic" -> new Attribute<>(
                "graphic",
                label.getGraphic() != null ? label.getGraphic().getClass().getSimpleName() : null,
                "graphicProperty",
                "-fx-graphic",
                ObservableType.of(label.graphicProperty()),
                DisplayHint.TEXT,
                ValueState.defaultIf(label.getGraphic() == null)
            );
            case "graphicTextGap" -> new Attribute<>(
                "graphicTextGap",
                label.getGraphicTextGap(),
                "graphicTextGapProperty",
                "-fx-graphic-text-gap",
                ObservableType.of(label.graphicTextGapProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(label.getGraphicTextGap() == 4)
            );
            case "labelPadding" -> new Attribute<>(
                "labelPadding",
                label.getLabelPadding(),
                "labelPaddingProperty",
                "-fx-label-padding",
                ObservableType.of(label.labelPaddingProperty()),
                DisplayHint.INSETS,
                ValueState.defaultIf(label.getLabelPadding() == null || Insets.EMPTY.equals(label.getLabelPadding()))
            );
            case "contentDisplay" -> new Attribute<>(
                "contentDisplay",
                label.getContentDisplay(),
                "contentDisplayProperty",
                "-fx-content-display",
                ObservableType.of(label.contentDisplayProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(label.getContentDisplay() == null || label.getContentDisplay() == ContentDisplay.LEFT),
                List.of(ContentDisplay.values())
            );
            case "alignment" -> new Attribute<>(
                "alignment",
                label.getAlignment(),
                "alignmentProperty",
                "-fx-alignment",
                ObservableType.of(label.alignmentProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(label.getAlignment() == null || label.getAlignment() == Pos.CENTER_LEFT),
                List.of(Pos.values())
            );
            case "textAlignment" -> new Attribute<>(
                "textAlignment",
                label.getTextAlignment(),
                "textAlignmentProperty",
                "-fx-text-alignment",
                ObservableType.of(label.textAlignmentProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(label.getTextAlignment() == null || label.getTextAlignment() == TextAlignment.LEFT),
                List.of(TextAlignment.values())
            );
            case "textOverrun" -> new Attribute<>(
                "textOverrun",
                label.getTextOverrun(),
                "textOverrunProperty",
                "-fx-text-overrun",
                ObservableType.of(label.textOverrunProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(label.getTextOverrun() == null || label.getTextOverrun() == OverrunStyle.ELLIPSIS),
                List.of(OverrunStyle.values())
            );
            case "wrapText" -> new Attribute<>(
                "wrapText",
                label.isWrapText(),
                "wrapTextProperty",
                "-fx-wrap-text",
                ObservableType.of(label.wrapTextProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(!label.isWrapText())
            );
            case "underline" -> new Attribute<>(
                "underline",
                label.isUnderline(),
                "underlineProperty",
                "-fx-underline",
                ObservableType.of(label.underlineProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(!label.isUnderline())
            );
            case "ellipsisString" -> new Attribute<>(
                "ellipsisString",
                label.getEllipsisString(),
                "ellipsisStringProperty",
                "-fx-ellipsis-string",
                ObservableType.of(label.ellipsisStringProperty()),
                DisplayHint.TEXT,
                ValueState.defaultIf("...".equals(label.getEllipsisString()))
            );
            default -> null;
        };
    }
}
