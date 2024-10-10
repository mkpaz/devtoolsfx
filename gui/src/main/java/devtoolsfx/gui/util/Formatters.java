package devtoolsfx.gui.util;

import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.NodeProperties;
import devtoolsfx.scenegraph.WindowProperties;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

@NullMarked
public final class Formatters {

    /**
     * Returns the HEX string representation of the specified color.
     */
    public static String colorToHexString(@Nullable Color color) {
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

    /**
     * Returns the RGBA string representation of the specified color.
     */
    public static String colorToRgbString(@Nullable Color color) {
        if (color == null) {
            return "";
        }

        if (color.getOpacity() == 1) {
            return "rgb(%d, %d, %d)".formatted(
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
            );
        }

        return "rgba(%d, %d, %d, %s)".formatted(
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255),
            color.getOpacity() % 1.0 != 0
                ? String.format("%s", color.getOpacity())
                : String.format("%.0f", color.getOpacity())
        );
    }

    /**
     * Returns the conventional representation of the element for display in the scene graph tree.
     */
    public static String formatForTreeItem(Element element) {
        var text = element.getSimpleClassName();

        if (element.isNodeElement()) {
            var props = element.getNodeProperties();
            if (props != null) {
                text = formatForTreeItem(element.getSimpleClassName(), props);
            }
        }

        if (element.isWindowElement()) {
            var props = element.getWindowProperties();
            if (props != null) {
                text = formatForTreeItem(element.getUID(), props);
            }
        }

        return text;
    }

    /**
     * See {@link #formatForTreeItem(Element)}.
     */
    public static String formatForTreeItem(String className, NodeProperties props) {
        var text = className;

        if (props.id() != null) {
            text += " " + asPropertyString("id", props.id());
        }

        if (!props.styleClass().isEmpty()) {
            text += " " + asPropertyString("class", String.join(" ", props.styleClass()));
        }

        return text;
    }

    /**
     * See {@link #formatForTreeItem(Element)}.
     */
    public static String formatForTreeItem(int uid, WindowProperties props) {
        String text;
        if (props.isPrimaryStage()) {
            text = "Primary Stage";
        } else {
            text = switch (props.windowType()) {
                case STAGE -> "Stage" + (
                    props.windowTitle() != null
                        ? " [" + asPropertyString("title", props.windowTitle()) + "]"
                        : "@" + uid
                );
                case MODAL -> "Modal" + (
                    props.windowTitle() != null
                        ? " [" + asPropertyString("title", props.windowTitle()) + "]"
                        : "@" + uid
                );
                case ALERT -> "Alert" + (
                    props.windowTitle() != null
                        ? " [" + asPropertyString("title", props.windowTitle()) + "]"
                        : "@" + uid
                );
                case POPUP -> "Popup" + (
                    props.ownerClassName() != null
                        ? " [" + asPropertyString("owner", props.ownerClassName())
                        : "@" + uid
                );
            };
        }
        return text;
    }

    /**
     * Returns a string in the format {@code key="value"}.
     */
    public static String asPropertyString(String key, String value) {
        return key + "=\"" + value + "\"";
    }

    /**
     * Trims the specified string to a maximum of {@code count} lines.
     * If the string exceeds this limit, it appends the {@code ellipsis} string.
     */
    public static String limitNumberOfLines(String s, int count, String ellipsis) {
        String[] lines = s.split("\n");

        if (lines.length <= count) {
            return s;
        } else {
            var sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(lines[i]);
                if (i < count - 1) {
                    sb.append("\n");
                }
            }
            sb.append(ellipsis);

            return sb.toString();
        }
    }

    /**
     * Returns the stack trace of the exception as a string.
     */
    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
