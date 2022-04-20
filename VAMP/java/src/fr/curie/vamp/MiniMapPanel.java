/*
 *
 * InfoPanel.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

class MiniMapPanel extends JPanel {

    private static final int PADY = 10;
    //private MiniMapDataFactory dataFact;
    //private String resolution;
    //private Cytoband cytoband;

    private HashMap resolution_map;
    private String organism;
    private String resolution;
    private Cytoband cytoband;

    private GlobalContext globalContext;
    private SystemConfig syscfg;

    private TopCanvas topCanvas;
    private MiniMapCanvas miniMapCanvas;
    private LocationCanvas locationCanvas;
    private JComboBox resolCB;
    private String chrName, arrayName, cloneName;
    private long begin = 0, end = 0;
    private long pos_x = 0;
    private Color bgColor = VAMPResources.getColor(VAMPResources.MINIMAP_BG);
    private View view;
    private PropertyElementMenu chrMenu;
    private PropertyElementMenu bandMenu;
    private static final int TOPCANVAS_HEIGHT = 35;
    private static final double LABEL_CANVAS_PERCENT = 0.40;
    private static final double MINIMAP_CANVAS_PERCENT = 0.18;
    private static final double LOCATION_CANVAS_PERCENT = 0.42;
    private static final double LEFT_CANVAS_PERCENT = LABEL_CANVAS_PERCENT +
	MINIMAP_CANVAS_PERCENT;
    private double pos_offset = 0;

    MiniMapPanel(View view) {
	setBackground(bgColor);

	this.globalContext = view.getGlobalContext();
	syscfg = (SystemConfig)globalContext.get(SystemConfig.SYSTEM_CONFIG);

	if (syscfg != null) {
	    chrMenu = syscfg.getMenu("MiniMapChr");
	    bandMenu = syscfg.getMenu("MiniMapBand");

	}
	this.view = view;
	chrName = arrayName = cloneName = "";

	//cytoband = null;
	organism = "";
	//datafact_map = new HashMap();
	resolution_map = new HashMap();
	resolution = null;

	topCanvas = new TopCanvas();
	miniMapCanvas = new MiniMapCanvas();
	locationCanvas = new LocationCanvas();

	topCanvas.setBackground(bgColor);
	miniMapCanvas.setBackground(bgColor);
	locationCanvas.setBackground(bgColor);

	setLayout(null);
	add(topCanvas);
	add(miniMapCanvas);
	add(locationCanvas);

	/*
	resolCB = new JComboBox(dataFact.getSupportedResolutions());
	*/
	resolCB = new JComboBox();
	resolCB.setBackground(Color.WHITE);
	resolCB.setFont(VAMPResources.getFont
			(VAMPResources.MINIMAP_TITLE_FONT));
	topCanvas.add(resolCB);

	resolCB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String newResolution = (String)((JComboBox)e.getSource()).getSelectedItem();
		    if (newResolution == null) return;

		    /*if (resolution == null ||
		      !resolution.equals(newResolution)) */ {
			resolution = newResolution;
			resolution_map.put(organism, newResolution);
			MiniMapDataFactory datafact = getDataFact();
			if (datafact != null) {
			    cytoband = datafact.getData(newResolution);
			    repaint();
			}
		    }
		}
	    });
	//resolution = dataFact.getSupportedResolutions()[0];
	//cytoband = dataFact.getData(resolution);
	addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    Dimension size =  ((Component)e.getSource()).getSize();
		    topCanvas.setLocation(0, 0);
		    miniMapCanvas.setLocation(0, TOPCANVAS_HEIGHT);
		    int left_width = (int)(size.width * LEFT_CANVAS_PERCENT);
		    int location_width = (int)(size.width * LOCATION_CANVAS_PERCENT);
		    locationCanvas.setLocation(left_width,
					       TOPCANVAS_HEIGHT);
		    topCanvas.setSize(new Dimension(size.width,
						    TOPCANVAS_HEIGHT));
		    miniMapCanvas.setSize(new Dimension(left_width,
							size.height -
							TOPCANVAS_HEIGHT));
		    locationCanvas.setSize(new Dimension(location_width,
							 size.height -
							 TOPCANVAS_HEIGHT));
		    repaint();
		}
		public void componentShown(ComponentEvent e) {
		    componentResized(e);
		}
	    });
    }

    static private double getPosX(int ry, double coef) {
	return (ry - PADY) / coef;
    }

    static private double getRY(double pos_x, double coef) {
	return (pos_x * coef + PADY);
    }

    static private int getRH(double pos_w, double coef) {
	return (int)(pos_w * coef);
    }

    static private int getLineW(int n) {
	return (n & 1) == 0 ? 10 : 40;
    }

    HashMap getDataFactMap() {
	return getDataFactMap(globalContext);
    }

    static HashMap getDataFactMap(GlobalContext globalContext) {
	return MiniMapDataFactory.getDataFactMap(globalContext);
    }

    MiniMapDataFactory getDataFact() {
	//return (MiniMapDataFactory)getDataFactMap().get(organism);
	return getDataFact(globalContext, organism);
    }

    static MiniMapDataFactory getDataFact(GlobalContext globalContext,
					  String organism) {
	return MiniMapDataFactory.getDataFact(globalContext, organism);
	/*
	MiniMapDataFactory datafact =
	    (MiniMapDataFactory)getDataFactMap(globalContext).get(organism);
	if (datafact == null) {
	    SystemConfig syscfg =
		(SystemConfig)globalContext.get(SystemConfig.SYSTEM_CONFIG);
	    datafact = new XMLMiniMapDataFactory
		(globalContext,
		 organism,
		 syscfg.getCytobandURL(organism),
		 syscfg.getCytobandResolutions(organism),
		 syscfg.getCytobandDefaultResolution(organism));
	}
	return datafact;
	*/
    }

    void unsetPosX() {
	//if (dataFact == null) return;
	if (this.pos_x != 0) {
	    this.pos_x = 0;
	    if (VAMPResources.getBool(VAMPResources.MINIMAP_HIDE)) {
		System.out.println("locationCanvas.repaint()");
		return;
	    }
	    locationCanvas.repaint();
	}
    }


    void setPosX(long begin, long end, long pos_x) {
	//if (dataFact == null) return;

	begin -= pos_offset;
	end -= pos_offset;
	pos_x -= pos_offset;

	long obegin = this.begin;
	long oend = this.end;
	long opos_x = this.pos_x;

	this.begin = begin;
	this.end = end;
	this.pos_x = pos_x;

	/*
	System.out.println("setPosX(posx=" + opos_x + " vs. " + pos_x +
			   ", begin=" + obegin + " vs. " + begin + ", end=" +
			   oend + " vs." + end + ")");
	*/
	if (opos_x != pos_x || obegin != begin || oend != end)
	    locationCanvas.repaint();
    }

    private void setMiniMap(GraphElement graphElement) {
	if (graphElement == null) return;
	String os = VAMPUtils.getOS(graphElement);

	if (os == null) return;

	if (os.equals(organism)) return;

	this.organism = os;
	MiniMapDataFactory datafact = getDataFact();
	    /*
	if (datafact == null) {
	    datafact = new XMLMiniMapDataFactory(globalContext,
						 syscfg.getCytobandURL(os),
						 syscfg.getCytobandResolutions(os),
						 syscfg.getCytobandDefaultResolution(organism));
	    //getDataFactMap().put(os, datafact);
	}
	    */
	
	String resol = (String)resolution_map.get(organism);
	if (resol == null)
	    resol = datafact.getDefaultResolution();
	    //resolution_map.put(organism, resolution);

	cytoband = datafact.getData(resol);
	resolCB.removeAllItems();
	String resolutions[] = datafact.getSupportedResolutions();
	int selected = 0;
	for (int n = 0; n < resolutions.length; n++) {
	    resolCB.addItem(resolutions[n]);
	}

	resolCB.setSelectedItem(resol);
    }

    void setInfo(GraphPanel panel, GraphElement graphElem, PropertyElement elem,
		 DataElementRange range, double vx) {
	setMiniMap(graphElem);
	String ochrName = this.chrName;
	String oarrayName = this.arrayName;
	String ocloneName = this.cloneName;
	pos_offset = 0;

	String chrName = null;
	if (graphElem != null) {
	    if (VAMPUtils.isMergeChr(graphElem)) {
		if (cytoband != null && useCytoband(panel, graphElem)) {
		    String nchrName = 
			cytoband.getChromosome((long)vx).getName();
		    chrName = nchrName;
		    pos_offset = cytoband.getChromosome((long)vx).getOffsetPos();
		}
		else {
		    DataSet set = (graphElem == null ? null : graphElem.asDataSet());
		    if (set != null) {
			if (elem == null &&
			    range.getIndMin() != DataElementRange.INVALID_IND)
			    elem = set.getData()[range.getIndMin()];
			if (elem == null)
			    return;
			chrName = VAMPUtils.getChr(elem);
			Double offset = (Double)elem.getPropertyValue
			    (VAMPProperties.MergeOffsetProp);
			if (offset != null)
			    pos_offset = offset.doubleValue();
		    }		
		}
	    }
	    else if (elem != null)
		chrName = VAMPUtils.getChr(elem);
	    else
		chrName = VAMPUtils.getChr(graphElem);
	
	    if (chrName != null)
		this.chrName = chrName;
	    String arrayName = (String)graphElem.getID();
	    if (arrayName != null)
		this.arrayName = arrayName;
	}

	if (elem != null) {
	    String cloneName = (String)elem.getID();
	    if (cloneName != null)
		this.cloneName = cloneName;
	}
	else
	    this.cloneName = "";
	
	boolean hide = VAMPResources.getBool(VAMPResources.MINIMAP_HIDE);
	if (!ocloneName.equals(this.cloneName)) {
	    if (hide) {
		System.out.println("locationCanvas.repaint()");
		return;
	    }
	    locationCanvas.repaint();
	}

	if (!ochrName.equals(this.chrName)) {
	    if (hide) {
		System.out.println("top + miniMap.repaint()");
		return;
	    }
	    topCanvas.repaint();
	    miniMapCanvas.forceRepaint = true;
	    miniMapCanvas.repaint();
	}

	if (!oarrayName.equals(this.arrayName)) {
	    if (hide) {
		System.out.println("topCanvas.repaint()");
		return;
	    }
	    topCanvas.repaint();
	}

    }

    class TopCanvas extends JPanel {

	public void paint(Graphics g) {
	    Dimension size = getSize();
	    g.setColor(bgColor);
	    g.fillRect(0, 0, size.width, size.height);
	    if (cytoband == null) return;

	    resolCB.setLocation(5, 5);
	    resolCB.repaint();

	    int padw = 70;
	    if (chrName.length() > 0) {
		g.setColor(VAMPResources.getColor
			   (VAMPResources.MINIMAP_TITLE_FG));
		g.setFont(VAMPResources.getFont
			  (VAMPResources.MINIMAP_TITLE_FONT));
		String s = "Chr " + chrName + " / " + cytoband.getOrganism() +
		    " / " + (new Integer(resolution)).toString();
		Dimension sz = Utils.getSize((Graphics2D)g, s);
		g.drawString(s, padw, 15);
		s = arrayName;
		Dimension sz2 = Utils.getSize((Graphics2D)g, s);
		g.drawString(s, padw, 15+sz.height+2);
	    }
	}
    }

    static double computeCoef(Dimension size, Band bands[]) {
	double end = bands[bands.length-1].getEnd();
	double rheight = size.height - getPadH();
	return rheight/end;
    }

    class MiniMapCanvas extends JPanel {

	boolean forceRepaint = false;

	MiniMapCanvas() {
	    addMouseListener(new MouseAdapter() {
		    public void mouseReleased(MouseEvent e) {
			if (cytoband == null) return;
			if (e.getButton() != MouseEvent.BUTTON1 &&
			    e.getButton() != MouseEvent.BUTTON3)
			    return;

			Dimension size = ((MiniMapCanvas)e.getSource()).getSize();
			Dimension tsize = getTotalSize();
			int miniMapWidth = (int)(tsize.width * MINIMAP_CANVAS_PERCENT);
			int leftmapx = size.width - miniMapWidth;
			int rx = e.getX();
			int ry = e.getY();
			Chromosome chr = cytoband.getChromosome(chrName);
			Band bands[] = chr.getBands();
			double coef = computeCoef(size, bands);
			double cur_pos_x = getPosX(e.getY(), coef);
			for (int n = 0; n < bands.length; n++) {
			    Band band = bands[n];
			    if (cur_pos_x  >= band.getBegin() &&
				cur_pos_x <= band.getEnd()) {
				if (e.getButton() == MouseEvent.BUTTON3) {
				    if (rx >= leftmapx) {
					makePopup(bandMenu, band, e);
					return;
				    }
				    break;
				}

				if (rx >= leftmapx - getLineW(n) - 1 &&
				    rx < leftmapx) {
				    double vby = band.getBegin() +
					(band.getEnd()-band.getBegin())/2;
				    double ryb = getRY(vby, coef);

				    if (ry >= ryb-2 && ry <= ryb+2) {
					cur_pos_x = vby;
				    }
				    else
					return;
				}
				else if (rx < leftmapx)
				    return;

				cur_pos_x = 
				    view.getGraphPanelSet().getPanel(0).getChrPosX
				    (organism, chrName, cur_pos_x);
				view.getGraphPanelSet().getPanel(0).setPosX
				    (cur_pos_x, true);
				break;
			    }
			}

			if (e.getButton() == MouseEvent.BUTTON3)
			    makePopup(chrMenu, chr, e);
		    }
		});
	}

	private void makePopup(PropertyElementMenu menu, PropertyElement elem,
			       MouseEvent e) {
	    if (menu == null) return;
	    JPopupMenu popup = new JPopupMenu();
	    menu.buildJPopupMenu(view, popup, elem);
	    popup.show(e.getComponent(), e.getX(), e.getY());
	}

	public void paint(Graphics g) {
	    Dimension size = getSize();
	    g.setColor(bgColor);
	    g.fillRect(0, 0, size.width, size.height);
	    if (cytoband == null) return;

	    Dimension tsize = getTotalSize();
	    Chromosome chr = cytoband.getChromosome(chrName);
	    if (chr == null)
		return;
	    int miniMapWidth = (int)(tsize.width * MINIMAP_CANVAS_PERCENT);
	    int miniMapX = size.width - miniMapWidth;
	    paintMarksAndRegions(g, size, chr);
	    paintMiniMap(g, size, bgColor, miniMapX,
			 miniMapWidth, chr, true);

	    view.repaintPrintPreviewer(forceRepaint);
	    forceRepaint = false;
	}
    }

    class LocationCanvas extends JPanel {

	public void paint(Graphics g) {
	    Dimension size = getSize();
	    g.setColor(bgColor);
	    g.fillRect(0, 0, size.width, size.height);
	    if (cytoband == null) return;

	    if (VAMPResources.getBool(VAMPResources.MINIMAP_HIDE)) {
		System.out.println("locationCanvas.repaint() " + size);
		return;
	    }

	    Chromosome chr = cytoband.getChromosome(chrName);
	    if (chr == null)
		return;
	    paintLocation(g, size, chr, 0, 0);
	    if (chr.getBands().length == 0) return;
	}
    }

    void paintString(Graphics g, String s0, Dimension size) {
	int len = s0.length();
	Dimension sz0 = null;

	int padw = 6;
	int x0 = padw;
	int y = 15;
	int offset = 0;
	size.width -= padw;

	String s = s0;
	for (;;) {
	    Dimension sz = Utils.getSize((Graphics2D)g, s);
	    if (sz.width < size.width) {
		if (offset == 0) {
		    x0 += (size.width-sz.width)/2;
		    sz0 = sz;
		    g.drawString(s, x0, y);
		}
		else
		    g.drawString(s, x0+sz0.width-sz.width, y);
		
		y += sz.height + 2;
		offset += s.length();
		if (offset == len)
		    break;
		s = s0.substring(offset, s0.length());
		continue;
	    }
	    s = s.substring(0, s.length()-1);
	}
    }

    private Dimension getTotalSize() {
	return getSize();
    }

    static void paintMiniMap(Graphics g, Dimension size,
			     Color bgColor, int miniMapX,
			     int miniMapWidth, Chromosome chr,
			     boolean drawLabels) {
	Band bands[] = chr.getBands();
	if (bands.length == 0) return;
	double coef = computeCoef(size, bands);
	Color labelFG = VAMPResources.getColor
	    (VAMPResources.MINIMAP_LABEL_FG);
	g.setFont(VAMPResources.getFont
		  (VAMPResources.MINIMAP_LABEL_FONT));
	for (int n = 0; n < bands.length; n++) {
	    Band band = bands[n];
	    int bandy = (int)getRY(band.getBegin(), coef);
	    int bandh = getRH(band.getEnd()-band.getBegin(), coef);
	    int bandh2 = bandh/2;
	    int bandh3 = bandh/3;
	    int bandh2_3 = (2*bandh)/3;
	    g.setColor(labelFG);
	    int angle = 180;
	    switch(band.getCode()) {
	    case 1:
		if (band.getColor().equals(bgColor)) {
		    g.drawArc(miniMapX, bandy,
			      miniMapWidth-1, bandh,
			      0, angle);
		    g.drawLine(miniMapX, bandy+bandh,
			       miniMapX+miniMapWidth-1, bandy+bandh);
		    g.drawLine(miniMapX, bandy+bandh2,
			       miniMapX, bandy+bandh);
		    g.drawLine(miniMapX+miniMapWidth-1, bandy+bandh2,
			       miniMapX+miniMapWidth-1, bandy+bandh);
		}

		else {
		    g.setColor(band.getColor());
		    g.fillArc(miniMapX, bandy, miniMapWidth, bandh,
			      0, angle);
		    g.fillRect(miniMapX, bandy+bandh2,
			       miniMapWidth, bandh2);
		}
		break;

	    case 2:
		if (band.getColor().equals(bgColor)) {
		    g.drawLine(miniMapX, bandy,
			       miniMapX+miniMapWidth-1, bandy);
		    g.drawLine(miniMapX, bandy,
			       miniMapX, bandy+bandh2);
		    g.drawLine(miniMapX+miniMapWidth-1, bandy,
			       miniMapX+miniMapWidth-1, bandy+bandh2);
		    g.drawArc(miniMapX, bandy,
			      miniMapWidth-1, bandh, 0, -angle);
		}
		else {
		    g.setColor(band.getColor());
		    g.fillArc(miniMapX, bandy,
			      miniMapWidth, bandh, 0, -angle);
		    g.fillRect(miniMapX, bandy,
			       miniMapWidth, bandh2+1);
		}
		break;
	    case 3:
		if (band.getColor().equals(bgColor))
		    g.drawArc(miniMapX, bandy, miniMapWidth-1, bandh, 0, 360);
		else {
		    g.setColor(band.getColor());
		    g.fillArc(miniMapX+1, bandy+1, miniMapWidth-2, bandh-1,
			      0, 360);
		}
		break;
	    case 4:
		g.setColor(band.getColor());
		g.drawLine(miniMapX+1, bandy+bandh3,
			   miniMapX+miniMapWidth-1, bandy+bandh3);
		g.drawLine(miniMapX+1, bandy+bandh2_3,
			   miniMapX+miniMapWidth-1, bandy+bandh2_3);
		break;
	    case 5:
		g.drawRect(miniMapX, bandy, miniMapWidth-1, bandh);
		g.setColor(band.getColor());
		g.fillRect(miniMapX+1, bandy+1, miniMapWidth-2, bandh-1);
		break;
	    default :
		System.err.println("CODE " + band.getCode() + " does not exist!");
	    }

	    if (drawLabels) {
		g.setColor(labelFG);
		int linew = getLineW(n);
		int liney = bandy + bandh/2;
		g.drawLine(miniMapX-linew, liney, miniMapX, liney);
		String name = band.getArm() + band.getName();
		Dimension bandsz = Utils.getSize((Graphics2D)g, name);
		g.drawString(name, miniMapX-linew-bandsz.width-5,
			     liney+bandsz.height/2-1);
	    }
	}
    }

    public void paintMarksAndRegions(Graphics g, Dimension size,
				     Chromosome chr) {
	GraphCanvas canvas = view.getGraphPanelSet().getPanel(0).getCanvas();
	Band bands[] = chr.getBands();
	if (bands.length == 0) return;
	double coef = computeCoef(size, bands);

	long b_begin = bands[0].getBegin();
	long b_end = bands[bands.length-1].getEnd();

	LinkedList regions = canvas.getRegions();
	for (int i = regions.size() - 1; i >= 0; i--) {
	    Region region = (Region)regions.get(i);
	    double vbegin = region.getBegin().getPosX() - pos_offset;
	    double vend = region.getEnd().getPosX() - pos_offset;

	    if (vend < b_begin || vbegin > b_end)
		continue;

	    if (vbegin < b_begin)
		vbegin = b_begin;
		
	    if (vend > b_end)
		vend = b_end;

	    int rbegy = (int)getRY(vbegin, coef);
	    int rendy = (int)getRY(vend, coef);
	    g.setColor(region.getColor());

	    int height = rendy-rbegy;
	    g.fillRect(0, rbegy, size.width, height);
	}

	LinkedList marks = canvas.getMarks();
	for (int i = marks.size() - 1; i >= 0; i--) {
	    Mark mark = (Mark)marks.get(i);
	    double vy = mark.getPosX() - pos_offset;
	    if (vy < b_begin || vy > b_end)
		continue;

	    int ry = (int)getRY(vy, coef);
	    g.setColor(mark.getColor());

	    g.fillRect(0, ry-1, size.width, 2);
	}
    }

    void paintLocation(Graphics g, Dimension size, Chromosome chr,
		       int offset_x, int offset_y) {
	Band bands[] = chr.getBands();
	if (bands.length == 0) return;
	double coef = computeCoef(size, bands);

	long b_begin = bands[0].getBegin();
	long b_end = bands[bands.length-1].getEnd();
	if (begin < b_begin)
	    begin = b_begin;
	if (end > b_end)
	    end = b_end;
	if (begin > end)
	    return;

	double rbegin = getRY(begin, coef);
	double rend = getRY(end, coef);
	double rpos_x = getRY(pos_x, coef);

	if (pos_x <= 0 || (long)rpos_x < (long)rbegin ||
	    (long)rpos_x > (long)rend)
	    rpos_x = 0;
	Color labelFG = VAMPResources.getColor
	    (VAMPResources.MINIMAP_LABEL_FG);
	Color locationFG = VAMPResources.getColor
	    (VAMPResources.MINIMAP_LOCATION_FG);
	g.setColor(locationFG);
	int linew = VAMPResources.getInt(VAMPResources.MINIMAP_LOCATION_WIDTH);
	g.setFont(VAMPResources.getFont
		  (VAMPResources.MINIMAP_LABEL_FONT));

	g.drawLine(offset_x, (int)rbegin + offset_y,
		   linew + offset_x, (int)rbegin + offset_y);
	g.drawLine(linew + offset_x, (int)rbegin + offset_y,
		   linew + offset_x, (int)rend + offset_y);
	g.drawLine(offset_x, (int)rend + offset_y,
		   linew + offset_x, (int)rend + offset_y);

	if (rpos_x > 0) {
	    Dimension sz;
	    int offset = 0;
	    g.setColor(labelFG);
	    if (cloneName.length() > 0) {
		sz = Utils.getSize((Graphics2D)g, cloneName);
		g.drawString(cloneName, 2*linew+3 + offset_x,
			     (int)rpos_x + sz.height/2 + offset_y);
		offset = sz.height;
	    }
	    String s = (new Long((long)(pos_x))).toString();
	    sz = Utils.getSize((Graphics2D)g, s);
	    g.drawString(s, 2*linew+3 + offset_x,
			 offset + (int)rpos_x + sz.height/2 + offset_y);
	    g.setColor(locationFG);
	    g.drawLine(linew + offset_x, (int)rpos_x + offset_y,
		       2*linew + offset_x, (int)rpos_x + offset_y);
	}
    }

    void sync() {
	topCanvas.repaint();
	miniMapCanvas.repaint();
	locationCanvas.repaint();
    }

    static int getPadY() {return PADY;}
    static int getPadH() {return 2*getPadY();}

    public Chromosome getChromosome() {
	if (cytoband == null) return null;
	if (chrName.length() > 0)
	    return cytoband.getChromosome(chrName);
	return null;
    }

    public String getResolution() {return resolution;}
    public Cytoband getCytoband() {return cytoband;}

    private boolean useCytoband(GraphPanel panel, GraphElement graphElem) {
	AxisDisplayer axisDisplayer = graphElem.getAxisDisplayer();
	if (axisDisplayer == null)
	    axisDisplayer = panel.getDefaultAxisDisplayer();

	if (axisDisplayer instanceof ChromosomeNameAxisDisplayer)
	    return ((ChromosomeNameAxisDisplayer)axisDisplayer).useCytoband(graphElem);
	return true;
    }
}
