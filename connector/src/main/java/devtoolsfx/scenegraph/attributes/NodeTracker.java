package devtoolsfx.scenegraph.attributes;

import devtoolsfx.event.EventBus;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.attributes.Attribute.DisplayHint;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.effect.BlendMode;
import javafx.scene.transform.Transform;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static devtoolsfx.scenegraph.attributes.Attribute.ObservableType;

/**
 * The {@link Tracker} implementation for the {@link Node} class.
 */
@NullMarked
public final class NodeTracker extends Tracker {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(
        "className", "pseudoClass", "styleClass", "stylesheets",
        "managed", "visible", "focusVisible", "focusWithin", "resizable",
        "layoutBounds", "boundsInParent", "baselineOffset", "layoutConstraints",
        "opacity", "viewOrder", "blendMode", "cursor", "effect", "clip", "rotate", "transforms",
        "layoutX", "layoutY", "scaleX", "scaleY", "scaleZ", "translateX", "translateY", "translateZ",
        "contentBias", "minWidth", "minHeight", "prefWidth", "prefHeight", "maxWidth", "maxHeight",
        "userAgentStylesheet", "userData"
    );
    public static final List<String> SUB_SCENE_PROPERTIES = List.of("userAgentStylesheet");

    private final ListChangeListener<Transform> transformListener = c -> reload("transforms");

    public NodeTracker(EventBus eventBus, EventSource eventSource) {
        super(eventBus, eventSource, AttributeCategory.NODE);
    }

    @Override
    public void reload(String... properties) {
        Node node = (Node) getTarget();
        if (node == null) {
            return;
        }

        var supportedProperties = new ArrayList<>(SUPPORTED_PROPERTIES);
        if (!(node instanceof SubScene)) {
            supportedProperties.removeAll(SUB_SCENE_PROPERTIES);
        }

        reload(property -> read(node, property), supportedProperties, properties);
    }

    @Override
    public void setTarget(@Nullable Object target) {
        Node old = (Node) getTarget();
        if (old != null) {
            old.getTransforms().removeListener(transformListener);
        }

        super.setTarget(target);

        if (target != null) {
            ((Node) target).getTransforms().addListener(transformListener);
        }
    }

    @Override
    public boolean accepts(@Nullable Object target) {
        return target instanceof Node;
    }

    @Override
    protected void beforeResetTarget(Object target) {
        ((Node) target).getTransforms().removeListener(transformListener);
    }

    ///////////////////////////////////////////////////////////////////////////

