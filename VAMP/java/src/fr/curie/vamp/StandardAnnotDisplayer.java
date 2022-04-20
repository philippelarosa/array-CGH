
/*
 *
 * StandardAnnotDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

class StandardAnnotDisplayer extends AnnotDisplayer {

    static final int MARGIN = 5;
    static final int PAD = 5;
    static int MAX; // should be in resources
    static int WIDTH; // should be in resources

    static StandardAnnotDisplayer instance;
    private GraphCanvas canvas;
    private Vector annot_v;

    private StandardAnnotDisplayer() {
    }

    public void manageInfo(int x, int y) {
	if (canvas == null || annot_v == null)
	    return;

	int sz = annot_v.size();
	for (int n = 0; n < sz; n++) {
	    AnnotDesc adesc = (AnnotDesc)annot_v.get(n);
	    if (x >= adesc.x &&	x <= adesc.x + adesc.width &&
		y >= adesc.y &&	y <= adesc.y + adesc.height) {
		/*
		System.out.println("Found annot: " +
				   adesc.graphElem.getID() + ", " +
				   adesc.annot.getProperty().getName() + ", " + adesc.annot.getValue());
		*/
		JPanel panel = canvas.getView().getInfoDisplayer().display
		    (canvas.getView().getInfoPanel(),
		     adesc.graphElem,
		     adesc.annot.getProperty().getName(),
		     PropertyElement.getStringOP(adesc.annot.getOP()),
		     adesc.annot.getValue(),
		     adesc.annot.getColor());
		canvas.getView().getInfoPanel().update(panel);
	    }
	}
    }

    static StandardAnnotDisplayer getInstance() {
	if (instance == null)
	    instance = new StandardAnnotDisplayer();

	WIDTH = VAMPResources.getInt(VAMPResources.SAMPLE_ANNOT_WIDTH);
	MAX = VAMPResources.getInt(VAMPResources.SAMPLE_ANNOT_MAX);

	return instance;
    }
    
    public void displayAnnots(GraphCanvas canvas, AnnotAxis annotAxis,
			      Graphics2D g, GraphElement graphElement,
			      int m,
			      PrintContext pctx) {
	this.canvas = canvas;
	if (annotAxis != null)
	    annotAxis.setAnnotDisplayer(this);

	Rectangle2D.Double rbounds = graphElement.getRBounds();
	int height = (int)(rbounds.height);
	if (height > MAX)
	    height = MAX;
	int y = (int)(rbounds.y - rbounds.height + (rbounds.height-height)/2);
	int width = WIDTH;

	if (pctx != null) {
	    y = (int)pctx.getRY(y);
	    width = (int)pctx.getRW(WIDTH);
	    height = (int)pctx.getRH(height);
	    if (height > MAX) {
		y += (height - MAX)/2;
		height = MAX;
	    }
	}

	canvas.getGraphPanel().resetPropertyAnnot();
	Property filter_prop = canvas.getView().getAnnotDisplayFilterProp();
	TreeMap properties = graphElement.getProperties();
	Iterator it = properties.entrySet().iterator();
	View view = canvas.getView();

	for (int n = 0; it.hasNext(); ) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    if (prop.isEligible(view, filter_prop)) {
		PropertyAnnot annot = prop.getPropertyAnnot(view,
							    graphElement);
		int x = MARGIN + n*PAD + n*width;
		if (pctx != null)
		    x = (int)pctx.getRX(x);

		if (annot != null) {
		    g.setColor(annot.getColor(graphElement));
		    g.fillRect(x, y, width, height);
		    if (annot_v != null)
			annot_v.add(new AnnotDesc(annot, graphElement,
						  x, y, width, height));
		}

		if (prop.getAnnotations(canvas.getView()) != null) {
		    canvas.getGraphPanel().setPropertyAnnot(x, prop);
		    n++;
		}
	    }
	}

	canvas.getGraphPanel().repaintPropertyAnnot();
    }

    void init() {
	annot_v = new Vector();
    }

    private class AnnotDesc {
	GraphElement graphElem;
	PropertyAnnot annot;
	int x, y, width, height;

	AnnotDesc(PropertyAnnot annot, GraphElement graphElem,
		  int x, int y, int width, int height) {
	    this.annot = annot;
	    this.graphElem = graphElem;
	    this.x = x;
	    this.y = y;
	    this.width = width;
	    this.height = height;
	}
    }
}

