
/*
 *
 * DifferentialAnalysisRedisplayOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;


class GTCorrelationAnalysisRedisplayOP extends GraphElementListOperation {

    static final String RESULT_PARAM = "Result";
    static final int CorrelCoef = 0x3;
    static final int Pvalue = 0x5;
    static final int FwerWg = 0x6;
    static final int FwerBc = 0x7;
    static final int FdrWg = 0x8;
    static final int FdrBc = 0x9;
    static final String NAME = "GT Correlation Analysis Redisplay";

    public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.GTCA_TYPE, VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE};
    }

    public String getReturnedType() {
	return null;
    }

    GTCorrelationAnalysisRedisplayOP() {
	super(NAME, SHOW_MENU);
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

        // int result_mask = ((Integer)params.get(RESULT_PARAM)).intValue();
	// 	if (result_mask == 0)
	// 		return null;
	//         String result = "";
	// 	if (result_mask == CorrelCoef)
	// 	     result += GTCorrelationAnalysisOP.CORREL;
	// 	else if  (result_mask == Pvalue)
	// 	     result += GTCorrelationAnalysisOP.PVALUE;
	// 	else if (result_mask == FwerWg)
	// 	     result += GTCorrelationAnalysisOP.FWERWG;
	// 	else if (result_mask == FwerBc)
	// 	     result += GTCorrelationAnalysisOP.FWERBC;
	// 	else if (result_mask == FdrWg)
	// 	     result += GTCorrelationAnalysisOP.FDRWG;
	// 	else if (result_mask == FdrBc)
	// 	     result += GTCorrelationAnalysisOP.FDRBC;

	int size = graphElements.size();

	System.out.println("GTCA APPLY: " + size);
	Vector rGraphElements = new Vector();
	for (int n = 0; n < size; n++) {
 	    DataSet dataSet = (DataSet)graphElements.get(n);

	    //  GTCorrelationAnalysisOP.rebuild(dataSet, (String)params.get(GTCorrelationAnalysisOP.RESULT_PARAM));
	    // 	    GTCorrelationAnalysisOP.rebuild(dataSet, result);

	    GTCorrelationAnalysisOP.rebuild(dataSet, (String)params.get(RESULT_PARAM));
             
	    rGraphElements.add(dataSet.clone());
	}

	return rGraphElements;
    }

    static final HashMap view_map = new HashMap();



    public TreeMap getDefaultParams(View view, Vector graphElements) {
	TreeMap params = (TreeMap)view_map.get(view);
	if (params != null)
	    return params;

	params = new TreeMap();
	// 	params.put(GTCorrelationAnalysisOP.RESULT_PARAM, new Integer(CorrelCoef));

	params.put(GTCorrelationAnalysisOP.RESULT_PARAM, GTCorrelationAnalysisOP.CORREL);
	view_map.put(view, params);
	return params;
    }

    public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = GTCorrelationAnalysisRedisplayDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	if (params != null)
	    view_map.put(view, params);
	return params;
    }

    public boolean useThread() {
	return true;
    }
}
