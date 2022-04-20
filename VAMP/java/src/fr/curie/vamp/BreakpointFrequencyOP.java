
/*
 *
 * BreakpointFrequencyOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;

class BreakpointFrequencyOP extends GraphElementListOperation {
   
    static int IS_NA = -100000;
    static boolean SUPPORT_REGION_FUSION = true;
    static boolean KEEP_PVALUE = true;

    private static double PVALUE_COEF = 1000000.;
    private static final double MBASE = 1000000.;

    private static int bkp_freq_cnt = 1;
    private static final int DEFAULT_SIZE = 10000;
    private static final double MIN__PVALUE = 0.05;

    static final String NAME = "Breakpoint Frequency";

    static final String SHOW_DENSITY_PARAM = "ShowDensity";

    static final String EXTENDS_NA_PARAM = "ExtendsNA";

    static final String SELECT_PARAM = "Select";

    static final String AVERAGE = "Average";
    static final String PERCENT = "Percentage";
    static final String NUMBER = "Number";

    static final String SELECT_VALUE = "SelectValue";

    static final String ANALYSIS_PARAM = "Analysis";

    static final int NO_ANALYSIS = 0;
    static final int COOC_ANALYSIS = BFAssoAlgorithm.CO_OCCURENCE;
    static final int EXCL_ANALYSIS = BFAssoAlgorithm.EXCLUSION;

    static final String PVALUE_PARAM = "P-Value";
    static final String REGION_FUSION_PARAM = "RegionFusion";
    static final String REGION_FUSION_VALUE = "RegionFusionValue";
    static final String FUSION_PARAM = "Fusion";
    static final String FUSION_VALUE = "FusionValue";

    static final String SHOW_ASSO_PARAM = "ShowAssociations";
    static final String SHOW_BARPLOTS_PARAM = "ShowBarPlots";

    static final String RESULT_PARAM = "Result";

    static final int HTML_REPORT = 0x1;
    static final int CSV_REPORT = 0x2; // ??
    static final int PROFILE_DISPLAY = 0x4;

    static final String VIEW_PARAM = "View";
    static final String CURRENT_VIEW_PARAM = "Current";
    static final String NEW_VIEW_PARAM = "New";

    static final String REPLACE_PARAM = "Replace";

    static final String TRACE_FILE = "TraceFile";

    static Property brkFreqAvgProp = Property.getProperty("Brk Freq Avg");
    static Property selectTProp = Property.getProperty("Select Threshold");
    static Property brkFreqProp = Property.getProperty("Brk Freq");
    static Property arrayProp = Property.getProperty("Arrays");
    static Property arrayURLProp = Property.getHiddenProperty("ArrayURLs");
    static Property densityProp = Property.getProperty("Density");
    static Property normDensityProp = Property.getHiddenProperty("NormDensity");
    static Property freqProp = Property.getProperty("Frequency");
    static Property minDensityProp = Property.getHiddenProperty("MinDensity");
    static Property bkpFreqParamsProp = Property.getHiddenProperty("Bkp Freq Params");
    static Property bkpFreqAssoProp = Property.getProperty("Bkp Freq Asso");
    static Property showAssoProp = Property.getProperty("Show Associations");
    static Property beginProp = Property.getHiddenProperty("Begin");
    static Property endProp = Property.getHiddenProperty("End");
    static Property assoCountProp = Property.getProperty("Association Count");
    static Property extendsNAProp = Property.getProperty("Extends NA");
    static Property pvalueProp = Property.getProperty("P-Value");
    static Property bkpFreqAssoLinkedProp = Property.getHiddenProperty("_brk_asso_");

    static final String TRUE = "true";
    static final String FALSE = "false";

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE,
			    VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.BREAKPOINT_FREQUENCY_TYPE,
			    VAMPConstants.BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE};
    }

public String getReturnedType() {
	return null;
    }

    BreakpointFrequencyOP() {
	super(NAME, SHOW_MENU);
    }

    TreeMap defaultParams;

public TreeMap getParams(View view, Vector graphElements) {
    TreeMap params = getDefaultParams(graphElements);
    if (params != null)
	    return BreakpointFrequencyDialog.getParams
		(view, graphElements, params);

	defaultParams = BreakpointFrequencyDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	return defaultParams;
    }

public TreeMap getDefaultParams(View view, Vector graphElements) {
	if (defaultParams == null) {
	    defaultParams = new TreeMap();

	    defaultParams.put(SELECT_PARAM, AVERAGE);
	    defaultParams.put(SHOW_BARPLOTS_PARAM, TRUE);
	    defaultParams.put(SHOW_ASSO_PARAM, TRUE);
	    defaultParams.put(ANALYSIS_PARAM, new Integer(0));
	    defaultParams.put(RESULT_PARAM, new Integer(PROFILE_DISPLAY));
	    defaultParams.put(VIEW_PARAM, NEW_VIEW_PARAM);
	    defaultParams.put(EXTENDS_NA_PARAM, FALSE);
	    defaultParams.put(PVALUE_PARAM, new Double(MIN__PVALUE));
	    defaultParams.put(FUSION_PARAM, TRUE);
	    defaultParams.put(FUSION_VALUE, new Double(10));
	    defaultParams.put(REGION_FUSION_PARAM, FALSE);
	    defaultParams.put(REGION_FUSION_VALUE, new Double(0));
	}

	return defaultParams;
    }

public TreeMap makeParams(HashMap map) {
	TreeMap params = new TreeMap();
	params.put(SELECT_PARAM, map.get(SELECT_PARAM));
	params.put(SHOW_BARPLOTS_PARAM, map.get(SHOW_BARPLOTS_PARAM));
	params.put(SHOW_ASSO_PARAM, map.get(SHOW_ASSO_PARAM));
	params.put(VIEW_PARAM, map.get(VIEW_PARAM));
	params.put(EXTENDS_NA_PARAM, map.get(EXTENDS_NA_PARAM));
	params.put(REGION_FUSION_PARAM, map.get(REGION_FUSION_PARAM));
	params.put(FUSION_PARAM, map.get(FUSION_PARAM));

	params.put(REPLACE_PARAM, map.get(REPLACE_PARAM));

	params.put(PVALUE_PARAM, Utils.makeDouble(map.get(PVALUE_PARAM)));
	params.put(FUSION_VALUE, Utils.makeDouble(map.get(FUSION_VALUE)));
	if (map.get(REGION_FUSION_VALUE) != null)
	    params.put(REGION_FUSION_VALUE, Utils.makeDouble(map.get(REGION_FUSION_VALUE)));

	params.put(ANALYSIS_PARAM, Utils.makeInteger(map.get(ANALYSIS_PARAM)));
	params.put(RESULT_PARAM, Utils.makeInteger(map.get(RESULT_PARAM)));
	return params;
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {

	int size = graphElements.size();
	if (size == 0)
	    return false;

	String otype = null;
	//int len = 0;
	for (int m = 0; m < size; m++) {
	    DataSet dataSet = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (dataSet == null)
		return false;
	    String type = VAMPUtils.getType(dataSet);
	    if (otype != null && !otype.equals(type))
		return false;
	    otype = type;
	    /*
	    if (len != 0 && len != dataSet.getData().length)
		return false;
	    len = dataSet.getData().length;
	    */
	}

	// only one pseudo profile is allowed
	if (otype.equals(VAMPConstants.BREAKPOINT_FREQUENCY_TYPE) ||
	    otype.equals(VAMPConstants.BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE))
	    return size == 1;

	return true;
    }

