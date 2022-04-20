
/*
 *
 * GraphPanel.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

public class GraphPanel extends JPanel implements AdjustmentListener {

    private final int MAX_DATA_LENGTH_SCROLLING_WHILE_ADJUSTING = 10000;
    //private final int MAX_DATA_LENGTH_SCROLLING_WHILE_ADJUSTING = 50000;
    private final int MAX_SIZE_SCROLLING_WHILE_ADJUSTING = 80;

    // axis
    private Axis _northX, _southX;
    private Axis _westY, _eastY;
    private Axis northX, southX;
    private Axis westY, eastY;

    private int eastY_width;

    private JPanel northWestPadPanel;
    private JPanel southWestPadPanel;
    private AnnotLegendPanel northEastPadPanel;
    private AnnotLegendPanel southEastPadPanel;

    // drawing canvas
    private GraphCanvas drawCanvas;

    // scrollbars
    private JScrollBar _scrollH, _scrollV;
    private JScrollBar scrollH, scrollV;
    private boolean centering = false;

    private Dimension2DDouble vSize = new Dimension2DDouble();
    private LinkedList linkedPaneX = new LinkedList();
    private LinkedList linkedPaneY = new LinkedList();

    private View view;
    private boolean adapting = false;
    private boolean adjusting = false;

    private GraphElementDisplayer defaultGraphElementDisplayer;
    private AxisDisplayer defaultAxisDisplayer;
    private GraphElementIDBuilder graphElementIDBuilder;

    private boolean internalReadOnly = false;

    static final int SYNCHRO_X = 1;
    static final int SYNCHRO_Y = 2;

    private int which;

    private int axisSizes[];
    // constants
    private static final int SCROLL_MIN = 0;
    private static final int SCROLL_MAX = 1000;
    private boolean supportX;
    private PanelProfile panelProfile;
    private GraphElementListOperation autoApplyDSLOP = null;

    private static void _setSize(JComponent comp, int w, int h) {
	comp.setPreferredSize(new Dimension(w, h));
    }

    private boolean highlightMinimalRegions = false;
    private boolean highlightRecurrentAlterations = false;
    private JSplitPane jpane;
    private JPanel e_eastPane, w_eastPane;
    private JSplitPane eastPane;

    private JSplitPane root_splitPane;

    static final int NORTH_X = 0;
    static final int SOUTH_X = 1;
    static final int WEST_Y = 2;
    static final int EAST_Y = 3;

    GraphPanel(View view, int which,
	       boolean supportX, PanelProfile _panelProfile) {
	GridBagConstraints c;
	this.view = view;
	this.which = which;
	this.supportX = supportX;
	this.panelProfile = _panelProfile;
	setAutoApplyDSLOP(panelProfile.getAutoApplyDSLOP());
	this.axisSizes = panelProfile.getAxisSizes();
	int scrollMask = panelProfile.getScrollMask();
	JPanel pad;

	setLayout(new GridBagLayout());
	setBackground(VAMPResources.getColor(VAMPResources.AXIS_BG));

	//Margins margins = new Margins(30, 40, 30, 40);
	Margins margins = panelProfile.getMargins();
	setDefaultDisplayers(panelProfile.getDefaultGraphElementDisplayer(),
			     panelProfile.getDefaultAxisDisplayer());
	drawCanvas = new GraphCanvas(this, view, supportX,
				     panelProfile.isReadOnly(), margins);

	int col_o = ((axisSizes != null && axisSizes[WEST_Y] != 0) ? 1 : 0);
	if ((scrollMask & PanelProfile.SCROLL_WEST) != 0)
	    col_o++;

	boolean hasWestPane = (col_o != 0);
	JPanel westPane;

	int westWidth = 0, northHeight = 0, southHeight = 0;
	int northHeightCount = 0;
	boolean yAutoAdapt = panelProfile.isYAxisAutoAdapt();
	//	if (!yAutoAdapt && col_o != 0) {
	if (!yAutoAdapt) {
	    col_o = 0;
	    jpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	    jpane.setBorder(null);
	    westPane = new JPanel(new GridBagLayout());
	    westPane.addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent e) {
			syncJPanes();
			//syncSizes();
		    }
		});

	    w_eastPane = new JPanel(new GridBagLayout());
	    w_eastPane.setBackground(VAMPResources.getColor(VAMPResources.AXIS_BG));
	    if (axisSizes == null || axisSizes[EAST_Y] == 0)
		e_eastPane = null;
	    else {
		e_eastPane = new JPanel(new GridBagLayout());
		e_eastPane.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
			    syncJPanes();
			}
		    });
		
		e_eastPane.setBackground(VAMPResources.getColor(VAMPResources.AXIS_BG));
	    }


	    eastPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	    eastPane.setOneTouchExpandable(true);
	    if (e_eastPane != null)
		eastPane.setDividerSize(2);
	    else
		eastPane.setDividerSize(0);
	    eastPane.setResizeWeight(1.);
	    eastPane.setDividerLocation(500);
	    
	    eastPane.setLeftComponent(w_eastPane);
	    eastPane.setRightComponent(e_eastPane);


	}
	else {
	    jpane = null;
	    westPane = this;
	    //eastPane = this;
	    eastPane = null;
	    e_eastPane = null;
	    w_eastPane = null;
	}

	int row_o = ((axisSizes != null && axisSizes[NORTH_X] != 0) ? 1 : 0);
	if ((scrollMask & PanelProfile.SCROLL_NORTH) != 0)
	    row_o++;

	int col = 0;
	int row = 0;

	if ((scrollMask & PanelProfile.SCROLL_WEST) != 0) {
	    _scrollV = new JScrollBar(JScrollBar.VERTICAL);
	    c = Utils.makeGBC(col, row_o);

	    if (!yAutoAdapt)
		c.weighty = 1.;

	    c.fill = GridBagConstraints.VERTICAL;

	    westPane.add(_scrollV, c);

	    _scrollV.addAdjustmentListener(this);
	    _scrollV.setMinimum(SCROLL_MIN);
	    _scrollV.setMaximum(SCROLL_MAX);
	    westWidth += _scrollV.getPreferredSize().width;

	    col++;
	}

	if (axisSizes != null && axisSizes[WEST_Y] != 0) {
	    _westY = new YAxis(drawCanvas, margins);
	    _setSize(_westY, (!yAutoAdapt ? 1 : axisSizes[WEST_Y]), 1);

	    c = Utils.makeGBC(col, row_o);

	    if (!yAutoAdapt) {
		c.weightx = 1.;
		c.weighty = 1.;
		c.fill = GridBagConstraints.BOTH;
	    }
	    else
		c.fill = GridBagConstraints.VERTICAL;
	    westWidth += axisSizes[WEST_Y];
	    westPane.add(_westY, c);
	}

	if ((scrollMask & PanelProfile.SCROLL_NORTH) != 0) {
	    _scrollH = new JScrollBar(JScrollBar.HORIZONTAL);
	    c = Utils.makeGBC(col_o, row);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    w_eastPane.add(_scrollH, c);
	    _scrollH.addAdjustmentListener(this);
	    _scrollH.setMinimum(SCROLL_MIN);
	    _scrollH.setMaximum(SCROLL_MAX);
	    northHeight += _scrollH.getPreferredSize().height;
	    northHeightCount++;
	    row++;
	}

	if (axisSizes != null && axisSizes[NORTH_X] != 0) {
	    _northX = new XAxis(drawCanvas, margins);
	    _setSize(_northX, 1, axisSizes[NORTH_X]);

	    c = Utils.makeGBC(col_o, row);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    w_eastPane.add(_northX, c);
	    northHeight += axisSizes[NORTH_X];
	    northHeightCount++;
	}

	col = col_o;
	row = row_o;

	c = Utils.makeGBC(col, row);
	c.weightx = 1.;
	c.weighty = 1.;
	c.fill = GridBagConstraints.BOTH;
	w_eastPane.add(drawCanvas, c);

	drawCanvas.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    Dimension size =  ((Component)e.getSource()).getSize();
		    /*
		    System.out.println("resize " +
				       panelProfile.getName() + ": " + size);
		    */
		    // this test is a kludge
		    if (size.width > 100 && size.height > 100) {
			drawCanvas.updateSize(true);
			updateScaleAxis();
			adaptScrollBars();
		    }
		}
	    });
	
	row = row_o;
	col++;

	if (axisSizes != null && axisSizes[EAST_Y] != 0) {
	    _eastY = new AnnotAxis(drawCanvas, margins);
	    _eastY.setBackground(Color.WHITE);
	    _setSize(_eastY, axisSizes[EAST_Y], 1);
	    //	    c = Utils.makeGBC(col, row);
	    c = Utils.makeGBC(0, 1);
	    c.fill = GridBagConstraints.BOTH;
	    c.weightx = 1.;
	    c.weighty = 1.;
	    e_eastPane.add(_eastY, c);
	    e_eastPane.setMinimumSize(new Dimension(axisSizes[EAST_Y], 1));
	    //_setSize(e_eastPane, axisSizes[EAST_Y], 1);
	    col++;
	}

	if (_scrollV == null) {
	    _scrollV = new JScrollBar(JScrollBar.VERTICAL);
	    c = Utils.makeGBC(col, row);
	    c.fill = GridBagConstraints.VERTICAL;
	    w_eastPane.add(_scrollV, c);
	    _scrollV.addAdjustmentListener(this);
	    _scrollV.setMinimum(SCROLL_MIN);
	    _scrollV.setMaximum(SCROLL_MAX);

	    if ((scrollMask & PanelProfile.SCROLL_EAST) == 0)
		_scrollV.setVisible(false);
	}

	col = col_o;
	row = row_o + 1;

	if (axisSizes != null && axisSizes[SOUTH_X] != 0) {
	    _southX = new XAxis(drawCanvas, margins);
	    _setSize(_southX, 1, axisSizes[SOUTH_X]);
	    c = Utils.makeGBC(col, row);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    w_eastPane.add(_southX, c);
	    southHeight += axisSizes[SOUTH_X];
	    row++;
	}

	col = col_o;

	if (_scrollH == null) {
	    _scrollH = new JScrollBar(JScrollBar.HORIZONTAL);
	    c = Utils.makeGBC(col, row);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    w_eastPane.add(_scrollH, c);
	    _scrollH.addAdjustmentListener(this);
	    _scrollH.setMinimum(SCROLL_MIN);
	    _scrollH.setMaximum(SCROLL_MAX);
	    
	    if ((scrollMask & PanelProfile.SCROLL_SOUTH) == 0)
		_scrollH.setVisible(false);
	    else
		southHeight += _scrollH.getPreferredSize().height;
	}

	if (jpane != null) {
	    if (westPane.getComponents().length > 0) {
		northWestPadPanel = new JPanel();
		_setSize(northWestPadPanel, 1, northHeight);
		northWestPadPanel.setBackground(VAMPResources.getColor(VAMPResources.AXIS_BG));
		c = Utils.makeGBC(0, 0);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 2; // westPane.getComponents().length;
		westPane.add(northWestPadPanel, c);
		MLLabel copyright = new MLLabel();
		copyright.setForeground(Color.BLACK);
		copyright.setForeground(new Color(0x000080));
		copyright.setFont(new Font("SansSerif", Font.PLAIN, 10));
		copyright.setBackground(VAMPResources.getColor(VAMPResources.AXIS_BG));
		copyright.setText(VAMPConstants.COPYRIGHT);

		northWestPadPanel.add(copyright);

		northEastPadPanel = new AnnotLegendPanel(AnnotLegendPanel.NORTH, view);
		_setSize(northEastPadPanel, 1, northHeight);
		northEastPadPanel.setBackground(VAMPResources.getColor(VAMPResources.AXIS_BG));
		if (e_eastPane != null) {
		    c = Utils.makeGBC(0, 0);
		    c.fill = GridBagConstraints.BOTH;
		    c.weightx = 1.;
		    e_eastPane.add(northEastPadPanel, c);
		}
		    
		//southWestPadPanel = new JPanel();
		southWestPadPanel = new JPanel() {
			public void paint(Graphics g) {
			    Dimension dim = getSize();
			    //g.setColor(VAMPResources.getColor(VAMPResources.AXIS_BG));
			    g.setColor(Color.WHITE);

			    g.fillRect(0, 0, dim.width, dim.height);
			    SystemConfig sysCfg =
				(SystemConfig)getView().getGlobalContext().
				get(SystemConfig.SYSTEM_CONFIG);
			    String logoURL = sysCfg.getParameter("logo:URL");
			    if (logoURL != null)
				Utils.drawImage(g, getToolkit(),
						logoURL,
						dim.width/2, dim.height/2,
						-1, true, true);
			}
		    };

		_setSize(southWestPadPanel, 1, southHeight);
		southWestPadPanel.setBackground(VAMPResources.getColor(VAMPResources.AXIS_BG));
		c = Utils.makeGBC(0, northHeightCount+1);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 2; // westPane.getComponents().length;
		westPane.add(southWestPadPanel, c);

		southEastPadPanel = new AnnotLegendPanel(AnnotLegendPanel.SOUTH, view);
		_setSize(southEastPadPanel, 1, southHeight);
		southEastPadPanel.setBackground(VAMPResources.getColor(VAMPResources.AXIS_BG));
		if (e_eastPane != null) {
		    c = Utils.makeGBC(0, 2);
		    c.fill = GridBagConstraints.BOTH;
		    c.gridwidth = 1;
		    c.weightx = 1.;
		    e_eastPane.add(southEastPadPanel, c);
		}
	    }

	    if (hasWestPane) {
		jpane.setLeftComponent(westPane);
		jpane.setRightComponent(eastPane);
		jpane.setOneTouchExpandable(true);
		jpane.setDividerSize(2);
		jpane.setResizeWeight(0.);
		jpane.setDividerLocation(westWidth);
	    }
	    else {
		jpane.setRightComponent(eastPane);
		jpane.setDividerSize(0);
		jpane.setResizeWeight(1.);
	    }

	    c = Utils.makeGBC(0, 0);
	    c.fill = GridBagConstraints.BOTH;
	    c.weightx = 1.0;
	    c.weighty = 1.0;

	    add(jpane, c);
	    showHideEastY();
	}

	if (panelProfile.isDisabled()) {
	    setReadOnly(true);
	    setInternalReadOnly(true);
	}
	else {
	    setReadOnly(panelProfile.isReadOnly());
	    setInternalReadOnly(panelProfile.isInternalReadOnly());
	}

	setScrollBars();
    }

    void setScrollBars() {
	if (getDefaultGraphElementDisplayer().isRotated()) {
	    scrollH = _scrollV;
	    scrollV = _scrollH;
	    northX = _eastY;
	    southX = _westY;
	    westY = _northX;
	    /*
	    if (westY != null)
		_setSize(westY, VAMPResources.getInt
			 (VAMPResources.AXIS_WEST_SIZE), 1);
	    */
	    eastY = _southX;
	}
	else {
	    scrollH = _scrollH;
	    scrollV = _scrollV;
	    northX = _northX;
	    southX = _southX;
	    westY = _westY;
	    eastY = _eastY;
	}
    }

    private boolean scrollWhileAdjusting() {
	if (!GraphCanvas.SCROLLING_WHILE_ADJUSTING)
	    return false;

	if (!VAMPResources.getBool(VAMPResources.SCROLLING_WHILE_ADJUSTING))
	    return false;
	LinkedList graphElements = drawCanvas.getGraphElements();
	if (graphElements.size() == 0)
	    return true;
	DataSet dataSet = ((GraphElement)graphElements.get(0)).asDataSet();
	if (dataSet == null)
	    return true;
	return dataSet.getData().length <
	    MAX_DATA_LENGTH_SCROLLING_WHILE_ADJUSTING &&
	    graphElements.size() < MAX_SIZE_SCROLLING_WHILE_ADJUSTING;
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
	if (adapting || adjusting) return;
	adjusting = true;
	JScrollBar source = (JScrollBar)e.getSource();

	if (!source.getValueIsAdjusting() || scrollWhileAdjusting()) {
	    //System.out.println("Adjusting: " + panelProfile.getName());
	    int value = e.getValue() - source.getMinimum();
	    int max = source.getMaximum() - source.getVisibleAmount() -
		source.getMinimum();

	    if (max == 0) {adjusting = false; return;}
	    double percent = (double)value/(double)max;

	    /*
	    System.out.println("AdjustmentValue: " + value + ", enabled=" +
			       source.isEnabled() +
			       ", max=" + max + ", percent=" + percent);
	    */

	    if (source == scrollH) {
		drawCanvas.setOrigX(percent);
		setOrigX(northX, percent);
		setOrigX(southX, percent);
		int l_cnt = linkedPaneX.size();
		for (int n = 0; n < l_cnt; n++) {
		    GraphPanel lnk = (GraphPanel)linkedPaneX.get(n);
		    lnk.scrollH.setValue(e.getValue());
		}
	    }
	    else {
		drawCanvas.setOrigY(percent);
		setOrigY(westY, percent);
		setOrigY(eastY, percent);
		int l_cnt = linkedPaneY.size();
		for (int n = 0; n < l_cnt; n++) {
		    GraphPanel lnk = (GraphPanel)linkedPaneY.get(n);
		    lnk.scrollV.setValue(e.getValue());
		}
	    }

	    //System.out.println(e);
	    /*
	    System.out.println("\tValue: " + e.getValue());
	    System.out.println("\tVisible = " + source.getVisibleAmount());
	    System.out.println("\tMaximum = " + source.getMaximum());
	    */
	}
	adjusting = false;
    }


    void adaptScrollH() {
	adapting = true;
	Dimension size = drawCanvas.getSize();
	Margins margins = drawCanvas.getRMargins();
	Scale scale = drawCanvas.scale;

	if (scrollH != null) {
	    int width = (int)(vSize.width * scale.getScaleX()) + margins.getMarginWidth();
	    if (size.width == 0) size.width = 1;

	    int width2 = (int)(((double)size.width / (double)width) * SCROLL_MAX);
	    if (width2 > SCROLL_MAX) {
		width2 = SCROLL_MAX-1;
		scrollH.setEnabled(false);
	    }
	    else {
		scrollH.setEnabled(true);
	    }

	    if (scrollH.getValue()+width2 > scrollH.getMaximum()) {
		//System.out.println("setting H value");
		scrollH.setValue(scrollH.getMaximum()-width2-1);
	    }
	    scrollH.setVisibleAmount(width2);
	}
	adapting = false;
    }

    void adaptScrollV() {
	adapting = true;
	Dimension size = drawCanvas.getSize();
	Margins margins = drawCanvas.getRMargins();
	Scale scale = drawCanvas.scale;

	if (scrollV != null) {
	    int height = (int)(vSize.height * scale.getScaleY()) + margins.getMarginHeight();
	    if (size.height == 0) size.height = 1;

	    int height2 = (int)(((double)size.height / (double)height) * SCROLL_MAX);
	    if (height2 > SCROLL_MAX) {
		height2 = SCROLL_MAX-1;
		scrollV.setEnabled(false);
	    }
	    else {
		scrollV.setEnabled(true);
	    }

	    /*
	      System.out.println("Adapt ScrollV: " + height2 + ", value: " +
	      scrollV.getValue());
	    */

	    if (scrollV.getValue()+height2 > scrollV.getMaximum()) {
		//System.out.println("setting V value");
		scrollV.setValue(scrollV.getMaximum()-height2-1);
	    }
	    scrollV.setVisibleAmount(height2);
	}
	adapting = false;
    }

    void adaptScrollBars() {
	Dimension size = drawCanvas.getSize();
	Margins margins = drawCanvas.getRMargins();
	Scale scale = drawCanvas.scale;

	adaptScrollH();
	adaptScrollV();
    }

    void setVirtualSize(double width, double height) {
	/*
	if (vSize.width == width && vSize.height == height)
	    return;

	*/
	/*
	System.out.println("setVirtualSize: " +
			   width + ", " + height + " vs. " +
			   vSize.width + ", " + vSize.height);
	*/
	vSize.width = width;
	vSize.height = height;

	setVirtualSize(northX, width, height);
	setVirtualSize(southX, width, height);
	setVirtualSize(westY, width, height);
	setVirtualSize(eastY, width, height);
	setVirtualSize(drawCanvas, width, height);
	adaptScrollBars();
    }

    public void addLinkedPane(GraphPanel linkedPane, int sync_mode) {
	if (sync_mode == SYNCHRO_X)
	    linkedPaneX.add(linkedPane);
	else if (sync_mode == SYNCHRO_Y)
	    linkedPaneY.add(linkedPane);
    }

    private static void setOrigX(MPanel panel, double percent) {
	if (panel != null)
	    panel.setOrigX(percent);
    }

    private static void setOrigY(MPanel panel, double percent) {
	if (panel != null)
	    panel.setOrigY(percent);
    }

    private static void setVirtualSize(MPanel panel, double width, double height) {
	if (panel != null)
	    panel.setVirtualSize(width, height);
    }

    private static void setScale(MPanel panel, double scaleX, double scaleY) {
	if (panel != null)
	    panel.setScale(scaleX, scaleY);
    }

    private static void setScaleX(MPanel panel, double value) {
	if (panel != null)
	    panel.setScaleX(value);
    }

    private static void setScaleY(MPanel panel, double value) {
	if (panel != null)
	    panel.setScaleY(value);
    }

    private static void setFitInPage(MPanel panel, boolean value) {
	if (panel != null)
	    panel.setFitInPage(value);
    }

    private static void setVCenter(MPanel panel, Point2D.Double vcenter) {
	if (panel != null)
	    panel.setVCenter(vcenter);
    }

    void updateScaleAxis() {
	double scaleX = drawCanvas.getScale().getScaleX();
	double scaleY = drawCanvas.getScale().getScaleY();
	setScale(northX, scaleX, scaleY);
	setScale(southX, scaleX, scaleY);
	setScale(westY, scaleX, scaleY);
	setScale(eastY, scaleX, scaleY);
    }

    void setFitInPage(boolean value) {
	boolean undo = (value && !isFitInPage() && getGraphElements().size() != 0);
	if (undo) {
	    StandardVMStatement vmstat = new StandardVMStatement
		(VMOP.getVMOP(VMOP.FIT_IN_PAGE), this);
	    vmstat.beforeExecute();
	    UndoVMStack.getInstance(this).push(vmstat);
	}

	boolean active = UndoVMStack.getInstance(this).isActive();
	if (undo)
	    UndoVMStack.getInstance(this).setActive(false);

	setFitInPage(drawCanvas, value);
	adaptScrollBars();
	updateScaleAxis();

	if (undo)
	    UndoVMStack.getInstance(this).setActive(active);
    }

    boolean isFitInPage() {
	return drawCanvas.isFitInPage();
    }

    void setScaleX(double scaleX) {

	UndoableVMStatement lvmstat = UndoVMStack.getInstance(this).getLastUndoableVMStatement();
	if (lvmstat == null || !lvmstat.getVMOP().getName().equals
	    (VMOP.SET_SCALE_X)) {
	    StandardVMStatement vmstat = new StandardVMStatement
		(VMOP.getVMOP(VMOP.SET_SCALE_X), this);
	    vmstat.beforeExecute();
	    UndoVMStack.getInstance(this).push(vmstat);
	}

	setScaleX(drawCanvas, scaleX);

	scaleX = drawCanvas.getScale().getScaleX();
	setScaleX(northX, scaleX);
	setScaleX(southX, scaleX);
	setScaleX(westY, scaleX);
	setScaleX(eastY, scaleX);

	// pb with vcenter
	adaptScrollBars();
    }

    void setScaleY(double scaleY) {
	UndoableVMStatement lvmstat = UndoVMStack.getInstance(this).getLastUndoableVMStatement();
	if (lvmstat == null || !lvmstat.getVMOP().getName().equals
	    (VMOP.SET_SCALE_Y)) {
	    StandardVMStatement vmstat = new StandardVMStatement
		(VMOP.getVMOP(VMOP.SET_SCALE_Y), this);
	    vmstat.beforeExecute();
	    UndoVMStack.getInstance(this).push(vmstat);
	}

	setScaleY(drawCanvas, scaleY);

	scaleY = drawCanvas.getScale().getScaleY();
	setScaleY(northX, scaleY);
	setScaleY(southX, scaleY);
	setScaleY(westY, scaleY);
	setScaleY(eastY, scaleY);

	// pb with vcenter
	adaptScrollBars();
    }

    void setVCenter(Point2D.Double vcenter) {
	if (centering) return;
	centering = true;

	setVCenter(drawCanvas, vcenter);
	setVCenter(northX, vcenter);
	setVCenter(southX, vcenter);
	setVCenter(westY, vcenter);
	setVCenter(eastY, vcenter);

	int l_cnt = linkedPaneX.size();
	for (int n = 0; n < l_cnt; n++) {
	    GraphPanel lnk = (GraphPanel)linkedPaneX.get(n);
	    lnk.setVCenter(vcenter);
	}

	/*
	if (linkedPane != null && (linkedMask & SYNCHRO_X) != 0)
	    linkedPane.setVCenter(vcenter);
	*/
	centering = false;
    }

    public boolean setGraphElements(LinkedList graphElements) {
	return drawCanvas.setGraphElements(graphElements);
    }

    void setInfoWindow(GraphElement set, PropertyElement elem, Region region,
		       Mark mark, DataElementRange range,
		       double vx, boolean pinnedUp) {
	JPanel panel = view.getInfoDisplayer().display
	    (view.getInfoPanel(), set, elem, region, mark, pinnedUp);
	view.getInfoPanel().setInfo(this, panel, set, elem, region, mark,
				    range, vx, pinnedUp);
    }

    void setInfoPanel(int which) {
	view.getInfoPanel().setInfoPanel(which);
    }

    void setPosX(long begin, long end, long position) {
	view.getInfoPanel().setPosX(begin, end, position);
    }

    double getChrPosX(String organism, String chr, double posx) {
	return drawCanvas.getChrPosX(organism, chr, posx);
    }

    void unsetPosX() {
	view.getInfoPanel().unsetPosX();
    }

    private void setScroll(JScrollBar scroll, double percent) {
	if (scroll == null) return;

	adapting = true;
	int max = scroll.getMaximum() - scroll.getVisibleAmount() -
	    scroll.getMinimum();

	/*
	System.out.println("setScroll " +
			   (scroll == scrollV ? "V " : "H ") +
			   percent + ", " +
			   (int)(max*percent) + ", " +
			   scroll.getValue());
	*/

	scroll.setValue((int)(max * percent));
	adapting = false;
    }

    void setScrollX(double percent) {
	setScroll(scrollH, percent);
    }

    void setScrollY(double percent) {
	setScroll(scrollV, percent);
    }

    void incrScrollY(int incr, boolean adjust) {
	double ratio;
	if (adjust)
	    ratio = 1.;
	else {
	    int cnt = drawCanvas.getGraphElements().size();
	    double max = scrollV.getMaximum() - scrollV.getMinimum();
	    ratio = max/cnt;
	}
	scrollV.setValue(scrollV.getValue() + (int)(incr * ratio));
    }

    void selectAll(boolean select) {
	selectAll(select, false);
    }

    void selectAll(boolean select, boolean immediate) {
	drawCanvas.selectAll(select, immediate);
    }

    void selectAllGraphElements(boolean select) {
	drawCanvas.selectAllGraphElements(select, false);
    }

    void selectAllGraphElements(boolean select, boolean immediate) {
	drawCanvas.selectAllGraphElements(select, immediate);
    }

    void selectAllRegions(boolean select) {
	drawCanvas.selectAllRegions(select);
    }

    void selectAllMarks(boolean select) {
	drawCanvas.selectAllMarks(select);
    }

    void removeAllS() {
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.REMOVE_ALL), this);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);
	drawCanvas.removeAllS();
	vmstat.afterExecute();
    }

    /*
    void removeAllS() {
	UndoVMStack.getInstance(this).pushExecute(new RemoveAllVMStatement(this));
    }

    void removeAllS_r() {
	drawCanvas.removeAllS();
    }
    */

    void selectAndCopyAll() {
	drawCanvas.selectAndCopyAll();
    }

    void selectAndCopyAllGraphElements() {
	drawCanvas.selectAndCopyAllGraphElements();
    }

    void selectAndCopyAllRegions() {
	drawCanvas.selectAndCopyAllRegions();
    }

    void selectAndCopyAllMarks() {
	drawCanvas.selectAndCopyAllMarks();
    }

    void setReadOnly(boolean isReadOnly) {
	drawCanvas.setReadOnly(isReadOnly);
    }

    boolean isReadOnly() {
	return drawCanvas.isReadOnly();
    }

    boolean isInternalReadOnly() {
	return internalReadOnly;
    }

    void setInternalReadOnly(boolean internalReadOnly) {
	this.internalReadOnly = internalReadOnly;
    }

    void cutSelection() {
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.CUT), this);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);
	drawCanvas.cutSelection();
	vmstat.afterExecute();
    }

    /*
    void cutSelection() {
	UndoVMStack.getInstance(this).pushExecute(new CutVMStatement(this));
    }

    void cutSelection_r() {
	drawCanvas.cutSelection();
    }
    */

    void copySelection() {
	drawCanvas.copySelection();
    }

    void pasteSelection() {
	pasteSelection(null);
    }

    void pasteSelection(GraphElement graphElem) {
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.PASTE), this);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);
	drawCanvas.pasteSelection(graphElem);
	vmstat.afterExecute();
    }

    /*
    void pasteSelection(GraphElement graphElem) {
	UndoVMStack.getInstance(this).pushExecute(new PasteVMStatement(this, graphElem));
    }

    void pasteSelection_r(GraphElement graphElem) {
	drawCanvas.pasteSelection(graphElem);
    }
    */

    void removeSelection() {
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.REMOVE_SELECTION), this);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);
	drawCanvas.removeSelection();
	vmstat.afterExecute();
    }

    /*
    void removeSelection() {
	UndoVMStack.getInstance(this).pushExecute(new RemoveSelectionVMStatement(this));
    }

    void removeSelection_r() {
	drawCanvas.removeSelection();
    }
    */

    void removeMarks() {
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.REMOVE_MARKS), this);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);
	drawCanvas.removeMarks();
	vmstat.afterExecute();
    }

    void removeGraphElements() {
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.REMOVE_PROFILES), this);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);
	drawCanvas.removeGraphElements();
	vmstat.afterExecute();
    }

    /*
    void removeMarks() {
	UndoVMStack.getInstance(this).pushExecute(new RemoveMarksVMStatement(this));
    }

    void removeMarks_r() {
	drawCanvas.removeMarks();
    }
    */

    void removeRegions(boolean removeRegionMarks) {
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.REMOVE_REGIONS), this);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);
	drawCanvas.removeRegions(removeRegionMarks);
	vmstat.afterExecute();
    }

    /*
    void removeRegions(boolean removeRegionMarks) {
	UndoVMStack.getInstance(this).pushExecute(new RemoveRegionsVMStatement(this, removeRegionMarks));
    }

    void removeRegions_r(boolean removeRegionMarks) {
	drawCanvas.removeRegions(removeRegionMarks);
    }
    */

    void addMark(Mark mark) {
	StandardVMStatement vmstat = new CreateMarkVMStatement
	    (this, mark);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);

	drawCanvas.addMark(mark);

	vmstat.afterExecute();
	view.getInfoPanel().sync();
    }

    void addRegion(Region region, Mark begin, Mark end) {
	StandardVMStatement vmstat = new CreateRegionVMStatement
	    (this, begin, end);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);

	drawCanvas.addRegion(region);

	vmstat.afterExecute();
	view.getInfoPanel().sync();
    }

    boolean removeMark(Mark mark) {
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.REMOVE_MARK), this);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);

	boolean r = drawCanvas.removeMark(mark);
	vmstat.afterExecute();
	if (!r)
	    return false;

	view.getInfoPanel().sync();
	return true;
    }

    void removeRegion(Region region, boolean removeMarks) {
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.REMOVE_REGION), this);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(this).push(vmstat);

	drawCanvas.removeRegion(region, removeMarks);

	vmstat.afterExecute();
	view.getInfoPanel().sync();
    }

    void sync(boolean invalidate) {
	if (invalidate)
	    drawCanvas.invalidateOffscreen();
	repaint();
	view.getInfoPanel().sync();
    }

    boolean cutRegion(Region region) {
	return drawCanvas.cutRegion(region);
    }

    boolean cutMark(Mark mark) {
	return drawCanvas.cutMark(mark);
    }

    boolean hasSelection() {
	return drawCanvas.hasSelection();
    }

    boolean hasGraphElementSelection() {
	return drawCanvas.hasGraphElementSelection();
    }

    boolean hasRegionSelection() {
	return drawCanvas.hasRegionSelection();
    }

    boolean hasMarkSelection() {
	return drawCanvas.hasMarkSelection();
    }

    Vector getSelectedGraphElements() {
	return drawCanvas.getSelectedGraphElements();
    }

    void replaceGraphElements(Vector from, Vector to) {
	drawCanvas.replaceGraphElements(from, to);
    }

    int setPaintingRegionMode(int sync_mode) {
	return drawCanvas.setPaintingRegionMode(sync_mode);
    }

    LinkedList getGraphElements() {
	return drawCanvas.getGraphElements();
    }

    void syncGraphElements(boolean readaptSize) {
	setScrollBars();
	drawCanvas.syncGraphElements(readaptSize);
    }

    void syncGraphElements(boolean readaptSize, boolean applyOP,
			   boolean warn) {
	setScrollBars();
	drawCanvas.syncGraphElements(readaptSize, applyOP, warn);
    }

    void clearCenter() {
	drawCanvas.clearCenter();
    }

    void recenter() {
	drawCanvas.recenter();
    }

    void centerOnMark(Mark mark) {
	//GraphElement graphElement = drawCanvas.getGraphElement(0);
	GraphElement graphElement = drawCanvas.getTemplateDS();
	setVCenter(new Point2D.Double(mark.getVX(drawCanvas), 0));
	mark.setCentered(true);
	drawCanvas.repaint();
    }

    void centerOnRegion(Region region) {
	//GraphElement graphElement = drawCanvas.getGraphElement(0);
	GraphElement graphElement = drawCanvas.getTemplateDS();
	double vx = region.getBegin().getVX(drawCanvas) +
	    (region.getEnd().getVX(drawCanvas) - region.getBegin().getVX(drawCanvas))/2.;
	setVCenter(new Point2D.Double(vx, 0));
	region.setCentered(true);
	drawCanvas.repaint();
    }

    GraphCanvas getCanvas() {return drawCanvas;}

    void setPosX(double pos_x, boolean center) {
	if (scrollH == null) return;

	int max = scrollH.getMaximum() - scrollH.getVisibleAmount() -
	    scrollH.getMinimum();

	double vx = drawCanvas.computeVX(pos_x);

	if (center)
	    vx -= drawCanvas.getVWidth()/2;

	double percentX = drawCanvas.getPercentX(vx);
	int value = (int)(percentX * max);
	/*
	System.out.println("setPositionX(" + vx + ", percentX: " +
			   percentX + ", max: " + max +
			   ", scaleX: " +
			   drawCanvas.getScale().getScaleX() +
			   ", canon_scaleX: " +
			   drawCanvas.getCanonScale().getScaleX() + ")");
	*/
	scrollH.setValue(value);
    }

    String getCenterType() {
	return drawCanvas.getCenterType();
    }

    void clearPinnedUp() {
	drawCanvas.clearPinnedUp();
    }

    boolean hasPinnedUp() {
	return drawCanvas.hasPinnedUp();
    }

    void setWestYSize(int sz, boolean force) {
	if (westY == null || sz < 0) return;

	Dimension size = westY.getPreferredSize();
	if (getDefaultGraphElementDisplayer().isRotated()) {
	    if (sz == size.height) return;
	    if (sz < size.height && !force) return;
	    westY.setPreferredSize(new Dimension(size.width, sz));
	}
	else {
	    if (sz == size.width) return;
	    if (sz < size.width && !force) return;
	    westY.setPreferredSize(new Dimension(sz, size.height));
	}
	revalidate();
    }

    void incrWestYSize(int incr) {
	if (westY == null || incr == 0) return;

	Dimension size = westY.getSize();
	if (getDefaultGraphElementDisplayer().isRotated()) {
	    if (size.height+incr >= 0)
		westY.setPreferredSize(new Dimension(size.width, size.height+incr));
	}
	else {
	    if (size.width+incr >= 0)
		westY.setPreferredSize(new Dimension(size.width+incr, size.height));
	}
	revalidate();
    }

    void incrWestMargin(int incr) {
	drawCanvas.incrWestMargin(incr);
	scrollH.setValue(scrollH.getValue()+1);
	scrollH.setValue(scrollH.getValue()-1);
	drawCanvas.updateSize(false);
    }

    void setRegions(LinkedList regions) {
	drawCanvas.setRegions(regions);
    }

    void setMarks(LinkedList marks) {
	drawCanvas.setMarks(marks);
    }

    LinkedList getMarks() {
	return drawCanvas.getMarks();
    }

    LinkedList getRegions() {
	return drawCanvas.getRegions();
    }

    String getRegionString() {
	LinkedList regions = getRegions();
	int size = regions.size();
	String region_str = "";
	for (int n = 0; n < size; n++) {
	    Region region = (Region)regions.get(n);
	    region_str += (region_str.length() > 0 ? "|" : "") +
		(long)region.getBegin().getPosX() + ":" +
		(long)region.getEnd().getPosX();
	}
	return region_str;
    }

    void reinitWestYSize() {
	if (axisSizes == null) return;
	if (!isYAxisAutoAdapt()) return;

	if (getDefaultGraphElementDisplayer().isRotated())
	    setWestYSize(axisSizes[NORTH_X], true);
	else
	    setWestYSize(axisSizes[WEST_Y], true);
    }

    void setDefaultGraphElementDisplayer(GraphElementDisplayer defaultGraphElementDisplayer) {
	try {
	    this.defaultGraphElementDisplayer = (GraphElementDisplayer)defaultGraphElementDisplayer.clone();
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

    void setDefaultAxisDisplayer(AxisDisplayer defaultAxisDisplayer) {
	try {
	    this.defaultAxisDisplayer = (AxisDisplayer)defaultAxisDisplayer.clone();
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

    void setDefaultDisplayers(GraphElementDisplayer defaultGraphElementDisplayer,
			      AxisDisplayer defaultAxisDisplayer) {
	this.setDefaultGraphElementDisplayer(defaultGraphElementDisplayer);
	this.setDefaultAxisDisplayer(defaultAxisDisplayer);
    }

    void setGraphElementIDBuilder(GraphElementIDBuilder graphElementIDBuilder) {
	this.graphElementIDBuilder = graphElementIDBuilder;
	syncGraphElementIDBuilder();
	repaint();
    }

    private void syncGraphElementIDBuilder() {
	if (defaultAxisDisplayer != null)
	    defaultAxisDisplayer.setGraphElementIDBuilder(graphElementIDBuilder);
	if (defaultGraphElementDisplayer != null)
	    defaultGraphElementDisplayer.setGraphElementIDBuilder(graphElementIDBuilder);
    }

    GraphElementIDBuilder getGraphElementIDBuilder() {
	return graphElementIDBuilder;
    }

    GraphElementDisplayer getDefaultGraphElementDisplayer() {
	return defaultGraphElementDisplayer;
    }

    AxisDisplayer getDefaultAxisDisplayer() {
	return defaultAxisDisplayer;
    }

    boolean supportX() {return supportX;}
    int[] getAxisSizes() {return panelProfile.getAxisSizes();}

    void setAutoApplyDSLOP(GraphElementListOperation autoApplyDSLOP) {
	this.autoApplyDSLOP = autoApplyDSLOP;
    }

    GraphElementListOperation getAutoApplyDSLOP() {
	return autoApplyDSLOP;
    }

    boolean checkSetGraphElements(LinkedList graphElements) {
	return defaultGraphElementDisplayer.checkGraphElements(graphElements) &&
	    defaultAxisDisplayer.checkGraphElements(graphElements);
    }

    void warnGraphElements(GlobalContext globalContext,
			   LinkedList graphElements) {
	defaultGraphElementDisplayer.warnGraphElements(globalContext,
						       graphElements);
	defaultAxisDisplayer.warnGraphElements(globalContext, graphElements);
    }

    LinkedList getLinkedPaneX() {return linkedPaneX;}
    LinkedList getLinkedPaneY() {return linkedPaneY;}

    public boolean isYAxisAutoAdapt() {return panelProfile.isYAxisAutoAdapt();}

    public String getPanelName() {return panelProfile.getName();}

    public int getWhich() {return which;}
    public PanelProfile getPanelProfile() {return panelProfile;}

    public boolean highlightMinimalRegions() {return highlightMinimalRegions;}

    void setHighlightMinimalRegions(boolean b) {
	highlightMinimalRegions = b;
    }

    boolean highlightRecurrentAlterations() {return highlightRecurrentAlterations;}

    void setHighlightRecurrentAlterations(boolean b) {
	highlightRecurrentAlterations = b;
    }

    void setRootSplitPane(JSplitPane root_splitPane) {
	this.root_splitPane = root_splitPane;
    }

    void syncSizes() {
	if (root_splitPane == null)
	    return;

	int l_cnt = linkedPaneX.size();
	for (int n = 0; n < l_cnt; n++) {
	    GraphPanel lnkPanel = (GraphPanel)linkedPaneX.get(n);
	    if (lnkPanel.root_splitPane != null)
		lnkPanel.root_splitPane.setDividerLocation
		    (root_splitPane.getDividerLocation());
	}
    }

    private void syncJPanes() {
	if (jpane == null)
	    return;

	int l_cnt = linkedPaneX.size();
	for (int n = 0; n < l_cnt; n++) {
	    GraphPanel lnkPanel = (GraphPanel)linkedPaneX.get(n);
	    if (lnkPanel.jpane != null)
		lnkPanel.jpane.setDividerLocation(jpane.getDividerLocation());
	    lnkPanel.eastPane.setDividerLocation(eastPane.getDividerLocation());
	}
    }

    void syncLinkedPaneY() {
	int l_cnt = linkedPaneY.size();
	for (int n = 0; n < l_cnt; n++) {
	    GraphPanel lnk = (GraphPanel)linkedPaneY.get(n);
	    lnk.syncGraphElements(true);
	    lnk.repaint();
	}
    }


    // 3/01/05: introduced setMinX and setMaxX
    void setMinX(double minX) {
	getCanvas().setMinX(minX);
	int l_cnt = linkedPaneX.size();
	for (int n = 0; n < l_cnt; n++) {
	    GraphPanel lnk = (GraphPanel)linkedPaneX.get(n);
	    double mX = lnk.getCanvas().getMinX();
	    if (mX < minX)
		getCanvas().setMinX(mX);
	    else
		lnk.getCanvas().setMinX(minX);
	}
    }

    void setMaxX(double maxX) {
	getCanvas().setMaxX(maxX);
	int l_cnt = linkedPaneX.size();
	for (int n = 0; n < l_cnt; n++) {
	    GraphPanel lnk = (GraphPanel)linkedPaneX.get(n);
	    double mX = lnk.getCanvas().getMaxX();
	    if (mX > maxX)
		getCanvas().setMaxX(mX);
	    else
		lnk.getCanvas().setMaxX(maxX);
	}
    }

    // 3/01/05: introduced setMinY and setMaxY
    // should be cloned from setMinX and setMaxX ???
    void setMinY(double minY) {
	getCanvas().setMinY(minY);
    }

    void setMaxY(double maxY) {
	getCanvas().setMaxY(maxY);
    }

    JSplitPane getSplitPane() {return root_splitPane;}

    boolean hasEastY() {
	return jpane.getRightComponent() == eastPane;
    }

    private boolean showHideEastY = false;

    void showHideEastY(boolean show) {
	if (_eastY == null)
	    return;

	if (showHideEastY)
	    return;

	showHideEastY = true;

	boolean hasEastY = hasEastY();
	if (!show && hasEastY) {
	    eastY_width = _eastY.getSize().width;
	    int min_width = axisSizes[EAST_Y] + 3;
	    if (eastY_width < min_width)
		eastY_width = min_width;
	    eastPane.setLeftComponent(null);
	    eastPane.setVisible(false);
	    jpane.setRightComponent(w_eastPane);
	    revalidate();
	}
	else if (show && !hasEastY) {
	    eastPane.setLeftComponent(w_eastPane);
	    eastPane.setVisible(true);
	    eastPane.setDividerLocation(eastPane.getSize().width - eastY_width);
	    jpane.setRightComponent(eastPane);
	    revalidate();
	}

	int sz = linkedPaneX.size();
	for (int n = 0; n < sz; n++) {
	    GraphPanel lnkPanel = (GraphPanel)linkedPaneX.get(n);
	    lnkPanel.showHideEastY(show);
	}

	showHideEastY = false;
    }

    void showHideEastY() {
	showHideEastY(!hasEastY());
    }

    void resetPropertyAnnot() {
	if (northEastPadPanel != null)
	    northEastPadPanel.reset();
	if (southEastPadPanel != null)
	    southEastPadPanel.reset();
    }

    void setPropertyAnnot(int x, Property prop) {
	if (northEastPadPanel != null)
	    northEastPadPanel.setPropertyAnnot(x, prop);
	if (southEastPadPanel != null)
	    southEastPadPanel.setPropertyAnnot(x, prop);
    }

    void repaintPropertyAnnot() {
	if (northEastPadPanel != null)
	    northEastPadPanel.repaint();
	if (southEastPadPanel != null)
	    southEastPadPanel.repaint();
    }

    View getView() {
	return view;
    }

    static void repaintAxis(Axis axis) {
	if (axis != null)
	    axis.repaint();
    }

    void repaintAxis() {
	repaintAxis(northX);
	repaintAxis(southX);
	repaintAxis(westY);
	repaintAxis(eastY);
    }


    private boolean reglock = false;

    synchronized boolean isRegionsLocked() {
	System.out.println(this + " isRegionLocked " + reglock);
	return reglock;
    }

    synchronized void regionsLocked(boolean reglock) {
	System.out.println(this + " regionLocked " + reglock + " " +
			   System.currentTimeMillis());
	this.reglock = reglock;
    }
}
