package devtoolsfx.event;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Notifies about an exception that occurred during monitoring.
 *
 * @param eventSource the source of the event
 * @param className   the name of the exception class
 * @param stackTrace  the stack trace of the exception
 * @param message     the exception message
 */
@NullMarked
public record ExceptionEvent(EventSource eventSource,
                             String className,
                             String stackTrace,
                             @Nullable String message) implements ConnectorEvent {

    private static final StringWriter STRING_WRITER = new StringWriter();
    private static final PrintWriter PRINT_WRITER = new PrintWriter(STRING_WRITER);

    @Override
    public String toLogString() {
        return "source=" + eventSource.toLogString()
            + " | class=" + className
            + " | message=" + message;
    }

    public static ExceptionEvent of(EventSource eventSource, Exception exception) {
        PRINT_WRITER.flush();
        exception.printStackTrace(PRINT_WRITER);

        return new ExceptionEvent(
            eventSource,
            exception.getClass().getSimpleName(),
            STRING_WRITER.toString(),
            exception.getMessage()
        );
    }
}
