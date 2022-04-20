
/*
 *
 * NameDataSetDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

class NameDataSetDisplayer extends GraphElementDisplayer {

    static int maxY = 30;
    boolean showAll;

    NameDataSetDisplayer(GraphElementIDBuilder graphElementIDBuilder,
			 boolean showAll) {
	super("List", graphElementIDBuilder);
	this.showAll = showAll;
    }

    NameDataSetDisplayer(GraphElementIDBuilder graphElementIDBuilder) {
	this(graphElementIDBuilder, false);
    }

    NameDataSetDisplayer() {
	this(null, false);
    }

    NameDataSetDisplayer(boolean showAll) {
	this(null, showAll);
    }


    private String getName(GraphElement graphElement) {

	String name = graphElementIDBuilder.buildID(graphElement);

	if (showAll) {
	    TreeMap properties = graphElement.getProperties();
	    Iterator it = properties.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		if (prop != VAMPProperties.NameProp) {
		    name += " / " + (String)entry.getValue();
		}
	    }
	}
	return name;
    }

    public void computeVBounds(GraphCanvas canvas, Graphics2D g,
			       GraphElement graphElement, int m) {

	String name = getName(graphElement);
	if (name == null) return;

	Font font = VAMPResources.getFont(VAMPResources.NAME_DATASET_DISPLAYER_FONT);
	g.setFont(font);
	g.setColor(Color.GRAY);
	
	Dimension size = Utils.getSize(g, font, name);

	double vwidth = (size.width+5) / canvas.getScale().getScaleX();
	double vheight = (size.height+2) / canvas.getScale().getScaleY();

	Rectangle2D.Double vbounds = graphElement.getVBounds();

	graphElement.setVBounds(vbounds.x, vbounds.y,
				vwidth, vbounds.height);

	double y = vbounds.y - (vbounds.height - vheight)/2;
	graphElement.setPaintVBounds(vbounds.x, y,
				     vwidth, vheight);
    }

    public void display(GraphCanvas canvas, Graphics2D g,
			GraphElement graphElement,
			int m, PrintContext pctx) {
	if (g == null) return;
	Rectangle2D.Double vbounds = graphElement.getVBounds();

	double x0 = canvas.getRX(vbounds.x);
	double y0 = canvas.getRY(vbounds.y - vbounds.height/2);

	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    y0 = pctx.getRY(y0);
	}

	Font font = VAMPResources.getFont(VAMPResources.NAME_DATASET_DISPLAYER_FONT);
	g.setFont(font);
	g.setColor(Color.GRAY);
	
	String name = getName(graphElement);
	if (name != null) {
	    Dimension size = Utils.getSize(g, font, name);
	    g.drawString(name, (int)x0 + 2, (int)y0 + size.height/2);
	}
    }

    boolean checkGraphElements(LinkedList graphElements) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    String name = getName((GraphElement)graphElements.get(n));
	    if (name == null)
		return false;
	}

	return true;
    }

    public double getMaxY(GraphElement graphElement) {
	return maxY;
    }

    public double getMinY(GraphElement graphElement) {
	return 0;
    }

    double getRPadY() {return 0.;}

    Scale getDefaultScale(Dimension rdim, LinkedList graphElements) {
	int y0 = graphElements.size() * 14 ;
	int y1 = rdim.height - 30;
	double scaleY = (double)y0/(double)y1;
	if (scaleY < 0) scaleY = 0;
	return new Scale(0, scaleY);
    }

    boolean isCompatible(AxisDisplayer axisDisplayer) {
	return axisDisplayer instanceof NullAxisDisplayer;
    }
}
