
/*
 *
 * ChromosomeNameAxisDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import fr.curie.vamp.data.Profile;

class ChromosomeNameAxisDisplayer extends GenomicPositionAxisDisplayer {

    static final int markWidth = 2;
    static final int markWidth2 = markWidth/2;
    static final int markHeight = 6;
    static final int YOFFSET = 10;
    private int flags;
    private final int SHOW_CHR_SEP = 0x1;
    private final int SHOW_CHR_BOUNDS = 0x2;
    static final String canonicalName = "Chromosome Name";

    ChromosomeNameAxisDisplayer(String name, double roundX, double roundY,
				boolean hLines, GraphElementIDBuilder builder) {
	super(name, canonicalName, roundX, roundY, hLines, builder);
	flags = SHOW_CHR_SEP;
    }

    ChromosomeNameAxisDisplayer(String name, double roundX, double roundY,
				boolean hLines) {
	this(name, roundX, roundY, hLines, null);
    }

    ChromosomeNameAxisDisplayer(String name) {
	this(name, canonicalName);
    }

    protected ChromosomeNameAxisDisplayer(String name, String canName) {
	super(name, canName, 0.1, 0.1, false, null);
	flags = SHOW_CHR_SEP;
    }

    protected void drawMark(Graphics2D g, GraphCanvas canvas,
			    GraphElement graphElement,
			    int m, boolean begin, double x0, double y0, double x,
			    PrintContext pctx) {
	boolean showChrSep = (flags & SHOW_CHR_SEP) != 0;
	g.setColor(VAMPResources.getColor(showChrSep ?
					 VAMPResources.CHROMOSOME_LINE_SEPARATOR_FG : VAMPResources.CHROMOSOME_SEPARATOR_FG));
	if (pctx != null)
	    x = pctx.getRX(x);

	if (showChrSep) {
	    Rectangle2D.Double vbounds = graphElement.getVBounds();
	    double ry = canvas.getRY(vbounds.y);
	    double rh = canvas.getRH(vbounds.height);
	    double ry0;

	    ry0 = ry;

	    if (mustExtendChrSep(canvas, m, pctx))
		rh += YOFFSET;

	    if (pctx != null) {
		ry0 = pctx.getRY(ry0);
		ry = pctx.getRY(ry);
		rh = pctx.getRH(rh);
	    }

	    g.drawLine((int)x, (int)(ry-rh), (int)x, (int)ry0);
	}
	else if ((flags & SHOW_CHR_BOUNDS) != 0)
	    g.fillRect((int)x-markWidth2, (int)y0, markWidth, markHeight);
    }

    public void displayXAxis(GraphCanvas canvas, Axis xaxis, Graphics2D g,
			     GraphElement graphElem, int m,
			     PrintContext pctx) {
	//if (VAMPUtils.getType(dataSet).equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
	if (canvas.isRotated()) {
	    return;
	}

	if (VAMPUtils.isMergeChr(graphElem)) {
	    displayXAxis_os(canvas, xaxis, g, graphElem, m, pctx);
	}
	else {
	    displayXAxis_chr(canvas, xaxis, g, graphElem, m, pctx);
	}
    }

    public void displayXAxis_os(GraphCanvas canvas, Axis xaxis, Graphics2D g,
				GraphElement graphElem, int m, PrintContext pctx) {
	displayInfo(canvas, xaxis, g);

	/*
	if (!graphElem.isFullImported())
	    return;
	*/

	double x0 = canvas.getRX(graphElem.getVBounds().x);

	double y0;
	if (xaxis == null)
	    y0 = canvas.getRY(graphElem.getVBounds().y);
	else
	    y0 = xaxis.getSize().height/2 - 5;

	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    y0 = pctx.getRY(y0);
	}

	Font font = VAMPResources.getFont(VAMPResources.AXIS_X_DISPLAYER_FONT);
	g.setFont(font);

	// X axis
	//DataElement data[] = graphElem.getData();

	String OS = VAMPUtils.getOS(graphElem);
	Vector cache = computeChrCache(canvas.getGlobalContext(),
				       graphElem, OS);
	int size = cache.size();

	g.setColor(Color.RED);
	for (int i = 0; i < size; i++) {
	    Object o[] = (Object[])cache.get(i);
	    String chr = (String)o[0];
	    double begin = ((Long)o[1]).longValue();
	    double end = ((Long)o[2]).longValue();

	    double x = getRX(canvas, graphElem, begin + (end - begin) / 2);
	    if (x == Double.MIN_VALUE)
		continue;
	    double rbegin = getRX(canvas, graphElem, begin);
	    double rend = getRX(canvas, graphElem, end);

	    if (pctx != null)
		x = pctx.getRX(x);
	    
	    if (mustDrawChr(canvas, xaxis, m, pctx)) {
		Point where = drawChr(g, chr, x, y0, vSize, pctx);
		if (xaxis != null && i == size-1) {
		    g.setColor(Color.GRAY);
		    g.setFont(VAMPResources.getFont
			      (VAMPResources.DATASET_DISPLAYER_FONT));
		    g.drawString(OS, where.x+4, where.y);
		}
	    }

	    if (xaxis == null) {
		drawMark(g, canvas, graphElem, m, false, x0, y0,
			 rbegin,
			 pctx);
		drawMark(g, canvas, graphElem, m, true,
			 x0, y0, rend, pctx);
	    }
	}

	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_FG));
	double max_x = canvas.getRX(canvas.getMaxX());
	if (pctx != null)
	    max_x = pctx.getRX(max_x);

	if (mustDrawBaseLine(canvas, xaxis, m, pctx))
	    g.drawLine((int)x0, (int)y0, (int)max_x, (int)y0);
    }

    public void displayXAxis_chr(GraphCanvas canvas, Axis xaxis, Graphics2D g,
				 GraphElement graphElem, int m, PrintContext pctx) {

	displayInfo(canvas, xaxis, g);

	/*
	if (!dataSet.isFullImported())
	    return;
	*/

	double x0 = canvas.getRX(graphElem.getVBounds().x);

	double y0;
	if (xaxis == null)
	    y0 = canvas.getRY(graphElem.getVBounds().y);
	else
	    y0 = xaxis.getSize().height/2 - 5;

	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    y0 = pctx.getRY(y0);
	}

	/*
	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_FG));

	if (pctx != null)
	    max_x = pctx.getRX(max_x);

	if (mustDrawBaseLine(canvas, xaxis, m, pctx))
	    g.drawLine((int)x0, (int)y0, (int)max_x, (int)y0);
	*/

	DataSet dataSet = graphElem.asDataSet();
	Profile profile = graphElem.asProfile();

	Font font = VAMPResources.getFont(VAMPResources.AXIS_X_DISPLAYER_FONT);
	g.setFont(font);

	// X axis
	DataElement data[] = (dataSet != null ? dataSet.getData() : null);

	//Vector cache = computeChrCache(dataSet, !dataSet.isFullImported());
	Vector cache = computeChrCache(graphElem, !graphElem.isFullImported());

	DataElement d0;
	double min_x, max_x;
	String pchr;

	if (dataSet != null && dataSet.isFullImported()) {
	    d0 = data[0];
	    min_x =  d0.getRX(dataSet);
	    max_x = data[data.length-1].getRX(dataSet);
	    pchr = (String)d0.getPropertyValue(VAMPProperties.ChromosomeProp);
	}
	else {
	    d0 = null;
	    if (profile != null) {
		min_x = canvas.getRX(profile.getMinX());
		max_x = canvas.getRX(profile.getMaxX());
	    }
	    else {
		min_x = canvas.getRX(graphElem.getLMinX());
		max_x = canvas.getRX(graphElem.getLMaxX());
	    }

	    pchr = (String)graphElem.getPropertyValue(VAMPProperties.ChromosomeProp);
	}

	if (xaxis == null) {
	    drawMark(g, canvas, graphElem, m, true, x0, y0, min_x, pctx);
	}

	int size = cache.size();
	
	for (int i = 0; i < size; i += 2) {
	    int n = ((Integer)cache.get(i)).intValue();
	    String chr = (String)cache.get(i+1);
	    
	    double x;
	    if (dataSet != null && dataSet.isFullImported()) {
		x = d0.getRX(dataSet) + (data[n-1].getRX(dataSet)-d0.getRX(dataSet))/2;
	    }
	    else {
		x = min_x + (max_x - min_x) / 2;
	    }

	    if (pctx != null)
		x = pctx.getRX(x);

	    if (mustDrawChr(canvas, xaxis, m, pctx)) {
		drawChr(g, pchr, x, y0, vSize, pctx);
	    }
		
	    if (!dataSet.isFullImported()) {
		break;
	    }

	    if (dataSet != null) {
		if (xaxis == null) {
		    drawMark(g, canvas, dataSet, m, false, x0, y0,
			     data[n-1].getRX(dataSet) + data[n-1].getRSize(dataSet) +
			     canvas.getRW(1), // kludge for testing
			     pctx);
		    drawMark(g, canvas, dataSet, m, true,
			     x0, y0, data[n].getRX(dataSet), pctx);
		}
		d0 = data[n];
	    }
		
	    pchr = chr;
	}

	double x = Double.MAX_VALUE;
	if (profile != null) {
	    x = min_x + (max_x - min_x)/2;
	}
	else if (d0 != null) {
	    x = d0.getRX(dataSet) + (data[data.length-1].getRX(dataSet)-d0.getRX(dataSet))/2;
	}

	if (x != Double.MAX_VALUE) {
	    if (pctx != null) {
		x = pctx.getRX(x);
	    }

	    if (mustDrawChr(canvas, xaxis, m, pctx)) {
		drawChr(g, pchr, x, y0, vSize, pctx);
	    }
	}

	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_FG));

	if (pctx != null)
	    max_x = pctx.getRX(max_x);

	if (mustDrawBaseLine(canvas, xaxis, m, pctx))
	    g.drawLine((int)x0, (int)y0, (int)max_x, (int)y0);
    }

    public void displayYAxis(GraphCanvas canvas, Axis yaxis, Graphics2D g,
			     GraphElement graphElement, int m,
			     PrintContext pctx) {
	super.displayYAxis(canvas, yaxis, g, graphElement, m, pctx);
	// moved to GenomicPositionAxisDisplayer
	/*
	if (yaxis == null) return;
	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null) return;

	double x0 = yaxis.getSize().width/2;
	double vy0 = dataSet.getVBounds().y;
	double y0 = canvas.getRY(vy0);

	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    y0 = pctx.getRY(y0);
	}

	String OS = VAMPUtils.getOS(dataSet);
	String s = OS;
	boolean middle = false;

	String syntenyOS = (String)
	    dataSet.getPropertyValue(VAMPConstants.SyntenyOrganismProp);

	if (syntenyOS != null && syntenyOS.equals(OS)) {
	    g.setColor(Color.RED);
	    String syntenyOrig = (String)
		dataSet.getPropertyValue(VAMPConstants.SyntenyOrigProp);

	    if (syntenyOrig != null) {
		s = syntenyOrig + " -> " + OS;
		middle = true;
	    }
	}
	else
	    g.setColor(Color.GRAY);

	g.setFont(VAMPResources.getFont(VAMPResources.AXIS_Y_DISPLAYER_FONT));

	Dimension d = Utils.getSize(g, s);
	int x = (int)x0;
	if (middle)
	    x -= d.width/2;
	else
	    x -= d.width;
	g.drawString(s, x, (int)y0 + d.height + 5);
	*/
    }

    public boolean showChrSep() {
	return (flags & SHOW_CHR_SEP) != 0;
    }

    public void showChrSep(boolean showChrSep) {
	if (showChrSep)
	    this.flags = SHOW_CHR_SEP;
	else
	    this.flags = 0;
    }

    static final Property ChrCacheProp =
	Property.getHiddenProperty("ChrCache");
    static final Property ChrCachePropAddFirst =
	Property.getHiddenProperty("ChrCacheAddFirst");
    static final Property ChrCache2Prop =
	Property.getHiddenProperty("ChrCache2");

    static Vector computeChrCache(GlobalContext globalContext,
				  GraphElement graphElement,
				  String organism) {
	Vector v = (Vector)graphElement.getPropertyValue(ChrCache2Prop);
	if (v != null) return v;
	Cytoband cytoband = MiniMapDataFactory.getCytoband
	    (globalContext, organism);

	if (cytoband == null)
	    return null;

	v = new Vector();
	int size = cytoband.getChrV().size();
	for (int n = 0; n < size; n++) {
	    Chromosome chr = (Chromosome)cytoband.getChrV().get(n);
	    v.add(new Object[]{chr.getName(),
			       new Long(chr.getBegin_o()),
			       new Long(chr.getEnd_o())});
	}

	return v;
    }

    static Vector computeChrCache(GraphElement graphElement, boolean add_first) {
	/*
	if (!graphElement.isFullImported())
	    return new Vector();
	*/

	Property prop = add_first ? ChrCachePropAddFirst : ChrCacheProp;
	Vector v = (Vector)graphElement.getPropertyValue(prop);

	if (v != null) {
	    return v;
	}

	v = new Vector();
	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null) {
	    return v; 
	}

	DataElement data[] = dataSet.getData();
	DataElement d0 = (dataSet.isFullImported() ? data[0] : null);
	String pchr;
	if (d0 != null)
	    pchr = VAMPUtils.getChr(d0);
	else
	    pchr = (String)dataSet.getPropertyValue(VAMPProperties.ChromosomeProp);
	// added 4/04/05
	if (add_first) {
	    v.add(new Integer(0));
	    v.add(pchr);
	}

	for (int n = 1; n < data.length; n++) {
	    String chr = VAMPUtils.getChr(data[n]);
	    if (!chr.equals(pchr)) {
		v.add(new Integer(n));
		v.add(chr);
		pchr = chr;
	    }
	}

	dataSet.setPropertyValue(prop, v);
	return v;
    }

    Point drawChr(Graphics2D g, String chr, double x,
		 double y0, int vSize, PrintContext pctx) {
	Dimension d = Utils.getSize(g, chr);
	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_LABEL_FG));
	int x1 =  (int)(x-d.width/2);
	int y1 = (int)y0 + vSize + d.height + 2;
	g.drawString(chr, x1, y1);

	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_FG));
	g.drawLine((int)x, (int)y0-vSize, (int)x, (int)y0+vSize);
	return new Point(x1+d.width, y1);
    }

    protected boolean mustDrawChr(GraphCanvas canvas, Axis xaxis, int m,
				PrintContext pctx) {
	if (!(this instanceof DotPlotAxisDisplayer) || xaxis != null)
	    return true;
	if (m == canvas.getGraphElements().size()-1)
	    return true;
	return pctx == null ? false : pctx.isLast();
    }

    protected boolean mustDrawBaseLine(GraphCanvas canvas, Axis xaxis,
				     int m, PrintContext pctx) {
	return mustDrawChr(canvas, xaxis, m, pctx);
    }

    protected boolean mustExtendChrSep(GraphCanvas canvas,
				     int m, PrintContext pctx) {
	if (!(this instanceof DotPlotAxisDisplayer))
	    return false;
	if (m == 0)
	    return true;
	return pctx == null ? false : pctx.isFirst();
    }

    public double getMaxX(GraphElement graphElement) {
	double maxX = super.getMaxX(graphElement);
	//if (!VAMPUtils.getType(graphElement).equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
	if (!VAMPUtils.isMergeChr(graphElement))
	    return maxX;
	String organism = VAMPUtils.getOS(graphElement);
	if (organism == null) return maxX;
	Cytoband cytoband = MiniMapDataFactory.getCytoband
	    (GlobalContext.getLastInstance(), organism);
	Vector v = cytoband.getChrV();
	Chromosome chr = (Chromosome)v.get(v.size()-1);
	return chr.getEnd_o();
    }

    double getRX(GraphCanvas canvas, GraphElement graphElem, double vx) {
	return canvas.getRX(vx);
    }

    boolean useCytoband(GraphElement graphElem) {
	return VAMPUtils.isMergeChr(graphElem);
    }

}
