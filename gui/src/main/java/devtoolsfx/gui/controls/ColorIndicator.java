package devtoolsfx.gui.controls;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public final class ColorIndicator extends Rectangle {

    public ColorIndicator(Color color) {
        super();
        getStyleClass().add("color-indicator");
        setFill(color);
        setWidth(10);
        setHeight(10);
    }
}
