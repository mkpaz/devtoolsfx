package devtoolsfx.connector;

import java.util.List;

/**
 * Provides system information about the monitored JavaFX application,
 * including system properties, environment variables, platform preferences, and more.
 */
public interface Env {

    /**
     * Returns the list of system properties for the JavaFX JVM process.
     */
    List<KeyValue> getSystemProperties();

    /**
     * Returns the list of env variables for the JavaFX JVM process.
     */
    List<KeyValue> getEnvVariables();

    /**
     * Returns the list of conditional features for the monitored JavaFX application.
     */
    List<KeyValue> getConditionalFeatures();

    /**
     * Returns the list of platform preferences for the monitored JavaFX application.
     */
    List<KeyValue> getPlatformPreferences();

    /**
     * Returns the list of optional platform preferences for the monitored JavaFX application.
     */
    List<KeyValue> getOtherPlatformProperties();
}