public Vector apply(View view, GraphPanel panel,
		 Vector _graphElements, TreeMap params,
		 boolean autoApply) {

	try {
	    Vector graphElements = getGraphElements(_graphElements);

	    graphElements = NormalizeOP.normalize(view.getGlobalContext(), graphElements);

	    int size = graphElements.size();

	    DataSet refDS = ((GraphElement)graphElements.get(0)).asDataSet();
	    if (refDS == null)
		return null;

	    String type = VAMPUtils.getType(refDS);

	    if (type.equals(VAMPConstants.CGH_ARRAY_TYPE))
		type = VAMPConstants.BREAKPOINT_FREQUENCY_TYPE;
	    else if (type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
		type = VAMPConstants.BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE;

	    Object v = params.get(SHOW_DENSITY_PARAM);
	    boolean show_density = v != null && v.equals(TRUE);
	    
	    v = params.get(SHOW_BARPLOTS_PARAM);
	    boolean show_barplots = v != null && v.equals(TRUE);

	    v = params.get(SHOW_ASSO_PARAM);
	    boolean show_asso = v != null & v.equals(TRUE);

	    v = params.get(EXTENDS_NA_PARAM);
	    boolean extends_NA = v != null & v.equals(TRUE);

	    int result = ((Integer)params.get(RESULT_PARAM)).intValue();
	    boolean dspProfile = (result & PROFILE_DISPLAY) != 0;

	    // parameters
	    boolean extended_size = true;

	    HashMap map = compute_map(graphElements, extends_NA);
	    if (map == null)
		return null;
	    DataElement refData[] = refDS.getData();

	    int ival[] = precompute(map, refDS, refData, extended_size);
	    int non_zero = ival[0];
	    int total = ival[1];

	    double dval[] = compute_params(params, non_zero, total, size);
	    double avg = dval[0];
	    double selectT = dval[1];

	    double fusion_region_threshold;
	    String has_region_fusion = (String)params.get(REGION_FUSION_PARAM);
	    if (!has_region_fusion.equals(TRUE))
		fusion_region_threshold = -MBASE;
	    else
		fusion_region_threshold = ((Double)params.get(REGION_FUSION_VALUE)).doubleValue() * MBASE + 1;

	    // region fusion
	    if (SUPPORT_REGION_FUSION) {
		Object o[] =
		    region_fusion_compute(refDS, refData, map, size, selectT,
					  fusion_region_threshold);
		refData = (DataElement[])o[0];
		map = (HashMap)o[1];
	    }

	    DataElement rData[] = new DataElement[refData.length];
	    double max_density = -1000000000;
	    double max_sum = Double.MIN_VALUE;
	    double min_density = Double.MAX_VALUE;
	    double min_sum = Double.MAX_VALUE;
	    DataSet rDataSet = new DataSet();

	    for (int n = 0; n < refData.length; n++) {
		rData[n] = new DataElement();
		rData[n].declare(rDataSet);

		Integer i = new Integer(n);
		Vector frq = (Vector)map.get(i);
		double sum = size(frq, false);
		if (sum != 0) {
		    non_zero++;
		    rData[n].setPropertyValue(arrayProp, makeNames(frq, true));
		    rData[n].setPropertyValue(arrayURLProp, makeURLs(frq));
		}

		rData[n].setPropertyValue(brkFreqProp, (int)sum + "/" + size);
		sum = sum/size;

		if (sum > max_sum)
		    max_sum = sum;
		if (sum != 0 && sum < min_sum)
		    min_sum = sum;

		rData[n].setPropertyValue(freqProp, Utils.toString(sum));
		rData[n].setPosY(rDataSet, sum);
		rData[n].removeProperty(Property.getProperty(VAMPProperties.NA));

		// pos, size, begin & end
		long sz = compute_size(refDS, refData, n, extended_size);
		rData[n].setPosSize(rDataSet, sz);
		rData[n].setPropertyValue(VAMPProperties.SizeProp, Utils.toString(sz));
		rData[n].setPosX(rDataSet, refData[n].getPosX(refDS));

		double posX = Double.parseDouble
		    ((String)refData[n].getPropertyValue(VAMPProperties.PositionProp));

		copyProp(rData[n], refData[n], VAMPProperties.PositionProp);
		setBegin(rData[n], posX);
		setEnd(rData[n], posX + sz);

		// density
		double density = frq != null && sum > 0 ? Math.log(sum/sz) : 0;

		if (frq != null) {
		    if (density > max_density)
			max_density = density;
		    if (density < min_density)
			min_density = density;
		}

		rData[n].setPropertyValue(densityProp, new Double(density));

		// name, nmc, chr & type
		copyProp(rData[n], refData[n], VAMPProperties.NmcProp);
		copyProp(rData[n], refData[n], VAMPProperties.NameProp);
		copyProp(rData[n], refData[n], VAMPProperties.ChromosomeProp);
		VAMPUtils.setType(rData[n], "Breakpoint Barplot");
	    }

	    double normDensity = max_sum/(max_density-min_density);

	    rDataSet.setAutoY(true);
	    copyProp(rDataSet, refDS, VAMPProperties.OrganismProp);
	    copyProp(rDataSet, refDS, VAMPProperties.ChromosomeProp);
	    //copyProp(rDataSet, refDS, VAMPConstants.CloneCountProp);
	    rDataSet.setPropertyValue(VAMPProperties.CloneCountProp,
				      new Integer(refData.length));

	    rDataSet.setPropertyValue(VAMPProperties.NameProp,
				      "Breakpoint Frequency [" +
				      bkp_freq_cnt++ + "]");

	    rDataSet.setPropertyValue(brkFreqAvgProp, new Double(avg));
	    rDataSet.setPropertyValue(ParamsProp, params);
	    rDataSet.setPropertyValue(VAMPProperties.ArraysRefProp, graphElements);

	    int analysis = ((Integer)params.get(ANALYSIS_PARAM)).intValue();
	    Object dataassoc[][][] = null;
	    int cross[] = null;
	    double min_pvalue = ((Double)params.get(PVALUE_PARAM)).doubleValue();
	    double fusion_threshold;
	    String has_fusion = (String)params.get(FUSION_PARAM);
	    if (!has_fusion.equals(TRUE))
		fusion_threshold = -MBASE;
	    else
		fusion_threshold = ((Double)params.get(FUSION_VALUE)).doubleValue() * MBASE + 1;

	    String cnt_asso = "";
	    Association asso[] = null;
	    if (analysis != NO_ANALYSIS) {
		cross = new int[refData.length];
		int alldata[][] = makeAllData(rDataSet, rData, selectT, map, graphElements, cross);
		PrintStream out;
		if (((String)params.get(TRACE_FILE)).trim().length() > 0) {
		    FileOutputStream os = new FileOutputStream
			(((String)params.get(TRACE_FILE)).trim());
		    out = new PrintStream(os);
		    BFAssoAlgorithm.setPrintStream(out);
		}
		else 
		    out = null;


		if (out != null) {
		    for (int jj = 0; jj < alldata.length; jj++) {
			for (int kk = 0; kk < alldata[jj].length; kk++) {
			    if (alldata[jj][kk] == IS_NA)
				out.print("X ");
			    else
				out.print(alldata[jj][kk] + " ");
			}
			out.println(" #" + jj + " " +
				    rData[cross[jj]].getID());
		    }
		}

		dataassoc = BFAssoAlgorithm.algo(analysis, alldata);

		int info[] = new int[1];
		asso = makeAsso(dataassoc, rDataSet, rData, cross,
				min_pvalue, fusion_threshold, info);

		if (out != null)
		    out.close();
		cnt_asso = asso.length + "/" + info[0];
	    }

	    /*
	    for (int jj = 0; jj < rData.length; jj++) {
		if (rData[jj].getPropertyValue(VAMPConstants.LinkedDataProp) != null)
		    System.out.println(rData[jj].getID() + " :: " +
				       ((Vector)rData[jj].getPropertyValue(VAMPConstants.LinkedDataProp)).size());
	    }
	    */

	    rDataSet.setPropertyValue(selectTProp,
				      new Double(selectT));

	    if (analysis != NO_ANALYSIS)
		rDataSet.setPropertyValue(assoCountProp, cnt_asso);

	    rDataSet.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				      VAMPConstants.THR_BRK_FRQ, false);
	    rDataSet.setPropertyValue(arrayProp, makeNames(graphElements, false));
	    rDataSet.setPropertyValue(normDensityProp, new Double(normDensity));
	    rDataSet.setPropertyValue(minDensityProp, new Double(min_density));
	    rDataSet.setPropertyValue(VAMPProperties.VectorArrayProp, graphElements);
	    rDataSet.setData(rData);
	    TreeMap dsp_params = new TreeMap();

	    dsp_params.put(SHOW_ASSO_PARAM, show_asso ? TRUE : FALSE);
	    dsp_params.put(SHOW_DENSITY_PARAM, show_density ? TRUE : FALSE);
	    dsp_params.put(SHOW_BARPLOTS_PARAM, show_barplots ? TRUE : FALSE);

	    rDataSet.setGraphElementDisplayer(new BreakpointFrequencyDataSetDisplayer(dsp_params));
	    rDataSet.setPropertyValue(bkpFreqParamsProp, params);
	    rDataSet.setPropertyValue(VAMPProperties.ArraysRefProp, graphElements);

	    rDataSet.setPropertyValue(showAssoProp, new Boolean(show_asso));
	    rDataSet.setPropertyValue(extendsNAProp, new Boolean(extends_NA));
	    rDataSet.setPropertyValue(pvalueProp, new Double(min_pvalue));

	    VAMPUtils.setType(rDataSet, type);

	    if (dspProfile) {
		if (params.get(VIEW_PARAM).equals(CURRENT_VIEW_PARAM)) {
		    Vector rGraphElements = new Vector();
		    v = params.get(REPLACE_PARAM);
		    if (!v.equals(TRUE))
			rGraphElements.addAll(_graphElements);
		    
		    rGraphElements.add(rDataSet);
		    return undoManage(panel, rGraphElements);
		}

		/*
		GraphPanelSet panelSet = view.getGraphPanelSet();
		ViewFrame vf = new ViewFrame(view.getGlobalContext(),
					     view.getName(),
					     panelSet.getPanelProfiles(),
					     panelSet.getPanelLayout(),
					     panelSet.getPanelLinks(),
					     null, null,
					     new LinkedList(),
					     Config.defaultDim,
					     null);
		*/
		BarplotDataSetDisplayer histo_dsp = new BarplotDataSetDisplayer();
		PanelProfile panelProfile = new PanelProfile
		    ("",
		     Config.defaultAxisSizes,
		     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
		     histo_dsp,
		     (VAMPUtils.isMergeChr(rDataSet) ? Config.defaultChromosomeNameAxisDisplayer :
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

		GlobalContext globalContext = (view != null ? view.getGlobalContext() :
					       (GlobalContext)params.get("GlobalContext"));
		ViewFrame vf = new ViewFrame(globalContext,
					     "Breakpoint Frequency",
					     panelProfiles,
					     null,
					     null,
					     null, null,
					     new LinkedList(),
					     Config.defaultDim,
					     null);
		LinkedList list = new LinkedList();
		list.add(rDataSet);
		vf.getView().getGraphPanelSet().getPanel(panel != null ? panel.getWhich() : 0).setGraphElements(list);
		vf.setVisible(true);
		vf.getView().syncGraphElements();
		return _graphElements;
	    }

	    return buildReport(view, panel, rDataSet, _graphElements, result,
			       asso);
	    //dataassoc, cross, min_pvalue);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    Vector buildReport(View view, GraphPanel panel,
		       DataSet rDataSet, Vector graphElements, int result,
		       Association asso[])
	//double dataassoc[][], int cross[],
	//double min_pvalue)
	throws Exception {
File file = DialogUtils.openFileChooser(new Frame(), "Save",
					DialogUtils.HTML_FILE_FILTER, true);
	if (file == null)
	    return graphElements;

	boolean isHTML = (result == HTML_REPORT);
	String ext = (isHTML ? ".html" : ".csv");

	if (!Utils.hasExtension(file.getName(), ext))
	    file = new File(file.getAbsolutePath() + ext);

	FileOutputStream os = new FileOutputStream(file);
	PrintStream ps = new PrintStream(os);
	
	String title = "Breakpoint Frequency";

	ReportBuilder builder;
	if (isHTML)
	    builder = new HTMLReportBuilder(ps, title);
	else
	    builder = new CSVReportBuilder(ps, title);

	builder.startDocument();
	builder.startCenter();
	builder.addTitle1("Breakpoint Frequency");
	builder.endCenter();
	builder.addHLine();

	DataElement data[] = rDataSet.getData();

	builder.addTitle2("Options");

	TreeMap params = (TreeMap)rDataSet.getPropertyValue(bkpFreqParamsProp);
	builder.addText("Extends NA: " + params.get(EXTENDS_NA_PARAM));
	builder.addVPad(1);

	Object v = params.get(SELECT_PARAM);
	if (v.equals(AVERAGE))
	    builder.addText("Bkp frequency >= average");
	else if (v.equals(PERCENT))
	    builder.addText("Bkp frequency >= " + params.get(SELECT_VALUE));
	else
	    builder.addText("Bkp number >= " + params.get(SELECT_VALUE));

	builder.addVPad(1);

	String has_region_fusion = (String)params.get(REGION_FUSION_PARAM);
	if (!has_region_fusion.equals(TRUE))
	    builder.addText("No region fusion");
	else
	    builder.addText("Region fusion: " + params.get(REGION_FUSION_VALUE));

	int analysis = ((Integer)params.get(ANALYSIS_PARAM)).intValue();

	builder.addVPad(1);

	if (analysis == NO_ANALYSIS)
	    builder.addText("No analysis");
	else if (analysis == COOC_ANALYSIS)
	    builder.addText("Breakpoint co-occurence");
	else if (analysis == EXCL_ANALYSIS)
	    builder.addText("Breakpoint exclusion");

	builder.addVPad(1);
	String has_fusion = (String)params.get(FUSION_PARAM);
	if (!has_fusion.equals(TRUE))
	    builder.addText("No association fusion");
	else
	    builder.addText("Association fusion: " + params.get(FUSION_VALUE));
	builder.addVPad(1);
	builder.addText("P-Value: " + params.get(PVALUE_PARAM));

	builder.addVPad(1);

	String bkpType = (data.length > 0 ?
			  VAMPUtils.getType(data[0]) : "Breakpoint Barplot");

	SystemConfig sysCfg = (SystemConfig)view.getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);

	String bkpColumns[] =
	    sysCfg.getBreakpointFrequencyColumns(bkpType);

	if (bkpColumns == null) {
	    InfoDialog.pop(view.getGlobalContext(),
			   "BreakpointBarplotColumns is not set " +
			   "for type " + bkpType +
			   " in configuration file");
	    return null;
	}

	builder.addTitle2("Breakpoints");
	builder.startTable(bkpColumns);

	for (int i = 0; i < data.length; i++) {
	    if (data[i].getPosY(rDataSet) == 0)
		continue;
	    builder.startRow();
	    for (int n = 0; n < bkpColumns.length; n++) {
		Object value = data[i].getPropertyValue
		    (Property.getProperty(bkpColumns[n]));

		if (value != null) {
		    if (Utils.checkDouble(value))
			builder.addCell(Utils.toString(value));
		    else if (value instanceof String)
			builder.addCell(builder.replaceNL((String)value));
		    else
			builder.addCell(value.toString());
		}
	    }
	    builder.endRow();
	}

	builder.endTable();

	if (asso != null) {
	    String bkpAssoColumns[] =
		sysCfg.getBreakpointFrequencyColumns("Breakpoint Barplot Association");

	    if (bkpAssoColumns == null) {
		InfoDialog.pop(view.getGlobalContext(),
			       "BreakpointColumns is not set " +
			       "for type Breakpoint Barplot Association " + 
			       " in configuration file");
		return null;
	    }

	    builder.addTitle2("Breakpoint Associations");
	    builder.startTable(bkpAssoColumns);

	    for (int i = 0; i < data.length; i++) {
		if (data[i].getPropertyValue(bkpFreqAssoProp) == null)
		    continue;
		builder.startRow();
		for (int n = 0; n < bkpAssoColumns.length; n++) {
		    Object value = data[i].getPropertyValue
			(Property.getProperty(bkpAssoColumns[n]));
		    
		    if (value != null) {
			if (Utils.checkDouble(value))
			    builder.addCell(Utils.toString(value));
			else if (value instanceof String)
			    builder.addCell(builder.replaceSP(builder.replaceNL((String)value)));
			else
			    builder.addCell(value.toString());
		    }
		}
		builder.endRow();
	    }

	    builder.endTable();

	    bkpAssoColumns =
		sysCfg.getBreakpointFrequencyColumns("Breakpoint Association");
	    if (bkpAssoColumns == null) {
		InfoDialog.pop(view.getGlobalContext(),
			       "BreakpointColumns is not set " +
			       "for type Breakpoint Association " + 
			       " in configuration file");
		return null;
	    }

	    builder.addVPad(2);
	    builder.addTitle2("Associations (" + rDataSet.getPropertyValue(assoCountProp) + ")");
	    builder.startTable(bkpAssoColumns);
	    
	    for (int n = 0; n < asso.length; n++) {
		Association as = asso[n];
		builder.startRow();
		for (int c = 0; c < bkpAssoColumns.length; c++) {
		    String col = bkpAssoColumns[c];
		    builder.addCell(as.getValue(col));
		}
		builder.endRow();
	    }

	    builder.endTable();
	}	

	builder.endDocument();

	ps.close();
	return graphElements;
    }

    static void copyProp(PropertyElement dest, PropertyElement src,
			 Property prop) {
	dest.setPropertyValue(prop, src.getPropertyValue(prop));
    }

    static final String sep = "------------\n";

    static String makeNames(Vector frq, boolean pure) {
	int size = frq.size();
	
	Vector name_v = SUPPORT_REGION_FUSION ? new Vector() : null;

	String names = sep;
	for (int n = 0, m = 0; n < size; n++) {
	    if (frq.get(n) == null)
		continue;

	    if (pure && SUPPORT_REGION_FUSION && size((Vector)frq.get(n), true) == 0)
		continue;

	    if (m != 0)
		names += "\n";

	    if (pure && SUPPORT_REGION_FUSION) {
		Vector v = (Vector)frq.get(n);
		for (int j = 0; j < v.size(); j++) {
		    if (v.get(j) != null) {
			Object name = ((GraphElement)v.get(j)).getID();
			if (!name_v.contains(name))
			    name_v.add(name);
		    }
		}
	    }
	    else
		names += ((GraphElement)frq.get(n)).getID();
	    m++;
	}

	if (SUPPORT_REGION_FUSION) {
	    names = sep;
	    for (int n = 0; n < name_v.size(); n++)
		names += (n > 0 ? "\n" : "") + name_v.get(n);
	}
	return names + "\n" + sep;
    }

    static String[] makeURLs(Vector frq) {

	int size = 2*frq.size();
	String URLs[] = SUPPORT_REGION_FUSION ? null : new String[size];
	Vector URL_v =  SUPPORT_REGION_FUSION ? new Vector() : null;
	
	for (int n = 0; n < size; n += 2) {
	    if (frq.get(n/2) == null)
		continue;

	    if (SUPPORT_REGION_FUSION) {
		Vector v = (Vector)frq.get(n/2);
		for (int j = 0; j < v.size(); j++) {
		    if (v.get(j) != null) {
			DataSet dataSet = ((GraphElement)v.get(j)).asDataSet();
			if (dataSet != null && !URL_v.contains(dataSet.getSourceURL())) {
			    URL_v.add((VAMPUtils.isMergeChr(dataSet) ? "CGH_CHRMERGE" : ""));
			    URL_v.add(dataSet.getSourceURL());
			}
		    }
		}
	    }
	    else {
		DataSet dataSet = ((GraphElement)frq.get(n/2)).asDataSet();
		URLs[n] = (VAMPUtils.isMergeChr(dataSet) ? "CGH_CHRMERGE" : "");
		URLs[n+1] = dataSet.getSourceURL();
	    }
	}

	if (URL_v != null) {
	    URLs = new String[URL_v.size()];
	    for (int n = 0; n < URL_v.size(); n++)
		URLs[n] = (String)URL_v.get(n);
	}

	return URLs;
    }

    static Vector getGraphElements(Vector graphElements) {
	GraphElement ref = (GraphElement)graphElements.get(0);

	String type = VAMPUtils.getType(ref);
	if (type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_TYPE) ||
	    type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE))
	    return (Vector)ref.getPropertyValue(VAMPProperties.VectorArrayProp);

	return graphElements;
    }

