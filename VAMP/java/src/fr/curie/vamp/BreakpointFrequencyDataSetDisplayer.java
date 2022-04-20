
/*
 *
 * BreakpointFrequencyDataSetDisplayer.java
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

class BreakpointFrequencyDataSetDisplayer extends StandardDataSetDisplayer {

    private static final int EPSILON = 4;
    private static final int sizeTHR = 6;
    private static final int sizeTHR2 = sizeTHR/2;
    private static final boolean DEBUG_DSP_MODE = false;
    private static final Color BlackRed = new Color(0xcc0000);
    private TreeMap params;
    private Vector layoutx_v;
    private Vector layouty_v;

    BreakpointFrequencyDataSetDisplayer(TreeMap params) {
	this("BreakpointFrequency", params);
    }

    BreakpointFrequencyDataSetDisplayer(String name, TreeMap params) {
	super(name, null);
	this.params = params;
    }

    private static final double MIN_SCALE = 5;
    static final Color transparentBG = new Color(0, 0, 0, 0);

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

	boolean isVisible;
	if (pctx == null) {
	    isVisible = dataSet.isVisible();
	    if (dataSet.getRBounds().height <
		VAMPResources.getDouble(VAMPResources.MIN_HEIGHT_PAINT))
		return;
	}
	else
	    isVisible = true;

	double vy0 = dataSet.getVBounds().y;

	//vy0 = dataSet.yTransform(vy0);

	double ry0 = canvas.getRY(vy0);
	double ry1 = ry0 - canvas.getRH(dataSet.getVBounds().height);

	if (pctx != null) {
	    ry0 = pctx.getRY(ry0);
	    ry1 = pctx.getRY(ry1);
	}

	DataElement data[] = dataSet.getData();

	double selectT = ((Double)dataSet.getPropertyValue(BreakpointFrequencyOP.selectTProp)).doubleValue();

	double normDensity = 0;
	double minDensity = 0;
	boolean show_density = showDensity();
	if (show_density) {
	    normDensity = ((Double)dataSet.getPropertyValue(BreakpointFrequencyOP.normDensityProp)).doubleValue();
	    minDensity = ((Double)dataSet.getPropertyValue(BreakpointFrequencyOP.minDensityProp)).doubleValue();
	}

	/*
	System.out.println("show density: " + show_density + 
			   ", " + normDensity + ", " +
			   minDensity);
	*/
	
	boolean show_barplots = showBarplots();
	boolean show_asso = showAssociations();

	Point lastDensity = null;

	layoutx_v = new Vector();
	layouty_v = new Vector();

	int min_y = Integer.MAX_VALUE;
	boolean odd = false;
	int step_height = 0;
	if (true /*show_asso*/) {
	    for (int n = 0; n < data.length; n++) {
		DataElement item = data[n];
		Rectangle rect = getRect(canvas, graphElement, item, ry0, pctx);
		if (rect.y < min_y)
		    min_y = rect.y;
	    }
	}

	min_y -= EPSILON;
	int cnt = 0;
	boolean nomore = false;

	for (int n = 0; n < data.length; n++) {
	    DataElement item = data[n];
	    Property prop = BreakpointFrequencyOP.bkpFreqAssoLinkedProp;
	    Vector link_v = (Vector)item.getPropertyValue(prop);
	    if (link_v != null) {
		int size = link_v.size();
		
		for (int k = 0; k < size; k++) {
		    BreakpointFrequencyOP.Association asso =
			(BreakpointFrequencyOP.Association)link_v.get(k);
		    asso.setDrawn(false);
		}
	    }
	}

	Vector asso_v = new Vector();
	Dimension dim = canvas.getSize();
	Rectangle2D.Double bounds = new Rectangle2D.Double(0, 0,
							   dim.width,
							   dim.height);
	for (int n = 0; n < data.length; n++) {
	    DataElement item = data[n];

	    Rectangle rect = getRect(canvas, graphElement, item, ry0, pctx);

	    if (pctx == null)
		item.setRBounds(dataSet, rect.x, rect.y,
				rect.width, rect.height);
	    if (g == null)
		continue;

	    if ((show_asso ||
		 item.getPropertyValue(BreakpointFrequencyOP.showAssoProp)
		 != null) && !nomore) {

		Property prop = BreakpointFrequencyOP.bkpFreqAssoLinkedProp;
		Vector link_v = (Vector)item.getPropertyValue(prop);
		if (link_v != null) {
		    int size = link_v.size();

		    for (int k = 0; k < size; k++) {
			BreakpointFrequencyOP.Association asso =
			    (BreakpointFrequencyOP.Association)link_v.get(k);

			if (asso.isDrawn())
			    continue;
			asso.setDrawn(true);

			int height = min_y - step_height;
			if (height < ry1) {
			    drawOutOfBounds(g, canvas, ry1, dataSet, pctx);
			    nomore = true;
			    break;
			}

			compute(asso, canvas, dataSet, vy0, height);

			if (setGraphics(asso, g)) {
			    asso_v.add(asso);
			    asso_v.add(new Integer(height));
			}
			else
			    drawAsso(asso, g, height, pctx);

			step_height += 2;
			cnt++;
		    }
		}
	    }

	    int rx0 = rect.x;
	    int ry = rect.y;
	    int width = rect.width;
	    int height = rect.height;

	    if (show_barplots) {
		if (height > 0 && width < 33000 && height < 33000) {
		    item.setGraphics(g, dataSet);
		    if (item.getPropertyValue(BreakpointFrequencyOP.showAssoProp)
			!= null) {
			g.setColor(Color.GREEN);
		    }
		    else if (item.getPropertyValue(BreakpointFrequencyOP.bkpFreqAssoProp) != null) {
			g.setColor(odd ? Color.RED : BlackRed);
			odd = !odd;
		    }
		    else if (item.getPosY(graphElement) < selectT)
			g.setColor(Color.BLUE);
		    else
			g.setColor(Color.ORANGE);

		    g.fillRect((int)rx0, (int)ry, (int)width, height);
		}
	    }
	    
	    if (show_density && item.getPosY(graphElement) != 0) {
		double rx = rx0 + (pctx != null ? pctx.getRW((double)width/2) :
				   (double)width/2);
		double density = ((Double)item.getPropertyValue(BreakpointFrequencyOP.densityProp)).doubleValue();
		double rdens = canvas.getRH((density-minDensity) * normDensity);
		if (pctx != null)
		    rdens = pctx.getRH(rdens);

		g.setColor(Color.BLACK);

		if (lastDensity != null) {
		    g.drawLine(lastDensity.x, lastDensity.y, (int)rx, (int)(ry0-rdens));
		}
		lastDensity = new Point((int)rx, (int)(ry0-rdens));
	    }
	}

	int asso_v_size = asso_v.size();
	for (int j = 0; j < asso_v_size; j += 2) {
	    BreakpointFrequencyOP.Association asso =
		(BreakpointFrequencyOP.Association)asso_v.get(j);
	    int height = ((Integer)asso_v.get(j+1)).intValue();
	    setGraphics(asso, g);
	    drawAsso(asso, g, height, pctx);
	}

	System.out.println(cnt + " associations drawn");
    }

    public void computeVBounds(GraphCanvas canvas, Graphics2D g,
			       GraphElement graphElement, int m) {
	graphElement.resetPaintVBounds();
    }


    Rectangle getRect(GraphCanvas canvas, GraphElement graphElement,
		      DataElement item, double ry0,
		      PrintContext pctx) {
	
	double rx0 = item.getRX(graphElement);
	double ry = item.getRY(graphElement);

	double width = canvas.getRW(item.getPosSize(graphElement));
	
	if (pctx != null) {
	    rx0 = pctx.getRX(rx0);
	    ry = pctx.getRY(ry);
	    width = pctx.getRW(width);
	}
	
	if (width < 1)
	    width = 1;
	int height = (int)(Math.rint(ry0) - Math.rint(ry));
	
	return new Rectangle((int)rx0, (int)ry, (int)width, (int)height);
    }

    /*
    void setParams(TreeMap params) {
	this.params = params;
    }
    */

    boolean showDensity() {
	Object v = params.get(BreakpointFrequencyOP.SHOW_DENSITY_PARAM);
	return v != null && v.equals(BreakpointFrequencyOP.TRUE);
    }

    boolean showBarplots() {
	Object v = params.get(BreakpointFrequencyOP.SHOW_BARPLOTS_PARAM);
	return v != null && v.equals(BreakpointFrequencyOP.TRUE);
    }

    boolean showAssociations() {
	Object v = params.get(BreakpointFrequencyOP.SHOW_ASSO_PARAM);
	return v != null && v.equals(BreakpointFrequencyOP.TRUE);
    }

    void showAssociations(boolean v) {
	params.put(BreakpointFrequencyOP.SHOW_ASSO_PARAM,
		   v ? BreakpointFrequencyOP.TRUE :
		   BreakpointFrequencyOP.FALSE);
    }

    void drawOutOfBounds(Graphics g, GraphCanvas canvas, double ry1, 
			 DataSet dataSet, PrintContext pctx) {

	double rx0 = canvas.getRX(dataSet.getVBounds().x);

	double width = canvas.getRW(dataSet.getVBounds().width);
	double height = 3;

	if (pctx != null) {
	    rx0 = pctx.getRX(rx0);
	    width = pctx.getRW(width);
	    height = pctx.getRH(height);
	}

	g.setColor(Color.RED);
	g.fillRect((int)rx0, (int)ry1, (int)width, (int)height);
	System.out.println("BKPFreq display overflow");
    }

    boolean showAsso(BreakpointFrequencyOP.Association asso) {
	for (int j = 0; j < asso.data1.length; j++) {
	    if (asso.data1[j].getPropertyValue
		(BreakpointFrequencyOP.showAssoProp) != null)
		return true;
	}
	
	for (int j = 0; j < asso.data2.length; j++) {
	    if (asso.data2[j].getPropertyValue
		(BreakpointFrequencyOP.showAssoProp) != null)
		return true;
	}

	return false;
    }

    boolean setGraphics(BreakpointFrequencyOP.Association asso, Graphics g) {

	if (showAsso(asso)) {
	    g.setColor(Color.BLUE);
	    return true;
	}

	g.setColor(Color.BLACK);
	return false;
    }

    private void compute(BreakpointFrequencyOP.Association asso,
			 GraphCanvas canvas, DataSet dataSet,
			 double vy0, int height) {
	asso.brakets = new Rectangle2D.Double[2];

	//	double y1 = canvas.getRY(vy0 - dataSet.yTransform(asso.posy1)) - EPSILON - 2;
	double y1 = canvas.getRY(dataSet.yTransform(dataSet.yTransform_1(vy0) - asso.posy1)) - EPSILON - 2;
	double b1 = asso.begin1.getRX(dataSet);
	double e1 = asso.end1.getRX(dataSet) + asso.end1.getRSize(dataSet) - 1;

	asso.brakets[0] = findy(b1, e1, y1);

	//double y2 = canvas.getRY(vy0 - dataSet.yTransform(asso.posy2)) - EPSILON - 2;
	double y2 = canvas.getRY(dataSet.yTransform(dataSet.yTransform_1(vy0) - asso.posy2)) - EPSILON - 2;
	double b2 = asso.begin2.getRX(dataSet);
	double e2 = asso.end2.getRX(dataSet) + asso.end2.getRSize(dataSet) - 1;

	asso.brakets[1] = findy(b2, e2, y2);

	asso.middles = new Rectangle2D.Double[2];
	asso.middles[0] = findx(b1+(e1-b1)/2, height,
				asso.brakets[0].y - height, e1);
	asso.middles[1] = findx(b2+(e2-b2)/2, height,
				asso.brakets[1].y - height, e2);
    }

    private Rectangle2D.Double findx(double x, double y, double height,
				     double max_x) {
	Rectangle2D.Double r = new Rectangle2D.Double(x, y, EPSILON-1, height);
	return find(layoutx_v, r, true, max_x);
    }

    private Rectangle2D.Double findy(double b, double e, double y) {
	Rectangle2D.Double r = new Rectangle2D.Double(b, y, e - b, EPSILON);
	return find(layouty_v, r, false, 0);
    }

    static final int INC_X = 2;
    static final int INC_Y = EPSILON;

    private Rectangle2D.Double find(Vector layout_v,
				    Rectangle2D.Double r,
				    boolean is_x, double max_x) {
	int size = layout_v.size();
	boolean found = true;

	//System.out.println("FIND " + is_x + " " + r + " VS.");
	while (found) {
	    found = false;
	    for (int n = 0; n < size; n++) {
		Rectangle2D.Double rn = (Rectangle2D.Double)layout_v.get(n);
		//System.out.println("\t" + rn);
		if (rn.intersects(r)) {
		    //System.out.println("found " + is_x);
		    if (is_x) {
			if (r.x + INC_X > max_x)
			    break; // found == false
			r.x += INC_X;
		    }
		    else
			r.y -= INC_Y;

		    found = true;
		    break;
		}
	    }
	}

	layout_v.add(r);
	return r;
    }

    private void drawAsso(BreakpointFrequencyOP.Association asso, Graphics g,
			  int height, PrintContext pctx) {
	double b1 = asso.brakets[0].x;
	double e1 = asso.brakets[0].x + asso.brakets[0].width;
	double y1 = asso.brakets[0].y;

	double b2 = asso.brakets[1].x;
	double e2 = asso.brakets[1].x + asso.brakets[1].width;
	double y2 = asso.brakets[1].y;

	double y1_b = y1 + EPSILON;
	double y2_b = y2 + EPSILON;

	double x1 = asso.middles[0].x;
	double x2 = asso.middles[1].x;

	if (pctx != null) {
	    y1 = pctx.getRY(y1);
	    y1_b = pctx.getRY(y1_b);
	    b1 = pctx.getRX(b1);
	    e1 = pctx.getRX(e1);

	    y2 = pctx.getRY(y2);
	    y2_b = pctx.getRY(y2_b);
	    b2 = pctx.getRX(b2);
	    e2 = pctx.getRX(e2);

	    x1 = pctx.getRX(x1);
	    x2 = pctx.getRX(x2);
	}

	g.drawLine((int)b1, (int)y1,
		   (int)e1, (int)y1);

	g.drawLine((int)b1, (int)y1,
		   (int)b1, (int)y1_b);

	g.drawLine((int)e1, (int)y1,
		   (int)e1, (int)y1_b);

	g.drawLine((int)b2, (int)y2,
		   (int)e2, (int)y2);

	g.drawLine((int)b2, (int)y2,
		   (int)b2, (int)y2_b);

	g.drawLine((int)e2, (int)y2,
		   (int)e2, (int)y2_b);

	//double x1 = b1+(e1-b1)/2;
	g.drawLine((int)x1, (int)y1,
		   (int)x1, height);

	//double x2 = b2+(e2-b2)/2;
	g.drawLine((int)x2, (int)y2,
		   (int)x2, height);

	g.drawLine((int)x1, height,
		   (int)x2, height);

    }
}