    private @Nullable Attribute<?> read(Node node, String property) {
        var dimensions = Dimensions.of(node);

        return switch (property) {
            case "className" -> new Attribute<>(
                "className",
                node.getClass().getName(),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.TEXT,
                ValueState.AUTO
            );
            case "pseudoClass" -> {
                String pseudoClass = node.getPseudoClassStates().stream()
                    .map(PseudoClass::getPseudoClassName)
                    .collect(Collectors.joining(" "));
                yield new Attribute<>(
                    "pseudoClass",
                    pseudoClass,
                    "getPseudoClassStates",
                    ObservableType.SET,
                    DisplayHint.TEXT,
                    ValueState.defaultIf(node.getPseudoClassStates().isEmpty())
                );
            }
            case "styleClass" -> {
                String styleClass = String.join(" ", node.getStyleClass());
                yield new Attribute<>(
                    "styleClass",
                    styleClass,
                    "getStyleClass",
                    ObservableType.LIST,
                    DisplayHint.TEXT,
                    ValueState.defaultIf(node.getStyleClass().isEmpty())
                );
            }
            case "visible" -> new Attribute<>(
                "visible",
                node.isVisible(),
                "visibleProperty",
                "visibility",
                ObservableType.of(node.visibleProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(node.isVisible())
            );
            case "managed" -> new Attribute<>(
                "managed",
                node.isManaged(),
                "managedProperty",
                "-fx-managed",
                ObservableType.of(node.managedProperty()),
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(node.isManaged()),
                List.of()
            );
            case "focusVisible" -> new Attribute<>(
                "focusVisible",
                node.isFocusVisible(),
                "focusVisibleProperty",
                ObservableType.READ_ONLY,
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(!node.isFocusVisible())
            );
            case "focusWithin" -> new Attribute<>(
                "focusWithin",
                node.isFocusWithin(),
                "focusWithinProperty",
                ObservableType.READ_ONLY,
                DisplayHint.BOOLEAN,
                ValueState.defaultIf(!node.isFocusWithin())
            );
            case "resizable" -> new Attribute<>(
                "resizable",
                node.isResizable(),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.BOOLEAN,
                ValueState.AUTO
            );
            case "layoutBounds" -> new Attribute<>(
                "layoutBounds",
                node.getLayoutBounds(),
                "layoutBoundsProperty",
                ObservableType.READ_ONLY,
                DisplayHint.BOUNDS,
                ValueState.AUTO
            );
            case "boundsInParent" -> new Attribute<>(
                "boundsInParent",
                node.getBoundsInParent(),
                "boundsInParentProperty",
                ObservableType.READ_ONLY,
                DisplayHint.BOUNDS,
                ValueState.AUTO
            );
            case "baselineOffset" -> new Attribute<>(
                "baselineOffset",
                node.getBaselineOffset(),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "layoutConstraints" -> {
                Map<String, String> properties = getLayoutConstraints(node);
                yield new Attribute<>(
                    "layoutConstraints",
                    properties,
                    null,
                    ObservableType.NOT_OBSERVABLE,
                    DisplayHint.PROPERTIES,
                    ValueState.AUTO
                );
            }
            case "opacity" -> new Attribute<>(
                "opacity",
                node.getOpacity(),
                "opacityProperty",
                "-fx-opacity",
                ObservableType.of(node.opacityProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(node.getOpacity() == 1.0),
                List.of(0.0, 1.0)
            );
            case "viewOrder" -> new Attribute<>(
                "viewOrder",
                node.getViewOrder(),
                "viewOrderProperty",
                "-fx-view-order",
                ObservableType.of(node.viewOrderProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(node.getViewOrder() == 0)
            );
            case "blendMode" -> new Attribute<>(
                "blendMode",
                node.getBlendMode(),
                "blendModeProperty",
                "-fx-blend-mode",
                ObservableType.of(node.blendModeProperty()),
                DisplayHint.ENUM,
                ValueState.defaultIf(node.getBlendMode() == null),
                List.of(BlendMode.values())
            );
            case "cursor" -> new Attribute<>(
                "cursor",
                node.getCursor() != null ? String.valueOf(node.getCursor()) : null,
                "cursorProperty",
                "-fx-cursor",
                ObservableType.of(node.cursorProperty()),
                DisplayHint.TEXT,
                ValueState.defaultIf(node.getCursor() == null)
            );
            case "effect" -> new Attribute<>(
                "effect",
                node.getEffect(),
                "effectProperty",
                "-fx-effect",
                ObservableType.of(node.effectProperty()),
                DisplayHint.EFFECT,
                ValueState.defaultIf(node.getEffect() == null)
            );
            case "clip" -> {
                var clip = node.getClip();
                yield new Attribute<>(
                    "clip",
                    clip != null ? new Clip(clip.getClass().getSimpleName(), clip.getBoundsInLocal()) : null,
                    "clipProperty",
                    ObservableType.of(node.clipProperty()),
                    DisplayHint.CLIP,
                    ValueState.defaultIf(node.getClip() == null)
                );
            }
            case "rotate" -> new Attribute<>(
                "rotate",
                node.getRotate(),
                "rotateProperty",
                "-fx-rotate",
                ObservableType.of(node.rotateProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(node.getRotate() == 0)
            );
            case "transforms" -> new Attribute<>(
                "transforms",
                Collections.unmodifiableList(node.getTransforms()),
                "getTransforms",
                ObservableType.LIST,
                DisplayHint.TRANSFORMS,
                ValueState.defaultIf(node.getTransforms().isEmpty())
            );
            case "layoutX" -> new Attribute<>(
                "layoutX",
                node.getLayoutX(),
                "layoutXProperty",
                ObservableType.of(node.layoutXProperty()),
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "layoutY" -> new Attribute<>(
                "layoutY",
                node.getLayoutY(),
                "layoutYProperty",
                ObservableType.of(node.layoutYProperty()),
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "scaleX" -> new Attribute<>(
                "scaleX",
                node.getScaleX(),
                "scaleXProperty",
                "-fx-scale-x",
                ObservableType.of(node.scaleXProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(node.getScaleX() == 1.0)
            );
            case "scaleY" -> new Attribute<>(
                "scaleY",
                node.getScaleY(),
                "scaleYProperty",
                "-fx-scale-y",
                ObservableType.of(node.scaleYProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(node.getScaleY() == 1.0)
            );
            case "scaleZ" -> new Attribute<>(
                "scaleZ",
                node.getScaleZ(),
                "scaleZProperty",
                "-fx-scale-z",
                ObservableType.of(node.scaleZProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(node.getScaleZ() == 1.0)
            );
            case "translateX" -> new Attribute<>(
                "translateX",
                node.getTranslateX(),
                "translateXProperty",
                "-fx-translate-x",
                ObservableType.of(node.translateXProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(node.getTranslateX() == 0)
            );
            case "translateY" -> new Attribute<>(
                "translateY",
                node.getTranslateY(),
                "translateYProperty",
                "-fx-translate-y",
                ObservableType.of(node.translateYProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(node.getTranslateY() == 0)
            );
            case "translateZ" -> new Attribute<>(
                "translateZ",
                node.getTranslateZ(),
                "translateZProperty",
                "-fx-translate-z",
                ObservableType.of(node.translateZProperty()),
                DisplayHint.NUMERIC,
                ValueState.defaultIf(node.getTranslateZ() == 0)
            );
            case "contentBias" -> new Attribute<>(
                "contentBias",
                node.getContentBias(),
                null,
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.ENUM,
                ValueState.defaultIf(node.getContentBias() == null),
                List.of(Orientation.values())
            );
            case "minWidth" -> new Attribute<>(
                "minWidth",
                dimensions.minWidth(),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "minHeight" -> new Attribute<>(
                "minHeight",
                dimensions.minHeight(),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "prefWidth" -> new Attribute<>(
                "prefWidth",
                dimensions.prefWidth(),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "prefHeight" -> new Attribute<>(
                "prefHeight",
                dimensions.prefHeight(),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "maxWidth" -> new Attribute<>(
                "maxWidth",
                dimensions.maxWidth(),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "maxHeight" -> new Attribute<>(
                "maxHeight",
                dimensions.maxHeight(),
                null,
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.NUMERIC,
                ValueState.AUTO
            );
            case "userData" -> new Attribute<>(
                "userData",
                String.valueOf(node.getUserData()),
                "userData",
                ObservableType.NOT_OBSERVABLE,
                DisplayHint.TEXT,
                ValueState.defaultIf(node.getUserData() == null)
            );
            // SubScene
            case "stylesheets" -> {
                if (node instanceof SubScene subScene) {
                    yield new Attribute<>(
                        "userAgentStylesheet",
                        subScene.getUserAgentStylesheet(),
                        "userAgentStylesheet",
                        ObservableType.of(subScene.userAgentStylesheetProperty()),
                        DisplayHint.TEXT,
                        ValueState.defaultIf(subScene.getUserAgentStylesheet() == null)
                    );
                } else {
                    yield null;
                }
            }
            default -> null;
        };
    }

    /**
     * Attempts to obtain layout constraints from the full node properties map.
     * See {@link Node#getProperties()} for more details.
     */
    private Map<String, String> getLayoutConstraints(Node node) {
        if (!node.hasProperties()) {
            return Map.of();
        }

        Map<String, String> properties = new TreeMap<>();
        for (var entry : node.getProperties().entrySet()) {
            if (entry.getKey() instanceof String key && (key.contains("pane-") || key.contains("box-"))) {
                var value = entry.getValue();
                if (key.endsWith("margin")) {
                    properties.put(key, insetsToString((Insets) value));
                } else {
                    properties.put(key, String.valueOf(value));
                }
            }
        }
        return properties;
    }

    private String insetsToString(Insets insets) {
        return insets.getTop() + " " + insets.getRight() + " " + insets.getBottom() + " " + insets.getLeft();
    }

    /**
     * Abstracts the size calculation logic based on the value of {@link Node#getContentBias()}.
     */
    private record Dimensions(double minWidth,
                              double minHeight,
                              double prefWidth,
                              double prefHeight,
                              double maxWidth,
                              double maxHeight) {

        public static Dimensions of(Node node) {
            double minWidth, minHeight, prefWidth, prefHeight, maxWidth, maxHeight;
            switch (node.getContentBias()) {
                case HORIZONTAL -> {
                    minWidth = node.minWidth(-1);
                    minHeight = node.minHeight(minWidth);
                    prefWidth = node.prefWidth(-1);
                    prefHeight = node.prefHeight(prefWidth);
                    maxWidth = node.maxWidth(-1);
                    maxHeight = node.maxHeight(maxWidth);
                }
                case VERTICAL -> {
                    minHeight = node.minHeight(-1);
                    minWidth = node.minWidth(minHeight);
                    prefHeight = node.prefHeight(-1);
                    prefWidth = node.prefWidth(prefHeight);
                    maxHeight = node.maxHeight(-1);
                    maxWidth = node.maxWidth(maxHeight);
                }
                case null -> {
                    minWidth = node.minWidth(-1);
                    minHeight = node.minHeight(-1);
                    prefWidth = node.prefWidth(-1);
                    prefHeight = node.prefHeight(-1);
                    maxWidth = node.maxWidth(-1);
                    maxHeight = node.maxHeight(-1);
                }
            }

            return new Dimensions(minWidth, minHeight, prefWidth, prefHeight, maxWidth, maxHeight);
        }
    }
}
