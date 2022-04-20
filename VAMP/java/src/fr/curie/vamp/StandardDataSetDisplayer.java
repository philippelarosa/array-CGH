
/*
 *
 * StandardDataSetDisplayer.java
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
import java.awt.image.*;
import java.awt.font.*;

import fr.curie.vamp.data.*;
import fr.curie.vamp.gui.*;

public abstract class StandardDataSetDisplayer extends CommonDataSetDisplayer {

    protected static final int EPSILON = 2;
    private static final int CHRSET_CHR_IDX = 0;
    private static final int CHRSET_OFFSET_IDX = 1;
    private static final int CHRSET_SIZE = 2;
    boolean show_normal, show_bkp, show_smt_lns, show_smt_pts, show_ctm, show_out,
	show_size, show_tag, show_tag_string, show_info, show_gene_info,
	show_out_of_bounds_info, show_links;
    boolean smt_lines;

    StandardDataSetDisplayer(String name, GraphElementIDBuilder graphElementIDBuilder) {
	super(name, graphElementIDBuilder);
	show_normal = true;
	show_bkp = false;
	show_smt_lns = false;
	show_smt_pts = false;
	show_ctm = false;
	show_out = false;
	show_size = true;
	// 31/03/06
	/*
	show_tag = true;
	show_tag_string = true;
	*/
	show_tag = false;
	show_tag_string = false;

	show_info = false;
	show_gene_info = false;
	show_out_of_bounds_info = false;
	show_links = false;
	smt_lines = false;
    }

    boolean showNormal() {return show_normal;}
    boolean showBreakpoints() {return show_bkp;}
    boolean showSmoothingLines() {return show_smt_lns;}
    boolean showSmoothingPoints() {return show_smt_pts;}
    boolean showCentromere() {return show_ctm;}
    boolean showOut() {return show_out;}
    boolean showSize() {return show_size;}
    boolean showTag() {return show_tag;}
    boolean showTagString() {return show_tag_string;}
    boolean showInfo() {return show_info;}
    boolean showGeneInfo() {return show_gene_info;}
    boolean showOutOfBoundsInfo() {return show_out_of_bounds_info;}
    boolean showLinks() {return show_links;}

    boolean showNormalEnabled() {return true;}
    boolean showBreakpointsEnabled() {return true;}
    boolean showSmoothingLinesEnabled() {return true;}
    boolean showSmoothingPointsEnabled() {return true;}
    boolean showCentromereEnabled() {return true;}
    boolean showOutEnabled() {return true;}
    boolean showSizeEnabled() {return true;}
    boolean showTagEnabled() {return true;}
    boolean showTagStringEnabled() {return true;}
    boolean showInfoEnabled() {return true;}
    boolean showOutOfBoundsInfoEnabled() {return true;}
    boolean showLinksEnabled() {return true;}

    void showNormal(boolean show_normal) {this.show_normal = show_normal;}
    void showBreakpoints(boolean show_bkp) {this.show_bkp = show_bkp;}
    void showSmoothingPoints(boolean show_smt_pts) {this.show_smt_pts = show_smt_pts;}
    void showSmoothingLines(boolean show_smt_lns) {this.show_smt_lns = show_smt_lns;}
    void showCentromere(boolean show_ctm) {this.show_ctm = show_ctm;}
    void showOut(boolean show_out) {this.show_out = show_out;}
    void showSize(boolean show_size) {this.show_size = show_size;}
    void showTag(boolean show_tag) {
	this.show_tag = show_tag;
	if (!show_tag)
	    show_tag_string = false;
    }

    void showTagString(boolean show_tag_string) {
	this.show_tag_string = show_tag_string;
	if (show_tag_string)
	    show_tag = true;
    }

    void showInfo(boolean show_info) {
	this.show_info = show_info;
    }

    void showGeneInfo(boolean show_gene_info) {
	this.show_gene_info = show_gene_info;
    }

    void showOutOfBoundsInfo(boolean show_out_of_bounds_info) {
	this.show_out_of_bounds_info = show_out_of_bounds_info;
    }

    void showLinks(boolean show_links) {
	this.show_links = show_links;
    }

    private static boolean isIn(double y, double by, double ty) {
	return y >= ty && y <= by;
    }

    private static double thrVal(double y, double by, double ty) {
	if (y >= ty && y <= by)
	    return y;
	if (y < ty)
	    return ty;
	return by;
    }

    public void display_x(GraphCanvas canvas, Graphics2D g,
			  GraphElement graphElement,
			  int dummy, PrintContext pctx) {
	if (g == null) {
	    return;
	}

	if (canvas.isRotated()) {
	    return;
	}

	boolean isVisible = graphElement.isVisible();
	if (!isVisible && pctx == null) {
	    return;
	}

	if (canvas.getCenteredSet() == graphElement) {
	    DataElement centerElem = canvas.getCenteredElem();
	    if (centerElem != null) {
		displayCentered(g, canvas, graphElement, centerElem, pctx);
	    }
	    else {
		Probe centerProbe = canvas.getCenteredProbe();
		if (centerProbe != null) {
		    displayCentered(g, canvas, graphElement, centerProbe, pctx);
		}
	    }
	}

	if (canvas.getPinnedUpSet() == graphElement) {
	    DataElement centerElem = canvas.getPinnedUpElem();
	    if (centerElem != null)
		displayPinnedUp(g, canvas, graphElement, centerElem, pctx);
	}

	if (!show_bkp && !show_out && !show_smt_pts && !show_smt_lns &&
	    !show_ctm && !show_tag && !show_info && !show_gene_info &&
	    !show_out_of_bounds_info &&
	    !show_links)
	    return;

	Profile profile = graphElement.asProfile();
	if (profile != null) {
	    display_x(canvas, g, profile, dummy, pctx);
	    return;
	}

	DataSet dataSet = graphElement.asDataSet();
	if (dataSet != null) {
	    display_x(canvas, g, dataSet, dummy, pctx);

	}
    }

    public void display_x(GraphCanvas canvas, Graphics2D g,
			  Profile profile,
			  int dummy, PrintContext pctx) {

	Rectangle2D.Double vbounds = profile.getVBounds();
	Painter painter = new Painter(profile.getGraphicProfile(), isGNLColorCodes(), false, true);
	fr.curie.vamp.gui.Scale scale = painter.makeScale(canvas, profile);

	double by = canvas.getRY(vbounds.y);
	double ty = canvas.getRY(vbounds.y - vbounds.height);

	if (pctx != null) {
	    by = pctx.getRY(by);
	    ty = pctx.getRY(ty);
	}

	if (show_bkp) {
	    // TBD: pctx
	    Color breakpointFG = VAMPResources.getColor(VAMPResources.BREAKPOINT_FG);
	    g.setColor(breakpointFG);

	    int bkp[] = profile.getBreakpoints();
	    for (int n = 0; n < bkp.length; n++) {
		try {
		    Probe p = profile.getProbe(bkp[n]);
		    //double x = canvas.getRX(p.getPanGenPos(profile));
		    double x = scale.getX(p.getPanGenPos(profile));
		    if (pctx != null) {
			x = (int)pctx.getRX(x);
		    }
		    drawBreakpoint(g, (int)x, (int)by, (int)ty);
		}
		catch(Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	if (show_out) {
	    int out[] = profile.getOutliers();
	    int pointOutWidth = VAMPResources.getInt
		(VAMPResources.POINT_OUT_WIDTH);

	    for (int n = 0; n < out.length; n++) {
		try {
		    Probe p = profile.getProbe(out[n]);
		    double rx = scale.getX(p.getPanGenPos(profile));
		    double ry = scale.getY(p.getPosY(profile));
		    double size = scale.getWidth(p.getSize());
		    if (pctx != null) {
			rx = (int)pctx.getRX(rx);
			ry = (int)pctx.getRY(ry);
			size = (int)pctx.getRW(size);
		    }

		    if (size < pointOutWidth)
			size = pointOutWidth;

		    g.setColor(VAMPResources.getColor(VAMPResources.CLONE_OUTLIER_FG));
		    double ry_t = thrVal(ry, by, ty);
		    g.drawRect((int)rx-1, (int)ry_t-1, (int)size+1, pointOutWidth+1);
		}
		catch(Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	if (show_ctm) {
	    showCtm(canvas, g, profile, by, ty, pctx);
	}

	if (show_smt_pts) {
	    System.out.println("show_smt_pts: " + profile.getSmoothings().length);
	}

	if (show_smt_lns) {
	    Color smoothingLineFG =
		VAMPResources.getColor(VAMPResources.SMOOTHING_LINE_FG);
	    g.setColor(smoothingLineFG);
	    double minY = canvas.getMinY();
	    int smt[] = profile.getSmoothings();
	    int sz = smt.length;
	    double orx = 0;
	    double ory = 0;
	    try {
		for (int n = 0; n < sz; n++) {
		    Probe p = profile.getProbe(smt[n]);
		    if (p.isNA()) {
			continue;
		    }
		    double rx = scale.getX(p.getPanGenPos(profile));
		    double ry = scale.getY(p.getSmoothing());
		    if (pctx != null) {
			rx = pctx.getRX(rx);
			ry = pctx.getRY(ry);
		    }
		    double ry_t = thrVal(ry, by, ty);
		    if (n > 0) {
			g.drawLine((int)orx, (int)ory, (int)rx, (int)ory);
			g.drawLine((int)rx, (int)ory, (int)rx, (int)ry_t);
		    }
		    orx = rx;
		    ory = ry_t;
		    
		}
	    }
	    catch(Exception e) {
	    }

	    double rx = canvas.getRX(vbounds.x + vbounds.width);
	    g.drawLine((int)orx, (int)ory, (int)rx, (int)ory);
	}

	// and so on...
    }

    public void display_x(GraphCanvas canvas, Graphics2D g,
			  DataSet dataSet,
			  int dummy, PrintContext pctx) {
	DataElement data[] = dataSet.getData();
	Rectangle2D.Double vbounds = dataSet.getVBounds();
	double minY = canvas.getMinY();
	int ty = (int)canvas.getRY(vbounds.y - vbounds.height);

	int by = (int)canvas.getRY(vbounds.y);
	if (pctx != null) {
	    by = (int)pctx.getRY(by);
	    ty = (int)pctx.getRY(ty);
	}

	int smtw = VAMPResources.getInt(VAMPResources.SMOOTHING_POINT_WIDTH);
	int smtw2 = smtw/2;
	Color smoothingLineFG =
	    VAMPResources.getColor(VAMPResources.SMOOTHING_LINE_FG);
	Color smoothingPointFG =
	    VAMPResources.getColor(VAMPResources.SMOOTHING_POINT_FG);

	double lastrx = 0, lastry = 0;
	String lastvdy = "";

	if (show_ctm) {
	    showCtm(canvas, g, dataSet, by, ty, pctx);
	}

	if (!show_bkp && !show_out && !show_smt_pts &&
	    !show_smt_lns && !show_tag && !show_info && !show_gene_info &&
	    !show_out_of_bounds_info && !show_links)
	    return;

	boolean r_show_links = reallyShowLinks(canvas, dataSet);

	long msn = System.currentTimeMillis();
	int smt_cnt = 0;

	if (show_smt_lns) {
	    long ms = System.currentTimeMillis();
	    Vector v = SmoothingLineEngine.computeSmoothingLines(dataSet);
	    int sz = v.size();
	    double orx = 0;
	    double ory = 0;
	    g.setColor(smoothingLineFG);
	    for (int n = 0; n < sz; n++) {
		SmoothingLineEngine.SmoothPoint sp = (SmoothingLineEngine.SmoothPoint)v.get(n);
		double rx = canvas.getRX(sp.posx);
		double ry = canvas.getRY(vbounds.y - sp.smty + minY);
		//System.out.println("std #" + n + " " + sp.posx + " " + sp.smty);
		if (pctx != null) {
		    rx = pctx.getRX(rx);
		    ry = pctx.getRY(ry);
		}
		double ry_t = thrVal(ry, by, ty);
		if (n > 0) {
		    if (ry_t == ry || ory != ry_t)
			g.drawLine((int)orx, (int)ory, (int)rx, (int)ry_t);
		}
		orx = rx;
		ory = ry_t;

	    }
	    //System.out.println("time #1 " + (System.currentTimeMillis() - ms));
	}


	long ms = System.currentTimeMillis();

	for (int n = 0; n < data.length; n++) {
	    DataElement item = data[n];
	    boolean isVisible_i = canvas.isRR_Visible(item.getRBounds(dataSet));
	    String value;

	    if (r_show_links)
		showLinks(canvas, g, dataSet, item, pctx);

	    if (show_bkp)
		showBkp(canvas, g, dataSet, data, n, by, ty, pctx);

	    if (isVisible_i) {
		if (showOut(item))
		    displayOut(g, dataSet, item, pctx);

		if (show_tag && VAMPUtils.hasTag(item))
		    displayTag(g, canvas, dataSet, item, pctx);

		if (show_info) {
		    if (VAMPUtils.getType(item).equals("Gene"))
			drawGeneInfo(g, dataSet, item, pctx);
		    else
			drawInfo(g, dataSet, item, pctx);
		}
		else if (show_gene_info)
		    drawGeneInfo(g, dataSet, item, pctx);
		else if (show_out_of_bounds_info && !VAMPUtils.isNA(item)) {

		    double t_vminY = VAMPUtils.getThresholdMinY(dataSet);
		    double t_vmaxY = VAMPUtils.getThresholdMaxY(dataSet);
		    
		    boolean thresholded =
			(item.getVY(dataSet) > t_vmaxY ||
			 item.getVY(dataSet) < t_vminY);
		    if (thresholded)
			drawInfo(g, dataSet, item, pctx);
		}
	    }

	    if (show_smt_pts || show_smt_lns) {

		value = (String)item.getPropertyValue(VAMPProperties.SmoothingProp);
		if (value != null && !value.equals("NA")) {
		    if (GraphCanvas.DEBUG1 && lastvdy.equals(value))
			continue;
		    lastvdy = value;
		    double dy = Utils.parseDouble(value);
		    dy = canvas.getRY(vbounds.y - dy + minY);
		    if (pctx != null)
			dy = pctx.getRY(dy);

		    if (show_smt_pts) {
			if (isIn(dy, by, ty)) {
			    g.setColor(smoothingPointFG);
			    double rx = item.getRX(dataSet);
			    if (pctx != null)
				rx = pctx.getRX(rx);
			    g.fillRect((int)(rx-smtw2),
				       (int)(dy-smtw2),
				       smtw, smtw);
			}
		    }
		}
	    }
	}

	//System.out.println("time #2 " + (System.currentTimeMillis() - ms));
    }

    void showBkp(GraphCanvas canvas, Graphics g, GraphElement graphElement,
		 DataElement data[],
		 int n, int by, int ty, PrintContext pctx) {
	if (g == null) return;
	DataElement item = data[n];
	/*
	String value = (String)item.getPropertyValue(VAMPConstants.BreakpointProp);
	if (value != null && value.equals("1")) {
	*/
	int bkp = item.getBkp();
	if (bkp == 1) {
	    Color breakpointFG = VAMPResources.getColor(VAMPResources.BREAKPOINT_FG);
	    g.setColor(breakpointFG);
	    double wx;
	    double rx = item.getRX(graphElement);
	    double rx1 = data[n+1].getRX(graphElement);
	    if (pctx != null) {
		rx = pctx.getRX(rx);
		rx1 = pctx.getRX(rx1);
	    }

	    if (n < data.length - 1) 
		wx = rx + (rx1 - rx) / 2.;
	    else
		wx = rx;
	    drawBreakpoint(g, (int)wx, by, ty);
	}
    }

    void showCtm(GraphCanvas canvas, Graphics g, GraphElement graphElement,
		 double by, double ty, PrintContext pctx) {
	if (g == null) {
	    return;
	}

	String OS = VAMPUtils.getOS(graphElement);
	DataSet dataSet = graphElement.asDataSet();
	Profile profile = graphElement.asProfile();

	if (dataSet != null) {
	    HashSet set = getChromosomeSet(dataSet);
	    Iterator it = set.iterator();

	    while (it.hasNext()) {
		Object o[] = (Object[])it.next();
		Chromosome chr = VAMPUtils.getChromosome
		    (canvas.getGlobalContext(),
		     OS,
		     (String)o[CHRSET_CHR_IDX]);

		if (chr == null)
		    continue;
		
		displayChr(canvas, g, graphElement, chr, pctx, by, ty);
	    }
	}
	else if (profile != null) {
	    int chrBegProbeNumMap[] = profile.getChrBegProbeNumMap();
	    for (int n = 0; n < chrBegProbeNumMap.length; n++) {
		try {
		    if (chrBegProbeNumMap[n] < 0) {
			break;
		    }

		    Chromosome chr = VAMPUtils.getChromosome
			(canvas.getGlobalContext(),
			 OS,
			 (String)profile.getProbe(chrBegProbeNumMap[n], true).getProp(VAMPProperties.ChromosomeProp));

		    if (chr == null)
			continue;
		    
		    displayChr(canvas, g, graphElement, chr, pctx, by, ty);
		}
		catch(Exception e) {
		}
	    }
	}
    }

    void displayChr(GraphCanvas canvas, Graphics g, GraphElement graphElement, Chromosome chr, PrintContext pctx, double by, double ty)
    {
	int ctmw = VAMPResources.getInt(VAMPResources.CENTROMERE_BASE_WIDTH);
	Color centromereFG = VAMPResources.getColor(VAMPResources.CENTROMERE_FG);
	long cen_pos = VAMPUtils.isMergeChr(graphElement) ?
	    chr.getCentromerePos_o() :  chr.getCentromerePos();
	if (cen_pos < 0)
	    return;

	double wx = canvas.getRX(cen_pos);
	if (pctx != null)
	    wx = pctx.getRX(wx);

	g.setColor(centromereFG);
	int iby = (int)by;
	int ity = (int)ty;
	g.drawLine((int)wx-ctmw, iby, (int)wx, iby-ctmw);
	g.drawLine((int)wx+ctmw, iby, (int)wx, iby-ctmw);
	g.drawLine((int)wx, iby-ctmw, (int)wx, ity+ctmw);
	g.drawLine((int)wx-ctmw, ity, (int)wx, ity+ctmw);
	g.drawLine((int)wx+ctmw, ity, (int)wx, ity+ctmw);
    }

    boolean showOut(DataElement item) {
	if (!show_out) return false;
	return VAMPUtils.isOutlier(item);
    }

    void displayOut(Graphics g, GraphElement graphElement, DataElement item,
		    PrintContext pctx) {
	if (g == null) return;

	int pointOutWidth = VAMPResources.getInt
	    (VAMPResources.POINT_OUT_WIDTH);
	int pointOutWidth2 = pointOutWidth/2;
	// 26/01/04: Outlier keep the same color than other points
	//g.setColor(VAMPResources.getColor(VAMPResources.CLONE_OUTLIER_FG));

	// EV added 24/06/04:
	item.setGraphics((Graphics2D)g, graphElement);

	double rx = item.getRX(graphElement);
	double ry = item.getRY(graphElement);
	if (pctx != null) {
	    rx = pctx.getRX(rx);
	    ry = pctx.getRY(ry);
	}

	double size = item.getRSize(graphElement);
	if (pctx != null) {
	    size = pctx.getRW(size);
	}

	if (size < pointOutWidth)
	    size = pointOutWidth;
	g.drawRect((int)rx, (int)ry-pointOutWidth2,
		   (int)size, pointOutWidth);

	// 24/06/04: round with outlier color
	g.setColor(VAMPResources.getColor(VAMPResources.CLONE_OUTLIER_FG));
	g.drawRect((int)rx-1, (int)ry-pointOutWidth2-1,
		   (int)size+2, pointOutWidth+2);
    }

    void showLinks(GraphCanvas canvas, Graphics2D g,
		   GraphElement graphElement,
		   DataElement item, PrintContext pctx) {

	Vector v = (Vector)
	    item.getPropertyValue(VAMPProperties.LinkedDataProp);
	if (v == null)
	    return;
	/*
	System.out.println("showLinks: " + item.getInd() + ", " +
			   item.getID() + " " +
			   item.getPropertyValue(VAMPConstants.LinkedDataProp));
	*/
	/*
	DataElement lnk_item = (DataElement)
	    item.getPropertyValue(VAMPConstants.LinkedDataProp);
	if (lnk_item == null)
	    return;
	*/
	
	g.setColor(Color.GRAY);
	int size = v.size();
	for (int n = 0; n < size; n++) {
	    DataElement lnk_item = (DataElement)v.get(n);
	    System.out.println(lnk_item.getID() + ": ind: " + lnk_item.getInd());
	    double rx = item.getRX(graphElement);
	    double ry = item.getRY(graphElement);
	    double lnk_rx = lnk_item.getRX(graphElement);
	    double lnk_ry = lnk_item.getRY(graphElement);
	    if (pctx != null) {
		rx = pctx.getRX(rx);
		ry = pctx.getRY(ry);
		lnk_rx = pctx.getRX(lnk_rx);
		lnk_ry = pctx.getRY(lnk_ry);
	    }

	    g.drawLine((int)rx, (int)ry, (int)lnk_rx, (int)lnk_ry);
	}
    }

    void displayTag(Graphics2D g, GraphCanvas canvas,
		    GraphElement graphElement, DataElement item,
		    PrintContext pctx) {
	if (g == null) {
	    return;
	}

	int centerWidth = VAMPResources.getInt(VAMPResources.ITEM_TAG_SIZE);
	Color itemCenteredFG = VAMPResources.getColor
	    (VAMPResources.ITEM_TAG_FG);

	g.setColor(itemCenteredFG);
	double rmiddle = item.getRMiddle(graphElement);
	double ry = item.getRY(graphElement);

	if (pctx != null) {
	    rmiddle = pctx.getRX(rmiddle);
	    ry = pctx.getRY(ry);
	}

	if (VAMPUtils.isNA(item)) {
	    double y0 = canvas.getRY(graphElement.getVBounds().y);
	    if (pctx != null)
		y0 = pctx.getRY(y0);

	    ry = y0;
	}

	int x = (int)rmiddle-centerWidth/4;
	int y = (int)(ry-centerWidth/2) - 2 * centerWidth;
	g.fillRect(x, y, centerWidth/4, 2 * centerWidth);

	if (show_tag_string) {
	    String tag = VAMPUtils.getTag(item);
	    Font textFont =
		VAMPResources.getFont(VAMPResources.AXIS_Y_NAME_DISPLAYER_FONT);
	    g.setFont(textFont);
	    Dimension sz = Utils.getSize(g, tag);
	    g.setColor(Color.BLACK);
	    g.drawString(tag, x - sz.width/2, y-4);
	}
    }

    void displayCentered(Graphics2D g, GraphCanvas canvas,
			 GraphElement graphElement,
			 DataElement item, PrintContext pctx) {
	displayMark(g, canvas, graphElement, item, null, pctx, true);
    }

    void displayCentered(Graphics2D g, GraphCanvas canvas,
			 GraphElement graphElement,
			 Probe probe, PrintContext pctx) {
	displayMark(g, canvas, graphElement, null, probe, pctx, true);
    }

    void displayPinnedUp(Graphics2D g, GraphCanvas canvas,
			 GraphElement graphElement,
			 DataElement item, PrintContext pctx) {
	displayMark(g, canvas, graphElement, item, null, pctx, false);
    }

    void displayMark(Graphics2D g, GraphCanvas canvas,
		     GraphElement graphElement,
		     DataElement item, Probe probe, PrintContext pctx, boolean type) {
	if (g == null) {
	    return;
	}

	int markWidth;
	Color itemMarkedFG;
	if (type) {
	    markWidth = VAMPResources.getInt(VAMPResources.ITEM_CENTERED_SIZE);
	    itemMarkedFG = VAMPResources.getColor(VAMPResources.ITEM_CENTERED_FG);
	}
	else {
	    markWidth = VAMPResources.getInt(VAMPResources.ITEM_PINNED_UP_SIZE);
	    itemMarkedFG = VAMPResources.getColor(VAMPResources.ITEM_PINNED_UP_FG);
	}

	g.setColor(itemMarkedFG);

	double ry, rmiddle;
	boolean isna;

	if (item != null) {
	    ry = item.getRY(graphElement);
	    rmiddle = item.getRMiddle(graphElement);

	    if (pctx != null) {
		rmiddle = pctx.getRX(rmiddle);
		ry = pctx.getRY(ry);
	    }

	    isna = VAMPUtils.isNA(item);
	}
	else {
	    Profile profile = graphElement.asProfile();
	    Painter painter = new Painter(profile.getGraphicProfile(), isGNLColorCodes(), false, true);
	    fr.curie.vamp.gui.Scale scale = painter.makeScale(canvas, profile);
	    ry = scale.getY(probe.getPosY(graphElement));
	    rmiddle = scale.getX(probe.getPanGenPosMiddle(profile));
	    isna = probe.isNA();
	}

	if (isna) {
	    double y0 = canvas.getRY(graphElement.getVBounds().y);
	    if (pctx != null) {
		y0 = pctx.getRY(y0);
	    }
	    
	    ry = y0;
	}

	int x = (int)rmiddle-markWidth/4;
	int y = (int)(ry-markWidth/2) - 2 * markWidth;
	g.fillRect(x, y, markWidth/2, 2 * markWidth);
    }

    static void drawBreakpoint(Graphics g, int x0, int y0, int y1) {
	drawVerticalDashLine(g, x0, y0, y1,
			     VAMPResources.getInt
			     (VAMPResources.BREAKPOINT_DASH_WIDTH),
			     VAMPResources.getInt
			     (VAMPResources.BREAKPOINT_DASH_PADDING));
    }

    static void drawVerticalDashLine(Graphics g, int x0, int y0, int y1,
				     int breakpointDashWidth,
				     int breakpointDashPadding) {
	if (g == null) return;
	int step_sum = breakpointDashWidth + breakpointDashPadding;
	
	for (int y = y0; y > y1; y -= step_sum) {
	    if (y - breakpointDashWidth > y1)
		g.drawLine(x0, y, x0, y - breakpointDashWidth);
	}
	g.drawLine(x0, y1 + breakpointDashWidth, x0, y1);
    }

    void drawInfo(Graphics2D g, GraphElement graphElement, DataElement item, PrintContext pctx) {
	if (g == null) return;
	int offset = 0;

	double rx = item.getRMiddle(graphElement);
	double ry = item.getRY(graphElement);
	if (pctx != null) {
	    rx = pctx.getRX(rx);
	    ry = pctx.getRY(ry);
	}

	Font textFont =
	    //VAMPResources.getFont(VAMPResources.AXIS_Y_DISPLAYER_FONT);
	    new Font("Times", Font.PLAIN, 8);
	
	g.setColor(Color.BLACK);
	g.setFont(textFont);

	FontRenderContext frc = g.getFontRenderContext();
	boolean horizontal_info =
	    VAMPResources.getBool(VAMPResources.HORIZONTAL_INFO);
	if (horizontal_info) {
	    String str = getTag(item);

	    if (!VAMPUtils.isNA(item)) {
		int vy = (int)(item.getVY(graphElement)*100);
		str += " (" + Utils.toString((double)vy/100.) + ")";
	    }
	    
	    Dimension sz = Utils.getSize((Graphics2D)g, str);
	    double descent = textFont.getLineMetrics(name, frc).getDescent();
	    offset = (int)descent + 4;
	    g.drawString(str, (int)(rx-sz.width/2), (int)ry-offset);
	}
	else {
	    if (!VAMPUtils.isNA(item)) {
		int vy = (int)(item.getVY(graphElement)*100);
		String s = Utils.toString((double)vy/100.);
		
		Dimension sz = Utils.getSize((Graphics2D)g, s);
		double descent = textFont.getLineMetrics(s, frc).getDescent();
		offset = (int)descent + 2;
		g.drawString(s, (int)(rx-sz.width/2), (int)ry-offset);
		offset = sz.height;
	    }
	    
	    String name = getTag(item);
	    Dimension sz = Utils.getSize((Graphics2D)g, name);
	    double descent = textFont.getLineMetrics(name, frc).getDescent();
	    offset += (int)descent + 2;
	    g.drawString(name, (int)(rx-sz.width/2), (int)ry-offset);
	}	
    }

    void drawGeneInfo(Graphics2D g, GraphElement graphElement, DataElement item, PrintContext pctx) {
	if (g == null) return;
	int offset = 0;

	double rx = item.getRMiddle(graphElement);
	double ry = item.getRY(graphElement);
	if (pctx != null) {
	    rx = pctx.getRX(rx);
	    ry = pctx.getRY(ry);
	}

	Font textFont =
	    new Font("Times", Font.PLAIN, 8);
	
	g.setColor(Color.BLACK);
	g.setFont(textFont);

	FontRenderContext frc = g.getFontRenderContext();
	String str = getTag(item);
	/*
	int vy = (int)(item.getVY()*100);
	str += " (" + Utils.toString((double)vy/100.) + ")";
	*/

	Dimension sz = Utils.getSize((Graphics2D)g, str);
	double descent = textFont.getLineMetrics(name, frc).getDescent();
	offset = (int)descent + 4;
	g.drawString(str, (int)(rx-sz.width/2), (int)ry-offset);
    }

    void drawThresholdedItem(Graphics g, GraphElement graphElement,
			     DataElement item, double t_maxY,
			     PrintContext pctx, boolean setColor) {
	if (g == null) return;
	if (setColor) {
	    Color itemThrMinYFG = VAMPResources.getColor
		(VAMPResources.THRESHOLD_MINY_FG);
	    Color itemThrMaxYFG = VAMPResources.getColor
		(VAMPResources.THRESHOLD_MAXY_FG);

	    g.setColor(item.getVY(graphElement) > t_maxY ? itemThrMaxYFG :
		       itemThrMinYFG);
	}

	double rx = item.getRMiddle(graphElement);
	double ry = item.getRY(graphElement);
	if (pctx != null) {
	    rx = pctx.getRX(rx);
	    ry = pctx.getRY(ry);
	}

	// 6/7/04
	drawThresholded(g, rx, ry);
    }

    public static void drawThresholded(Graphics g, double rx, double ry) {
	if (g == null) {
	    return;
	}

	g.drawLine((int)rx-EPSILON, (int)ry,
		   (int)rx+EPSILON, (int)ry-2*EPSILON);
	g.drawLine((int)rx-EPSILON, (int)ry-EPSILON,
		   (int)rx+EPSILON, (int)ry-3*EPSILON);
    }

    boolean isCompatible(AxisDisplayer axisDisplayer) {
	return !(axisDisplayer instanceof NullAxisDisplayer);
    }

    protected HashMap buildTrsColors(GraphElement graphElement) {
	HashMap cmap = new HashMap();
	Object names[] = (Object [])graphElement.getPropertyValue(VAMPProperties.TNamesProp);
	if (names == null) return null;
	Color trs_colors[] = new Color[names.length];

	Color baseColor = VAMPResources.getColor
	    (VAMPResources.TRANSCRIPTOME_MERGE_COLOR_BASE);
	int base = (baseColor.getRGB() & 0xffffff);
	boolean must_shift = (base == 0);
	if (base == 0)
	    base = 0xff;
	int step;
	if (names.length > 1)
	    step = base/(names.length-1);
	else
	    step = base;
	for (int n = 0; n < names.length; n++) {
	    int shift = must_shift ? ((n % 3) * 8) : 0;
	    int rgb = ((n * step) << shift) & 0xffffff;
	    trs_colors[n] = new Color(rgb);
	}
	for (int n = 0; n < names.length; n++)
	    cmap.put(names[n], trs_colors[n]);

	return cmap;
    }

    protected Color getColor(HashMap cmap, DataElement item) {
	return (Color)cmap.get(item.getPropertyValue(VAMPProperties.ArrayProp));
    }

    /*
    public Chromosome getChromosome(GraphCanvas canvas, String chr) {
	return VAMPUtils.getChromosome(canvas.getGlobalContext(),
				 VAMPUtils.getOS(canvas.getTemplateDS()), chr);
    }
    */

    public HashSet getChromosomeSet(DataSet dataSet) {
	HashSet set = new HashSet();
	if (VAMPUtils.isMergeChr(dataSet)) {
	    DataElement data[] = dataSet.getData();
	    String lastChr = "";
	    for (int n = 0; n < data.length; n++) {
		String chr = VAMPUtils.getChr(data[n]);
		if (!chr.equals(lastChr)) {
		    Object o[] = new Object[CHRSET_SIZE];
		    o[CHRSET_CHR_IDX] = chr;
		    o[CHRSET_OFFSET_IDX] =
			data[n].getPropertyValue(VAMPProperties.MergeOffsetProp);
		    set.add(o);
		    lastChr = chr;
		}
	    }
	}
	else {
	    Object o[] = new Object[CHRSET_SIZE];
	    o[CHRSET_CHR_IDX] = VAMPUtils.getChr(dataSet);
	    o[CHRSET_OFFSET_IDX] = null;
	    set.add(o);
	}
	return set;
    }

    boolean reallyShowLinks(GraphCanvas canvas, DataSet dataSet) {
	if (!show_links) return false;
	DataSet lDataSet = (DataSet)dataSet.getPropertyValue(VAMPProperties.LinkedDataSetProp);
	if (lDataSet == null) return false;
	int which = -1;
	LinkedList l = canvas.getGraphElements();
	int sz = l.size();

	for (int n = 0; n < sz; n++)
	    if (((GraphElement)l.get(n)).asDataSet() == dataSet) {
		which = n;
		break;
	    }

	System.out.println("which: " + which);
	if (which < 0) return false; // strange !!

	for (int n = 0; n < sz; n++)
	    if (((GraphElement)l.get(n)).asDataSet() == lDataSet) {
		System.out.println("n: " + n + ", returns: " +
				   ((n-which) == 1 || (which-n) == 1));
		return ((n-which) == 1 || (which-n) == 1);
	    }

	return false;
    }

    static final double INVALID_Y = Double.MIN_VALUE;

    void drawColorLines(Graphics2D g, GraphCanvas canvas,
			GraphElement graphElement, PrintContext pctx) {
	if (g == null)
	    return;

	ColorCodes cc = VAMPUtils.getColorCodes(graphElement);
	if (cc == null || !(cc instanceof StandardColorCodes))
	    return;

	double x0 = canvas.getRX(graphElement.getVBounds().x);
	double vy0 = graphElement.getVBounds().y;
	double xn = canvas.getRX(graphElement.getVBounds().x +
				 graphElement.getVBounds().width);
	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    xn = pctx.getRX(xn);
	}

	Rectangle2D.Double rBounds = graphElement.getRBounds();
	double top_bound = rBounds.y - rBounds.height;
	double bottom_bound = rBounds.y;

	StandardColorCodes scc = (StandardColorCodes)cc;
	double dsmin = canvas.getMinY();
	//System.out.println("dsmin: " + dsmin + ", vy0: " + vy0);
	double min = canvas.getRY(vy0 - (scc.getMin() - dsmin));

	double normal_min = canvas.getRY(vy0 -
					 (scc.getNormalMin() - dsmin));
	if (normal_min < top_bound || normal_min > bottom_bound)
	    normal_min = INVALID_Y;

	double normal_max = canvas.getRY(vy0 -
					 (scc.getNormalMax() - dsmin));
	if (normal_max < top_bound || normal_max > bottom_bound)
	    normal_max = INVALID_Y;

	double max = canvas.getRY(vy0 - (scc.getMax() - dsmin));
	if (max < top_bound || max > bottom_bound)
	    max = INVALID_Y;

	double amplicon = canvas.getRY(vy0 - (scc.getAmplicon() - dsmin));
	if (amplicon < top_bound || amplicon > bottom_bound)
	    amplicon = INVALID_Y;

	if (pctx != null) {
	    if (min != INVALID_Y)
		min = pctx.getRY(min);

	    if (normal_min != INVALID_Y)
		normal_min = pctx.getRY(normal_min);

	    if (normal_max != INVALID_Y)
		normal_max = pctx.getRY(normal_max);

	    if (max != INVALID_Y)
		max = pctx.getRY(max);

	    if (amplicon != INVALID_Y)
		amplicon = pctx.getRY(amplicon);
	}

	/*
	System.out.println("max: " + max);
	System.out.println("normal_max: " + normal_max);
	System.out.println("norma_min: " + normal_min);
	System.out.println("min: " + min);
	System.out.println("amplicon: " + amplicon);
	*/

	if (min != INVALID_Y) {
	    g.setColor(scc.getMinColor());
	    g.drawLine((int)x0, (int)min, (int)xn, (int)min);
	}

	g.setColor(scc.getNormalColor());
	if (normal_min != INVALID_Y)
	    g.drawLine((int)x0, (int)normal_min, (int)xn, (int)normal_min);

	if (normal_max != INVALID_Y)
	    g.drawLine((int)x0, (int)normal_max, (int)xn, (int)normal_max);

	if (max != INVALID_Y) {
	    g.setColor(scc.getMaxColor());
	    g.drawLine((int)x0, (int)max, (int)xn, (int)max);
	}

	if (amplicon != INVALID_Y) {
	    g.setColor(scc.getAmpliconColor());
	    g.drawLine((int)x0, (int)amplicon, (int)xn, (int)amplicon);
	}

	/*
	else {
	    g.setColor(scc.getMinColor());
	    g.fillRect((int)x0, (int)normal_min,
		       (int)(xn-x0), (int)(min-normal_min));
	    g.setColor(scc.getNormalColor());
	    g.fillRect((int)x0, (int)normal_max,
		       (int)(xn-x0), (int)(normal_min-normal_max));
	    g.setColor(scc.getMaxColor());
	    g.fillRect((int)x0, (int)max,
		       (int)(xn-x0), (int)(normal_max-max));
	}
	*/
    }

    void tcmManage(Graphics g, GraphCanvas canvas, GraphElement graphElement,
		   PrintContext pctx) {
	if (graphElement.getPropertyValue(TCMOP.S0Prop) == null)
	    return;

	double vy0 = graphElement.getVBounds().y;
	double dsmin = canvas.getMinY();
	Double d = (Double)graphElement.getPropertyValue(TCMOP.S0Prop);
	double s0 = canvas.getRY(vy0 - (d.doubleValue() - dsmin));
	d = (Double)graphElement.getPropertyValue(TCMOP.S1Prop);
	double s1 = canvas.getRY(vy0 - (d.doubleValue() - dsmin));
	double x0 = canvas.getRX(graphElement.getVBounds().x);
	double xn = canvas.getRX(graphElement.getVBounds().x +
				 graphElement.getVBounds().width);
	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    xn = pctx.getRX(xn);
	    s0 = pctx.getRY(s0);
	    s1 = pctx.getRY(s1);
	}

	g.setColor(Color.RED);
	g.drawLine((int)x0, (int)s0, (int)xn, (int)s0);
	g.drawLine((int)x0, (int)s1, (int)xn, (int)s1);

	/*
	g.setColor(Color.BLACK);
	g.drawString("S1", (int)x0+2, (int)s1-3);
	g.drawString("S0", (int)x0+2, (int)s0-3);
	*/
    }

    boolean useOptSelection() {return !show_links;}

    boolean isTrans(DataSet dataSet) {
	return VAMPUtils.getType(dataSet).equals
	    (VAMPConstants.TRANSCRIPTOME_TYPE) ||
	    VAMPUtils.getType(dataSet).equals
	    (VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE) ||
	    VAMPUtils.getType(dataSet).equals
	    (VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE) ||
	    VAMPUtils.getType(dataSet).equals
	    (VAMPConstants.TCM_TYPE) ||
	    VAMPUtils.getType(dataSet).equals
	    (VAMPConstants.TCM_CHROMOSOME_MERGE_TYPE);
    }

    static String getTag(DataElement item) {
	String str = (String)item.getPropertyValue(VAMPProperties.GeneSymbolProp);
	if (str != null)
	    return str;

	str = (String)item.getPropertyValue(Property.getProperty("Nmc"));
	if (str != null)
	    return str;

	return (String)item.getID();
    }
}
