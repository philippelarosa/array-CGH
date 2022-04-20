
/*
 *
 * XDendrogramAxisDisplayer.java
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

class XDendrogramAxisDisplayer extends AxisDisplayer {

    static final int W_OFFSET_R = 10;
    static final int W_OFFSET_L = 12;

    XDendrogramAxisDisplayer() {
	super("Dendrogram", "XDendrogram", DendrogramIDBuilder.getInstance());
    }

    public void displayXAxis(GraphCanvas canvas, Axis xaxis,
			     Graphics2D g, GraphElement graphElement,
			     int m,
			     PrintContext pctx) {
	if (true)
	    return;

	if (xaxis == null)
	    return;

	Rectangle2D.Double vb = graphElement.getVBounds();

	double y0 = canvas.getRY(vb.y - vb.height/2);
	if (pctx != null)
	    y0 = pctx.getRY(y0);

	String s = graphElementIDBuilder.buildID(graphElement);
	int w2 = xaxis.getSize().width/2;
	Font font = VAMPResources.getFont(VAMPResources.AXIS_Y_NAME_DISPLAYER_FONT);
	g.setFont(font);
	g.setColor(VAMPResources.getColor
		   (VAMPResources.AXIS_Y_PROPERTY_NAME_FG));

	if (s != null) {
	    //System.out.println("DendrogramAxis.displayXaxis " + s + " -> " + vb + ", y0=" + y0);
	    Dimension d = Utils.getSize(g, s);
	    g.drawString(s, w2-d.width/2, (int)y0+2);
	    canvas.setWestYSize(d.width + 10, false);
	}
    }

    public void displayYAxis(GraphCanvas canvas, Axis yaxis,
			     Graphics2D g, GraphElement graphElement,
			     int m,
			     PrintContext pctx) {
	if (yaxis == null && pctx == null)
	    return;

	if (pctx != null && (pctx.getFlags() & PrintContext.Y_AXIS) == 0)
	    return;

	// 13/12/04 dot not work any more
	DendrogramGraphElement dendroGE =
	    graphElement.asDendrogramGraphElement();

	/*
	if (dendroGE == null || !dendroGE.isLeaf())
	    return;

	DendrogramLeaf dendro_leaf = (DendrogramLeaf)dendroGE.getDendrogramNode();
	*/

	if (dendroGE == null)
	    return;

	DendrogramNode dendro_node = dendroGE.getDendrogramNode();

	double w2;
	if (yaxis == null)
	    w2 = canvas.getRW(graphElement.getVBounds().width) / 10;
	else
	    w2 = yaxis.getSize().width/2;
	double w2_r = w2 + W_OFFSET_R;

	double minY = canvas.getMinY();
	double maxY = canvas.getMaxY();
	double minRY = canvas.getRY(minY);
	double maxRY = canvas.getRY(maxY);
	if (pctx != null) {
	    w2 = pctx.getRX(w2);
	    w2_r = pctx.getRX(w2_r);
	    minRY = pctx.getRY(minRY);
	    maxRY = pctx.getRY(maxRY);
	}

	g.setColor(Color.BLACK);
	g.drawLine((int)w2_r, (int)minRY, (int)w2_r, (int)maxRY);

	Dendrogram dendro = dendro_node.getDendrogram();
	double height = dendro.getMaxHeight();
	double height8 = height/8;
	Font font = VAMPResources.getFont(VAMPResources.AXIS_X_DISPLAYER_FONT);
	g.setFont(font);

	FontRenderContext frc = g.getFontRenderContext();
	double ascent = font.getLineMetrics("012345689", frc).getAscent();
	double descent = font.getLineMetrics("012345689", frc).getDescent();

	Dimension2DDouble vSize = canvas.getVirtualSize();

	for (double d = 0; d <= height; d += height8) {
	    double y = canvas.getRY(vSize.height - d);
	    if (pctx != null)
		y = pctx.getRY(y);
	    String s = toString(d);
	    Dimension dim = Utils.getSize(g, s);
	    //	    g.drawLine((int)x, h2-2, (int)x, h2+2);
	    //g.drawString(s, (int)x-dim.width/2, (int)(h2-3-descent));
	    g.drawLine((int)w2_r - 2, (int)y, (int)w2_r + 2, (int)y);
	    g.drawString(s, (int)(w2 - W_OFFSET_L), (int)(y+descent+2));
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
