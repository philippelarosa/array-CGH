
/*
 *
 * GenomicPositionAxisDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import fr.curie.vamp.data.Profile;

import java.awt.*;
import java.net.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;

class GenomicPositionAxisDisplayer extends AxisDisplayer {

    protected int vSize = 3;
    protected double stepX, stepY;
    protected double roundX, roundY;
    protected boolean hLines;
    protected int precisX, precisY;

    protected int canvasWidth, canvasHeight;
    protected int canvasRWidth, canvasRHeight;
    protected double canvasXScale, canvasYScale;
    private int flags;
    static private final int FONT_DELTA = 1;
    static private final double angle = -Math.PI/2;
    static final int ARRAY_MERGE_Y_OFFSET =
	MergeArrayOP.Y_OFFSET;
    static final int IS_TRANS = 0x1;
    static final int IS_TRANS_REF = 0x2;
    static final int IS_TRANS_REL = 0x4;
    static final String	CANONICAL_NAME = "Genomic Position";

    protected GenomicPositionAxisDisplayer(String name, String canName,
					   double roundX, double roundY,
					   boolean hLines,
					   GraphElementIDBuilder graphElementIDBuilder,
					   int flags) {
	super(name, canName, graphElementIDBuilder);
	this.roundX = roundX;

	this.roundY = roundY;
	this.hLines = hLines;
	precisX = computePrecision(roundX);
	precisY = computePrecision(roundY);
	this.canvasWidth = 0;
	this.canvasHeight = 0;
	this.canvasXScale = 0;
	this.canvasYScale = 0;
	this.flags = flags;
    }

    GenomicPositionAxisDisplayer(String name, double roundX, double roundY,
				 boolean hLines,
				 GraphElementIDBuilder graphElementIDBuilder,
				 int flags) {
	this(name, CANONICAL_NAME, roundX, roundY, hLines, graphElementIDBuilder,
	     flags);
    }

    GenomicPositionAxisDisplayer(String name, double roundX, double roundY,
				 boolean hLines, GraphElementIDBuilder graphElementIDBuilder) {
	this(name, roundX, roundY, hLines, graphElementIDBuilder, 0);
    }

    protected GenomicPositionAxisDisplayer(String name, String canName,
					   double roundX, double roundY,
					   boolean hLines, GraphElementIDBuilder graphElementIDBuilder) {
	this(name, canName, roundX, roundY, hLines, graphElementIDBuilder, 0);
    }

    static protected int computePrecision(double ox) {
	int precis = 0;
	double x = ox;
	for (;;) {
	    if (x == (long)x) break;
	    x *= 10.;
	    precis++;
	}
	return precis;
    }

    private void computeStepX(Graphics2D g, GraphCanvas canvas,
			      PrintContext pctx) {
	if (pctx == null) {
	    std_computeStepX(g, canvas);
	    return;
	}

	PrintableSet printableSet = pctx.getPrintableSet();
	computeStepX_r(g, canvas, (int)pctx.getBounds().width,
		       !printableSet.isWholeX());
	canvasWidth = canvasRWidth = 0;
    }

    private void std_computeStepX(Graphics2D g, GraphCanvas canvas) {
	int width = canvas.getSize().width;
	int rWidth = canvas.getAvailableRWidth();

	if ((width == canvasWidth || rWidth == canvasRWidth) &&
	    canvas.getScale().getScaleX() == canvasXScale)
	    return;

	canvasWidth = width;
	canvasRWidth = rWidth;

	int baseWidth;
	if (canvasRWidth > canvasWidth)
	    baseWidth = canvasWidth;
	else
	    baseWidth = canvasRWidth;
	computeStepX_r(g, canvas, baseWidth, true);
    }

    private void computeStepX_r(Graphics2D g, GraphCanvas canvas,
				int baseWidth, boolean scale) {
	canvasXScale = canvas.getScale().getScaleX();
	double maxX = canvas.getMaxX();
	String s = performRoundX((new Long((long)maxX)).toString());
	Dimension d = Utils.getSize(g, s);
	int cnt = (baseWidth - canvas.getRMargins().getMarginWidth()) /
	    d.width;
	if (cnt == 0) {
	    stepX = 0;
	    return;
	}

	double vstepX = maxX / cnt;
	if (scale) {
	    vstepX = vstepX * canvas.getBFScale().getScaleX() / canvasXScale;
	}

	if (Double.isNaN(vstepX)) {
	    stepX = 0;
	    return;
	}

	stepX = (long)(vstepX/roundX) * roundX + roundX;
	//System.out.println("stepX: " + stepX + " " + vstepX + " " + roundX + " " + baseWidth + " " + d.width);
    }

    private void computeStepY(Graphics2D g, GraphCanvas canvas,
			      PrintContext pctx) {
	if (pctx == null) {
	    std_computeStepY(g, canvas);
	    return;
	}

	PrintableSet printableSet = pctx.getPrintableSet();
	if (!printableSet.isWholeY()) {
	    std_computeStepY(g, canvas);
	    return;
	}

	int baseHeight = (int)pctx.getBounds().height;
	computeStepY_r(g, canvas, baseHeight, false,
		       (canvas.getMaxY() - canvas.getMinY()) *
		       printableSet.getGraphElementCountPerPage() * 1.20);

	canvasHeight = canvasRHeight = 0;
    }


    private void std_computeStepY(Graphics2D g, GraphCanvas canvas) {

	int height = canvas.getSize().height;
	int rHeight = canvas.getAvailableRHeight();

	if ((height == canvasHeight || rHeight == canvasRHeight) &&
	    canvas.getScale().getScaleY() == canvasYScale)
	    return;

	canvasHeight = height;
	canvasRHeight = rHeight;

	int baseHeight;
	if (canvasRHeight > canvasHeight)
	    baseHeight = canvasHeight;
	else
	    baseHeight = canvasRHeight;

	computeStepY_r(g, canvas, baseHeight, true, 0.);
    }

    private void computeStepY_r(Graphics2D g, GraphCanvas canvas,
				int baseHeight, boolean scale, double maxY) {
	canvasYScale = canvas.getScale().getScaleY();

	if (maxY == 0)
	    maxY = canvas.getTopMaxY();

	String s = performRoundY((new Long((long)maxY)).toString());
	Dimension d = Utils.getSize(g, s);
	int cnt = (baseHeight - canvas.getRMargins().getMarginHeight()) /
	    d.height;

	double vstepY = maxY / cnt;
	if (scale)
	    vstepY = vstepY * canvas.getBFScale().getScaleY() / canvasYScale;
	stepY = (long)(vstepY/roundY)*roundY+roundY;
    }

    private double displayX(Graphics2D g,
			    GraphCanvas canvas, Axis xaxis,
			    double x0, double y0, 
			    boolean dspString, double n,
			    double affineTransform[],
			    PrintContext pctx) {
	if (pctx == null && !canvas.isVX_Visible(n)) {
	    return 0;
	}

	double x = canvas.getRX(n);
	if (pctx != null)
	    x = pctx.getRX(x);

	if (n != 0) {
	    setAxisFG(g, xaxis);
	    g.drawLine((int)x, (int)y0, (int)x, (int)y0+vSize);
	}

	if (dspString) {
	    String s;
	    if (affineTransform != null)
		s = performRoundX((new Double(getValue(n, affineTransform))).toString());
	    else
		s = performRoundX((new Long((long)n)).toString());
	    Dimension d = Utils.getSize(g, s);
	    setAxisLabelFG(g);
	    g.drawString(s, (int)x-d.width/2, (int)y0 + vSize + d.height + 2);
	}

	return x;
    }

    public void displayXAxis(GraphCanvas canvas, Axis xaxis, Graphics2D g,
			     GraphElement graphElem, int m, PrintContext pctx) {
	/*
	System.out.println("displayXAxis: " + xaxis + ", " +
			   canvas.isRotated() + ", " +
			   VAMPUtils.getType(graphElem));
	*/

	if (canvas.isRotated()) {
	    if (VAMPUtils.getType(graphElem).equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE) ||
		VAMPUtils.getType(graphElem).equals(VAMPConstants.FRAGL_ARRAY_MERGE_TYPE)) {
		displayArrayXAxis(canvas, xaxis, g, graphElem, m, pctx);
		return;
	    }
	    return;
	}

	double minX = canvas.getMinX();
	double maxX = canvas.getMaxX();

	if (Double.isNaN(maxX)) {
	    return;
	}

	double x0, y0;
	x0 = canvas.getRX(graphElem.getVBounds().x);
	if (xaxis == null)
	    y0 = canvas.getRY(graphElem.getVBounds().y);
	else
	    y0 = xaxis.getSize().height/2 - 5;


	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    y0 = pctx.getRY(y0);
	}

	displayInfo(canvas, xaxis, g);

	Font font = VAMPResources.getFont(VAMPResources.AXIS_X_DISPLAYER_FONT);
	    
	g.setFont(font);


	computeStepX(g, canvas, pctx);
	if (stepX == 0) {
	    return;
	}
	// X axis

	double n = 0;

	boolean dspString = (graphElem.getRBounds().width >= 30);
	    
	double[] affineTransform = (double[])graphElem.getPropertyValue(VAMPProperties.AffineTransformProp);
	double mx = 0.;
	//System.out.println("minX " + minX + ":" + maxX + " " + stepX);
	for (n = minX; n <= maxX; n += stepX)
	    mx = displayX(g, canvas, xaxis, x0, y0, dspString, n,
			  affineTransform, pctx);

	//System.out.println("done !");
	if (n != maxX)
	    mx = displayX(g, canvas, xaxis, x0, y0, false, maxX,
			  affineTransform, pctx);

	if (n > maxX) n = maxX;
	mx = canvas.getRX(n);
	if (pctx != null)
	    mx = pctx.getRX(mx);

	setAxisFG(g, xaxis);
	g.drawLine((int)x0, (int)y0, (int)mx, (int)y0);
    }

    private double displayY(Graphics2D g, GraphCanvas canvas,
			    GraphElement graphElem,
			    Axis yaxis, double mX, double x0, double y0,
			    double vy0,
			    int maxW, double minY, boolean dspString, double n,
			    PrintContext pctx)
    {
	double y = canvas.getRY(vy0 - n + minY);
	if (pctx != null)
	    y = pctx.getRY(y);

	// EV 10/12/04
	/*if (!VAMPUtils.getType(graphElem).equals(VAMPConstants.LOH_TYPE))*/ {
	    String s = performRoundY(graphElem.yTransform_1(n));
	    if (dspString) {
		Dimension d = Utils.getSize(g, s);
		setAxisLabelFG(g);
		g.drawString(s, (int)x0 - maxW - 4, (int)(y+d.height/2));
	    }
	}

	// EV 10/12/04
	/*if (!VAMPUtils.getType(graphElem).equals(VAMPConstants.LOH_TYPE) ||
	  n == VAMPConstants.LOH_POS_Y) */ {
	    if (hLines && yaxis == null && n != minY) {
		setAxisLineFG(g);
		g.drawLine((int)x0, (int)y, (int)mX, (int)y);
	    }
	    else if (x0 >= 0) {
		setAxisFG(g, yaxis);
		g.drawLine((int)x0-vSize, (int)y, (int)x0, (int)y);
	    }
	}

	return y;
    }

    public void displayYAxis(GraphCanvas canvas, Axis yaxis, Graphics2D g,
			     GraphElement graphElem, int m, PrintContext pctx) {
	if (canvas.isRotated())
	    return;

	if (VAMPUtils.getType(graphElem).equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE) ||
		VAMPUtils.getType(graphElem).equals(VAMPConstants.FRAGL_ARRAY_MERGE_TYPE)) {
	    displayArrayYAxis(canvas, yaxis, g, graphElem, m, pctx);
	    return;
	}

	// end of testing image
	  
	double maxY = canvas.getMaxY();
	double minY = canvas.getMinY();

	double x0, y0;
	if (pctx != null) {
	    if ((pctx.getFlags() & PrintContext.Y_AXIS) != 0)
		x0 = pctx.getBounds().x + pctx.getBounds().width/2;
	    else {
		x0 = canvas.getRX(graphElem.getVBounds().x);
		x0 = pctx.getRX(x0);
	    }
	}
	else if (yaxis == null) {
	    x0 = canvas.getRX(graphElem.getVBounds().x);
	}
	else
	    x0 = yaxis.getSize().width/2;
	
	double vy0 = graphElem.getVBounds().y;
	y0 = canvas.getRY(vy0);

	if (pctx != null)
	    y0 = pctx.getRY(y0);

	Font fontName = VAMPResources.getFont(VAMPResources.AXIS_Y_NAME_DISPLAYER_FONT);
	if (VAMPUtils.getType(graphElem).equals(VAMPConstants.GENOME_ANNOT_TYPE) ||
	    VAMPUtils.getType(graphElem).equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE)) {
	    double h = canvas.getRH(graphElem.getVBounds().height);
	    if (pctx != null)
		h = pctx.getRH(h);
	    if (yaxis == null) {
		setAxisFG(g, yaxis);
		g.drawLine((int)x0, (int)(y0-h), (int)x0, (int)y0);
		// must draw an horizontal line 
		return;
	    }
	    
	    int mY = (int)(y0-h/2);
	    String name = (graphElementIDBuilder != null ?
			   graphElementIDBuilder.buildID(graphElem) : null);

	    if (name == null)
		return;

	    g.setFont(fontName);

	    Dimension d = Utils.getSize(g, name);
	    if (displayPropName(canvas, yaxis, d.height, pctx)) {
		g.setColor(VAMPResources.getColor
			   (yaxis == null ?
			    VAMPResources.AXIS_Y_CANVAS_PROPERTY_NAME_FG :
			    VAMPResources.AXIS_Y_PROPERTY_NAME_FG));
		g.drawString(name, (int)(x0-d.width/2), (int)(mY-d.height/2));
	    }
	    else if (pctx != null) {
		g.drawString(name, (int)(x0-PrintableSet.L_PADX+5), (int)(mY-d.height/2-2));
	    }
	    
	    return;
	}

	if (graphElem.getRBounds().height < 12 && yaxis != null) {
	    String name = (graphElementIDBuilder != null ?
			   graphElementIDBuilder.buildID(graphElem) : null);

	    if (name != null) {
		g.setFont(fontName);
		g.setColor(VAMPResources.getColor
			   (yaxis == null ?
			    VAMPResources.AXIS_Y_CANVAS_PROPERTY_NAME_FG :
			    VAMPResources.AXIS_Y_PROPERTY_NAME_FG));
		Dimension d = Utils.getSize(g, name);
		
		g.drawString(name, (int)(x0-d.width/2), (int)y0);
		if (canvas.getGraphPanel().isYAxisAutoAdapt())
		    canvas.setWestYSize(d.width + 10, false);
	    }
	    return;
	}

	boolean dspString = (graphElem.getRBounds().height >= 10);

	Font font = VAMPResources.getFont(VAMPResources.AXIS_Y_DISPLAYER_FONT);
	g.setFont(font);

	computeStepY(g, canvas, pctx);

	// Y axis
	int maxW = 0;


	double delta = computeDelta(graphElem, minY, maxY, stepY, roundY);

	for (double n = minY + delta; n <= maxY; n += stepY) {
	    if (n < minY)
		continue;
	    String s = performRoundY(graphElem.yTransform_1(n));
	    Dimension d = Utils.getSize(g, s);
	    if (d.width > maxW) maxW = d.width;
	}

	setAxisFG(g, yaxis);
	double mY = 0;
	double mX = canvas.getRX(canvas.getMaxX());
	double n;

	for (n = minY + delta; n <= maxY; n += stepY) {
	    if (n < minY)
		continue;
	    mY = displayY(g, canvas, graphElem, yaxis, mX, x0, y0, vy0,
			  maxW, minY, dspString, n, pctx);
	}

	if (n != maxY) {
	    mY = displayY(g, canvas, graphElem, yaxis, mX, x0, y0, vy0,
			  maxW, minY, false, maxY, pctx);
	}

	setAxisFG(g, yaxis);
	g.drawLine((int)x0, (int)mY, (int)x0, (int)y0);

	String name = (graphElementIDBuilder != null ?
		       graphElementIDBuilder.buildID(graphElem) : null);

	if (name != null) {
	    g.setFont(fontName);

	    Dimension d = Utils.getSize(g, name);
	    if (displayPropName(canvas, yaxis, d.height, pctx)) {
		g.setColor(VAMPResources.getColor
			   (yaxis == null ?
			    VAMPResources.AXIS_Y_CANVAS_PROPERTY_NAME_FG :
			    VAMPResources.AXIS_Y_PROPERTY_NAME_FG));
		if (yaxis != null) {
		    g.drawString(name, (int)(x0-d.width/2), (int)(mY-d.height/2));
		    if (canvas.getGraphPanel().isYAxisAutoAdapt())
			canvas.setWestYSize(d.width + 10, false);
		}
		else if (pctx != null) {
		    g.drawString(name, (int)(x0-PrintableSet.L_PADX+5), (int)(mY-d.height/2-2));
		}
	    }
	}

	if (hLines && yaxis == null) {
	    setAxisFG(g, yaxis);
	    g.drawLine((int)mX, (int)mY, (int)mX, (int)y0);
	}

	if (yaxis != null ||
	    (pctx != null && (pctx.getFlags() & PrintContext.Y_AXIS) != 0
	     && pctx.getArea().getHint("SHOW_ICONS") != null)) {
	    int maxh = (int)(canvas.getRH(maxY-minY)-2);
	    if (pctx != null)
		maxh = (int)pctx.getRH(maxh);
	    int img_offset = 6;
	    Dimension imgsize = Utils.drawImage(g, canvas.getToolkit(),
						canvas.getSystemConfig().getGraphElementIcon
						(VAMPUtils.getType(graphElem)),
						(int)x0+img_offset, (int)(y0 + (mY-y0)/2), maxh, false, true);
	    
	    /*
	    System.out.println("drawing image... at " +
			       (x0+img_offset) + " . " +
			       (y0 + (mY-y0)/2) + " maxh=" +  maxh);
	    */
	    if (yaxis != null) {
		if (imgsize != null) {
		    if (canvas.getGraphPanel().isYAxisAutoAdapt())
			canvas.setWestYSize((int)x0 + imgsize.width + 2 * img_offset, false);
		}
	    }
	}


	if (yaxis == null) return;

	x0 = yaxis.getSize().width/2;
	vy0 = graphElem.getVBounds().y;
	y0 = canvas.getRY(vy0);

	if (pctx != null) {
	    x0 = pctx.getRX(x0);
	    y0 = pctx.getRY(y0);
	}

	String OS = VAMPUtils.getOS(graphElem);
	// KLUDGE
	if (OS == null)
	    OS = "Human";

	String s = OS;
	boolean middle = false;

	String syntenyOS = (String)
	    graphElem.getPropertyValue(VAMPProperties.SyntenyOrganismProp);

	if (syntenyOS != null && syntenyOS.equals(OS)) {
	    g.setColor(Color.RED);
	    String syntenyOrig = (String)
		graphElem.getPropertyValue(VAMPProperties.SyntenyOrigProp);

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

    }

    protected String performRound(String s, int precis) {
	int idx = s.indexOf(".");
	if (idx < 0) return s;
	idx += 1 + precis;
	if (idx > s.length()) idx = s.length();
	return s.substring(0, idx);
    }

    protected String performRoundX(String s) {
	return performRound(s, precisX);
    }

    static final double ROUND_D = 10000.;

    protected String performRoundY(double d) {
	d = Math.round(d*ROUND_D)/ROUND_D;
	return performRoundY(Utils.toString(d));
    }

    protected String performRoundY(String s) {
	return performRound(s, precisY);
    }
    
    public static double roundMax(double max, double round) {
	double rd = (long)(max / round) * round;
	//if (rd == 0 || rd == max) return rd;
	if (rd == max) return rd;
	return rd + round;
    }

    public static double roundMin(double min, double round) {
	double rd = (long)(min / round) * round;
	// EV: 19/07/04 reconnected because of problem with chip_chip
	if (rd == 0 || rd == min) return rd;
	//if (rd == min) return rd;
	if (rd < 0)
	    return rd - round;
	return rd;
    }

    public double getMaxX(GraphElement graphElem) {
	if (graphElem.asProfile() != null) {
	    return graphElem.asProfile().getMaxX();
	}

	if (!graphElem.isFullImported()) {
	    //System.out.println("lmaxX -> " + graphElem.getLMaxX()); 
	    return graphElem.getLMaxX();
	}

	double maxX = Double.MIN_VALUE;
	DataSet dataSet = graphElem.asDataSet();
	if (dataSet == null) return maxX;

	DataElement data[] = dataSet.getData();

	for (int n = 0; n < data.length; n++)
	    if (data[n].getVX(graphElem) + data[n].getVSize(graphElem) > maxX)
		maxX = data[n].getVX(graphElem) + data[n].getVSize(graphElem);
	//System.out.println("maxx -> " + roundMax(maxX, roundX));
	return roundMax(maxX, roundX);
    }

    public double getMaxY(GraphElement graphElem) {
	if (VAMPUtils.getType(graphElem).equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE)) {
	    int gained_size = ((Vector<String>)graphElem.getPropertyValue(VAMPProperties.GainedVectorArrayProp)).size();
	    int lost_size = ((Vector<String>)graphElem.getPropertyValue(VAMPProperties.LostVectorArrayProp)).size();
	    int size = gained_size > lost_size ? gained_size : lost_size;
	    return size + ARRAY_MERGE_Y_OFFSET + 0.5;
	}

	if (graphElem.asProfile() != null) {
	    double maxY = graphElem.asProfile().getMaxY();
	    if (false) {
		System.out.println("WARNING getMaxY() !!!!");
		return maxY;
	    }
	    double thrMaxY = VAMPUtils.getThresholdMaxY(graphElem);
	    return maxY < thrMaxY ? maxY : thrMaxY;
	}

	if (!graphElem.isFullImported()) {
	    //System.out.println("lmaxY -> " + graphElem.getLMaxY()); 
	    return graphElem.getLMaxY();
	}

	double maxY = Double.MIN_VALUE;
	DataSet dataSet = graphElem.asDataSet();
	if (dataSet == null)
	    return maxY;

	/*
	if (VAMPUtils.getType(graphElem).equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE)) {
	    int gained_size = ((Vector<String>)graphElem.getPropertyValue(VAMPProperties.GainedVectorArrayProp)).size();
	    int lost_size = ((Vector<String>)graphElem.getPropertyValue(VAMPProperties.LostVectorArrayProp)).size();
	    int size = gained_size > lost_size ? gained_size : lost_size;
	    return size + ARRAY_MERGE_Y_OFFSET + 0.5;
	}
	*/

	DataElement data[] = dataSet.getData();
	double thry = VAMPUtils.getThresholdMaxY(graphElem);
	for (int n = 0; n < data.length; n++) {
	    double y = data[n].getVY(graphElem);
	    if (y > thry) y = thry;
	    if (y > maxY) maxY = y;
	}

	//System.out.println("maxy -> " + maxY + ", " + roundMax(maxY, roundY));
	return roundMax(maxY, roundY) * maxYCoef(graphElem);
    }

    public double getMinX(GraphElement graphElem) {
	if (graphElem.asProfile() != null) {
	    return graphElem.asProfile().getMinX();
	}

	if (!graphElem.isFullImported()) {
	    //System.out.println("lminX -> " + graphElem.getLMinX()); 
	    return graphElem.getLMinX();
	}

	double minX = Double.MAX_VALUE;
	DataSet dataSet = graphElem.asDataSet();
	if (dataSet == null)
	    return minX;

	DataElement data[] = dataSet.getData();
	for (int n = 0; n < data.length; n++) {
	    if (data[n].getVX(graphElem) < minX) {
		minX = data[n].getVX(graphElem);
	    }
	}

	//System.out.println("minx -> " + roundMin(minX, roundX));
	return roundMin(minX, roundX);
    }

    public double getMinY(GraphElement graphElem) {
	if (VAMPUtils.getType(graphElem).equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE)) {
	    int gained_size = ((Vector<String>)graphElem.getPropertyValue(VAMPProperties.GainedVectorArrayProp)).size();
	    int lost_size = ((Vector<String>)graphElem.getPropertyValue(VAMPProperties.LostVectorArrayProp)).size();
	    int size = gained_size > lost_size ? gained_size : lost_size;
	    return -(size + ARRAY_MERGE_Y_OFFSET + 0.5);
	}

	if (graphElem.asProfile() != null) {
	    double minY = graphElem.asProfile().getMinY();
	    if (false) {
		System.out.println("WARNING getMinY() !!!!");
		return minY;
	    }
	    double thrMinY = VAMPUtils.getThresholdMinY(graphElem);
	    return minY > thrMinY ? minY : thrMinY;
	}

	if (!graphElem.isFullImported()) {
	    //System.out.println("lminY -> " + graphElem.getLMinY()); 
	    return graphElem.getLMinY();
	}

	double minY = Double.MAX_VALUE;
	DataSet dataSet = graphElem.asDataSet();
	if (dataSet == null) return minY;

	// EV 10/12/04
	/*
	if (VAMPUtils.getType(graphElem).equals(VAMPConstants.LOH_TYPE))
	    return 0.;
	*/

	/*
	if (VAMPUtils.getType(graphElem).equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE)) {
	    int gained_size = ((Vector<String>)graphElem.getPropertyValue(VAMPProperties.GainedVectorArrayProp)).size();
	    int lost_size = ((Vector<String>)graphElem.getPropertyValue(VAMPProperties.LostVectorArrayProp)).size();
	    int size = gained_size > lost_size ? gained_size : lost_size;
	    return -(size + ARRAY_MERGE_Y_OFFSET + 0.5);
	}
	*/

	DataElement data[] = dataSet.getData();
	double thry = VAMPUtils.getThresholdMinY(dataSet);
	for (int n = 0; n < data.length; n++) {
	    double y = data[n].getVY(graphElem);
	    if (y < thry) y = thry;
	    if (y < minY) minY = y;
	}
	double rminy = roundMin(minY, roundY);
	if (rminy > minY) { // kludge 13/01/06 for GenomeAnnotation display
	    //System.out.println("this case: rminy " + rminy + ", " + minY);
	    rminy -= roundY;
	}
	//System.out.println("miny -> " + rminy);
	return rminy;
    }

    private boolean displayPropName(GraphCanvas canvas, Axis axis, int height,
				    PrintContext pctx) {
	if (pctx != null)
	    return true;

	if (axis == null)
	    return false;

	double rpady = canvas.getVPadY() * canvas.getScale().getScaleY();
	return height+vSize < rpady;
    }

    public void displayArrayXAxis(GraphCanvas canvas, Axis xaxis,
				  Graphics2D g, GraphElement graphElem, int m,
				  PrintContext pctx) {

	if (xaxis == null && pctx == null) return;
	if (pctx != null && (pctx.getFlags() & PrintContext.X_AXIS) == 0) return;

	double maxY = canvas.getMaxY();
	double minY = canvas.getMinY();

	int y0;
	if (xaxis != null)
	    y0 = xaxis.getSize().height - 5;
	else
	    y0 = (int)(pctx.getBounds().y + pctx.getBounds().height) - 5;

	int y0e = y0 + 4;
	double vx0 = graphElem.getVBounds().y; // rotated !
	int x0 = (int)canvas.getRY(vx0); // rotated !
	int y1 = (int)canvas.getRX(vx0 + minY-maxY);

	if (pctx != null) {
	    x0 = (int)pctx.getRX(x0);
	    y1 = (int)pctx.getRY(y1);
	}

	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_LABEL_FG));
	// for now
	Vector<String> gainedGraphElements = (Vector<String>)graphElem.getPropertyValue(VAMPProperties.GainedVectorArrayProp);
	Vector<String> lostGraphElements = (Vector<String>)graphElem.getPropertyValue(VAMPProperties.LostVectorArrayProp);
	Vector graphElems = (Vector)graphElem.getPropertyValue(VAMPProperties.VectorArrayProp);
	int gained_size = gainedGraphElements.size();
	int lost_size = lostGraphElements.size();
	int total_size = graphElems.size();
	int size = (gained_size > lost_size ? gained_size : lost_size);

	String s = "Chr " + VAMPUtils.getChr(graphElem);

	Font font;
	font = VAMPResources.getFont(VAMPResources.AXIS_KARYO_NAME_DISPLAYER_FONT);
	g.setFont(font);
	Dimension d = Utils.getSize(g, s);
	int x = (int)canvas.getRY(vx0 + minY);
	if (pctx != null)
	    x = (int)pctx.getRX(x);

	g.drawString(s, x-d.width/2, y0-20);

	if (VAMPUtils.getType(graphElem).equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE)) {
	    s = lost_size + "/" + total_size + " : " + gained_size + "/" + total_size;

	    font = VAMPResources.getFont(VAMPResources.AXIS_KARYO_DISPLAYER_FONT);
	    g.setFont(font);
	    d = Utils.getSize(g, s);
	    x = (int)canvas.getRY(vx0 + minY);
	    if (pctx != null)
		x = (int)pctx.getRX(x);

	    g.drawString(s, x-d.width/2, y0-3);

	    font = getFont(size, canvas);
	    if (font == null)
		return;

	    g.setFont(font);

	    for (int n = 0; n < lost_size; n++) {
		//String id = (String)((GraphElement)lostGraphElements.get(n)).getID();
		String id = lostGraphElements.get(n);
	    
		x = (int)canvas.getRY(vx0 - (n+ARRAY_MERGE_Y_OFFSET) + minY);
		if (pctx != null)
		    x = (int)pctx.getRX(x);
		g.rotate(angle, x, y0);
		g.drawString(id, x, y0);
		g.rotate(-angle, x, y0);
	    }
	    
	    for (int n = 0; n < gained_size; n++) {
		//String id = (String)((GraphElement)gainedGraphElements.get(n)).getID();
		String id = gainedGraphElements.get(n);
		x = (int)canvas.getRY(vx0 + (n+ARRAY_MERGE_Y_OFFSET) + minY);
		if (pctx != null)
		    x = (int)pctx.getRX(x);
		g.rotate(angle, x, y0);
		g.drawString(id, x, y0);
		g.rotate(-angle, x, y0);
	    }
	}
    }

    public void displayArrayYAxis(GraphCanvas canvas, Axis yaxis,
				  Graphics2D g, GraphElement graphElem, int m,
				  PrintContext pctx) {

	// display in case of MergeArray in a point view

	double maxY = canvas.getMaxY();
	double minY = canvas.getMinY();

	//System.out.println("displayArrayYAxis: " + pctx);
	int x0;
	if (yaxis == null)
	    x0 = (int)canvas.getRX(graphElem.getVBounds().x);
	else
	    x0 = yaxis.getSize().width - 12;

	int x0e = x0 + 4;
	double vy0 = graphElem.getVBounds().y;
	int y0 = (int)canvas.getRY(vy0);
	int y1 = (int)canvas.getRY(vy0 + minY-maxY);

	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_LABEL_FG));
	if (yaxis != null) {
	    Vector<String> gainedGraphElements = (Vector<String>)graphElem.getPropertyValue(VAMPProperties.GainedVectorArrayProp);
	    Vector<String> lostGraphElements = (Vector<String>)graphElem.getPropertyValue(VAMPProperties.LostVectorArrayProp);
	    int gained_size = gainedGraphElements.size();
	    int lost_size = lostGraphElements.size();
	    int size = (gained_size > lost_size ? gained_size : lost_size);

	    /*
	    Vector graphElems = (Vector)graphElem.getPropertyValue(VAMPProperties.GainedVectorArrayProp);
	    int size = graphElems.size();
	    */

	    int maxW = 0;

	    Font font = VAMPResources.getFont(VAMPResources.AXIS_Y_DISPLAYER_FONT);
	    g.setFont(font);

	    for (int n = 0; n < lost_size; n++) {
		//String id = (String)((GraphElement)lostGraphElements.get(n)).getID();
		String id = lostGraphElements.get(n);
		Dimension d = Utils.getSize(g, id);
		if (d.width > maxW)
		    maxW = d.width;
	    }

	    for (int n = 0; n < gained_size; n++) {
		//String id = (String)((GraphElement)gainedGraphElements.get(n)).getID();
		String id = gainedGraphElements.get(n);
		Dimension d = Utils.getSize(g, id);
		if (d.width > maxW)
		    maxW = d.width;
	    }

	    int x0w = x0 - maxW - 4;
	    for (int n = 0; n < lost_size; n++) {
		//String id = (String)((GraphElement)lostGraphElements.get(n)).getID();
		String id = lostGraphElements.get(n);
		Dimension d = Utils.getSize(g, id);

		double y = canvas.getRY(vy0 - (n+ARRAY_MERGE_Y_OFFSET) + minY);
		g.drawString(id, x0w, (int)(y+d.height/2));
		g.drawLine(x0, (int)y, x0e, (int)y);
	    }

	    for (int n = 0; n < gained_size; n++) {
		//String id = (String)((GraphElement)gainedGraphElements.get(n)).getID();
		String id = gainedGraphElements.get(n);

		Dimension d = Utils.getSize(g, id);
		double y = canvas.getRY(vy0 + (n+ARRAY_MERGE_Y_OFFSET) + minY);
		g.drawString(id, x0w, (int)(y+d.height/2));
		g.drawLine(x0, (int)y, x0e, (int)y);
	    }
	    g.drawLine(x0, (int)y0, x0e, (int)y0);
	    g.drawLine(x0, (int)y1, x0e, (int)y1);
	}

	g.drawLine(x0, y0, x0, y1);
	double mY = y1;

	String name = (graphElementIDBuilder != null ?
		       graphElementIDBuilder.buildID(graphElem) : null);
	if (name != null) {
	    Font fontName = VAMPResources.getFont(VAMPResources.AXIS_Y_NAME_DISPLAYER_FONT);
	    g.setFont(fontName);

	    Dimension d = Utils.getSize(g, name);
	    if (displayPropName(canvas, yaxis, d.height, null)) {
		if (yaxis == null) {
		    g.setColor(VAMPResources.getColor
			       (VAMPResources.AXIS_Y_CANVAS_PROPERTY_NAME_FG));
		    g.drawString(name, (int)(x0-d.width/2), (int)(mY-d.height/2-2));
		}
		else {
		    g.setColor(VAMPResources.getColor
			       (VAMPResources.AXIS_Y_PROPERTY_NAME_FG));
		    g.drawString(name, 5, (int)(mY-d.height/2-2));
		}

	    }
	}

    }

    /*
    boolean checkGraphElements(LinkedList graphElems) {
	int size = graphElems.size();
	for (int n = 0; n < size; n++) {
	    DataSet dataSet = ((GraphElement)graphElems.get(n)).asDataSet();
	    Profile profile = ((GraphElement)graphElems.get(n)).asProfile();
	    if (dataSet == null && profile == null)
		return false;
	}

	return true;
    }
    */

    private Font getFont(int size, GraphCanvas canvas) {
	Font font =
	    VAMPResources.getFont(VAMPResources.AXIS_KARYO_DISPLAYER_FONT);
	/*
	if (size < 2)
	    return font;
	*/

	double w = canvas.getRH(1) + FONT_DELTA;
	if (w >= font.getSize())
	    return font;

	// try smaller font
	w++;
	font = VAMPResources.getFont(VAMPResources.AXIS_KARYO_SMALL_DISPLAYER_FONT);
	if (w >= font.getSize())
	    return font;

	// too small to display
	return null;
    }

    private void setAxisFG(Graphics g, Axis axis) {
	String res;
	if (axis != null)
	    res = VAMPResources.AXIS_FG;
	else if ((flags & IS_TRANS) == IS_TRANS)
	    res = VAMPResources.AXIS_TRANSCRIPTOME_FG;
	else if ((flags & IS_TRANS_REF) == IS_TRANS_REF)
	    res = VAMPResources.AXIS_TRANSCRIPTOME_REF_FG;
	else if ((flags & IS_TRANS_REL) == IS_TRANS_REL)
	    res = VAMPResources.AXIS_TRANSCRIPTOME_REL_FG;
	else
	    res = VAMPResources.AXIS_FG;

	g.setColor(VAMPResources.getColor(res));
    }

    private void setAxisLineFG(Graphics g) {
	String res;
	if ((flags & IS_TRANS) == IS_TRANS)
	    res = VAMPResources.AXIS_TRANSCRIPTOME_LINE_FG;
	else if ((flags & IS_TRANS_REF) == IS_TRANS_REF)
	    res = VAMPResources.AXIS_TRANSCRIPTOME_REF_LINE_FG;
	else if ((flags & IS_TRANS_REL) == IS_TRANS_REL)
	    res = VAMPResources.AXIS_TRANSCRIPTOME_REL_LINE_FG;
	else
	    res = VAMPResources.AXIS_LINE_FG;

	g.setColor(VAMPResources.getColor(res));
    }

    private void setAxisLabelFG(Graphics g) {
	String res;
	if ((flags & IS_TRANS) == IS_TRANS)
	    res = VAMPResources.AXIS_TRANSCRIPTOME_LABEL_FG;
	else if ((flags & IS_TRANS_REF) == IS_TRANS_REF)
	    res = VAMPResources.AXIS_TRANSCRIPTOME_REF_LABEL_FG;
	else if ((flags & IS_TRANS_REL) == IS_TRANS_REL)
	    res = VAMPResources.AXIS_TRANSCRIPTOME_REL_LABEL_FG;
	else
	    res = VAMPResources.AXIS_LABEL_FG;

	g.setColor(VAMPResources.getColor(res));
    }

    void displayInfo(GraphCanvas canvas, Axis xaxis, Graphics2D g) {
	if (xaxis == null)
	    return;
	String msg = canvas.getView().getMessage();
	if (msg == null)
	    return;

	int width = xaxis.getSize().width;
	g.setFont(VAMPResources.getFont(VAMPResources.AXIS_MESSAGE_FONT));
	Dimension sz = Utils.getSize(g, msg);
	g.setColor(VAMPResources.getColor(VAMPResources.AXIS_MESSAGE_COLOR));
	//	g.drawString(msg, (width-sz.width)/2, sz.height-1); // was +2
	g.drawString(msg, (width-sz.width)/2, sz.height); // was +2
    }

    protected double maxYCoef(GraphElement graphElem) {
	Object show_asso_o = graphElem.getPropertyValue(BreakpointFrequencyOP.showAssoProp);
	if (show_asso_o == null || !((Boolean)show_asso_o).booleanValue())
	    return 1;

	DataSet dataSet = graphElem.asDataSet();
	DataElement data[] = dataSet.getData();
	int size = 0;
	for (int n = 0; n < data.length; n++) {
	    Vector link_v = (Vector)data[n].getPropertyValue(VAMPProperties.LinkedDataProp);
	    if (link_v != null)
		size += link_v.size();
	}

	if (size > 1000)
	    return 5.;

	if (size > 300)
	    return 4.;

	if (size > 100)
	    return 3.;

	if (size > 50)
	    return 2.;

	return 1.5;
    }

    static double getValue(double n, double affineTransform[]) {
	return n/affineTransform[1] - affineTransform[0];
    }


    static double computeDelta(double value, GraphElement graphElem, double minY,
			       double maxY,
			       double stepY, double roundY) {
	double delta = 0;
	if (value >= minY && value <= maxY) {
	    for (double n = minY; n <= maxY; n += stepY) {
		if (n >= value) {
		    delta = n - value;
		    break;
		}
	    }

	    double d = (long)(delta / roundY) * roundY;
	    /*
	    System.out.println("-> " + d + " " + delta + " " + (d + roundY) + " " + roundY);
	    */

	    if ((delta - d) >= (d + roundY - delta))
		delta = d + roundY;
	    else
		delta = d;
	}

	return -delta;
    }

    static double computeDelta(GraphElement graphElem, double minY, double maxY,
			       double stepY, double roundY) {
	if (VAMPUtils.isLogScale(graphElem))
	    return computeDelta(0., graphElem, minY, maxY, stepY, roundY);

	return computeDelta(1., graphElem, minY, maxY, stepY, roundY);
    }

}

