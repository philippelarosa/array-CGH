
/*
 *
 * RSignalProperty.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

class RSignalProperty extends Property {

    boolean active;

    RSignalProperty() {
	super("RSignal", INFOABLE|SERIALIZABLE);
	active = true;
    }

    public void setGraphics(Graphics2D g, Object value, PropertyElement item,
			    GraphElement graphElement) {
	if (!active || g == null) return;
	double v;
	try {
	    double d = VAMPProperties.RSignalProp.toDouble(item);
	    ColorCodes cc = VAMPUtils.getColorCodes(graphElement);
	    if (cc != null)
		g.setColor(cc.getColor(d));
	}
	catch(java.lang.NumberFormatException e) {
	    System.out.println(e);
	}
    }

    boolean isActive() {return active;}
    void setActive(boolean active) {this.active = active;}
}

