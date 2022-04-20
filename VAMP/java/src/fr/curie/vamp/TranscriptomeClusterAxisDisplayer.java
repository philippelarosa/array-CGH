
/*
 *
 * TranscriptomeClusterAxisDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;
import java.awt.font.*;
import java.awt.geom.*;

class TranscriptomeClusterAxisDisplayer extends AxisDisplayer {

    protected int vSize = 3;

    static private final double angle = Math.PI/2;
    static private final int MINIMUM_SIZE = 8;

    TranscriptomeClusterAxisDisplayer() {
	super("Transcriptome", "Cluster", null);
    }

    public void displayXAxis(GraphCanvas canvas, Axis xaxis, Graphics2D g,
			     GraphElement graphElement, int m,
			     PrintContext pctx) {
	if (xaxis == null && pctx == null)
	    return;

	if (pctx != null && (pctx.getFlags() & PrintContext.X_AXIS) == 0)
	    return;

	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null) return;
	DataElement data[] = dataSet.getData();
	
	Font font = VAMPResources.getFont(VAMPResources.AXIS_X_DISPLAYER_FONT);
	g.setFont(font);

	int y0 = 5;
	if (pctx != null)
	    y0 = (int)pctx.getRY(0);

	Dimension size;
	if (xaxis == null)
	    size = pctx.getArea().getSize();
	else
	    size = xaxis.getSize();

	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_Y_PROPERTY_NAME_FG));
	String objDspProp = (String)graphElement.getPropertyValue(Property.getProperty("ObjDisplay"));

	if (objDspProp == null)
	    objDspProp = "SourceID";

	for (int n = 0; n < data.length; n++) {
	    DataElement d = data[n];
	    double width = d.getRBounds(graphElement).width;
	    if (width < MINIMUM_SIZE)
		continue;

	    double x = d.getRX(graphElement) + width/2;
	    if (x < 0 || x > size.width)
		continue;

	    if (pctx != null)
		x = pctx.getRX(x);

	    String id = (String)d.getPropertyValue(Property.getProperty(objDspProp));
	    if (id == null)
		id = (String)d.getID();

	    g.rotate(angle, x, y0);
	    g.drawString(id, (int)x, y0);
	    g.rotate(-angle, x, y0);
	}
    }

    public void displayYAxis(GraphCanvas canvas, Axis yaxis, Graphics2D g,
			     GraphElement graphElement, int m, PrintContext pctx) {
	if (yaxis == null && pctx == null)
	    return;

	if (pctx != null && (pctx.getFlags() & PrintContext.Y_AXIS) == 0)
	    return;

	int x0;
	if (yaxis != null)
	    x0 = yaxis.getSize().width/2 + 5;
	else
	    x0 = (int)(pctx.getBounds().x + pctx.getBounds().width/2) + 5;

	//	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_LABEL_FG));
	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_Y_PROPERTY_NAME_FG));

	double ry0 = canvas.getRY(0);
	double ry1 = canvas.getRY(1);
	if (pctx != null) {
	    ry0 = pctx.getRY(ry0);
	    ry1 = pctx.getRY(ry1);
	}

	double rheight = ry1 - ry0;
	double rheight2 = rheight/2;
	Object name = graphElement.getPropertyValue(VAMPProperties.NameProp);
	Font font = VAMPResources.getFont(VAMPResources.AXIS_Y_DISPLAYER_FONT);
	if (name != null) {
	    g.setFont(font);
	    Dimension d = Utils.getSize(g, (String)name);
	    if (displayPropName((int)rheight, d.height)) {
		double ry = canvas.getRY(m);
		if (pctx != null)
		    ry = pctx.getRY(ry);
		g.drawString((String)name, (int)(x0-d.width/2),
			     (int)(ry + rheight2 + d.height/2));
	    }
	}
    }

    public double getMaxX(GraphElement graphElement) {

	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null) return 0;

	DataElement data[] = dataSet.getData();
	if (data.length == 0) return 0;
	return data[data.length-1].getVX(graphElement);
    }

    public double getMaxY(GraphElement graphElement) {
	return 1;
    }

    public double getMinX(GraphElement graphElement) {
	return 0;
    }

    public double getMinY(GraphElement graphElement) {
	return 0;
    }

    private boolean displayPropName(int rheight, int height) {
	//	return height <= rheight;
	return height <= rheight+3; // 20/04/05: added: +3
    }

    /*
    double getRX(GraphCanvas canvas, DataSet dataSet, double vx) {
	DataElement e = dataSet.dataAtPosX(vx);
	if (e == null || e.getInd() == dataSet.getData().length-1)
	    return Double.MIN_VALUE;

	return e.getRX();
    }
    */

    boolean useCytoband(GraphElement graphElem) {
	return false;
    }
}
