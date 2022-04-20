
/*
 *
 * VAMPApplet.java
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
import java.io.*;
import java.net.*;

public class VAMPApplet extends JApplet {

    View mainView;
    XMLLoadDataFactory ldf;
    GlobalContext globalContext = null;
    String appletHomeImg;
    ViewFrame vf;

    public void init() {

	Utils.gc();

	String url = getParameter("url");
	String type = getParameter("vtype");
	String import_data_url = getParameter("import_data_url");
	String login = getParameter("login");
	String cookie = getParameter("cookie");

	if (type == null) {
	    System.err.println("Applet param vtype is mandatory");
	    return;
	}

	Utils.setCookie(cookie, true);

	if (!type.equals("LOAD") && url == null) {
	    System.err.println("Applet param url is mandatory when vtype is "
			       + type);
	    return;
	}


	boolean displayChr;

	System.out.println(VAMPUtils.getTitle() + " Version " + VersionManager.getStringVersion() + " " + VersionManager.getPatchLevel());

	VAMP.init();
	globalContext = new GlobalContext(getAppletContext(),
					  getCodeBase().toString());

	SystemConfig sysCfg = getSystemConfig(globalContext);
	if (sysCfg == null) 
	    return;

	if (!LicenseClient.checkLicense(globalContext, sysCfg, false))
	    return;

	appletHomeImg = sysCfg.getParameter("applet_home_img:URL");

	VAMP.init(globalContext);
	VAMPUtils.setLogin(login);
	globalContext.put(VAMPConstants.IMPORT_DATA_URL, import_data_url);

	checkMemory(globalContext, sysCfg);

	/*
	System.out.println(VAMPUtils.getTitle() + " Version " + VersionManager.getStringVersion() + " " + VersionManager.getPatchLevel());
	*/

	VAMPApplib.loadPlugins(sysCfg, globalContext);

	GraphElementIDBuilder dataSetIDBuilder;

	if (type.equals("LOAD")) {
	    loadPerform(globalContext);
	    return;
	}

	dataSetIDBuilder = VAMPApplib.getIDBuilder(type);
	if (dataSetIDBuilder == null)
	    return;

	XMLArrayDataFactory factory =
	    new XMLArrayDataFactory(globalContext, null);

	String urls[] = url.split("\\|");

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

	vf = new ViewFrame(globalContext,
			   "Point View",
			   panelProfiles,
			   null, null,
			   null,
			   null,
			   new LinkedList(),
			   Config.defaultDim, null);

	View view = vf.getView();

	LinkedList graphElements = VAMPApplib.makeGraphElements
	    (globalContext, view, view.getPanel(0), type, urls, 0);

	view.getPanel(0).setGraphElements(graphElements);
	
	/*
	vf.setVisible(true);
	view.syncGraphElements();
	*/
    }

    public void start() {
	setVisible(true);

	if (ldf != null && mainView != null)
	    ldf.postAction(mainView);

	(new ToFronter()).start();
    }

    public void stop() {
    }

    public void destroy() {
	VAMPUtils.destroy();
    }

    public void paint(Graphics g) {
	Dimension dim = Utils.drawImage(g, getToolkit(), appletHomeImg,
					0, 0, -1, false, false);
    }

    private void checkMemory(GlobalContext globalContext,
			     SystemConfig sysCfg) {
	long min_memory = Integer.parseInt(sysCfg.getParameter("min:memory"));
	long max_memory = Runtime.getRuntime().maxMemory()/(1024*1024);
	if (max_memory < min_memory) {
	    InfoDialog.pop(globalContext,
			   "Warning: the maximum Java memory size is " +
			   max_memory + " MB.\n" +
			   "The minimum recommended value is " + min_memory +
			   " MB");
	}
    }

    private void loadPerform(GlobalContext globalContext) {
	String s = getParameter("view_count");
	//int viewCount = Utils.parseInt(s);
	ldf = new XMLLoadDataFactory(globalContext, false, false);
	for (int n = 0; ; n++) {
	    try {
		String file = getParameter("view_" + Utils.toString(n));
		if (file == null)
		    break;
		InputStream is = Utils.openStream(file);
		ViewFrame vf = ldf.makeViewFrame(globalContext, is, file);
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }
	}
    }

    private SystemConfig getSystemConfig(GlobalContext globalContext) {
	String sys_cfg = getParameter("sys_cfg");
	SystemConfig sysCfg = SystemConfig.build(globalContext, sys_cfg);
	if (sysCfg == null)
	    InfoDialog.pop(globalContext,
			   "System configuration file error");

	return sysCfg;
    }

    class ToFronter extends Thread {
	public void run() {
	    try {
		Thread.sleep(1000);
	    }
	    catch(java.lang.InterruptedException e) {
	    }

	    if (vf != null) {
		vf.setVisible(true);
		vf.getView().syncGraphElements();
		vf.toFront();
	    }
	}
    }
}
