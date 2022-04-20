
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

class GTCorrelationAnalysisOP extends GraphElementListOperation {
   
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

    static final String SEP_PAIRS = "#";

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
    // static final int PROFILE_DISPLAY = 0x8;
    static final int CorrelCoef = 0x3;
    static final int Pvalue = 0x5;
    static final int FwerWg = 0x6;
    static final int FwerBc = 0x7;
    static final int FdrWg = 0x8;
    static final int FdrBc = 0x9;
    static final int PRECISION = 3;
    static final String SYSCFG_NAME = "gtca";
    static final String NAME = "Genome / Transcriptome Correlation Analysis";
    static final String RUNNING_MESSAGE = "Computing GTCA...";
    static final String DONE_MESSAGE = "GTCA done !";
    static final String CORREL = "CorrelCoef";
    static final String PVALUE = "Pvalue";
    static final String FWERWG = "FwerWg";
    static final String FWERBC = "FwerBc";
    static final String FDRWG = "FdrWg";
    static final String FDRBC = "FdrBc";

    // examples:
    static final Property CorrelCoefProp = Property.getProperty(CORREL);
    static final Property PvalueProp = Property.getProperty(PVALUE);
    static final Property FwerWgProp = Property.getProperty(FWERWG);
    static final Property FwerBcProp = Property.getProperty(FWERBC);
    static final Property FdrWgProp = Property.getProperty(FDRWG);
    static final Property FdrBcProp = Property.getProperty(FDRBC);

    static File tempfile;

    private String curChr = null;

