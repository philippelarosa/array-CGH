
/*
 *
 * SignalHistogramDataSetDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

class SignalHistogramDataSetDisplayer extends StandardDataSetDisplayer {

    static final int SIZE = 4;
    static final int SIZE2 = SIZE/2;

    SignalHistogramDataSetDisplayer() {
	super("Signal Histogram", null);
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

	/*
	DataSet dataSet = graphElement.asDataSet();
	System.out.println("SignalHistogram displayer " + dataSet);
	if (dataSet == null) {
	    return;
	}

	DataElement data[] = dataSet.getData();
	DataElement d0 = data[0];
	*/
	try {
	    RODataElementProxy d0 = graphElement.getDataProxy(0);

	    if (g != null) {
		g.setColor(Color.BLACK);
	    }
	    
	    int length = graphElement.getProbeCount();

	    double minY = graphElement.getVBounds().y;

	    for (int n = 1; n < length; n++) {
		//DataElement d = data[n];
		RODataElementProxy d = graphElement.getDataProxy(n);
		boolean isNA = VAMPUtils.isNA(d);
		boolean isMissing = VAMPUtils.isMissing(d);
		if (!isNA && !isMissing) {
		    /*
		      double x0 = d0.getRX(graphElement);
		      double y0 = d0.getRY(graphElement);
		      double x = d.getRX(graphElement);
		      double y = d.getRY(graphElement);
		    */
		    double x0 = canvas.getRX(d0.getPosX(graphElement));
		    double y0 = canvas.getRY(minY - d0.getPosY(graphElement));
		    double x = canvas.getRX(d.getPosX(graphElement));
		    double y = canvas.getRY(minY - d.getPosY(graphElement));

		    if (pctx != null) {
			x0 = pctx.getRX(x0);
			y0 = pctx.getRY(y0);
			x = pctx.getRX(x);
			y = pctx.getRY(y);
		    }

		    d.setGraphics(g, graphElement);

		    if (g != null) {
			g.drawLine((int)x0, (int)y0, (int)x, (int)y);
		    }

		    d0 = d;
		    if (g != null) {
			d.setRBounds(graphElement, (int)(x-SIZE2), (int)(y-SIZE2), SIZE, SIZE);
		    }
		}
		else {
		    d.setRBounds(graphElement, 0, 0, 0, 0);
		}
	    }
	}
	catch(Exception e) {
	    e.printStackTrace();
	}

	//dataSet.resetPaintVBounds();
	graphElement.resetPaintVBounds();
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
