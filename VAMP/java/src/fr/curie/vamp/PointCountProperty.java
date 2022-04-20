
/*
 *
 * PointCountProperty.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

class PointCountProperty extends Property {

    PointCountProperty() {
	super("NbPoint", PropertyIntegerNAType.getInstance(), INFOABLE|SERIALIZABLE);
    }

    void setGraphics(Graphics2D g, PropertyElement item, Object propValue,
		     GraphElement graphElement) {
    }
}

