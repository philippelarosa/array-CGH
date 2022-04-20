
/*
 *
 * VAMP2Image.java
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

class VAMP2Image {

    static private void printVersion() {
	System.err.println("VAMP2Image Version 1.1 based on VAMP Library Version " + VersionManager.getStringVersion() + " " + VersionManager.getPatchLevel() + "\n");
    }

    static private void usage() {
	printVersion();
	System.err.println("usage: VAMP2Image SYSCONFIG_URL cgh_chr|cgh_pangen|snp_chr|snp_pangen PROFILE_URL gnl|default OUTPUT_PNG_FILE TEMPLATE\nexample:\nVAMP2Image syscfg.xml pangen http://bioinfo.curie.fr/cghaia/.../array/my_profile.xml gnl /tmp/alpha.png /home/xxx/hermes_template_CGH.xml");
	System.exit(1);
    }

    static public void main(String args[]) {
	if (args.length != 6) {
	    usage();
	    return;
	}

	String sys_cfg = args[0];
	String type = args[1];
	String url = args[2];
	String gnl = args[3];
	String output = args[4];
	String template = args[5];

	String whatType;
	if (type.equalsIgnoreCase("cgh_chr")) {
	    whatType = "CGH_CHR";
	}
	else if (type.equalsIgnoreCase("cgh_pangen")) {
	    whatType = "CGH_CHRMERGE";
	}
	else if (type.equalsIgnoreCase("snp_chr")) {
	    whatType = "SNP";
	}
	else if (type.equalsIgnoreCase("snp_pangen")) {
	    whatType = "SNP_CHRMERGE";
	}
	else {
	    usage();
	    return;
	}

	boolean isGNL = false;
	if (gnl.equalsIgnoreCase("gnl")) {
	    isGNL = true;
	}
	else if (gnl.equalsIgnoreCase("default")) {
	    isGNL = false;
	}
	else {
	    usage();
	    return;
	}

	VAMP.init();

	ViewFrame view;
	GraphElementIDBuilder dataSetIDBuilder = null;

	dataSetIDBuilder = VAMPApplib.getIDBuilder(whatType);
	if (dataSetIDBuilder == null) {
	    usage();
	}
	
	GlobalContext globalContext = new GlobalContext(null, null);

	SystemConfig sysCfg = SystemConfig.build(globalContext, sys_cfg);
	if (sysCfg == null) {
	    System.exit(0);
	}

	VAMP.init(globalContext);

	printVersion();

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

	System.err.println("Importing " + url);
	LinkedList graphElements = VAMPApplib.makeGraphElements
	    (globalContext, view.getView(), view.getView().getPanel(0), whatType, args, 2, 3);

	view.getView().getPanel(0).setGraphElements(graphElements);
	
	SwingUtilities.invokeLater(new Printer(globalContext, view.getView(), template, output, isGNL));
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
	    
	    if (template == null) {
		System.exit(1);
	    }

	    printPreviewer.updateTemplates();
	    printPreviewer.init(template, 0, 0);

	    // exporting in PNG
	    PrintExportDialog printExportDialog = PrintExportDialog.getInstance(globalContext);
	    printExportDialog.exportPNG(printPreviewer, outputFile);

	    System.exit(0);
	}
    }
}
