package devtoolsfx.util;

import devtoolsfx.scenegraph.ClassInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassInfoCache {

    private static final Map<Class<?>, ClassInfo> cache = new ConcurrentHashMap<>();

    /**
     * Returns the simple class name for the given object/
     */
    public static ClassInfo get(Object obj) {
        ClassInfo info = cache.get(obj.getClass());
        if (info == null) {
            Class<?> cls = obj.getClass();
            String className = cls.getName();
            String simpleName = cls.getSimpleName();

            // getSimpleName() for anonymous classes in Scala does not return empty string,
            // instead it will contain some encoded name
            while (simpleName.isEmpty() || simpleName.contains("anon$")) {
                cls = cls.getSuperclass();
                className = cls.getName();
                simpleName = cls.getSimpleName();
            }

            info = new ClassInfo(cls.getModule().getName(), className, simpleName);

            cache.put(obj.getClass(), info);
            return info;
        }

        return info;
    }
}
