
/*
 *
 * PointDataSetDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.*;
import java.awt.geom.*;

import fr.curie.vamp.gui.ProfileDisplayer;
import fr.curie.vamp.gui.Painter;
import fr.curie.vamp.data.Profile;

class PointDataSetDisplayer extends StandardDataSetDisplayer {

    private boolean useArcs;
    private static final int sizeTHR = 6;
    private static final int sizeTHR2 = sizeTHR/2;

    PointDataSetDisplayer(boolean useArcs) {
	this("Point", useArcs);
    }

    PointDataSetDisplayer(String name, boolean useArcs) {
	super(name, null);
	this.useArcs = useArcs;
    }

    static final Color transparentBG = new Color(0, 0, 0, 0);

    public void display(GraphCanvas canvas, Graphics2D g,
			GraphElement graphElement,
			int m, PrintContext pctx) {
	Profile profile = graphElement.asProfile();
	if (profile != null) {
	    if (!graphElement.isVisible()) {
		return;
	    }
	    ProfileDisplayer profDsp = new ProfileDisplayer(isGNLColorCodes(), !showNormal(), showSize(), Painter.POINT_MODE);
	    profDsp.display(canvas, g, graphElement, m, pctx);
	}
	else {
	    display_os(canvas, g, graphElement, m, pctx);
	}

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

	boolean isVisible;
	if (pctx == null) {
	    isVisible = dataSet.isVisible();
	    if (dataSet.getRBounds().height <
		VAMPResources.getDouble(VAMPResources.MIN_HEIGHT_PAINT))
		return;
	}
	else
	    isVisible = true;

	/*
	RepaintManager rpm = RepaintManager.currentManager(canvas);
	boolean double_buffering = rpm.isDoubleBufferingEnabled();
	rpm.setDoubleBufferingEnabled(false);
	*/

	double y0 = canvas.getRY(dataSet.getVBounds().y);

	//System.out.println(graphElement.getID() + " " + dataSet.getRBounds() + " " +  dataSet.getVBounds().y + " -> " + y0);

	if (pctx != null)
	    y0 = pctx.getRY(y0);

	Font font = VAMPResources.getFont(VAMPResources.DATASET_DISPLAYER_FONT);
	int pointWidth = VAMPResources.getInt(VAMPResources.POINT_WIDTH);
	int pointWidth2 = pointWidth/2;
	int pointNAWidth = VAMPResources.getInt(VAMPResources.POINT_NA_WIDTH);
	int pointNAWidth2 = pointNAWidth/2;

	DataElement data[] = dataSet.getData();
	Color probeSetFG = VAMPResources.getColor
	    (VAMPResources.PROBE_SET_FG);
	Color naFG = VAMPResources.getColor
	    (VAMPResources.CLONE_NA_FG);

	boolean isTransMerge = VAMPUtils.getType(dataSet).equals
	    (VAMPConstants.TRANSCRIPTOME_MERGE_TYPE);

	boolean isTrans = isTrans(dataSet);
	
	HashMap cmap;
	if (isTransMerge)
	    cmap = buildTrsColors(dataSet);
	else
	    cmap = null;

	boolean isArrayMerge = VAMPUtils.getType(dataSet).equals
	    (VAMPConstants.CGH_ARRAY_MERGE_TYPE);

	double t_vminY = VAMPUtils.getThresholdMinY(dataSet);
	double t_vmaxY = VAMPUtils.getThresholdMaxY(dataSet);

	Dimension rSize = canvas.getSize();

	if (data.length > 0)
	    GNLCodeManage(data[0].getPropertyValue(VAMPProperties.GNLProp) == null);

	int visible_cnt = 0;
	int lastrx = -1;
	long msn = System.currentTimeMillis();
	DisplayOptimizer dspOptim = new DisplayOptimizer();
	for (int n = 0; n < data.length; n++) {
	    DataElement item = data[n];
	    double rx = item.getRX(graphElement);

	    boolean isNA = VAMPUtils.isNA(item);
	    boolean isMissing = VAMPUtils.isMissing(item);
	    double vy = item.getVY(graphElement);
	    boolean thresholded =
		!isArrayMerge && (vy > t_vmaxY || vy < t_vminY);

	    double size = item.getRSize(graphElement);
	    if (pctx != null)
		size = pctx.getRW(size);

	    double sizeNA = size;
	    int offset_x = 0;
	    if (size < pointWidth) {
		size = pointWidth;
		offset_x = pointWidth2;
	    }

	    if (sizeNA < pointNAWidth)
		sizeNA = pointNAWidth;

	    if (pctx == null) {
		if (isMissing)
		    item.setRBounds(graphElement, 0, 0, 0, 0);
		else if (isNA)
		    item.setRBounds(graphElement, item.getRX(graphElement)-pointNAWidth2,
				    y0-pointNAWidth,
				    sizeNA, pointNAWidth);
		else if (thresholded)
		    item.setRBounds(graphElement, item.getRMiddle(graphElement)-sizeTHR2,
				    item.getRY(graphElement)-sizeTHR2, 
				    sizeTHR, sizeTHR);
		else
		    item.setRBounds(graphElement, item.getRX(graphElement)-offset_x,
				    item.getRY(graphElement)-pointWidth2, 
				    size, pointWidth);

		if (!offScreen) {
		    boolean isVisible_i = canvas.isRR_Visible(item.getRBounds(graphElement));
		    if (!isVisible || !isVisible_i) {
			// testing
			if (!isVisible) {
			    /*
			    System.out.println(graphElement.getID() +
					       " not drawing");
			    */
			    break;
			}
			if (!isVisible_i) {
			    if (item.getRX(graphElement) > canvas.getSize().width) {
				/*
				System.out.println(item.getID() +
						   " *not* drawning " + n);
				*/
				break;
			    }
			}
			continue;
		    }
		}
	    }

	    if (isArrayMerge && (isNA || item.getPosY(graphElement) == 0))
		continue;

	    if (g == null)
		continue;

	    if (isMissing)
		continue;

	    if (dspOptim.alreadyDrawn((int)rx, (int)item.getRY(graphElement), (int)size)) {
		continue;
	    }

	    g.setColor(Color.BLACK);
	    g.setFont(font);

	    visible_cnt++;
	    item.setGraphics(g, dataSet);

	    if (isTrans)
		g.setColor(probeSetFG);
	    else if (isTransMerge)
		g.setColor(getColor(cmap, item));
		
	    if (isNA) {
		g.setColor(naFG);
		if (pctx != null)
		    rx = pctx.getRX(rx);
		/*
		if (sizeNA < pointNAWidth)
		    g.drawLine((int)rx, (int)(y0-pointNAWidth),
			       (int)rx, (int)y0);
		else
		*/
		    g.fillRect((int)(rx-pointNAWidth2),
			       (int)(y0-pointNAWidth),
			       (int)sizeNA, pointNAWidth);
	    }
	    else if (thresholded) 
		drawThresholdedItem(g, graphElement, item, t_vmaxY, pctx,
				    isTrans||isTransMerge);
	    else {
		double ry = item.getRY(graphElement);
		//System.out.print("rx " + rx + ", " + ry + " -> ");
		if (pctx != null) {
		    rx = pctx.getRX(rx);
		    ry = pctx.getRY(ry);
		}
		//System.out.println("rx " + rx + ", " + ry);

		if (size < pointWidth)
		    g.drawLine((int)rx, (int)(ry-1),
			       (int)rx, (int)ry);
		else {
		    g.fillRect((int)rx-offset_x,
			       (int)ry-pointWidth2,
			       (int)size, pointWidth);
		}
	    }
	}

	if (GraphCanvas.VERBOSE)
	    System.out.println(graphElement.getID() + ": drawing end " + (System.currentTimeMillis() - msn) +
			       " already drawn:" + dspOptim.getAlreadyDrawnCount() +
			       " really drawn:" + dspOptim.getDrawnCount() +
			       " length:" + data.length);
	
	//rpm.setDoubleBufferingEnabled(double_buffering);
	tcmManage(g, canvas, graphElement, pctx);
    }
    public void computeVBounds(GraphCanvas canvas, Graphics2D g,
			       GraphElement graphElement, int m) {
	graphElement.resetPaintVBounds();
    }

    void setGraphElements(java.util.LinkedList graphElements) {
	DataSetSizePerformer.getSetSizePerformer().apply(graphElements);
	showSize(true);
    }

    boolean isCompatible(AxisDisplayer axisDisplayer) {
	if (axisDisplayer instanceof DotPlotAxisDisplayer)
	    return false;
	return super.isCompatible(axisDisplayer);
    }
}
