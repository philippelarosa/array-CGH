
/*
 *
 * BarplotDataSetDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import fr.curie.vamp.data.Profile;
import fr.curie.vamp.gui.ProfileDisplayer;
import fr.curie.vamp.gui.Painter;

public class BarplotDataSetDisplayer extends StandardDataSetDisplayer {

    static final Color transparentBG = new Color(0, 0, 0, 0);
    boolean draw_centered = false;

    public BarplotDataSetDisplayer() {
	this("Barplot");
    }

    BarplotDataSetDisplayer(String name) {
	super(name, null);
    }

    BarplotDataSetDisplayer(String name, boolean draw_centered) {
	super(name, null);
	this.draw_centered = draw_centered;
    }

    BarplotDataSetDisplayer(boolean draw_centered) {
	this((draw_centered ? "Centered " : "") + "Barplot", draw_centered);
    }

    public void display(GraphCanvas canvas, Graphics2D g,
			GraphElement graphElement,
			int dummy, PrintContext pctx) {

	Profile profile = graphElement.asProfile();
	if (profile != null) {
	    if (!graphElement.isVisible()) {
		return;
	    }
	    ProfileDisplayer profDsp = new ProfileDisplayer(isGNLColorCodes(), !showNormal(), showSize(), Painter.BARPLOT_MODE | (draw_centered ? Painter.CENTERED_MODE : 0));
	    profDsp.display(canvas, g, graphElement, dummy, pctx);
	}
	else {
	    display_os(canvas, g, graphElement, dummy, pctx);
	}

	display_x(canvas, g, graphElement, dummy, pctx);
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
	    isVisible = canvas.isVR_Visible(dataSet.getVBounds());
	    if (dataSet.getRBounds().height <
		VAMPResources.getDouble(VAMPResources.MIN_HEIGHT_PAINT))
		return;
	}
	else
	    isVisible = true;

	Font font = VAMPResources.getFont(VAMPResources.DATASET_DISPLAYER_FONT);
	int lineWidth = VAMPResources.getInt(VAMPResources.LINE_WIDTH);
	int lineWidth2 = lineWidth/2;
	int pointNAWidth = VAMPResources.getInt(VAMPResources.POINT_NA_WIDTH);
	int pointNAWidth2 = pointNAWidth/2;
	int ovalWidth = VAMPResources.getInt(VAMPResources.OVAL_WIDTH);
	int ovalWidth2 = ovalWidth/2;
	DataElement data[] = dataSet.getData();

	double vy0 = graphElement.getVBounds().y;
	double y0 = canvas.getRY(vy0);
	double by0 = y0;

	if (draw_centered) {
	    double dsmin = canvas.getMinY();
	    y0 = canvas.getRY(vy0 + dsmin);
	}

	double x0 = canvas.getRX(graphElement.getVBounds().x);
	double xn = canvas.getRX(graphElement.getVBounds().x +
				 graphElement.getVBounds().width);

	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    xn = pctx.getRX(xn);
	    by0 = pctx.getRY(by0);
	    y0 = pctx.getRY(y0);
	}

	Color itemCenteredFG = VAMPResources.getColor
	    (VAMPResources.ITEM_CENTERED_FG);
	Color naFG = VAMPResources.getColor
	    (VAMPResources.CLONE_NA_FG);
	Color probeSetFG = VAMPResources.getColor
	    (VAMPResources.PROBE_SET_FG);

	boolean isTrans = isTrans(dataSet);

	double t_minY = VAMPUtils.getThresholdMinY(dataSet);
	double t_maxY = VAMPUtils.getThresholdMaxY(dataSet);

	if (data.length > 0)
	    GNLCodeManage(data[0].getPropertyValue(VAMPProperties.GNLProp) == null);

	for (int n = 0; n < data.length; n++) {
	    DataElement item = data[n];
	    boolean isNA = VAMPUtils.isNA(item);
	    boolean isMissing = VAMPUtils.isMissing(item);
	    if (pctx == null) {
		if (isMissing)
		    item.setRBounds(graphElement, 0, 0, 0, 0);
		else if (isNA)
		    item.setRBounds(graphElement, (int)(item.getRX(graphElement)-pointNAWidth2),
				    by0-pointNAWidth, pointNAWidth, pointNAWidth);
		else {
		    double y_b, y_t;
		    if (item.getRY(graphElement) > y0) {
			y_b = item.getRY(graphElement);
			y_t = y0;
		    }
		    else {
			y_b = y0;
			y_t = item.getRY(graphElement);
		    }

		    item.setRBounds(graphElement, (int)(item.getRX(graphElement)-EPSILON), (int)y_t,
				    2*EPSILON, (int)(y_b-y_t));

		    /*
		      item.setRBounds((int)(item.getRX()-EPSILON),
		      (int)(item.getRY()-EPSILON), 
		      2*EPSILON, (int)(y0 - item.getRY()));
		    */
		}

		if (!offScreen) {
		    boolean isVisible_i = canvas.isRR_Visible(item.getRBounds(graphElement));
		    if (!isVisible || !isVisible_i)
			continue;
		}
	    }

	    if (g == null)
		continue;

	    if (isMissing)
		continue;

	    g.setFont(font);
	    item.setGraphics(g, dataSet);
	    if (isTrans)
		g.setColor(probeSetFG);
	    if (isNA) {
		g.setColor(naFG);
		double rx = item.getRX(graphElement);
		if (pctx != null)
		    rx = pctx.getRX(rx);
		g.fillRect((int)(rx-pointNAWidth2),
			   (int)(by0-pointNAWidth),
			   pointNAWidth, pointNAWidth);
	    }
	    else {
		double rx = item.getRX(graphElement);
		double ry = item.getRY(graphElement);
		if (pctx != null) {
		    rx = pctx.getRX(rx);
		    ry = pctx.getRY(ry);
		}

		if (lineWidth <= 1)
		    g.drawLine((int)rx, (int)y0,
			       (int)rx, (int)ry);
		else
		    g.fillRect((int)rx - lineWidth2,
			       (int)ry,
			       lineWidth, (int)(y0 - ry));
	    }

	    if (!isNA && (item.getVY(graphElement) > t_maxY || item.getVY(graphElement) < t_minY))
		drawThresholdedItem(g, graphElement, item, t_maxY, pctx, false);
	}

	if (draw_centered && g != null) {
	    g.setColor(Color.BLACK);
	    g.drawLine((int)x0, (int)y0, (int)xn, (int)y0);
	}
    }

    public void computeVBounds(GraphCanvas canvas, Graphics2D g,
			       GraphElement graphElement, int m) {
	graphElement.resetPaintVBounds();
    }

    void setGraphElements(java.util.LinkedList graphElements) {
	DataSetSizePerformer.getUnsetSizePerformer().apply(graphElements);
	showSize(false);
    }

    boolean drawCentered() {return draw_centered;}
    void drawCentered(boolean draw_centered) {this.draw_centered = draw_centered;}    boolean isCompatible(AxisDisplayer axisDisplayer) {
	if (axisDisplayer instanceof DotPlotAxisDisplayer)
	    return false;
	return super.isCompatible(axisDisplayer);
    }
}
