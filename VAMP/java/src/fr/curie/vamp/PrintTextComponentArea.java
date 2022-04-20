
/*
 *
 * PrintTextComponentArea.java
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

abstract class PrintTextComponentArea extends PrintArea {

    String template;

    protected PrintTextComponentArea(String name, String template,
				     Rectangle2D.Double area,
				     Color bgColor) {
	super(name, area, bgColor);
	this.template = template;
    }

    String getTemplate() {return template;}

    Rectangle2D.Double getArea() {
	return area;
    }

    void setTemplate(String template) {
	if (template != null && !this.template.equals(template))
	    pageTemplate.setModified();
	this.template = template;
    }
}


