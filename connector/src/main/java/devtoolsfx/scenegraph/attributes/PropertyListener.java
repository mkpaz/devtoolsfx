package devtoolsfx.scenegraph.attributes;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import org.jspecify.annotations.NullMarked;

import java.lang.System.Logger.Level;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The property listener accepts the target node, reflectively scans all its
 * methods that return observable properties (which must end with the 'Property' suffix),
 * and listens for changes to all found properties.
 * <p>
 * When a change is detected, the {@link #onPropertyChanged} method is called. To use this class,
 * the client should implement this abstract method and include the desired logic within it.
 */
@NullMarked
public abstract class PropertyListener {

    private static final System.Logger LOGGER = System.getLogger(PropertyListener.class.getName());
    public static final String PROPERTY_SUFFIX = "Property";

    private final Map<ObservableValue<?>, String> properties = new HashMap<>();
    private final InvalidationListener propertyListener = obs ->
        onPropertyChanged(properties.get((ObservableValue<?>) obs), (ObservableValue<?>) obs);

    public PropertyListener() {
        // pass
    }

    /**
     * This method is called when an observable property change is detected.
     */
    protected abstract void onPropertyChanged(String propertyName, ObservableValue<?> obs);

    /**
     * Returns the map of observable properties that have been found and are being tracked.
     */
    public Map<ObservableValue<?>, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Sets the target to be tracked for property changes.
     */
    public void use(Object target) throws InvocationTargetException, IllegalAccessException {
        properties.clear();

        // using reflection, locate all properties and their corresponding property references
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().endsWith(PROPERTY_SUFFIX)) {
                continue;
            }

            Class<?> returnType = method.getReturnType();
            if (ObservableValue.class.isAssignableFrom(returnType)) {
                // suppress InaccessibleObjectException on classes not exported from javafx-controls module
                try {
                    method.setAccessible(true);
                } catch (InaccessibleObjectException e) {
                    LOGGER.log(Level.INFO, e.getMessage());
                }

                ObservableValue<?> property = (ObservableValue<?>) method.invoke(target);

                String propertyName = method.getName().substring(0, method.getName().lastIndexOf(PROPERTY_SUFFIX));
                properties.put(property, propertyName);
            }
        }

        for (ObservableValue<?> obs : properties.keySet()) {
            obs.addListener(propertyListener);
        }
    }

    /**
     * Stops the listener from tracking property changes.
     */
    public void release() {
        for (ObservableValue<?> obs : properties.keySet()) {
            obs.removeListener(propertyListener);
        }
        properties.clear();
    }
}
