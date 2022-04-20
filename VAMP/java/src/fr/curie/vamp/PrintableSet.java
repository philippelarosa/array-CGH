
/*
 *
 * PrintableSet.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.net.*;
import javax.swing.*;
import javax.swing.text.html.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import java.text.DateFormat;

class PrintableSet implements Pageable, Printable {

    public static final int PRINT_SELECTED = 0x1;
    public static final int PRINT_ALL = 0x2;
    public static final int PRINT_MERGE = 0x4; // not yet used
    public static final boolean STRICT_CLIP = true;

    public static final String DEFAULT_TABLE_BACKGROUND = "#FFFFFF";

    public static final int THRU = 0x8;
    public static final int WHOLE_X = 0x10;
    public static final int WHOLE_Y = 0x20;

    private boolean showAreaBounds = false;
    private boolean showAreaNames = false;
    private boolean editMode = false;

    View view;
    PrintPreviewerPanel printPreviewerPanel;
    private int flags;
    Vector graphElements_v;
    Vector panel_v;
    Date currentDate;
    boolean rotated;
    private PrintPageTemplate template;
    PageFormat format;
    private int pageCount, graphElementCountPerPage;
    static Property PageNumProp = Property.getHiddenProperty("PageNum");
    static Property PageCountProp = Property.getHiddenProperty("PageCount");
    static Property ProjectProp = Property.getHiddenProperty("Project");
    static Property TeamProp = Property.getHiddenProperty("Team");
    static Property PrintDateProp = Property.getHiddenProperty("PrintDate");
    static Property PrintDateTimeProp = Property.getHiddenProperty("PrintDatetime");
    static Property GraphElementCountProp = Property.getHiddenProperty("Datasetcount");
    static Property TitleProp = Property.getHiddenProperty("Title");
    static Property MinimapChrProp = Property.getHiddenProperty("MMChr");
    static Property MinimapResolProp = Property.getHiddenProperty("MMResolution");
    static Property MinimapOrgProp = Property.getHiddenProperty("MMOrganism");

    static Property CCMinRGBProp = Property.getHiddenProperty("CCMinRGB");
    static Property CCNormalRGBProp = Property.getHiddenProperty("CCNormalRGB");
    static Property CCMaxRGBProp = Property.getHiddenProperty("CCMaxRGB");
    static Property CCAmpliconRGBProp = Property.getHiddenProperty("CCAmpliconRGB");
    static Property CCMinProp = Property.getHiddenProperty("CCMin");
    static Property CCNormalMinProp = Property.getHiddenProperty("CCNormalMin");
    static Property CCNormalMaxProp = Property.getHiddenProperty("CCNormalMax");
    static Property CCMaxProp = Property.getHiddenProperty("CCMax");
    static Property CCAmpliconProp = Property.getHiddenProperty("CCAmplicon");

    static Property AnnotRGBProp = Property.getHiddenProperty("AnnotRGB");
    static Property AnnotNameProp = Property.getHiddenProperty("AnnotName");
    static Property AnnotOPProp = Property.getHiddenProperty("AnnotOP");
    static Property AnnotValueProp = Property.getHiddenProperty("AnnotValue");

    Vector area_v;

    static DateFormat dateFormat =
	DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH);
    static DateFormat dateTimeFormat =
	DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
				       DateFormat.SHORT,
				       Locale.ENGLISH);
    static final int XMARGIN = 5;
    static final int YMARGIN = 1;
    PropertyElement textElem;
    static final double MIN_PAD = 30;
    boolean has_m_pad;
    double m_pad;
    PrintTextEditor printTextEditor;
    PrintPreviewer previewer;

    PrintableSet(View view, int flags, int pageCount,
		 int graphElementCountPerPage,
		 PrintPageTemplate template) {
	this.view = view;
	init(flags, pageCount, graphElementCountPerPage, template);
    }

    void init(int flags, int pageCount,
	      int graphElementCountPerPage,
	      PrintPageTemplate template) {
	has_m_pad = hasMPad();
	this.template = template;
	this.area_v = new Vector();
	textElem = new PropertyElement();

	if (template.getPrintFlags() > 0)
	    flags = template.getPrintFlags();

	this.flags = flags;

	if (template.getPerPage() > 0)
	    graphElementCountPerPage = template.getPerPage();

	int panel_count = view.getGraphPanelSet().getPanelCount();
	graphElements_v = new Vector();
	panel_v = new Vector();

	for (int m = 0; m < panel_count; m++) {
	    GraphPanel panel = view.getGraphPanelSet().getPanel(m);
	    rotated = panel.getDefaultGraphElementDisplayer().isRotated();
	    panel_v.add(m, panel);
	
	    LinkedList graphElements = null;

	    if ((flags & PRINT_SELECTED) != 0)
		graphElements = Utils.vectorToList
		    (view.getSelectedGraphElements(m));
	    else if ((flags & PRINT_ALL) != 0)
		graphElements = view.getGraphElements(m);

	    graphElements_v.add(m, graphElements);
	}

	if (isVisibleY()) {
	    this.pageCount = 1;
	    this.graphElementCountPerPage = 1;
	}
	else {
	    if (pageCount <= 0 && graphElementCountPerPage <= 0)
		pageCount = 1;

	    int size = getMaxSize();

	    if (graphElementCountPerPage > size)
		graphElementCountPerPage = size;

	    if (pageCount > 0) {
		this.pageCount = pageCount;
		this.graphElementCountPerPage = size / pageCount;
	    }
	    else if (graphElementCountPerPage > 0) {
		this.graphElementCountPerPage = graphElementCountPerPage;
		this.pageCount = size / graphElementCountPerPage;
		if ((size % graphElementCountPerPage) != 0)
		    this.pageCount++;
	    }
	    // else: never reached

	    if (this.pageCount <= 0)
		this.pageCount = 1;
	}

	currentDate = new Date();
	format = template.getPageFormat();
	orderAreas(null);
	reset();
    }

    void reset() {
	int sz = area_v.size();

	for (int n = 0; n < sz; n++) {
	    PrintArea area = (PrintArea)area_v.get(n);
	    area.setSelected(false);
	}
    }

    PrintPreviewer preview() {
	/*
	  System.out.println("preview: pg_cnt=" +
			   pageCount + ", dspg_cnt=" +
			   graphElementCountPerPage);
	System.out.println("format: W=" +
		       (int)format.getWidth() + ", H=" + 
		       (int)format.getHeight() + ", ix=" + 
		       (int)format.getImageableX() + ", iy=" +
		       (int)format.getImageableY() + ", iw=" +
		       (int)format.getImageableWidth() + ", ih=" +
		       (int)format.getImageableHeight());
	*/

	//previewer = new PrintPreviewer(this);
	previewer.display();
	return previewer;
    }

    public int getNumberOfPages() {return pageCount;}

    public PageFormat getPageFormat(int pagenum) {return format;}

    public Printable getPrintable(int pagenum) {return this;}

    static final int T_PADY = 30;
    static final int B_PADY = 20;
    static final int PADY = T_PADY + B_PADY;

    static final int L_PADX = 30;
    static final int R_PADX = 10;
    static final int PADX = R_PADX + L_PADX;
    int lastPageNum;

    public int print(Graphics _g, PageFormat _page_fmt, int pagenum) {
	if (pagenum < 0 || pagenum >= pageCount)
	    return NO_SUCH_PAGE;

	boolean isPreview = (_page_fmt == null);
	Graphics2D g = (Graphics2D)_g;
	Rectangle mainClip = g.getClipBounds();
	    
	g.clipRect((int)format.getImageableX(),
		   (int)format.getImageableY(),
		   (int)format.getImageableWidth(),
		   (int)format.getImageableHeight());

	setTextElemProperties(pagenum);
	syncHTMLAreas();

	int sz = area_v.size();

	if (hasMPad() && !isVisibleY()) {
	    m_pad = format.getImageableHeight() / (graphElementCountPerPage * 4);
	    if (m_pad < MIN_PAD)
		m_pad = MIN_PAD;
	}
	else
	    m_pad = 0;

	for (int n = 0; n < sz; n++) {
	    PrintArea area = (PrintArea)area_v.get(n);
	    showArea(g, area);

	    String name = area.getName();
	    int panel_num = area.getPanelNum();

	    GraphPanel panel = getPanel(panel_num);

	    if (panel == null)
		continue;

	    if (name.equals(PrintArea.MINIMAP))
		paintMiniMap(area, g);
	    else if (name.equals(PrintArea.GRAPHELEMENTS))
		paintGraphElements(area, g,
				   panel,
				   getGraphElements(panel_num),
				   pagenum);
	    else if (name.equals(PrintArea.XSCALE))
		paintXScale(area, g,
			    panel,
			    getGraphElements(panel_num),
			    pagenum);
	    else if (name.equals(PrintArea.YSCALE))
		paintYScale(area, g,
			    panel,
			    getGraphElements(panel_num),
			    pagenum);
	    else if (name.equals(PrintArea.YANNOT))
		paintYAnnot(area, g,
			    panel,
			    getGraphElements(panel_num),
			    pagenum);
	    else if (area instanceof PrintTextArea)
		printTextArea((PrintTextArea)area, g,
			      panel,
			      getGraphElements(panel_num),
			      pagenum);
	    else if (area instanceof PrintHTMLArea)
		printHTMLArea((PrintHTMLArea)area, g,
			      panel,
			      getGraphElements(panel_num),
			      pagenum,
			      isPreview);
	    else if (area instanceof PrintImageArea)
		printImageArea((PrintImageArea)area, g,
			       panel,
			       getGraphElements(panel_num),
			       pagenum);
	}

	if (mainClip != null)
	    g.setClip(mainClip.x, mainClip.y, mainClip.width, mainClip.height);
	/*
	System.out.println("PrintableSet.print(" + pagenum + ") end " +
			   (((new Date()).getTime()) - t0));
	*/
	return PAGE_EXISTS;
    }

    private void printTextArea(PrintTextArea area, Graphics2D g,
			       GraphPanel panel,
			       LinkedList graphElements, int pagenum) {
	Rectangle2D.Double r = area.getArea();
	Rectangle clip = g.getClipBounds();
	g.clipRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);

	g.setColor(area.getFGColor());
	Font font = area.getFont();
	g.setFont(font != null ? font :
		  VAMPResources.getFont(VAMPResources.SEARCH_PANEL_BUTTON_FONT));
	String lines[] = Utils.getLines(textElem.
					fromTemplate(area.getTemplate()));
	int yoffset = 0;
	for (int j = 0; j < lines.length; j++) {
	    String s = lines[j];
	    Dimension d = Utils.getSize(g, s);
	    int xoffset = XMARGIN;
	    if (s.charAt(0) == '@') {
		s = s.substring(1, s.length());
		xoffset += (int)(r.width-d.width)/2;
	    }
	    yoffset += YMARGIN + d.height;
	    g.drawString(s, (int)(r.x+xoffset), (int)(r.y+yoffset));
	}
	
	g.setClip(clip.x, clip.y, clip.width, clip.height);
    }

    private void printHTMLArea(PrintHTMLArea area, Graphics2D g,
			       GraphPanel panel,
			       LinkedList graphElements, int pagenum,
			       boolean isPreview) {
	if (!area.isInitialized())
	    syncHTMLArea(area);

	Point r = area.getPreviewArea().getLocation();
	g.translate(r.x, r.y);
	area.getPreviewArea().paint(g);
	g.translate(-r.x, -r.y);
    }

    private void printImageArea(PrintImageArea area, Graphics2D g,
				GraphPanel panel,
				LinkedList graphElements, int pagenum) {
	String url = area.getImageURL();
	//System.out.println("printImageArea: " + url);
	if (url == null) return;
	Rectangle2D.Double r = area.getArea();
	Rectangle clip = g.getClipBounds();
	g.clipRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);

	Dimension img_sz =
	    Utils.drawImage(g, view.getToolkit(), url,
			    (int)r.x, (int)r.y, -1, false, false);
	area.setImageSize(img_sz);
	//System.out.println("img_sz: " + img_sz);
	g.setClip(clip.x, clip.y, clip.width, clip.height);
    }

    private void showArea(Graphics2D g, PrintArea area) {
	if (g == null) return;

	Rectangle2D.Double r = area.getArea();
	boolean isTextArea = area instanceof PrintTextArea;
	boolean isHTMLArea = area instanceof PrintHTMLArea;

	boolean editTextMode = false;

	if (editMode && isHTMLArea) {
	    if (editTextMode)
		g.setColor(Color.ORANGE);
	    else
		g.setColor(new Color(0xdddddd));
	    g.fillRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	}
	else {
	    g.setColor(area.getBGColor());
	    g.fillRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	}

	if (area.isSelected()) {
	    g.setColor(Color.BLUE);
	    g.drawRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	}
	else if (area.hasBorder()) {
	    g.setColor(area.getBDColor());
	    g.drawRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	}
	else if (showAreaBounds || (isTextArea && editTextMode)) {
	    g.setColor(Color.GRAY);
	    g.drawRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	}

	if (showAreaNames) {
	    g.setColor(Color.GRAY);
	    g.setFont(VAMPResources.getFont(VAMPResources.SEARCH_PANEL_BUTTON_FONT));
	    String name = area.getName();
	    Dimension d = Utils.getSize(g, name);
	    g.drawString(name, (int)(r.x+(r.width-d.width)/2),
			 (int)(r.y+(r.height)/2));
	}
    }

    PrintContext makePrintContext(GraphCanvas canvas,
				  GraphElement graphElement,
				  PrintArea area, double h,
				  int which, int start, int end, int pflags) {
	Rectangle2D.Double rBounds = graphElement.getRBounds();

	if (isVisibleY() && !inOnePage() && !canvas.isRR_Visible(rBounds)) {
	    return null;
	}

	return makePrintContext(canvas, rBounds, area, h, which, start, end,
				pflags);
    }

    PrintContext makePrintContext(GraphCanvas canvas,
				  Rectangle2D.Double rBounds,
				  PrintArea area, double h,
				  int which, int start, int end,
				  int pflags) {
	double dx = 0, dy = 0;
	double sx = 0., sy = 0.;
	Rectangle2D.Double r = area.getArea();
	Dimension canvas_size = canvas.getSize();

	/*
	System.out.println(which + which);
	System.out.println("rBounds " + rBounds);

	System.out.println("r.x " + r.x);
	System.out.println("r.width " + r.width);
	System.out.println("canvas.width " + canvas_size.width);
	System.out.println("canvas.scaleX " + canvas.getScale().getScaleX());
	System.out.println("canvas.canonScaleX " + canvas.getCanonScale().getScaleX());
	System.out.println("canvas.origX " + canvas.getOrig().x);
	System.out.println("sx " + (r.width / (double)canvas_size.width));
	System.out.println("isVisibleX " + isVisibleX() + " " + pflags);
	*/

	if (isVisibleX()) {
	    sx = r.width / (double)canvas_size.width;
	    dx = (int)r.x;
	    if (inOnePage()) {
		sx /= canvas.getCanonScale().getScaleX();
		dx -= canvas.getOrig().x * sx;
		dx += sx * canvas.getRMargins().getMarginWest();
	    }
	}
	else if ((pflags & PrintContext.Y_AXIS) == 0) {
	    if (rotated) {
		sx = h / rBounds.width;
		dx = r.x + L_PADX + (which-start) * (h + m_pad) -
		    sx * rBounds.x;
	    }
	    else {
		sx = (r.width - PADX) / rBounds.width;
		dx = r.x + L_PADX - sx * rBounds.x;
	    }
	}
	else if ((pflags & PrintContext.Y_ANNOT) == 0) {
	    if (rotated) {
		sx = h / rBounds.width;
		dx = r.x + L_PADX + (which-start) * (h + m_pad) -
		    sx * rBounds.x;
	    }
	    else {
		sx = (r.width - PADX) / rBounds.width;
		dx = r.x + L_PADX - sx * rBounds.x;
	    }
	}

	if (isVisibleY()) {
	    sy = r.height / (double)canvas_size.height;
	    dy = r.y;
	    if (inOnePage()) {
		sy /= canvas.getCanonScale().getScaleY();
		dy -= canvas.getOrig().y * sy;
		dy += sy * canvas.getRMargins().getMarginNorth();
	    }
	}
	else if ((pflags & PrintContext.X_AXIS) == 0) {
	    if (rotated) {
		sy = (r.height - PADY) / rBounds.height;
		dy = r.y + T_PADY -
		    sy * (rBounds.y - rBounds.height);
	    }
	    else {
		sy = h / rBounds.height;
		dy = r.y + T_PADY + (which-start) * (h + m_pad) -
		    sy * (rBounds.y - rBounds.height);
	    }
	}

	if ((pflags & PrintContext.X_AXIS) != 0) {
	    sy = 0.;
	    dy = r.y + r.height / 2;
	}
	
	if ((pflags & PrintContext.Y_AXIS) != 0) {
	    sx = 0.;
	    dx = r.x + r.width / 2;
	}

	if ((pflags & PrintContext.Y_ANNOT) != 0) {
	    sx = 1.;
	    dx = r.x;
	}

	return new PrintContext(this, dx, dy, sx, sy, area,
				which, start, end, pflags);
    }	

    int getStart(LinkedList graphElements, int pagenum) {
	if (isVisibleY())
	    return 0;
	return graphElementCountPerPage * pagenum;
    }


    int getEnd(LinkedList graphElements, int start, int pagenum) {
	int size = graphElements.size();
	if (isVisibleY())
	    return size;

	int end = start + graphElementCountPerPage;
	return end;
    }

    boolean isVisibleX() {
	return (flags & WHOLE_X) == 0;
    }

    boolean isVisibleY() {
	return (flags & WHOLE_Y) == 0;
    }

    boolean inOnePage() {
	return (flags & THRU) != 0;
    }

    boolean isWholeX() {
	return (flags & WHOLE_X) != 0;
    }

    boolean isWholeY() {
	return (flags & WHOLE_Y) != 0;
    }

    View getView() {return view;}

    boolean needRedisplay() {
	return isVisibleX() || isVisibleY();
    }

    boolean showAreaBounds(boolean showAreaBounds) {
	boolean b = this.showAreaBounds;
	this.showAreaBounds = showAreaBounds;
	return b;
    }

    boolean showAreaNames(boolean showAreaNames) {
	boolean b = this.showAreaNames;
	this.showAreaNames = showAreaNames;
	return b;
    }

    void setEditMode(boolean editMode) {
	this.editMode = editMode;
	if (editMode) return;

	reset();
	int sz = area_v.size();
	for (int n = 0; n < sz; n++) {
	    if (area_v.get(n) instanceof PrintTextComponentArea)
		setEditTextMode_r((PrintTextComponentArea)area_v.get(n), false);
	}
    }

    void syncHTMLAreas() {
	int sz = area_v.size();
	for (int n = 0; n < sz; n++)
	    if (area_v.get(n) instanceof PrintHTMLArea)
		syncHTMLArea((PrintHTMLArea)area_v.get(n));
    }

    void syncHTMLArea(PrintHTMLArea area) {
	area.getPreviewArea().setText
	    (textElem.fromTemplate(area.getTemplate()));
	area.setInitialized(true);
    }

    void setEditTextMode_r(PrintTextComponentArea area,
			   boolean editTextMode) {
	if (editTextMode)
	    printTextEditor.pop(area);
    }

    void setEditTextMode(PrintTextComponentArea area, boolean editTextMode) {
	setEditTextMode_r(area, editTextMode);

	if (editTextMode)
	    pushFront(area);
    }

    boolean isEditMode() {return editMode;}

    void paintXScale(PrintArea area, Graphics2D g, GraphPanel panel,
		     LinkedList graphElements,
		     int pagenum) {
	if (graphElements.size() == 0) return;

	GraphCanvas canvas = panel.getCanvas();
	AxisDisplayer axis_dsp = panel.getDefaultAxisDisplayer();
	Rectangle2D.Double r = area.getArea();
	int start = getStart(graphElements, pagenum);
	int end = getEnd(graphElements, start, pagenum);

	int cnt = end - start;
	double h = getH(r, cnt);

	Rectangle clip = g.getClipBounds();
	    
	g.clipRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	    
	if (end > graphElements.size()) end = graphElements.size();
	for (int n = start; n < end; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);

	    PrintContext pctx = makePrintContext(canvas, graphElement, area,
						 h, n, start, end,
						 PrintContext.X_AXIS);
	    if (pctx == null) continue;

	    axis_dsp.displayXAxis(canvas, null, g, graphElement, n, pctx);
	}
	    
	g.setClip(clip.x, clip.y, clip.width, clip.height);
    }

    void paintGraphElements(PrintArea area, Graphics2D g,
			    GraphPanel panel, LinkedList graphElements,
			    int pagenum) {
	if (graphElements.size() == 0) return;

	GraphCanvas canvas = panel.getCanvas();
	//GraphElementDisplayer ds_dsp = panel.getDefaultGraphElementDisplayer();
	AxisDisplayer axis_dsp = panel.getDefaultAxisDisplayer();
	Rectangle2D.Double r = area.getArea();
	int start = getStart(graphElements, pagenum);
	int end = getEnd(graphElements, start, pagenum);

	int cnt = end - start;
	double h = getH(r, cnt);

	Rectangle clip = g.getClipBounds();
	    
	g.clipRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	PrintContext pctx;
	    
	if (end > graphElements.size()) end = graphElements.size();

	GraphElementDisplayer last_ds_dsp = null;

	for (int n = start; n < end; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);

	    pctx = makePrintContext(canvas, graphElement, area, h, n, start, end, 0);

	    if (pctx == null) {
		continue;
	    }

	    if (n == start) {
		canvas.paintMarksAndRegions(g, pctx);
	    }

	    GraphElementDisplayer ds_dsp = graphElement.getGraphElementDisplayer();
	    if (ds_dsp == null) {
		ds_dsp = panel.getDefaultGraphElementDisplayer();
	    }

	    // added 27/05/05
	    if (ds_dsp != last_ds_dsp) { // added 6/09/05
		ds_dsp.computeRCoords(canvas, true);
		last_ds_dsp = ds_dsp;
	    }

	    ds_dsp.display(canvas, g, graphElement, n, pctx);

	    axis_dsp.displayXAxis(canvas, null, g, graphElement, n, pctx);
	    axis_dsp.displayYAxis(canvas, null, g, graphElement, n, pctx);
	}
	    
	g.setClip(clip.x, clip.y, clip.width, clip.height);
    }

    void paintYScale(PrintArea area, Graphics2D g, GraphPanel panel,
		     LinkedList graphElements,
		     int pagenum) {
	if (graphElements.size() == 0) {
	    return;
	}

	GraphCanvas canvas = panel.getCanvas();
	AxisDisplayer axis_dsp = panel.getDefaultAxisDisplayer();
	Rectangle2D.Double r = area.getArea();
	int start = getStart(graphElements, pagenum);
	int end = getEnd(graphElements, start, pagenum);

	int cnt = end - start;
	double h = getH(r, cnt);

	Rectangle clip = g.getClipBounds();
	    
	if (STRICT_CLIP)
	    g.clipRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	else
	    g.clipRect(clip.x, (int)r.y, clip.width, (int)r.height);
	    
	if (end > graphElements.size()) end = graphElements.size();
	for (int n = start; n < end; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);

	    PrintContext pctx = makePrintContext(canvas,
						 graphElement, area,
						 h, n, start, end,
						 PrintContext.Y_AXIS);
	    if (pctx == null) continue;

	    axis_dsp.displayYAxis(canvas, null, g, graphElement, n, pctx);
	}
	    
	g.setClip(clip.x, clip.y, clip.width, clip.height);
    }

    void paintYAnnot(PrintArea area, Graphics2D g, GraphPanel panel,
		     LinkedList graphElements,
		     int pagenum) {
	if (graphElements.size() == 0) return;

	GraphCanvas canvas = panel.getCanvas();
	//AxisDisplayer axis_dsp = panel.getDefaultAxisDisplayer();
	AnnotDisplayer annot_dsp = StandardAnnotDisplayer.getInstance();
	Rectangle2D.Double r = area.getArea();
	int start = getStart(graphElements, pagenum);
	int end = getEnd(graphElements, start, pagenum);

	int cnt = end - start;
	double h = getH(r, cnt);

	Rectangle clip = g.getClipBounds();
	    
	if (STRICT_CLIP)
	    g.clipRect((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	else
	    g.clipRect(clip.x, (int)r.y, clip.width, (int)r.height);
	    
	if (end > graphElements.size()) end = graphElements.size();
	for (int n = start; n < end; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);

	    PrintContext pctx = makePrintContext(canvas,
						 graphElement, area,
						 h, n, start, end,
						 PrintContext.Y_ANNOT);
	    if (pctx == null) continue;

	    annot_dsp.displayAnnots(canvas, null, g, graphElement, n, pctx);
	}
	    
	g.setClip(clip.x, clip.y, clip.width, clip.height);
    }

    void paintMiniMap(PrintArea area, Graphics2D g) {
	MiniMapPanel miniMap = view.getInfoPanel().getMiniMap();
	if (miniMap == null) return;

	Chromosome chr = miniMap.getChromosome();
	if (chr == null) return;

	textElem.setPropertyValue(MinimapChrProp, chr.getName());
	textElem.setPropertyValue(MinimapResolProp, miniMap.getResolution());
	textElem.setPropertyValue(MinimapOrgProp, miniMap.getCytoband().getOrganism());

	Rectangle2D.Double r = area.getArea();
	g.translate(r.x, r.y);
	int wl = 75;
	int wloc = 10;
	Dimension size = new Dimension((int)r.width, (int)r.height);
	miniMap.paintMarksAndRegions(g, size, chr);

	miniMap.paintMiniMap(g, size,
			     Color.WHITE,
			     wl, (int)r.width-wl-wloc, chr, true);

	if (isVisibleY())
	    miniMap.paintLocation(g, 
				  new Dimension((int)wloc, (int)r.height),
				  chr, (int)(r.width-wloc), 0);
	g.translate(-r.x, -r.y);
    }

    void makeComponents(PrintPreviewerPanel panel) {
	int sz = area_v.size();
	for (int n = 0; n < sz; n++) {
	    if (area_v.get(n) instanceof PrintTextArea) {
		PrintTextArea area = (PrintTextArea)area_v.get(n);
		makeComponent(panel, area);
	    }
	    else if (area_v.get(n) instanceof PrintHTMLArea) {
		PrintHTMLArea area = (PrintHTMLArea)area_v.get(n);
		makeComponent(panel, area);
	    }
	}
    }

    static final int TEXT_PAD = 6;
    static final int TEXT_PAD2 = TEXT_PAD + TEXT_PAD;

    void makeComponent(PrintPreviewerPanel panel, PrintTextArea area) {
    }

    void makeComponent(PrintPreviewerPanel panel, PrintHTMLArea area) {
	Color bgColor = VAMPResources.getColor(VAMPResources.PRINT_PREVIEW_EDIT_BG);
	Rectangle2D.Double r = area.getArea();
	JEditorPane previewArea = new JEditorPane();
	previewArea.setEditable(false);
	previewArea.setContentType("text/html");
	previewArea.setEditorKit(new HTMLEditorKit());
	previewArea.setText("");

	previewArea.setLocation((int)r.x+TEXT_PAD, (int)r.y+TEXT_PAD);
	previewArea.setSize((int)r.width-TEXT_PAD2, (int)r.height-TEXT_PAD2);
	previewArea.setBackground(area.getBGColor());
	previewArea.setVisible(true);

	area.setComponents(previewArea);
    }

    PrintArea getArea(int x, int y) {
	int sz = area_v.size();
	for (int n = sz-1; n >= 0; n--) {
	    PrintArea area = (PrintArea)area_v.get(n);
	    Rectangle2D.Double r = area.getArea();
	    if (x >= r.x && x <= r.x+r.width &&
		y >= r.y && y <= r.y+r.height)
		return area;
	}
	return null;
    }

    private void addArea(PrintArea area) {
	area.setBGColor(Color.WHITE);
	reset();
	template.addArea(area);
	setSelected(area, true);
    }

    void addArea(PrintPreviewerPanel panel,
		 String name, int panel_num, Rectangle2D.Double area) {
	PrintArea parea = new PrintArea(name, panel_num, area);
	addArea(parea);
    }

    void addImageArea(PrintPreviewerPanel panel,
		 String name, Rectangle2D.Double area) {
	PrintImageArea parea = new PrintImageArea(name, area, null);
	addArea(parea);
    }

    void addTextArea(PrintPreviewerPanel panel,
		     String name, String templ, Rectangle2D.Double area) {
	PrintTextArea parea = new PrintTextArea(name, templ, area);

	addArea(parea);

	makeComponent(panel, parea);
    }

    void addHTMLArea(PrintPreviewerPanel panel,
		     String name, String templ, Rectangle2D.Double area) {
	PrintHTMLArea parea = new PrintHTMLArea(name, templ, area);

	addArea(parea);

	makeComponent(panel, parea);
    }

    void setSelected(PrintArea area, boolean selected) {
	if (area == null) return;
	area.setSelected(selected);
	if (selected)
	    pushFront(area);
    }

    void suppress(PrintArea area) {
	if (area instanceof PrintHTMLArea)
	    printPreviewerPanel.remove(((PrintHTMLArea)area).getPreviewArea());

	template.suppressArea(area);
	area_v.remove(area);
    }

    void suppressSelected(PrintPreviewerPanel panel) {
	int sz = area_v.size();

	for (int n = 0; n < sz; n++) {
	    PrintArea area = (PrintArea)area_v.get(n);
	    if (area.isSelected()) {
		suppress(area);
		break;
	    }
	}
    }

    void pushBack(PrintArea area) {
	area_v.remove(area);
	area_v.add(0, area);
    }

    void pushFront(PrintArea area) {
	area_v.remove(area);
	area_v.add(area);
    }

    void orderAreas(PrintArea except) {
	Vector areas = template.getAreas();
	int sz = areas.size();

	for (int n = 0; n < sz; n++) {
	    PrintArea area = (PrintArea)areas.get(n);
	    if (area != except)
		area_v.add(area);
	}
    }

    public PrintPageTemplate getPageTemplate() {return template;}

    void setPageTemplate(PrintPageTemplate template) {
	this.template = template;
    }

    int getGraphElementCountPerPage() {return graphElementCountPerPage;}

    double getH(Rectangle2D.Double r, int cnt) {
	if (rotated)
	    return (r.width - L_PADX - (cnt-1) * m_pad - R_PADX) / cnt;

	return (r.height - T_PADY - (cnt-1) * m_pad - B_PADY) / cnt;
    }

    boolean hasMPad() {
	return view.getPanel(0).getDefaultGraphElementDisplayer().getRPadY() != 0;
    }

    private LinkedList getGraphElements(int panel_num) {
	if (panel_num < graphElements_v.size())
	    return (LinkedList)graphElements_v.get(panel_num);
	return null;
    }

    private GraphPanel getPanel(int panel_num) {
	if (panel_num < panel_v.size())
	    return (GraphPanel)panel_v.get(panel_num);
	return null;
    }

    private void setTextElemProperties(int pagenum) {
	textElem.removeAllProperties();

	textElem.setPropertyValue(VAMPProperties.LoginProp, VAMPUtils.getLogin());

	textElem.setPropertyValue(TitleProp,
				  (view.getOrigName() != null ?
				   view.getOrigName() : view.getName()));
	textElem.setPropertyValue(PageCountProp,
				  (new Integer(this.pageCount)).toString());
	textElem.setPropertyValue(PrintDateProp,
				  dateFormat.format(currentDate));
	textElem.setPropertyValue(PrintDateTimeProp,
				  dateTimeFormat.format(currentDate));
	textElem.setPropertyValue(MinimapChrProp, "");
	textElem.setPropertyValue(MinimapResolProp, "");
	textElem.setPropertyValue(MinimapOrgProp, "");

	textElem.setPropertyValue(PageNumProp,
				  (new Integer(pagenum+1)).toString());

	TreeMap annot_map = new TreeMap();
	int sz = graphElements_v.size();
	for (int m = 0; m < sz; m++) {
	    GraphPanel panel = view.getGraphPanelSet().getPanel(m);
	    LinkedList graphElements = getGraphElements(m);
	    int start = getStart(graphElements, pagenum);
	    int end = getEnd(graphElements, start, pagenum);

	    int dsz = graphElements.size();
	    if (end > dsz) end = dsz;

	    for (int n = start; n < end; n++) {
		GraphElement graphElement = (GraphElement)graphElements.get(n);
		if (m == 0 && n == start) {
		    textElem.setPropertyValue(ProjectProp,
					      graphElement.getPropertyValue(VAMPProperties.ProjectProp));
		    textElem.setPropertyValue(TeamProp,
					      graphElement.getPropertyValue(VAMPProperties.TeamProp));
		}

		/*
		setTextElemProperties(graphElement,
				      (sz == 1 ? -1 : m),
				      panel.getPanelName(),
				      (end-start == 1 ? -1 : n-start+1),
				      annot_map);
		*/
		setTextElemProperties(graphElement,
				      m,
				      panel.getPanelName(),
				      n-start+1,
				      annot_map);
	    }

	    setTextElemAnnotProperties(annot_map);

	    for (int annot_ind = annot_map.size(); annot_ind < 20;
		 annot_ind++) {
		/*
		Property p = Property.getProperty
		    (makePropName(AnnotRGBProp,
				      (sz == 1 ? -1 : m),
				      panel.getPanelName(),
				  annot_ind+1));

		textElem.setPropertyValue(p, DEFAULT_TABLE_BACKGROUND);
		*/
		Property props[] = Property.getProperties
		    (makePropNames(AnnotRGBProp,
				   m,
				   panel.getPanelName(),
				   annot_ind+1));

		textElem.setPropertyValues(props, DEFAULT_TABLE_BACKGROUND);
	    }
	}

	//textElem.display();
    }

    private class AnnotGE {
	PropertyAnnot annot;
	GraphElement graphElem;
	int m;
	String panel_name;

	AnnotGE(PropertyAnnot annot, GraphElement graphElem, int m,
		String panel_name) {
	    this.annot = annot;
	    this.graphElem = graphElem;
	    this.m = m;
	    this.panel_name = panel_name;
	}
    }

    /*
    static String makePropName(Property prop, int m, String panel_name,
			       int n) {
	String propName = prop.getName();
	if (m != -1)
	    propName += "[" + panel_name + "]";
	if (n != -1)
	    propName += "[" + n + "]";
	return propName;
    }
    */

    static String[] makePropNames(Property prop, int m, String panel_name,
				  int n) {
	int cnt = 1;
	if (panel_name.length() > 0)
	    cnt++;
	if (m == 0 && n == 1)
	    cnt++;

	String propNames[] = new String[cnt];
	String propName = prop.getName();

	int i = 0;
	propNames[i] = propName;
	propNames[i] += "[" + m + "]";
	propNames[i] += "[" + n + "]";
	i++;

	if (panel_name.length() > 0) {
	    propNames[i] = propName;
	    propNames[i] += "[" + panel_name + "]";
	    propNames[i] += "[" + n + "]";
	    i++;
	}
	
	if (m == 0 && n == 1) {
	    propNames[i] = propName;
	    i++;
	}

	return propNames;
    }

    private void setTextElemProperties(GraphElement graphElem, int m, 
				       String panel_name, int n,
				       TreeMap annot_map) {
	TreeMap properties = graphElem.getProperties();
	Iterator it = properties.entrySet().iterator();

	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    if (!prop.isInfoable()) {
		continue;
	    }

	    if (entry.getValue() == null) {
		continue;
	    }

	    String propNames[] = makePropNames(prop, m, panel_name, n);
	    textElem.setPropertyValues(Property.getProperties(propNames),
				       entry.getValue().toString());
	    
	    setTextElemAnnotProperties(graphElem, prop, m, panel_name, n,
				       annot_map);
	}


	setTextElemCCProperties(graphElem, m, panel_name, n);
    }

    private void setTextElemAnnotProperties(GraphElement graphElem,
					    Property prop, int m, 
					    String panel_name, int n,
					    TreeMap annot_map) {
	Property filter_prop = view.getAnnotDisplayFilterProp();

	if (!prop.isEligible(view, filter_prop))
	    return;

	PropertyAnnot annot = prop.getPropertyAnnot(view,
						    graphElem);
	if (annot == null)
	    return;

	String tag = prop.getName() + 
	    PropertyElement.getStringOP(annot.getOP()) +
	    annot.getValue();
	
	if (annot_map.get(tag) != null)
	    return;

	annot_map.put(tag, new AnnotGE(annot, graphElem, m, panel_name));
    }

    private void setTextElemAnnotProperties(TreeMap annot_map) {

	Iterator it = annot_map.entrySet().iterator();
	TreeMap tree = new TreeMap();
	int annot_ind = 1;
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    String tag = (String)entry.getKey();
	    AnnotGE age = (AnnotGE)entry.getValue();
	    Property props[];
	    int m = age.m;
	    String panel_name = age.panel_name;
	    GraphElement graphElem = age.graphElem;
	    PropertyAnnot annot = age.annot;

	    props = Property.getProperties
		(makePropNames(AnnotRGBProp, m, panel_name, annot_ind));
			
	    textElem.setPropertyValues(props,
				      toRGB(annot.getColor(graphElem).getRGB()));
	    // ---
	    props = Property.getProperties
		(makePropNames(AnnotNameProp, m, panel_name, annot_ind));
			
	    textElem.setPropertyValues(props, annot.getProperty().getName());
	    // ---
	    props = Property.getProperties(makePropNames(AnnotOPProp, m, panel_name, annot_ind));
			
	    textElem.setPropertyValues(props,  PropertyElement.getStringOP(annot.getOP()));
	    // ---
	    props = Property.getProperties(makePropNames(AnnotValueProp, m, panel_name, annot_ind));
	    
	    textElem.setPropertyValues(props, annot.getValue());
	    
	    annot_ind++;
	}
    }

    static String toRGB(int rgb) {
	return Integer.toHexString(rgb & 0xffffff);
    }

    static String toRatio(double ratio) {
	ratio = ((int)Math.round(ratio*100))/100.;
	return Utils.toString(ratio);
    }

    private void setTextElemCCProperties(GraphElement graphElem, int m, 
					 String panel_name, int n) {
	ColorCodes cc = VAMPUtils.getColorCodes(graphElem);
	if (!(cc instanceof StandardColorCodes))
	    return;

	StandardColorCodes scc =(StandardColorCodes)cc;
	/*
	String propName;

	propName = makePropName(CCMinRGBProp, m, panel_name, n);
	textElem.setPropertyValue(Property.getProperty(propName),
				  toRGB(scc.getMinRGB()));
	*/

	String propNames[];
	propNames = makePropNames(CCMinRGBProp, m, panel_name, n);
	textElem.setPropertyValues(Property.getProperties(propNames),
				   toRGB(scc.getMinRGB()));
	propNames = makePropNames(CCMaxRGBProp, m, panel_name, n);
	textElem.setPropertyValues(Property.getProperties(propNames),
				  toRGB(scc.getMaxRGB()));

	propNames = makePropNames(CCNormalRGBProp, m, panel_name, n);
	textElem.setPropertyValues(Property.getProperties(propNames),
				  toRGB(scc.getNormalRGB()));

	propNames = makePropNames(CCAmpliconRGBProp, m, panel_name, n);
	textElem.setPropertyValues(Property.getProperties(propNames),
				  toRGB(scc.getAmpliconRGB()));

	propNames = makePropNames(CCMinProp, m, panel_name, n);
	textElem.setPropertyValues(Property.getProperties(propNames),
				  toRatio(scc.getMin()));

	propNames = makePropNames(CCNormalMinProp, m, panel_name, n);
	textElem.setPropertyValues(Property.getProperties(propNames),
				  toRatio(scc.getNormalMin()));

	propNames = makePropNames(CCNormalMaxProp, m, panel_name, n);
	textElem.setPropertyValues(Property.getProperties(propNames),
				  toRatio(scc.getNormalMax()));

	propNames = makePropNames(CCMaxProp, m, panel_name, n);
	textElem.setPropertyValues(Property.getProperties(propNames),
				  toRatio(scc.getMax()));

	propNames = makePropNames(CCAmpliconProp, m, panel_name, n);
	textElem.setPropertyValues(Property.getProperties(propNames),
				  toRatio(scc.getAmplicon()));
    }

    private int getMaxSize() {
	int size = 0;
	int sz = graphElements_v.size();
	for (int m = 0; m < sz; m++) {
	    LinkedList graphElements = getGraphElements(m);
	    if (graphElements.size() > size)
		size = graphElements.size();
	}

	return size;
    }

    PrintPageTemplate getTemplate() {return template;}

    void close() {
	setEditMode(false);
	int sz = area_v.size();

	for (int n = 0; n < sz; n++) {
	    if (area_v.get(n) instanceof PrintHTMLArea)
		((PrintHTMLArea)area_v.get(n)).setInitialized(false);
	}
    }

    void setPrintPreviewerPanel(PrintPreviewerPanel panel) {
	printPreviewerPanel = panel;
	printTextEditor = new PrintTextEditor(panel);
    }

    void reinit(int flags, int pageCount,
		int graphElementCountPerPage,
		PrintPageTemplate template) {
	if (previewer != null) {
	    init(flags, pageCount, graphElementCountPerPage,
		 template);
	    previewer.display();
	}
    }

    public int getPrintFlags() {return flags;}
}



