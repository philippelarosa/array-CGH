
/*
 *
 * NullAxisDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

class NullAxisDisplayer extends AxisDisplayer {

    static NullAxisDisplayer instance;

    NullAxisDisplayer() {
	super("Array", "Null", null);
    }

    public static NullAxisDisplayer getInstance() {
	if (instance == null)
	    instance = new NullAxisDisplayer();
	return instance;
    }

    public void displayXAxis(GraphCanvas canvas, Axis xaxis, Graphics2D g,
			     GraphElement graphElement, int m, PrintContext pctx) {
    }

    public void displayYAxis(GraphCanvas canvas, Axis yaxis, Graphics2D g,
			     GraphElement graphElement, int m, PrintContext pctx) {
    }

    public double getMaxX(GraphElement graphElement) {return 1;}
    public double getMaxY(GraphElement graphElement) {return 1;}
    public double getMinX(GraphElement graphElement) {return 0;}
    public double getMinY(GraphElement graphElement) {return 0;}
}