public TreeMap getDefaultParams(Vector graphElements) {
	if (graphElements.size() != 1)
	    return null;

	GraphElement graphElement = (GraphElement)graphElements.get(0);
	String type = VAMPUtils.getType(graphElement);

	if (type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_TYPE) ||
	    type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE))
	    return (TreeMap)graphElement.getPropertyValue(bkpFreqParamsProp);
	return null;
    }

    void setInds(int alldata[], Vector frq, Vector graphElements) {
	if (frq == null)
	    return;

	int size = graphElements.size();
	int sz = frq.size();
	for (int m = 0; m < sz; m++) {
	    Object o = frq.get(m);
	    if (o == null)
		continue;
	    if (SUPPORT_REGION_FUSION && ((Vector)o).size() == 0)
		continue;

	    if (SUPPORT_REGION_FUSION) {
		Vector v = (Vector)o;
		for (int j = 0; j < v.size(); j++) {
		    GraphElement g = (GraphElement)v.get(j);
		    for (int n = 0; n < size; n++) {
			if (g == graphElements.get(n)) {
			    alldata[n] = 1;
			    break;
			}
 		    }
		}
	    }
	    else {
		boolean found = false;
		for (int n = 0; n < size; n++) {
		    if (o == graphElements.get(n)) {
			alldata[n] = 1;
			found = true;
			break;
		    }
		}

		if (!found)
		    System.out.println("OUPS: not found");
	    }
	}
    }

    int [][] makeAllData(DataSet rDataSet,
			 DataElement rData[], double selectT, HashMap map,
			 Vector graphElements, int cross[]) {

	int size = graphElements.size();
	Vector v = new Vector();
	for (int n = 0, m = 0; n < rData.length; n++) {
	    if (rData[n].getPosY(rDataSet) >= selectT) {
		Integer i = new Integer(n);
		int data[] = new int[size];
		// 18/07/2005
		for (int j = 0; j < size; j++) {
		    DataSet dataSet = (DataSet)graphElements.get(j);
		    if (VAMPUtils.isNA(dataSet.getData()[n]))
			data[j] = IS_NA;
		}
		//
		setInds(data, (Vector)map.get(i), graphElements);
		v.add(data);
		cross[m] = n;
		m++;
	    }
	}

	int sz = v.size();
	if (sz == 0) {
	    System.out.println("oups: " + sz);
	    sz = 1;
	}

	int alldata[][] = new int[sz][size];
	for (int n = 0; n < sz; n++)
	    alldata[n] = (n < v.size() ? (int[])v.get(n) : new int[0]);
	
	/*
	for (int n = 0; n < alldata.length; n++)
	    for (int m = 0; m < alldata[0].length; m++)
		if (alldata[n][m] != 0)
		    System.out.println("alldata[" + n + "][" + m + "] = " +
				       alldata[n][m]);
	*/

	return alldata;
    }

    static Vector initLinkedDataProp(DataElement d) {
	Vector v = (Vector)d.getPropertyValue(bkpFreqAssoLinkedProp);
	if (v == null) {
	    v = new Vector();
	    d.setPropertyValue(bkpFreqAssoLinkedProp, v);
	}
	return v;
    }

    static long getBegin(DataElement data) {
	return ((Long)data.getPropertyValue(beginProp)).longValue();
    }

    static long getEnd(DataElement data) {
	return ((Long)data.getPropertyValue(endProp)).longValue();
    }

    static void setBegin(DataElement data, double begin) {
	data.setPropertyValue(beginProp, new Long((long)begin));
    }

    static void setEnd(DataElement data, double end) {
	data.setPropertyValue(endProp, new Long((long)end));
    }

    static class Association {
	DataElement begin1, end1;
	DataElement begin2, end2;
	Rectangle2D.Double brakets[];
	Rectangle2D.Double middles[];
	DataSet rDataSet;
	double posy1, posy2;

	DataElement data1[];
	DataElement data2[];

	double pvalue;
	double pvalues[];
	Vector pvalue_v;

	Vector alltab_v;

	private boolean drawn;

	Association(DataSet rDataSet, Vector data1_v, Vector data2_v, Vector pvalue_v,
		    Vector alltab_v) {
	    init(rDataSet, data1_v, data2_v, 1., pvalue_v, alltab_v);
	}

	Association(DataSet rDataSet, Vector data1_v, Vector data2_v, double pvalue) {
	    init(rDataSet, data1_v, data2_v, pvalue, null, null);
	}

	void merge(Association asso) {
	    Vector data1_v = new Vector();
	    Vector data2_v = new Vector();

	    if (pvalue_v != null)
		pvalue_v.addAll(asso.pvalue_v);

	    if (alltab_v != null)
		alltab_v.addAll(asso.alltab_v);

	    for (int n = 0; n < data1.length; n++)
		data1_v.add(data1[n]);

	    for (int n = 0; n < asso.data1.length; n++)
		if (KEEP_PVALUE || !data1_v.contains(asso.data1[n]))
		    data1_v.add(asso.data1[n]);

	    for (int n = 0; n < data2.length; n++)
		data2_v.add(data2[n]);

	    for (int n = 0; n < asso.data2.length; n++)
		if (KEEP_PVALUE || !data2_v.contains(asso.data2[n]))
		    data2_v.add(asso.data2[n]);

	    if (KEEP_PVALUE && data1_v.size() != pvalue_v.size()) {
		System.out.println("erreur sizes: " +
				   data1_v.size() + " vs. " +
				   pvalue_v.size());
	    }

	    if (KEEP_PVALUE && data1_v.size() != alltab_v.size()) {
		System.out.println("erreur sizes2: " +
				   data1_v.size() + " vs. " +
				   alltab_v.size());
	    }

	    init(rDataSet, data1_v, data2_v, pvalue < asso.pvalue ? pvalue : asso.pvalue,
		 pvalue_v, alltab_v);
	}

	void init(DataSet rDataSet, Vector data1_v, Vector data2_v, double pvalue,
		  Vector pvalue_v, Vector alltab_v) {

	    this.rDataSet = rDataSet;
	    this.pvalue_v = pvalue_v;
	    this.alltab_v = alltab_v;

	    if (pvalue_v != null) {
		pvalue = 1.;
		pvalues = new double[pvalue_v.size()];
		for (int n = 0; n < pvalues.length; n++) {
		    double pv = ((Integer)pvalue_v.get(n)).intValue()/PVALUE_COEF;
		    pvalues[n] = pv;
		    if (pv < pvalue)
			pvalue = pv;
		}
	    }

	    this.pvalue = pvalue;

	    int data1_sz = data1_v.size();
	    int data2_sz = data2_v.size();
	    if (data1_sz != data2_sz)
		System.out.println("sizes differ: " + data1_sz + ", " +
				   data2_sz);
	    data1 = new DataElement[data1_sz];
	    data2 = new DataElement[data2_sz];

	    long v_begin1, v_end1;
	    long v_begin2, v_end2;

	    v_begin1 = v_begin2 = Long.MAX_VALUE;
	    v_end1 = v_end2 = Long.MIN_VALUE;
	    posy1 = posy2 = - Double.MAX_VALUE;

	    for (int j = 0; j < data1_sz; j++) {
		DataElement d = (DataElement)data1_v.get(j);
		if (getBegin(d) < v_begin1) {
		    v_begin1 = getBegin(d);
		    begin1 = d;
		}
		if (getEnd(d) > v_end1) {
		    v_end1 = getEnd(d);
		    end1 = d;
		}
		if (d.getPosY(rDataSet) > posy1)
		    posy1 = d.getPosY(rDataSet);

		data1[j] = d;
	    }

	    for (int j = 0; j < data2_sz; j++) {
		DataElement d = (DataElement)data2_v.get(j);
		if (getBegin(d) < v_begin2) {
		    v_begin2 = getBegin(d);
		    begin2 = d;
		}
		if (getEnd(d) > v_end2) {
		    v_end2 = getEnd(d);
		    end2 = d;
		}
		if (d.getPosY(rDataSet) > posy2)
		    posy2 = d.getPosY(rDataSet);

		data2[j] = d;
	    }

	    // 6/09/05
	    // trouver un slot de libre (dans Break...DataSetDisplayer ?)
	    /*
	      while (posy1 in [begin1, end1] is busy)
	      posy1 += inc;
	      // idem posy2
	    /*
	    if (posy1 == posy2)
		posy2 += 0.01;
	    */

	    //initLinkedDataProp(begin1).add(this);
	}

	void epilogue() {
	    for (int j = 0; j < data1.length; j++) {
		addProps(data1[j], data2[j],
			 pvalues != null ? pvalues[j] : 0,
			 alltab_v.get(j));
	    }

	    for (int j = 0; j < data2.length; j++) {
		addProps(data2[j], data1[j],
			 pvalues != null ? pvalues[j] : 0,
			 alltab_v.get(j));
	    }
	}

	String getValue(String name) {
	    if (name.equals("Begin1"))
		return Utils.toString(getBegin(begin1));

	    if (name.equals("End1"))
		return Utils.toString(getEnd(end1));

	    if (name.equals("Begin2"))
		return Utils.toString(getBegin(begin2));

	    if (name.equals("End2"))
		return Utils.toString(getEnd(end2));

	    if (name.equals("Chr1"))
		return Utils.toString(VAMPUtils.getChr(begin1));

	    if (name.equals("Chr2"))
		return Utils.toString(VAMPUtils.getChr(begin2));

	    if (name.equals("P-Value"))
		return Utils.toString(pvalue);

	    if (name.equals("Data1")) {
		String s = "";
		Vector data1_v = new Vector();
		for (int j = 0; j < data1.length; j++) {
		    if (!data1_v.contains(data1[j].getID()))
			data1_v.add(data1[j].getID());
		}

		for (int j = 0; j < data1_v.size(); j++) {
		    s += (j > 0 ? " : " : "") + data1_v.get(j);
		}
		return s;
	    }

	    if (name.equals("Data2")) {
		String s = "";
		Vector data2_v = new Vector();
		for (int j = 0; j < data2.length; j++) {
		    if (!data2_v.contains(data2[j].getID()))
			data2_v.add(data2[j].getID());
		}

		for (int j = 0; j < data2_v.size(); j++)
		    s += (j > 0 ? " : " : "") + data2_v.get(j);
		return s;
	    }

	    if (name.equals("Association"))
		return toString();

	    return "";
	}

	boolean isDrawn() {
	    return drawn;
	}

	void setDrawn(boolean drawn) {
	    this.drawn = drawn;
	}

	private void addProps(DataElement data1, DataElement data2,
			      double xpvalue, Object xall_tab) {
	    initLinkedDataProp(data1).add(this);

	    if (!KEEP_PVALUE) {
		String s = (String)data1.getPropertyValue(bkpFreqAssoProp);
		if (s != null)
		    s += "\n";
		else
		    s = "";
		s += toString();
		data1.setPropertyValue(bkpFreqAssoProp, s);
	    }
	    else {
		String s = (String)data1.getPropertyValue(bkpFreqAssoProp);
		if (s != null)
		    s += "\n";
		else
		    s = "";

		s += data1.getID() + " <> " + data2.getID();
		int all_tab[][] = (int[][])xall_tab;

		s += "\n  [" +
		    all_tab[0][0] + "," +
		    all_tab[0][1] + "," +
		    all_tab[1][0] + "," +
		    all_tab[1][1] + "]";

		s += "\n  p-value=" + Utils.performRound(xpvalue, 4);

		data1.setPropertyValue(bkpFreqAssoProp, s);
	    }
	}

	public String toString() {

	    String s = "";
	    for (int j = 0; j < data1.length; j++)
		s += (j > 0 ? " : " : "") + data1[j].getID();
	    s += " <> ";
	    for (int j = 0; j < data2.length; j++)
		s += (j > 0 ? " : " : "") + data2[j].getID();

	    return s;
	}
    }

    Association[] makeAsso(Object dataassoc[][][], DataSet rDataSet,
			   DataElement rData[],
			   int cross[], double min_pvalue,
			   double fusion_threshold, int info[]) {

	Vector fv = new Vector();
	Vector tabv = new Vector();
	for (int n = 0; n < dataassoc.length; n++) {
	    for (int m = n+1; m < dataassoc[n].length; m++) {
		double pvalue = ((Double)dataassoc[n][m][0]).doubleValue();
		if (pvalue <= min_pvalue) {
		    fv.add(new int[]{cross[n], cross[m],
				     (int)(pvalue * PVALUE_COEF)});
		    int all_tab[][] = (int[][])dataassoc[n][m][1];
		    tabv.add(all_tab);
		    /*
		    System.out.println("all_tab.length " + all_tab.length);
		    for (int k = 0; k < all_tab.length; k++)
			System.out.println("all_tab[" + k + "] = " +
					   all_tab[k][0][0] + ", " +
					   all_tab[k][0][1] + ", " +
					   all_tab[k][1][0] + ", " +
					   all_tab[k][1][1]);
		    */
		}
	    }
	}
	
	int fv_sz = fv.size();
	info[0] = fv_sz;
	if (fv_sz == 0)
	    return new Association[0];

	int ind[] = (int[])fv.get(0);
	int lastind[] = ind;

	int all_tab[][] = (int[][])tabv.get(0);
	int lastall_tab[][] = all_tab;

	DataElement data1_1 = rData[ind[0]];
	DataElement data1_2 = rData[ind[1]];
	long begin1_1 = getBegin(data1_1);
	long end1_1 = getEnd(data1_1);
	long begin1_2 = getBegin(data1_2);
	long end1_2 = getEnd(data1_2);
	String chr1_1 = VAMPUtils.getChr(data1_1);
	String chr1_2 = VAMPUtils.getChr(data1_2);
	int cnt = 0;
	Vector last_fusion = null;
	Vector fusion_v = new Vector();

	for (int n = 1; n < fv_sz; n++) {
	    ind = (int[])fv.get(n);
	    all_tab = (int[][])tabv.get(n);

	    DataElement data2_1 = rData[ind[0]];
	    DataElement data2_2 = rData[ind[1]];
	    long begin2_1 = getBegin(data2_1);
	    long end2_1 = getEnd(data2_1);
	    long begin2_2 = getBegin(data2_2);
	    long end2_2 = getEnd(data2_2);
	    String chr2_1 = VAMPUtils.getChr(data2_1);
	    String chr2_2 = VAMPUtils.getChr(data2_2);

	    /*	    System.out.println("begin1_1: " + begin1_1);
	    System.out.println("end1_1: " + end1_1);
	    System.out.println("chr1_1: " + chr1_1);
	    System.out.println("");
	    System.out.println("begin1_2: " + begin1_2);
	    System.out.println("end1_2: " + end1_2);
	    System.out.println("chr1_2: " + chr1_2);
	    System.out.println("");
	    System.out.println("begin2_1: " + begin2_1);
	    System.out.println("end2_1: " + end2_1);
	    System.out.println("chr2_1: " + chr2_1);
	    System.out.println("");
	    System.out.println("begin2_2: " + begin2_2);
	    System.out.println("end2_2: " + end2_2);
	    System.out.println("chr2_2: " + chr2_2);
	    System.out.println("");
	    */

	    if (chr1_1.equals(chr2_1) && chr1_2.equals(chr2_2) &&
		(ind[0] == lastind[0] ||
		 Math.abs(begin2_1 - end1_1) < fusion_threshold) &&
		(ind[1] == lastind[1] ||
		 Math.abs(begin2_2 - end1_2) < fusion_threshold ||
		 Math.abs(begin1_2 - end2_2) < fusion_threshold)) {
		//System.out.println("fusion " + lastind[0] + ":" + ind[0] + " <-> " + lastind[1] + ":" + ind[1]);

		/*
		System.out.println("fusion " + lastind[0] + ":" + ind[0] + " <-> " + lastind[1] + ":" + ind[1] + " " +
				   data1_1.getID() + ":" + data2_1.getID() + " <-> " +
				   data1_2.getID() + ":" + data2_2.getID() +
				   " " +
				   begin1_1 + "," +
				   end1_1 + "," +
				   begin2_1 + "," +
				   end2_1 + " :: " +
				   begin1_2 + "," +
				   end1_2 + "," +
				   begin2_2 + "," +
				   end2_2);
		*/

		if (last_fusion == null) {	
		    last_fusion = new Vector();
		    if (KEEP_PVALUE) {
			last_fusion.add(new Object[]
			    {new Integer(lastind[0]),
			     new Integer(lastind[1]),
			     new Integer(lastind[2]),
			     lastall_tab,
			     new Integer(ind[0]),
			     new Integer(ind[1]),
			     new Integer(ind[2]),
			     all_tab});
		    }
		    else {
			last_fusion.add(new Object[]
			    {new Integer(lastind[0]),
			     new Integer(ind[0]),
			     new Integer(lastind[1]),
			     new Integer(ind[1]),
			     new Integer(lastind[2] < ind[2] ? lastind[2] : ind[2])});
		    }
		      
		    fusion_v.add(last_fusion);
		}
		else {
		    last_fusion.add(new Object[]{
			new Integer(ind[0]),
			new Integer(ind[1]),
			new Integer(ind[2]),
			all_tab});
		}
				
		cnt++;

		//begin1_1 = begin1_1;
		end1_1 = end2_1;
		if (begin1_2 < begin2_2) {
		    //begin1_2 = begin1_2;
		    end1_2 = end2_2;
		}
		else {
		    begin1_2 = begin2_2;
		    //end1_2 = end1_2;
		}
	    }
	    else {
		//System.out.println("nofusion");
		if (last_fusion == null) {
		    Vector v = new Vector();
		    v.add(new Object[]{
			new Integer(lastind[0]),
			new Integer(lastind[1]),
			new Integer(lastind[2]),
			lastall_tab});
		    fusion_v.add(v);
		}
		last_fusion = null;

		begin1_1 = begin2_1;
		end1_1 = end2_1;
		begin1_2 = begin2_2;
		end1_2 = end2_2;
		chr1_1 = chr2_1;
		chr1_2 = chr2_2;
	    }

	    lastind = ind;
	    lastall_tab = all_tab;
	}

	if (last_fusion == null) {
	    Vector v = new Vector();
	    v.add(new Object[]{
		new Integer(lastind[0]),
		new Integer(lastind[1]),
		new Integer(lastind[2]),
		lastall_tab});
	    fusion_v.add(v);
	}

	return makeAsso_p(fusion_v, rDataSet, rData, fusion_threshold, fv_sz);
    }

    static Association[] makeAsso_p(Vector fusion_v, DataSet rDataSet,
				    DataElement rData[],
				    double fusion_threshold, int fv_sz) {

	int fusion_sz = fusion_v.size();
	Association asso[] = new Association[fusion_sz];
	boolean has_fusion = fusion_threshold > 0;

	for (int n = 0; n < fusion_sz; n++) {
	    Vector data1_v = new Vector();
	    Vector data2_v = new Vector();
	    Vector pvalue_v = new Vector();
	    Vector alltab_v = new Vector();
	    Vector fusion = (Vector)fusion_v.get(n);

	    Object val[] = (Object[])fusion.get(0);
	    if (val.length == 8) {
		add(data1_v, rData[((Integer)val[0]).intValue()]);
		add(data2_v, rData[((Integer)val[1]).intValue()]);
		pvalue_v.add(val[2]);
		alltab_v.add(val[3]);
		add(data1_v, rData[((Integer)val[4]).intValue()]);
		add(data2_v, rData[((Integer)val[5]).intValue()]);
		pvalue_v.add(val[6]);
		alltab_v.add(val[7]);
	    }
	    else if (val.length == 4) {
		add(data1_v, rData[((Integer)val[0]).intValue()]);
		add(data2_v, rData[((Integer)val[1]).intValue()]);
		pvalue_v.add(val[2]);
		alltab_v.add(val[3]);
	    }
	    else {
		System.out.println("FATAL ERROR !!!!");
		System.exit(1);
	    }

	    int size = fusion.size();
	    for (int j = 1; j < size; j++) {
		Object xval[] = (Object[])fusion.get(j);
		add(data1_v, rData[((Integer)xval[0]).intValue()]);
		add(data2_v, rData[((Integer)xval[1]).intValue()]);
		pvalue_v.add(xval[2]);
		alltab_v.add(xval[3]);
	    }
	    
	    if (KEEP_PVALUE)
		asso[n] = new Association(rDataSet, data1_v, data2_v, pvalue_v,
					  alltab_v);
	    /*
	    else
		asso[n] = new Association(data1_v, data2_v,
					  (double)min_pvalue/PVALUE_COEF);
	    */
	    if (!has_fusion)
		asso[n].epilogue();
	}

	if (!has_fusion)
	    return asso;

	int post_fusion_cnt = 0;
	do {
	    post_fusion_cnt = 0;
	    for (int n = 0; n < asso.length; n++) {
		if (asso[n] == null)
		    continue;
		for (int m = n+1; m < asso.length; m++) {
		    if (asso[m] == null)
			continue;
		
		    if (!VAMPUtils.getChr(asso[n].begin1).equals(VAMPUtils.getChr(asso[m].end1)))
			break;
		
		    if (Math.abs(getBegin(asso[n].begin1) - getEnd(asso[m].end1)) < fusion_threshold &&
			VAMPUtils.getChr(asso[n].begin2).equals(VAMPUtils.getChr(asso[m].end2)) &&
			Math.abs(getBegin(asso[n].begin2) - getEnd(asso[m].end2)) < fusion_threshold) {
			asso[n].merge(asso[m]);
			asso[m] = null;
			post_fusion_cnt++;
		    }
		}
	    }
	} while (post_fusion_cnt != 0);

	int asso_cnt = 0;
	for (int n = 0; n < asso.length; n++) {
	    if (asso[n] != null)
		asso_cnt++;
	}

	Association asso_n[] = new Association[asso_cnt];
	for (int n = 0, m = 0; n < asso.length; n++) {
	    if (asso[n] != null) {
		asso_n[m] = asso[n];
		asso_n[m].epilogue();
		m++;
	    }
	}

	System.out.println("after post fusion: " + asso_cnt + "/" +
			   asso.length + "/" + fv_sz);
	return asso_n;
    }

    static void add(Vector v, DataElement data) {
	if (KEEP_PVALUE || !v.contains(data))
	    v.add(data);
    }

    static int size(Vector frq, boolean pre) {
	int sz = 0;
	int size = frq.size();
	for (int n = 0; n < size; n++) {
	    if (!pre && SUPPORT_REGION_FUSION) {
		if (size((Vector)frq.get(n), true) > 0)
		    sz++;
	    }
	    else if (frq.get(n) != null)
		sz++;
	}
	return sz;
    }


    HashMap compute_map(Vector graphElements, boolean extends_NA) {
	// map<Integer, Vector<DataSet> > map;
	HashMap map = new HashMap();

	int size = graphElements.size();

	for (int m = 0; m < size; m++) {
	    DataSet dataSet = ((GraphElement)graphElements.get(m)).
		asDataSet();

	    if (dataSet == null)
		return null;

	    DataElement data[] = dataSet.getData();
	    int lastn = -2;
	    for (int n = 0; n < data.length; n++) {
		Integer i = new Integer(n);
		Vector frq = (Vector)map.get(i);
		if (frq == null)
		    frq = new Vector();

		if (VAMPUtils.isBreakpoint(data[n]) ||
		    (extends_NA && VAMPUtils.isNA(data[n]) && n-lastn == 1)) {
		    frq.add(dataSet);
		    lastn = n;
		}
		else
		    frq.add(null);
		
		map.put(i, frq);
	    }
	}
	return map;
    }

    int[] precompute(HashMap map, DataSet refDS, DataElement refData[],
		     boolean extended_size) {

	int non_zero = 0;
	int total = 0;
	for (int n = 0; n < refData.length; n++) {
	    Integer i = new Integer(n);
	    Vector frq = (Vector)map.get(i);
	    int sum = size(frq, true);
	    if (sum != 0)
		non_zero++;

	    total += sum;

	    long sz = compute_size(refDS, refData, n, extended_size);
	    
	    double posX = Double.parseDouble
		((String)refData[n].getPropertyValue(VAMPProperties.PositionProp));
	    
	    setBegin(refData[n], posX);
	    setEnd(refData[n], posX + sz);
	}

	return new int[]{non_zero, total};
    }

    long compute_size(DataSet refDS, DataElement refData[], int n, boolean extended_size) {

	long sz;

	if (extended_size && n < refData.length-1 &&
	    VAMPUtils.getChr(refData[n+1]).equals(VAMPUtils.getChr(refData[n]))) {
	    sz = (long)(refData[n+1].getPosX(refDS) - refData[n].getPosX(refDS));
	    if (sz == 0) 
		sz = (long)refData[n].getPosSize(refDS);
	}
	else
	    sz = (long)refData[n].getPosSize(refDS);
	    
	if (sz == 0)
	    sz = DEFAULT_SIZE;

	return sz;
    }

    double[] compute_params(TreeMap params, int non_zero, int total,
			    int size) {
	Object v = params.get(SELECT_PARAM);
	    
	double avg = (double)total/(double)(non_zero*size);
	double selectT = 0.;

	if (v != null) {
	    if (v.equals(AVERAGE))
		selectT = avg;
	    else if (v.equals(PERCENT))
		selectT = Double.parseDouble
		    ((String)params.get(SELECT_VALUE))/100.;
	    else if (v.equals(NUMBER))
		selectT = (double)
		    (((Integer)params.get(SELECT_VALUE)).intValue()) /
		    size;
	}
	else
	    selectT = avg;

	return new double[] {avg, selectT};
    }

    Object[] region_fusion_compute(DataSet refDS, DataElement data[],
				   HashMap map,
				   int size, double selectT, double threshold) {
    
	Vector data_v = new Vector();

	DataElement lastSelectData = null;
	int lastSelect = 0;

	int good = -1;
	for (int n = 0; n < data.length; n++) {
	    Integer i = new Integer(n);
	    Vector frq = (Vector)map.get(i);
	    double sum = (double)size(frq, true)/size;

	    boolean last = false;
	    if (sum < selectT) {
		if (n != data.length - 1)
		    continue;
		last = true;
	    }
	    
	    boolean r = false;
	    if (!last)
		r = region_fusioned(lastSelectData, data[n], threshold);
	    
	    if (r) {
		if (good < 0)
		    good = lastSelect;
	    }

	    if (!r || last) {
		if (good >= 0) {
		    Vector group = new Vector();
		    for (int j = good; j <= lastSelect; j++)
			group.add(new Integer(j));
		    data_v.add(group);
		    good = -1;
		    ++lastSelect;
		}

		int end = n + (last ? 1 : 0);
		for (int j = lastSelect; j < end; j++) {
		    Vector group = new Vector();
		    group.add(new Integer(j));
		    data_v.add(group);
		}
	    }

	    lastSelectData = data[n];
	    lastSelect = n;
	}

	/*
	System.out.println("data_v.size() " + data_v.size());
	int cnt = 0;
	for (int n = 0; n < data_v.size(); n++) {
	    Vector v = (Vector)data_v.get(n);
	    System.out.println("cluster #" + n + ": ");

	    for (int j = 0; j < v.size(); j++) {
		Integer i = (Integer)v.get(j);
		System.out.print("\t");
		Vector frq = (Vector)map.get(i);
		for (int k = 0; k < frq.size(); k++) {
		    if (frq.get(k) != null)
			System.out.print("1 ");
		    else
			System.out.print("0 ");
		}
		System.out.println(data[i.intValue()].getID());
	    }
	    cnt += v.size();
	}

	System.out.println("cnt: " + cnt);
	*/

	DataElement rdata[] = new DataElement[data_v.size()];
	HashMap nmap = new HashMap();

	int cnt = 0;
	for (int n = 0; n < rdata.length; n++) {
	    Vector v = (Vector)data_v.get(n);

	    String name = "";
	    Vector nfrq = new Vector();
	    for (int k = 0; k < size; k++)
		nfrq.add(new Vector());

	    int m = 0;
	    for (int j = 0; j < v.size(); j++) {
		Integer i = (Integer)v.get(j);
		DataElement ndata = data[i.intValue()];

		try {
		    if (j == 0) {
			rdata[n] = (DataElement)ndata.clone();
			rdata[n].copyPos(refDS, ndata, refDS);
			rdata[n].setSharedElem(null);
		    }
		} catch(Exception e) {
		}

		Vector frq = (Vector)map.get(i);

		if (size(frq, true) > 0 || v.size() == 1) {
		    if (name.length() > 0)
			name += "|";
		    name += ndata.getPropertyValue(VAMPProperties.NameProp);
		    m++;
		}

		for (int k = 0; k < frq.size(); k++) {
		    ((Vector)nfrq.get(k)).add(frq.get(k));
		}
	    }

	    if (v.size() > 1) {
		if (m == 1) {
		    System.out.println("strange !");
		    for (int j = 0; j < v.size(); j++) {
			Integer i = (Integer)v.get(j);
			DataElement ndata = data[i.intValue()];
			System.out.println(ndata.getID() + ": " +
					   ndata.getPosY(refDS)); // not shure
		    }
		}
		name = "[" + name + "]";
	    }
	    rdata[n].setPropertyValue(VAMPProperties.NameProp, name);
	    nmap.put(new Integer(n), nfrq);
	    cnt += v.size();
	}

	return new Object[]{rdata, nmap};
    }

    private boolean region_fusioned(DataElement lastSelectData,
				    DataElement curData,
				    double threshold) {

	return (lastSelectData != null &&
		getBegin(curData) - getEnd(lastSelectData) <= threshold &&
		VAMPUtils.getChr(curData).equals(VAMPUtils.getChr(lastSelectData)));
    }
}

