package devtoolsfx.event;

import org.jspecify.annotations.NullMarked;

/**
 * Refers to the source window that emitted an event.
 *
 * @param application    the name of the application
 * @param uid            the ID of the window (stage)
 * @param isPrimaryStage true if the source window is the primary stage; false otherwise
 */
@NullMarked
public record EventSource(String application, int uid, boolean isPrimaryStage) {

    public String toLogString() {
        return application + "#" + uid;
    }
}
