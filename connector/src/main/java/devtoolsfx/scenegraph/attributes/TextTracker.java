package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.geometry.VPos;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The {@link Tracker} implementation for the {@link Text} class.
 */
@NullMarked
public final class TextTracker extends Tracker {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "text", "font", "textOrigin", "x", "y", "textAlignment", "boundsType",
        "tabSize", "lineSpacing", "wrappingWidth", "underline", "strikethrough", "fontSmoothingType"
    );

    public TextTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.TEXT);
    }

    @Override
    public void reload(String... properties) {
        Text text = (Text) getTarget();
        if (text == null) {
            return;
        }

        reload(property -> read(text, property), SUPPORTED_PROPERTIES, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof Text;
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(Text text, String property) {
        return switch (property) {
            case "text" -> new Attribute<>(
                "text",
                text.getText(),
                "textProperty",
                ObservableType.of(text.textProperty()),
                DisplayHint.TEXT,
                ValueState.defaultIf(text.getText() == null)
            );
            case "font" -> new Attribute<>(
                "font",
                text.getFont(),
                "fontProperty",
                "-fx-font",
                ObservableType.of(text.fontProperty()),
                DisplayHint.FONT,
                ValueState.defaultIf(text.getFont() == null)
            );
            case "textOrigin" -> new Attribute<>(
                "textOrigin",
                text.getTextOrigin(),
                "textOriginProperty",
                "-fx-text-origin",
                ObservableType.of(text.textOriginProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(text.getTextOrigin() == null),
                List.of(VPos.values())
            );
            case "x" -> new Attribute<>(
                "x",
                text.getX(),
                "xProperty",
                ObservableType.of(text.xProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(text.getX() == 0)
            );
            case "y" -> new Attribute<>(
                "y",
                text.getY(),
                "yProperty",
                ObservableType.of(text.yProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(text.getY() == 0)
            );
            case "textAlignment" -> new Attribute<>(
                "textAlignment",
                text.getTextAlignment(),
                "textAlignmentProperty",
                "-fx-text-alignment",
                ObservableType.of(text.textAlignmentProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(text.getTextAlignment() == TextAlignment.LEFT)
            );
            case "boundsType" -> new Attribute<>(
                "boundsType",
                text.getBoundsType(),
                "boundsTypeProperty",
                "-fx-bounds-type",
                ObservableType.of(text.boundsTypeProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(text.getBoundsType() == TextBoundsType.LOGICAL),
                List.of(TextBoundsType.values())
            );
            case "tabSize" -> new Attribute<>(
                "tabSize",
                text.getTabSize(),
                "tabSizeProperty",
                "-fx-tab-size",
                ObservableType.of(text.tabSizeProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(text.getTabSize() == 8)
            );
            case "lineSpacing" -> new Attribute<>(
                "lineSpacing",
                text.getLineSpacing(),
                "lineSpacingProperty",
                "-fx-line-spacing",
                ObservableType.of(text.lineSpacingProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(text.getLineSpacing() == 0)
            );
            case "wrappingWidth" -> new Attribute<>(
                "wrappingWidth",
                text.getWrappingWidth(),
                "wrappingWidthProperty",
                ObservableType.of(text.wrappingWidthProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(text.getWrappingWidth() == 0)
            );
            case "underline" -> new Attribute<>(
                "underline",
                text.isUnderline(),
                "underlineProperty",
                "-fx-underline",
                ObservableType.of(text.underlineProperty()),
                DisplayHint.BOOLEAN,
                Attribute.ValueState.defaultIf(!text.isUnderline())
            );
            case "strikethrough" -> new Attribute<>(
                "strikethrough",
                text.isStrikethrough(),
                "strikethroughProperty",
                "-fx-strikethrough",
                ObservableType.of(text.strikethroughProperty()),
                DisplayHint.BOOLEAN,
                Attribute.ValueState.defaultIf(!text.isStrikethrough())
            );
            case "fontSmoothingType" -> new Attribute<>(
                "fontSmoothingType",
                text.getFontSmoothingType(),
                "fontSmoothingTypeProperty",
                "-fx-font-smoothing-type",
                ObservableType.of(text.fontSmoothingTypeProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(text.getFontSmoothingType() == FontSmoothingType.GRAY),
                List.of(FontSmoothingType.values())
            );
            default -> null;
        };
    }
}
