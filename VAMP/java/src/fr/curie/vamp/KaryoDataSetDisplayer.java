
/*
 *
 * KaryoDataSetDisplayer.java
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

class KaryoDataSetDisplayer extends StandardDataSetDisplayer {

    static final double RPADY = 5.;
    static final int ARRAY_MERGE_Y_OFFSET = MergeArrayOP.Y_OFFSET;
    static final int MM_OFFSET = 20;
    static final int MAX_BARPLOT_WIDTH = 150;
    static Property arrayProp = VAMPProperties.ArrayProp;
    static final int POINT_TYPE = 1;
    static final int BARPLOT_TYPE = 2;

    static final int FRAGL_TYPE = 1;
    static final int PROFILE_TYPE = 2;

    static double DEFAULT_COEF = 0.96;

    int type, disptype;
    int MIN_CHROMOSOME_WIDTH;
    int MAX_CHROMOSOME_WIDTH;

    KaryoDataSetDisplayer(int type, int disptype) {
	super((type & FRAGL_TYPE) != 0 ? "Karyotype FrAGL" : "Karyotype Classic", null);
	this.type = type;
	this.disptype = disptype;

	if (type == PROFILE_TYPE) {
	    setGNLColorCodes(true);
	    showSmoothingLines(true);
	}

	MIN_CHROMOSOME_WIDTH =
	    VAMPResources.getInt(VAMPResources.MIN_KARYO_CHROMOSOME_WIDTH);
	MAX_CHROMOSOME_WIDTH =
	    VAMPResources.getInt(VAMPResources.MAX_KARYO_CHROMOSOME_WIDTH);
    }

    public void display(GraphCanvas canvas, Graphics2D g,
			GraphElement graphElement,
			int m, PrintContext pctx) {
	//DataSet dataSet = graphElement.asDataSet();
	long ms0 = System.currentTimeMillis();

	Font font = VAMPResources.getFont(VAMPResources.DATASET_DISPLAYER_FONT);

	int pointWidth = VAMPResources.getInt(VAMPResources.POINT_KARYO_WIDTH);

	int pointWidth2 = pointWidth/2;
	int centerWidth = VAMPResources.getInt(VAMPResources.ITEM_CENTERED_SIZE);
	//DataElement data[] = dataSet.getData();
	boolean isVisible;
	if (pctx == null)
	    isVisible = canvas.isRR_Visible(graphElement.getRBounds());
	else
	    isVisible = true;

	Dimension rSize = canvas.getSize();

	int x0 = (int)graphElement.getRBounds().x;
	Color probeSetFG = VAMPResources.getColor
	    (VAMPResources.PROBE_SET_FG);
	Color itemCenteredFG = VAMPResources.getColor
	    (VAMPResources.ITEM_CENTERED_FG);

	try {
	    int data_cnt = graphElement.getProbeCount();

	    if (data_cnt > 0) {
		GNLCodeManage(graphElement.getDataProxy(0, true).getPropertyValue(VAMPProperties.GNLProp) == null);
	    }

	    Rectangle2D.Double rb = graphElement.getRBounds();

	    int maxw = (int)canvas.getRH(ARRAY_MERGE_Y_OFFSET*2);
	    if (pctx != null)
		maxw = (int)pctx.getRW(maxw);

	    int mmw = maxw - MM_OFFSET;
	    for (; mmw < maxw; mmw++)
		if (mmw >= MIN_CHROMOSOME_WIDTH)
		    break;

	    if (mmw > MAX_CHROMOSOME_WIDTH)
		mmw = MAX_CHROMOSOME_WIDTH;

	    int mmw2 = mmw/2;
	    double rbx = rb.x;
	    double rbw = rb.width;
	    if (pctx != null) {
		rbx = pctx.getRX(rbx);
		rbw = pctx.getRW(rbw);
	    }

	    int xm = (int)(rbx + rbw/2);

	    // check for maxwidth
	    int maxwidth = 0;
	    for (int n = 0; n < data_cnt; n++) {
		RODataElementProxy item = graphElement.getDataProxy(n, false);
		//double rx = item.getRX(canvas, graphElement);
		double rx = item.getRX(canvas, graphElement, false);
		if (pctx != null)
		    rx = pctx.getRX(rx);

		int width = computeWidth(rx, xm, mmw2);
		if (width > maxwidth)
		    maxwidth = width;
	    }

	    double coef = DEFAULT_COEF;
	    if (maxwidth > MAX_BARPLOT_WIDTH)
		coef *= (double)MAX_BARPLOT_WIDTH/maxwidth;

	    // must compute smoothing info for all dataset (lost or gained)
	    // must get the MergeArrayOP.paramProp property to have the parameters:
	    // (gain || amplicon || mergeGainAmplicon), lost to compute the segments


	    TreeMap params = (TreeMap)graphElement.getPropertyValue(MergeArrayOP.paramsProp);

	    boolean first = true;
	    for (int gained = 0; gained <= 1; gained++) {

		HashMap graphElement_map = makeGraphElementMap(graphElement, gained);
		int cnt_ds = graphElement_map.size();

		double lastsy[] = new double[cnt_ds];
		int lastry[] = new int[cnt_ds];
		int lastrx[] = new int[cnt_ds];
		int lastgnl[] = new int[cnt_ds];
		for (int n = 0; n < cnt_ds; n++) {
		    lastsy[n] = Double.MAX_VALUE;
		    lastry[n] = Integer.MIN_VALUE;
		}
	    
		for (int n = 0; n < data_cnt; n++) {
		    RODataElementProxy item = graphElement.getDataProxy(n, false);

		    int skip = item.getUserVal(MergeArrayOP.SKIP_OFFSET, MergeArrayOP.SKIP_SIZE);
		    
		    if (skip < 0) {
			if (item.getPropertyValue(MergeArrayOP.SkipProbeProp) != null)
			    continue;
		    }
		    else {
			if (skip > 0) {
			    continue;
			}
		    }

		    double posy = item.getPosY(graphElement);
		    //int which = getWhich(graphElement_map, item);
		    int which = getWhich2(graphElement_map, item, graphElement);
		    // getWhich2 does not work because graphElement has not the good name ! It is named Chr N Array instead of the original profile name
		    //System.out.println("which " + which + " " + which2);
		    //assert which == which2;
		    //System.out.println("which: " + which);
		    if (which < 0 && type != FRAGL_TYPE) {
			continue;
		    }
		
		    if (posy == 0) {
			//System.out.println("posy == 0 " + item.getID() + " " + VAMPUtils.isNA(item));
			if (!VAMPUtils.isNA(item)) {
			    reset(lastsy, lastry, which);
			}
			continue;
		    }

		    if (gained == 0) {
			if (posy < 0) {
			    //System.out.println("posy < 0 " + item.getID() + " " + VAMPUtils.isNA(item));
			    if (!VAMPUtils.isNA(item)) {
				reset(lastsy, lastry, which);
			    }
			    continue;
			}
		    }
		    else {
			if (posy > 0) {
			    //System.out.println("posy > 0 " + item.getID() + " " + VAMPUtils.isNA(item));
			    if (!VAMPUtils.isNA(item)) {
				reset(lastsy, lastry, which);
			    }
			    continue;
			}
		    }

		    double size = item.getRSize(canvas, graphElement);
		    int offset_y = 0;
		    if (size < pointWidth) {
			size = pointWidth;
			offset_y = pointWidth2;
		    }

		    double rx = item.getRX(canvas, graphElement, false);
		    double ry = item.getRY(canvas, graphElement, false);
		    //System.out.println(item.getPropertyValue(FrAGLOP.FrequencyProp) + " " + item.getPosX(graphElement) + " " + rx + " " + ry + " " + item.getPosY(graphElement));
		    if (pctx != null) {
			rx = pctx.getRX(rx);
			ry = pctx.getRY(ry);
		    }

		    //System.out.println("rx " + rx + ", ry " + ry);
		    Point barplotRX = null;
		    int sy1 = (int)(ry-offset_y);

		    if (disptype != POINT_TYPE)
			barplotRX = computeBarplotRX(rx, xm, mmw2, coef);

		    if (pctx == null) {
			if (disptype == POINT_TYPE)
			    item.setRBounds(graphElement, (int)rx, sy1, pointWidth, (int)size);
			else {
			    //barplotRX = computeBarplotRX(rx, xm, mmw2, coef);
			    item.setRBounds(graphElement, barplotRX.x, sy1, barplotRX.y-barplotRX.x, 2);
			}
		    }
		
		    //String value = (String)item.getPropertyValue(VAMPProperties.SmoothingProp);
		    //String gnl_s = (String)item.getPropertyValue(VAMPProperties.GNLProp);
		    String gnl_s = VAMPUtils.getGNL(item);

		    if (gnl_s == null) {
			throw new Exception("Cannot find GNL for " + item.getID());
		    }

		    int gnl = GNLProperty.getGNL(gnl_s);

		    if (isVisible) {
			g.setColor(Color.BLACK);
			g.setFont(font);
			item.setGraphics(g, graphElement);
			if (disptype == POINT_TYPE &&
			    (showSmoothingLines() && isGNLColorCodes())) {

			    /*if (value_gnl != null && !value_gnl.equals("NA"))*/ {
				//double sy = Utils.parseDouble(value);
				//if (sy == lastsy[which]) {
				if (gnl == lastgnl[which]) {
				    if (lastry[which] != Integer.MIN_VALUE) {
					g.setColor(getGNLColor(gnl));
					g.fillRect((int)lastrx[which],
						   (int)(lastry[which] - offset_y),
						   pointWidth,
						   (int)(ry-lastry[which]) + (int)size);
				    }
				}

				lastgnl[which] = GNLProperty.getGNL
				    (VAMPUtils.getGNL(item));
				//((String)item.getPropertyValue(VAMPProperties.GNLProp));
			    
				lastrx[which] = (int)rx;
				//lastsy[which] = sy;
				lastry[which] = (int)ry;
			    }
			    /*
			      else
			      reset(lastsy, lastry, which);
			    */
			}
			else {
			    if (disptype == POINT_TYPE)
				g.fillRect((int)rx, sy1, pointWidth, (int)size);

			    else {
				g.drawLine(barplotRX.x, sy1,  barplotRX.y, sy1);
				if (first) {
				    /*
				      g.setColor(Color.BLUE);
				      barplotRX = computeBarplotRX(1., xm, mmw2, coef);
				      g.drawLine(barplotRX.x, sy1,  barplotRX.y, sy1);
				      barplotRX = computeBarplotRX(-1., xm, mmw2, coef);
				      g.drawLine(barplotRX.x, sy1,  barplotRX.y, sy1);
				    */
				    first = false;
				}
			    }
			}
		    }
		}
	    }

	    if (mmw >= MIN_CHROMOSOME_WIDTH) {
		Chromosome chr = VAMPUtils.getChromosome(canvas.getGlobalContext(),
							 VAMPUtils.getOS(graphElement),
							 VAMPUtils.getChr(graphElement));
		Band bands[] = chr.getBands();
		int height = (int)canvas.getRW(bands[bands.length-1].getEnd())
		    + MiniMapPanel.getPadH();
	    
		//double rbx = rb.x;
		double rby = rb.y;
		//double rbw = rb.width;
		double rbh = rb.height;
		if (pctx != null) {
		    //rbx = pctx.getRX(rbx);
		    rby = pctx.getRY(rby);
		    //rbw = pctx.getRW(rbw);
		    rbh = pctx.getRH(rbh);
		    height = (int)pctx.getRH(height);
		}

		//int xm = (int)(rbx + rbw/2);
		int dx = xm-mmw/2;
		int dy = (int)(rby - rbh) - MiniMapPanel.getPadY();

		g.translate(dx, dy);

		g.setColor(canvas.getBGColor());
		MiniMapPanel.paintMiniMap(g, new Dimension(mmw, (int)height),
					  Color.WHITE, 0, mmw, chr, false);
	    
		g.translate(-dx, -dy);
	    }
	}
	catch(Exception e) {
	    e.printStackTrace();
	}

	long ms1 = System.currentTimeMillis();
	//System.out.println("karyo: " + (ms1 - ms0) + " ms");
	//display_x(canvas, g, graphElement, m, pctx);
    }

    public void computeVBounds(GraphCanvas canvas, Graphics2D g, GraphElement graphElement, int m) {
	graphElement.resetPaintVBounds();
    }

    void setGraphElements(java.util.LinkedList graphElements) {
	DataSetSizePerformer.getSetSizePerformer().apply(graphElements);
	showSize(true);
    }

    boolean isRotated() { return true; }
    double getRPadY() {return RPADY;}

    boolean showBreakpointsEnabled() {return false;}
    boolean showSmoothingLinesEnabled() {return isGNLColorCodes();}
    boolean showSmoothingPointsEnabled() {return false;}
    boolean showCentromereEnabled() {return false;}
    boolean showOutEnabled() {return false;}
    boolean showTagEnabled() {return false;}
    boolean showTagTextEnabled() {return false;}

    private void reset(double lastsy[], int lastry[], int which) {
	if (type != FRAGL_TYPE) {
	    lastsy[which] = Double.MAX_VALUE;
	    lastry[which] = Integer.MIN_VALUE;
	}
    }

    private HashMap makeGraphElementMap(GraphElement graphElement, int gained) {
	HashMap map = new HashMap();
	Vector<String> graphElements = (Vector<String>)graphElement.getPropertyValue(gained == 0 ? VAMPProperties.LostVectorArrayProp : VAMPProperties.GainedVectorArrayProp); // for now
	if (graphElements == null)
	    return map;
	int size = graphElements.size();
	//System.out.println("makeGraphElementMap:");
	for (int n = 0; n < size; n++) {
	    /*
	    GraphElement dset = (GraphElement)graphElements.get(n);
	    map.put(dset.getID(), new Integer(n));
	    */
	    //System.out.println(graphElements.get(n) + " -> " + n);
	    map.put(graphElements.get(n), new Integer(n));
	}

	return map;
    }

    private int getWhich(HashMap map, RODataElementProxy item) {
	String arr_name = (String)item.getPropertyValue(arrayProp);
	if (map.get(arr_name) == null) {
	    return -1;
	}
	return ((Integer)map.get(arr_name)).intValue();
    }

    private int getWhich2(HashMap map, RODataElementProxy item, GraphElement graphElement) {
	int which = item.getUserVal(MergeArrayOP.ARRAY_OFFSET, MergeArrayOP.ARRAY_SIZE);
	if (which < 0) {
	    return getWhich(map, item);
	}

	Vector arrayMap = (Vector)graphElement.getPropertyValue(Property.getProperty("ALL"));

	if (arrayMap == null) {
	    return -1;
	}

	String arr_name = (String)arrayMap.get(which);
	if (map.get(arr_name) == null) {
	    return -1;
	}
	return ((Integer)map.get(arr_name)).intValue();
    }

    private int computeWidth(double rx, double xm, int mmw2) {
	double left, right;
	if (rx < xm) {
	    left = rx;
	    right = xm - mmw2 - 1;
	}
	else {
	    left = xm + mmw2 + 1;
	    right = rx;
	}
	return (int)(right - left);
    }

    private Point computeBarplotRX(double rx, double xm, int mmw2,
				   double coef) {
	double left, right;
	if (rx < xm) {
	    right = xm - mmw2 - 1;
	    double width = (right - rx) * coef;
	    left = right - width;
	}
	else {
	    left = xm + mmw2 + 1;
	    double width = (rx - left) * coef;
	    right = left + width;
	}

	return new Point((int)left, (int)right);
    }

    private Color getGNLColor(int gnl) {
	if (gnl == VAMPConstants.CLONE_GAINED)
	    return VAMPResources.getColor(VAMPResources.GNL_GAINED_FG);

	if (gnl == VAMPConstants.CLONE_LOST)
	    return VAMPResources.getColor(VAMPResources.GNL_LOST_FG);

	if (gnl == VAMPConstants.CLONE_AMPLICON)
	    return VAMPResources.getColor(VAMPResources.GNL_AMPLICON_FG);

	return Color.BLACK;
    }

    boolean useHardThresholds() {
	return false;
    }
}
