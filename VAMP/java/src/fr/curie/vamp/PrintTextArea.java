
/*
 *
 * PrintTextArea.java
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

class PrintTextArea extends PrintTextComponentArea {

    Font font;
    Color fgColor;

    PrintTextArea(String name, String template, Rectangle2D.Double area) {
	this(name, template, area, Color.WHITE, null);
    }

    PrintTextArea(String name, String template, Rectangle2D.Double area,
		  Color bgColor) {
	this(name, template, area, bgColor, null);
    }

    PrintTextArea(String name, String template, Rectangle2D.Double area,
		  Font font) {
	this(name, template, area, Color.WHITE, font);
    }

    PrintTextArea(String name, String template, Rectangle2D.Double area,
		  Color bgColor, Font font) {
	super(name, template, area, bgColor);
	this.font = font;
	this.fgColor = Color.BLACK;
    }

    Color getFGColor() {return fgColor;}
    void setFGColor(Color fgColor) {this.fgColor = fgColor;}

    Font getFont() {return font;}
    void setFont(Font font) {this.font = font;}

    public Object clone() {
	PrintTextArea a = new PrintTextArea(name, template,
					    (Rectangle2D.Double)area.clone(),
					    bgColor, font);
	a.bdColor = bdColor;
	a.fgColor = fgColor;
	a.hasBorder = hasBorder;
	return a;
    }
}


