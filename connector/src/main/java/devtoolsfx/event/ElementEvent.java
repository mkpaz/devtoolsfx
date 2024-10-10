package devtoolsfx.event;

import devtoolsfx.scenegraph.Element;

/**
 * Signifies that the event has been triggered by an element and provides access to that element.
 */
public interface ElementEvent {

    Element getElement();
}
