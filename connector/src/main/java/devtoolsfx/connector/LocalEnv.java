package devtoolsfx.connector;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@NullMarked
public final class LocalEnv implements Env {

    @Override
    public List<KeyValue> getSystemProperties() {
        return System.getProperties().entrySet().stream()
            .map(KeyValue::of)
            .toList();
    }

    @Override
    public List<KeyValue> getEnvVariables() {
        return System.getenv().entrySet().stream()
            .map(KeyValue::of)
            .toList();
    }

    @Override
    public List<KeyValue> getConditionalFeatures() {
        return Arrays.stream(ConditionalFeature.values())
            .map(cf -> new KeyValue(
                "ConditionalFeature." + cf.toString(),
                String.valueOf(Platform.isSupported(cf)))
            )
            .toList();
    }

    @Override
    public List<KeyValue> getPlatformPreferences() {
        var preferences = Platform.getPreferences();

        var staticPreferences = Stream.of(
            new KeyValue("Preferences.colorScheme", String.valueOf(preferences.getColorScheme())),
            new KeyValue("Preferences.accentColor", colorToHexString(preferences.getAccentColor())),
            new KeyValue("Preferences.backgroundColor", colorToHexString(preferences.getBackgroundColor())),
            new KeyValue("Preferences.foregroundColor", colorToHexString(preferences.getForegroundColor()))
        );

        var uiPreferences = preferences.entrySet().stream().map(entry -> {
            if (entry.getValue() instanceof Color color) {
                return new KeyValue(entry.getKey(), colorToHexString(color));
            }

            return KeyValue.of(entry);
        });

        return Stream.concat(staticPreferences, uiPreferences).toList();
    }

    @Override
    public List<KeyValue> getOtherPlatformProperties() {
        return List.of(
            new KeyValue("accessibilityActive", String.valueOf(Platform.isAccessibilityActive())),
            new KeyValue("implicitExit", String.valueOf(Platform.isImplicitExit())),
            new KeyValue("keyLocked.CAPS", unwrap(Platform.isKeyLocked(KeyCode.CAPS))),
            new KeyValue("keyLocked.NUM_LOCK", unwrap(Platform.isKeyLocked(KeyCode.NUM_LOCK)))
        );
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private String unwrap(Optional<Boolean> opt) {
        return String.valueOf(opt.isPresent() && opt.get());
    }

    private static String colorToHexString(@Nullable Color color) {
        if (color == null) {
            return "";
        }

        if (color.getOpacity() == 1) {
            return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
            ).toUpperCase();
        }

        return String.format("#%02X%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255),
            (int) (color.getOpacity() * 255)
        ).toUpperCase();
    }
}
