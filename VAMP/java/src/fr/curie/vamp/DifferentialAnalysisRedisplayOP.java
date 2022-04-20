
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

class DifferentialAnalysisRedisplayOP extends GraphElementListOperation {

    static final String NAME = "Differential Analysis Redisplay";

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.DIFFANA_TYPE, VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE};
    }

public String getReturnedType() {
	return null;
    }

    DifferentialAnalysisRedisplayOP() {
	super(NAME, SHOW_MENU);
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {

	int size = graphElements.size();
	Vector rGraphElements = new Vector();
	for (int n = 0; n < size; n++) {
	    DataSet dataSet = (DataSet)graphElements.get(n);

	    int colorbar_mask = ((Integer)params.get(DifferentialAnalysisOP.COLORBARS_PARAM)).intValue();
	    boolean useConfidence = (colorbar_mask & DifferentialAnalysisOP.USE_CONFIDENCE_MASK) != 0;
	    
	    DifferentialAnalysisOP.rebuild(dataSet, useConfidence,
					   (String)params.get(DifferentialAnalysisOP.PVALUE_PARAM));
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

	int size = graphElements.size();
	if (size == 1) {
	    DataSet dataSet = (DataSet)graphElements.get(0);
	    params.put(DifferentialAnalysisOP.PVALUE_PARAM,
		       dataSet.getPropertyValue(DifferentialAnalysisOP.pvalueProp));
	    boolean use_confidence = dataSet.getPropertyValue(DifferentialAnalysisOP.useProp).equals("Confidence");

	    params.put(DifferentialAnalysisOP.COLORBARS_PARAM,
		       new Integer(use_confidence ?
				   DifferentialAnalysisOP.USE_CONFIDENCE_MASK :
				   DifferentialAnalysisOP.USE_SIGN_MASK));
	}
	else {
	    params.put(DifferentialAnalysisOP.PVALUE_PARAM, "1.0");
	    params.put(DifferentialAnalysisOP.COLORBARS_PARAM, new Integer(DifferentialAnalysisOP.USE_SIGN_MASK));
	}

	view_map.put(view, params);
	return params;
    }

public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = DifferentialAnalysisRedisplayDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	if (params != null)
	    view_map.put(view, params);
	return params;
     }
}
