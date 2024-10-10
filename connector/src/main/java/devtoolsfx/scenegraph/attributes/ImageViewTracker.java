package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ObservableType;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * The {@link Tracker} implementation for the {@link ImageView} class.
 */
@NullMarked
public final class ImageViewTracker extends Tracker {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "fitWidth", "fitHeight", "image", "preserveRatio", "smooth"
    );

    public ImageViewTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.IMAGE_VIEW);
    }

    @Override
    public void reload(String... properties) {
        ImageView imageView = (ImageView) getTarget();
        if (imageView == null) {
            return;
        }

        reload(property -> read(imageView, property), SUPPORTED_PROPERTIES, properties);
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof ImageView;
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(ImageView imageView, String property) {
        return switch (property) {
            case "fitWidth" -> new Attribute<>(
                "fitWidth",
                imageView.getFitWidth(),
                "fitWidthProperty",
                "-fx-fit-width",
                ObservableType.of(imageView.fitWidthProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(imageView.getFitWidth() == 0)
            );
            case "fitHeight" -> new Attribute<>(
                "fitHeight",
                imageView.getFitHeight(),
                "fitHeightProperty",
                "-fx-fit-height",
                ObservableType.of(imageView.fitHeightProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(imageView.getFitWidth() == 0)
            );
            case "image" -> {
                var image = imageView.getImage();
                yield new Attribute<>(
                    "image",
                    image != null
                        ? Objects.requireNonNullElse(image.getUrl(), String.valueOf(image))
                        : null,
                    "imageProperty",
                    "-fx-image",
                    ObservableType.of(imageView.imageProperty()),
                    DisplayHint.TEXT,
                    ValueState.defaultIf(image == null)
                );
            }
            case "preserveRatio" -> new Attribute<>(
                "preserveRatio",
                imageView.isPreserveRatio(),
                "preserveRatioProperty",
                "-fx-preserve-ratio",
                ObservableType.of(imageView.preserveRatioProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(!imageView.isPreserveRatio())
            );
            case "smooth" -> new Attribute<>(
                "smooth",
                imageView.isSmooth(),
                "smoothProperty",
                "-fx-smooth",
                ObservableType.of(imageView.smoothProperty()),
                DisplayHint.BOOLEAN,
                ValueState.AUTO // platform-dependent, no API
            );
            default -> null;
        };
    }
}
