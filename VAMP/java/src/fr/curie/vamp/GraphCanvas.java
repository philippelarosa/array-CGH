
/*
 *
 * GraphCanvas.java
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
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import fr.curie.vamp.data.*;
import fr.curie.vamp.gui.*;

public class GraphCanvas extends MPanel implements DragGestureListener,
						   DragSourceListener, DropTargetListener {

    static int NUM_PAINTS = 1;
    static final int INCR_WEST_MARGIN = 5;
    static final int INCR_WEST_YAXIS = 5;

    public static boolean VERBOSE = false;
    static boolean NO_DISPLAY = false;
    static boolean PROFILE = false;
    static boolean USE_OPT_SEL = false;
    static boolean COMPUTE_RCOORDS = true;
    static boolean DSP_MAP = true;
    static boolean DOUBLE_BUFFERING = true;
    static boolean SCROLLING_WHILE_ADJUSTING = true;
    static boolean DEBUG1 = false;
    static boolean DEBUG2 = false;
    static boolean DEBUG3 = false;
    static boolean DEBUG4 = false;

    private LinkedList graphElements = new LinkedList();
    private Vector selGraphElements = new Vector();
    private DataSet templateDS = null;
    private Profile templateProfile = null;
    private GraphPanel graphPanel;
    private int sizeSets = 0;
    private double maxX, maxY, minX, minY;
    private GraphElement centeredGraphElem;
    private DataElement centeredElem;
    private Probe centeredProbe;
    private DendrogramGraphElement centeredDendroGE;
    private Color centeredDendroGEColor;
    private GraphElement pinnedUpGraphElem;
    private DataElement pinnedUpElem;
    private Probe pinnedUpProbe;
    private Region pinnedUpRegion;
    private Mark centeredMark;
    private Region centeredRegion;
    private double vPadY;
    private View view;
    private LinkedList _marks, _regions;
    private final int EPSILON = 2;
    private final int EPSILON2 = EPSILON/2;
    private GraphPanelSet graphPanelSet;
    private boolean syncing = false;

    private int curPosX, curPosY;
    private int curPX, curPY;
    private boolean isReadOnly;
    private boolean marksEnabled = false;
    private static Clipboard clipboard = Clipboard.getInstance();
    private boolean isIn = false;
    private SystemConfig systemConfig;
    private GraphElement shiftRef;
    private boolean self_dropping = false;
    private int which;

    static final int SYNC_MODE = 1;
    static final int ASYNC_MODE = 2;
    private Object lockObj = new Object();
    private Vector<Selectable> select_v = null;

    private int painting_region_mode = SYNC_MODE;

    GraphCanvas(GraphPanel _graphPanel, View _view, boolean hasX,
		boolean isReadOnly, Margins margins) {
	super("canvas", hasX,
	      _graphPanel.getDefaultGraphElementDisplayer().isRotated(), false, margins);
	RepaintManager rpm = RepaintManager.currentManager(this);
	rpm.setDoubleBufferingEnabled(GraphCanvas.DOUBLE_BUFFERING);

	this.graphPanel = _graphPanel;
	this.which = graphPanel.getWhich();
	this.view = _view;
	systemConfig = (SystemConfig)getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);
	this.isReadOnly = isReadOnly;
	setMarks(new LinkedList());
	setRegions(new LinkedList());
	DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer
	    (this, DnDConstants.ACTION_COPY_OR_MOVE, this);

	setDropTarget(new DropTarget(this, this));
	addKeyListener(new KeyAdapter() {
		public void keyReleased(KeyEvent e) {
		    //System.out.println("canvas " + e);
		    int c = e.getKeyCode();
		    int mod = e.getModifiers();
		    //System.out.println("keyCode: " + c);
		    //System.out.println("modifier: " + mod);
		    //if ((mod & MouseEvent.CTRL_MASK) != 0) {
		    if ((mod & MouseEvent.ALT_MASK) != 0) {
			if (c == 'Z') {
			    VERBOSE = !VERBOSE;
			    System.out.println("Verbose: " + VERBOSE);
			}
			else if (c == 'P')
			    PROFILE = !PROFILE;
			else if (c == 'T')
			    GraphElementListOperation.toggleThread();
			else if (c == 'A')
			    SCROLLING_WHILE_ADJUSTING =
				!SCROLLING_WHILE_ADJUSTING;
			else if (c == 'D')
			    NO_DISPLAY = !NO_DISPLAY;
			else if (c == 'S')
			    USE_OPT_SEL = !USE_OPT_SEL;
			else if (c == 'R')
			    COMPUTE_RCOORDS = !COMPUTE_RCOORDS;
			else if (c == 'E') {
			    System.out.println("ALLOC_PROBE_CNT " + Probe.ALLOC_PROBE_CNT);
			    Utils.gc();
			    Utils.freeMemory();
			    int cnt = 0;
			    for (int n = 0; n < graphElements.size(); n++) {
				Profile profile = ((GraphElement)graphElements.get(n)).asProfile();
				if (profile != null) {
				    profile.release();
				    profile.getGraphicProfile().release();
				    cnt++;
				}
			    }
			    fr.curie.vamp.utils.FileInputManager.getInstance().clear();
			    System.out.println("HAVE RELEASED " + cnt + " PROFILES");
			    Utils.gc();
			    Utils.freeMemory();
			}
			else if (c == '8') {
			    Utils.gc();
			    Utils.freeMemory();
			    int cnt = 0;
			    for (int n = 0; n < graphElements.size(); n++) {
				Profile profile = ((GraphElement)graphElements.get(n)).asProfile();
				if (profile != null) {
				    profile.getGraphicProfile().restore();
				    cnt++;
				}
			    }
			    fr.curie.vamp.utils.FileInputManager.getInstance().clear();
			    System.out.println("HAVE RESTORED " + cnt + " PROFILES");
			    Utils.gc();
			    Utils.freeMemory();
			}
			else if (c == 'G') {
			    Utils.gc();
			    Utils.freeMemory();
			}
			else if (c == 'H')
			    Utils.COOKIE_V1_5 = !Utils.COOKIE_V1_5;
			else if (c == 'Y')
			    graphPanel.showHideEastY();
			else if (c == 'O')
			    GraphElement.SUPPORT_OFFSCREEN = !GraphElement.SUPPORT_OFFSCREEN;
			else if (c == 'N')
			    GraphElement.FORCE_OFFSCREEN = !GraphElement.FORCE_OFFSCREEN;
			else if (c == 'B')
			    DSP_MAP = !DSP_MAP;
			else if (c == 'M')
			    setModified(true);

			else if (c == '1' || c == 'U') {
			    DEBUG1 = !DEBUG1;
			    System.out.println("DEBUG1 " + DEBUG1);
			}
			else if (c == '2' || c == 'V') {
			    DEBUG2 = !DEBUG2;
			    System.out.println("DEBUG2 " + DEBUG2);
			}
			else if (c == '3') {
			    DEBUG3 = !DEBUG3;
			    System.out.println("DEBUG3 " + DEBUG3);
			}
			else if (c == '4') {
			    DEBUG4 = !DEBUG4;
			    System.out.println("DEBUG4 " + DEBUG4);
			}

			else if (c == 'F')
			    SortDialog.pop(getGlobalContext(),
					     "Filter Panel",
					     view,
					     graphPanel, null);
			else
			    Utils.freeMemory();

			System.out.println("\nUse Optim Selection: " + USE_OPT_SEL + " [" + useOptSel() + "]");
			System.out.println("OffScreen: " + GraphElement.SUPPORT_OFFSCREEN);
			System.out.println("Force OffScreen: " + GraphElement.FORCE_OFFSCREEN);
			System.out.println("Profile: " + PROFILE);
			System.out.println("NoDisplay: " + NO_DISPLAY);
			System.out.println("ComputeRCoords: " + COMPUTE_RCOORDS);
			System.out.println("DSP_MAP: " + DSP_MAP);

			//display_info();
		    }

		    if ((mod & MouseEvent.CTRL_MASK) != 0) {
			if (c == 'X')
			    graphPanel.cutSelection();
			else if (c == 'A')
			    selectAll(true, true);
			else if (c == 'C')
			    graphPanel.copySelection();
			else if (c == 'V')
			    graphPanel.pasteSelection(getPreviousGraphElementAt(curPosX, curPosY));
			else if (c == 'S')
			    selectAndCopyAll();
			else if (c == 'U')
			    selectAll(false, true);
			else if (c == 'I' || c == 'M')
			    selectInfoPanel(c);
		    }

		    // info scrolling
		    if (c == KeyEvent.VK_DOWN || c == KeyEvent.VK_KP_DOWN)
			view.getInfoPanel().scrollDown(false);
		    else if (c == KeyEvent.VK_PAGE_DOWN)
			view.getInfoPanel().scrollDown(true);
		    else if (c == KeyEvent.VK_UP || c == KeyEvent.VK_KP_UP)
			view.getInfoPanel().scrollUp(false);
		    else if (c == KeyEvent.VK_PAGE_UP)
			view.getInfoPanel().scrollUp(true);
		    else if (c == KeyEvent.VK_F1) {
			getGraphPanelSet().incrWestYSize(-INCR_WEST_YAXIS);
		    }
		    else if (c == KeyEvent.VK_F2) {
			getGraphPanelSet().incrWestYSize(INCR_WEST_YAXIS);
		    }
		    else if (c == KeyEvent.VK_F3) {
			getGraphPanelSet().incrWestMargin(-INCR_WEST_MARGIN);
		    }
		    else if (c == KeyEvent.VK_F4) {
			getGraphPanelSet().incrWestMargin(INCR_WEST_MARGIN);
		    }
		}
	    });

	addMouseWheelListener(new MouseWheelListener() {
		public void mouseWheelMoved(MouseWheelEvent e) {
		    int mod = e.getModifiers();
		    graphPanel.incrScrollY(e.getWheelRotation(),
					   (mod & MouseEvent.CTRL_MASK) != 0);
		}
	    });
	
	addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    requestFocus();
		    int button = e.getButton();
		    if (button == MouseEvent.BUTTON1) {
			if (isPaintingRegionMode(ASYNC_MODE))
			    return;
			int mod = e.getModifiers();

			Selectable selectable = 
			    getSelectableAt(e.getX(), e.getY());

			if ((mod & MouseEvent.CTRL_MASK) == 0) {
			    // PH requirement: classic selection feel :
			    /*
			      if (selectable == null || !selectable.isSelected())
			    */
			    //selectAll_r(false);
			    view.selectAll(View.ALL, false);
			}

			if (selectable != null) {
			    if ((mod & MouseEvent.SHIFT_MASK) != 0) {
				if (selectable instanceof GraphElement) {
				    if (shiftRef == null)
					shiftRef = (GraphElement)selectable;
				    selectBetween(shiftRef, (GraphElement)selectable);
				}
			    }
			    else {
				if ((mod & MouseEvent.CTRL_MASK) == 0 ||
				    !selectable.isSelected()) {
				    setSelected(selectable, true);
				}
				else {
				    setSelected(selectable, false);
				}

				if (selectable instanceof GraphElement)
				    shiftRef = (GraphElement)selectable;
			    }
			}
			else
			    shiftRef = null;

			getGraphPanelSet().selectionSync();
			if (!useOptSel()) {
			    getGraphPanelSet().sync(false);
			}
		    }
		    else if (button == MouseEvent.BUTTON3) {
			int x = e.getX(), y = e.getY();

			curPX = x;
			curPY = y;

			DataElement elem;
			Probe probe;
			Mark mark;
			GraphElement graphElement;
			Region region;
			JPopupMenu popup;

			if ((elem = getDataElementAt(x, y)) != null) {
			    popup = createPopupMenu(elem,
						    getGraphElementAt(x, y));
			}
			else if ((probe = getProbeAt(x, y)) != null) {
			    popup = createPopupMenu(probe,
						    getGraphElementAt(x, y));
			}
			else if ((graphElement = getGraphElementAt(x, y)) !=
				 null) {
			    popup = createPopupMenu(graphElement);
			}
			else {
			    mark = getMarkAt(x, y);
			    if (mark != null &&	mark.getRegion() == null)
				popup = createPopupMenu(mark);
			    else {
				Vector region_v = getRegionsAt(x, y);
				int sz = region_v.size();
				if (sz == 0) {
				    if (mark != null)
					popup = createPopupMenu(mark);
				    else
					popup = createPopupMenu();
				}
				else if (sz == 1)
				    popup = createPopupMenu((Region)region_v.get(0));
				else {
				    popup = new JPopupMenu();
				    popup.add("Region List");
				    popup.addSeparator();
				    for (int n = 0; n < sz; n++) {
					region = (Region)region_v.get(n);
					String suffix = "#" + Utils.toString(n+1);
					JMenu menu = new JMenu
					    (getRegionTitle(region, suffix));
					menu.setBackground(region.getColor());
					JMenu_PopupMenu menu_popup =
					    new JMenu_PopupMenu(menu);
					createMenu(region, menu_popup, suffix);
					popup.add(menu);
				    }
				}
			    }
			}

			if (popup != null)
			    popup.show(e.getComponent(), e.getX(), e.getY());
		    }
		}

		public void mouseEntered(MouseEvent e) {
		    isIn = true;
		    //requestFocus();
		}

		public void mouseExited(MouseEvent e) {
		    graphPanel.unsetPosX();
		    isIn = false;
		}
	    });

	addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseMoved(MouseEvent e) {
		    manageInfo(e.getX(), e.getY());
		}
	    });
    }

    int setPaintingRegionMode(int sync_mode) {
	int o_painting_region_mode = painting_region_mode;
	painting_region_mode = sync_mode;
	return o_painting_region_mode;
    }


    boolean isPaintingRegionMode(int sync_mode) {
	return painting_region_mode == sync_mode;
    }

    private void selectBetween(GraphElement select1, GraphElement select2) {
	selectAll(false);

	if (select1 == select2) {
	    //select1.setSelected(true);
	    setSelected(select1, true);

	}

	boolean select = false;
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    if (graphElement == select1 || graphElement == select2) {
		//if (select) {graphElement.setSelected(true); return;}
		if (select) {
		    setSelected(graphElement, true);
		    return;
		}
		select = true;
	    }

	    if (select) {
		//graphElement.setSelected(true);
		setSelected(graphElement, true);
	    }
	}
    }

    private void selectInfoPanel(int c) {
	if (c == 'I')
	    graphPanel.setInfoPanel(InfoPanel.INFO_PANEL);
	else if (c == 'M')
	    graphPanel.setInfoPanel(InfoPanel.MINIMAP_PANEL);
    }

    private Mark getMarkAt_exact(double posx) {
	if (isPaintingRegionMode(ASYNC_MODE))
	    return null;
	int size = getMarks().size();
	for (int i = 0; i < size; i++) {
	    Mark mark = (Mark)getMarks().get(i);
	    double mposx = mark.getPosX();
	    if (mposx == posx)
		return mark;
	}
	return null;
    }

    private Mark getMarkAt(int rx, int ry) {
	if (isPaintingRegionMode(ASYNC_MODE))
	    return null;
	if (sizeSets == 0) return null;
	int size = getMarks().size();
	for (int i = 0; i < size; i++) {
	    Mark mark = (Mark)getMarks().get(i);
	    double mrx = mark.getRX(this);
	    if (rx >= mrx-EPSILON && rx <= mrx+EPSILON)
		return mark;
	}
	return null;
    }

    private Vector getMarksAt(int rx, int ry) {
	Vector v = new Vector();
	if (isPaintingRegionMode(ASYNC_MODE))
	    return v;
	if (sizeSets == 0)
	    return v;
	int size = getMarks().size();
	for (int i = 0; i < size; i++) {
	    Mark mark = (Mark)getMarks().get(i);
	    double mrx = mark.getRX(this);
	    if (rx >= mrx-EPSILON && rx <= mrx+EPSILON)
		v.add(mark);
	}
	return v;
    }

    private void setPosX(String tag) {
	graphPanel.setPosX((long)getPosX(0),
			   (long)getPosX(getTSize().width),
			   (isIn ? (long)getPosX(getTCurPosX()) : -1));
    }


    private void manageInfoInPopupMenu() {
	boolean _isIn = isIn;
	isIn = true;
	manageInfo(curPX, curPY);
	isIn = _isIn;
    }

    private void manageInfo() {
	boolean _isIn = isIn;
	isIn = true;
	manageInfo(curPosX, curPosY);
	isIn = _isIn;
    }

    private void manageInfo(int rx, int ry) {
	if (!isIn) {
	    return;
	}

	boolean pinnedUp;
	GraphElement graphElem;
	PropertyElement elem;
	Probe probe;
	Region region;
	Mark mark;

	if (pinnedUpElem != null) {
	    elem = pinnedUpElem;
	    graphElem = pinnedUpGraphElem;
	    probe = null;
	    region = null;
	    mark = null;
	    pinnedUp = true;
	}
	else if (pinnedUpProbe != null) {
	    probe = pinnedUpProbe;
	    graphElem = pinnedUpGraphElem;
	    elem = null;
	    region = null;
	    mark = null;
	    pinnedUp = true;
	}
	else if (pinnedUpGraphElem != null) {
	    elem = null;
	    probe = null;
	    graphElem = pinnedUpGraphElem;
	    region = null;
	    mark = null;
	    pinnedUp = true;
	}
	else if (pinnedUpRegion != null) {
	    elem = null;
	    probe = null;
	    graphElem = null;
	    region = pinnedUpRegion;
	    mark = null;
	    pinnedUp = true;
	}
	else {
	    graphElem = getGraphElementAt(rx, ry);
	    elem = getDataElementAt(rx, ry);
	    probe = getProbeAt(rx, ry);
	    region = getRegionAt(rx, ry);
	    mark = getMarkAt(rx, ry);
	    pinnedUp = false;
	}

	curPosX = rx;
	curPosY = ry;

	if (probe != null) {
	    assert elem == null;
	    elem = new PropertyElement(probe);
	}

	graphPanel.setInfoWindow(graphElem, elem, region, mark,
				 getBoundDataElementsAt(rx, graphElem),
				 getVX(rx), pinnedUp);

	if (probe != null && probe != pinnedUpProbe) {
	    probe.release();
	}

	setPosX("manageInfo");
    }

    private Region getRegionAt(int rx, int ry) {
	if (isPaintingRegionMode(ASYNC_MODE))
	    return null;
	if (sizeSets == 0)
	    return null;
	int size = getRegions().size();
	for (int i = 0; i < size; i++) {
	    Region region = (Region)getRegions().get(i);
	    double mbegrx = region.getBegin().getRX(this);
	    double mendrx = region.getEnd().getRX(this);
	    if (rx >= mbegrx-EPSILON && rx <= mendrx+EPSILON)
		return region;
	}
	return null;
    }

    private Vector getRegionsAt(int rx, int ry) {
	if (isPaintingRegionMode(ASYNC_MODE))
	    return null;
	Vector v = new Vector();
	if (sizeSets == 0) return v;
	int size = getRegions().size();
	for (int i = 0; i < size; i++) {
	    Region region = (Region)getRegions().get(i);
	    double mbegrx = region.getBegin().getRX(this);
	    double mendrx = region.getEnd().getRX(this);
	    if (rx >= mbegrx-EPSILON && rx <= mendrx+EPSILON)
		v.add(region);
	}

	return v;
    }

    GraphElement getGraphElement(int m) {
	if (m >= sizeSets) return null;
	return (GraphElement)graphElements.get(m);
    }

    DataSet getDataSet(int m) {
	if (m >= sizeSets) return null;
	GraphElement graphElement = (GraphElement)graphElements.get(m);
	return graphElement.asDataSet();
    }

    LinkedList getGraphElements() {
	return graphElements;
    }

    private boolean isTrans(GraphElement graphElement) {
	String type = (String)graphElement.getPropertyValue(VAMPProperties.TypeProp);
	return type.equals(VAMPConstants.TRANSCRIPTOME_TYPE);
    }

    void computeRCoords() {
	_computeRCoords(false);
    }

    void _computeRCoords(boolean force) {
	graphPanel.getDefaultGraphElementDisplayer().
	    computeRCoords(this, force);
    }

    static final boolean AVOID_REAPPLY_OP = true;

    void syncGraphElements(boolean readaptSize) {
	if (AVOID_REAPPLY_OP) {
	    /*
	    if (graphPanel.getAutoApplyDSLOP() != null) {
		System.err.println("WARNING: syncing without autoapplying");
	    }
	    */
	    syncGraphElements(readaptSize, false, true);
	}
	else {
	    syncGraphElements(readaptSize, true, true);
	}
    }

    void syncGraphElements(boolean readaptSize, boolean applyOP,
			   boolean warn) {
	if (syncing)
	    return;

	syncing = true;
	setRotated(graphPanel.getDefaultGraphElementDisplayer().isRotated());
	setGraphElements(graphElements, applyOP, warn);
	if (readaptSize) {
	    readaptSize();
	}
	syncing = false;
	computeSelectedGraphElements();
    }

    boolean areMarksEnabled() {
	return getTemplateDSorProfile() != null;
    }

    boolean setGraphElements(LinkedList graphElements) {
	return setGraphElements(graphElements, true, true);
    }

    boolean setGraphElements(LinkedList graphElements, boolean applyOP,
			     boolean warn) {
	if (applyOP && graphElements.size() > 0) {
	    //System.out.println("applyOP: " + applyOP);
	    //(new Exception()).printStackTrace();
	    if (!view.checkAutoApplyDSLOP(graphPanel, graphElements)) {
		GraphElementListOperation op = graphPanel.getAutoApplyDSLOP();
		InfoDialog.pop(getGlobalContext(),
			       "Error applying operation " + op.getName() +
			       " on current panel");
		//beep();
		return false;
	    }

	    graphElements = view.autoApplyDSLOP(graphPanel, graphElements);
	    if (graphElements == null) {
		System.err.println("INTERNAL ERROR #283: " +
				   "View.autoApplyDSLOP() should not return null");
		return false;
	    }

	    if (!graphPanel.checkSetGraphElements(graphElements)) {
		InfoDialog.pop(getGlobalContext(),
			       "Profiles or graphic elements are not compatible with this panel: use another type of view");
		beep();
		return false;
	    }

	    if (warn)
		graphPanel.warnGraphElements(getGlobalContext(), graphElements);
	}

	/*
	for (int n = 0; n < graphElements.size(); n++)
	    System.out.println("srcURL: " + ((GraphElement)graphElements.get(n)).getSourceURL());
	*/

	this.graphElements = graphElements;
	sizeSets = this.graphElements.size();
	this.templateDS = _getTemplateDS();
	this.templateProfile = _getTemplateProfile();

	getGraphPanelSet().changeGraphElements();

	// added 13/12/04
	graphPanel.syncLinkedPaneY();

	marksEnabled = areMarksEnabled();
	computeGraphElements();
	shiftRef = null;
	setUpdate(true);

	invalidateOffscreen();

	if (!self_dropping)
	    reinitWestYSize();
	return true;
    }

    private void computeGraphElementVBounds(double scaleY) {
	computeGraphElementVBounds(this, scaleY);
    }

    private void computeGraphElements() {
	computeGraphElements(this);
    }

    void computeGraphElementVBounds(GraphCanvas canvas,
				    double scaleY) {
	graphPanel.getDefaultGraphElementDisplayer().
	    computeGraphElementVBounds(canvas, scaleY);
    }

    void computeGraphElements(GraphCanvas canvas) {
	graphPanel.getDefaultGraphElementDisplayer().
	    computeGraphElements(canvas);
    }

    double getMiddle(Rectangle2D.Double rBounds) {
	return getTRX((int)(rBounds.x + rBounds.width/2),
		      (int)(rBounds.y + rBounds.height/2));
    }

    DataElementRange getBoundDataElementsAt(int rx) {
	return getBoundDataElementsAt(rx, null);
    }

    double getPosX() {
	return getPosX(curPosX);
    }

    double getPosX(int rx) {
	double vx = getVX(rx);
	DataSet dataSet = getTemplateDS();
	if (dataSet == null || !dataSet.isFullImported() ||
	    graphPanel.getDefaultGraphElementDisplayer() == null ||
	    !graphPanel.getDefaultGraphElementDisplayer().isVXRelocated())
	    return vx;
	return dataSet.vxToPosX(vx);
    }

    DataElementRange getBoundDataElementsAt(int rx,
					    GraphElement graphElement) {
	if (sizeSets == 0) return null;

	if (graphElement == null)
	    graphElement = getTemplateDS();

	if (graphElement == null)
	    return null;

	// should use polymorphism instead
	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null) return null;

	DataElement data[] = dataSet.getData();
	if (data.length == 0) return null;
	Rectangle2D.Double rBounds = data[0].getRBounds(dataSet);
	double middle = getMiddle(rBounds);
	if (rx < middle) {
	    double rx0 = getRX(0, 0);
	    double percent = (rx - rx0) / (middle - rx0);
	    return new DataElementRange(dataSet, -1, 0, percent);
	}

	for (int n = 0; n < data.length; n++) {
	    rBounds = data[n].getRBounds(dataSet);
	    middle = getMiddle(rBounds);
	    if (rx >= middle-2 && rx <= middle+2) {
		return new DataElementRange(dataSet, n);
	    }

	    if (n < data.length - 1) {
		Rectangle2D.Double rBoundsN = data[n+1].getRBounds(dataSet);
		double middleN = getMiddle(rBoundsN);
		if (rx >= middle && rx < middleN) {
		    double percent = (rx - middle) / (middleN - middle);
		    return new DataElementRange(dataSet, n, n+1, percent);
		}
	    }
	}

	return new DataElementRange(dataSet, data.length-1);
    }

    DataElementRange getBoundDataElements() {
	return getBoundDataElements(null);
    }

    DataElementRange getBoundDataElements(GraphElement baseGraphElement) {
	return getBoundDataElementsAt(getTCurPosX(), baseGraphElement);
    }

    static final boolean NEW_GET_DATA_ELEM = true;

    Probe getProbeAt(int rx, int ry) {
	GraphElement graphElem = getGraphElementAt(rx, ry);
	if (graphElem == null) {
	    return null;
	}
	Profile profile = graphElem.asProfile();
	if (profile == null) {
	    return null;
	}

	Painter painter = new Painter(profile.getGraphicProfile());
	fr.curie.vamp.gui.Scale gscale = painter.makeScale(this, profile);
	Probe p = painter.getProbeAt(rx, ry, gscale);
	/*
	  fr.curie.vamp.gui.Scale gscale = new fr.curie.vamp.gui.Scale(getScale().getScaleX(), (int)getRX(0, 0), getScale().getScaleY(), (int)getRY(profile.getVBounds().y - profile.getVBounds().height/2));
	Probe p = (new Painter(profile.getGraphicProfile())).getProbeAt(rx, ry, gscale);
	*/
	return p;
    }

    DataElement getDataElementAt(int rx, int ry) {
	if (NEW_GET_DATA_ELEM) {
	    // 7/02/05
	    GraphElement graphElem = getGraphElementAt(rx, ry);
	    if (graphElem == null)
		return null;
	    DataSet dataSet = graphElem.asDataSet();
	    if (dataSet == null)
		return null;

	    DataElement data[] = dataSet.getData();
	    for (int n = 0; n < data.length; n++) {
		Rectangle2D.Double rBounds = data[n].getRBounds(dataSet);
		if (rx >= rBounds.x && rx <= rBounds.x + rBounds.width &&
		    ry >= rBounds.y && ry <= rBounds.y + rBounds.height) {
		    if (!dataSet.isVisible()) {
			System.out.println("possible error on " + dataSet.getID());
		    }
		    return data[n];
		}
	    }
	    return null;
	}

	if (sizeSets == 0) return null;
	for (int m = 0; m < sizeSets; m++) {
	    DataSet dataSet = getDataSet(m);
	    if (dataSet == null)
		continue;

	    DataElement data[] = dataSet.getData();
	    for (int n = 0; n < data.length; n++) {
		Rectangle2D.Double rBounds = data[n].getRBounds(dataSet);
		if (rx >= rBounds.x && rx <= rBounds.x + rBounds.width &&
		    ry >= rBounds.y && ry <= rBounds.y + rBounds.height) {
		    if (!dataSet.isVisible()) {
			System.out.println("possible error on " + dataSet.getID());
		    }
		    return data[n];
		}
	    }
	}
	return null;
    }

    Selectable getSelectableAt(int rx, int ry) {
	/*
	if (isPaintingRegionMode(ASYNC_MODE))
	    return null;
	*/
	Mark mark;
	GraphElement graphElement;
	if ((mark = getMarkAt(rx, ry)) != null)
	    return mark;
	if ((graphElement = getGraphElementAt(rx, ry)) != null)
	    return graphElement;
	return getRegionAt(rx, ry);
    }

    GraphElement getGraphElementAt(int rx, int ry) {
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    Rectangle2D.Double rBounds = graphElement.getRBounds();
	    int minrx = (int)rBounds.x;
	    int minry = (int)(rBounds.y - rBounds.height);
	    // used to include thresholded dataelements
	    minry -= 5; // 3/02/05
	    int maxrx = (int)(rBounds.x + rBounds.width);
	    int maxry = (int)rBounds.y;
	    if (rx >= minrx && rx <= maxrx && ry >= minry && ry <= maxry)
		return graphElement;
	}
	return null;
    }

    GraphElement getPreviousGraphElementAt(int rx, int _ry) {
	int prevmaxry = 0;
	int t_ry = getTRY(rx, _ry);
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    Rectangle2D.Double rBounds = graphElement.getRBounds();

	    int minrx = (int)rBounds.x;
	    int minry = (int)(rBounds.y - rBounds.height);
	    int maxrx = (int)(rBounds.x + rBounds.width);
	    int maxry = (int)rBounds.y;

	    minry = getTRY(minrx, minry);
	    maxry = getTRY(maxrx, maxry);

	    if (t_ry >= minry && t_ry <= maxry) {
		return graphElement;
	    }
	    if (prevmaxry != 0) {
		if (t_ry > prevmaxry && t_ry < minry) {
		    return graphElement;
		}
	    }
	    else {
		if (t_ry < minry) {
		    return graphElement;
		}
	    }
	    prevmaxry = maxry;
	}
	return null;
    }

    public Rectangle2D.Double getVBounds(int m) {
	return getGraphElement(m).getVBounds();
    }

    public double getMaxX() {return maxX;}
    public double getMaxY() {return maxY;}

    public double getMinX() {return minX;}
    public double getMinY() {return minY;}

    public void resetMaxX() {maxX = Double.MIN_VALUE;}
    public void resetMinX() {minX = Double.MAX_VALUE;}

    public void resetMaxY() {maxY = Double.MIN_VALUE;}
    public void resetMinY() {minY = Double.MAX_VALUE;}

    public void setMaxX(double maxX) {
	if (maxX > this.maxX)
	    this.maxX = maxX;
	/*
	System.out.println("setMaxX: " + maxX + " -> " + this.maxX);
	(new Exception()).printStackTrace();
	*/
    }

    public void setMaxY(double maxY) {
	if (maxY > this.maxY)
	    this.maxY = maxY;
    }

    public void setMinX(double minX) {
	if (minX < this.minX)
	    this.minX = minX;
	//System.out.println("setMinX: " + minX + " -> " + this.minX);
    }

    public void setMinY(double minY) {
	if (minY < this.minY)
	    this.minY = minY;
    }

    public double getTopMaxY() {
	return getGraphElement(sizeSets-1).getVBounds().y;
    }

    public void center(Point2D.Double vp) {
	if (vp == null) return;
	super.center(vp, true);
    }

    void adaptScroll() {
	graphPanel.adaptScrollBars();
	graphPanel.setScrollX(getPercentX());
	graphPanel.setScrollY(getPercentY());
    }

    public void paintGraphElement(Graphics g, GraphElement graphElement, Color color) {
	Rectangle2D.Double rBounds = graphElement.getRBounds();
	g.setColor(color);
	Rectangle2D.Double bounds = makeVisibleRect
	    (new Rectangle2D.Double
	     (rBounds.x, rBounds.y-rBounds.height,
	      rBounds.width, rBounds.height));
	g.fillRect((int)bounds.x, (int)bounds.y,
		    (int)bounds.width, (int)bounds.height);
    }

    public void paintSelect(Graphics g, GraphElement graphElement) {
	paintGraphElement(g, graphElement, VAMPResources.getColor
			  (VAMPResources.DATASET_SELECTED_FG));
    }

    public void paintLightImported(Graphics g, GraphElement graphElement) {
	paintGraphElement(g, graphElement, VAMPResources.getColor
			  (VAMPUtils.isTranscriptome(graphElement) ?
			   VAMPResources.TRANSCRIPTOME_LIGHT_IMPORTED_FG :
			   VAMPResources.CGH_ARRAY_LIGHT_IMPORTED_FG));
    }

    public void paintPinnedUp(Graphics g, GraphElement graphElement) {
	paintGraphElement(g, graphElement, VAMPResources.getColor
			  (VAMPResources.DATASET_PINNED_UP_FG));
    }

    public void paint(Graphics g) {

	long ms00, ms0, ms1, ms2, ms5=0, ms6;

	ms00 = System.currentTimeMillis();

	Graphics2D g2 = (Graphics2D)g;
	Dimension size = getSize();
	g.setColor(getBGColor());
	g.fillRect(0, 0, size.width, size.height);

	if (PROFILE)
	    System.out.println("paint #0: modified=" + isModified());

	Thread thread = updateGraphElementBounds(g2);

	    ms0 = System.currentTimeMillis();
	if (PROFILE) {
	    System.out.println("paint #1: " + (ms0-ms00) + "ms");
	}

	//boolean offscreen = false;

	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    /*
	      if (modified) {
	      getGraphElementDisplayer(graphElement).computeVBounds(this, g2, graphElement, m);
	      graphElement.setRBounds(getRRect(graphElement.getPaintVBounds()));
	      graphElement.setVisible(isVR_Visible(graphElement.getVBounds()));
	      if (graphElement.isOffScreenable() &&
	      graphElement.isOffScreenValid())
	      offscreen = true;
	      }
	    */

	    displayGraphElementState(g2, graphElement);
	    /*
	    if (graphElement.isSelected())
		paintSelect(g2, graphElement);
	    else if (graphElement.isPinnedUp())
		paintPinnedUp(g2, graphElement);
	    else if (!graphElement.isFullImported())
		paintLightImported(g2, graphElement);
	    */
	}

	ms1 = System.currentTimeMillis();
	if (PROFILE) {
	    System.out.println("paint #2: " + (ms1-ms0) + "ms");
	}

	paintMarksAndRegions(g, null);

	// modified = false;

	ms2 = System.currentTimeMillis();
	long ms_dsp = 0;
	long ms_axi = 0;

	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    long msn = System.currentTimeMillis();
	    getGraphElementDisplayer(graphElement).display(this, g2, graphElement, m, null);

	    if (PROFILE)
		ms_dsp += System.currentTimeMillis() - msn;

	    if (!graphElement.isVisible())
		continue;

	    msn = System.currentTimeMillis();
	    getAxisDisplayer(graphElement).display(this, g2, graphElement, m, null);

	    if (PROFILE)
		ms_axi += System.currentTimeMillis() - msn;
	}

	if (PROFILE)
	    System.out.println("paint #3: " +
			       "ms_dsp " + ms_dsp + ", " +
			       "ms_axi " + ms_axi);

	if (PROFILE) {
	    ms5 = System.currentTimeMillis();
	    System.out.println("paint #4: " + (ms5-ms2) + "ms");
	}

	setPosX("paint");

	view.repaintPrintPreviewer(false);
	if (PROFILE) {
	    ms6 = System.currentTimeMillis();
	    System.out.println("paint #5: " + (ms6-ms5) + "ms\n");
	}

	getGraphPanel().repaintAxis();

	String bgImg = getGraphPanel().getPanelProfile().getBGImg();
	if (bgImg != null)
	    Utils.drawImage(g, getToolkit(), bgImg, 0, 0, -1, false, false);

	if (thread != null)
	    SwingUtilities.invokeLater(thread);
    }

    Thread updateGraphElementBounds(Graphics2D g2) {
	// je pense que l'on peut reconnecter ce test et le setModified(false)
	// final...

	// 21/01/05
	if (!isModified())
	    return null;

	long ms0 = System.currentTimeMillis(), ms1, ms2;
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    getGraphElementDisplayer(graphElement).computeVBounds(this, g2, graphElement, m);
	    graphElement.setRBounds(getRRect(graphElement.getPaintVBounds()));
	    graphElement.setVisible(isVR_Visible(graphElement.getVBounds()));
	}

	ms1 = System.currentTimeMillis();
	if (PROFILE) {
	    System.out.println("paint #0.1: " + (ms1-ms0) + "ms");
	}

	Thread thread = null;

	boolean must_compute_immediate = false;
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    if (graphElement.isVisible() &&
		(!graphElement.isOffScreenable() ||
		 !graphElement.isOffScreenValid())) {
		if (GraphCanvas.VERBOSE)
		    System.out.println("MUST COMPUTE RCOORDS");
		computeRCoords();
		must_compute_immediate = true;
		break;
	    }
	}

	if (!must_compute_immediate) {
	    computeRCoords();
	    // 5/05/06:
	    // must be synchrone !!
	    /*
	    thread = new Thread() {
		    public void run() {
			computeRCoords();
		    }
		};
	    SwingUtilities.invokeLater(thread);
	    thread = null;
	    */
	}


	if (PROFILE) {
	    ms2 = System.currentTimeMillis();
	    System.out.println("paint #0.2: " + (ms2-ms1) + "ms");
	}

	// 21/01/05
	setModified(false);
	return thread;
    }

    void paintXAxis(Graphics2D g2, Axis xaxis) {
	// 31/03/06: added for testing
	// disconnected 4/05/06
	/*
	if (isModified())
	    computeRCoords();
	*/

	if (!isRotated()) {
	    if (sizeSets == 0)
		graphPanel.getDefaultAxisDisplayer().displayInfo(this, xaxis, g2);
	    else {
		boolean found = false;
		for (int m = 0; m < sizeSets; m++) {
		    GraphElement graphElement = getGraphElement(m);
		    if (isVR_Visible(graphElement.getVBounds())) {
			getAxisDisplayer(graphElement).displayXAxis(this, xaxis, g2,
								    graphElement,
								    m, null);
			found = true;
			break;
		    }
		}

		if (!found && sizeSets > 0) {
		    GraphElement graphElement = getGraphElement(0);
		    getAxisDisplayer(graphElement).displayXAxis(this, xaxis, g2,
								graphElement,
								0, null);
		}
	    }
	}
	else {
	    if (sizeSets == 0)
		graphPanel.getDefaultAxisDisplayer().displayInfo(this, xaxis, g2);
	    else
		for (int m = 0; m < sizeSets; m++) {
		    GraphElement graphElement = getGraphElement(m);
		    if (!isVR_Visible(graphElement.getVBounds()))
			continue;
		    getAxisDisplayer(graphElement).displayXAxis(this, xaxis, g2,
								graphElement,
								m, null);
		}
	}
    }

    void paintYAxis(Graphics2D g2, Axis yaxis) {
	// disconnected 4/05/06
	//updateGraphElementBounds(g2);

	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    // 13/12/04: changed isVR_Visible to isVR_Visible_Y
	    if (!isVR_Visible_Y(graphElement.getVBounds())) continue;
	    getAxisDisplayer(graphElement).displayYAxis(this, yaxis,
							g2,
							graphElement,
							m, null);
	}
    }

    // 8/3/05
    // should be performed in a AnnotDisplayer :
    // abstract class AnnotDisplayer
    // class StandardAnnotDisplayer extends AnnotDisplayer {
    // }
    void paintAnnotAxis(Graphics2D g2, AnnotAxis annotAxis) {
	updateGraphElementBounds(g2);
	StandardAnnotDisplayer annotDisplayer = StandardAnnotDisplayer.getInstance();
	annotDisplayer.init();
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    if (!isVR_Visible_Y(graphElement.getVBounds())) continue;
	    annotDisplayer.displayAnnots(this, annotAxis, g2, graphElement, m, null);
	}
    }

    public void setScaleY(double scaleY) {
	computeGraphElementVBounds(scaleY);
	setVCenter();
	super.setScaleY(scaleY);

	// test !
	graphPanel.adaptScrollBars();
    }

    private void setVCenter() {
	if (centeredElem != null) {
	    double vy = VAMPUtils.getThresholdVY(centeredGraphElem, centeredElem);
	    //if (centeredGraphElem.asDataSet() != null)
	    //vy = centeredGraphElem.asDataSet().yTransform(vy);
	    vy = centeredGraphElem.yTransform(vy);

	    /*
	    System.out.println("centerElem: " + 
			       centeredElem.getVMiddle() + ", " +
			       centeredGraphElem.getVBounds().y + ", " +
			       centeredElem.getVY() + ", " +
			       centeredGraphElem.asDataSet().yTransform(centeredElem.getVY()) + ", " +
			       centeredGraphElem.asDataSet().yTransform_1(centeredElem.getVY()) + ", " +
			       minY);
	    */

	    /*
	    graphPanel.setVCenter
		(new Point2D.Double(centeredElem.getVMiddle(),
				    centeredGraphElem.getVBounds().y-
				    centeredElem.getVY()+minY));
	    */
	    graphPanel.setVCenter
		(new Point2D.Double(centeredElem.getVMiddle(centeredGraphElem),
				    centeredGraphElem.getVBounds().y-
				    vy + minY));
	}
	else if (centeredProbe != null) {
	    double vy = VAMPUtils.getThresholdVY(centeredGraphElem, centeredProbe);
	    //double vy = centeredProbe.getRatio(); // must threshold
	    vy = centeredGraphElem.yTransform(vy);

	    graphPanel.setVCenter
		(new Point2D.Double(centeredProbe.getPanGenPosMiddle(centeredGraphElem.asProfile()),
				    centeredGraphElem.getVBounds().y-
				    vy + minY));
	}
    }

    static final int GRAPH_ELEM_MASK = 0x1;
    static final int REGION_MASK = 0x2;
    static final int MARK_MASK = 0x4;
    static final int ALL_MASK = (GRAPH_ELEM_MASK | REGION_MASK | MARK_MASK);
    static final int IMMEDIATE_MASK = 0x0; // 0x0 means that it is disconnected
    //    static final int IMMEDIATE_MASK = 0x1000;

    void selectAndCopyAll() {
	selectAll(true, true, ALL_MASK);
    }

    void selectAll(boolean select) {
	selectAll(select, false);
    }

    void selectAll(boolean select, boolean immediate) {
	selectAll(select, false, ALL_MASK|IMMEDIATE_MASK);
    }

    void selectAndCopyAllGraphElements() {
	selectAll(true, true, GRAPH_ELEM_MASK);
    }

    void selectAllGraphElements(boolean select, boolean immediate) {
	selectAll(select, false, GRAPH_ELEM_MASK|IMMEDIATE_MASK);
    }

    void selectAndCopyAllRegions() {
	selectAll(true, true, REGION_MASK);
    }

    void selectAllRegions(boolean select) {
	selectAll(select, false, REGION_MASK);
    }

    void selectAndCopyAllMarks() {
	selectAll(true, true, MARK_MASK);
    }

    void selectAllMarks(boolean select) {
	selectAll(select, false, MARK_MASK);
    }

    private void selectAll(boolean select, boolean copy, int mask) {
	selectAll_r(select, mask);
	if (copy) {
	    cutOrCopy(Pasteable.COPY);
	}

	if (!useOptSel() || (mask & IMMEDIATE_MASK) != 0) {
	    repaint();
	}
    }

    private void selectAll_r(boolean select, int mask) {
	if ((mask & GRAPH_ELEM_MASK) != 0)
	    selectAll(graphElements, select, (mask & IMMEDIATE_MASK) != 0);

	if ((mask & REGION_MASK) != 0)
	    selectAll(getRegions(), select, (mask & IMMEDIATE_MASK) != 0);

	if ((mask & MARK_MASK) != 0)
	    selectAll(getMarks(), select, (mask & IMMEDIATE_MASK) != 0);
    }

    private void selectAll(LinkedList list, boolean select, boolean immediate) {
	int size = list.size();
	for (int i = 0; i < size; i++)
	    setSelected(((Selectable)list.get(i)), select, immediate);
	getGraphPanelSet().selectionSync();
    }

    double getVPadY() {return vPadY;}
    void setVPadY(double vPadY) {this.vPadY = vPadY;}

    JMenuItem createPropEditMenu(PropertyElement elem) {
	return createPropEditMenu(elem, null);
    }

    JMenuItem createPropEditMenu(PropertyElement elem, Probe probe) {
	JMenuItem menuItem = new JMenuItem(probe != null ? "Show properties" : "Edit properties");
	menuItem.addActionListener(new ActionListenerWrapper(new Object[]{elem, probe}) {
		public void actionPerformed(ActionEvent e) {
		    Object o[] = (Object[])getValue();
		    PropertyElement elem = (PropertyElement)o[0];
		    Probe probe = (Probe)o[1];
		    PropertyEditDialog.pop(getGlobalContext(), view, elem, probe);
		}
	    });
	return menuItem;
    }

    void createMarkMenu(JPopupMenu popup, Probe probe,
			Profile profile) {
	if (probe != null) {
	    createMarkMenu(popup, probe.getPanGenPos(profile) + probe.getSize()/2, profile);
	}
	else {
	    createMarkMenu(popup, Long.MAX_VALUE, profile);
	}
    }

    void createMarkMenu(JPopupMenu popup, DataElement data,
			GraphElement graphElem) {
	/*
	if (!marksEnabled) {
	    return;
	}
	*/

	if (data != null) {
	    createMarkMenu(popup, (long)data.getPosMiddle(graphElem), graphElem);
	}
	else {
	    createMarkMenu(popup, Long.MAX_VALUE, graphElem);
	}

	/*
	JMenuItem menuItem;

	menuItem = new JMenuItem("Put landmark");
	if (isPaintingRegionMode(ASYNC_MODE))
	    menuItem.setEnabled(false);

	menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		public void actionPerformed(ActionEvent e) {
		    Object o[] = (Object[])getValue();
		    DataElement data = (DataElement)o[0];
		    if (data != null)
			getGraphPanelSet().addMark(which, data.getPosMiddle((GraphElement)o[1]));
		    else
			getGraphPanelSet().addMark(which, getPosX());
		}
	    });
	popup.add(menuItem);

	menuItem = new JMenuItem("Put landmark and center");
	if (isPaintingRegionMode(ASYNC_MODE))
	    menuItem.setEnabled(false);
	menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		public void actionPerformed(ActionEvent e) {
		    Object o[] = (Object[])getValue();
		    DataElement data = (DataElement)o[0];
		    Mark mark;
		    if (data != null)
			mark = getGraphPanelSet().addMark(which,
							  data.getPosMiddle((GraphElement)o[1]));
		    else
			mark = getGraphPanelSet().addMark(which, getPosX());

		    getGraphPanelSet().clearCenter(which);
		    centeredMark = mark;
		    graphPanel.centerOnMark(mark);
		}
	    });
	popup.add(menuItem);

	menuItem = new JMenuItem("Put begin landmark");
	if (isPaintingRegionMode(ASYNC_MODE))
	    menuItem.setEnabled(false);
	menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		public void actionPerformed(ActionEvent e) {
		    Object o[] = (Object[])getValue();
		    DataElement data = (DataElement)o[0];
		    if (data != null)
			getGraphPanelSet().addMarkBegin(which,
							data.getPosMiddle((GraphElement)o[1]));
		    else
			getGraphPanelSet().addMarkBegin(which, getPosX());
		}
	    });
	popup.add(menuItem);

	if (getGraphPanelSet().getMarkBegin() != null) {
	    menuItem = new JMenuItem("Put end landmark");
	    if (isPaintingRegionMode(ASYNC_MODE))
		menuItem.setEnabled(false);
	    menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		    public void actionPerformed(ActionEvent e) {
		    Object o[] = (Object[])getValue();
			DataElement data = (DataElement)o[0];
			Mark mark;
			if (data != null)
			    mark = getGraphPanelSet().addMark
				(which, data.getPosMiddle((GraphElement)o[1]));
			else
			    mark = getGraphPanelSet().addMark(which,
							      getPosX());
			getGraphPanelSet().addRegion(which, mark);
		    }
		});
	    popup.add(menuItem);
	}
	*/
    }

    void createMarkMenu(JPopupMenu popup, long pos_middle,
			GraphElement graphElem) {
	if (!marksEnabled) {
	    return;
	}

	JMenuItem menuItem;

	menuItem = new JMenuItem("Put landmark");
	if (isPaintingRegionMode(ASYNC_MODE)) {
	    menuItem.setEnabled(false);
	}

	Long ipos_middle = (pos_middle != Long.MAX_VALUE ? new Long(pos_middle) : null);

	menuItem.addActionListener(new ActionListenerWrapper(ipos_middle) {
		public void actionPerformed(ActionEvent e) {
		    Long ipos_middle = (Long)getValue();
		    if (ipos_middle != null)
			getGraphPanelSet().addMark(which, ipos_middle.longValue());
		    else
			getGraphPanelSet().addMark(which, getPosX());
		}
	    });

	popup.add(menuItem);

	menuItem = new JMenuItem("Put landmark and center");
	if (isPaintingRegionMode(ASYNC_MODE)) {
	    menuItem.setEnabled(false);
	}

	menuItem.addActionListener(new ActionListenerWrapper(ipos_middle) {
		public void actionPerformed(ActionEvent e) {
		    Long ipos_middle = (Long)getValue();
		    Mark mark;
		    if (ipos_middle != null)
			mark = getGraphPanelSet().addMark(which,
							  ipos_middle.longValue());
		    else
			mark = getGraphPanelSet().addMark(which, getPosX());

		    getGraphPanelSet().clearCenter(which);
		    centeredMark = mark;
		    graphPanel.centerOnMark(mark);
		}
	    });
	popup.add(menuItem);

	menuItem = new JMenuItem("Put begin landmark");
	if (isPaintingRegionMode(ASYNC_MODE))
	    menuItem.setEnabled(false);
	menuItem.addActionListener(new ActionListenerWrapper(ipos_middle) {
		public void actionPerformed(ActionEvent e) {
		    Long ipos_middle = (Long)getValue();
		    if (ipos_middle != null)
			getGraphPanelSet().addMarkBegin(which,
							ipos_middle.longValue());
		    else
			getGraphPanelSet().addMarkBegin(which, getPosX());
		}
	    });
	popup.add(menuItem);

	if (getGraphPanelSet().getMarkBegin() != null) {
	    menuItem = new JMenuItem("Put end landmark");

	    if (isPaintingRegionMode(ASYNC_MODE)) {
		menuItem.setEnabled(false);
	    }

	    menuItem.addActionListener(new ActionListenerWrapper(ipos_middle) {
		    public void actionPerformed(ActionEvent e) {
			Long ipos_middle = (Long)getValue();
			Mark mark;
			if (ipos_middle != null) {
			    mark = getGraphPanelSet().addMark
				(which, ipos_middle.longValue());
			}
			else
			    mark = getGraphPanelSet().addMark(which,
							      getPosX());
			getGraphPanelSet().addRegion(which, mark);
		    }
		});

	    popup.add(menuItem);
	}
    }

    private String buildID(String type, String id) {
	if (id == null) {
	    return null;
	}

	int MAX_LEN = 40;
	if (id.length() > MAX_LEN)
	    id = id.substring(0, MAX_LEN) + "...";
	id = id.replaceAll("\n", " ");
	if (type != null)
	    id = type + " : " + id;
	return id;
    }

    JPopupMenu createPopupMenu(Probe probe, GraphElement graphElem) {
        JPopupMenu popup = new JPopupMenu();
	JMenuItem menuItem;

	String type = (String)probe.getProp(VAMPProperties.TypeProp);
	PropertyElement propElem = new PropertyElement(probe);

	String objKey = (String)graphElem.getPropertyValue(VAMPProperties.ObjKeyProp);
	String id = null;
	if (objKey != null) {
	    id = (String)probe.getProp(Property.getProperty(objKey));
	    if (id != null) {
		popup.add(buildID(type, id));
		popup.addSeparator();
	    }
	}

	// centering..

	if (probe == centeredProbe) {
	    menuItem = new JMenuItem("Uncenter " +
				     (type != null ? type : "element"));
	    menuItem.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			getGraphPanelSet().clearCenter(which);
			repaint();
		    }
		});
	}
	else {
	    menuItem = new JMenuItem("Center on " +
				     (type != null ? type : "element"));
	    menuItem.addActionListener(new ActionListenerWrapper(new Object[]{probe, graphElem}) {
		    public void actionPerformed(ActionEvent e) {
			Object o[] = (Object[])getValue();
			Probe probe = (Probe)o[0];
			centeredGraphElem = (GraphElement)o[1];

			getGraphPanelSet().clearCenter(which);

			if (centeredGraphElem != null && centeredGraphElem != null) {
			    centeredProbe = probe;
			    setVCenter();
			}
		    }
		});
	}

	popup.add(menuItem);
	// end of centering...

	// pinned up
	if (probe == pinnedUpProbe) {
	    menuItem = new JMenuItem("Take info down");
	    menuItem.addActionListener(new ActionListenerWrapper(new Object[]{probe, graphElem}) {
		    public void actionPerformed(ActionEvent e) {
			clearPinnedUp();
			manageInfoInPopupMenu();
			repaint();
		    }
		});
	}
	else {
	    menuItem = new JMenuItem("Pin up info");
	    menuItem.addActionListener(new ActionListenerWrapper(new Object[]{probe, graphElem}) {
		    public void actionPerformed(ActionEvent e) {
			Object o[] = (Object[])getValue();
			setPinnedUp((GraphElement)o[1], (Probe)o[0]);
				    
		    }
		});
	}

	popup.add(menuItem);
	// end of centering...

	createMarkMenu(popup, canAddMark(propElem) ? probe : null, graphElem.asProfile());

	menuItem = createPropEditMenu(propElem, probe);
	popup.add(menuItem);

	if (id != null) {
	    menuItem = createCopyIDInSearchPanel(id);
	    popup.add(menuItem);
	}

	if (systemConfig != null) {
	    PropertyElementMenu menu = systemConfig.getMenu("DataElement", type);
	    if (menu != null) {
		menu.buildJPopupMenu(view, popup, propElem);
	    }
	}

	return popup;
    }

    JMenuItem createCopyIDInSearchPanel(String id) {
	JMenuItem menuItem = new JMenuItem("Copy ID in search panel");
	menuItem.addActionListener(new ActionListenerWrapper(id) {
		public void actionPerformed(ActionEvent e) {
		    view.getSearchDataElementPanel().setDataElementText((String)getValue());
		}
	    });		
	return menuItem;
    }

    JPopupMenu createPopupMenu(DataElement data, GraphElement graphElem) {
        JPopupMenu popup = new JPopupMenu();
	JMenuItem menuItem;

	String id = (String)data.getID();
	String type = (String)data.getPropertyValue(VAMPProperties.TypeProp);
	id = buildID(type, id);
	if (id != null) {
	    popup.add(id);
	    popup.addSeparator();
	}

	if (data.isCentered()) {
	    menuItem = new JMenuItem("Uncenter " +
				     (type != null ? type : "element"));
	    menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		    public void actionPerformed(ActionEvent e) {
			getGraphPanelSet().clearCenter(which);
			repaint();
		    }
		});
	}
	else {
	    menuItem = new JMenuItem("Center on " +
				     (type != null ? type : "element"));
	    menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		    public void actionPerformed(ActionEvent e) {
			Object o[] = (Object[])getValue();
			DataElement elem = (DataElement)o[0];

			getGraphPanelSet().clearCenter(which);
			Rectangle2D.Double rBounds = elem.getRBounds((GraphElement)o[1]);
			centeredGraphElem = getGraphElementAt((int)rBounds.x,
							      (int)rBounds.y);

			if (centeredGraphElem == null)
			    System.out.println("no dataset found at " +
					       rBounds.x + ":" +
					       rBounds.y);
			
			if (centeredGraphElem != null) {
			    centeredElem = elem;
			    centeredElem.setCentered(true);
			    setVCenter();
			}
		    }
		});
	}

	popup.add(menuItem);

	if (data.isPinnedUp()) {
	    menuItem = new JMenuItem("Take info down");
	    menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		    public void actionPerformed(ActionEvent e) {
			clearPinnedUp();
			manageInfoInPopupMenu();
			repaint();
		    }
		});
	}
	else {
	    menuItem = new JMenuItem("Pin up info");
	    menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		    public void actionPerformed(ActionEvent e) {
			Object o[] = (Object[])getValue();
			Rectangle2D.Double rBounds = ((DataElement)o[0]).getRBounds((GraphElement)o[1]);
			setPinnedUp(getGraphElementAt((int)rBounds.x,
						      (int)rBounds.y),
				    (DataElement)o[0]);
				    
		    }
		});
	}

	popup.add(menuItem);

	createMarkMenu(popup, canAddMark(data) ? data : null, graphElem);

	menuItem = createPropEditMenu(data);
	popup.add(menuItem);

	/*
	menuItem = new JMenuItem("Copy ID in search panel");
	menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		public void actionPerformed(ActionEvent e) {
		    Object o[] = (Object[])getValue();
		    DataElement elem = (DataElement)o[0];
		    view.getSearchDataElementPanel().setDataElementText((String)elem.getID());
		}
	    });
	*/
	menuItem = createCopyIDInSearchPanel((String)data.getID());
	popup.add(menuItem);

	if (type.equals("Breakpoint Barplot")) {
	    if (data.getPropertyValue(BreakpointFrequencyOP.showAssoProp) ==
		null) {
		menuItem = new JMenuItem("Show associations");
		menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
			public void actionPerformed(ActionEvent e) {
			    Object o[] = (Object[])getValue();
			    DataElement elem = (DataElement)o[0];
			    elem.setPropertyValue(BreakpointFrequencyOP.showAssoProp, "true");
			    getGraphPanelSet().sync(true);
			}
		    });

	    }
	    else {
		menuItem = new JMenuItem("Hide associations");
		menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
			public void actionPerformed(ActionEvent e) {
			    Object o[] = (Object[])getValue();
			    DataElement elem = (DataElement)o[0];
			    elem.removeProperty(BreakpointFrequencyOP.showAssoProp);
			    getGraphPanelSet().sync(true);
			}
		    });
	    }
	    popup.add(menuItem);

	    menuItem = new JMenuItem("Import related arrays");
	    menuItem.addActionListener(new ActionListenerWrapper(new Object[]{data, graphElem}) {
		    public void actionPerformed(ActionEvent e) {
			Object o[] = (Object[])getValue();
			DataElement elem = (DataElement)o[0];
			String URLs[] = (String[])elem.getPropertyValue(BreakpointFrequencyOP.arrayURLProp);
			if (URLs == null)
			    return;

			LinkedList list = new LinkedList();
			for (int n = 0; n < URLs.length; n += 2) {
			    LinkedList l =
				ImportData.importData(getGlobalContext(),
						      getView(), getGraphPanel(),
						      URLs[n], URLs[n+1], ImportData.XML_IMPORT, null, null, true, false, true);
			    if (l != null && l.size() > 0)
				list.addAll(l);
			}

			if (list.size() > 0) {
			    getGraphElements().addAll(list);
			    syncGraphElements(true);
			}
		    }
		});
	    popup.add(menuItem);
	}

	if (systemConfig != null) {
	    PropertyElementMenu menu = systemConfig.getMenu("DataElement", type);
	    if (menu != null)
		menu.buildJPopupMenu(view, popup, data);
	}
	return popup;
    }

    void createDendroMenu(JPopupMenu popup,
			  DendrogramGraphElement dendroGE) {
	JMenuItem menuItem;

	if (!dendroGE.isLeaf()) {
	    menuItem = new JMenuItem("Select Subtree");
	
	    menuItem.addActionListener(new ActionListenerWrapper(dendroGE) {
		    public void actionPerformed(ActionEvent e) {
			DendrogramGraphElement dendroGE =
			    (DendrogramGraphElement)getValue();
			dendroGE.setSelected(true,
					     DendrogramGraphElement.DEPTH_MODE);
			repaint();
		    }
		});
	    popup.add(menuItem);

	    menuItem = new JMenuItem("Copy Subtree");
	
	    menuItem.addActionListener(new ActionListenerWrapper(dendroGE) {
		    public void actionPerformed(ActionEvent e) {
			DendrogramGraphElement dendroGE =
			    (DendrogramGraphElement)getValue();
			LinkedList list = dendroGE.copySubTree();
			clipboard.clear();
			int sz = list.size();
			for (int n = 0; n < sz; n++)
			    clipboard.add((GraphElement)list.get(n));
			dendroGE.setSelected(true,
					     DendrogramGraphElement.DEPTH_MODE);
			repaint();
		    }
		});
	    popup.add(menuItem);
	}
	
	
	menuItem = new JMenuItem("Set Color");
	
	menuItem.addActionListener(new ActionListenerWrapper(dendroGE) {
		public void actionPerformed(ActionEvent e) {
		    DendrogramGraphElement dendroGE =
			(DendrogramGraphElement)getValue();
		    setColor(dendroGE, DendrogramGraphElement.LOCAL_MODE);
		}
	    });

	popup.add(menuItem);

	if (dendroGE.isLeaf())
	    return;

	menuItem = new JMenuItem("Set Subtree Color");
	
	menuItem.addActionListener(new ActionListenerWrapper(dendroGE) {
		public void actionPerformed(ActionEvent e) {
		    DendrogramGraphElement dendroGE =
			(DendrogramGraphElement)getValue();
		    setColor(dendroGE, DendrogramGraphElement.DEPTH_MODE);
		}
	    });

	popup.add(menuItem);

	if (dendroGE.isBridge()) {
	    menuItem = new JMenuItem("Set Bridge Color");

	    menuItem.addActionListener(new ActionListenerWrapper(dendroGE) {
		    public void actionPerformed(ActionEvent e) {
			DendrogramGraphElement dendroGE =
			    (DendrogramGraphElement)getValue();
			setColor(dendroGE, DendrogramGraphElement.BRIDGE_MODE);
		    }
		});
	    popup.add(menuItem);
	}

	if (!dendroGE.isLeaf()) {
	    menuItem = new JMenuItem("Center on Subtree");
	    menuItem.addActionListener(new ActionListenerWrapper(dendroGE) {
		    public void actionPerformed(ActionEvent e) {
			DendrogramGraphElement dendroGE =
			    (DendrogramGraphElement)getValue();
			centerOnSubtree(dendroGE);
		    }
		});

	    popup.add(menuItem);

	    menuItem = new JMenuItem("Export Subtree Gene List");
	    menuItem.addActionListener(new ActionListenerWrapper(dendroGE) {
		    public void actionPerformed(ActionEvent e) {
			DendrogramGraphElement dendroGE =
			    (DendrogramGraphElement)getValue();
			TranscriptomeClusterExportTool.getInstance().
			    perform(getThis(), dendroGE);
		    }
		});

	    popup.add(menuItem);
	}
    }

    void manageBreakpointFrequency(GraphElement graphElement,
				   JPopupMenu popup) {
	String type = (String)graphElement.getPropertyValue(VAMPProperties.TypeProp);

	if ((!type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE) &&
	     !type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_TYPE)) ||
	    !(graphElement.getGraphElementDisplayer() instanceof
	      BreakpointFrequencyDataSetDisplayer))
	    return;

	BreakpointFrequencyDataSetDisplayer dsp =
	    (BreakpointFrequencyDataSetDisplayer)graphElement.getGraphElementDisplayer();

	JMenuItem menuItem = new JMenuItem((dsp.showAssociations() ? "Hide" :
					    "Show") + " associations");
	menuItem.addActionListener(new ActionListenerWrapper(dsp) {
		public void actionPerformed(ActionEvent e) {
		    BreakpointFrequencyDataSetDisplayer dsp =
			(BreakpointFrequencyDataSetDisplayer)getValue();
		    dsp.showAssociations(!dsp.showAssociations());
		    
		    //repaint();
		    syncGraphElements(false);
		}
	    });

	popup.add(menuItem);
    }

    JPopupMenu createPopupMenu(GraphElement graphElement) {
        JPopupMenu popup = new JPopupMenu();
	JMenuItem menuItem;

	String id = (String)graphElement.getID();
	String type = (String)graphElement.getPropertyValue(VAMPProperties.TypeProp);
	if (id != null) {
	    if (type != null)
		id = type + " : " + id;
	    popup.add(id);
	    popup.addSeparator();
	}

	if (graphElement.isPinnedUp()) {
	    menuItem = new JMenuItem("Take info down");
	    menuItem.addActionListener(new ActionListenerWrapper(graphElement) {
		    public void actionPerformed(ActionEvent e) {
			clearPinnedUp();
			manageInfoInPopupMenu();
			repaint();
		    }
		});
	}
	else {
	    menuItem = new JMenuItem("Pin up info");
	    menuItem.addActionListener(new ActionListenerWrapper(graphElement) {
		    public void actionPerformed(ActionEvent e) {
			clearPinnedUp();
			pinnedUpGraphElem = (GraphElement)getValue();
			pinnedUpGraphElem.setPinnedUp(true);
			manageInfoInPopupMenu();
			repaint();
		    }
		});
	}

	popup.add(menuItem);

	if (graphElement instanceof DendrogramGraphElement)
	    createDendroMenu(popup, (DendrogramGraphElement)graphElement);

	createMarkMenu(popup, (DataElement)null, (GraphElement)null);

	menuItem = createPropEditMenu(graphElement);
	popup.add(menuItem);

	manageBreakpointFrequency(graphElement, popup);

	menuItem = new JMenuItem("Copy ID in search panel");
	menuItem.addActionListener(new ActionListenerWrapper(graphElement) {
		public void actionPerformed(ActionEvent e) {
		    GraphElement graphElement = (GraphElement)getValue();
		    view.getSearchGraphElementPanel().setGraphElementText((String)graphElement.getID());
		}
	    });

	popup.add(menuItem);

	if (systemConfig != null) {
	    PropertyElementMenu menu = systemConfig.getMenu("GraphElement", type);
	    if (menu != null)
		menu.buildJPopupMenu
		    (view, popup, graphElement);
	}
	return popup;
    }

    JPopupMenu createPopupMenu(Mark mark) {
        JPopupMenu popup = new JPopupMenu();
	JMenuItem menuItem;
	//GraphElement ds = getTemplateDS();

	String title = "Landmark";
	String tag = VAMPUtils.getTag(mark);
	if (tag != null)
	    title += " " + tag;
	popup.add(title);
	popup.getComponent(0).setBackground(mark.getColor());
	popup.addSeparator();

	if (VAMPUtils.getType(mark).equals(VAMPConstants.RECURRENT_BREAKPOINT_TYPE) &&
	    mark.getPropertyValue(GenomeAlterationOP.SupportVProp) != null) {

	    menuItem = new JMenuItem("Sort CGH arrays");
	    menuItem.addActionListener(new ActionListenerWrapper(mark) {
		    public void actionPerformed(ActionEvent e) {
			Mark mark = (Mark)getValue();
			sortArrays(mark);
		    }
		});
	    popup.add(menuItem);
	}

	if (mark.isCentered()) {
	    menuItem = new JMenuItem("Uncenter landmark");
	    menuItem.addActionListener(new ActionListenerWrapper(mark) {
		    public void actionPerformed(ActionEvent e) {
			getGraphPanelSet().clearCenter(which);
			repaint();
		    }
		});
	}
	else {
	    menuItem = new JMenuItem("Center on landmark");
	    menuItem.addActionListener(new ActionListenerWrapper(mark) {
		    public void actionPerformed(ActionEvent e) {
			Mark mark = (Mark)getValue();

			getGraphPanelSet().clearCenter(which);
			centeredMark = mark;
			graphPanel.centerOnMark(mark);
		    }
		});
	}
	popup.add(menuItem);

	if (mark.getRegion() == null) {
	    menuItem = new JMenuItem("Remove landmark");
	    menuItem.addActionListener(new ActionListenerWrapper(mark) {
		    public void actionPerformed(ActionEvent e) {
			getGraphPanelSet().removeMark(which,
						      (Mark)getValue());
		    }
		});
	    popup.add(menuItem);
	}

	menuItem = new JMenuItem("Begin");
	menuItem.addActionListener(new ActionListenerWrapper(mark) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setMarkBegin((Mark)getValue());
		}
	    });
	popup.add(menuItem);
	if (getGraphPanelSet().getMarkBegin() != null) {
	    menuItem = new JMenuItem("End");
	    menuItem.addActionListener(new ActionListenerWrapper(mark) {
		    public void actionPerformed(ActionEvent e) {
			getGraphPanelSet().addRegion(which,
						     (Mark)getValue());
			repaint();
		    }
		});
	}
	popup.add(menuItem);

	menuItem = createPropEditMenu(mark);
	popup.add(menuItem);

	JMenu subMenu = new JMenu("Set color");

	menuItem = new JMenuItem("Default");
	menuItem.addActionListener(new ActionListenerWrapper(mark) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setMarkColor(((Mark)getValue()),
						    VAMPResources.getColor
						    (VAMPResources.MARK_FG));
		}
	    });
	subMenu.add(menuItem);

	menuItem = new JMenuItem("Red");
	menuItem.addActionListener(new ActionListenerWrapper(mark) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setMarkColor(((Mark)getValue()),
						    Color.RED);
		}
	    });
	subMenu.add(menuItem);

	menuItem = new JMenuItem("Yellow");
	menuItem.addActionListener(new ActionListenerWrapper(mark) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setMarkColor(((Mark)getValue()),
						    Color.YELLOW);
		    repaint();
		}
	    });
	subMenu.add(menuItem);

	menuItem = new JMenuItem("Blue");
	menuItem.addActionListener(new ActionListenerWrapper(mark) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setMarkColor(((Mark)getValue()),
						    Color.BLUE);
		}
	    });

	subMenu.add(menuItem);

	menuItem = new JMenuItem("Other ...");
	menuItem.addActionListener(new ActionListenerWrapper(mark) {
		public void actionPerformed(ActionEvent e) {
		    Mark mark = (Mark)getValue();
		    Color color = JColorChooser.showDialog(view, "Landmark Color",
							   mark.getColor());
		    if (color != null)
			getGraphPanelSet().setMarkColor(mark, color);
		}
	    });

	subMenu.add(menuItem);

	popup.add(subMenu);

	if (systemConfig != null) {
	    PropertyElementMenu menu = systemConfig.getMenu("Landmark");
	    if (menu == null)
		menu = systemConfig.getMenu("Landmark");
	    if (menu != null)
		menu.buildJPopupMenu(view, popup, mark);
	}
	return popup;
    }


    JPopupMenu createPopupMenu(Region region) {
        JPopupMenu popup = new JPopupMenu();
	JMenu_PopupMenu menu_popup = new JMenu_PopupMenu(popup);
	createMenu(region, menu_popup, null);
	return popup;
    }

    private static String getRegionTitle(Region region, String suffix) {
	String title = "Region";
	String tag = VAMPUtils.getTag(region);
	if (tag != null && tag.length() > 0)
	    return title + " " + tag;

	if (suffix == null)
	    return title;
	return title + " " + suffix;
    }

    void createBackFrontMenu(Region region, JMenu_PopupMenu popup) {
	boolean[] ba;
	boolean is_back = isBack(region);
	boolean is_front = isFront(region);
	if (is_back && is_front)
	    ba = new boolean[]{true, false};
	else if (is_back)
	    ba = new boolean[]{false};
	else if (is_front)
	    ba = new boolean[]{true};
	else
	    ba = new boolean[]{};

	for (int n = 0; n < ba.length; n++) {
	    Object args[] = new Object[2];	
	    args[0] = new Boolean(ba[n]);
	    args[1] = region;
	    JMenuItem menuItem = new JMenuItem(ba[n] ? "Push back" : "Push front");
	    menuItem.addActionListener(new ActionListenerWrapper(args) {
		    public void actionPerformed(ActionEvent e) {
			Object args[] = (Object[])getValue();
			boolean is_back = ((Boolean)args[0]).booleanValue();
			Region region = (Region)args[1];
			if (is_back)
			    push_back(region);
			else
			    push_front(region);
			repaint();
		    }
		});
	    popup.add(menuItem);
	}
    }

    void createMenu(Region region, JMenu_PopupMenu popup, String suffix) {
	JMenuItem menuItem;
	String title = getRegionTitle(region, suffix);
	popup.add(title);
	Color c = region.getColor();
	popup.getComponent(0).setBackground(c);
	popup.addSeparator(); 


	if (region.isPinnedUp()) {
	    menuItem = new JMenuItem("Take info down");
	    menuItem.addActionListener(new ActionListenerWrapper(region) {
		    public void actionPerformed(ActionEvent e) {
			clearPinnedUp();
			manageInfoInPopupMenu();
			repaint();
		    }
		});
	}
	else {
	    menuItem = new JMenuItem("Pin up info");
	    menuItem.addActionListener(new ActionListenerWrapper(region) {
		    public void actionPerformed(ActionEvent e) {
			clearPinnedUp();
			pinnedUpRegion = (Region)getValue();
			pinnedUpRegion.setPinnedUp(true);
			manageInfoInPopupMenu();
			repaint();
		    }
		});
	}

	popup.add(menuItem);

	createBackFrontMenu(region, popup);

	if ((VAMPUtils.getType(region).equals(VAMPConstants.MINIMAL_REGION_TYPE) ||
	    VAMPUtils.getType(region).equals(VAMPConstants.RECURRENT_REGION_TYPE)) && region.getPropertyValue(GenomeAlterationOP.SupportVProp) != null) {
	    menuItem = new JMenuItem("Sort CGH arrays");
	    menuItem.addActionListener(new ActionListenerWrapper(region) {
		    public void actionPerformed(ActionEvent e) {
			Region region = (Region)getValue();
			sortArrays(region);
		    }
		});
	    popup.add(menuItem);
	}

	menuItem = new JMenuItem("Center on region");
	menuItem.addActionListener(new ActionListenerWrapper(region) {
		public void actionPerformed(ActionEvent e) {
		    Region region = (Region)getValue();
		    getGraphPanelSet().clearCenter(which);
		    centeredRegion = region;
		    graphPanel.centerOnRegion(region);
		}
	    });
	popup.add(menuItem);

	menuItem = new JMenuItem("Remove region and landmarks");
	menuItem.addActionListener(new ActionListenerWrapper(region) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().removeRegion(which,
						    (Region)getValue(), true);
		}
	    });
	popup.add(menuItem);

	menuItem = new JMenuItem("Remove region but not landmarks");
	menuItem.addActionListener(new ActionListenerWrapper(region) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().removeRegion(which,
						    (Region)getValue(), false);
		}
	    });
	popup.add(menuItem);

	menuItem = createPropEditMenu(region);
	popup.add(menuItem);

	JMenu subMenu = new JMenu("Set color");

	menuItem = new JMenuItem("Default");
	menuItem.setBackground(VAMPResources.getColor(VAMPResources.REGION_BG));
	menuItem.addActionListener(new ActionListenerWrapper(region) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setRegionColor(((Region)getValue()),
						      VAMPResources.getColor
						      (VAMPResources.REGION_BG));
		}
	    });
	subMenu.add(menuItem);

	menuItem = new JMenuItem("Light Green #1");
	menuItem.setBackground(VAMPResources.LIGHTGREEN_1);
	menuItem.addActionListener(new ActionListenerWrapper(region) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setRegionColor(((Region)getValue()),
						      VAMPResources.LIGHTGREEN_1);
		}
	    });
	subMenu.add(menuItem);

	menuItem = new JMenuItem("Light Green #2");
	menuItem.setBackground(VAMPResources.LIGHTGREEN_2);
	menuItem.addActionListener(new ActionListenerWrapper(region) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setRegionColor(((Region)getValue()),
						      VAMPResources.LIGHTGREEN_2);
		}
	    });
	subMenu.add(menuItem);

	menuItem = new JMenuItem("Light Pink #1");
	menuItem.setBackground(VAMPResources.LIGHTPINK_1);
	menuItem.addActionListener(new ActionListenerWrapper(region) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setRegionColor(((Region)getValue()),
						      VAMPResources.LIGHTPINK_1);;
		}
	    });
	subMenu.add(menuItem);

	menuItem = new JMenuItem("Light Pink #2");
	menuItem.setBackground(VAMPResources.LIGHTPINK_2);
	menuItem.addActionListener(new ActionListenerWrapper(region) {
		public void actionPerformed(ActionEvent e) {
		    getGraphPanelSet().setRegionColor(((Region)getValue()),
						      VAMPResources.LIGHTPINK_2);
		}
	    });
	subMenu.add(menuItem);

	menuItem = new JMenuItem("Other ...");
	menuItem.setBackground(Color.WHITE);
	menuItem.addActionListener(new ActionListenerWrapper(region) {
		public void actionPerformed(ActionEvent e) {
		    Region region = (Region)getValue();
		    Color color = JColorChooser.showDialog(view,
							   "Region Color",
							   region.getColor());
		    if (color != null)
			getGraphPanelSet().setRegionColor(region, color);
		}
	    });
	subMenu.add(menuItem);

	popup.add(subMenu);

	if (systemConfig != null) {
	    /*
	      PropertyElementMenu menu = systemConfig.getMenu("Region",
	      VAMPUtils.getType(region));
	      if (menu != null)
	      menu.buildJPopupMenu(view, popup, region);
	    */
	}
    }


    // popup menu on no man's land...
    JPopupMenu createPopupMenu() {
	/*
	  if (getVCenter() != null) {
	  JPopupMenu popup = new JPopupMenu();
	  JMenuItem menuItem;
	  menuItem = new JMenuItem("Center");
	  menuItem.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	  center(getVCenter());
	  repaint();
	  }
	  });
	  popup.add(menuItem);
	  return popup;
	  }
	*/

	return null;
    }

    void addMark(Mark mark) {
	mark.setGraphPanel(graphPanel);
	getMarks().add(mark);
	if (isPaintingRegionMode(SYNC_MODE))
	    repaint();
    }

    void addRegion(Region region) {
	region.setGraphPanel(graphPanel);
	getRegions().add(region);
	if (isPaintingRegionMode(SYNC_MODE))
	    repaint();
    }

    boolean cutRegion(Region region) {
	getRegions().remove(region);
	if (isPaintingRegionMode(SYNC_MODE))
	    repaint();
	return true;
    }

    boolean cutMark(Mark mark) {
	Region region = mark.getRegion();
	if (region != null && getRegions().indexOf(region) >= 0 &&
	    !region.isPasteable(Pasteable.CUT) &&
	    !region.isPasteable(Pasteable.REMOVE))
	    return false;

	getMarks().remove(mark);
	if (isPaintingRegionMode(SYNC_MODE))
	    repaint();
	return true;
    }

    void removeRegion(Region region, boolean removeMarks) {
	getRegions().remove(region);
	region.dispose();
	if (removeMarks) {
	    removeMark(region.getBegin());
	    removeMark(region.getEnd());
	    // added 18/03/05:
	    getMarks().remove(region.getBegin());
	    getMarks().remove(region.getEnd());
	}
	else {
	    if (isPaintingRegionMode(SYNC_MODE))
		repaint();
	}
    }

    boolean removeMark(Mark mark) {
	if (mark.getRegion() == null) {
	    getMarks().remove(mark);
	    if (isPaintingRegionMode(SYNC_MODE))
		repaint();
	    return true;
	}

	System.err.println("WARNING: cannot remove mark with region");
	return false;
    }

    void removeMarks() {
	int size = getMarks().size();
	if (size > 0) {
	    Object mark_arr[] = getMarks().toArray();
	    centeredMark = null;
	    for (int i = 0; i < size; i++)
		removeMark((Mark)mark_arr[i]);
	    if (isPaintingRegionMode(SYNC_MODE))
		repaint();
	}
    }

    void removeRegions(boolean removeRegionMarks) {
	int size = getRegions().size();
	if (size > 0) {
	    Object region_arr[] = getRegions().toArray();
	    for (int i = 0; i < size; i++)
		removeRegion((Region)region_arr[i], removeRegionMarks);
	    if (isPaintingRegionMode(SYNC_MODE))
		repaint();
	}
    }

    String getCenterType() {
	if (centeredElem != null)
	    return (String)centeredElem.getPropertyValue(VAMPProperties.TypeProp);
	if (centeredMark != null)
	    return "landmark";
	if (centeredRegion != null)
	    return "region";
	return null;
    }

    void clearCenter() {
	if (centeredElem != null) {
	    centeredElem.setCentered(false);
	    centeredElem = null;
	}
	else if (centeredMark != null) {
	    centeredMark.setCentered(false);
	    centeredMark = null;
	}
	else if (centeredRegion != null) {
	    centeredRegion.setCentered(false);
	    centeredRegion = null;
	}
	else if (centeredDendroGE != null) {
	    centeredDendroGE.setColor(centeredDendroGEColor, 
				      DendrogramGraphElement.DEPTH_MODE);
	    centeredDendroGE = null;
	}
	else if (centeredProbe != null) { // this test is not useful
	    centeredProbe = null;
	}

	graphPanel.setVCenter(null);
    }

    private GraphPanelSet getGraphPanelSet() {
	if (graphPanelSet == null)
	    graphPanelSet = view.getGraphPanelSet();
	return graphPanelSet;
    }

    void readaptSize() {
	updateSize(false);
	computeGraphElementVBounds(canon_scale.getScaleY());
	graphPanel.adaptScrollBars();
	updateSize(false);
    }

    //
    // Copy/Cut & Paste methods
    // 

    void cutSelection() {
	cutOrCopy(Pasteable.CUT);
    }

    void copySelection() {
	cutOrCopy(Pasteable.COPY);
    }

    void removeSelection() {
	cutOrCopy(Pasteable.REMOVE);
    }

    void removeAllS() {
	selectAll_r(true, ALL_MASK);
	cutOrCopy(Pasteable.REMOVE);
    }

    void removeGraphElements() {
	selectAll_r(true, GRAPH_ELEM_MASK);
	cutOrCopy(Pasteable.REMOVE);
    }

    private boolean cutOrCopy(int action) {
	clipboard.clear();
	LinkedList graphElements_c = (LinkedList)graphElements.clone();
	int cntGraphElements = cutOrCopyList(graphElements_c, graphElements, action);
	int cnt = cntGraphElements;
	cnt += cutOrCopyList(null, getMarks(), action);
	cnt += cutOrCopyList(null, getRegions(), action);

	if (cnt == 0) {
	    beep();
	    return false;
	}

	if (action == Pasteable.CUT || action == Pasteable.REMOVE)
	    return postCut(graphElements_c, cntGraphElements, cnt, false);

	repaint(); // added 1/12/03
	return true;
    }

    private int cutOrCopyList(LinkedList graphElements_c, LinkedList list,
			      int action) {
	int cnt = 0;
	Object arr[] = list.toArray();
	for (int m = 0; m < arr.length; m++) {
	    Pasteable pastable = (Pasteable)arr[m];
	    cnt += cutOrCopyPasteable(graphElements_c, pastable, action);
	}
	return cnt;
    }

    static private boolean hasSelection(LinkedList list) {
	int size = list.size();
	for (int i = 0; i < size; i++) {
	    Pasteable select = (Pasteable)list.get(i);
	    if (select.isSelected())
		return true;
	}
	return false;
    }

    public boolean hasSelection() {
	return hasSelection(graphElements) ||
	    hasSelection(getMarks()) ||
	    hasSelection(getRegions());
    }

    public boolean hasGraphElementSelection() {
	return hasSelection(graphElements);
    }

    public boolean hasMarkSelection() {
	return hasSelection(getMarks());
    }

    public boolean hasRegionSelection() {
	return hasSelection(getRegions());
    }

    void addSelectedGraphElement(GraphElement graphElement) {
	selGraphElements.add(graphElement);
    }

    void removeSelectedGraphElement(GraphElement graphElement) {
	selGraphElements.remove(graphElement);
    }

    public void computeSelectedGraphElements() {
	selGraphElements = new Vector();
	int size = graphElements.size();
	for (int i = 0; i < size; i++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(i);
	    if (graphElement.isSelected()) {
		selGraphElements.add(graphElement);
	    }
	}
    }

    public Vector getSelectedGraphElements() {
	//System.out.println("getSelectedGraphElements: " + selGraphElements.size());
	return selGraphElements;
	/*
	Vector v = new Vector();
	int size = graphElements.size();
	for (int i = 0; i < size; i++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(i);
	    if (graphElement.isSelected()) {
		v.add(graphElement);
	    }
	}
	return v;
	*/
    }

    public void replaceGraphElements(Vector from, Vector to) {
	if (from.equals(to)) {
	    repaint();
	    return;
	}

	if (isReadOnly) {
	    beep();
	    return;
	}

	LinkedList graphElements_c;

	int size = from.size();
	int min;
	int del_cnt = 0;
	if (size > 0) {
	    graphElements_c = (LinkedList)graphElements.clone();
	    min = Integer.MAX_VALUE;
	    Vector<GraphElement> toUnselect = new Vector();
	    for (int i = 0; i < size; i++) {
		GraphElement graphElement = (GraphElement)from.get(i);
		int idx = graphElements.indexOf(graphElement);
		if (idx < min) {
		    min = idx;
		}
		if (idx >= 0) {
		    del_cnt++;
		}

		toUnselect.add(graphElement);
		graphElements_c.remove(graphElement);
	    }

	    for (int i = 0; i < toUnselect.size(); i++) {
		toUnselect.get(i).setSelected(false, this);
	    }
	}
	else {
	    graphElements_c = new LinkedList();
	    min = 0;
	}

	if (del_cnt != size) {
	    InfoDialog.pop(getGlobalContext(),
			   "GraphCanvas: Internal Error: " + del_cnt + " vs. "+
			   size);
	    return;
	}

	size = to.size();
	for (int i = 0; i < size; i++) {
	    GraphElement graphElement = (GraphElement)to.get(i);
	    graphElement.setSelected(true, this);
	    //setSelected(graphElement, true);
	    graphElements_c.add(min+i, graphElement);
	}

	getGraphPanelSet().selectionSync();
	setGraphElements(graphElements_c);
	readaptSize();
    }

    private int cutOrCopyPasteable(LinkedList graphElements_c, Pasteable pasteable,
				   int action) {
	/*
	  System.out.println("cutOrCopyPasteable: " + pasteable + ", " +
	  action);
	*/
	if (!pasteable.isPasteable(action)) return 0;
	//System.out.println("cutOrCopyPasteable really...");

	if (action == Pasteable.COPY) {
	    try {
		pasteable = (Pasteable)pasteable.clone_light();
		pasteable.postClone();
	    }
	    catch(CloneNotSupportedException e) {
		e.printStackTrace();
		return 0;
	    }
	}
	else if (action == Pasteable.CUT || action == Pasteable.REMOVE)
	    if (cutPasteable(graphElements_c, pasteable, false) == 0)
		return 0;
	
	if (action != Pasteable.REMOVE)
	    clipboard.add(pasteable);
	return 1;
    }

    private int cutPasteable(LinkedList graphElements_c,
			     Pasteable pasteable,
			     boolean selfDropping) {
	if (pasteable instanceof Region) {
	    if (!selfDropping) {
		//System.out.println("cutting region: " + pasteable);
		if (!getGraphPanelSet().cutRegion(which,
						  (Region)pasteable))
		    return 0;
	    }
	}
	else if (pasteable instanceof Mark) {
	    if (!selfDropping) {
		//System.out.println("cutting mark: " + ((Mark)pasteable).getRegion());
		if (!getGraphPanelSet().cutMark(which,
						(Mark)pasteable))
		    return 0;
	    }
	}
	else if (pasteable instanceof GraphElement) {
	    /*
	      if (isReadOnly && !selfDropping) {
	      beep();
	      return 0;
	      }
	    */
	    if (isReadOnly) {
		beep();
		return 0;
	    }

	    graphElements_c.remove(pasteable);
	}

	pasteable.setSelected(false, this);
	return 1;
    }

    private boolean postCut(LinkedList graphElements_c, int cntGraphElements, int cnt,
			    boolean selfDropping) {
	//System.out.println("postCut begin");
	if (cntGraphElements != 0) {
	    self_dropping = selfDropping;
	    boolean r = setGraphElements(graphElements_c);
	    self_dropping = false;
	    if (!r)
		return false;
	    readaptSize();
	}

	// -- 10/03/04: moved from cutPasteable()
	getGraphPanelSet().updateBottomTitle();
	getGraphPanelSet().selectionSync();
	// --

	if (cnt != 0)
	    repaint();
	//System.out.println("postCut end");
	return true;
    }

    void pasteSelection(GraphElement before) {
	LinkedList list = clipboard.getContents();
	if (!checkPaste(before, list, false)) {beep(); return;}
	paste(before, list, false);
    }

    private boolean checkPaste(GraphElement before, LinkedList toPaste,
			       boolean selfDropping) {
	int size = toPaste.size();
	if (size == 0) return true;

	//System.out.println("checkPaste begin " + size);
	int idx = (before != null ? graphElements.indexOf(before) : -1);

	LinkedList graphElements_c = (LinkedList)graphElements.clone();

	for (int i = 0; i < size; i++) {
	    if (toPaste.get(i) instanceof GraphElement) {
		/*
		  if (isReadOnly && !selfDropping)
		  return false;
		*/
		// 15/12/04
		if (isReadOnly)
		    return false;
		GraphElement graphElement = (GraphElement)toPaste.get(i);
		if (idx != -1)
		    graphElements_c.add(idx, graphElement);
		else
		    graphElements_c.add(graphElement);
	    }
	}

	return view.checkAutoApplyDSLOP(graphPanel, graphElements_c);
    }

    private void paste(GraphElement before, LinkedList toPaste,
		       boolean selfDropping) {
	//System.out.println("paste begin");
	int size = toPaste.size();
	if (size == 0) return;

	selectAll(false);

	int cnt = 0;
	int idx = (before != null ? graphElements.indexOf(before) : -1);

	LinkedList graphElements_c = (LinkedList)graphElements.clone();

	for (int i = 0; i < size; i++)
	    ((Pasteable)toPaste.get(i)).prePaste();

	for (int i = 0; i < size; i++) {
	    if (toPaste.get(i) instanceof GraphElement) {
		/*
		  if (isReadOnly && !selfDropping)
		  beep();
		*/
		// 15/12/04
		if (isReadOnly)
		    beep();
		else {
		    cnt++;
		    GraphElement graphElement = (GraphElement)toPaste.get(i);
		    if (idx != -1)
			graphElements_c.add(idx, graphElement);
		    else
			graphElements_c.add(graphElement);
		    graphElement.resetPaintVBounds();
		}
	    }
	}

	if (!selfDropping) {
	    UndoVMStack undo_stack = UndoVMStack.getInstance(getGraphPanelSet().getPanel(which));
	    boolean active = undo_stack.setActive(false);
	    for (int i = 0; i < size; i++)
		if (toPaste.get(i) instanceof Mark)
		    getGraphPanelSet().addMark(which,
					       (Mark)toPaste.get(i));

	    for (int i = 0; i < size; i++)
		if (toPaste.get(i) instanceof Region)
		    getGraphPanelSet().addRegion(which,
						 (Region)toPaste.get(i));
	    undo_stack.setActive(active);
	}

	boolean selected = true;
	if (graphPanel.getDefaultGraphElementDisplayer() != null &&
	    !graphPanel.getDefaultGraphElementDisplayer().selectAfterPaste())
	    selected = false;

	for (int i = 0; i < size; i++)
	    ((Selectable)toPaste.get(i)).setSelected(selected, this);
	//setSelected(((Selectable)toPaste.get(i)), selected);

	if (cnt != 0) {
	    self_dropping = selfDropping;
	    boolean r = setGraphElements(graphElements_c);
	    self_dropping = false;
	    if (!r)
		return;
	    readaptSize();
	}

	// 1/12/03 : moved from pasteSelection()
	getGraphPanelSet().selectionSync();
	clipboard.clear();
	//System.out.println("paste end");
	repaint();
    }

    void setReadOnly(boolean isReadOnly) {
	if (this.isReadOnly != isReadOnly) {
	    this.isReadOnly = isReadOnly;
	    repaint();
	}
    }

    //
    // Drag & Drop methods
    //

    // DragGestureListener

    public void dragGestureRecognized(DragGestureEvent e) {
	boolean isMoving = (e.getTriggerEvent().getModifiers() & (MouseEvent.ALT_MASK|MouseEvent.CTRL_MASK)) == 0;
	cutOrCopy(isMoving ? Pasteable.DRAG : Pasteable.COPY);
	LinkedList list = clipboard.getContents();
	if (list.size() != 0) {
	    Cursor cursor;
	    Mark mark;
	    if ((mark = isDraggingMark(list)) != null) {
		Region region = mark.getRegion();
		if (region != null) {
		    if (region.getBegin() == mark)
			cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
		    else
			cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
		}
		else
		    cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
	    }
	    else if (isMoving)
		cursor = DragSource.DefaultMoveDrop;
	    else
		cursor = DragSource.DefaultCopyDrop;

	    setCursor(cursor); // for windows
	    e.startDrag(cursor,
			new TransferableObject(list),
			this);
		
	    clipboard.setDragInfo(this, isMoving);
	}
    }

    // DragSourceListener

    public void dragDropEnd(DragSourceDropEvent e) {
	setCursor(Cursor.getDefaultCursor());
    }

    public void dragEnter(DragSourceDragEvent e) {
    }

    public void dragExit(DragSourceEvent e) {
    }

    public void dragOver(DragSourceDragEvent e) {
	Transferable t = e.getDragSourceContext().getTransferable();
	LinkedList list = getTransferableList(t);
	Mark mark = isDraggingMark(list);
	if (mark != null) {
	    UndoableVMStatement vmstat =  UndoVMStack.getInstance(graphPanel).getLastUndoableVMStatement();
	    if (vmstat instanceof MoveMarkVMStatement &&
		vmstat.getGraphPanel() == getGraphPanel()) {
		((MoveMarkVMStatement)vmstat).setPosX(getPosX());
	    }
	    else {
		vmstat = new MoveMarkVMStatement(getGraphPanel(), mark,
						 getPosX());
		UndoVMStack.getInstance(graphPanel).push(vmstat);
	    }

	    getGraphPanelSet().moveMark(mark, getPosX());
	}
    }

    public void dropActionChanged(DragSourceDragEvent e) { }

    // DropTargetListener

    public void dragOver(DropTargetDragEvent e) {
	Point p = e.getLocation();
	manageInfo(p.x, p.y);
    }

    public void drop(DropTargetDropEvent e) {
	GraphCanvas source = (GraphCanvas)clipboard.getDragSource();
	boolean selfDropping = (source == this && clipboard.isDragMoving());

	setCursor(Cursor.getDefaultCursor());
	clipboard.setDropTarget(this);
	Transferable t = e.getTransferable();
	LinkedList list = getTransferableList(t);
	if (list == null || list.size() == 0)
	    return;

	// it is important to get where to paste before cutting in
	// drag source :

	GraphElement before = getPreviousGraphElementAt(curPosX, curPosY);

	if (!checkPaste(before, list, selfDropping)) {
	    beep();
	    return;
	}

	if (list.size() == 1 && list.get(0) == before)
	    return;

	boolean moveMark = selfDropping && list.size() == 1 &&
	    list.get(0) instanceof Mark;

	DragAndDropVMStatement vmstat = new DragAndDropVMStatement
	    (clipboard.isDragMoving(),
	     source.getGraphPanel(), getGraphPanel(), list);
	vmstat.beforeExecute();

	State state = null;
	if (selfDropping)
	    state = new State(this, true);

	if (!grantPaste(list, selfDropping) ||
	    !source.dragDropEnd(list, this)) {
	    e.dropComplete(false);
	    return;
	}

	e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
	paste(before, list, selfDropping);
	e.dropComplete(true);

	if (!moveMark && (!selfDropping || !state.equals(new State(this, false)))) {
	    UndoVMStack.getInstance(source.getGraphPanel()).push(vmstat);
	}

	vmstat.afterExecute();
    }
    
    public void dropActionChanged(DropTargetDragEvent e) {}
    public void dragEnter(DropTargetDragEvent e) {
	setCursor(Clipboard.getInstance().isDragMoving() ?
		  DragSource.DefaultMoveDrop : DragSource.DefaultCopyDrop);
    }

    public void dragExit(DropTargetEvent e) {
	setCursor(Cursor.getDefaultCursor());
    }

    // Utility DND methods

    private boolean dragDropEnd(LinkedList list, GraphCanvas source) {
	if (clipboard.getDragSource() != this) return true;
	//System.out.println("DragDropEnd source = " + (source == this));
	//System.out.println("dragDropEnd begin " + list.size());
	boolean isMoving = clipboard.isDragMoving();
	boolean selfDropping = (clipboard.getDropTarget() == this && isMoving);
	clipboard.setDragInfo(null, false);
	if (!isMoving)
	    return true;

	int size = list.size();
	if (size != 0) {
	    LinkedList graphElements_c = (LinkedList)graphElements.clone();
	    int cntGraphElements = 0;
	    for (int i = 0; i < size; i++) {
		Pasteable pasteable = (Pasteable)list.get(i);
		if (pasteable instanceof GraphElement)
		    cntGraphElements++;
		if (cutPasteable(graphElements_c, pasteable, selfDropping) == 0)
		    return false;
	    }

	    if (!postCut(graphElements_c, cntGraphElements, size, selfDropping))
		return false;
	}

	//System.out.println("dragDropEnd end");
	return true;
    }

    private LinkedList getTransferableList(Transferable t) {
	try {
	    return (LinkedList)t.getTransferData(TransferableObject.pasteableFlavor);
	}
	catch (UnsupportedFlavorException ex) { beep(); }
	catch (java.io.IOException ex) { beep(); }
	catch (Exception e) {
	    System.out.println("NO DROP: " + e);
	}
	return null;
    }

    private Mark isDraggingMark(LinkedList list) {
	if (list.size() == 1 &&	list.get(0) instanceof Mark)
	    return (Mark)list.get(0);
	return null;
    }

    private void beep() {
	//(new Exception()).printStackTrace();
	getToolkit().beep();
    }

    private boolean grantPaste(LinkedList toPaste, boolean selfDropping) {
	//if (selfDropping || !isReadOnly) return true;
	// 15/12/04
	if (isReadOnly) return false;
	int size = toPaste.size();
	for (int i = 0; i < size; i++)
	    if (toPaste.get(i) instanceof GraphElement &&
		isReadOnly && !selfDropping) {
		beep();
		return false;
	    }
	return true;
    }

    private static boolean matchesPropValue(PropertyElement elem,
					    String propName, Pattern pattern) {
	if (VAMPUtils.isMissing(elem))
	    return false;
	TreeMap properties = elem.getProperties();
	Iterator it = properties.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    String name = (String)prop.getName();
	    if (propName.equalsIgnoreCase(name) &&
		//pattern.matches((String)entry.getValue()))
		pattern.matches(entry.getValue().toString()))
		return true;
	}
	return false;
    }

    SearchGraphElementCursor searchGraphElement
	(SearchGraphElementCursor cursor,
	 String propName, String input,
	 boolean isBackward, boolean all) {

	if (cursor == null) {
	    cursor = new SearchGraphElementCursor();
	    if (isBackward) cursor.setIndex(sizeSets);
	}

	Pattern pattern = new Pattern(input, false);
	int incr = (isBackward ? -1 : 1);

	if (all)
	    selectAll(false);

	int found = 0;
	for (int m = cursor.getIndex()+incr; m < sizeSets && m >= 0;
	     m += incr) {
	    GraphElement graphElement = getGraphElement(m);
	    if (matchesPropValue(graphElement, propName, pattern)) {
		cursor.setIndex(m);
		Rectangle2D.Double vbounds = graphElement.getVBounds();
		getGraphPanelSet().clearCenter(which);
		if (!all)
		    selectAll(false);
		graphElement.setSelected(true, this);
		setVCenter(new Point2D.Double(vbounds.x,
					      vbounds.y - vbounds.height/2));
		getGraphPanelSet().selectionSync();
		if (!all)
		    return cursor;
		found++;
	    }
	}

	return (found > 0 ? cursor : null);
    }

    SearchDataElementCursor searchDataElement(SearchDataElementCursor cursor,
					      String propName, String input,
					      boolean isBackward,
					      boolean pinupInfo,
					      boolean editProp,
					      boolean putMark) {
	if (cursor == null) {
	    cursor = new SearchDataElementCursor();
	    if (isBackward) cursor.setIndexDS(sizeSets-1);
	    else cursor.setIndexDS(0);
	}

	Pattern pattern = new Pattern(input, false);
	int incr = (isBackward ? -1 : 1);

	for (int m = cursor.getIndexDS(); m < sizeSets && m >= 0;
	     m += incr) {
	    DataSet dataSet = getDataSet(m);
	    if (dataSet == null) continue;
	    DataElement data[] = dataSet.getData();

	    if (isBackward && cursor.getIndexDE() == SearchCursor.BEGIN_INDEX)
		cursor.setIndexDE(data.length);

	    if (!pinupInfo) {
		clearPinnedUp();
		repaint();
	    }

	    for (int n = cursor.getIndexDE()+incr; n < data.length && n >= 0;
		 n += incr) {
		if (matchesPropValue(data[n], propName, pattern)) {
		    //System.out.println("Matching => DS: " + m + ", DE: " + n);
		    cursor.setIndexDE(n);
		    getGraphPanelSet().clearCenter(which);
		    centeredElem = data[n];
		    centeredElem.setCentered(true);
		    centeredGraphElem = dataSet;
		    setVCenter();

		    if (pinupInfo)
			setPinnedUp(dataSet, data[n]);

		    if (editProp) {
			//PropertyEditDialog.unpop(getGlobalContext());
			PropertyEditDialog.pop(getGlobalContext(), view, data[n], null);
		    }
		    if (putMark && getMarkAt_exact(data[n].getPosX(dataSet)) == null)
			getGraphPanelSet().addMark(which, data[n].getPosX(dataSet));

		    return cursor;
		}
	    }
	    cursor.setIndex(m+incr, SearchCursor.BEGIN_INDEX);
	}

	return null;
    }

    void setRegions(LinkedList regions) {
	synchronized(getLockObj()) {
	    this._regions = regions;
	}
    }

    void setMarks(LinkedList marks) {
	synchronized(getLockObj()) {
	    this._marks = marks;
	}
    }

    LinkedList getRegions() {
	synchronized(getLockObj()) {
	    return _regions;
	}
    }

    LinkedList getMarks() {
	synchronized(getLockObj()) {
	    return _marks;
	}
    }

    /*
    LinkedList getStandaloneMarks() {
	LinkedList l = new LinkedList();
	int sz = getMarks().size();
	for (int n = 0; n < sz; n++)
	    if (((Mark)getMarks().get(n)).getRegion() == null)
		l.add(getMarks().get(n);
	return l;
    }
    */

    // convert data element proper position to virtual position
    double computeVX(double pos_x) {
	if (sizeSets == 0) return 0;
	DataSet dataSet = getTemplateDS();
	if (dataSet == null) return 0;
	DataElement data[] = dataSet.getData();
	DataElement d0 = data[0];
	if (pos_x < d0.getPosX(dataSet))
	    return d0.getVX(dataSet);
	for (int n = 0; n < data.length-1; n++) {
	    DataElement dl = data[n];
	    DataElement dr = data[n+1];
	    if (pos_x >= dl.getPosX(dataSet) && pos_x < dr.getPosX(dataSet)) {
		return dl.getVX(dataSet) +
		    ((pos_x - dl.getPosX(dataSet)) / (dr.getPosX(dataSet) - dl.getPosX(dataSet))) *
		    (dr.getVX(dataSet) - dl.getVX(dataSet));
	    }
	}
	return data[data.length-1].getVX(dataSet);
    }

    private boolean isIn() {
	DataSet dataSet = getTemplateDS();
	if (dataSet == null) return false;
	DataElement data[] = dataSet.getData();
	Rectangle2D.Double rBounds_first = data[0].getRBounds(dataSet);
	Rectangle2D.Double rBounds_last = data[data.length-1].getRBounds(dataSet);
	if (!isIn ||
	    getTCurPosX() <
	    getTRX((int)rBounds_first.x, (int)rBounds_first.y) ||
	    getTCurPosX() >
	    getTRX((int)(rBounds_last.x + rBounds_last.width),
		   (int)(rBounds_last.y + rBounds_last.height)))
	    return false;
	return true;
    }

    GraphCanvas getThis() {return this;}

    // TBD: SHOULD NOT BE HERE
    // or must be written in another way

    boolean checkType(String type) {
	return type.equals(VAMPConstants.CGH_ARRAY_TYPE) ||
	    type.equals(VAMPConstants.CGH_AVERAGE_TYPE) ||
	    type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE) ||
	    type.equals(VAMPConstants.SNP_TYPE) ||
	    type.equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.SNP_AVERAGE_TYPE) ||
	    type.equals(VAMPConstants.GENOME_ANNOT_TYPE) ||
	    type.equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_TYPE) ||
	    type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.CHIP_CHIP_TYPE) ||
	    type.equals(VAMPConstants.CHIP_CHIP_AVERAGE_TYPE) ||
	    type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE) ||
	    type.equals(VAMPConstants.SIGNAL_DENSITY_TYPE);
    }

    DataSet getTemplateDS() {
	return templateDS;
    }

    Profile getTemplateProfile() {
	return templateProfile;
    }

    GraphElement getTemplateDSorProfile() {
	if (templateDS != null) {
	    return templateDS;
	}
	return templateProfile;
    }

    DataSet _getTemplateDS() {
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    DataSet dataSet = graphElement.asDataSet();

	    if (dataSet == null) {
		continue;
	    }

	    String type = (String)dataSet.getPropertyValue(VAMPProperties.TypeProp);
	    if (checkType(type)) {
		return dataSet;
	    }

	    Object o = graphElement.getPropertyValue(VAMPProperties.ArrayRefProp);
	    if (o != null)
		return (DataSet)o;
	}

	return null;
    }

    Profile _getTemplateProfile() {
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    Profile profile = graphElement.asProfile();

	    if (profile == null) {
		continue;
	    }

	    String type = (String)profile.getPropertyValue(VAMPProperties.TypeProp);
	    if (checkType(type)) {
		return profile;
	    }
	}

	return null;
    }

    /*
    GraphElement getTemplateDSorProfile() {
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    DataSet dataSet = graphElement.asDataSet();

	    if (dataSet == null) {
		Profile profile = graphElement.asProfile();

		if (profile == null) {
		    continue;
		}
	    }

	    String type = (String)graphElement.getPropertyValue(VAMPProperties.TypeProp);
	    if (checkType(type)) {
		return graphElement;
	    }

	    Object o = graphElement.getPropertyValue(VAMPProperties.ArrayRefProp);
	    if (o != null)
		return (GraphElement)o;
	}

	return null;
    }
    */

    boolean canAddMark(PropertyElement elem) {
	String type = (String)elem.getPropertyValue(VAMPProperties.TypeProp);
	return type.equals("Clone") || type.equals("ProbeSet") ||
	    type.equals("Oligo") || type.equals("Breakpoint Barplot") ||
	    type.equals(VAMPConstants.SIGNAL_DENSITY_ITEM_TYPE);
    }

    GraphElementDisplayer getGraphElementDisplayer(GraphElement graphElement) {
	if (graphElement.getGraphElementDisplayer() != null)
	    return graphElement.getGraphElementDisplayer();
	return graphPanel.getDefaultGraphElementDisplayer();
    }

    AxisDisplayer getAxisDisplayer(GraphElement graphElement) {
	if (graphElement.getAxisDisplayer() != null)
	    return graphElement.getAxisDisplayer();
	return graphPanel.getDefaultAxisDisplayer();
    }

    int getTCurPosX() {
	return getTRX(curPosX, curPosY);
    }

    int getTCurPosY() {
	return getTRY(curPosX, curPosY);
    }

    void incrWestMargin(int incr) {
	int mg = rmargins.getMarginWest();
	if (mg + incr >= 0)
	    rmargins.setMarginWest(mg + incr);
    }

    GlobalContext getGlobalContext() {
	return view.getGlobalContext();
    }

    Color getBGColor() {
	if (view.isRunningMode())
	    return VAMPResources.getColor(VAMPResources.CANVAS_RUNNING_BG);

	return isReadOnly ? VAMPResources.getColor(VAMPResources.CANVAS_LOCKED_BG) : VAMPResources.getColor(VAMPResources.CANVAS_BG);
    }

    void setWestYSize(int sz, boolean force) {
	getGraphPanelSet().setWestYSize(sz, force);
    }

    void reinitWestYSize() {
	getGraphPanelSet().reinitWestYSize();
    }

    public SystemConfig getSystemConfig() {
	return systemConfig;
    }

    void paintMarksAndRegions(Graphics g, PrintContext pctx) {
	paintMarksAndRegions(g, pctx, true);
    }

    void paintMarksAndRegions(Graphics g, PrintContext pctx,
			      boolean fill) {
	if (isPaintingRegionMode(ASYNC_MODE))
	    return;

	if (sizeSets == 0)
	    return;

	int regionsSize = getRegions().size();
	int marksSize = getMarks().size();

	if (regionsSize == 0 && marksSize == 0)
	    return;

	// 14/09/04: Mark.getRDelta() needs to have templateDS.rbounds computed
	if (isModified()) {
	    GraphElement ds = getTemplateDSorProfile();
	    if (ds != null)
		getGraphElementDisplayer(ds).display(this, null, ds, 0, null);
	}
	
	Rectangle2D.Double rect;
	if (pctx != null)
	    rect = pctx.getBounds();
	else {
	    Dimension size = getSize();
	    rect = new Rectangle2D.Double(0, 0, size.width, size.height);
	}

	if (regionsSize > 0) {
	    for (int i = regionsSize-1; i >= 0; i--) {
		if (isPaintingRegionMode(ASYNC_MODE)) {
		    System.out.println("paintMarksAndRegions avoid exception");
		    return;
		}
		Region region = (Region)getRegions().get(i);
		double rbegx = region.getBegin().getRX(this);
		double rendx = region.getEnd().getRX(this);
		if (pctx != null) {
		    rbegx = pctx.getRX(rbegx);
		    rendx = pctx.getRX(rendx);
		}

		if (rbegx < rect.x + rect.width && rendx > rect.x) {
		    g.setColor(region.getColor());

		    double width = rendx-rbegx;
		    if (width < 1.)
			width = 1.;

		    if (rbegx < 0) {
			width += rbegx;
			rbegx = 0;
		    }

		    if (width > rect.width)
			width = rect.width;

		    if (fill) {
			g.fillRect((int)rbegx, (int)rect.y,
				   (int)width, (int)rect.height);
			if (graphPanel.highlightMinimalRegions() ||
			    graphPanel.highlightRecurrentAlterations()) {
			    Rectangle2D.Double regionBounds =
				new Rectangle2D.Double(rbegx, rect.y, width, rect.height);
			    highlight(graphPanel, g, region, regionBounds, pctx);
			}
		    }

		    if (region.isSelected()) {
			Color regionSelectedFG = VAMPResources.getColor
			    (VAMPResources.REGION_SELECTED_FG);
			g.setColor(regionSelectedFG);
		    }
		    
		    if (!fill || region.isSelected()) {
			g.fillRect((int)rbegx, (int)rect.y,
				   EPSILON, (int)rect.height);
			g.fillRect((int)(rbegx + width-EPSILON), (int)rect.y,
				   EPSILON, (int)rect.height);
		    }
		}
	    }
	}

	if (marksSize > 0) {
	    Color centeredMarkFG = VAMPResources.getColor
		(VAMPResources.MARK_CENTERED_FG);
	    Color markSelectedFG = VAMPResources.getColor
		(VAMPResources.MARK_SELECTED_FG);
	    for (int i = marksSize-1; i >= 0; i--) {
		int offset;
		if (isPaintingRegionMode(ASYNC_MODE)) {
		    System.out.println("paintMarksAndRegions avoid exception");
		    return;
		}
		Mark mark = (Mark)getMarks().get(i);
		if (mark.getRegion() != null) {
		    if (fill &&
			!mark.isSelected() && !mark.isCentered() &&
			mark.getColor().equals(mark.getRegion().getColor()))
			continue;

		    if (mark.isEndingRegion())
			offset = -EPSILON;
		    else
			offset = 0;
		}
		else
		    offset = -EPSILON2;

		double rx = mark.getRX(this);
		if (pctx != null)
		    rx = pctx.getRX(rx);

		if (mark.isSelected())
		    g.setColor(markSelectedFG);
		else if (mark.isCentered())
		    g.setColor(centeredMarkFG);
		else
		    g.setColor(mark.getColor());
		//g.setColor(Color.BLACK);
		g.fillRect((int)rx+offset, (int)rect.y, EPSILON, (int)rect.height);
		if (graphPanel.highlightMinimalRegions() ||
		    graphPanel.highlightRecurrentAlterations()) {
		    Rectangle2D.Double markBounds =
			new Rectangle2D.Double(rx+offset, rect.y,
					       EPSILON, rect.height);
		    highlight(graphPanel, g, mark, markBounds, pctx);
		}
		// vertical draw
		//g.fillRect(0, rx-EPSILON2, size.width, EPSILON);
	    }
	}
    }

    View getView() {return view;}

    private void displayGraphElementState(Graphics g, GraphElement graphElement) {
	if (graphElement.isSelected())
	    paintSelect(g, graphElement);
	else if (graphElement.isPinnedUp())
	    paintPinnedUp(g, graphElement);
	else if (!graphElement.isFullImported())
	    paintLightImported(g, graphElement);
	else
	    paintGraphElement(g, graphElement, getBGColor());
    }

    void setSelected(Selectable selectable, boolean select) {
	setSelected(selectable, select, false);
    }

    void setSelected(Selectable selectable, boolean select, boolean immediate) {
	if ((select && !selectable.isSelected()) ||
	    (!select && selectable.isSelected())) {
	    selectable.setSelected(select, this);
	    
	    if (!useOptSel() || immediate)
		return;

	    if (selectable instanceof Mark ||
		selectable instanceof Region) {
		paintMarksAndRegions((Graphics2D)getGraphics(), null, false);
		return;
	    }

	    if (!(selectable instanceof GraphElement))
		return;

	    GraphElement graphElement = (GraphElement)selectable;
	    Graphics2D g2 = (Graphics2D)getGraphics();

	    displayGraphElementState(g2, graphElement);
	    Rectangle2D.Double rBounds = graphElement.getRBounds();

	    Rectangle clip = g2.getClipBounds();
	    g2.clipRect((int)rBounds.x, (int)(rBounds.y - rBounds.height),
			(int)rBounds.width, (int)rBounds.height);
	    
	    paintMarksAndRegions(g2, null);
	    g2.setClip(clip);

	    int m = graphElements.indexOf(graphElement);
	    getGraphElementDisplayer(graphElement).display(this, g2,
							   graphElement, m, null);
	    getAxisDisplayer(graphElement).display(this, g2, graphElement, m, null);
	}
    }

    DataElement getCenteredElem() {
	return centeredElem;
    }

    Probe getCenteredProbe() {
	return centeredProbe;
    }

    boolean useOptSel() {
	if (USE_OPT_SEL)
	    return getRegions().size() == 0 &&
		graphPanel.getDefaultGraphElementDisplayer().useOptSelection();
	return false;
    }

    void invalidateOffscreen() {
	for (int m = 0; m < sizeSets; m++)
	    getGraphElement(m).setOffScreenValid(false);
    }

    boolean isReadOnly() {return isReadOnly;}

    GraphPanel getGraphPanel() {return graphPanel;}

    public double getChrPosX(String organism, String chr, double posx) {
	for (int m = 0; m < sizeSets; m++) {
	    GraphElement graphElement = getGraphElement(m);
	    DataSet set = graphElement.asDataSet();
	    if (set == null) continue;
	    String os = VAMPUtils.getOS(set);
	    if (!organism.equals(os)) continue;

	    Vector chr_cache = ChromosomeNameAxisDisplayer.computeChrCache
		(set, false);
	    int size = chr_cache.size();

	    for (int i = 0; i < size; i += 2) {
		int n = ((Integer)chr_cache.get(i)).intValue();
		String c = (String)chr_cache.get(i+1);
		if (!c.equals(chr)) continue;
		Double lastPos =
		    (Double)set.getData()[n].getPropertyValue(VAMPProperties.MergeOffsetProp);
		if (lastPos != null)
		    return lastPos.doubleValue() + posx;
		return posx;
	    }
	}

	return posx;
    }

    GraphElement getPinnedUpSet() {return pinnedUpGraphElem;}
    DataElement getPinnedUpElem() {return pinnedUpElem;}

    GraphElement getCenteredSet() {return centeredGraphElem;}

    void push_back(Region region) {
	if (getRegions().remove(region))
	    getRegions().addLast(region);
    }

    void push_front(Region region) {
	if (getRegions().remove(region))
	    getRegions().addFirst(region);
    }

    boolean isBack(Region region) {
	return isBackOrFront(region, true);
    }

    boolean isFront(Region region) {
	return isBackOrFront(region, false);
    }

    boolean isBackOrFront(Region region, boolean back) {
	int size = getRegions().size();
	if (size == 1)
	    return false;

	double begx = region.getBegin().getRX(this);
	double endx = region.getEnd().getRX(this);
	boolean pass = false;
	for (int i = size-1; i >= 0; i--) {
	    Region r = (Region)getRegions().get(i);
	    if (region == r) {
		pass = true;
		continue;
	    }
	    
	    double r_begx = r.getBegin().getRX(this);
	    double r_endx = r.getEnd().getRX(this);

	    if ((begx >= r_begx && begx <= r_endx) ||
		(endx >= r_begx && endx <= r_endx) ||
		(r_begx >= begx && r_begx <= endx) ||
		(r_endx >= begx && r_endx <= endx)) {
		// overlapped
		if (back && pass)
		    return true;
		if (!back && !pass)
		    return true;
	    }
	}
	return false;
    }

    private boolean shouldHighlight(GraphPanel panel,
				     PropertyElement elem,
				     GraphElement graphElem) {
	Vector v;
	if (VAMPUtils.getType(elem).equals(VAMPConstants.MINIMAL_REGION_TYPE)) {
	    if (!panel.highlightMinimalRegions())
		return false;
	    v = (Vector)elem.getPropertyValue(GenomeAlterationOP.SupportVProp);
	}
	else if (VAMPUtils.getType(elem).equals(VAMPConstants.RECURRENT_REGION_TYPE) ||
		 VAMPUtils.getType(elem).equals(VAMPConstants.RECURRENT_BREAKPOINT_TYPE)) {
	    if (!panel.highlightRecurrentAlterations())
		return false;
	    v = (Vector)elem.getPropertyValue(GenomeAlterationOP.SupportVProp);
	}
	else
	    return false;

	if (v == null)
	    return false;
	int sz = v.size();
	String id = (String)graphElem.getID();
	for (int n = 0; n < sz; n++)
	    if (((GraphElement)v.get(n)).getID().equals(id))
		return true;
	return false;
    }

    private void highlight(GraphPanel panel, Graphics g, PropertyElement elem,
			    Rectangle2D.Double elemBounds,
			    PrintContext pctx) {
	if (elem instanceof Region)
	    g.setColor(Color.GRAY);
	else
	    g.setColor(Color.BLUE);
	//g.setColor(Color.ORANGE);
	for (int m = 0; m < sizeSets; m++) {

	    GraphElement graphElem = getGraphElement(m);
	    if (!shouldHighlight(panel, elem, graphElem))
		continue;
	    Rectangle2D.Double graphElemBounds = graphElem.getRBounds();
	    graphElemBounds = (Rectangle2D.Double)graphElemBounds.clone();
	    graphElemBounds.y -= graphElemBounds.height;

	    graphElemBounds.x -= 4;
	    graphElemBounds.width += 8;

	    if (pctx != null) {
		graphElemBounds.x = pctx.getRX(graphElemBounds.x);
		graphElemBounds.y = pctx.getRY(graphElemBounds.y);
		graphElemBounds.width = pctx.getRW(graphElemBounds.width);
		graphElemBounds.height = pctx.getRH(graphElemBounds.height);
	    }

	    Rectangle2D.Double dest = new Rectangle2D.Double();
	    Rectangle2D.intersect(graphElemBounds, elemBounds, dest);

	    double x = dest.x;
	    double y = dest.y;
	    double width = dest.width;
	    double height = dest.height;

	    if (width < 2)
		width = 2;
	    if (height < 2)
		height = 2;

	    /*
	      if (pctx != null) {
	      x = pctx.getRX(x);
	      y = pctx.getRY(y);
	      width = pctx.getRW(width);
	      height = pctx.getRH(height);
	      }
	    */

	    g.fillRect((int)x, (int)y, (int)width, (int)height);
	}
    }

    private void sortArrays(PropertyElement elem) {
	Vector v = (Vector)elem.getPropertyValue(GenomeAlterationOP.SupportVProp);
	if (v.size() == 0)
	    return;
	
	if (!graphElements.contains(v.get(0))) {
	    // EV : 21/02/07
	    // graphElem from support is not in canvas: multiple panels
	    return;
	}

	LinkedList l = Utils.vectorToList(v);

	for (int n = 0; n < sizeSets; n++) { 
	    GraphElement graphElem = (GraphElement)graphElements.get(n);

	    if (!l.contains(graphElem))
		l.add(graphElem);
	}

	setGraphElements(l);
    }	    

    public void recenter() {
	center(getVCenter());
    }

    boolean hasPinnedUp() {
	return pinnedUpElem != null ||
	    pinnedUpGraphElem != null ||
	    pinnedUpRegion != null;
    }

    public void clearPinnedUp() {
	if (pinnedUpGraphElem != null) {
	    pinnedUpGraphElem.setPinnedUp(false);
	    pinnedUpGraphElem = null;
	}

	if (pinnedUpElem != null) {
	    pinnedUpElem.setPinnedUp(false);
	    pinnedUpElem = null;
	}

	if (pinnedUpRegion != null) {
	    pinnedUpRegion.setPinnedUp(false);
	    pinnedUpRegion = null;
	}

	if (pinnedUpProbe != null) {
	    pinnedUpProbe.release();
	    pinnedUpProbe = null;
	}

	manageInfo();
    }

    void setColor(DendrogramGraphElement dendroGE, int mode) {
	Color color = JColorChooser.showDialog
	    (view,
	     "SubTree Color",
	     dendroGE.getColor());
	if (color != null) {
	    dendroGE.setColor(color, mode);
	    repaint();
	}
    }

    void setPinnedUp(GraphElement dataSet, DataElement dataElement) {
	clearPinnedUp();
	pinnedUpElem = dataElement;
	pinnedUpElem.setPinnedUp(true);
	Rectangle2D.Double rBounds = pinnedUpElem.getRBounds(dataSet);
	pinnedUpGraphElem = dataSet;
	if (pinnedUpGraphElem != null)
	    pinnedUpGraphElem.setPinnedUp(true);
	manageInfoInPopupMenu();
	repaint();
    }

    void setPinnedUp(GraphElement graphElem, Probe probe) {
	clearPinnedUp();
	pinnedUpProbe = probe;
	try {
	    graphElem.asProfile().complete(pinnedUpProbe);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
	pinnedUpGraphElem = graphElem;
	if (pinnedUpGraphElem != null)
	    pinnedUpGraphElem.setPinnedUp(true);
	manageInfoInPopupMenu();
	repaint();
    }

    // canvas state

    static class State {
	LinkedList graphElements, regions, marks;
	Scale scale;
	Scale canonScale;
	Point2D.Double rorig;
	boolean fitInPage;

	State(GraphCanvas canvas, boolean clone) {
	    fitInPage = canvas.isFitInPage();
	    scale = new Scale(canvas.getScale().getScaleX(), canvas.getScale().getScaleY());
	    canonScale = new Scale(canvas.getCanonScale().getScaleX(),
				   canvas.getCanonScale().getScaleY());
	    rorig = new Point2D.Double(canvas.getOrig().x, canvas.getOrig().y);
	    if (clone) {
		graphElements = (LinkedList)canvas.getGraphElements().clone();
		regions = (LinkedList)canvas.getRegions().clone();
		marks = (LinkedList)canvas.getMarks().clone();
	    }
	    else {
		graphElements = canvas.getGraphElements();
		regions = canvas.getRegions();
		marks = canvas.getMarks();
	    }
	}

	boolean restore(GraphCanvas canvas) {
	    boolean active = 
		UndoVMStack.getInstance(canvas.getGraphPanel()).setActive(false);

	    boolean diff = false;
	    boolean set_ge = false;
	    if (!canvas.getGraphElements().equals(graphElements)) {
		canvas.setGraphElements(graphElements);
		diff = true;
		set_ge = true;
	    }

	    if (!canvas.getRegions().equals(regions)) {
		canvas.setRegions(regions);
		diff = true;
	    }

	    if (!canvas.getMarks().equals(marks)) {
		canvas.setMarks(marks);
		diff = true;
	    }

	    if (canvas.isFitInPage() != fitInPage) {
		canvas.setFitInPage(fitInPage);
		canvas.getView().getZoomPanel().setFitInPage(fitInPage);
		diff = true;
	    }

	    if (!scale.equals(canvas.getScale())) {
		canvas.getView().getZoomPanel().setScaleAndValue(canonScale);
	    }

	    if (canvas.getScale().getScaleX() != scale.getScaleX()) {
		canvas.setScaleX(scale.getScaleX()/canvas.getBFScale().getScaleX());
		diff = true;
	    }

	    if (canvas.getScale().getScaleY() != scale.getScaleY()) {
		canvas.setScaleY(scale.getScaleY()/canvas.getBFScale().getScaleY());
		diff = true;
	    }

	    if (!canonScale.equals(canvas.getCanonScale())) {
		canvas.setCanonScale(canonScale);
		diff = true;
	    }

	    if (!canvas.getOrig().equals(rorig)) {
		canvas.setOrig(rorig);
		canvas.adaptScroll();
		diff = true;
	    }

	    if (diff) {
		canvas.getGraphPanel().syncGraphElements(true);
		//canvas.repaint();
	    }

	    UndoVMStack.getInstance(canvas.getGraphPanel()).setActive(active);
	    return diff;
	}

	boolean equals(State state) {
	    if (fitInPage != state.fitInPage)
		return false;

	    if (!rorig.equals(state.rorig))
		return false;

	    if (!scale.equals(state.scale))
		return false;
	    
	    if (!graphElements.equals(state.graphElements))
		return false;

	    if (!regions.equals(state.regions))
		return false;

	    if (!marks.equals(state.marks))
		return false;

	    /*
	    if (graphElements.size() != state.graphElements.size())
		return false;
	    if (getRegions().size() != state.getRegions().size())
		return false;
	    if (getMarks().size() != state.getMarks().size())
		return false;

	    int sz = graphElements.size();
	    for (int n = 0; n < sz; n++)
		if (graphElements.get(n) != state.graphElements.get(n))
		    return false;

	    sz = getRegions().size();
	    for (int n = 0; n < sz; n++)
		if (getRegions().get(n) != state.getRegions().get(n))
		    return false;

	    sz = getMarks().size();
	    for (int n = 0; n < sz; n++)
		if (getMarks().get(n) != state.getMarks().get(n))
		    return false;

	    */

	    return true;
	}
    }

    void centerOnSubtree(DendrogramGraphElement dendroGE) {
	getGraphPanelSet().clearCenter(which);

	centeredDendroGE = dendroGE;
	centeredDendroGEColor = dendroGE.getColor();
	dendroGE.setColor(Color.BLUE, DendrogramGraphElement.DEPTH_MODE);
	if (!dendroGE.isBridge())
	    dendroGE.setColor(centeredDendroGEColor,
			      DendrogramGraphElement.LOCAL_MODE);
	DendrogramBinNode node =
	    (DendrogramBinNode)dendroGE.getDendrogramNode();
	double left_pos = node.getExtLeftPos();
	double right_pos = node.getExtRightPos();
	double v = left_pos + (right_pos - left_pos) / 2;

	boolean x_type = (node.getDendrogram().getType() == Dendrogram.X_TYPE);
	Scale canonScale;
			
	if (x_type)
	    canonScale = new Scale((getVirtualSize().width +
				    getVW(rmargins.getMarginWidth()))/(right_pos - left_pos), getCanonScale().getScaleY());
	else 
	    canonScale = new Scale(getCanonScale().getScaleX(),
				   (getVirtualSize().height +
				    getVH(rmargins.getMarginHeight()))/(right_pos - left_pos));
			
	int slider_ind = getView().getZoomPanel().
	    getSliderInd(graphPanel.getWhich(), x_type);

	getView().getZoomPanel().setScaleAndValue(slider_ind,
						  canonScale);

	if (x_type)
	    setVCenter(new Point2D.Double(v, 0));
	else
	    setVCenter(new Point2D.Double(0, v));

	view.repaint();
    }

    public Object getLockObj() {
	return lockObj;
    }

    /*
    public fr.curie.vamp.gui.Scale makeGScale(Profile profile) {
	return new fr.curie.vamp.gui.Scale
	    (getScale().getScaleX(),
	     (int)getRX(0, 0),
	     getScale().getScaleY(),
	     (int)getRY(profile.getVBounds().y - profile.getVBounds().height/2));
    }
    */
}