    public String[] getSupportedInputTypes() {
	// 	return null;
	// return new String[]{VAMPConstants.CGH_ARRAY_TYPE, VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE};
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE, VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE, VAMPConstants.TRANSCRIPTOME_TYPE, VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE};
    }

    public String getReturnedType() {
	return null;
    }

    GTCorrelationAnalysisOP() {
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

    Vector makePairs(Vector graphElements) {
	int size = graphElements.size();

	if (size == 0)
	    return null;

	Vector trs_v = new Vector();
	Vector cgh_v = new Vector();

	for (int m = 0; m < size; m++) {
	    GraphElement graphElem = (GraphElement)graphElements.get(m);
	    String type = VAMPUtils.getType(graphElem);

	    if (m == 0) {
		if (type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE) ||
		    type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
		    curChr = null;
		else if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
			 type.equals(VAMPConstants.CGH_ARRAY_TYPE))
		    curChr = VAMPUtils.getChr(graphElem);
		else {
		    return null;
		}
	    }
	    else {
		if (type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE) ||
		    type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE)) {
		    if (curChr != null) {
			return null;
		    }
		}
		else if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
			 type.equals(VAMPConstants.CGH_ARRAY_TYPE)) {
		    if (curChr == null ||
			!curChr.equals(VAMPUtils.getChr(graphElem))) {
			return null;
		    }
		}
		else {
		    return null;
		}
	    }

	    if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
		type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE))
		trs_v.add(graphElem);
	    else if (type.equals(VAMPConstants.CGH_ARRAY_TYPE) || 
		     type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
		cgh_v.add(graphElem);
	    else {
		return null;
	    }
	}

	int trs_size = trs_v.size();
	int cgh_size = cgh_v.size();

	if (trs_size == 0 || cgh_size == 0) {
	    return null;
	}

	Vector pair_v = new Vector();

	Vector cgh_set = new Vector();

	for (int n = 0; n < trs_size; n++) {
	    DataSet trs = (DataSet)trs_v.get(n);
	    DataSet ref = (DataSet)trs.getPropertyValue(VAMPProperties.ArrayRefProp);
	    if (ref == null) {
		return null;
	    }

	    if (!contains(cgh_v, ref)) {
		return null;
	    }

	    if (!contains(cgh_set, ref))
		cgh_set.add(ref);

	    pair_v.add(new Pair(ref, trs));
	}

	if (cgh_set.size() != cgh_v.size()) {
	    return null; // orphan cgh 
	}

	return pair_v;
    }

    public boolean mayApplyP(View view,  GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	// return makePairs(graphElements) != null;
	graphElements = getGraphElements(panel, graphElements, autoApply);
	SystemConfig sysCfg = (SystemConfig)view.getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);
	return sysCfg.getParameter(SYSCFG_NAME + ":URL") != null &&
	    getChr(graphElements) != null;
    }

    static class ProbeSetComparator implements Comparator {

	static class Item {
	    DataSet dataSet;
	    DataElement data;

	    Item(DataSet dataSet, DataElement data) {
		this.dataSet = dataSet;
		this.data = data;
	    }
	}

	ProbeSetComparator() {
	}

	public int compare(Object o1, Object o2) {
	    Item item1 = (Item)o1;
	    Item item2 = (Item)o2;
	    DataElement pset1 = item1.data;
	    DataElement pset2 = item2.data;
	    if (getProbeSetID(pset1).equals(getProbeSetID(pset2)))
		return 0;
	    int delta = (int)(pset1.getPosX(item1.dataSet) -
			      pset2.getPosX(item2.dataSet));
	    if (delta != 0)
		return delta;
	    return getProbeSetID(pset1).compareTo(getProbeSetID(pset2));
	}
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	graphElements = getGraphElements(panel, graphElements, autoApply);
	//  graphElements = makePairs(graphElements);

	if (graphElements.size() == 0)
	    return null;

	String chr_s = getChr(graphElements);
	if (chr_s == null) {
	    InfoDialog.pop(view.getGlobalContext(), "cannot apply " +
			   "GTCA on different chromosomes");
	    return null;
	}

	SystemConfig sysCfg = (SystemConfig)view.getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);
	String cgi_name = sysCfg.getParameter(SYSCFG_NAME + ":URL");
	String surl = cgi_name;

	String data = "action=gtca";
	String urlMake = (String)makeURLName(graphElements);
        if ( (urlMake.length() == 0) || (urlMake == null) ) {  
	    InfoDialog.pop(view.getGlobalContext(), "cannot apply " +
			   "GTCA : you don't have any pairs of CGH/TRANSCRIPTOME");
	    return null;
	}

	data += "&url=" + urlMake;
        boolean region_scope = params.get(SCOPE_PARAM).equals(REGIONS);
        boolean isAllClones = params.get(SCOPE_PARAM).equals(ALL);

	data += "&chr=" + chr_s;
	data += "&isAllClones=" + (isAllClones ? "true" : "false");
	data += "&criteria=" + (String)params.get(CRITERIA_PARAM);
	data += "&correlation=" + (String)params.get(CORRELATION_PARAM);
	// data += "&result=" + result;
	data += "&result=" + (String)params.get(RESULT_PARAM);

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

	String method = sysCfg.getParameter("gtca:method");

	if (method == null || !method.equalsIgnoreCase("post")) {
	    surl += "?" + data;
	    data = null;
	}

	System.out.println("GTCA URL: " + surl + " :: " + data);

	try {
	    InputStream is = Utils.openStream(surl, data);
	    boolean merge = chr_s.equals("all");
	    GraphElementListOperation op = null;

	    if (merge) {
		op = GraphElementListOperation.get(MergeChrOP.GTCA_NAME);
	    }

	    ViewFrame vf = buildProfileView(view.getGlobalContext(), panel,
					    merge);

	    RemoteOP rop;

	    if (vf == null)
		rop = new RemoteOP(null, view, panel, false,
				   false, 0, true, is,
				   RUNNING_MESSAGE, DONE_MESSAGE,
				   RemoteOP.ARRAY_FACTORY);
	    else {
		rop = new RemoteOP(vf, view, null, false,
				   false, 0, true, is,
				   RUNNING_MESSAGE, DONE_MESSAGE,
				   RemoteOP.ARRAY_FACTORY);
	    }

	    if (merge)
		rop.setPostOP(op);
	    rop.setPost2OP(this);
	    rop.start();
	   
	} catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(view.getGlobalContext(), e.getMessage());
	    return null;
        }
	return null;
    }


    ViewFrame buildProfileView(GlobalContext globalContext, GraphPanel panel,
			       boolean merge) {
	BarplotDataSetDisplayer histo_dsp = new BarplotDataSetDisplayer(true);

	PointDataSetDisplayer point_dsp = new PointDataSetDisplayer(true);

	PanelProfile panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     point_dsp,
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

	ViewFrame vf = new ViewFrame(globalContext,
				     "GTCA",
				     panelProfiles,
				     null,
				     null,
				     null, null,
				     new LinkedList(),
				     Config.defaultDim,
				     null);



	return vf;
    }


    // public Vector apply(View view, GraphPanel panel,
    // 		 Vector graphElements, TreeMap params,
    // 		 boolean autoApply) {
    // 	try {
    // 	    Vector pair_v = makePairs(graphElements);
    // 	    if (pair_v == null)
    // 		return null;

    // 	    int result_mask = ((Integer)params.get(RESULT_PARAM)).intValue();
    // 	    if (result_mask == 0)
    // 		return null;

    // 	    int type;

    // // 	    if (params == null ||
    // // 		params.get(CRITERIA_PARAM).equals(SMOOTHING_VALUE))
    // // 		type = SMOOTHING;
    // // 	    else
    // // 		type = GNL;
    // 	    if (params == null ||
    // 		params.get(CRITERIA_PARAM).equals(RATIO_VALUE))
    // 		type = RATIO;
    // 	    else
	
    // 		type = SMOOTHING;

    // 	    CorrelationInfo.init();
    // 	    int pair_size = pair_v.size();

    // 	    DataSet refDataSet = (DataSet)graphElements.get(0);
    // 	    TreeMap pset_map = new TreeMap(new ProbeSetComparator());
    // 	    boolean isHTML = ((result_mask & HTML_REPORT) != 0);
    // 	    boolean isFullHTML = ((result_mask & HTML_FULL_REPORT) != 0);
    // 	    String sthr = (String)params.get(THRESHOLD_PARAM);
    // 	    double threshold = (sthr != null && sthr.trim().length() > 0 ?
    // 				Math.abs(Double.parseDouble(sthr.trim())) : 0.);
    // 	    String spvalue = (String)params.get(PVALUE_PARAM);
    // 	    double pvalue_max = (spvalue != null &&
    // 				 spvalue.trim().length() > 0 ?
    // 				 Math.abs(Double.parseDouble(spvalue.trim())) : 1.);

    // 	    int which = 0;
    // 	    if (params.get(CORRELATION_PARAM).equals(PEARSON))
    // 		which = CorrelationInfo.PEARSON_I;
    // 	    else if (params.get(CORRELATION_PARAM).equals(SPEARMAN))
    // 		which = CorrelationInfo.SPEARMAN_I;

    // 	    boolean region_scope = params.get(SCOPE_PARAM).equals(REGIONS);

    // 	    for (int n = 0; n < pair_size; n++) {
    // 		Pair p = (Pair)pair_v.get(n);
    // 		DataElement psets[] = p.trs.getData();
    // 		int pset_length = psets.length;

    // 		for (int i = 0; i < pset_length; i++) {
    // 		    DataElement pset = psets[i];
    // 		    // EV 05/02/07 CORRECTION
    // 		    // if (region_scope && !isEligible(panel, pset, refDataSet))
    // 		    if (region_scope && !isEligible(panel, pset, p.trs))
    // 			continue;

    // 		    String signal = (String)pset.getPropertyValue(VAMPProperties.SignalProp);
    // 		    if (signal == null || signal.equals(VAMPProperties.NA))
    // 			continue;

    // 		    double xi = pset.getPosY(p.trs);
    // 		    double yi = getYi(p.trs, pset, p.cgh, type);

    // 		    ProbeSetComparator.Item pset_item =
    // 			new ProbeSetComparator.Item(p.trs, pset);

    // 		    CorrelationInfo correl_info = (CorrelationInfo)
    // 			pset_map.get(pset_item);

    // 		    if (correl_info == null) {
    // 			DataElement pset_c = (DataElement)pset.clone();
    // 			// 27/04/06 ?
    // 			pset_c.copyPos(p.trs, pset, p.trs);
    // 			pset_c.removeProperty(VAMPProperties.ArrayProp);
    // 			pset_c.removeProperty(Property.getProperty("Detection"));
    // 			pset_c.removeProperty(TranscriptomeFactory.PValueProp);
    // 			correl_info = new CorrelationInfo(p.trs, pset_c);
    // 			pset_map.put(pset_item, correl_info);
    // 		    }
		    
    // 		    if (isFullHTML) {
    // 			String xi_s = Utils.performRound(xi, PRECISION);
    // 			String yi_s = Utils.performRound(yi, PRECISION);
    // 			String s =
    // 			    "<tr>" +
    // 			    "<td>" + (String)p.cgh.getID() + "</td>" +
    // 			    "<td>" + yi_s + "</td>" +
    // 			    "<td>" + p.trs.getID() + "</td>" +
    // 			    "<td>" + xi_s + "</td>" +
    // 			    "</tr>";
			
    // 			String info = (String)correl_info.getData().getPropertyValue
    // 			    (Property.getProperty("Info"));

    // 			if (info == null)
    // 			    info = "";
    // 			info += s;

    // 			correl_info.getData().setPropertyValue
    // 			    (Property.getProperty("Info"), info);
    // 		    }

    // 		    correl_info.add(xi, yi);
    // 		}
    // 	    }

    // 	    Iterator it = pset_map.values().iterator();
    // 	    for (int ii = 0; it.hasNext(); ii++) {
    // 		CorrelationInfo correl_info = (CorrelationInfo)it.next();
    // 		if (correl_info.getCorrelSize() > 1) {
    // 		    double correl = correl_info.computeCorrel(which);

    // 		    if (TRACE)
    // 			System.out.println("#" + ii + ", CORREL = " + correl);

    // 		    if (!Double.isNaN(correl) &&
    // 			Math.abs(correl) >= threshold ) {
    // 			double pvalue = correl_info.computePValue(correl);
    // 			if (pvalue <= pvalue_max) {
    // 			    correl_info.setCorrelation(correl);
    // 			    correl_info.setPValue(pvalue);
    // 			}
    // 		    }
    // 		}
    // 	    }

    // 	    GlobalContext globalContext = (view != null ? view.getGlobalContext() :
    // 					   (GlobalContext)params.get("GlobalContext"));
    // 	    if ( ((result_mask & CorrelCoef) != 0) || ((result_mask & Pvalue) != 0)||  ((result_mask & FwerWg) != 0) ||  ((result_mask & FwerBc) != 0)  || ((result_mask & FdrWg) != 0) || ((result_mask & FdrBc) != 0) )
    // 		return profileDisplay(globalContext, panel, graphElements, params, pair_v,
    // 				      type, threshold, pset_map);

    // 	    return buildReport(view, panel, result_mask, graphElements, pair_v,
    // 			       type, threshold, pset_map);
    // 	}
    // 	catch(Exception e) {
    // 	    e.printStackTrace();
    // 	    System.err.println(e);
    // 	    return null;

    // 	}
    //     }



    Vector buildReport(View view, GraphPanel panel, int result_mask,
		       Vector graphElements, Vector pair_v, int type,
		       double threshold, TreeMap pset_map) throws Exception {

	File file = DialogUtils.openFileChooser(new Frame(), "Save", 0, true);
	if (file == null)
	    return graphElements;

	boolean isHTML = (result_mask & HTML_REPORT) != 0;
	boolean isFullHTML = (result_mask & HTML_FULL_REPORT) != 0;
	boolean isCSV = (result_mask & CSV_REPORT) != 0;

	String ext = isCSV ? ".csv" : ".html";

	if (!Utils.hasExtension(file.getName(), ext))
	    file = new File(file.getAbsolutePath() + ext);

	FileOutputStream os = new FileOutputStream(file);
	PrintStream ps = new PrintStream(os);
	ReportBuilder builder;
	if (isHTML || isFullHTML)
	    builder = new HTMLReportBuilder(ps, NAME);
	else
	    builder = new CSVReportBuilder(ps, NAME);

	builder.startDocument();
	
	builder.addTitle3("Parameters");
	builder.addText("Criteria", HTMLReportBuilder.BOLD_STYLE);
	builder.addText(": " + (isSmoothing(type) ? "Smoothing" : "GNL"));
	builder.addVPad();
	builder.addText("Threshold", HTMLReportBuilder.BOLD_STYLE);
	builder.addText(": " + threshold);
	builder.addVPad(2);

	int pair_size = pair_v.size();
	builder.addTitle3("Pair List (" + pair_size + ")");

	builder.startTable(new String[]{"CGH Array", "Transcriptome"});
	
	for (int n = 0; n < pair_size; n++) {
	    Pair p = (Pair)pair_v.get(n);
	    builder.startRow();
	    builder.addCell(p.cgh.getID().toString());
	    builder.addCell(p.trs.getID().toString());
	    builder.endRow();
	}
	    
	builder.endTable();

	Iterator it = pset_map.values().iterator();
	int cnt = 0;
	while (it.hasNext()) {
	    CorrelationInfo correl_info = (CorrelationInfo)it.next();
	    double correl = correl_info.getCorrelation();
	    if (!Double.isNaN(correl))
		cnt++;
	}

	builder.addTitle3("Correlations (" + cnt + ")");

	String columns[] = new String[isFullHTML ? 4 : 3]; 
	columns[0] = "ID";
	columns[1] = "Correlation";
	columns[2] = "P-Value";
	if (isFullHTML)
	    columns[3] = "Details";
	builder.addVPad(1);
	builder.startTable(columns);

	it = pset_map.values().iterator();
	while (it.hasNext()) {
	    CorrelationInfo correl_info = (CorrelationInfo)it.next();
	    double correl = correl_info.getCorrelation();
	    if (Double.isNaN(correl))
		continue;

	    double pvalue = correl_info.getPValue();
	    DataElement pset = correl_info.getData();
	    String id = getProbeSetID(pset);
	    String src = (String)pset.getPropertyValue(TranscriptomeFactory.SourceProp);
	    if (src != null) {
		if (isCSV)
		    id += " / " + src;
		else
		    id += "&nbsp;/&nbsp;" + src;
	    }

	    String srcID = (String)pset.getPropertyValue(TranscriptomeFactory.SourceIDProp);
	    if (srcID != null)
		id += ":" + srcID;
		    
	    SystemConfig sysCfg = (SystemConfig)
		view.getGlobalContext().get
		(SystemConfig.SYSTEM_CONFIG);
	    String urlTemplate = sysCfg.getParameter("GTCA:probeSetURL");
		    
	    if (urlTemplate != null && !isCSV)
		id = "<a href='" + pset.fromTemplate(urlTemplate) + "'>" + id + "</a>";
		    
	    builder.startRow();
	    builder.addCell(id);
	    builder.addCell(Utils.performRound(correl, PRECISION));
	    builder.addCell(Utils.performRound(pvalue, PRECISION));
	    if (isFullHTML) {
		String info = (String)pset.getPropertyValue
		    (Property.getProperty("Info"));
		builder.startCell();
		builder.startTable(new String[]{"CGH Array", "Yi",
						"Transcriptome", "Xi"});
		builder.startRow();
		builder.addCell(info.replaceAll(" ", "&nbsp;"));
		builder.endRow();
		builder.endTable();
		builder.endCell();
	    }

	    builder.endRow();
	}
	
	builder.endTable();
	builder.endDocument();

	ps.close();

	return graphElements;
    }

    Vector profileDisplay(GlobalContext globalContext, GraphPanel panel, Vector graphElements,
			  TreeMap params,
			  Vector pair_v, int type, double threshold,
			  TreeMap pset_map) throws Exception {
	Vector pset_v = new Vector();
	Iterator it = pset_map.values().iterator();
	DataSet refDataSet = (DataSet)graphElements.get(0);
	DataSet dset = new DataSet();
	while (it.hasNext()) {
	    CorrelationInfo correl_info = (CorrelationInfo)it.next();
	    double correl = correl_info.getCorrelation();
	    if (Double.isNaN(correl))
		continue;
	    DataElement pset = (DataElement)correl_info.getData().clone();
	    pset.copyPos(dset, correl_info.getData(), correl_info.getDataSet());
	    pset.setPosY(dset, correl);
	    pset.setPropertyValue(VAMPProperties.SignalProp,
				  Utils.toString(correl));
	    pset.setPropertyValue(Property.getProperty("P-Value"),
				  Utils.toString(correl_info.getPValue()));
	    pset_v.add(pset);
	}

	DataElement cor_pset[] = new DataElement[pset_v.size()];
	for (int i = 0; i < cor_pset.length; i++)
	    cor_pset[i] = (DataElement)pset_v.get(i);

	dset.setData(cor_pset);

	if (curChr == null)
	    VAMPUtils.setType(dset, VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE);
	else
	    VAMPUtils.setType(dset, VAMPConstants.GTCA_TYPE);
	dset.setPropertyValue(VAMPProperties.ThresholdsNameProp,
			      VAMPConstants.THR_TRS, false);
	dset.setPropertyValue(VAMPProperties.ProbeSetCountProp,
			      new Integer(cor_pset.length), false);
	dset.setPropertyValue(VAMPProperties.SignalScaleProp, "L");
	dset.setPropertyValue(Property.getProperty("Mode"),
			      isSmoothing(type) ? "Smoothing" : "GNL");

	dset.setPropertyValue(ParamsProp, params);
	dset.setPropertyValue(VAMPProperties.ArraysRefProp, graphElements);

	DataSet odset = (DataSet)((Pair)pair_v.get(0)).trs;
	dset.setPropertyValue(VAMPProperties.OrganismProp,
			      odset.
			      getPropertyValue(VAMPProperties.OrganismProp));
	BarplotDataSetDisplayer histo_dsp = new BarplotDataSetDisplayer();
	histo_dsp.drawCentered(true);

	PanelProfile panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     histo_dsp,
	     (curChr == null ? Config.defaultChromosomeNameAxisDisplayer :
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

	ViewFrame vf = new ViewFrame(globalContext,
				     "GTCA",
				     panelProfiles,
				     null,
				     null,
				     null, null,
				     new LinkedList(),
				     Config.defaultDim,
				     null);

	Vector rGraphElements = new Vector();
	rGraphElements.add(dset);
	LinkedList list = Utils.vectorToList(rGraphElements);
	vf.getView().getGraphPanelSet().getPanel(panel != null ? panel.getWhich() : 0).setGraphElements(list);
	vf.setVisible(true);
	vf.getView().syncGraphElements();
	return graphElements;
    }

    double getYi(DataSet trs, DataElement pset, DataSet cgh, int type) {
	DataElement data[] = cgh.getData();

	int ind[] = getYi_2_ind(trs, pset, cgh, false);

	// if (ind.length == 1)
	// 	    return isSmoothing(type) ? getSmoothing(data[ind[0]]) : getGNL(data[ind[0]]);

	if (ind.length == 1)
	    return isSmoothing(type) ? getSmoothing(data[ind[0]]) : getRatio(data[ind[0]]);
	DataElement before = data[ind[0]];
	DataElement after = data[ind[1]];
	
	long x1 = (long)pset.getPosX(trs);
	long x2 = (long)(x1 + pset.getPosSize(trs));

	if (isSmoothing(type)) {
	    double smt1 = getSmoothing(before);
	    double smt2 = getSmoothing(after);

	    // linear interpolation: a * x + b

	    double a = (smt2 - smt1) /
		(after.getPosX(cgh) - (before.getPosX(cgh)+before.getPosSize(cgh)));
	    double x = (x1 - (before.getPosX(cgh)+before.getPosSize(cgh)));
	    double b = smt1;

	    double smt = a * x + b;
	    return smt;
	}

	int gnl1 = getGNL(before);
	int gnl2 = getGNL(after);
	if (gnl1 == gnl2)
	    return gnl1;

	if (gnl1 == VAMPConstants.CLONE_NA || gnl1 == VAMPConstants.CLONE_UNKNOWN || gnl1 == VAMPConstants.CLONE_NORMAL)
	    return gnl2;

	if (gnl2 == VAMPConstants.CLONE_NA || gnl2 == VAMPConstants.CLONE_UNKNOWN || gnl2 == VAMPConstants.CLONE_NORMAL)
	    return gnl1;

	return (x1-(before.getPosX(cgh)+before.getPosSize(cgh)) <
		after.getPosX(cgh) - x1) ? gnl1 : gnl2;
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

    // TBD: tenir compte des frontieres de chr
    int[] getYi_2_ind(DataSet trs, DataElement pset, DataSet cgh, boolean mode) {
	long x1 = (long)pset.getPosX(trs);
	long x2 = (long)(x1 + pset.getPosSize(trs));

	DataElement data[] = cgh.getData();

	//System.out.print(getProbeSetID(pset) + ": [" + x1 + "," + x2 + "] ");

	int start, end;
	if (mode) {
	    start = 0;
	    end = data.length;
	}
	else {
	    start = -1;
	    end = -1;
	    Vector chr_cache = ChromosomeNameAxisDisplayer.
		computeChrCache(cgh, true);
	    int size = chr_cache.size();
	    String chr = VAMPUtils.norm2Chr(VAMPUtils.getChr(pset));

	    for (int i = 0; i < size; i += 2) {
		int n = ((Integer)chr_cache.get(i)).intValue();
		if (chr.equals(VAMPUtils.norm2Chr((String)chr_cache.get(i+1))))
		    start = n;
		else if (start != -1) {
		    end = n+1;
		    break;
		}
	    }

	    if (end == -1)
		end = data.length;
	}

			       
	int k = (end-start)/2;

	int nn;
	for (nn = 1; ; nn++) {
	    int old_k = k;
	    int where = find(x1, x2, data, cgh, k);

	    if (where == 0)
		break;

	    if (where > 0) {
		end = k;
		k = start + ((k-start)/2);
	    }
	    else {
		start = k;
		k = start + ((end - k)/2);
	    }

	    if (end - start <= 1 &&
		(k == old_k || find(x1, x2, data, cgh, k) != 0)) {
		k = start;
		break;
	    }
	}

	if (k == end)
	    k = end-1;

	int before = end;

	for (int n = k; n >= start; n--) {
	    DataElement d = data[n];
	    if (VAMPUtils.isNA(d))
		continue;

	    long dx1 = (long)d.getPosX(cgh);
	    long dx2 = (long)(d.getPosX(cgh) + d.getPosSize(cgh));
	    if ((x1 >= dx1 && x1 <= dx2) ||
		(x2 >= dx1 && x2 <= dx2)) {
		return new int[]{n};
	    }

	    if (before == end && x1 > dx2) {
		before = n;
		break;
	    }
	}

	if (before == end)
	    before = start;

	int after = start;

	for (int n = k+1; n < end; n++) {
	    DataElement d = data[n];
	    if (VAMPUtils.isNA(d))
		continue;

	    long dx1 = (long)d.getPosX(cgh);
	    long dx2 = (long)(d.getPosX(cgh) + d.getPosSize(cgh));
	    if ((x1 >= dx1 && x1 <= dx2) ||
		(x2 >= dx1 && x2 <= dx2)) {
		return new int[]{n};
	    }

	    if (after == start && dx1 > x2) {
		after = n;
		break;
	    }
	}

	if (after == start)
	    after = end-1;

	return new int[]{before, after};
    }

    private String getSupport(String support) {
	return ":" + (support != null ? support.replaceAll("\n", ",") : "null");
    }

    private static String makeURLName(Vector graphElements) {
	Hashtable name_ht = new Hashtable();
	String s = "";
	int t = 0;
	Object s_arr[] = graphElements.toArray();
	for (int n = 0; n < s_arr.length; n++) {
	    GraphElement graphElem = (GraphElement)s_arr[n];
            String type = VAMPUtils.getType(graphElem);	
	    String url = graphElem.getURL();
	    if (url == null)
		url = graphElem.getSourceURL();
	    String namePuce = (String)graphElem.getPropertyValue(VAMPProperties.NameProp);
            if ( type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE) || 
		 type.equals(VAMPConstants.CGH_ARRAY_TYPE) ) {
		name_ht.put(namePuce, url);
	    }
	}
        
	for (int n = 0; n < s_arr.length; n++) {
	    GraphElement graphElem = (GraphElement)s_arr[n];
            String type = VAMPUtils.getType(graphElem);	
	    if ( type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE) || 
		 type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ) {
		String url_tr = graphElem.getURL();
		if (url_tr == null)
		    url_tr = graphElem.getSourceURL();
		String arrayRefName =  (String)graphElem.getPropertyValue(VAMPProperties.ArrayRefNameProp);
		String urlRef =  (String)name_ht.get(arrayRefName); 
		if ( (arrayRefName != null) && (urlRef != null) ) {
		    String namePuce = (String)graphElem.getPropertyValue(VAMPProperties.NameProp);
		    if ( (url_tr.length() == 0) || (url_tr == null) ) {  
			String projectId = (String)graphElem.getPropertyValue(VAMPProperties.ProjectIdProp);
			String numHisto = (String)graphElem.getPropertyValue(VAMPProperties.NumHistoProp);
			//url_tr = "trs/" + projectId + "/" + numHisto + "/array/" + namePuce + ".xml";
			url_tr = "TRS/" + projectId + "/" + numHisto + "/array/" + namePuce + ".xml";
		    }
		    String url = (String)name_ht.get(arrayRefName) + SEP_PAIRS + url_tr;
		    s += (t > 0 ? "|" : "") + url;
		}
		t++;
	    }
	}
        
	return s;
    }

    static String getProbeSetID(DataElement pset) {
	return (String)pset.getPropertyValue(TranscriptomeFactory.ObjectIdProp);
    }

    static double getSmoothing(DataElement d) {
	String value = (String)d.getPropertyValue(VAMPProperties.SmoothingProp);
	if (value == null || value.equals("NA"))
	    return 0.;

	return Utils.parseDouble(value);
    }

    boolean isSmoothing(int type) {
	return type == SMOOTHING;
    }

    static double getRatio(DataElement d) {
	String value = (String)d.getPropertyValue(VAMPProperties.RatioProp);
	if (value == null || value.equals("NA"))
	    return 0.;

	return Utils.parseDouble(value);
    }

    boolean isRatio(int type) {
	return type == RATIO;
    }


    static int getGNL(DataElement d) {
	return GNLProperty.getGNL(VAMPUtils.getGNL(d));
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
	// 	params.put(RESULT_PARAM, new Integer(CorrelCoef));
	params.put(RESULT_PARAM, CORREL);

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
	// 	params.put(RESULT_PARAM, Utils.makeInteger(map.get(RESULT_PARAM)));
	params.put(RESULT_PARAM, map.get(RESULT_PARAM));
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
	return RUNNING_MESSAGE;
    }


    public static void rebuild(DataSet dataSet, String result_params) {

        System.out.println("GTCA REBUILD : result_params: " + result_params);
        System.out.println("GTCA REBUILD : property: " + Property.getProperty(result_params));
	DataElement data[] = dataSet.getData();
	for (int n = 0; n < data.length; n++) {
	    DataElement d = data[n];
	    //System.out.println("n = " + n);
	    String s = (String)d.getPropertyValue(Property.getProperty(result_params));
            if (s != null)  {
		//System.out.println("result_get: " + s);
		d.setPropertyValue(VAMPProperties.RatioProp, s);
		d.setPosY(dataSet, Utils.parseDouble(s));

		d.maskProperty(VAMPProperties.RatioProp);
		d.maskProperty(VAMPProperties.IsNAProp);
	    }
	    else  {

		System.out.println("result_get: NULL impossible de recuperer les valeurs ?");
	    }
	}
    }

    public boolean mayApplyOnLightImportedProfiles() {
	return true;
    }
}
