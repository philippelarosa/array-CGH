
/*
 *
 * VAMPAppli.java
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
import java.text.DateFormat;
import java.io.*;
import java.net.*;

class VAMPAppli {

    static private void usage() {
	System.err.println("VAMP Version " + VersionManager.getStringVersion() +
			   ": usage: " + VAMPApplib.getSupportedTypes() +
			   "|LOAD <sysconfig_url> {<dataset_url>} ");
	System.exit(1);
    }

    static public void main(String args[]) {
	if (args.length < 3) {
	    usage();
	    return;
	}

	String type = args[0];
	String sys_cfg = args[1];
	String import_data_url = args[2];
	String cookie = args[3];
	String login = args[4];

	Utils.setCookie(cookie, false);

	System.out.println(VAMPUtils.getTitle() + " Version " + VersionManager.getStringVersion() + " " + VersionManager.getPatchLevel());

	VAMP.init();

	ViewFrame view, view2, view3, view4, view5;
	GraphElementIDBuilder dataSetIDBuilder = null;

	if (!type.equals("LOAD")) {
	    dataSetIDBuilder = VAMPApplib.getIDBuilder(type);
	    if (dataSetIDBuilder == null)
		usage();
	}
	
	GlobalContext globalContext = new GlobalContext(null, null);
	SystemConfig sysCfg = null;
	if (sys_cfg != null && sys_cfg.length() > 0) {
	    sysCfg = SystemConfig.build(globalContext, sys_cfg);
	    if (sysCfg == null)
		System.exit(0);
	}

	if (!LicenseClient.checkLicense(globalContext, sysCfg, false))
	    System.exit(1);

	VAMP.init(globalContext);
	VAMPUtils.setLogin(login);
	globalContext.put(VAMPConstants.IMPORT_DATA_URL, import_data_url);

	/*
	System.out.println(VAMPUtils.getTitle() + " Version " + VersionManager.getStringVersion() + " " + VersionManager.getPatchLevel());
	*/

	VAMPApplib.loadPlugins(sysCfg, globalContext);

	long min_memory = Integer.parseInt(sysCfg.getParameter("min:memory"));
	long max_memory = Runtime.getRuntime().maxMemory()/(1024*1024);
	if (max_memory < min_memory) {
	    InfoDialog.pop(globalContext,
			   "Warning: the maximum Java memory size is " +
			   max_memory + " MB.\n" +
			   "The minimum recommended value is " + min_memory +
			   " MB");
	}

	if (type.equals("LOAD")) {
	    XMLLoadDataFactory ldf =
		new XMLLoadDataFactory(globalContext, true, false);

	    for (int n = 5; n < args.length; n++) {
		try {
		    InputStream is = Utils.openStream(args[n]);
		    ViewFrame vf = ldf.makeViewFrame(globalContext, is,
						     args[n]);
		    if (vf != null)
			vf.setVisible(true);
		}
		catch(Exception e) {
		    e.printStackTrace();
		}
	    }

	    return;
	}


	XMLArrayDataFactory factory =
	    new XMLArrayDataFactory(globalContext, null);

	/*
	O[] o = new O[100000];
	DataElement[] data = new DataElement[100000];
	Property props[] = new Property[40];
	Integer ii = new Integer(0);

	for (int n = 0; n < props.length; n++)
	    props[n] = Property.getProperty("prop#" + n);

	VAMPConstants.gc();
	VAMPConstants.freeMemory();
	VAMPConstants.gc();
	VAMPConstants.freeMemory();

	long busy = VAMPConstants.busyMemory();

	for (int i = 0; i < data.length; i++) {
	    data[i] = new DataElement();
	    DataElement d = data[i];
	    for (int n = 0; n < props.length; n++) {
		d.setPropertyValue(props[n], new byte[12]);
	    }
	}

	VAMPConstants.gc();
	VAMPConstants.gc();
	VAMPConstants.gc();
	VAMPConstants.gc();
	long used = VAMPConstants.busyMemory() - busy;
	System.out.println(data.length + ":" + props.length + " used " + (used/1000.));

	System.exit(0);
	*/

	Dimension dim = new Dimension(400, 700);

	Scale scale = null;
	PanelProfile panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     Config.defaultMargins,
	     null);
						     
	PanelProfile panelProfiles[] = new PanelProfile[]{panelProfile};

	view = new ViewFrame(globalContext,
			     "Simple View",
			     panelProfiles,
			     null,
			     null,
			     null, null,
			     new LinkedList(), Config.defaultDim,
			     null);

	LinkedList graphElements = VAMPApplib.makeGraphElements
	    (globalContext, view.getView(), view.getView().getPanel(0), type, args, 5);

	if (graphElements.size() > 0) {
	    view.getView().getPanel(0).setGraphElements(graphElements);

	    SwingUtilities.invokeLater(new Printer(globalContext, view.getView(), "/home/eric/hermes_template_CGH.xml", "/tmp/toto.png", true));

	    return;
	}

	// WARNING: suppressed the 28/01/05
	/*
	VAMPApplib.makeGraphElements(view.getView(),
				     view.getView().getPanel(0),
				     type, graphElements);

	*/

	//InfoFrame.pop(globalContext);
	if (true) {
	    view.setVisible(true);
	    return;
	}

	PanelProfile panelProfile_top = new PanelProfile
	    ("Top",
	     new int[]{50, 0, 90, 0},
	     PanelProfile.SCROLL_NORTH|PanelProfile.SCROLL_EAST,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     //true,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     Config.defaultMargins,
	     null);
						     

	PanelProfile panelProfile_bottom = new PanelProfile
	    ("Bottom",
	     new int[]{0, 50, 90, 0},
	     PanelProfile.SCROLL_SOUTH|PanelProfile.SCROLL_EAST,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     //true,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     Config.defaultMargins,
	     null);
						     

	panelProfiles = new PanelProfile[]{panelProfile_top,
					    panelProfile_bottom};

	view2 = new ViewFrame(globalContext,
			     "Horizontal 2-panel View",
			      panelProfiles,
			      new PanelSplitLayout
			      (PanelSplitLayout.HORIZONTAL,
			       new PanelFinalLayout(0),
			       new PanelFinalLayout(1)),
			      new PanelLinks[]{
				  new PanelLinks("Both",
						 GraphPanel.SYNCHRO_X,
						 new int[]{0, 1})},
			      null, null,
			      new LinkedList(), Config.defaultDim,
			      null);

	PanelProfile panelProfile_left = new PanelProfile
	    ("Left",
	     new int[]{50, 50, 90, 0},
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 40, 30, 20),
	     null);
						     

	PanelProfile panelProfile_right = new PanelProfile
	    ("Right",
	     new int[]{50, 50, 0, 0},
	     PanelProfile.SCROLL_EAST|PanelProfile.SCROLL_SOUTH,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 20, 30, 30),
	     null);
						     

	panelProfiles = new PanelProfile[]{panelProfile_left,
					    panelProfile_right};

	view3 = new ViewFrame(globalContext,
			     "Vertical 2-panel View",
			      panelProfiles,
			      new PanelSplitLayout(PanelSplitLayout.VERTICAL,
						   new PanelFinalLayout(0),
						   new PanelFinalLayout(1)),
			      new PanelLinks[]{
				  new PanelLinks("Both",
						 GraphPanel.SYNCHRO_Y,
						 new int[]{0, 1})},
			      null, null,
			      new LinkedList(), Config.defaultDim,
			      null);

	PanelProfile panelProfile_1 = new PanelProfile
	    ("Left",
	     new int[]{50, 50, 90, 0},
	     PanelProfile.SCROLL_SOUTH|PanelProfile.SCROLL_WEST,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 10, 30, 0),
	     null);

	PanelProfile panelProfile_2 = new PanelProfile
	    ("Middle Left",
	     new int[]{50, 50, 0, 0},
	     PanelProfile.SCROLL_SOUTH|PanelProfile.SCROLL_EAST,
	     new CurveDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 0, 30, 0),
	     null);

	PanelProfile panelProfile_3 = new PanelProfile
	    ("Middle Right",
	     new int[]{50, 50, 0, 0},
	     PanelProfile.SCROLL_SOUTH,
	     new BarplotDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 0, 30, 0),
	     null);

	PanelProfile panelProfile_4 = new PanelProfile
	    ("Right",
	     new int[]{50, 50, 0, 0},
	     PanelProfile.SCROLL_SOUTH|PanelProfile.SCROLL_WEST,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 0, 30, 10),
	     null);

	panelProfiles = new PanelProfile[]{panelProfile_1,
					    panelProfile_2,
					    panelProfile_3,
					    panelProfile_4};

	view4 = new ViewFrame
	    (globalContext,
	     "Vertical 4-panel View",
	     panelProfiles,
	     new PanelSplitLayout(PanelSplitLayout.VERTICAL,
				  new PanelSplitLayout(PanelSplitLayout.VERTICAL,
						       new PanelSplitLayout(PanelSplitLayout.VERTICAL,
									    new PanelFinalLayout(0),
									    new PanelFinalLayout(1)),
						       new PanelFinalLayout(2)),
				  new PanelFinalLayout(3)),
	     new PanelLinks[]{
		 new PanelLinks("All",
				GraphPanel.SYNCHRO_Y,
				new int[]{0, 1, 2, 3})},
	     null, null,
	     new LinkedList(), Config.defaultDim,
	     null);

	panelProfile_1 = new PanelProfile
	    ("North/West",
	     new int[]{50, 0, 90, 0},
	     PanelProfile.SCROLL_NORTH|PanelProfile.SCROLL_WEST,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 20, 30, 0),
	     null);

	panelProfile_2 = new PanelProfile
	    ("North/East",
	     new int[]{50, 0, 0, 0},
	     PanelProfile.SCROLL_NORTH|PanelProfile.SCROLL_EAST,
	     new CurveDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 0, 30, 0),
	     null);

	panelProfile_3 = new PanelProfile
	    ("South/West",
	     new int[]{0, 50, 90, 0},
	     PanelProfile.SCROLL_SOUTH|PanelProfile.SCROLL_WEST,
	     new BarplotDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 20, 30, 0),
	     null);

	panelProfile_4 = new PanelProfile
	    ("South/East",
	     new int[]{0, 50, 0, 0},
	     PanelProfile.SCROLL_SOUTH|PanelProfile.SCROLL_EAST,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     dataSetIDBuilder,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     scale,
	     0,
	     true,
	     new Margins(30, 0, 30, 0),
	     null);

	panelProfiles = new PanelProfile[]{panelProfile_1,
					   panelProfile_2,
					   panelProfile_3,
					   panelProfile_4};

	view5 = new ViewFrame
	    (globalContext,
	     "Vertical/Horizontal 4-panel View",
	     panelProfiles,
	     new PanelSplitLayout
	     (PanelSplitLayout.HORIZONTAL,
	      new PanelSplitLayout(PanelSplitLayout.VERTICAL,
				   new PanelFinalLayout(0),
				   new PanelFinalLayout(1)),
	      new PanelSplitLayout(PanelSplitLayout.VERTICAL,
				   new PanelFinalLayout(2),
				   new PanelFinalLayout(3))),
	     new PanelLinks[]{
		 new PanelLinks("North/West-South/West",
				GraphPanel.SYNCHRO_X,
				new int[]{0, 2}),
		 new PanelLinks("North/East-South/East",
				GraphPanel.SYNCHRO_X,
				new int[]{1, 3}),
		 new PanelLinks("North/West-North/East",
				GraphPanel.SYNCHRO_Y,
				new int[]{0, 1}),
		 new PanelLinks("South/West-South/East",
				GraphPanel.SYNCHRO_Y,
				new int[]{2, 3}),
	     },
	     null, null,
	     new LinkedList(), Config.defaultDim,
	     null);

	//Runtime.getRuntime().traceMethodCalls(true);

	factory = null;
	view.setVisible(true);
	view.getView().syncGraphElements(true);

	if (true) {
	    //view2.setVisible(true);
	    //view3.setVisible(true);
	    //view4.setVisible(true);

	    view5.setVisible(true);
	}
    }

    static class Printer implements Runnable {
	GlobalContext globalContext;
	View view;
	String templateFile;
	String outputFile;
	boolean gnl;

	Printer(GlobalContext globalContext, View view, String templateFile, String outputFile, boolean gnl) {
	    this.globalContext = globalContext;
	    this.view = view;
	    this.templateFile = templateFile;
	    this.outputFile = outputFile;
	    this.gnl = gnl;
	}

	public void run() {
	    // initialization
	    view.setSize(new Dimension(300, 400));

	    view.syncGraphElements();

	    GraphPanel panel = view.getGraphPanelSet().getPanel(0);
	    GraphCanvas canvas = panel.getCanvas();
	    canvas.setSize(new Dimension(300, 400));
	    canvas.updateSize(true);

	    LinkedList graphElements = view.getGraphElements(0);
	    for (int n = 0; n < graphElements.size(); n++) {
		GraphElement graphElement = (GraphElement)graphElements.get(n);
		graphElement.setRBounds(canvas.getRRect(graphElement.getPaintVBounds()));
	    }

	    // setting GNL or default color codes
	    GraphElementDisplayer ds = panel.getDefaultGraphElementDisplayer();
	    if (ds != null && ds instanceof CommonDataSetDisplayer) {
		((CommonDataSetDisplayer)ds).setGNLColorCodes(gnl);
	    }

	    // printing
	    PrintPreviewer printPreviewer = PrintPreviewer.getInstance(globalContext);
	    printPreviewer.preview(view, null, false);

	    // loading template
	    File file = new File(templateFile);
	    XMLLoadPrintTemplate loadPrint = new XMLLoadPrintTemplate(globalContext, false);
	    PrintPageTemplate template = loadPrint.getTemplate(file);
	    
	    printPreviewer.updateTemplates();
	    printPreviewer.init(template, 0, 0);

	    // exporting in PNG
	    PrintExportDialog printExportDialog = PrintExportDialog.getInstance(globalContext);
	    printExportDialog.exportPNG(printPreviewer, outputFile);

	    System.exit(0);
	}
    }
}
