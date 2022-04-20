
/*
 *
 * FrAGLOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

/*
 * idee :
 * + support minimul
 * + interpolation sur les N voisins (voir P. Huppe)
 *     
 */

import java.util.*;
import java.io.*;
import java.awt.*;
import fr.curie.vamp.data.Profile;

class FrAGLOP extends GraphElementListOperation {
   
    static final boolean USE_BARPLOT_DISPLAYER = true;
    static final boolean KEEP_ONLY_COMMON_PROPERTIES = false;

    static final String NAME = "CGH FrAGL";

    //    static final double CONFIDENCE_COEF = 0.5;

    static final Property SupportCountProp = Property.getProperty("Support Count");
    static final Property GainedCountProp = Property.getProperty("Gained Count");
    static final Property LostCountProp = Property.getProperty("Lost Count");
    static final Property AmpliconCountProp = Property.getProperty("Amplicon Count");
    static final Property GainedAmpliconCountProp = Property.getProperty("Gained/Amplicon Count");

    static final Property GainedListProp = Property.getProperty("Gained List");
    static final Property LostListProp = Property.getProperty("Lost List");
    static final Property AmpliconListProp = Property.getProperty("Amplicon List");
    static final Property GainedAmpliconListProp = Property.getProperty("Gained/Amplicon List");
    
    static final Property GainedRatioAverageProp = Property.getProperty("Gained Ratio Average");
    static final Property LostRatioAverageProp = Property.getProperty("Lost Ratio Average");
    static final Property GainedAmpliconRatioAverageProp = Property.getProperty("Gained/Amplicon Ratio Average");
    static final Property AmpliconRatioAverageProp = Property.getProperty("Amplicon Ratio Average");

    static final Property FrequencyProp = Property.getProperty("Frequency");
    static final Property ConfidenceProp = Property.getProperty("Confidence");
    static final Property SkipProp = Property.getHiddenProperty("__skip");
    static final Property ConfidenceRatioProp = Property.getHiddenProperty("__confidence_ratio");
    static final Property AverageRatioProp = Property.getHiddenProperty("__average_ratio");

    boolean merge;

    static final String GNL_PARAM = "GNL";
    static final String RATIO_PARAM = "Ratio";

    static final int GAIN_AMPLICON_MASK = 0x1;
    static final int GAIN_MASK = 0x2;
    static final int AMPLICON_MASK = 0x4;
    static final int MERGE_GAIN_AMPLICON_MASK = 0x8;
    static final int LOSS_MASK = 0x10;

    static final String ALT_MASK_PARAM = "AltMask";

    static final String COLOR_MASK_PARAM = "UseColorMask";

    static final int USE_RATIO_AVERAGE_MASK = 0x1;
    static final int USE_CONFIDENCE_MASK = 0x2;

    static final String DISPLAY_PARAM = "Display";
    static final int DISPLAY_PROFILE = 0x1;
    static final int DISPLAY_KARYO = 0x2;

    static final String REPORT_PARAM = "Report";
    static final int HTML_REPORT = 0x4;
    static final int CSV_REPORT = 0x8;

    static final String RESULT_PARAM = "Result";
    static final String DETAILS_PARAM = "Details";

    static final String MIN_SUPPORT_ALT_PARAM = "MinSupportAlt";
    static final String MIN_PERCENT_ALT_PARAM = "MinPercentAlt";

    static final String MIN_VALUE_CONF_PARAM = "MinSupportConf";
    static final String MIN_PERCENT_CONF_PARAM = "MinPercentConf";

    static final String ALL = "All";
    static final String TRUE = "True";
    static final String	FALSE = "False";

    boolean useRatioAverage = true;
    boolean gainAmpliconAlt = true;
    boolean gainAlt = true;
    boolean ampliconAlt = true;
    boolean mergeGainAmpliconAlt = true;
    boolean lostAlt = true;
    //boolean normalize_ok;

