package devtoolsfx.gui.util;

import devtoolsfx.scenegraph.ClassInfo;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.NodeProperties;
import devtoolsfx.scenegraph.WindowProperties;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Dummy class to represent the root of a scene graph element tree.
 */
@NullMarked
public final class DummyElement implements Element {

    private final String name;

    public DummyElement(String name) {
        this.name = name;
    }

    @Override
    public int getUID() {
        return hashCode();
    }

    @Override
    public ClassInfo getClassInfo() {
        return new ClassInfo("", "", name);
    }

    @Override
    public String getSimpleClassName() {
        return name;
    }

    @Override
    public @Nullable Element getParent() {
        return null;
    }

    @Override
    public List<Element> getChildren() {
        return List.of();
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public @Nullable NodeProperties getNodeProperties() {
        return null;
    }

    @Override
    public @Nullable WindowProperties getWindowProperties() {
        return null;
    }

    @Override
    public boolean isWindowElement() {
        return false;
    }

    @Override
    public boolean isNodeElement() {
        return false;
    }
}
