package devtoolsfx.scenegraph;

/**
 * Represents the short information about an arbitrary {@link Class} name.
 */
public record ClassInfo(String module,
                        String className,
                        String simpleClassName) {
}
