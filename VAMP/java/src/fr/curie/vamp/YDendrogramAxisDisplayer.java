
/*
 *
 * YDendrogramAxisDisplayer.java
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
import java.util.*;

class YDendrogramAxisDisplayer extends AxisDisplayer {

    YDendrogramAxisDisplayer() {
	super("Dendrogram", "YDendrogram", DendrogramIDBuilder.getInstance());
    }

    public void displayXAxis(GraphCanvas canvas, Axis xaxis,
			     Graphics2D g, GraphElement graphElement,
			     int m,
			     PrintContext pctx) {
	if (xaxis == null && pctx == null)
	    return;

	if (pctx != null && (pctx.getFlags() & PrintContext.X_AXIS) == 0)
	    return;

	// 13/12/04 dot not work any more
	DendrogramGraphElement dendroGE =
	    graphElement.asDendrogramGraphElement();

	if (dendroGE == null || !dendroGE.isLeaf())
	    return;

	DendrogramLeaf dendro_leaf = (DendrogramLeaf)dendroGE.getDendrogramNode();

	double h2;
	if (xaxis == null)
	    h2 = pctx.getArea().getSize().height/2;
	else
	    h2 = xaxis.getSize().height/2;

	double minX = canvas.getMinX();
	double maxX = canvas.getMaxX();
	double minRX = canvas.getRX(minX);
	double maxRX = canvas.getRX(maxX);
	if (pctx != null) {
	    h2 = pctx.getRY(h2);
	    minRX = pctx.getRX(minRX);	    
	    maxRX = pctx.getRX(maxRX);
	}

	int h2i = (int)h2;
	g.setColor(Color.BLACK);
	g.drawLine((int)minRX, h2i, (int)maxRX, h2i);

	Dendrogram dendro = dendro_leaf.getDendrogram();
	double height = dendro.getMaxHeight();
	if (height != 0) {
	    double height8 = height/8;
	    Font font = VAMPResources.getFont(VAMPResources.AXIS_X_DISPLAYER_FONT);
	    g.setFont(font);

	    FontRenderContext frc = g.getFontRenderContext();
	    double descent = font.getLineMetrics("012345689", frc).getDescent();

	    for (double d = 0; d <= height; d += height8) {
		double x = canvas.getRX(d);
		if (pctx != null)
		    x = pctx.getRX(x);
		String s = toString(d);
		Dimension dim = Utils.getSize(g, s);
		g.drawLine((int)x, h2i-2, (int)x, h2i+2);
		g.drawString(s, (int)x-dim.width/2, (int)(h2i-3-descent));
	    }
	}
    }

    public void displayYAxis(GraphCanvas canvas, Axis yaxis,
			     Graphics2D g, GraphElement graphElement,
			     int m,
			     PrintContext pctx) {
	if (yaxis == null)
	    return;

	Rectangle2D.Double vb = graphElement.getVBounds();

	double y0 = canvas.getRY(vb.y - vb.height/2);
	if (pctx != null)
	    y0 = pctx.getRY(y0);

	String s = graphElementIDBuilder.buildID(graphElement);
	int w2 = yaxis.getSize().width/2;
	Font font = VAMPResources.getFont(VAMPResources.AXIS_Y_NAME_DISPLAYER_FONT);
	g.setFont(font);
	g.setColor(VAMPResources.getColor
		   (VAMPResources.AXIS_Y_PROPERTY_NAME_FG));

	if (s != null) {
	    //System.out.println("DendrogramAxis.displayYAxis " + s + " -> " + vb + ", y0=" + y0);
	    Dimension d = Utils.getSize(g, s);
	    g.drawString(s, w2-d.width/2, (int)y0+2);
	    canvas.setWestYSize(d.width + 10, false);
	}
    }

    public double getMinX(GraphElement graphElement) {
	return 0;
    }

    public double getMaxX(GraphElement graphElement) {
	DendrogramGraphElement dendroGE =
	    graphElement.asDendrogramGraphElement();
	if (dendroGE == null) return 0.;
	return dendroGE.getDendrogramNode().getDendrogram().getMaxHeight();
	/*
	DendrogramLeaf dendro_leaf =
	    (DendrogramLeaf)graphElement.getPropertyValue
	    (Dendrogram.DendrogramLeafProp);
	if (dendro_leaf == null) return 0;
	return dendro_leaf.getDendrogram().getMaxHeight();
	*/
    }

    public double getMinY(GraphElement graphElement) {
	return 0;
    }

    public double getMaxY(GraphElement graphElement) {
	/*
	DendrogramGraphElement dendroGE =
	    graphElement.asDendrogramGraphElement();
	if (dendroGE == null) return 0.;
	return dendroGE.getDendrogramNode().getDendrogram().getLeaves().size();
	*/
	return 1.;
	/*
	DendrogramGraphElement dendroGE =
	    graphElement.asDendrogramGraphElement();
	if (dendroGE.isLeaf())
	    return 2.8; // TO TRY !!!
	return 0;
	*/
    }

    private String toString(double d) {
	String s = Utils.toString(d);
	int idx = s.lastIndexOf('.');
	if (idx < 0)
	    return s;

	if (s.length() - idx > 3)
	    return s.substring(0, idx+3);
	return s;
    }
}
