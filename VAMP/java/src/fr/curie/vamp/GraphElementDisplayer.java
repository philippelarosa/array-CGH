
/*
 *
 * GraphElementDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;
import java.awt.geom.*;

public abstract class GraphElementDisplayer implements Cloneable {

    protected String name;
    protected GraphElementIDBuilder graphElementIDBuilder;
    static private Vector displayers = new Vector();
    private final double RPADY = 60.;
    static final boolean VERBOSE = false;
    static final boolean BUG_90 = true;

    public GraphElementDisplayer(String name, GraphElementIDBuilder graphElementIDBuilder) {
	this.name = name;
	setGraphElementIDBuilder(graphElementIDBuilder);
	displayers.add(this);
    }

    void setGraphElementIDBuilder(GraphElementIDBuilder
				  graphElementIDBuilder) {
	this.graphElementIDBuilder = graphElementIDBuilder;
    }

    GraphElementIDBuilder getGraphElementIDBuilder() {
	return graphElementIDBuilder;
    }

    static GraphElementDisplayer get(String name) {
	int size = displayers.size();
	for (int n = 0; n < size; n++) {
	    GraphElementDisplayer displayer =
		(GraphElementDisplayer)displayers.get(n);
	    if (displayer.getName().equals(name))
		return displayer;
	}

	return null;
    }

    String getName() {return name;}

    abstract public void computeVBounds(GraphCanvas canvas,
					Graphics2D g,
					GraphElement graphElement,
					int m);

    abstract public void display(GraphCanvas canvas, Graphics2D g,
				 GraphElement graphElement, int m,
				 PrintContext pctx);

    protected Object clone() throws CloneNotSupportedException {
	return super.clone();
    }

    boolean selectAfterPaste() {return true;}
    double getRPadY() {return RPADY;}

    Scale getDefaultScale(Dimension rdim, LinkedList graphElements) {
	return null;
    }

    public void computeVCoords(GraphCanvas canvas,
			       GraphElement graphElement,
			       int m) {
	if (graphElement instanceof DataSet) {
	    DataElement data[] = ((DataSet)graphElement).getData();
	    for (int n = 0; n < data.length; n++)
		data[n].resetVXY(graphElement);
	}
    }

    void setGraphElements(LinkedList graphElements) {
    }

    boolean isRotated() {return false;}

    boolean checkGraphElements(LinkedList graphElements) {return true;}

    boolean isCompatible(AxisDisplayer axisDisplayer) {return true;}

    boolean isVXRelocated() {return false;}
    boolean needDeltaEndRegion() {return false;}

    int getHSizeSets(LinkedList graphElements) {
	return graphElements.size();
    }

    void computeGraphElements(GraphCanvas canvas) {
	double minAutoY, maxAutoY;
	double minAutoY2, maxAutoY2;
	boolean hasAutoY = false;
	boolean hasNoAutoY = false;

	double maxX, minX, maxY, minY;

	GraphPanel graphPanel = canvas.getGraphPanel();
	LinkedList graphElements = canvas.getGraphElements();
	int sizeSets = graphElements.size();

	if (sizeSets == 0) {
	    maxX = minX = maxY = minY = 0;
	    maxAutoY = minAutoY = 0;
	    maxAutoY2 = minAutoY2 = 0;
	}
	else {
	    maxX = Double.MIN_VALUE;
	    maxY = Double.MIN_VALUE;
	    minX = Double.MAX_VALUE;
	    minY = Double.MAX_VALUE;

	    minAutoY = Double.MAX_VALUE;
	    maxAutoY = Double.MIN_VALUE;
	    minAutoY2 = Double.MAX_VALUE;
	    maxAutoY2 = Double.MIN_VALUE;
	}

	for (int m = 0; m < sizeSets; m++) {
	    double min_x, max_x, min_y, max_y;
	    GraphElement graphElement =
		(GraphElement)graphElements.get(m);

	    boolean p_hasAuto = graphElement.asDataSet() != null &&
		(graphElement.asDataSet().isAutoY() ||
		 graphElement.asDataSet().isAutoY2());

	    if (graphPanel.getDefaultGraphElementDisplayer() != null)
		graphPanel.getDefaultGraphElementDisplayer().
		    computeVCoords(canvas, graphElement, m);

	    AxisDisplayer axisDisplayer = canvas.getAxisDisplayer(graphElement);
	    max_x = axisDisplayer.getMaxX(graphElement);
	    min_x = axisDisplayer.getMinX(graphElement);

	    if (max_x > maxX) maxX = max_x;
	    if (min_x < minX) minX = min_x;
		
	    if (BUG_90 && !p_hasAuto && useHardThresholds()) {
		max_y = VAMPUtils.getThresholdMaxY(graphElement);
		if (max_y == Double.MAX_VALUE) {
		    max_y = axisDisplayer.getMaxY(graphElement);
		}

		min_y = VAMPUtils.getThresholdMinY(graphElement);
		if (min_y == -Double.MAX_VALUE) {
		    min_y = axisDisplayer.getMinY(graphElement);
		}
		//System.out.println("hard Y #" + m + " " + min_y + " " + max_y);
	    }
	    else {
		max_y = axisDisplayer.getMaxY(graphElement);
		min_y = axisDisplayer.getMinY(graphElement);
		//System.out.println("Y #" + m + " " + min_y + " " + max_y);
	    }

	    //System.out.println("X #" + m + " " + min_x + " " + max_x);
	    //System.out.println("Y #" + m + " " + min_y + " " + max_y);

	    if (graphElement.asDataSet() != null &&
		graphElement.asDataSet().isAutoY()) {
		if (max_y > maxAutoY) maxAutoY = max_y;
		if (min_y < minAutoY) minAutoY = min_y;
		hasAutoY = true;
	    }
	    else if (graphElement.asDataSet() != null &&
		graphElement.asDataSet().isAutoY2()) {
		if (max_y > maxAutoY2) maxAutoY2 = max_y;
		if (min_y < minAutoY2) minAutoY2 = min_y;
		hasAutoY = true;
	    }
	    else {
		if (max_y > maxY) maxY = max_y;
		if (min_y < minY) minY = min_y;	
		hasNoAutoY = true;
	    }
	}

	if (hasAutoY) {
	    for (int m = 0; m < sizeSets; m++) {
		DataSet dataSet = canvas.getDataSet(m);
		if (dataSet == null)
		    continue;
		if (dataSet.isAutoY()) {
		    if (hasNoAutoY) {
			double ycoef = (maxAutoY - minAutoY) /
			    (maxY - minY);
			dataSet.setYInfo(ycoef, maxY - maxAutoY/ycoef);
		    }
		    else
			dataSet.setYInfo(1, 0);
		}

		if (dataSet.isAutoY2()) {
		    if (hasNoAutoY) {
			double ycoef = (maxAutoY2 - minAutoY2) /
			    (maxY - minY);
			dataSet.setYInfo(ycoef, maxY - maxAutoY2/ycoef);
		    }
		    else
			dataSet.setYInfo(1, 0);
		}
	    }

	    if (!hasNoAutoY) {
		/*
		minY = minAutoY;
		maxY = maxAutoY;
		*/
		minY = (minAutoY < minAutoY2 ? minAutoY : minAutoY2);
		maxY = (maxAutoY > maxAutoY2 ? maxAutoY : maxAutoY2);
	    }
	}

	// really ?
	if (minX > 0) minX = 0.;
	// 14/09/04 disconnected 
	// if (minY > 0) minY = 0.;

	//System.out.println("before final: " + minX + " " + maxX + " " + minY + " " + maxY);

	computeGraphElementVBounds(canvas,
				   canvas.getCanonScale().getScaleY());

	canvas.getView().getSearchGraphElementPanel().update();
	canvas.getView().getSearchDataElementPanel().update();
	FilterDialog.update(canvas.getGlobalContext(),
			    canvas.getView(),
			    graphPanel);

	SortDialog.update(canvas.getGlobalContext(),
			  canvas.getView(),
			  graphPanel);

	if (graphPanel.getDefaultGraphElementDisplayer() != null)
	    graphPanel.getDefaultGraphElementDisplayer().
		setGraphElements(graphElements);

	canvas.resetMinX();
	canvas.resetMaxX();
	canvas.resetMinY();
	canvas.resetMaxY();

	//System.out.println("final: " + minX + " " + maxX + " " + minY + " " + maxY);
	graphPanel.setMaxX(maxX);
	graphPanel.setMinX(minX);
	graphPanel.setMaxY(maxY);
	graphPanel.setMinY(minY);
    }

    void computeGraphElementVBounds(GraphCanvas canvas, double scaleY) {
	GraphPanel graphPanel = canvas.getGraphPanel();
	LinkedList graphElements = canvas.getGraphElements();
	int sizeSets = graphElements.size();

	double maxX = canvas.getMaxX();
	double minX = canvas.getMinX();
	double maxY = canvas.getMaxY();
	double minY = canvas.getMinY();

	if (VERBOSE) {
	    System.out.println("computeGraphElementVBounds: " + minX + " " + maxX + " " + minY + " " + maxY);
	}

	double wY = maxY - minY;
	double wX = maxX - minX;

	double maxVPadY = 2*wY;
	double rPadY = graphPanel.getDefaultGraphElementDisplayer().
	    getRPadY();

	double height = canvas.getSize().height;
	int hSizeSets = graphPanel.getDefaultGraphElementDisplayer().
	    getHSizeSets(graphElements);

	double vPadY;
	if (rPadY != 0. && height != 0) {
	    double visible_cnt = hSizeSets / scaleY;
	    double percent_y = (rPadY / height);
	    double ry = percent_y * visible_cnt;
	    vPadY = wY * ry;
	    if (vPadY > maxVPadY)
		vPadY = maxVPadY;
	}
	else
	    vPadY = 0.;

	canvas.setVPadY(vPadY);

	if (VERBOSE) {
	    System.out.println("vPadY: " + vPadY);
	}

	double y = 0;
	for (int m = 0; m < sizeSets; m++) {
	    y += wY;
	    if (m != 0) y += vPadY;
	    ((GraphElement)graphElements.get(m)).setVBounds(0, y, wX, wY);
	}
	
	graphPanel.setVirtualSize(maxX,
				  wY*hSizeSets + (vPadY*(hSizeSets-1)));
    }

    protected void computeRCoords(GraphCanvas canvas,
				  GraphElement graphElement,
				  double t_minY, double t_maxY) {
	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null)
	    return;
	DataElement data[] = dataSet.getData();
	Rectangle2D.Double vBounds = dataSet.getVBounds();
	double vx0 = vBounds.x;
	double vy0 = vBounds.y;
	boolean isArrayMerge = VAMPUtils.getType(dataSet).equals
	    (VAMPConstants.CGH_ARRAY_MERGE_TYPE);

	double maxX = canvas.getMaxX();
	double minX = canvas.getMinX();
	double maxY = canvas.getMaxY();
	double minY = canvas.getMinY();

	for (int n = 0; n < data.length; n++) {
	    DataElement d = data[n];
	    double y = d.getVY(graphElement);

	    if (!isArrayMerge) {
		if (y > t_maxY)
		    y = t_maxY;

		if (y < t_minY)
		    y = t_minY;
	    }

	    y = dataSet.yTransform(y);

	    double vx = d.getVX(graphElement) + vx0 + minX;
	    double vy = vy0 - y + minY;
	       
	    d.setRX(graphElement, canvas.getRX(vx, vy));
	    d.setRY(graphElement, canvas.getRY(vx, vy));
	    /*
	    if (VAMPUtils.getChr(d).equals("14")) {
		System.out.println("vx: " + vx + " -> " +
				   d.getRX());
		System.out.println("vy: " + vy + " -> " +
				   d.getRY());
	    }
	    */
	    d.setRSize(graphElement, d.getVSize(graphElement) * canvas.getScale().getScaleX());
	}
    }

    void computeRCoords(GraphCanvas canvas) {
	computeRCoords(canvas, false);
    }

    void computeRCoords(GraphCanvas canvas, boolean force) {
	if (!canvas.COMPUTE_RCOORDS)
	    return;

	if (GraphCanvas.VERBOSE) {
	    System.out.println("computeRCoords");
	}

	//(new Exception()).printStackTrace();
	Vector refV = new Vector();
	int sizeSets = canvas.getGraphElements().size();
	for (int m = 0; m < sizeSets; m++) {
	    DataSet dataSet = canvas.getDataSet(m);
	    if (dataSet == null)
		continue;

	    // added 21/01/05
	    if (!force && !dataSet.isVisible()) // added force 27/05/05
		continue;

	    double t_minY = VAMPUtils.getThresholdMinY(dataSet);
	    double t_maxY = VAMPUtils.getThresholdMaxY(dataSet);
	    computeRCoords(canvas, dataSet, t_minY, t_maxY);
	    DataSet ref = (DataSet)dataSet.getPropertyValue(VAMPProperties.ArrayRefProp);

	    if (ref != null && !refV.contains(ref)) {
		t_minY = VAMPUtils.getThresholdMinY(ref);
		t_maxY = VAMPUtils.getThresholdMaxY(ref);
		computeRCoords(canvas, ref, t_minY, t_maxY);
		refV.add(ref);
	    }

	}
    }

    boolean useOptSelection() {return true;}

    void warnGraphElements(GlobalContext globalContext,
			   LinkedList graphElements) {
    }

    boolean useHardThresholds() {
	return true;
    }
}
