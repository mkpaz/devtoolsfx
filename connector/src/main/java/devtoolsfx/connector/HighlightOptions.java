package devtoolsfx.connector;

import org.jspecify.annotations.NullMarked;

/**
 * Contains the highlighting options to apply when selecting a node.
 */
@NullMarked
public record HighlightOptions(boolean showLayoutBounds,
                               boolean showBoundsInParent,
                               boolean showBaseline) {

    /**
     * Returns default {@link HighlightOptions}.
     */
    public static HighlightOptions defaults() {
        return new HighlightOptions(true, true, false);
    }
}
