/*
 *
 * ClusterOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;

class ClusterOP extends GraphElementListOperation {

    static final String NAME = "CGH Cluster";

    static final String TRUE = "true";
    static final String FALSE = "false";

    static final String ALL_CLONES_PARAM = "AllClones";

    static final String RATIO_PARAM = "Ratio";
    static final String GNL_PARAM = "Status (Gained/Lost Color Code)";
    static final String SMOOTHING_PARAM = "Smoothing";
    static final String PROBE_VALUE_PARAM = "Probe Value";

    static final String REGIONS_PARAM = "Regions";
    static final String SIZE_PARAM = "SizeParam";
    static final String PERCENT_PARAM = "PercentParam";
    static final String PERCENT = "Percentage";

    static final String DIST_PARAM = "Distance";
    static final String EUCLIDIAN = "Euclidian";
    static final String PEARSON = "Pearson";
    static final String MANHATTAN = "Manhattan";

    static final String ALGO_PARAM = "Algorithm";
    static final String SLINK = "SingleLinkage";
    static final String CLINK = "CompleteLinkage";
    static final String GAVG = "GroupAverage";
    static final String WARD = "Ward";

    static final String SEX_CHR_PARAM = "SexChromosomes";

    static final String DISPLAY_PARAM = "Dendro";
    static final String HIERARCHICAL_CLUSTERING = "Hierarchical Clustering";
    static final String CLUSTERING_ONLY = "Profiles Only";
    static final String DENDROGRAM_ONLY = "Dendrogram Only";
    static final String CLUSTERING_DENDROGRAM = "Profiles and Dendrogram";

    static final String MESSAGE = "Computing clustering...";
    //"Waiting for clustering results...",

    static final boolean USE_POST = false;

    public String[] getSupportedInputTypes() {
	//	return null;
	// warning: modified 25/05/05
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE, VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.SNP_TYPE, VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE};
    }

    public String getReturnedType() {
	return VAMPConstants.CGH_AVERAGE_TYPE;
    }

    ClusterOP() {
	super(NAME, SHOW_MENU | ON_ALL_AUTO);
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	graphElements = getGraphElements(panel, graphElements, autoApply);
	SystemConfig sysCfg = (SystemConfig)view.getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);
	return sysCfg.getParameter("cluster:URL") != null &&
	    getChr(graphElements) != null;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	graphElements = getGraphElements(panel, graphElements, autoApply);

	if (graphElements.size() == 0)
	    return null;

	String chr_s = getChr(graphElements);
	if (chr_s == null) {
	    InfoDialog.pop(view.getGlobalContext(), "cannot apply " +
			   "clustering on different chromosomes");
	    return null;
	}

	SystemConfig sysCfg = (SystemConfig)view.getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);
	String cgi_name = sysCfg.getParameter("cluster:URL");
	String surl = cgi_name;

	String data = "action=clust&dendro=" + getDendrogramOption(params);

	data += "&url=" + makeURLName(graphElements);

	Object v = params.get(ALL_CLONES_PARAM);
	boolean isAllClones = (v != null && v.equals(new Boolean(true)));
	boolean gnl;
	boolean size;
	String percent;

	if (isAllClones) {
	    gnl = params.get(RATIO_PARAM).equals(GNL_PARAM);
	    size = false;
	}
	else {
	    gnl = false;
	    isAllClones = false;
	    v = params.get(SIZE_PARAM);
	    size = (v != null && v.equals(new Boolean(true)));
	}

	String ratio = "";

	if (params.get(RATIO_PARAM).equals(GNL_PARAM))
	    ratio = "GNL";
	else if (params.get(RATIO_PARAM).equals(SMOOTHING_PARAM))
	    ratio = "smoothing";
	else if (params.get(RATIO_PARAM).equals(PROBE_VALUE_PARAM))
	    ratio = "probevalue";

	//percent = (String)params.get(PERCENT);
	percent = "";

	String dist = (String)params.get(DIST_PARAM);
	String algo = (String)params.get(ALGO_PARAM);
	v = params.get(SEX_CHR_PARAM);
	boolean sexChr = (v != null && v.equals(new Boolean(true)));

	/*
	  System.out.println("isAllClones: " + isAllClones);
	  System.out.println("gnl: " + gnl);
	  System.out.println("ratio: " + ratio);
	  System.out.println("size: " + size);
	  System.out.println("percent: " + percent);
	  System.out.println("dist: " + dist);
	  System.out.println("algo: " + algo);
	  System.out.println("sexChr: " + sexChr);
	*/

	data += "&chr=" + chr_s;
	data += "&isAllClones=" + (isAllClones ? "true" : "false");
	data += "&gnl=" + (gnl ? "true" : "false");
	data += "&ratio=" + ratio;
	data += "&size=" + (size ? "true" : "false");
	data += "&percent=" + (percent.trim().length() > 0 ? percent : "null");
	data += "&dist=" + dist;
	data += "&algo=" + algo;
	data += "&sexChr=" + (sexChr ? "true" : "false");

	long offset = 0;
	if (!isAllClones) {
	    Cytoband cytoband = MiniMapDataFactory.getCytoband
		(view.getGlobalContext(),
		 VAMPUtils.getOS((GraphElement)graphElements.get(0)));

	    LinkedList regions = view.getRegions();
	    data += "&regions=";
	    int sz = regions.size();
	    for (int n = 0; n < sz; n++) {
		Region region = (Region)regions.get(n);
		String support = 
		    (String)region.getPropertyValue(GenomeAlterationOP.SupportProp);
		if (n != 0)
		    data += "|";
		long begin = (long)region.getBegin().getPosX();
		long end = (long)region.getEnd().getPosX();

		Object g = region.getPropertyValue(VAMPProperties.GNLProp);
		String state;
		if (g != null)
		    state = (String)g;
		else
		    state = "m";

		if (VAMPUtils.isMergeChr((GraphElement)graphElements.get(0))) {
		    Chromosome chr_b = cytoband.getChromosome(begin);
		    Chromosome chr_e = cytoband.getChromosome(end-1);

		    int s = -1, e = -1;
		    Vector chrv = cytoband.getChrV();
		    int chrv_size = chrv.size();
		    for (int j = 0; j < chrv_size; j++) {
			Chromosome chr = (Chromosome)chrv.get(j);
			if (chr == chr_b)
			    s = j;

			if (chr == chr_e) {
			    e = j;
			    break;
			}
		    }

		    if (s < 0 || e < 0) {
			InfoDialog.pop(view.getGlobalContext(),
				       "invalid chromosomes " + chr_b.getName() +
				       " " + chr_e.getName());
			return null;
		    }

		    begin -= chr_b.getBegin_o();
		    end -= chr_e.getBegin_o();

		    for (int j = s; j <= e; j++) {
			Chromosome chr = (Chromosome)chrv.get(j);
			if (j != s)
			    data += "|";

			data += chr.getName() + ":";
			if (j == s)
			    data += begin;
			else
			    data += "0";

			data += ":";
		    
			if (j == e)
			    data += end;
			else
			    data += chr.getEnd();
			data += ":" + state;
			data += getSupport(support);
		    }

		}
		else {
		    data += chr_s + ":" + begin + ":" + end + ":" + state;
		    data += getSupport(support);
		    if (offset == 0)
			offset = cytoband.getChromosome(chr_s).getOffsetPos();
		}
	    }
	}

	String method = sysCfg.getParameter("cluster:method");

	if (method == null || !method.equalsIgnoreCase("post")) {
	    surl += "?" + data;
	    data = null;
	}

	System.out.println("Cluster URL: " + surl + " :: " + data);

	try {
	    InputStream is = Utils.openStream(surl, data);
	    ViewFrame vf = makeDendrogramView(view, params);

	    //is = Utils.tee(is, "/tmp/VAMP-CLUSTER");

	    RemoteOP op;
	    boolean isRegions = !isAllClones;

	    if (vf == null)
		op = new RemoteOP(null, view, panel, false,
				  isRegions, offset, true, is,
				  MESSAGE,
				  "Clustering done !",
				  RemoteOP.LOAD_FACTORY);
	    else {
		vf.getView().setInternalReadOnly(true);
		op = new RemoteOP(vf, view, null, true,
				  isRegions, offset, true, is,
				  MESSAGE,
				  "Clustering done !",
				  RemoteOP.LOAD_FACTORY);
	    }
	    op.start();

        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(view.getGlobalContext(), e.getMessage());
	    return null;
        }
	return null;
    }

    private String getSupport(String support) {
	return ":" + (support != null ? support.replaceAll("\n", ",") : "null");
    }

    private static String makeURLName(Vector graphElements) {
	String s = "";
	Object s_arr[] = graphElements.toArray();
	for (int n = 0; n < s_arr.length; n++) {
	    // EV 10/12/04
	    GraphElement graphElem = (GraphElement)s_arr[n];
	    String url = graphElem.getURL();
	    if (url == null)
		url = graphElem.getSourceURL();
	    s += (n > 0 ? "|" : "") + url;
	    /*
	      s += (n > 0 ? "|" : "") +
	      graphElem.getPropertyValue(VAMPConstants.NameProp);
	    */
	}
	    
	return s;
    }

    TreeMap defaultParams;

    public TreeMap getDefaultParams(View view, Vector graphElements) {
	if (defaultParams == null) {
	    defaultParams = new TreeMap();
	    defaultParams.put(ALL_CLONES_PARAM, new Boolean(true));
	}
	return defaultParams; // could depends on view and graphElements
	// (c.f. MinimalRegionOP)
    }

    public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = ClusterDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	defaultParams = params;
	return params;
    }

    /*
      boolean isDendrogram(TreeMap params) {
      return !params.get(DISPLAY_PARAM).equals(CLUSTERING_ONLY);
      }
    */

    String getDendrogramOption(TreeMap params) {
	if (params == null)
	    return "";

	if (params.get(DISPLAY_PARAM).equals(CLUSTERING_ONLY))
	    return "0";
	if (params.get(DISPLAY_PARAM).equals(DENDROGRAM_ONLY))
	    return "2";
	return "1";
    }

    ViewFrame makeDendrogramView(View view, TreeMap params) {
	
	if (params.get(DISPLAY_PARAM).equals(CLUSTERING_ONLY))
	    return null;

	GlobalContext globalContext = view.getGlobalContext();
	PanelProfile panelProfiles[];
	if (params.get(DISPLAY_PARAM).equals(DENDROGRAM_ONLY)) {
	    panelProfiles = new PanelProfile[1];

	    panelProfiles[0] = new PanelProfile
		("Dendrogram",
		 Config.defaultAxisSizes,
		 PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
		 new YDendrogramGraphElementDisplayer(),
		 new YDendrogramAxisDisplayer(),
		 null,
		 false,
		 //GraphElementListOperation.get(ChrAxisOP.NAME),
		 null,
		 Config.defaultZoomTemplate,
		 null,
		 0,
		 true,
		 new Margins(30, 0, 30, 30),
		 null);

	    return new ViewFrame
		(globalContext, "Dendrogram View",
		 panelProfiles,
		 null,
		 null,
		 null, null, // menu
		 new LinkedList(),
		 Config.defaultDim,
		 null);
	}

	panelProfiles = new PanelProfile[2];

	panelProfiles[0] = new PanelProfile
	    ("Left",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new PointDataSetDisplayer(false),
	     //Config.defaultGenomicPositionAxisDisplayer,
	     Config.defaultChromosomeNameAxisDisplayer,
	     null,
	     false,
	     //GraphElementListOperation.get(ChrAxisOP.NAME),
	     null,
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     new Margins(30, 20, 30, 20),
	     null);
						     
	panelProfiles[1] = new PanelProfile
	    ("Dendrogram",
	     new int[]{VAMPResources.getInt(VAMPResources.AXIS_NORTH_SIZE),
		       VAMPResources.getInt(VAMPResources.AXIS_SOUTH_SIZE),
		       0, 0},
	     PanelProfile.SCROLL_EAST|PanelProfile.SCROLL_SOUTH,
	     new YDendrogramGraphElementDisplayer(),
	     new YDendrogramAxisDisplayer(),
	     null,
	     false,
	     //GraphElementListOperation.get(ChrAxisOP.NAME),
	     null,
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     new Margins(30, 0, 30, 30),
	     null);

	return new ViewFrame
	    (globalContext, "Dendrogram View",
	     panelProfiles,
	     new PanelSplitLayout
	     (PanelSplitLayout.VERTICAL,
	      new PanelFinalLayout(0),
	      new PanelFinalLayout(1)),
	     new PanelLinks[]{
		 new PanelLinks("Group",
				GraphPanel.SYNCHRO_Y,
				new int[]{0, 1})},
	     null, null,
	     new LinkedList(),
	     new java.awt.Dimension(900, 600),
	     null);
    }

    public boolean mayApplyOnReadOnlyPanel() {
	return true;
    }

    /*
      boolean useThread() {
      return true;
      }
    */

    static String getChr(Vector graphElements) {
	String chr = VAMPUtils.getChr((GraphElement)graphElements.get(0));
	int size = graphElements.size();
	for (int n = 1; n < size; n++)
	    if (!VAMPUtils.getChr((GraphElement)graphElements.get(n)).equals(chr))
		return null;
	if (chr.indexOf(",") >= 0)
	    return "all";
	return chr;
    }

    public String getMessage() {
	return MESSAGE;
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}
}
