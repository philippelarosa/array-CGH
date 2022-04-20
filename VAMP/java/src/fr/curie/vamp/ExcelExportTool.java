
/*
 *
 * ExcelExportTool.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.applet.*;
import java.util.*;
import java.net.*;

class ExcelExportTool extends ExportTool {

    private static final String NAME_EXCEL_STD = "to eXcel";
    private static final String NAME_EXCEL_AVG = "to eXcel (Chr average)";
    private static final String NAME_CSV_STD = "to CSV";
    private static final String NAME_CSV_AVG = "to CSV (Chr average)";

    private int flags;
    private String cgi_name = null;
    private String method = null;

    static final int EXCEL = 0x1;
    static final int CSV = 0x2;
    static final int STD = 0x4;
    static final int AVG = 0x8;

    static String getName(int flags) {
	if (flags == (EXCEL|STD))
	    return NAME_EXCEL_STD;
	if (flags == (EXCEL|AVG))
	    return NAME_EXCEL_AVG;
	if (flags == (CSV|STD))
	    return NAME_CSV_STD;
	if (flags == (CSV|AVG))
	    return NAME_CSV_AVG;
	    
	return "<UNKNWON EXPORT TOOL>";
    }

    ExcelExportTool(int flags) {
	super(getName(flags), 0);
	this.flags = flags;
    }

    void perform(View view) {
	Vector graphElements = view.getSelectedGraphElements(View.ALL);
	int size = graphElements.size();
	if (size == 0)
	    return;

	String method = getMethod(view);

	boolean use_post;
	if (method != null && method.equalsIgnoreCase("post"))
	    use_post = true;
	else
	    use_post = false;

	String surl = getCGIName(view);
	String data = "name=" + makeURLName(graphElements);

	GraphElement ds0 = (GraphElement)graphElements.get(0);
	//data += "&team=" + ds0.getPropertyValue(VAMPProperties.TeamProp);
	StandardColorCodes cc = (StandardColorCodes)
	    VAMPUtils.getColorCodes(ds0);
	data += "&normal_max=" + cc.getNormalMax();
	data += "&normal_min=" + cc.getNormalMin();

	data += "&min_color=" + ColorResourceBuilder.RGBString(cc.getMinRGB());
	data += "&max_color=" + ColorResourceBuilder.RGBString(cc.getMaxRGB());
	data += "&normal_color=" + ColorResourceBuilder.RGBString(cc.getNormalRGB());

	if (!use_post) {
	    surl += "&" + data;
	    data = null;
	}

	System.out.println("eXcel URL: " + surl + "::" + data);
	GlobalContext globalContext = view.getGlobalContext();

	AppletContext appletContext = globalContext.getAppletContext();
	if (appletContext != null) {
	    try {
		Utils.showDocument(globalContext, appletContext, surl, data, "_blank");
		//URL url = new URL(surl);
		//appletContext.showDocument(url, "_blank");
	    }
	    catch(Exception exc) {
		InfoDialog.pop(globalContext, exc.getMessage());
	    }
	}
    }

    private static String makeURLName(Vector graphElements) {
	Vector set = new Vector();
	makeURLSet(graphElements, set);
	String s = "";
	Object s_arr[] = set.toArray();
	for (int n = 0; n < s_arr.length; n++) {
	    s += (n > 0 ? "|" : "") + s_arr[n];
	}
	    
	return s;
    }

    private static void makeURLSet(Vector graphElements, Vector set) {
	int size = graphElements.size();

	for (int n = 0; n < size; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);

	    if (VAMPUtils.getType(graphElement).equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE)) {
		makeURLSet((Vector)graphElement.getPropertyValue(VAMPProperties.VectorArrayProp),
			   set);
	    }
	    else {
		String s = graphElement.getID() + "+" +
		    (VAMPUtils.isMergeChr(graphElement) ? "all" :
		     VAMPUtils.getChr(graphElement));
		String rs = (String)graphElement.getPropertyValue(VAMPProperties.RatioScaleProp);
		s += "+" + (rs == null ? "null" : rs);
		s += "+" + graphElement.getPropertyValue(VAMPProperties.TeamProp);
		if (!set.contains(s))
		    set.add(s);
	    }
	}

    }

    boolean isEnabled(View view) {
	return view.getSelectedGraphElements(View.ALL).size() > 0 &&
	    getCGIName(view) != null;
    }

    private String getCGIName(View view) {
	if (cgi_name == null) {
	    GlobalContext globalContext = view.getGlobalContext();
	    SystemConfig sysCfg = (SystemConfig)globalContext.get(SystemConfig.SYSTEM_CONFIG);

	    if (flags == (EXCEL|STD))
		cgi_name = sysCfg.getParameter("eXcel:URL");
	    else if (flags == (EXCEL|AVG))
		cgi_name = sysCfg.getParameter("eXcelChrAvg:URL");
	    else if (flags == (CSV|STD))
		cgi_name = sysCfg.getParameter("CSV:URL");
	    else if (flags == (CSV|AVG))
		cgi_name = sysCfg.getParameter("CSVChrAvg:URL");
	}

	return cgi_name;
    }

    private String getMethod(View view) {
	if (method == null) {
	    GlobalContext globalContext = view.getGlobalContext();
	    SystemConfig sysCfg = (SystemConfig)globalContext.get(SystemConfig.SYSTEM_CONFIG);
	    if (flags == (EXCEL|STD))
		method = sysCfg.getParameter("eXcel:method");
	    else if (flags == (EXCEL|AVG))
		method = sysCfg.getParameter("eXcelChrAvg:method");
	    else if (flags == (CSV|STD))
		method = sysCfg.getParameter("CSV:method");
	    else if (flags == (CSV|AVG))
		method = sysCfg.getParameter("CSVChrAvg:method");
	}

	return method;
    }
}
