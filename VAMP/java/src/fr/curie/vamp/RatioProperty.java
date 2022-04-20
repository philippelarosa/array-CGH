
/*
 *
 * RatioProperty.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

public class RatioProperty extends Property {

    boolean active;

    RatioProperty(String name) {
	super(name, PropertyFloatNAType.getInstance(),
	      INFOABLE|EDITABLE|SERIALIZABLE);
	active = true;
    }

    RatioProperty(String name, boolean hidden) {
	super(name, PropertyFloatNAType.getInstance(),
	      (hidden ? 0 : INFOABLE|EDITABLE));
	active = true;
    }

    RatioProperty() {
	this("Ratio");
    }

    public void setGraphics(Graphics2D g, Object value, PropertyElement item,
			    GraphElement graphElement) {
	if (!active || g == null) {
	    return;
	}
	double v;
	try {
	    String ratio = (String)value;
	    if (!ratio.equals(VAMPProperties.NA)) {
		v = Utils.parseDouble(ratio);
		ColorCodes cc = VAMPUtils.getColorCodes(graphElement);
		if (cc != null)
		    g.setColor(cc.getColor(v));
	    }
	}
	catch(java.lang.NumberFormatException e) {
	    System.out.println(e);
	}
    }

    public void setGraphics(Graphics2D g, float ratio, PropertyElement item,
			    GraphElement graphElement) {
	if (!active || g == null) {
	    return;
	}
	double v;
	try {
	    ColorCodes cc = VAMPUtils.getColorCodes(graphElement);
	    if (cc != null) {
		g.setColor(cc.getColor(ratio));
	    }
	}
	catch(java.lang.NumberFormatException e) {
	    System.out.println(e);
	}
    }

    boolean isActive() {return active;}
    void setActive(boolean active) {this.active = active;}
}

