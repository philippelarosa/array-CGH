
/*
 *
 * PrintHTMLArea.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;

class PrintHTMLArea extends PrintTextComponentArea {

    JEditorPane previewArea = null;
    boolean init = false;

    PrintHTMLArea(String name, String template, Rectangle2D.Double area) {
	this(name, template, area, Color.WHITE);
    }

    PrintHTMLArea(String name, String template, Rectangle2D.Double area,
		  Color bgColor) {
	super(name, template, area, bgColor);
    }

    void setComponents(JEditorPane previewArea) {
	this.previewArea = previewArea;
    }

    JEditorPane getPreviewArea() {return previewArea;}

    void setBGColor(Color bgColor) {
	super.setBGColor(bgColor);
	if (previewArea != null)
	    previewArea.setBackground(bgColor);
    }

    void move(int dx, int dy) {
	super.move(dx, dy);

	if (previewArea != null) {
	    Point loc = previewArea.getLocation();
	    previewArea.setLocation(loc.x + dx, loc.y + dy);
	}
    }

    void resize(int dw, int dh) {
	super.resize(dw, dh);
	if (previewArea != null) {
	    Dimension dim = previewArea.getSize();
	    previewArea.setSize(dim.width + dw, dim.height + dh);
	}
    }

    public Object clone() {
	PrintHTMLArea a = new PrintHTMLArea(name, template,
					    (Rectangle2D.Double)area.clone(),
					    bgColor);
	a.bdColor = bdColor;
	a.hasBorder = hasBorder;
	return a;
    }

    boolean isInitialized() {return init;}
    void setInitialized(boolean init) {this.init = init;}
}


