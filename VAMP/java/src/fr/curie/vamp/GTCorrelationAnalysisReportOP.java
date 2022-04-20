
/*
 *
 * GTCorrelationAnalysisOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;
import java.awt.*;

class GTCorrelationAnalysisReportOP extends GraphElementListOperation {
   
    // for debug
    static final boolean TRACE = false;

    static final String SCOPE_PARAM = "Scope";
    static final String CRITERIA_PARAM = "Criteria";
    static final String CORRELATION_PARAM = "Correlation";
    static final String THRESHOLD_PARAM = "Threshold";
    static final String PVALUE_PARAM = "PValue";
    static final String RESULT_PARAM = "Result";

    static final String ALL = "All";
    static final String REGIONS = "Regions";

    // static final String GNL_VALUE = "GNL";
    static final String RATIO_VALUE = "Ratio";
    static final String SMOOTHING_VALUE = "Smoothing";

    static final String PEARSON = "Pearson";
    static final String SPEARMAN = "Spearman";

   //  static final int GNL = 1;

    static final int RATIO = 1;
    static final int SMOOTHING = 2;

    static final int HTML_REPORT = 0x1;
    static final int HTML_FULL_REPORT = 0x2;
    static final int CSV_REPORT = 0x4;
    static final int PROFILE_DISPLAY = 0x8;
    static final int PRECISION = 3;

    static final String NAME = "Genome / Transcriptome Correlation Analysis";

    static File tempfile;

    private String curChr = null;

public String[] getSupportedInputTypes() {
	return null;
    }

public String getReturnedType() {
	return null;
    }

    GTCorrelationAnalysisReportOP() {
	super(NAME, SHOW_MENU | ON_ALL_AUTO);
    }

    private class Pair {
	DataSet cgh;
	DataSet trs;

	Pair(DataSet cgh, DataSet trs) {
	    this.cgh = cgh;
	    this.trs = trs;
	}
    }

    static boolean contains(Vector cgh_v, DataSet ref) {
	int cgh_size = cgh_v.size();
	for (int n = 0; n < cgh_size; n++) {
	    if (((DataSet)cgh_v.get(n)).getID().equals(ref.getID()))
		return true;
	}

	return false;
    }

    
public boolean mayApplyP(View view,  GraphPanel panel,
		      Vector graphElements, boolean autoApply) {

	return false;
    }

    

// public Vector apply(View view, GraphPanel panel,
// 		 Vector graphElements, TreeMap params,
// 		 boolean autoApply) {

// 	int report_mask = ((Integer)params.get(RESULT_PARAM)).intValue();
// 	if (report_mask == 0)
// 	    return graphElements;
// 	try {
// 	    buildReport((DataSet)graphElements.get(0), (report_mask & HTML_REPORT) != 0);
// 	}
// 	catch(Exception e) {
// 	    e.printStackTrace();
// 	    InfoDialog.pop(view.getGlobalContext(), e.getMessage());
// 	    return null;
// 	}

// 	return graphElements;
//     }


public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {


	    int result_mask = ((Integer)params.get(RESULT_PARAM)).intValue();
	    if (result_mask == 0)
		return null;

	    int type;

	    
	  

	    // DataSet refDataSet = (DataSet)graphElements.get(0);
	    boolean isHTML = ((result_mask & HTML_REPORT) != 0);
	    boolean isFullHTML = ((result_mask & HTML_FULL_REPORT) != 0);
	    String sthr = (String)params.get(THRESHOLD_PARAM);
	    double threshold = (sthr != null && sthr.trim().length() > 0 ?
				Math.abs(Double.parseDouble(sthr.trim())) : 0.);
	    String spvalue = (String)params.get(PVALUE_PARAM);
	    double pvalue_max = (spvalue != null &&
				 spvalue.trim().length() > 0 ?
				 Math.abs(Double.parseDouble(spvalue.trim())) : 1.);


       return graphElements;
    }

    Vector buildReport(View view, GraphPanel panel, int result_mask,
		       Vector graphElements, Vector pair_v, int type,
		       double threshold, TreeMap pset_map) throws Exception {

	File file = DialogUtils.openFileChooser(new Frame(), "Save", 0, true);
	if (file == null)
	    return graphElements;


	return graphElements;
    }

    

    

    int find(long x1, long x2, DataElement data[], DataSet cgh, int k) {
	DataElement d = data[k];

	long dx1 = (long)d.getPosX(cgh);
	long dx2 = (long)(d.getPosX(cgh) + d.getPosSize(cgh));
	if ((x1 >= dx1 && x1 <= dx2) || (x2 >= dx1 && x2 <= dx2))
	    return 0;
	
	if (x1 > dx2)
	    return -1;

	return 1;
    }

   
    static String str(int tab[]) {
	String s = "[";
	for (int n = 0; n < tab.length; n++)
	    s += (n > 0 ? ":" : "") + tab[n];
	return s + "]";
    }

    static boolean equals(int tab1[], int tab2[]) {
	if (tab1.length != tab2.length)
	    return false;
	for (int n = 0; n < tab1.length; n++)
	    if (tab1[n] != tab2[n])
		return false;
	return true;
    }

    static final HashMap view_map = new HashMap();

public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = GTCorrelationAnalysisDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	if (params != null)
	    view_map.put(view, params);
	return params;
    }

public TreeMap getDefaultParams(View view, Vector graphElements) {
	TreeMap params = (TreeMap)view_map.get(view);
	if (params != null)
	    return params;

	params = new TreeMap();
	params.put(SCOPE_PARAM, ALL);
	params.put(CRITERIA_PARAM, SMOOTHING_VALUE);
	params.put(CORRELATION_PARAM, PEARSON);
	params.put(THRESHOLD_PARAM, "0.5");
	params.put(PVALUE_PARAM, "0.05");
	params.put(RESULT_PARAM, new Integer(PROFILE_DISPLAY));

	view_map.put(view, params);
	return params;
    }

public TreeMap makeParams(HashMap map) {
	TreeMap params = new TreeMap();
	params.put(SCOPE_PARAM, map.get(SCOPE_PARAM));
	params.put(CRITERIA_PARAM, map.get(CRITERIA_PARAM));
	params.put(CORRELATION_PARAM, map.get(CORRELATION_PARAM));
	params.put(THRESHOLD_PARAM, map.get(THRESHOLD_PARAM));
	params.put(PVALUE_PARAM, map.get(PVALUE_PARAM));
	params.put(RESULT_PARAM, Utils.makeInteger(map.get(RESULT_PARAM)));
	return params;
    }

    private boolean isEligible(GraphPanel panel, DataElement data,
			       GraphElement graphElement) {
	int size = panel.getRegions().size();
	for (int n = 0; n < size; n++) {
	    Region region = (Region)panel.getRegions().get(n);
	    if (data.crossRegion(region, graphElement))
		return true;
	}
	    
	return false;
    }

public boolean useThread() {
	return true;
    }

    public String getMessage() {
	return "Computing GTCA...";
    }
}