    public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE,
			    VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.SNP_TYPE,
			    VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE};
    }

    public String getReturnedType() {
	return null;
    }

    static String getName(int type) {
	return NAME;
    }

    FrAGLOP() {
	super(NAME, SHOW_MENU | ON_ALL_AUTO);
    }

    static final HashMap view_map = new HashMap();

    public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = FrAGLDialog.getParams
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
	params.put(RATIO_PARAM, TRUE);
	params.put(GNL_PARAM, FALSE);
	params.put(ALT_MASK_PARAM, new Integer(GAIN_AMPLICON_MASK | GAIN_MASK | LOSS_MASK));
	params.put(COLOR_MASK_PARAM, new Integer(USE_CONFIDENCE_MASK));
	params.put(RESULT_PARAM, new Integer(DISPLAY_PROFILE));
	params.put(DETAILS_PARAM, FALSE);
	params.put(MIN_SUPPORT_ALT_PARAM, new Integer(1));
	params.put(MIN_VALUE_CONF_PARAM, new Integer(1));

	view_map.put(view, params);
	return params;
    }

    public TreeMap makeParams(HashMap map) {
	TreeMap params = new TreeMap();
	params.put(RATIO_PARAM, map.get(RATIO_PARAM));
	params.put(GNL_PARAM, map.get(GNL_PARAM));
	params.put(ALT_MASK_PARAM, Utils.makeInteger(map.get(ALT_MASK_PARAM)));
	params.put(COLOR_MASK_PARAM, Utils.makeInteger(map.get(COLOR_MASK_PARAM)));
	params.put(RESULT_PARAM, Utils.makeInteger(map.get(RESULT_PARAM)));
	params.put(DISPLAY_PARAM, map.get(DISPLAY_PARAM));
	params.put(DETAILS_PARAM, map.get(DETAILS_PARAM));

	if (map.get(MIN_SUPPORT_ALT_PARAM) != null)
	    params.put(MIN_SUPPORT_ALT_PARAM, Utils.makeInteger(map.get(MIN_SUPPORT_ALT_PARAM)));

	if (map.get(MIN_VALUE_CONF_PARAM) != null)
	    params.put(MIN_VALUE_CONF_PARAM, Utils.makeInteger(map.get(MIN_VALUE_CONF_PARAM)));

	return params;
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	if (graphElements.size() < 2)
	    return false;;

	int size = graphElements.size();
	/*
	DataSet dataSet = ((GraphElement)graphElements.get(0)).asDataSet();
	if (dataSet == null)
	    return false;
	merge = VAMPUtils.isMergeChr(dataSet);

	for (int m = 1; m < size; m++) {
	    dataSet = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (dataSet == null)
		return false;
	    if (merge != VAMPUtils.isMergeChr(dataSet))
		return false;
	}
	*/
	GraphElement graphElement = (GraphElement)graphElements.get(0);
	merge = VAMPUtils.isMergeChr(graphElement);

	for (int m = 1; m < size; m++) {
	    graphElement = (GraphElement)graphElements.get(m);
	    if (merge != VAMPUtils.isMergeChr(graphElement))
		return false;
	}
	return true;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	try {
	    int result_mask = ((Integer)params.get(RESULT_PARAM)).intValue();
	    if (result_mask == 0)
		return null;

	    boolean display = params.get(DISPLAY_PARAM) != null;
	    int color_mask = ((Integer)params.get(COLOR_MASK_PARAM)).intValue();
	    int alt_mask = ((Integer)params.get(ALT_MASK_PARAM)).intValue();

	    gainAmpliconAlt = (alt_mask & GAIN_AMPLICON_MASK) != 0;
	    gainAlt = (alt_mask & GAIN_MASK) != 0;
	    lostAlt = (alt_mask & LOSS_MASK) != 0;
	    ampliconAlt = (alt_mask & AMPLICON_MASK) != 0;
	    mergeGainAmpliconAlt = (alt_mask & MERGE_GAIN_AMPLICON_MASK) != 0;

	    useRatioAverage = (color_mask & USE_RATIO_AVERAGE_MASK) != 0;

	    boolean use_gnl = params.get(GNL_PARAM).equals(TRUE);
	    GraphElement ref = (GraphElement)graphElements.get(0);

	    GlobalContext globalContext = (view != null ? view.getGlobalContext() :
					   (GlobalContext)params.get("GlobalContext"));
	    // resetting result and display for cache management
	    params.put(RESULT_PARAM, new Integer(DISPLAY_PROFILE));
	    params.put(DISPLAY_PARAM, new Boolean(true));

	    GraphElement dataSet = new DataSet();

	    ToolResultContext toolResultContext = ToolResultManager.getInstance().prologue(globalContext, NAME, params, graphElements, dataSet, ref);

	    toolResultContext.getInfo().grphDispName = "Centered Barplot";
	    toolResultContext.getInfo().viewType = "Centered Barplot";

	    GraphElement rGraphElement = toolResultContext.getGraphElementResult();
	    if (rGraphElement == null && view != null) {
		graphElements = NormalizeOP.normalizeOnDemand(view.getGlobalContext(), "FrAGL", graphElements);
		if (graphElements == null) {
		    return null;
		}
	    }

	    long ms0 = System.currentTimeMillis();
	    int l = getLength();
	    if (l == 0) {
		return null;
	    }

	    int length = ref.getProbeCount();
	    GraphElementFactory factory = null;

	    boolean mustPerform = true;

	    // resetting result and display for cache management
	    /*
	    params.put(RESULT_PARAM, new Integer(DISPLAY_PROFILE));
	    params.put(DISPLAY_PARAM, new Boolean(true));

	    ToolResultContext toolResultContext = ToolResultManager.getInstance().prologue(globalContext, NAME, params, graphElements, dataSet, ref);

	    toolResultContext.getInfo().grphDispName = "Centered Barplot";
	    toolResultContext.getInfo().viewType = "Centered Barplot";

	    GraphElement rGraphElement = toolResultContext.getGraphElementResult();
	    */

	    if (rGraphElement != null) {
		dataSet = rGraphElement;
		mustPerform = false;
		//System.out.println("FrAGL: ALREADY DONE!");
	    }
	    else {
		factory = toolResultContext.getFactory();
		//System.out.println("FrAGL: MUST USE FACTORY: " + factory.getClass().getName());
	    }

	    int size = graphElements.size();
	    String arr_cnt = (new Integer(size)).toString();
	    String array_name = "FrAGL [" + arr_cnt + "]";

	    int min_support_cnt;
	    int min_confidence_cnt;

	    Double vpercent;
	    Object vmin;

	    vmin = params.get(MIN_SUPPORT_ALT_PARAM);
	    if (vmin != null) {
		if (vmin.equals(ALL))
		    min_support_cnt = size;
		else
		    min_support_cnt = ((Integer)vmin).intValue();
	    }
	    else if ((vpercent = (Double)params.get(MIN_PERCENT_ALT_PARAM)) != null) {
		min_support_cnt = (int)(size * vpercent.doubleValue()/100);
	    }
	    else
		min_support_cnt = 1;

	    vmin = params.get(MIN_VALUE_CONF_PARAM);
	    if (vmin != null) {
		if (vmin.equals(ALL))
		    min_confidence_cnt = size;
		else
		    min_confidence_cnt = ((Integer)vmin).intValue();
	    }
	    else if ((vpercent = (Double)params.get(MIN_PERCENT_CONF_PARAM)) != null) {
		min_confidence_cnt = (int)(size * vpercent.doubleValue()/100);
	    }
	    else
		min_confidence_cnt = 0;

	    if (mustPerform) {
		StandardColorCodes cc[] = new StandardColorCodes[size];
		for (int m = 0; m < size; m++)
		    cc[m] = (StandardColorCodes)VAMPUtils.getColorCodes
			((GraphElement)graphElements.get(m));

		//DataElement data[];
		//data = null;
		factory.init("", l * length, ref.getProperties());

		for (int n = 0; n < length; n++) {
		    int gained_n, lost_n;
		    RWDataElementProxy data_gained, data_lost;

		    if (gainAmpliconAlt) {
			gained_n = l * n;
		    }
		    else {
			gained_n = -1;
		    }

		    if (lostAlt) {
			if (gained_n >= 0) {
			    lost_n = gained_n + 1;
			}
			else {
			    lost_n = l * n;
			}
		    }
		    else {
			lost_n = -1;
		    }

		    RODataElementProxy refData = ref.getDataProxy(n);

		    if (gained_n >= 0) {
			refData.complete(ref);
			data_gained = refData.cloneToRWProxy(true);
			refData.copyToPos(dataSet, data_gained, ref);
		    }
		    else {
			data_gained = null;
		    }

		    if (lost_n >= 0) {
			refData.complete(ref);
			data_lost = refData.cloneToRWProxy(true);
			refData.copyToPos(dataSet, data_lost, ref);
		    }
		    else {
			data_lost = null;
		    }

		    TreeMap propMap = (data_gained != null ? data_gained : data_lost).getProperties();

		    int support_cnt = 0;
		    int gained_cnt = 0;
		    int lost_cnt = 0;
		    String gained_list = "";
		    String lost_list = "";
		    boolean isNA = true;
		    double gained_avg_y = 0;
		    double lost_avg_y = 0;

		    Vector ds_id_v = new Vector();
		    for (int m = 0; m < size; m++) {
			GraphElement ds = (GraphElement)graphElements.get(m);
			ds_id_v.add(ds.getID());
		    }

		    for (int m = 0; m < size; m++) {

			GraphElement ds = (GraphElement)graphElements.get(m);

			RODataElementProxy d = ds.getDataProxy(n);

			if (!VAMPUtils.isNA(d)) {
			    isNA = false;
			    support_cnt++;
			    double y = d.getVY(ds);

			    boolean gained, lost, amplicon;
			    if (use_gnl) {
				int gnl = VAMPUtils.getGNL(view, ds, d, true);
				gained = (gnl == VAMPConstants.CLONE_GAINED);
				lost = (gnl == VAMPConstants.CLONE_LOST);
				amplicon = (gnl == VAMPConstants.CLONE_AMPLICON);
			    } else {
				lost = (y <= cc[m].getNormalMin());
				gained = (y >= cc[m].getNormalMax() &&
					  y <= cc[m].getAmplicon());
				amplicon = y > cc[m].getAmplicon();
			    }
				
			    if (mergeGainAmpliconAlt) {
				if (amplicon) {
				    gained = true;
				    amplicon = false;
				}
			    }
			    else if (ampliconAlt)
				gained = false;

			    if (lost) {
				if (lost_list.length() > 0)
				    lost_list += "\n";
				lost_list += (String)ds_id_v.get(m);
				lost_avg_y += y;
				lost_cnt++;
			    }

			    if (gained || amplicon) {
				if (gained_list.length() > 0)
				    gained_list += "\n";
				gained_list += (String)ds_id_v.get(m);
				gained_avg_y += y;
				gained_cnt++;
			    }
			}

			if (KEEP_ONLY_COMMON_PROPERTIES) {
			    d.complete(ds);
			    Vector toRemove = new Vector();
			    Iterator it = propMap.entrySet().iterator();
			    while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				Property prop = (Property)entry.getKey();
				Object value = d.getPropertyValue(prop);
				if (value == null || !value.equals(entry.getValue())) {
				    System.out.println("#" + n + ": removing " +
						       prop.getName() + " " +
						       value + " vs. " +
						       entry.getValue());
				    toRemove.add(prop);
				}
				else {
				    System.out.println("#" + n + ": NOT removing " +
						       prop.getName() + " " +
						       value);
				}
			    }

			    for (int k = 0; k < toRemove.size(); k++) {
				propMap.remove(toRemove.get(k));
			    }
			    System.out.println("has removed");
			    d.release();
			}

			d.release(ds, n);
		    }

		    updateProperties(size, dataSet, cc[0],
				     data_gained, data_lost,
				     propMap, isNA, min_support_cnt, min_confidence_cnt,
				     gained_list, gained_cnt, gained_avg_y,
				     lost_list, lost_cnt, lost_avg_y,
				     support_cnt, array_name);

		    if (data_gained != null) {
			factory.write(data_gained);
			data_gained.release();
		    }
		    
		    if (data_lost != null) {
			factory.write(data_lost);
			data_lost.release();
		    }
		}

		dataSet = factory.getGraphElement();

		// note: was before the loop
		dataSet.setCommonProperties(graphElements);
		
		//dataSet.setPropertyValue(VAMPProperties.RatioScaleProp, VAMPProperties.RatioScale_M);

		dataSet.setPropertyValue(ParamsProp, params);
		dataSet.setPropertyValue(VAMPProperties.CloneCountProp, new Integer(length));
		dataSet.setPropertyValue(GenomeAlterationOP.SupportProp, params.get(MIN_SUPPORT_ALT_PARAM));
		dataSet.setPropertyValue(VAMPProperties.RatioProp, params.get(RATIO_PARAM));

		VAMPUtils.setType(dataSet, merge ? VAMPConstants.FRAGL_CHROMOSOME_MERGE_TYPE :
				  VAMPConstants.FRAGL_TYPE);

		dataSet.setPropertyValue(VAMPProperties.ArrayCountProp, arr_cnt);
		dataSet.setPropertyValue(VAMPProperties.NameProp, array_name);
		dataSet.setPropertyValue(VAMPProperties.ArraysRefProp, makeVectorID(graphElements));
		dataSet.setPropertyValue(VAMPProperties.VectorArrayProp, makeVectorID(graphElements));

		//dataSet.setAutoY(true);

		dataSet.setPropertyValue(VAMPProperties.ThresholdsNameProp, VAMPConstants.THR_FRAGL);

		factory.setGraphElementProperties(dataSet.getProperties());
		dataSet = ToolResultManager.getInstance().epilogue(toolResultContext);
	    }

	    if (display &&
		(result_mask & DISPLAY_PROFILE) != 0 ||
		(result_mask & DISPLAY_KARYO) != 0) {
		// under some condition:
		long ms1 = System.currentTimeMillis();
		System.out.println("FrAGL duration: " + (ms1-ms0)/1000 + " seconds");
		factory = null;
		for (int m = 0; m < size; m++) {
		    GraphElement ds = (GraphElement)graphElements.get(m);
		    ds.release();
		}

		if ((result_mask & DISPLAY_PROFILE) != 0) {
		    Vector rGraphElements = new Vector();
		    rGraphElements.add(dataSet);
		    buildProfileView(globalContext, panel, rGraphElements);
		}

		if ((result_mask & DISPLAY_KARYO) != 0) {
		    Vector rGraphElements = new Vector();
		    if ((result_mask & DISPLAY_PROFILE) != 0) {
			dataSet = dataSet.dupSerializer();
		    }
		    rGraphElements.add(dataSet);
		    buildKaryoView(globalContext, panel, rGraphElements);
		}
	    }
	    
	    if (params.get(REPORT_PARAM) != null &&
		(result_mask & HTML_REPORT) != 0 ||
		(result_mask & CSV_REPORT) != 0) {
		if (getLength() != 2) {
		    InfoDialog.pop(globalContext,
				   "Cannot only build report for complete FrAGL computation (including Gain and Loss)");
		    return graphElements;
		}
		buildReport(dataSet, graphElements, use_gnl, min_support_cnt,
			    min_confidence_cnt,
			    result_mask, params.get(DETAILS_PARAM).equals(TRUE));
	    }
	    
	    return graphElements;
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    void buildReport(GraphElement dataSet, Vector graphElements, boolean use_gnl,
		     int min_support_cnt,
		     int min_confidence_cnt,
		     int result_mask,
		     boolean details) throws Exception {
	File file = DialogUtils.openFileChooser(new Frame(), "Save", 0, true);
	if (file == null)
	    return;

	boolean isHTML = (result_mask & HTML_REPORT) != 0;
	boolean isCSV = (result_mask & CSV_REPORT) != 0;

	String ext = isCSV ? ".csv" : ".html";

	if (!Utils.hasExtension(file.getName(), ext))
	    file = new File(file.getAbsolutePath() + ext);

	FileOutputStream os = new FileOutputStream(file);
	PrintStream ps = new PrintStream(os);
	ReportBuilder builder;
	if (isHTML)
	    builder = new HTMLReportBuilder(ps, NAME);
	else
	    builder = new CSVReportBuilder(ps, NAME);

	builder.startDocument();

	int size = graphElements.size();
	
	builder.addTitle3("Parameters");
	builder.addText("Based on " + (use_gnl ? "Status" : "Ratio"));
	builder.addVPad();
	builder.addText("Minimal support of alterations: " + min_support_cnt +
			" (" + Utils.performRound(((double)min_support_cnt/size)*100, 2) + " %)");
	builder.addVPad();
	builder.addText("Minimal confidence: " + min_confidence_cnt +
			" (" + Utils.performRound(((double)min_confidence_cnt/size)*100, 2) + " %)");
	builder.addVPad();
	String str = "Alterations: ";
	if (gainAmpliconAlt) {
	    if (gainAlt)
		str += "Gain";
	    else if (ampliconAlt)
		str += "Amplicon";
	    else if (mergeGainAmpliconAlt)
		str += "Merge Gain/Amplicon";
	}

	if (lostAlt) {
	    if (gainAmpliconAlt)
		str += " & ";
	    str += "Loss";
	}

	builder.addText(str);


	builder.addVPad(2);
	builder.addText("Array Scope (" + size + ")",
			HTMLReportBuilder.BOLD_STYLE);
	builder.addVPad();
	for (int m = 0; m < size; m++) {
	    builder.addText((String)((GraphElement)graphElements.get(m)).getID());
	    builder.addVPad();
	}

	builder.addVPad();

	builder.addTitle3("Results");
	String std_tbl[] = buildReportList(false);
	String details_tbl[] = buildReportList(true);

	//	builder.startTable(details ? details_tbl : std_tbl, "style='background-color: #eeeeee'");
	builder.startTable(details ? details_tbl : std_tbl);
	
	StandardColorCodes cc = (StandardColorCodes)VAMPUtils.getColorCodes(dataSet);
	//DataElement data[] = dataSet.getData();

	//	for (int n = 0; n < data.length; n++) {
	int length = dataSet.getProbeCount();
	for (int n = 0; n < length; n++) {

	    RODataElementProxy d = dataSet.getDataProxy(n);
	    d.complete(dataSet);

	    String confidence_ratio = (String)d.getPropertyValue(ConfidenceRatioProp);
	    String confidence_style = getStyle(confidence_ratio, cc);

	    String average_ratio = (String)d.getPropertyValue(AverageRatioProp);
	    String ratio_average_style = getStyle(average_ratio, cc);

	    if ((n & 1) == 0) {
		if (d.getPropertyValue(SkipProp) != null) {
		    d.release();
		    continue;
		}
		builder.startRow();
		builder.addCell((String)d.getID());
		//builder.addCell(VAMPUtils.getChr(d));
		builder.addCell(d.getSChr());
		builder.addCell((String)d.getPropertyValue(SupportCountProp));

		if (mergeGainAmpliconAlt) {
		    String s = (String)d.getPropertyValue(GainedAmpliconCountProp);
		    builder.addCell(s);
		    if (s.equals("0")) {
			builder.addCell("-", "align=center");
			builder.addCell("-", "align=center");
		    }
		    else {
			builder.addCell(Utils.performRound(d.getPropertyValue(GainedAmpliconRatioAverageProp)), ratio_average_style);
			builder.addCell(Utils.performRound((String)d.getPropertyValue(ConfidenceProp), 2), confidence_style);
		    }
		}
		else if (ampliconAlt) {
		    String s = (String)d.getPropertyValue(AmpliconCountProp);
		    builder.addCell(s);
		    if (s.equals("0")) {
			builder.addCell("-", "align=center");
			builder.addCell("-", "align=center");
		    }
		    else {
			builder.addCell(Utils.performRound(d.getPropertyValue(AmpliconRatioAverageProp)), ratio_average_style);
			builder.addCell(Utils.performRound((String)d.getPropertyValue(ConfidenceProp), 2), confidence_style);
		    }
		}
		else {
		    String s = (String)d.getPropertyValue(GainedCountProp);
		    builder.addCell(s);
		    if (s.equals("0")) {
			builder.addCell("-", "align=center");
			builder.addCell("-", "align=center");
		    }
		    else {
			builder.addCell(Utils.performRound(d.getPropertyValue(GainedRatioAverageProp)), ratio_average_style);
			builder.addCell(Utils.performRound((String)d.getPropertyValue(ConfidenceProp), 2), confidence_style);
		    }
		}

		if (details) {
		    String s;
		    if (mergeGainAmpliconAlt)
			s = (String)d.getPropertyValue(GainedAmpliconListProp);
		    else if (ampliconAlt)
			s = (String)d.getPropertyValue(AmpliconListProp);
		    else
			s = (String)d.getPropertyValue(GainedListProp);
		    if (s.length() > 0)
			builder.addCell(builder.replaceNL(s));
		    else
			builder.addEmptyCell();
		}
	    }
	    else {
		if (d.getPropertyValue(SkipProp) != null) {
		    d.release();
		    continue;
		}

		String s = (String)d.getPropertyValue(LostCountProp);
		builder.addCell(s);
		if (s.equals("0")) {
		    builder.addCell("-", "align=center");
		    builder.addCell("-", "align=center");
		}
		else {
		    builder.addCell(Utils.performRound(d.getPropertyValue(LostRatioAverageProp)), ratio_average_style);
		    builder.addCell(Utils.performRound((String)d.getPropertyValue(ConfidenceProp), 2), confidence_style);
		}

		if (details) {
		    s = (String)d.getPropertyValue(LostListProp);
		    if (s.length() > 0)
			builder.addCell(builder.replaceNL(s));
		    else {
			builder.addEmptyCell();
		    }
		}
		builder.endRow();
	    }
	    d.release();
	}

	builder.endTable();
	builder.endDocument();
    }

    void updateProperties(int size, GraphElement dataSet, StandardColorCodes cc,
			  RWDataElementProxy gained, RWDataElementProxy lost,
			  TreeMap propMap, boolean isNA, int min_support_cnt,
			  int min_confidence_cnt,
			  String gained_list, int gained_cnt, double gained_avg_y,
			  String lost_list, int lost_cnt, double lost_avg_y,
			  int support_cnt,
			  String array_name) {

	if (gained != null)
	    updateProperties(gained, propMap);
	if (lost != null)
	    updateProperties(lost, propMap);

	if (isNA) {
	    if (gained != null)
		gained.setPropertyValue(VAMPProperties.IsNAProp, "True");
	    if (lost != null)
		lost.setPropertyValue(VAMPProperties.IsNAProp, "True");
	}
	else {
	    if (gained != null)
		gained.setPropertyValue(VAMPProperties.IsNAProp, "False");
	    if (lost != null)
		lost.setPropertyValue(VAMPProperties.IsNAProp, "False");
	}

	if (gained != null) {
	    if (mergeGainAmpliconAlt)
		gained.setPropertyValue(GainedAmpliconCountProp,
					Utils.toString(gained_cnt));
	    else if (ampliconAlt)
		gained.setPropertyValue(AmpliconCountProp,
					Utils.toString(gained_cnt));
	    else
		gained.setPropertyValue(GainedCountProp,
					Utils.toString(gained_cnt));
	    
	    gained.setPropertyValue(SupportCountProp,
				    Utils.toString(support_cnt));
	}

	if (lost != null) {
	    lost.setPropertyValue(LostCountProp,
				  Utils.toString(lost_cnt));

	    lost.setPropertyValue(SupportCountProp,
				  Utils.toString(support_cnt));
	}

	double confidence = (double)support_cnt / size;
	double min_confidence = (double)min_confidence_cnt / size;

	if (support_cnt < min_support_cnt ||
	    confidence < min_confidence ||
	    (gained_cnt == 0 && lost_cnt == 0)) {
	    if (gained != null)
		gained.setPropertyValue(SkipProp, TRUE);
	    if (lost != null)
		lost.setPropertyValue(SkipProp, TRUE);
	}	    

	if (gained_cnt != 0)
	    gained_avg_y /= gained_cnt;

	double gained_freq = (double)gained_cnt / support_cnt;
	double gained_ratio;

	if (gained != null) {
	    double beta_gained = cc.getNormalMin() + (cc.getNormalMax() - cc.getNormalMin())/2;
	    double alpha_gained = cc.getMax() - beta_gained;
	    
	    double gained_average_ratio = gained_avg_y;
	    double gained_confidence_ratio = alpha_gained * confidence + beta_gained;
	    if (useRatioAverage) {
		gained_ratio = gained_average_ratio;
	    }
	    else {
		gained_ratio = gained_confidence_ratio;
	    }

	    if (mergeGainAmpliconAlt)
		gained.setPropertyValue(GainedAmpliconRatioAverageProp,
					Utils.toString(gained_avg_y));
	    else if (ampliconAlt)
		gained.setPropertyValue(AmpliconRatioAverageProp,
					Utils.toString(gained_avg_y));
	    else
		gained.setPropertyValue(GainedRatioAverageProp,
					Utils.toString(gained_avg_y));

	    gained.setPropertyValue(FrequencyProp,
				    Utils.toString((double)gained_cnt / support_cnt));

	    gained.setPropertyValue(ConfidenceRatioProp,
				    Utils.toString(gained_confidence_ratio));

	    gained.setPropertyValue(AverageRatioProp,
				    Utils.toString(gained_average_ratio));

	    gained.setPropertyValue(VAMPProperties.RatioProp,
				    Utils.toString(gained_ratio));

	    if (mergeGainAmpliconAlt)
		gained.setPropertyValue(GainedAmpliconListProp, gained_list);
	    else if (ampliconAlt)
		gained.setPropertyValue(AmpliconListProp, gained_list);
	    else
		gained.setPropertyValue(GainedListProp, gained_list);
	    
	    gained.setPropertyValue(ConfidenceProp, Utils.toString(confidence));
	}

	double lost_freq = (double)lost_cnt / support_cnt;
	double lost_ratio;

	if (lost_cnt != 0)
	    lost_avg_y /= lost_cnt;

	if (lost != null) {
	    
	    double beta_lost = cc.getNormalMin() + (cc.getNormalMax() - cc.getNormalMin())/2;
	    double alpha_lost = cc.getMin() - beta_lost;
	    
	    double lost_average_ratio = lost_avg_y;
	    double lost_confidence_ratio = alpha_lost * confidence + beta_lost;

	    if (useRatioAverage) {
		lost_ratio = lost_average_ratio;
	    }
	    else {
		lost_ratio = lost_confidence_ratio;
	    }

	    lost.setPropertyValue(LostRatioAverageProp,
				  Utils.toString(lost_avg_y));
	    
	    lost.setPropertyValue(FrequencyProp, Utils.toString(lost_freq));

	    lost.setPropertyValue(ConfidenceRatioProp,
				  Utils.toString(lost_confidence_ratio));

	    lost.setPropertyValue(AverageRatioProp,
				  Utils.toString(lost_average_ratio));

	    lost.setPropertyValue(VAMPProperties.RatioProp,
				  Utils.toString(lost_ratio));

	    lost.setPropertyValue(LostListProp, lost_list);
	    
	    lost.setPropertyValue(ConfidenceProp, Utils.toString(confidence));
	}

	if (support_cnt >= min_support_cnt && confidence >= min_confidence) {
	    if (gained != null)
		gained.setPosY(dataSet, gained_freq);
	    if (lost != null)
		lost.setPosY(dataSet, -lost_freq);
	} else {
	    if (gained != null)
		gained.setPosY(dataSet, 0);
	    if (lost != null)
		lost.setPosY(dataSet, 0);
	}

	if (gained != null) {
	    gained.setPropertyValue(VAMPProperties.ArrayProp, array_name);
	    maskProperties(gained);
	}

	if (lost != null) {
	    lost.setPropertyValue(VAMPProperties.ArrayProp, array_name);
	    maskProperties(lost);
	}
    }

    void buildProfileView(GlobalContext globalContext, GraphPanel panel,
			  Vector rGraphElements) {
	GraphElementDisplayer histo_dsp;
	if (USE_BARPLOT_DISPLAYER) {
	    histo_dsp = new BarplotDataSetDisplayer(true);
	}
	else {
	    histo_dsp = new PointDataSetDisplayer(false);
	}

	PanelProfile panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     histo_dsp,
	     (merge ? Config.defaultChromosomeNameAxisDisplayer :
	      Config.defaultGenomicPositionAxisDisplayer),
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	    
	PanelProfile panelProfiles[] = new PanelProfile[]{panelProfile};

	buildView(globalContext, panelProfiles, Config.defaultDim, panel,
		  rGraphElements);
	/*
	//GraphPanelSet panelSet = view.getGraphPanelSet();
	ViewFrame vf = new ViewFrame(globalContext,
	"FrAGL",
	panelProfiles,
	null,
	null,
	null, null,
	new LinkedList(),
	Config.defaultDim,
	null);

	LinkedList list = Utils.vectorToList(rGraphElements);
	vf.getView().getGraphPanelSet().getPanel(panel != null ? panel.getWhich() : 0).setGraphElements(list);
	vf.setVisible(true);
	vf.getView().syncGraphElements();
	*/
    }

    void buildKaryoView(GlobalContext globalContext, GraphPanel panel,
			Vector rGraphElements) {
	PanelProfile panelProfile = new PanelProfile
	    ("Karyo FrAGL View",
	     Config.karyoAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new KaryoDataSetDisplayer(KaryoDataSetDisplayer.FRAGL_TYPE,
				       KaryoDataSetDisplayer.BARPLOT_TYPE),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(MergeArrayOP.FRAGL_NAME),
	     Config.karyoZoomTemplate,
	     new Scale(Utils.pow(0.30), Utils.pow(1.80)),
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	    
	PanelProfile profiles[] = new PanelProfile[]{panelProfile};

	Dimension dim = new Dimension(1000, 700);

	buildView(globalContext, profiles, dim, panel, rGraphElements);
	/*
	  buildView(globalContext, Config.karyoPanelProfiles, Config.karyoDim,
	  panel, rGraphElements);
	*/
    }

    private void buildView(GlobalContext globalContext,
			   PanelProfile panelProfiles[], Dimension dim,
			   GraphPanel panel,
			   Vector rGraphElements) {
	ViewFrame vf = new ViewFrame(globalContext,
				     "FrAGL",
				     panelProfiles,
				     null,
				     null,
				     null, null,
				     new LinkedList(),
				     dim,
				     null);

	LinkedList list = Utils.vectorToList(rGraphElements);
	//	vf.getView().getGraphPanelSet().getPanel(panel != null ? panel.getWhich() : 0).setGraphElements(list);
	vf.getView().getGraphPanelSet().getPanel(0).setGraphElements(list);
	if (task != null)
	    task.performBeforeOPFrameVisible();
	vf.setVisible(true);

	//vf.getView().getGraphPanelSet().getPanel(panel != null ? panel.getWhich() : 0).getCanvas().paste(null, list, false);
	
	vf.getView().syncGraphElements();
    }

    /*
    private void updateProperties(DataElement data,
				  boolean isNA, boolean isOut, boolean isBkp,
				  Object gnl) {
	if (!isNA) {
	    data.setPropertyValue(VAMPProperties.IsNAProp, "False");
	}

	if (!isOut) {
	    if (data.getPropertyValue(VAMPProperties.OutProp) != null)
		data.setPropertyValue(VAMPProperties.OutProp, "0");
	}

	if (!isBkp) {
	    if (data.getPropertyValue(VAMPProperties.BreakpointProp) != null)
		data.setPropertyValue(VAMPProperties.BreakpointProp, "0");
	}

	data.removeProperty(VAMPProperties.SmoothingProp);

	if (gnl == null)
	    data.removeProperty(VAMPProperties.GNLProp);
	else
	    data.setPropertyValue(VAMPProperties.GNLProp, gnl);
    }
    */

    private void updateProperties(RWDataElementProxy data, TreeMap propMap) {
	data.removeAllProperties();
	Iterator it = propMap.entrySet().iterator();
	boolean hasGNL = false;
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    data.setPropertyValue(prop, entry.getValue());
	    if (!hasGNL && prop.equals(VAMPProperties.GNLProp))
		hasGNL = true;
	}

	// EV: added 28/05/08
	if (!hasGNL) {
	    data.removeProperty(VAMPProperties.GNLProp);
	}
	data.removeProperty(VAMPProperties.BreakpointProp);
	data.removeProperty(VAMPProperties.OutProp);
	data.removeProperty(VAMPProperties.SmoothingProp);
    }

    private int getLength() {
	int l = 0;
	if (gainAmpliconAlt)
	    l++;
	if (lostAlt)
	    l++;
	return l;
    }

    public boolean useThread() {
	return true;
    }

    private String [] buildReportList(boolean details) {
	String list[] = new String[details ? 11 : 9];

	String gained;

	if (mergeGainAmpliconAlt)
	    gained = "Gained/Amplicon";
	else if (ampliconAlt)
	    gained = "Amplicon";
	else
	    gained = "Gained";

	int n = 0;
	list[n++] = "Clone";
	list[n++] = "Chr";
	list[n++] = "Support";
	list[n++] = gained;
	list[n++] = gained + " Average";
	list[n++] = gained + " Confidence";
	if (details)
	    list[n++] = gained + " List";
	list[n++] = "Lost";
	list[n++] = "Lost Ratio Average";
	list[n++] = "Lost Confidence";
	if (details)
	    list[n++] = "Lost List";

	return list;
    }

    String getStyle(String s, StandardColorCodes cc) {
	try {
	    double d = Double.parseDouble(s);
	    Color c = cc.getColor(d);
	    return "style='background-color: #" + ColorResourceBuilder.RGBString(c.getRGB()) + "'";
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return "";
	}
    }

    public String getMessage() {
	return "Computing FrAGL...";
    }

    private void maskProperties(RWDataElementProxy elem) {

	elem.maskProperty(VAMPProperties.RatioProp);
	elem.maskProperty(VAMPProperties.IsNAProp);
	elem.maskProperty(VAMPProperties.GNLProp);
	elem.maskProperty(VAMPProperties.BreakpointProp);
	elem.maskProperty(VAMPProperties.FlagProp);
	elem.maskProperty(VAMPProperties.NBPProp);
	elem.maskProperty(VAMPProperties.OutProp);
	elem.maskProperty(VAMPProperties.SmoothingProp);

	// remove properties
	/*
	  NA
	  Gnl
	  Bkp
	  Flag
	  NbP
	  Out
	  Smt
	*/
    }

    public boolean supportProfiles() {
	return true;
    }
}
