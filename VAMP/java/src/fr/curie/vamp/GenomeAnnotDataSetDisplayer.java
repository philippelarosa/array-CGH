
/*
 *
 * GenomeAnnotDataSetDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.awt.geom.*;

class GenomeAnnotDataSetDisplayer extends StandardDataSetDisplayer {

    private static final int sizeTHR = 6;
    private static final int sizeTHR2 = sizeTHR/2;
    private static final boolean DEBUG_DSP_MODE = false;

    GenomeAnnotDataSetDisplayer() {
	this("GenomeAnnot");
    }

    GenomeAnnotDataSetDisplayer(String name) {
	super(name, null);
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

	double y0 = canvas.getRY(dataSet.getVBounds().y);

	// drawing middle line
	double vy0 = dataSet.getVBounds().y;
	double dsmin = canvas.getMinY();
	double middle = canvas.getRY(vy0 + dsmin);
	double x0 = canvas.getRX(dataSet.getVBounds().x);
	double x1 = canvas.getRX(dataSet.getVBounds().x + dataSet.getVBounds().width);
	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    x1 = pctx.getRX(x1);
	    y0 = pctx.getRY(y0);
	    middle = pctx.getRY(middle);
	}

	g.setColor(Color.GRAY);
	g.drawLine((int)x0, (int)middle, (int)x1, (int)middle);

	int exonHeight = VAMPResources.getInt(VAMPResources.EXON_HEIGHT);
	int intronHeight = VAMPResources.getInt(VAMPResources.INTRON_HEIGHT);

	Font font = VAMPResources.getFont(VAMPResources.DATASET_DISPLAYER_FONT);
	int exonHeight2 = exonHeight/2;

	DataElement data[] = dataSet.getData();

	double t_vminY = VAMPUtils.getThresholdMinY(dataSet);
	double t_vmaxY = VAMPUtils.getThresholdMaxY(dataSet);

	Dimension rSize = canvas.getSize();

	if (data.length > 0)
	    GNLCodeManage(data[0].getPropertyValue(VAMPProperties.GNLProp) == null);

	int lastrx = -1;
	boolean isMerge = (VAMPUtils.getType(dataSet).
			   equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE));
	for (int n = 0; n < data.length; n++) {
	    DataElement item = data[n];
	    double rx = item.getRX(graphElement);

	    double vy = item.getVY(graphElement);

	    double size = item.getRSize(graphElement);
	    if (pctx != null)
		size = pctx.getRW(size);

	    if (pctx == null) {
		if (size < 2)
		    size = 2;
		item.setRBounds(graphElement, item.getRX(graphElement),
				item.getRY(graphElement)-exonHeight2, 
				size, exonHeight);

		if (!offScreen) {
		    boolean isVisible_i = canvas.isRR_Visible(item.getRBounds(graphElement));
		    if (!isVisible || !isVisible_i)
			continue;
		}
	    }

	    if (g == null)
		continue;

	    g.setFont(font);

	    item.setGraphics(g, dataSet);
		
	    double ry = item.getRY(graphElement);
	    if (pctx != null) {
		rx = pctx.getRX(rx);
		ry = pctx.getRY(ry);
	    }
	    
	    double scaleX = Utils.log(canvas.getCanonScale().getScaleX());
	    long lastPos;
	    if (isMerge) {
		Object lastPosO = item.getPropertyValue(VAMPProperties.MergeOffsetProp);
		lastPos = (long)(lastPosO != null ?
				 ((Double)lastPosO).doubleValue() : 0);
	    }
	    else
		lastPos = 0;

	    if (scaleX < MIN_SCALE)
		drawIntrons(canvas, g, rx, size, ry, item, intronHeight,
			    0, lastPos, pctx);
	    else {
		int dsp_exon_cnt = 
		    drawExons(canvas, g, rx, size, ry, item, exonHeight, scaleX,
			      lastPos, pctx, false);
		drawIntrons(canvas, g, rx, size, ry, item, intronHeight,
			    dsp_exon_cnt, lastPos, pctx);
		drawExons(canvas, g, rx, size, ry, item, exonHeight, scaleX,
			  lastPos, pctx, true);
	    }
	}
    }

    private int draw(GraphCanvas canvas, Graphics2D g, 
		     String item, double ry,
		     long lastPos, double scaleX, int height,
		     PrintContext pctx,  boolean draw) {
	String items[] = item.split(":");
	if (items[0].equals("NA"))
	    return 0;
	long begin = Integer.parseInt(items[0]) + lastPos;
	long end = Integer.parseInt(items[1]) + lastPos + 1;
	double rx = canvas.getRX(begin);
	double size = canvas.getRW(end - begin);
	if (pctx != null) {
	    size = pctx.getRW(size);
	}

	if (size < 0.5 && scaleX < MIN_SCALE)
	    return 0;
	if (size < 1) {
	    size = 1;
	}

	if (pctx != null)
	    rx = pctx.getRX(rx);

	if (draw)
	    g.fillRect((int)rx, (int)ry-(height/2),
		       (int)size, height);
	return 1;
    }

    private int drawIntrons(GraphCanvas canvas, Graphics2D g, double s_rx,
			    double tsize,
			    double ry, DataElement item, int intronHeight,
			    int dsp_exon_cnt, long lastPos, PrintContext pctx) {
	int intronHeight2 = intronHeight/2;
	Color intronFG = VAMPResources.getColor(VAMPResources.INTRON_FG);
	g.setColor(intronFG);
	String intron_str = (String)item.getPropertyValue(VAMPProperties.IntronsProp);
	if (tsize < 1 || dsp_exon_cnt == 0 || intron_str == null) {
	    if (tsize < 1)
		tsize = 1;
	    g.fillRect((int)s_rx, (int)ry-intronHeight2,
		       (int)tsize, intronHeight);
	    return 0;
	}

	String introns[] = intron_str.split("\\|");
	int dsp_cnt = 0;
	for (int m = 0; m < introns.length; m++) {
	    dsp_cnt += draw(canvas, g, introns[m],  ry, lastPos, Double.MAX_VALUE, intronHeight, pctx, true);
	}

	if (dsp_cnt == 0)
	    g.fillRect((int)s_rx, (int)ry-intronHeight2,
		       (int)tsize, intronHeight);

	return dsp_cnt;
    }
    
    private int drawExons(GraphCanvas canvas, Graphics2D g, double s_rx,
			  double tsize, double ry,
			  DataElement item, int exonHeight, double scaleX,
			  long lastPos, PrintContext pctx, boolean draw) {
	if (tsize < 1)
	    return 0;

	String exon_str = (String)item.getPropertyValue(VAMPProperties.ExonsProp);
	if (exon_str == null)
	    return 0;

	int exonHeight2 = exonHeight/2;
	Color exonFG = VAMPResources.getColor(VAMPResources.EXON_FG);
	g.setColor(exonFG);

	String exons[] = exon_str.split("\\|");
	int dsp_cnt = 0;
	for (int m = 0; m < exons.length; m++) {
	    dsp_cnt += draw(canvas, g, exons[m], ry, lastPos, scaleX, exonHeight, pctx, draw);
	}

	int utrHeight = VAMPResources.getInt(VAMPResources.UTR_HEIGHT);
	String utr5_str = (String)item.getPropertyValue(Property.getProperty("Utr5"));
	if (utr5_str != null) {
	    Color utr5FG = VAMPResources.getColor(VAMPResources.UTR5_FG);
	    g.setColor(utr5FG);
	    String utr5s[] = utr5_str.split("\\|");
	    if (DEBUG_DSP_MODE) {
		for (int m = 0; m < utr5s.length; m++) {
		    dsp_cnt += draw(canvas, g, utr5s[m], ry+exonHeight2, lastPos, scaleX, utrHeight, pctx, draw);
		    dsp_cnt += draw(canvas, g, utr5s[m], ry-exonHeight2, lastPos, scaleX, utrHeight, pctx, draw);
		}
	    }
	    else
		for (int m = 0; m < utr5s.length; m++)
		    dsp_cnt += draw(canvas, g, utr5s[m], ry, lastPos, scaleX, exonHeight, pctx, draw);
	}

	String utr3_str = (String)item.getPropertyValue(Property.getProperty("Utr3"));
	if (utr3_str != null) {
	    Color utr3FG = VAMPResources.getColor(VAMPResources.UTR3_FG);
	    g.setColor(utr3FG);
	    String utr3s[] = utr3_str.split("\\|");
	    if (DEBUG_DSP_MODE) {
		for (int m = 0; m < utr3s.length; m++) {
		    dsp_cnt += draw(canvas, g, utr3s[m], ry+exonHeight2, lastPos, scaleX, utrHeight, pctx, draw);
		    dsp_cnt += draw(canvas, g, utr3s[m], ry-exonHeight2, lastPos, scaleX, utrHeight, pctx, draw);
		}
	    }
	    else
		for (int m = 0; m < utr3s.length; m++)
		    dsp_cnt += draw(canvas, g, utr3s[m], ry, lastPos, scaleX, exonHeight, pctx, draw);
	}

	return dsp_cnt;
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
