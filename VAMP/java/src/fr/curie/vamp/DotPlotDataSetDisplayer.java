
/*
 *
 * DotPlotDataSetDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004a
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

class DotPlotDataSetDisplayer extends CommonDataSetDisplayer {

    private final int SPACE_HEIGHT = 2;
    private final int SPACE_WIDTH = 2;
    private final int SPACE_WIDTH_2 = SPACE_WIDTH/2;
    private final int SPACE_HEIGHT2 = SPACE_HEIGHT*2;
    private final int MIN_VALUE = 1;
    private static final int CHR_OFFSET = 2;

    DotPlotDataSetDisplayer() {
	super("DotPlot", null);
    }

    private double getValue(double value) {
	if (value < MIN_VALUE) 
	    value = MIN_VALUE;
	return value;
    }

    public void display(GraphCanvas canvas, Graphics2D g,
			GraphElement graphElement,
			int m, PrintContext pctx) {
	display_os(canvas, g, graphElement, m, pctx);
    }

    public void display_r(GraphCanvas canvas, Graphics2D g,
			  GraphElement graphElement,
			  boolean offscreen, int m, PrintContext pctx) {

	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null) return;

	if (!dataSet.isFullImported()) {
	    return;
	}

	DataElement data[] = dataSet.getData();
	double x0 = canvas.getRX(0);
	double y0 = canvas.getRY(canvas.getVBounds(m).y);

	boolean isVisible;

	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    y0 = pctx.getRY(y0);
	    isVisible = true;
	}
	else
	    isVisible = canvas.isVR_Visible(dataSet.getVBounds());

	double ry = canvas.getRY(m);

	double rym = canvas.getRY(m);
	double rym1 = canvas.getRY(m+1);
	double rym_1 = canvas.getRY(m-1);
	if (pctx != null) {
	    ry = pctx.getRY(ry);
	    rym = pctx.getRY(rym);
	    rym1 = pctx.getRY(rym1);
	    rym_1 = pctx.getRY(rym_1);
	}

	double bwidth, bheight;
	if (m < canvas.getGraphElements().size() - 1)
	    bheight = rym1 - rym;
	else
	    bheight = rym - rym_1;

	double rheight = getValue(bheight);

	// 1/10/04: disconnected !
	/*
	if (pctx == null &&
	    dataSet.isSelected()) { // this test: for dotplot mark & region support
	    Color bgColor = dataSet.isSelected() ?
		VAMPResources.getColor(VAMPResources.DATASET_SELECTED_FG) : Color.WHITE;
	    if (g != null) {
		g.setColor(bgColor);
		g.fillRect((int)x0, (int)ry,
			   (int)(canvas.getRX(data.length)-x0), (int)rheight);
	    }
	}
	*/

	GNLCodeManage(data[0].getPropertyValue(VAMPProperties.GNLProp) == null);

	double rh = getValue(bheight - SPACE_HEIGHT);

	for (int n = 0; n < data.length; n++) {
	    DataElement item = data[n];
	    double rx = item.getRX(graphElement);
	    double rxn = canvas.getRX(n);
	    double rxn1 = canvas.getRX(n+1);
	    double rxn_1 = canvas.getRX(n-1);
	    if (pctx != null) {
		rx = pctx.getRX(rx);
		rxn = pctx.getRX(rxn);
		rxn1 = pctx.getRX(rxn1);
		rxn_1 = pctx.getRX(rxn_1);
	    }

	    if (n < data.length - 1)
		bwidth = (int)(rxn1 - rxn);
	    else
		bwidth = (int)(rxn - rxn_1);

	    double rwidth = getValue(bwidth);
	    double rw = getValue(bwidth - SPACE_WIDTH);
	    boolean isNA = VAMPUtils.isNA(item);
	    boolean isMissing = VAMPUtils.isMissing(item);
	    // 14/09/04: added +SPACE_WIDTH_2
	    rx += SPACE_WIDTH_2;
	    if (isVisible || offscreen) {
		if (g != null) {
		    if (isMissing)
			g.setColor(Color.BLACK);
		    else
			setGraphics(g, item, dataSet, isNA);
		    g.fillRect((int)rx, (int)ry, (int)rw, (int)rh);
		}
	    }

	    if (pctx == null)
		item.setRBounds(graphElement, rx, ry, rwidth, rheight);
	}
    }

    public void computeVBounds(GraphCanvas canvas, Graphics2D g,
			       GraphElement graphElement, int m) {
	graphElement.resetPaintVBounds();
    }

    public void computeVCoords(GraphCanvas canvas, GraphElement graphElement,
			       int m) {
	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null) return;
	// for dotplot mark & region support
	DataElement data[] = dataSet.getData();
	String lastChr = null;
	int offset = 0;
	boolean is_trs_cls = VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_CLUSTER_TYPE);
	for (int n = 0; n < data.length; n++) {
	    if (!is_trs_cls) {
		String chr = VAMPUtils.getChr(data[n]);
		if (lastChr != null && chr != null && !chr.equals(lastChr))
		    offset += CHR_OFFSET;
		lastChr = chr;
	    }
	    data[n].setVX(graphElement, n+offset);
	    data[n].setVSize(graphElement, 0);
	}
    }

    double getRPadY() {return 0.;}

    private void setGraphics(Graphics2D g, DataElement item,
			     GraphElement graphElement,
			     boolean isNA) {
	if (g == null) return;
	item.setGraphics(g, graphElement);
	if (isNA)
	    g.setColor(VAMPResources.getColor
		       (VAMPResources.CLONE_NA_FG));

    }

    // 1/10/04: why returning false ? => disconnected...
    //boolean selectAfterPaste() {return false;}

    boolean checkGraphElements(LinkedList graphElements) {
	// 5/09/05: disconnected check for supporting profiles of
	// different sizes
	// reported on warnGlobalContext

	/*
	int size = graphElements.size();
	int len = Integer.MIN_VALUE;
	for (int n = 0; n < size; n++) {
	    DataSet dataSet = ((GraphElement)graphElements.get(n)).asDataSet();
	    if (dataSet == null) continue;
	    int l = dataSet.getData().length;
	    if (len != Integer.MIN_VALUE && len != l) {
		return false;
	    }
	    len = l;
	}
	*/

	return true;
    }

    void warnGraphElements(GlobalContext globalContext,
			   LinkedList graphElements) {
	int size = graphElements.size();
	int len = Integer.MIN_VALUE;
	for (int n = 0; n < size; n++) {
	    DataSet dataSet = ((GraphElement)graphElements.get(n)).asDataSet();
	    if (dataSet == null) continue;
	    int l = dataSet.getData().length;
	    if (len != Integer.MIN_VALUE && len != l) {
		InfoDialog.pop(globalContext,
			       "Profiles have different sizes, " +
			       "normalization will be applied");
		Vector v = NormalizeOP.normalize(globalContext, Utils.listToVector(graphElements));
		graphElements.clear();
		graphElements.addAll(v);
		return;
	    }
	    len = l;
	}
    }

    boolean isCompatible(AxisDisplayer axisDisplayer) {
	return axisDisplayer instanceof DotPlotAxisDisplayer;
    }

    boolean isVXRelocated() {return true;}
    boolean needDeltaEndRegion() {return true;}

    boolean useHardThresholds() {
	return false;
    }
}
