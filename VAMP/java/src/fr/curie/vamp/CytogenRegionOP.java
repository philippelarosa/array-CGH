
/*
 *
 * CytogenRegionOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.awt.*;

class CytogenRegionOP extends GraphElementListOperation {
   
    int type;

    static final String NAME = "Cytogenetic Banding";

    static final String OS_PARAM = "Organism";
    static final String BEGIN_PARAM = "Begin";
    static final String END_PARAM = "End";
    static final String COLOR_PARAM = "Color";

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    CytogenRegionOP() {
	super(NAME, SHOW_MENU | ON_ALL_AUTO);
    }

    static TreeMap def_params;

    public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = CytogenRegionDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	def_params = params;
	return params;
    }

    public TreeMap getDefaultParams(View view, Vector graphElements) {
	if (def_params != null)
	    return def_params;

	def_params = new TreeMap();
	return def_params;
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	return graphElements.size() > 0;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	try {
	    String begin = (String)params.get(BEGIN_PARAM);
	    String end = (String)params.get(END_PARAM);
	    boolean isMerge = VAMPUtils.isMergeChr((GraphElement)graphElements.get(0));
	    long begin_p = CytogenRegionDialog.getBegin(begin, isMerge);
	    long end_p = CytogenRegionDialog.getEnd(end, isMerge);
	    GraphPanelSet panel_set = view.getGraphPanelSet();
	    Mark begin_m = panel_set.addMark(panel.getWhich(), begin_p);
	    Mark end_m = panel_set.addMark(panel.getWhich(), end_p);
	    panel_set.addRegion(panel.getWhich(), new Region(begin_m, end_m,
							     (Color)params.get(COLOR_PARAM)));

	    return undoManage(panel, graphElements);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public boolean supportProfiles() {
	return true;
    }
}
