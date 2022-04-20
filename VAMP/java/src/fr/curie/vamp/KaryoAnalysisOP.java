
/*
 *
 * KaryoAnalysisOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;
import java.awt.*;

class KaryoAnalysisOP extends GraphElementListOperation {
   
    // for debug
    static final boolean TRACE = false;

    static final String NAME = "Karyotype Analysis";

    static final String ALT_MASK_PARAM = "AltMask";
    static final String SKIP_OUTLIERS_PARAM = "SkipOutliers";
    static final String SKIP_EMPTY_PROFILES_PARAM = "SkipEmptyProfiles";
    static final String SORT_ALGO_PARAM = "Sort";
    static final String NO_SORT_ALGO = "No Sort";
    static final String STD_SORT_ALGO = "Standard Sort";

    static final String SAME_VIEW_PARAM = "SameView";
    static final String NEW_VIEW_PARAM = "NewView";

    static final int GAIN_AMPLICON_MASK = 0x1;
    static final int GAIN_MASK = 0x2;
    static final int AMPLICON_MASK = 0x4;
    static final int MERGE_GAIN_AMPLICON_MASK = 0x8;
    static final int LOSS_MASK = 0x10;

    KaryoAnalysisOP() {
	super(NAME, SHOW_MENU | ON_ALL_AUTO);
    }

    public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE, VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE, VAMPConstants.SNP_TYPE, VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE, VAMPConstants.CGH_ARRAY_MERGE_TYPE, VAMPConstants.FRAGL_ARRAY_MERGE_TYPE};
    }

    public String getReturnedType() {
	return null;
    }

    public boolean mayApplyP(View view,  GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	return true;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	graphElements = getGraphElements(panel, graphElements, autoApply);

	if (graphElements.size() == 0)
	    return null;

	/*
	System.out.println("SKIP_OUTLIERS_PARAM: " + params.get(SKIP_OUTLIERS_PARAM));
	System.out.println("SKIP_EMPTY_PROFILES_PARAM: " + params.get(SKIP_EMPTY_PROFILES_PARAM));
	System.out.println("SORT_ALGO_PARAM: " + params.get(SORT_ALGO_PARAM));

	System.out.println("SAME_VIEW_PARAM: " + params.get(SAME_VIEW_PARAM));
	System.out.println("NEW_VIEW_PARAM: " + params.get(NEW_VIEW_PARAM));
	*/
	buildKaryoView(view.getGlobalContext(), panel, graphElements, params);
	return graphElements;
    }

    static final HashMap view_map = new HashMap();

    public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = KaryoAnalysisDialog.getParams
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
	params.put(SKIP_OUTLIERS_PARAM, new Boolean(true));
	params.put(SKIP_EMPTY_PROFILES_PARAM, new Boolean(true));
	params.put(SORT_ALGO_PARAM, STD_SORT_ALGO);

	params.put(ALT_MASK_PARAM, new Integer(GAIN_AMPLICON_MASK | GAIN_MASK | LOSS_MASK));

	params.put(SAME_VIEW_PARAM, new Boolean(false));
	params.put(NEW_VIEW_PARAM, new Boolean(true));

	view_map.put(view, params);
	return params;
    }

    void buildKaryoView(GlobalContext globalContext, GraphPanel panel,
			Vector rGraphElements, TreeMap params) {
	PanelProfile panelProfile = new PanelProfile
		("Karyotype View",
		 Config.karyoAxisSizes,
		 PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
		 new KaryoDataSetDisplayer(KaryoDataSetDisplayer.PROFILE_TYPE,
					   KaryoDataSetDisplayer.POINT_TYPE),
		 Config.defaultGenomicPositionAxisDisplayer,
		 //null,
		 null,
		 false,
		 new MergeArrayOP(MergeOP.CGH_TYPE, params),
		 //GraphElementListOperation.get(MergeArrayOP.CGH_NAME),
		 Config.karyoZoomTemplate,
		 new Scale(Utils.pow(0.30), Utils.pow(1.80)),
		 //null,
		 0,
		 true,
		 Config.defaultMargins,
		 null);
	    
	PanelProfile profiles[] = new PanelProfile[]{panelProfile};

	Dimension dim = new Dimension(1000, 700);

	View rView = buildView(globalContext, profiles, dim,
			       panel, rGraphElements);

	view_map.put(rView, params);
    }

    private View buildView(GlobalContext globalContext,
			   PanelProfile panelProfiles[], Dimension dim,
			   GraphPanel panel,
			   Vector rGraphElements) {
	ViewFrame vf = new ViewFrame(globalContext,
				     "Karyotype",
				     panelProfiles,
				     null,
				     null,
				     null, null,
				     new LinkedList(),
				     dim,
				     null);

	LinkedList list = Utils.vectorToList(rGraphElements);
	vf.getView().getGraphPanelSet().getPanel(panel != null ? panel.getWhich() : 0).setGraphElements(list);
	if (task != null)
	    task.performBeforeOPFrameVisible();
	vf.setVisible(true);

	vf.getView().syncGraphElements();

	return vf.getView();
    }

    public boolean supportProfiles() {
	return true;
    }

    public boolean useThread() {
	return true;
    }
}
