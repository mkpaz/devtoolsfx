package devtoolsfx.gui.style;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A wrapper for the stylesheet URI that provides additional information
 * and utility methods.
 */
record Stylesheet(String uri, boolean isUserAgentStylesheet) {

    public static final String DATA_URI_PREFIX = "data:base64,";

    boolean isDataURI() {
        return uri.startsWith(DATA_URI_PREFIX);
    }

    String decodeFromDataURI() {
        return new String(Base64.getDecoder().decode(
            uri.substring(DATA_URI_PREFIX.length()).getBytes(UTF_8)
        ), UTF_8);
    }
}
