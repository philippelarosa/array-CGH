
/*
 *
 * CurveDataSetDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

class CurveDataSetDisplayer extends StandardDataSetDisplayer {

    static final int SIZE = 4;
    static final int SIZE2 = SIZE/2;

    CurveDataSetDisplayer() {
	super("Curve", null);
    }

    public void display(GraphCanvas canvas, Graphics2D g,
			GraphElement graphElement,
			int m, PrintContext pctx) {

	display_os(canvas, g, graphElement, m, pctx);
	display_x(canvas, g, graphElement, m, pctx);
    }

    public void display_r(GraphCanvas canvas, Graphics2D g,
			  GraphElement graphElement,
			  boolean offScreen, int dummy, PrintContext pctx) {

	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null)
	    return;

	if (!dataSet.isFullImported()) {
	    return;
	}

	DataElement data[] = dataSet.getData();
	DataElement d0 = data[0];

	if (g != null)
	    g.setColor(Color.BLACK);

	for (int n = 1; n < data.length; n++) {
	    DataElement d = data[n];
	    boolean isNA = VAMPUtils.isNA(d);
	    boolean isMissing = VAMPUtils.isMissing(d);
	    if (!isNA && !isMissing) {
		double x0 = d0.getRX(graphElement);
		double y0 = d0.getRY(graphElement);
		double x = d.getRX(graphElement);
		double y = d.getRY(graphElement);
		if (pctx != null) {
		    x0 = pctx.getRX(x0);
		    y0 = pctx.getRY(y0);
		    x = pctx.getRX(x);
		    y = pctx.getRY(y);
		}

		// d.setGraphics(g, dataSet); // ??

		if (g != null)
		    g.drawLine((int)x0, (int)y0, (int)x, (int)y);
		d0 = d;
		if (g != null)
		    d.setRBounds(graphElement, (int)(x-SIZE2), (int)(y-SIZE2), SIZE, SIZE);
	    }
	    else
		d.setRBounds(graphElement, 0, 0, 0, 0);
	}

	drawColorLines(g, canvas, dataSet, pctx);
	dataSet.resetPaintVBounds();
    }

    public void computeVBounds(GraphCanvas canvas, Graphics2D g,
			       GraphElement graphElement, int m) {
	graphElement.resetPaintVBounds();
    }

    boolean isCompatible(AxisDisplayer axisDisplayer) {
	if (axisDisplayer instanceof DotPlotAxisDisplayer)
	    return false;
	return super.isCompatible(axisDisplayer);
    }
}
