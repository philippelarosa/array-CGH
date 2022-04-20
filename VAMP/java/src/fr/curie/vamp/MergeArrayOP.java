
/*
 *
 * MergeArrayOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

import fr.curie.vamp.data.Probe;

class MergeArrayOP extends MergeOP {
   
    static final int Y_OFFSET = 6;
    static final int Y_OFFSET_FRAGL = 3;
    static final double Y_COEF_FRAGL = 10;
    static final String CGH_NAME = "Merge CGH Arrays";
    static final String CHIP_CHIP_NAME = "Merge ChIp-chip Arrays";
    static final String FRAGL_NAME = "Merge FrAGL Arrays";
    static final int NO_SORT = 0;
    static final int STANDARD_SORT = 1;
    static final Property SkipProbeProp = Property.getHiddenProperty("SkipProbeProp");

    static boolean SKIP_PASS_1 = true;
    static boolean USE_FACTORY = true;
    static final int ARRAY_OFFSET = 1;
    static final int ARRAY_SIZE = 12;
    static final int SKIP_OFFSET = 0;
    static final int SKIP_SIZE = 1;

    static final boolean SKIP_PROBES = true;
    static final boolean SKIP_PROBES2 = true;

    boolean skipOutliers = true;
    boolean skipEmptyProfiles = true;

    boolean keepGainAmplicon = true;
    boolean stdSort = false;

    boolean gainOnly = true;
    boolean ampliconOnly = false;
    boolean gainAndAmplicon = false;

    boolean keepLost = true;

    TreeMap opParams;
    static Property paramsProp = Property.getHiddenProperty("MergeArrayParams");
    public String[] getSupportedInputTypes() {
	if (isChipChip())
	    return new String[]{VAMPConstants.CHIP_CHIP_TYPE,
				VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE,
				VAMPConstants.CHIP_CHIP_AVERAGE_TYPE,
				VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE};
	if (isCGH())
	    return new String[]{VAMPConstants.CGH_ARRAY_TYPE,
				VAMPConstants.CGH_ARRAY_MERGE_TYPE,
				VAMPConstants.CGH_AVERAGE_TYPE,
				VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE};
	if (isFrAGL())
	    return new String[]{VAMPConstants.FRAGL_ARRAY_MERGE_TYPE,
				VAMPConstants.FRAGL_TYPE,
				VAMPConstants.FRAGL_CHROMOSOME_MERGE_TYPE};
	return null;
    }

    public String getReturnedType() {
	if (isChipChip())
	    return VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE;
	if (isCGH())
	    return VAMPConstants.CGH_ARRAY_MERGE_TYPE;
	if (isFrAGL())
	    return VAMPConstants.FRAGL_ARRAY_MERGE_TYPE;
	return null;
    }

    static TreeSet make(Vector graphElements) {
	TreeSet treeSet = new TreeSet();
	int size = graphElements.size();
	for (int n = 0; n < size; n++)
	    treeSet.add(new DataSetArrayComparator((GraphElement)graphElements.get(n)));

	return treeSet;
    }

    static String getName(int type) {
	if (type == CGH_TYPE)
	    return CGH_NAME;
	if (type == CHIP_CHIP_TYPE)
	    return CHIP_CHIP_NAME;
	if (type == FRAGL_TYPE)
	    return FRAGL_NAME;
	return "";
    }

    MergeArrayOP(int type) {
	super(getName(type), type, 0);
    }

    MergeArrayOP(int type, TreeMap params) {
	//	super(getName(type) + "_internal", type, 0);
	super(getName(type) + "_" + params.toString(), type, 0);
	this.opParams = params;
	skipOutliers = ((Boolean)opParams.get(KaryoAnalysisOP.SKIP_OUTLIERS_PARAM)).booleanValue();
	skipEmptyProfiles = ((Boolean)opParams.get(KaryoAnalysisOP.SKIP_EMPTY_PROFILES_PARAM)).booleanValue();

	int alt_mask = ((Integer)opParams.get(KaryoAnalysisOP.ALT_MASK_PARAM)).intValue();
	keepGainAmplicon = (alt_mask & KaryoAnalysisOP.GAIN_AMPLICON_MASK) != 0;
	keepLost = (alt_mask & KaryoAnalysisOP.LOSS_MASK) != 0;

	gainOnly = (alt_mask & KaryoAnalysisOP.GAIN_MASK) != 0;
	ampliconOnly = (alt_mask & KaryoAnalysisOP.AMPLICON_MASK) != 0;
	gainAndAmplicon = (alt_mask & KaryoAnalysisOP.MERGE_GAIN_AMPLICON_MASK) != 0;
	stdSort = opParams.get(KaryoAnalysisOP.SORT_ALGO_PARAM).equals(KaryoAnalysisOP.STD_SORT_ALGO);
    }

    public boolean mayApply(GraphElementListOperation op) {
	if (op == null)
	    return true;

	if (op.equals(this))
	    return false;

	String opname = "";
	if (isChipChip())
	    opname = SplitArrayOP.CHIP_CHIP_NAME;
	else if (isCGH())
	    opname = SplitArrayOP.CGH_NAME;
	else if (isFrAGL())
	    return true; // TBD

	return !op.equals(GraphElementListOperation.get(opname));
    }

    static boolean check(TreeSet treeSet) {
	String lastID = null;
	Object graphElements[] = treeSet.toArray();
	for (int n = 0; n < graphElements.length; n++) {
	    GraphElement graphElement = ((DataSetArrayComparator)graphElements[n]).graphElement;
	    if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		(VAMPConstants.CGH_ARRAY_MERGE_TYPE) ||
		graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		(VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE) ||
		graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		(VAMPConstants.FRAGL_ARRAY_MERGE_TYPE))
		continue;

	    String id = (String)graphElement.getID();
	    if (lastID == null && id.equals(lastID))
		return false;
	    lastID = id;
	}
	return true;
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	if (autoApply)
	    return check(make(graphElements));
	return check(make(graphElements)) && !areAllMerged(graphElements);
    }

    public Vector apply_p(View view, GraphPanel panel,
			  Object graphElements[], TreeMap params,
			  boolean autoApply, boolean reapply) throws Exception {
	long ms0 = System.currentTimeMillis();

	Vector rDataSets = new Vector();
	String lastChr = null;

	boolean ended = false;
	GraphElement rDataSet = null;
	int ds_cnt = 0;
	Vector<GraphElement> curDataSets = null;
	GraphElement oDataSet = null;

	HashMap url_map = null;

	int m1 = 0;
	//System.out.println("pass apply#1 full size: " + graphElements.length);
	HashMap gained_cnt_map = null;
	HashMap lost_cnt_map = null;

	// CC: pass apply#1
	for (int m = 0; m < graphElements.length; m++, m1++) {
	    // chr comparison
	    GraphElement dataSet;
	    if (reapply) {
		dataSet = (GraphElement)graphElements[m];
	    }
	    else {
		dataSet = ((DataSetArrayComparator)graphElements[m]).graphElement;
	    }
	    if (dataSet.asDataSet() != null) {
		//System.out.println("is a dataSet: " + dataSet + " " + dataSet.asDataSet().getData().length + " " + dataSet.getProbeCount());
	    }

	    String chr = VAMPUtils.getChr(dataSet);
	    String id = (String)dataSet.getID();
	    if (!chr.equals(lastChr)) {
		if (lastChr != null) {
		    if (USE_FACTORY) {
			//System.out.println("call to complete " + curDataSets + " " + curDataSets.size());
			rDataSet = complete(view, oDataSet, lastChr, url_map, curDataSets, gained_cnt_map, lost_cnt_map, params, reapply);
		    }
		    rDataSets.add(rDataSet);
		}

		gained_cnt_map = new HashMap();
		lost_cnt_map = new HashMap();

		ds_cnt = 0;
		m1 = 0;
		url_map = new HashMap();
		curDataSets = new Vector();
		lastChr = chr;
		ended = false;
		oDataSet = dataSet;
	    }

	    curDataSets.add(dataSet);
	    url_map.put(chr, dataSet.getURL());
	}

	//complete_2(rDataSet, curDataList, lastChr, url_map, curDataSets, params);
	if (USE_FACTORY) {
	    rDataSet = complete(view, oDataSet, lastChr, url_map, curDataSets, gained_cnt_map, lost_cnt_map, params, reapply);
	}

	rDataSets.add(rDataSet);

	if (panel != null) {
	    panel.setGraphElementIDBuilder(Config.dataSetIDArrayBuilder);
	}

	long ms1 = System.currentTimeMillis();
	System.out.println("Kayro duration: " + ((ms1 - ms0)/1000) + " seconds");

	Probe.VERBOSE = true;
	return undoManage(panel, rDataSets);
    }

    private void setViewType(ToolResultContext toolResultContext) {
	if (isFrAGL()) {
	    toolResultContext.getInfo().grphDispName = "Karyotype FrAGL";
	    toolResultContext.getInfo().viewType = "Karyotype FrAGL";
	}
	else {
	    toolResultContext.getInfo().grphDispName = "Karyotype Classic";
	    toolResultContext.getInfo().viewType = "Karyotype Classic";
	}
    }

    public Vector apply(View view, GraphPanel panel,
			Vector igraphElements, TreeMap params,
			boolean autoApply) {

	params = opParams;

	boolean reapply = false;
	 if (areAllMerged(igraphElements)) {
	     if (isFrAGL()) {
		 return igraphElements;
	     }
	    // the problem is that in this case, the self DnD is very slow
	    // because all is reApplyd !
	     //System.out.println("reapplying direct...\n");
	    reapply = true;
	}

	try {
	    Vector tmpGraphElements = new Vector();
	    for (int m = 0; m < igraphElements.size(); m++) {
		tmpGraphElements.add(((GraphElement)igraphElements.get(m)).dupSerializer());
		//System.out.println("CHR #" + VAMPUtils.getChr((GraphElement)igraphElements.get(m)));
	    }

	    boolean mustPostCompute = (opParams != null && !isFrAGL());
	    /*
	    System.out.println("opParams : " + opParams);
	    System.out.println("isFrAGL : " + isFrAGL());
	    System.out.println("mustPostCompute : " + mustPostCompute);
	    */

	    Vector _graphElements;
	    if (reapply) {
		_graphElements = tmpGraphElements;
	    }
	    else {
		_graphElements = applyPrologue(view, panel, tmpGraphElements, autoApply);
		if (_graphElements == null) {
		    System.out.println("returning original graphelements");
		    return igraphElements;
		}
	    }

	    Object graphElements[];
	    if (reapply) {
		graphElements = _graphElements.toArray();
	    }
	    else {
		TreeSet treeSet = make(_graphElements);
		graphElements = treeSet.toArray();
	    }

	    SKIP_PASS_1 = !GraphCanvas.DEBUG1;
	    USE_FACTORY = !GraphCanvas.DEBUG2;
	    if (SKIP_PASS_1 && mustPostCompute) {
		//System.out.println("trying to skip pass #1");
		return apply_p(view, panel,
			       graphElements, params, autoApply, reapply);
	    }

	    assert isFrAGL();

	    //System.out.println("NOT trying to skip pass #1");

	    long ms0 = System.currentTimeMillis();

	    Vector rDataSets = new Vector();
	    String lastChr = null;

	    boolean ended = false;
	    //DataSet rDataSet = null;
	    GraphElement rDataSet = null;
	    LinkedList curDataList = null;
	    int ds_cnt = 0;
	    Vector curDataSets = null;

	    HashMap url_map = null;

	    int m1 = 0;
	    //System.out.println("pass apply#1 size: " + graphElements.length);

	    ToolResultContext toolResultContext = null;
	    GraphElementFactory factory = null;

	    // CC: pass apply#1
	    for (int m = 0; m < graphElements.length; m++, m1++) {
		GraphElement dataSet = ((DataSetArrayComparator)graphElements[m]).graphElement;
		String chr = VAMPUtils.getChr(dataSet);
		String id = (String)dataSet.getID();
		if (!chr.equals(lastChr)) {
		    lastChr = chr;
		    ended = false;

		    if (toolResultContext != null) {
			complete_2(rDataSet, curDataList, lastChr, url_map,
				   curDataSets, params, false);
			rDataSets.add(ToolResultManager.getInstance().epilogue(toolResultContext));
		    }

		    toolResultContext = ToolResultManager.getInstance().prologue(view.getGlobalContext(), "CGH FrAGL Karyotype Chr#" + chr, params, makeVectorID(tmpGraphElements), dataSet, null, chr);
		    //rDataSet = new DataSet();
		    rDataSet = toolResultContext.getGraphElementResult();
		    if (rDataSet != null) {
			//System.out.println("Karyo ALREADY DONE");
			rDataSets.add(rDataSet);
			toolResultContext = null;
			continue;
		    }

		    setViewType(toolResultContext);
		    //System.out.println("Karyo to be DONE");

		    ds_cnt = 0;
		    m1 = 0;
		    url_map = new HashMap();
		    curDataSets = new Vector();

		    factory = toolResultContext.getFactory();
		    factory.init("", 0, dataSet.getProperties(), false);
		    rDataSet = factory.getGraphElement();

		    rDataSet.setSourceURL(dataSet.getSourceURL());
		    rDataSet.setSourceType(dataSet.getSourceType());
		    //rDataSets.add(rDataSet);
		    curDataList = new LinkedList();
		    rDataSet.cloneProperties(dataSet);
		}

		curDataSets.add(dataSet);
		url_map.put(chr, dataSet.getURL());
		int length = dataSet.getProbeCount();
		//System.out.println("Profile len " + length + ", chr #" + VAMPUtils.getChr(dataSet) + " " + dataSet);

		int probe_cnt = 0;
		RODataElementProxy last_data = null;

		LoadPropertiesCondition load_props_cond = new MergeArrayLoadPropertiesCondition(dataSet);

		for (int n = 0; n < length; n++) {
		    RODataElementProxy odata = dataSet.getDataProxy(n);
		    if (odata == null) {
			System.out.println("data proxy is null ! at " + n + " " + dataSet.getID());
		    }

		    RWDataElementProxy data;

		    if (SKIP_PROBES2) {
			data = odata.cloneToRWProxy(load_props_cond);
			odata.copyToPos(rDataSet, data, dataSet);
		    }
		    else {
			odata.complete(dataSet);
			data = odata.cloneToRWProxy(true);
			odata.copyToPos(rDataSet, data, dataSet);
		    }

		    odata.release();

		    if (data.getPosY(rDataSet) == 0) {
			if (SKIP_PROBES2) {
			    continue;
			}
			data.setPosY(rDataSet, 0);
		    }
		    else if (data.getPosY(rDataSet) < 0) {
			data.setPosY(rDataSet, Y_COEF_FRAGL * data.getPosY(rDataSet) - Y_OFFSET_FRAGL);
		    }
		    else {
			data.setPosY(rDataSet, Y_COEF_FRAGL * data.getPosY(rDataSet) + Y_OFFSET_FRAGL);
		    }

		    factory.write(data);
		    data.release();
		    last_data = data;
		    probe_cnt++;
		}

		//System.out.println("PROBE_CNT: " + probe_cnt);
		factory.setProbeCount(probe_cnt, last_data);
	    }

	    if (toolResultContext != null) {
		complete_2(rDataSet, curDataList, lastChr, url_map, curDataSets, params, false);

		rDataSets.add(ToolResultManager.getInstance().epilogue(toolResultContext));
	    }

	    if (panel != null) {
		panel.setGraphElementIDBuilder(Config.dataSetIDArrayBuilder);
	    }

	    long ms1 = System.currentTimeMillis();
	    System.out.println("FrAGL Karyo duration: " + ((ms1 - ms0)/1000) + " seconds");

	    return undoManage(panel, rDataSets);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    class MergeArrayLoadPropertiesCondition extends LoadPropertiesCondition {

	MergeArrayLoadPropertiesCondition(GraphElement graphElement) {
	    super(graphElement);
	}

	public boolean loadProperties(Probe probe) {
	    if (skipOutliers && probe.getOut() != 0) {
		return false;
	    }

	    return probe.getGnl() != VAMPConstants.CLONE_NORMAL;
	}
    }

    // constructing a Profile instead of a DataSet
    private GraphElement complete(View view,
				  GraphElement oDataSet,
				  String lastChr,
				  HashMap url_map, Vector<GraphElement> curDataSets,
				  HashMap gained_cnt_map, HashMap lost_cnt_map,
				  TreeMap params, boolean reapply) throws Exception {
	//System.out.println("complete " + curDataSets + " " + curDataSets.size() + " Chr#" + lastChr);

	int size = curDataSets.size();
	Vector vID = (reapply ? new Vector() : null);
	Vector<String> toolVID = null;
	HashMap graphElementMap = new HashMap();
	if (reapply) {
	    for (int j = 0; j < size; j++) {
		GraphElement dataSet2 = (GraphElement)curDataSets.get(j);
		Vector<String> origGraphElements = (Vector<String>)dataSet2.
		    getPropertyValue(VAMPProperties.VectorArrayProp);
		
		Vector incrMapGraphElements = (origGraphElements != null ?
					       origGraphElements : curDataSets);
		
		for (int nn = 0; nn < incrMapGraphElements.size(); nn++) {
		    String id = (String)incrMapGraphElements.get(nn);
		    if (graphElementMap.get(id) == null) {
			graphElementMap.put(id, new Boolean(true));
			vID.add(id);
		    }
		}
	    }
	    toolVID = vID;
	}
	else {
	    toolVID = makeVectorID(curDataSets);
	}

	ToolResultContext toolResultContext = ToolResultManager.getInstance().prologue(view.getGlobalContext(), "Karyotype Chr#" + lastChr, params, toolVID, null, oDataSet, lastChr); // oDataSet
	GraphElement rGraphElement = toolResultContext.getGraphElementResult();
	if (rGraphElement != null) {
	    //System.out.println("KARYO: ALREADY DONE!");
	    return rGraphElement;
	}

	setViewType(toolResultContext);

	ToolResultContext toolResultContext1 = ToolResultManager.getInstance().prologue(view.getGlobalContext(), "Pre" + getName(type), params, curDataSets, null); // oDataSet
	GraphElementFactory factory1 = toolResultContext1.getFactory();
	//System.out.println("KARYO: MUST USE FACTORY: " + factory1.getClass().getName());
	int total_length = 0;
	for (int j = 0; j < size; j++) {
	    GraphElement dataSet2 = (GraphElement)curDataSets.get(j);
	    total_length += dataSet2.getProbeCount();
	}

	//factory1.init("", total_length, oDataSet.getProperties(), false);
	factory1.init("", 0, oDataSet.getProperties(), false);

	LinkedList curDataList = new LinkedList();

	GraphElement rDataSet = factory1.getGraphElement();

	int probe_cnt = 0;
	RODataElementProxy last_data = null;
	boolean last_normal = false;

	int gained_cnt = 0;
	int lost_cnt = 0;

	for (int j = 0; j < size; j++) {
	    GraphElement dataSet2 = (GraphElement)curDataSets.get(j);
	    Vector<String> origGraphElements = (Vector<String>)dataSet2.
		getPropertyValue(VAMPProperties.VectorArrayProp);

	    Vector incrMapGraphElements = (origGraphElements != null ?
					   origGraphElements : curDataSets);

	    int length = dataSet2.getProbeCount();
	    LoadPropertiesCondition load_props_cond = new MergeArrayLoadPropertiesCondition(dataSet2);

	    for (int n = 0; n < length; n++) {
		boolean is_normal = false;
		RODataElementProxy odata = dataSet2.getDataProxy(n);
		if (odata == null) {
		    System.out.println("data proxy is null ! at " + n + " " + dataSet2.getID());
		}

		RWDataElementProxy data;
		if (!last_normal || !SKIP_PROBES) {
		    odata.complete(dataSet2);
		    data = odata.cloneToRWProxy(true);
		    odata.release();
		}
		else {
		    data = odata.cloneToRWProxy(load_props_cond);
		    odata.release();
		}

		odata.copyToPos(rDataSet, data, dataSet2);

		int gnl = VAMPUtils.getGNL(view, dataSet2, data);
			    
		if (skipOutliers && VAMPUtils.isOutlier(data)) {
		    curDataList.add(data);
		    if (!SKIP_PROBES) {
			factory1.write(data);
		    }
		    continue;
		}
		else if (isGained(gnl)) {
		    String arrayName = (String)data.getPropertyValue(VAMPProperties.ArrayProp);
		    incrMap(gained_cnt_map, incrMapGraphElements, arrayName, reapply);
		    gained_cnt++;
		}
		else if (isLost(gnl)) {
		    String arrayName = (String)data.getPropertyValue(VAMPProperties.ArrayProp);
		    incrMap(lost_cnt_map, incrMapGraphElements, arrayName, reapply);
		    lost_cnt++;
		}
		else if (SKIP_PROBES) {
		    if (last_normal) {
			continue;
		    }
		    last_normal = true;
		    is_normal = true;
		}

		curDataList.add(data);
		factory1.write(data);
		last_data = data;
		if (!is_normal) {
		    last_normal = false;
		}
		probe_cnt++;
	    }
	}

	if (SKIP_PROBES) {
	    //System.out.println("PROBE_COUNT CHANGED " + rDataSet.getProbeCount() + " " + probe_cnt);
	    factory1.setProbeCount(probe_cnt, last_data);
	}

	if (vID == null) {
	    vID = makeVectorID(curDataSets);
	}

	complete_2(rDataSet, curDataList, lastChr, url_map,
		   vID, params, true);

	// on applique le reapply pour ce profile chr avec 
	// reapply pass #1
	//Vector vID = makeVectorID(graphElementMap);

	HashMap<String, Integer> arrayMap = makeMap(vID);
	rDataSet.setPropertyValue(Property.getProperty("ALL"), vID);

	if (skipEmptyProfiles) {
	    //System.out.println("SKIP EMPTY PROFILES");

	    setProfiles(rDataSet, gained_cnt_map,
			VAMPProperties.GainedVectorArrayProp, true, reapply);
	    setProfiles(rDataSet, lost_cnt_map,
			VAMPProperties.LostVectorArrayProp, false, reapply);
	}
	else {
	    if (reapply) {
		rDataSet.setPropertyValue
		    (VAMPProperties.LostVectorArrayProp, vID);
		rDataSet.setPropertyValue
		    (VAMPProperties.GainedVectorArrayProp, vID);
	    }
	    else {
		rDataSet.setPropertyValue
		    (VAMPProperties.LostVectorArrayProp,
		     makeVectorID(algoOrder(curDataSets, false)));
		rDataSet.setPropertyValue
		    (VAMPProperties.GainedVectorArrayProp,
		     makeVectorID(algoOrder(curDataSets, true)));
	    }
	}

	//System.out.println("gained_cnt: " + gained_cnt);
	//System.out.println("lost_cnt: " + lost_cnt);

	gained_cnt = 0;
	lost_cnt = 0;

	rDataSet = ToolResultManager.getInstance().epilogue(toolResultContext1);

	GraphElementFactory factory = toolResultContext.getFactory();

	int length = rDataSet.getProbeCount();

	//assert length == total_length;

	factory.init("", length, rDataSet.getProperties(), false);

	GraphElement rDataSet2 = factory.getGraphElement();
	// reapply pass #2
	Vector<String> lostArraySet = (Vector<String>)rDataSet.getPropertyValue(VAMPProperties.LostVectorArrayProp);
	Vector<String> gainedArraySet = (Vector<String>)rDataSet.getPropertyValue(VAMPProperties.GainedVectorArrayProp);

	HashMap gainedMap = buildMap(gainedArraySet);
	HashMap lostMap = buildMap(lostArraySet);

	for (int n = 0; n < length; n++) {
	    RWDataElementProxy data = (RWDataElementProxy)rDataSet.getDataProxy(n);
	    data.copyToPos(rDataSet2, data, rDataSet);

	    int gnl = VAMPUtils.getGNL(view, rDataSet, data);
	    // CC: close to pass apply#1 !mustPostCompute but... se below
	    data.complete(rDataSet);

	    int which = getWhich(arrayMap, (String)data.getPropertyValue(VAMPProperties.ArrayProp));
	    
	    data.setUserVal(ARRAY_OFFSET, ARRAY_SIZE, which);

	    int ind = getInd(data, isGained(gnl) ? gainedMap : lostMap);
	    if (skipOutliers && VAMPUtils.isOutlier(data)) {
		data.setPosY(rDataSet2, 0);
		data.setPropertyValue(SkipProbeProp, new Boolean(true));
		data.setUserVal(SKIP_OFFSET, SKIP_SIZE, 1);
	    }
	    else if (ind >= 0) {
		// CC: but... the position is not set to A*m+B (DataSet number) but is set to ind which could A*m+B is all profiles are kept or kept(m) in the a
		String arrayName = (String)data.getPropertyValue(VAMPProperties.ArrayProp);
		if (isGained(gnl)) {
		    data.setPosY(rDataSet2, -(ind + Y_OFFSET));
		    gained_cnt++;
		}
		else if (isLost(gnl)) {
		    data.setPosY(rDataSet2, ind + Y_OFFSET);
		    lost_cnt++;
		}
		else {
		    data.setPosY(rDataSet2, 0);
		}
	    }
	    else {
		data.setPosY(rDataSet2, 0);
	    }

	    factory.write(data);
	    data.release();
	}

	//System.out.println("gained_cnt2: " + gained_cnt);
	//System.out.println("lost_cnt2: " + lost_cnt);

	rDataSet = ToolResultManager.getInstance().epilogue(toolResultContext);

	toolResultContext1.getFactory().deleteSerialFiles();

	return rDataSet;
    }

    private void complete_2(GraphElement graphElement, LinkedList curDataList,
			    String chr, HashMap url_map,
			    Vector curDataSets, TreeMap params, boolean full) {
	if (graphElement == null) {
	    return;
	}

	DataSet rDataSet = graphElement.asDataSet();
	if (rDataSet != null) {
	    rDataSet.setData(DataElement.makeData(curDataList));
	}
	graphElement.setPropertyValue(VAMPProperties.NameProp, "Chr " + chr + " Array");
	if (isChipChip())
	    VAMPUtils.setType(graphElement, VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE);
	else if (isCGH())
	    VAMPUtils.setType(graphElement, VAMPConstants.CGH_ARRAY_MERGE_TYPE);
	else if (isFrAGL()) {
	    VAMPUtils.setType(graphElement, VAMPConstants.FRAGL_ARRAY_MERGE_TYPE);
	    graphElement.setPropertyValue(VAMPProperties.ThresholdsNameProp, VAMPConstants.THR_KARYO_FRAGL);
	}

	graphElement.setPropertyValue(VAMPProperties.URLMapProp, url_map);

	if (full) {
	    graphElement.setPropertyValue(VAMPProperties.LostVectorArrayProp, curDataSets);
	    graphElement.setPropertyValue(VAMPProperties.GainedVectorArrayProp, curDataSets);
	}
	else {
	    graphElement.setPropertyValue(VAMPProperties.LostVectorArrayProp, makeVectorID(curDataSets));
	    graphElement.setPropertyValue(VAMPProperties.GainedVectorArrayProp, makeVectorID(curDataSets));
	}

	if (!isFrAGL()) {
	    //System.out.println("VECTOR_ARRAY_PROP: setting " + curDataSets);
	    if (full) {
		graphElement.setPropertyValue(VAMPProperties.VectorArrayProp, curDataSets);
	    }
	    else {
		graphElement.setPropertyValue(VAMPProperties.VectorArrayProp, makeVectorID(curDataSets));
	    }
	}

	graphElement.setPropertyValue(paramsProp, params);
    }


    // ok
    static boolean areAllMerged(Vector graphElements) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);
	    if (!graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		(VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE) &&
		!graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		(VAMPConstants.CGH_ARRAY_MERGE_TYPE) &&
		!graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		(VAMPConstants.FRAGL_ARRAY_MERGE_TYPE))
		return false;
	}

	return true;
    }

    public boolean mustInitScale() {return true;}

    static class SmoothComparator implements Comparator {

	int which, which2;

	SmoothComparator(boolean gained, boolean mergeGainAmplicon) {
	    if (gained) {
		which = SmoothingLineEngine.SmoothingInfo.GAINED_IND;
	    }
	    else {
		which = SmoothingLineEngine.SmoothingInfo.LOST_IND;
	    }

	    if (gained && mergeGainAmplicon) {
		which2 = SmoothingLineEngine.SmoothingInfo.AMPLICON_IND;
	    }
	    else {
		which2 = -1;
	    }
	}
	
	public int compare(Object o1, Object o2) {
	    SmoothingLineEngine.SmoothingInfo i1 =
		(SmoothingLineEngine.SmoothingInfo)o1;

	    SmoothingLineEngine.SmoothingInfo i2 =
		(SmoothingLineEngine.SmoothingInfo)o2;

	    int len1, len2;
	    len1 = i1.getSegmentCount(which);
	    len2 = i2.getSegmentCount(which);

	    if (which2 >= 0) {
		len1 += i1.getSegmentCount(which2);
		len2 += i2.getSegmentCount(which2);
	    }

	    if (len1 == 0 && len2 == 0)
		return 0;

	    if (len1 == 0 && len2 != 0)
		return -1;

	    if (len1 != 0 && len2 == 0)
		return 1;

	    double spos1, spos2;
	    long slen1, slen2;

	    if (i1.getSegmentCount(which) > 0) {
		spos1 = i1.getSegment(which, 0).getPosX();
		slen1 = i1.getSegment(which, 0).getLen();
	    }
	    else {
		spos1 = Double.MAX_VALUE;
		slen1 = 0;
	    }

	    if (i2.getSegmentCount(which) > 0) {
		spos2 = i2.getSegment(which, 0).getPosX();
		slen2 = i2.getSegment(which, 0).getLen();
	    }
	    else {
		spos2 = Double.MAX_VALUE;
		slen2 = 0;
	    }

	    if (which2 >= 0) {
		if (i1.getSegmentCount(which2) > 0) {
		    double sp = i1.getSegment(which2, 0).getPosX();
		    if (sp < spos1) {
			spos1 = sp;
			slen1 = i1.getSegment(which2, 0).getLen();
		    }
		}

		if (i2.getSegmentCount(which2) > 0) {
		    double sp = i2.getSegment(which2, 0).getPosX();
		    if (sp < spos2) {
			spos2 = sp;
			slen2 = i2.getSegment(which2, 0).getLen();
		    }
		}
	    }

	    if (spos1 < spos2) {
		return -1;
	    }

	    if (spos2 < spos1) {
		return 1;
	    }

	    /*
	    if (slen1 < slen2) {
		return -1;
	    }

	    if (slen2 < slen1) {
		return 1;
	    }
	    */

	    if (slen2 < slen1) {
		return -1;
	    }

	    if (slen1 < slen2) {
		return 1;
	    }

	    return 0;
	}
    }

    private Vector algoOrder(Vector arraySet, boolean gained) {

	if (!stdSort) {
	    return arraySet;
	}


	Vector infoV = new Vector();
	for (int n = 0; n < arraySet.size(); n++) {
	    GraphElement graphElem = (GraphElement)arraySet.get(n);
	    SmoothingLineEngine.SmoothingInfo info = graphElem.getSmoothingInfo();
	    infoV.add(info);
	}

	Collections.sort(infoV, new SmoothComparator(gained, gainAndAmplicon));

	Vector rGraphElemSet = new Vector();
	for (int n = 0; n < infoV.size(); n++) {
	    rGraphElemSet.add(((SmoothingLineEngine.SmoothingInfo)infoV.get(n)).getGraphElement());
	}

	return rGraphElemSet;
    }

    private HashMap buildMap(Vector<String> arraySet) {
	HashMap map = new HashMap();
	int sz = arraySet.size();
	for (int n = 0; n < sz; n++) {
	    map.put(arraySet.get(n), new Integer(n));
	}
	return map;
    }

    private int getInd(RODataElementProxy data, HashMap map) {
	Integer i = (Integer)map.get(data.getPropertyValue(VAMPProperties.ArrayProp));
	if (i == null) {
	    return -1;
	}
	return i.intValue();
    }

    private boolean isGained(int gnl) {
	if (!keepGainAmplicon) {
	    return false;
	}

	if (gainOnly) {
	    return gnl == VAMPConstants.CLONE_GAINED;
	}

	if (ampliconOnly) {
	    return gnl == VAMPConstants.CLONE_AMPLICON;
	}

	if (gainAndAmplicon) {
	    return gnl == VAMPConstants.CLONE_GAINED ||
		gnl == VAMPConstants.CLONE_AMPLICON;
	}

	return false;
    }

    private boolean isLost(int gnl) {
	if (!keepLost) {
	    return false;
	}

	return gnl == VAMPConstants.CLONE_LOST;
    }

    GraphElement get(Vector graphElements, String name) {
	for (int n = 0; n < graphElements.size(); n++) {
	    if (name.compareTo((String)((GraphElement)graphElements.get(n)).
			       getPropertyValue(VAMPProperties.NameProp)) == 0) {
		return (GraphElement)graphElements.get(n);
	    }
	}

	System.out.println("OUPS " + name + " not found! size " + graphElements.size());
	(new Exception()).printStackTrace();
	return null;
    }

    String getID(Vector graphElements, String name) {
	for (int n = 0; n < graphElements.size(); n++) {
	    if (name.compareTo((String)graphElements.get(n)) == 0) {
		return (String)graphElements.get(n);
	    }
	}

	System.out.println("OUPS ID " + name + " not found! size " + graphElements.size());
	(new Exception()).printStackTrace();
	return null;
    }

    void incrMap(HashMap map, Vector graphElements, String name, boolean reapply) {
	if (!reapply) {
	    incrMap(map, graphElements, name);
	    return;
	}

	String id = getID(graphElements, name);

	Integer i = (Integer)map.get(id);
	if (i == null) {
	    map.put(id, new Integer(0));
	}
	else {
	    map.put(id, new Integer(i.intValue() + 1));
	}
    }

    void incrMap(HashMap map, Vector graphElements, String name) {
	GraphElement graphElem = get(graphElements, name);

	Integer i = (Integer)map.get(graphElem);
	if (i == null) {
	    map.put(graphElem, new Integer(0));
	}
	else {
	    map.put(graphElem, new Integer(i.intValue() + 1));
	}
    }

    /*
    SerialGraphElement getSerialGraphElement(Vector<SerialGraphElement> serialGraphElements, String name) {
	int size = serialGraphElements.size();

	for (int n = 0; n < size; n++) {
	    if (name.compareTo(serialGraphElements.get(n).getID()) == 0) {
		return serialGraphElements.get(n);
	    }
	}

	System.out.println("OUPS2 " + name + " not found! size " + serialGraphElements.size());
	(new Exception()).printStackTrace();
	return null;
    }

    void incrMap2(HashMap map, Vector<SerialGraphElement> serialGraphElements, String name) {
	SerialGraphElement serialGraphElem = getSerialGraphElement(serialGraphElements, name);

	Integer i = (Integer)map.get(serialGraphElem);
	if (i == null) {
	    map.put(serialGraphElem, new Integer(0));
	}
	else {
	    map.put(serialGraphElem, new Integer(i.intValue() + 1));
	}
    }
    */

    /*
    void displaySkip(HashMap map, String side, GraphElement dataSet) {
	Iterator it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    if (((Integer)entry.getValue()).intValue() == 0)
		System.out.println("should skip profile " + entry.getKey() + " from the " + side + " side on " + dataSet.getID());
	    else
		System.out.println("should keep profile " + entry.getKey() + " from the " + side + " side " + entry.getValue() + " on " + dataSet.getID());
	}
    }
    */

    void setProfiles(GraphElement dataSet, HashMap map, Property prop, boolean gained, boolean reapply) {
	if (!reapply) {
	    setProfiles(dataSet, map, prop, gained);
	    return;
	}

	Iterator it = map.entrySet().iterator();
	Vector profileV = new Vector();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    if (((Integer)entry.getValue()).intValue() != 0) {
		profileV.add(entry.getKey()); // Vector<String>
	    }
	}

	dataSet.setPropertyValue(prop, profileV);
    }

    void setProfiles(GraphElement dataSet, HashMap map, Property prop, boolean gained) {
	Iterator it = map.entrySet().iterator();
	Vector profileV = new Vector();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    if (((Integer)entry.getValue()).intValue() != 0) {
		profileV.add(entry.getKey());
	    }
	}

	profileV = algoOrder(profileV, gained);
	dataSet.setPropertyValue(prop, makeVectorID(profileV));
    }

    private HashMap<String, Integer> makeMap(Vector<String> graphElementID) {
	HashMap<String, Integer> arrayMap = new HashMap();
	for (int n = 0; n < graphElementID.size(); n++) {
	    arrayMap.put(graphElementID.get(n), new Integer(n));
	}
	return arrayMap;
    }

    private int getWhich(HashMap<String, Integer> arrayMap, String arr_name) {
	return arrayMap.get(arr_name).intValue();
    }

    public boolean supportProfiles() {
	return true;
    }
}
