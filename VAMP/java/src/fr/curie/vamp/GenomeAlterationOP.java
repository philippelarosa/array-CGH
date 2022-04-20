
/*
 *
 * GenomeAlterationOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.awt.*;
import java.io.*;

class GenomeAlterationOP extends GraphElementListOperation {
   
    static final boolean TRACK_BUG_CHR = true;
    static final boolean NEW_RECREG_ALGO = true;

    static final boolean DEFAULT_TRACE_BKP = false;
    static final boolean DEFAULT_TRACE_REG = false;

    static boolean TRACE_BKP_DETAILS = false;
    static boolean TRACE_BKP = false;
    static boolean TRACE_REG = false;

    static final boolean TRY_NA = true;

    static final String OUTLIER_HELP = 
	"Consider outlier clones for\nregion computation.";
    static final String EXTENDED_NA_HELP = 
	"Extends region when clone status\nis unknown (NA).";
    static final String WIDEN_REGION_HELP =
	"Alteration is presumed after\nlast clone, upto the next clone.";

    static final String COMPUTE_MASK = "Compute";

    static final int MINIMAL_REGIONS_MASK = 0x1;
    static final int RECURRENT_REGIONS_MASK = 0x2;
    static final int IN_RECURRENT_BREAKPOINTS_MASK = 0x4;
    static final int OUT_RECURRENT_BREAKPOINTS_MASK = 0x8;

    static final String SUPPRESS_REGIONS = "Suppress";

    static final String SCOPE_MASK = "Scope";

    static final int ALL_CHR_MASK = 0x1;
    static final int EXCL_X_CHR_MASK = 0x2;
    static final int EXCL_Y_CHR_MASK = 0x4;

    // minimal support of alterations
    static final String MIN_NUMBER_ALT = "MinNumberAlt";
    static final String MIN_PERCENT_ALT = "MinPercentAlt";

    // minimal breakpoint support for minimal regions
    static final String MIN_NUMBER_BKP = "MinNumberBkp";
    static final String MIN_PERCENT_BKP = "MinPercentBkp";

    static final String OUTLIERS = "Outliers";
    static final String WIDEN_REGIONS = "WideRegions";
    static final String EXTENDED_NA = "ExtendedNA";
    static final String TRUE = "true";
    static final String FALSE = "false";
    static final String ALL = "All";
    static final String ALT_MASK = "AltMask";

    static final String RESULT_MASK = "ResultMask";
    static final int DISPLAY_REGIONS_MASK = 0x1;
    static final int CSV_REPORT_MASK = 0x2;
    static final int HTML_REPORT_MASK = 0x4;

    static Property CytogBeginProp = Property.getProperty("Cytog Begin");
    static Property CytogEndProp = Property.getProperty("Cytog End");
    static Property CytogProp = Property.getProperty("Cytog");
    static Property OverlappedProp = Property.getProperty("Overlapped");
    static Property NumProp = Property.getProperty("Num");
    static Property StatsProp = Property.getProperty("Stats");
    static Property DataProp = Property.getHiddenProperty("data elements");
    static Property OriStringProp = Property.getHiddenProperty("OriString");
    static Property SupportProp = Property.getProperty("Support");
    static Property SupportVProp = Property.getHiddenProperty("Support_V");
    static Property HParamsProp = Property.getHiddenProperty("HParams");
    static Property ParamsProp = Property.getProperty("Params");
    static Property UserModifiedProp = Property.getProperty
	("User Modified", PropertyBooleanType.getInstance());

    static final String GAINED = Utils.toString(VAMPConstants.CLONE_GAINED);
    static final String LOST = Utils.toString(VAMPConstants.CLONE_LOST);
    static final String AMPLICON = Utils.toString(VAMPConstants.CLONE_AMPLICON);
    static final String NORMAL = Utils.toString(VAMPConstants.CLONE_NORMAL);

    static final int GAINED_MASK = 0x1;
    static final int LOST_MASK = 0x2;
    static final int AMPL_MASK = 0x4;
    static final int NORMAL_MASK = 0x8;
    static final int MERGE_GAINED_AMPL_MASK = 0x10;
    static final int IGNORE_AMPL_MASK = 0x20;

    static final String TOLERATION = "Toleration";

    static final String NAME = "GenomeAlteration";

    static final int MIN_BKP_I = 0;
    static final int MIN_ALT_I = 1;
    static final int TOLERATION_I = 2;

    static final int OUTLIER_I = 3;
    static final int EXTENDED_NA_I = 4;
    static final int WIDEN_REGIONS_I = 5;

    static final int ALT_MASK_I = 6;
    static final int SCOPE_MASK_I = 7;

    static final int PARAMS_CNT = 8;

    int toleration, min_bkp, min_alt, alt_mask, scope_mask, compute_mask,
	res_mask;
    boolean extended_NA, widen_regions;
    boolean outlier, suppress_reg;
    boolean dsp_regions, csv_report, html_report;
    String defaultGNL = null;
    static boolean mergeGainedAmplicon, ignoreAmplicon;
    private boolean test;

    void setDefaultGNL(String defaultGNL) {
	this.defaultGNL = defaultGNL;
    }

    static String getGNLString(String gnl) {
	if (gnl.equals(GAINED))
	    return "gained";
	if (gnl.equals(LOST))
	    return "lost";
	if (gnl.equals(AMPLICON))
	    return "amplicon";
	if (gnl.equals(NORMAL))
	    return "normal";
	return null;
    }

    static String getCGNLString(String gnl) {
	if (gnl.equals(GAINED)) {
	    return (mergeGainedAmplicon ? "G+A" : "G");
	}
	if (gnl.equals(LOST))
	    return "L";
	if (gnl.equals(AMPLICON))
	    return "A";
	if (gnl.equals(NORMAL))
	    return "N";
	return null;
    }

    boolean support(RODataElementProxy data) {
	String chr = VAMPUtils.getChr(data);
	if ((scope_mask & EXCL_X_CHR_MASK) != 0 &&
	    (chr.equals("X") || chr.equals("23")))
	    return false;

	if ((scope_mask & EXCL_Y_CHR_MASK) != 0 &&
	    (chr.equals("Y") || chr.equals("24")))
	    return false;
	return true;
    }

    public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE,
			    VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.CGH_ARRAY_MERGE_TYPE,
			    VAMPConstants.GENOME_ANNOT_TYPE,
			    VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.SNP_TYPE,
			    VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.GENOME_ANNOT_TYPE,
			    VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE};
    }

    public String getReturnedType() {
	return null;
    }

    GenomeAlterationOP(boolean test) {
	super(NAME, SHOW_MENU | ON_ALL_AUTO);
	this.test = test;
    }

    GenomeAlterationOP() {
	this(false);
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	return true;
    }

    static final HashMap view_map = new HashMap();

    public TreeMap getParams(View view, Vector graphElements) {
	TreeMap defParams;
	if (test) {
	    TRACE_REG = true;
	    TRACE_BKP = true;
	    File file = DialogUtils.openFileChooser(new Frame(), "Load", 0, "Genome Alteration Test File", false);
	    if (file == null)
		return null;

	    try {
		graphElements = makeGraphElements(view.getGlobalContext(), file);
		if (graphElements == null)
		    return null;
	    }
	    catch(Exception e) {
		InfoDialog.pop(view.getGlobalContext(), "File", e);
		return null;
	    }

	    defParams = getDefaultParams(view, graphElements);
	    defParams.put("File", graphElements);
	}
	else {
	    TRACE_REG = DEFAULT_TRACE_REG;
	    TRACE_BKP = DEFAULT_TRACE_BKP;
	    defParams = getDefaultParams(view, graphElements);
	    defParams.remove("File");
	}

	TreeMap params = GenomeAlterationDialog.getParams
	    (view, graphElements, defParams);
	if (params != null)
	    view_map.put(view, params);
	return params;
    }

    public TreeMap getDefaultParams(View view, Vector graphElements) {
	TreeMap params = (TreeMap)view_map.get(view);
	if (params != null) {
	    Object vmin;
	    vmin = params.get(MIN_NUMBER_ALT);
	    if (vmin != null && !vmin.equals(ALL)) { 
		int min = ((Integer)vmin).intValue();
		if (min > graphElements.size()) {
		    InfoDialog.pop(view.getGlobalContext(),
				   "Warning: changing support " +
				   "number from " + vmin + " to All");
		    params.put(MIN_NUMBER_ALT, ALL);
		}
	    }

	    vmin = params.get(MIN_NUMBER_BKP);
	    if (vmin != null && !vmin.equals(ALL)) { 
		int min = ((Integer)vmin).intValue();
		if (min > graphElements.size()) {
		    InfoDialog.pop(view.getGlobalContext(),
				   "Warning: changing support " +
				   "number from " + vmin + " to All");
		    params.put(MIN_NUMBER_BKP, ALL);
		}
	    }
	    return params;
	}

	params = new TreeMap();

	params.put(COMPUTE_MASK, new Integer(0));
	params.put(SCOPE_MASK, new Integer(ALL_CHR_MASK));

	params.put(MIN_NUMBER_ALT, ALL);
	params.put(MIN_NUMBER_BKP, new Integer(1));
	params.put(SUPPRESS_REGIONS, FALSE);
	params.put(OUTLIERS, FALSE);
	params.put(WIDEN_REGIONS, TRUE);
	params.put(EXTENDED_NA, FALSE);
	params.put(ALT_MASK, Utils.toString(GAINED_MASK|LOST_MASK|AMPL_MASK));
	params.put(RESULT_MASK, Utils.toString(DISPLAY_REGIONS_MASK));
	view_map.put(view, params);
	return params;
    }

    /*
      View _view;
      GraphPanel _panel;
      Vector _graphElements;
      TreeMap _params;
      boolean _autoApply;

      public void perform1() {
      }

      public void perform2() {
      Vector v = apply_r(_view, _panel, _graphElements, _params, _autoApply);
      }
    */

    /*
      public View getView() {
      return _view;
      }

      public GraphPanel getPanel() {
      return _panel;
      }
    */

    public String getMessage() {
	return "Computing genome alterations...";
    }

    private int getLength(String array) {
	int len = 0;
	int size = array.length();

	for (int n = 0; n < size; n++) {
	    char c = array.charAt(n);
	    if (c == '#')
		return len;

	    if (c == ' ')
		continue;

	    if (c != '|') {
		len++;
	    }
	}

	return len;
    }

    private Vector getChrBoundaries(String array) {
	Vector chr_v = new Vector();
	int size = array.length();

	for (int n = 0, nd = 0; n < size; n++) {
	    char c = array.charAt(n);
	    if (c == '#')
		break;

	    if (c == ' ')
		continue;

	    if (c != '|') {
		chr_v.add(new Integer(nd));
	    }
	    nd++;
	}

	return chr_v;
    }


    private Vector makeGraphElements(GlobalContext globalContext, String arrays[]) throws Exception {
	int len = -1;
	String error = "";

	for (int m = 0; m < arrays.length; m++) {
	    int arr_len = getLength(arrays[m]);
	    if (arr_len == 0) {
		continue;
	    }

	    if (len < 0) {
		len = arr_len;
	    }
	    else if (arr_len != len) {
		if (error.length() > 0)
		    error += "\n";
		error += "line #" + (m+1) + " invalid size: expected " + len;
	    }
	}

	Vector chr_v = null;
	for (int m = 0; m < arrays.length; m++) {
	    int arr_len = getLength(arrays[m]);
	    if (arr_len == 0) {
		continue;
	    }

	    if (chr_v == null)
		chr_v = getChrBoundaries(arrays[m]);
	    else if (!chr_v.equals(getChrBoundaries(arrays[m]))) {
		if (error.length() > 0)
		    error += "\n";
		error += "line #" + (m+1) + " invalid chromosome boundaries";
	    }
	}

	if (error.length() > 0) {
	    InfoDialog.pop(globalContext, error);
	    return null;
	}

	Vector v = new Vector();

	for (int m = 0, mm = 0; m < arrays.length; m++) {
	    String array = arrays[m];

	    if (getLength(arrays[m]) == 0) {
		continue;
	    }

	    int chr_n = 1;
	    DataElement data[] = new DataElement[len];
	    char strchar[] = new char[len];
	    int size = array.length();
	    for (int n = 0, nd = 0; n < size; n++) {
		char c = array.charAt(n);

		if (c == ' ')
		    continue;

		if (c == '#')
		    break;

		if (c == '|') {
		    chr_n++;
		    continue;

		}

		DataElement d = new DataElement();
		data[nd] = d;
		d.setPropertyValue(VAMPProperties.ChromosomeProp, Integer.toString(chr_n));
		d.setPropertyValue(VAMPProperties.NameProp, "#" + nd);
		strchar[nd] = c;
		nd++;

		if (c == 'G')
		    d.setPropertyValue(VAMPProperties.GNLProp, "1");
		else if (c == 'L')
		    d.setPropertyValue(VAMPProperties.GNLProp, "-1");
		else if (c == 'N')
		    d.setPropertyValue(VAMPProperties.GNLProp, "0");
		else if (c == 'A')
		    d.setPropertyValue(VAMPProperties.GNLProp, "2");
		else if (c == 'I') {
		    d.setPropertyValue(VAMPProperties.GNLProp, "0");
		    d.setPropertyValue(VAMPProperties.IsNAProp, "true");
		}
		else if (c == 'M') {
		    d.setPropertyValue(VAMPProperties.GNLProp, "0");
		    d.setPropertyValue(VAMPProperties.MissingProp, "true");
		}
		else {
		    if (error.length() > 0)
			error += "\n";
		    error += "line #" + (m+1) + " character " + (n+1) + " unexecpeted " + c;
		}
	    }

	    if (error.length() > 0) {
		InfoDialog.pop(globalContext, error);
		return null;
	    }
	    
	    DataSet dset = new DataSet(data);
	    dset.setPropertyValue(VAMPProperties.NameProp, "#" + mm);
	    dset.setPropertyValue(VAMPProperties.ChromosomeProp, "1");
	    dset.setPropertyValue(VAMPProperties.OrganismProp, "Human");
	    dset.setPropertyValue(OriStringProp, new String(strchar, 0, strchar.length));
	    VAMPUtils.setType(dset, VAMPConstants.CGH_ARRAY_TYPE);
	    v.add(dset);
	    mm++;
	}

	/*
	for (int m = 0; m < v.size(); m++) {
	    DataSet dset = (DataSet)v.get(m);
	    DataElement data[] = dset.getData();
	    for (int n = 0; n < data.length; n++)
		System.out.print(data[n].getPropertyValue(VAMPProperties.GNLProp) + " ");
	    System.out.println("");
	}
	*/
	return v;
    }

    private Vector makeGraphElements(GlobalContext globalContext, File file) throws Exception {
	FileInputStream is = new FileInputStream(file);
	String s = "";
	byte b[] = new byte[4096];
	int n;
	while ((n = is.read(b)) > 0) {
	    s += new String(b, 0, n);
	}

	return makeGraphElements(globalContext, s.split("\\n"));
    }

    static Vector RETURN(Vector v, Vector v_bak) {
	if (v_bak != null)
	    return v_bak;
	return v;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	long ms0 = System.currentTimeMillis();
	try {
	    Vector graphElements_bak = null;

	    if (params.get("File") != null) {
		graphElements_bak = graphElements;
		graphElements = (Vector)params.get("File");
		//graphElements = makeGraphElements(view.getGlobalContext(),
		//(File)params.get("File"));
	    }

	    int size = graphElements.size();

	    if (size == 0)
		return RETURN(graphElements, graphElements_bak);

	    getParams(params, size);

	    dsp_regions = (res_mask & DISPLAY_REGIONS_MASK) != 0;
	    csv_report = (res_mask & CSV_REPORT_MASK) != 0;
	    html_report = (res_mask & HTML_REPORT_MASK) != 0;

	    if (!dsp_regions && !csv_report && !html_report)
		return RETURN(graphElements, graphElements_bak);

	    LinkedList iniRegions = null, iniMarks = null;
	    GraphPanelSet panel_set = view.getGraphPanelSet();

	    int sync_mode = panel_set.setPaintingRegionMode(panel.getWhich(), GraphCanvas.ASYNC_MODE);
	    //int sync_mode = panel_set.setPaintingRegionMode(panel.getWhich(), GraphCanvas.SYNC_MODE);

	    if (!dsp_regions) {
		iniRegions = (LinkedList)panel_set.getRegions().clone();
		iniMarks = (LinkedList)panel_set.getMarks().clone();
	    }

	    graphElements = NormalizeOP.normalizeOnDemand(view.getGlobalContext(), "Genome Alteration", graphElements);
	    if (graphElements == null) {
		return null;
	    }
	    //graphElements = NormalizeOP.normalize(view.getGlobalContext(), graphElements);

	    Vector altV = new Vector();

	    mergeGainedAmplicon = 
		(alt_mask & MERGE_GAINED_AMPL_MASK) != 0;
	    ignoreAmplicon = 
		(alt_mask & IGNORE_AMPL_MASK) != 0;

	    if ((alt_mask & GAINED_MASK) != 0 ||
		(alt_mask & MERGE_GAINED_AMPL_MASK) != 0)
		altV.add(GAINED);
	    if ((alt_mask & LOST_MASK) != 0)
		altV.add(LOST);
	    if ((alt_mask & NORMAL_MASK) != 0)
		altV.add(NORMAL);
	    if ((alt_mask & AMPL_MASK) != 0 &&
		!mergeGainedAmplicon && !ignoreAmplicon)
		altV.add(AMPLICON);

	    int altV_size = altV.size();

	    synchronized (panel.getCanvas().getLockObj()) {
		// to simulate long operations:
		//Thread.sleep(10000);
		suppressRegions(panel_set, panel);

		for (int m = 0; m < altV_size; m++) {
		    String gnl = (String)altV.get(m);
		    if (ignoreAmplicon) {
			setDefaultGNL(gnl);
		    }
		    else if (mergeGainedAmplicon && gnl.equals(GAINED)) {
			setDefaultGNL(gnl);
		    }
		    else {
			setDefaultGNL(null);
		    }

		    Vector recBkp_v[] = computeRecurrentBreakpoints(graphElements, gnl, (compute_mask & MINIMAL_REGIONS_MASK) != 0);
		    Vector recInBkp_v = recBkp_v[0];
		    Vector recOutBkp_v = recBkp_v[1];

		    GraphElement dataSet0 = first(graphElements);
		    //DataElement data[] = dataSet0.getData();
		    Vector bkp_v = new Vector();

		    if (((compute_mask & MINIMAL_REGIONS_MASK) != 0) ||
			((compute_mask & RECURRENT_REGIONS_MASK) != 0)) {
			bkp_v.addAll(recInBkp_v);
			bkp_v.addAll(recOutBkp_v);
			Collections.sort(bkp_v, new BreakpointComparator());
			if ((compute_mask & MINIMAL_REGIONS_MASK) != 0)
			    computeMinimalRegions(view.getGlobalContext(), panel_set, panel, graphElements, gnl, bkp_v);
			if ((compute_mask & RECURRENT_REGIONS_MASK) != 0)
			    computeRecurrentRegions(view.getGlobalContext(), panel_set, panel, graphElements, gnl, bkp_v);
		    }

		    bkp_v = new Vector();

		    if ((compute_mask & IN_RECURRENT_BREAKPOINTS_MASK) != 0)
			bkp_v.addAll(recInBkp_v);
		    if ((compute_mask & OUT_RECURRENT_BREAKPOINTS_MASK) != 0)
			bkp_v.addAll(recOutBkp_v);

		    for (int j = 0; j < bkp_v.size(); j++) {
			Breakpoint bkp = (Breakpoint)bkp_v.get(j);
			double posx = dataSet0.getDataProxy(bkp.getInd()).getPanGenPosX(dataSet0);
			if (bkp.isOUT())
			    posx += dataSet0.getDataProxy(bkp.getInd()).getPosSize(dataSet0);
			Mark mark = new Mark(posx);
			VAMPUtils.setType(mark, VAMPConstants.RECURRENT_BREAKPOINT_TYPE);
			mark.setPropertyValue(SupportProp, Breakpoint.getStringNLSupport(bkp.getSupport()));
			mark.setPropertyValue(SupportVProp, bkp.getSupport());
			panel.addMark(mark);
			if (gnl.equals(GAINED))
			    mark.setColor(VAMPResources.getColor(VAMPResources.GNL_GAINED_FG));
			else if (gnl.equals(LOST))
			    mark.setColor(VAMPResources.getColor(VAMPResources.GNL_LOST_FG));
			else if (gnl.equals(AMPLICON))
			    mark.setColor(VAMPResources.getColor(VAMPResources.GNL_AMPLICON_FG));
			else if (gnl.equals(NORMAL))
			    mark.setColor(VAMPResources.getColor(VAMPResources.GNL_NORMAL_FG));
		    }
		}

		LinkedList regions = (LinkedList)panel_set.getRegions().clone();
		sort_regions(regions);

		if (!dsp_regions) {	
		    panel_set.setRegions(0, iniRegions, false);
		    panel_set.setMarks(0, iniMarks, false);
		    buildReport(graphElements, regions);
		}
		else if (csv_report || html_report) {
		    sort_regions(regions);
		    buildReport(graphElements, regions);
		}

		panel_set.setPaintingRegionMode(panel.getWhich(), sync_mode);
	    }

	    long ms1 = System.currentTimeMillis();
	    System.out.println("GenomeAlteration duration: " + (ms1-ms0)/1000 + " seconds");
	    return RETURN(graphElements, graphElements_bak);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.err.println(e);
	    return null;
	}
    }

    void sort_regions(LinkedList regions) {
	Collections.sort(regions, new Comparator() {
		public int compare(Object o1, Object o2) {
		    Region r1 = (Region)o1;
		    Region r2 = (Region)o2;
		    if (r1.getBegin().getPosX() <
			r2.getBegin().getPosX())
			return -1;
		    
		    if (r1.getBegin().getPosX() >
			r2.getBegin().getPosX())
			return 1;
		    
		    if (r1.getEnd().getPosX() <
			r2.getEnd().getPosX())
			return -1;

		    if (r1.getEnd().getPosX() >
			r2.getEnd().getPosX())
			return 1;
		    
		    return 0;
		}
	    });
    }

    void buildReport(Vector graphElements, LinkedList regions) throws Exception {
	File file = DialogUtils.openFileChooser(new Frame(), "Save",
						DialogUtils.HTML_FILE_FILTER, true);
	if (file == null)
	    return;

	boolean isHTML = (res_mask & HTML_REPORT_MASK) != 0;
	String ext = (isHTML ? ".html" : ".csv");

	java.util.Date date = new java.util.Date();
	buildParamsReport(graphElements, file, isHTML, ext, date);
	buildSupportReport(graphElements, regions, file, isHTML, ext, date);
	buildCoocReport(graphElements, regions, file, isHTML, ext, date);
    }

    void buildParamsReport(Vector graphElements, File file, boolean isHTML,
			   String ext, Date date) throws Exception {
	file = new File(Utils.suppressExtension(file.getAbsolutePath()) +
			"_params" + ext);
	
	FileOutputStream os = new FileOutputStream(file);
	PrintStream ps = new PrintStream(os);
	
	String title = "Genome Alteration Params";

	ReportBuilder builder;
	if (isHTML)
	    builder = new HTMLReportBuilder(ps, title);
	else
	    builder = new CSVReportBuilder(ps, title, "\t");

	builder.startDocument(date);
	builder.startCenter();
	builder.addTitle1("Genome Alteration Params");
	builder.endCenter();
	builder.addHLine();

	builder.startTable();

	String value;

	builder.startRow();

	builder.addCell("Profiles (" + graphElements.size() + ")");
	value = "";
	for (int n = 0; n < graphElements.size(); n++) {
	    if (n > 0)
		value += "\n";
	    if (test) {
		value += ((GraphElement)graphElements.get(n)).getPropertyValue(OriStringProp) + builder.getUnsecableSpace();
	    }
	    value += ((GraphElement)graphElements.get(n)).getID();
	}

	builder.addCell(builder.replaceNL(value));
	builder.endRow();

	builder.startRow();
	builder.addCell("Display Alterations");
	value = "";
	if ((compute_mask & MINIMAL_REGIONS_MASK) != 0)
	    value = "Minimal regions";
	else if ((compute_mask & RECURRENT_REGIONS_MASK) != 0)
	    value = "Recurrent regions";
	else if ((compute_mask & IN_RECURRENT_BREAKPOINTS_MASK) != 0)
	    value = "In recurrent breakpoints";
	else if ((compute_mask & OUT_RECURRENT_BREAKPOINTS_MASK) != 0)
	    value = "Out recurrent breakpoints";
	
	builder.addCell(value);
	builder.endRow();

	builder.startRow();
	builder.addCell("Scope");
	value = "";
	if ((scope_mask & ALL_CHR_MASK) != 0)
	    value = "All chromosomes";
	else {
	    String excl_chr = "";
	    if ((scope_mask & EXCL_X_CHR_MASK) != 0 &&
		(scope_mask & EXCL_Y_CHR_MASK) != 0)
		excl_chr = "X and Y chromosomes";
	    else if ((scope_mask & EXCL_X_CHR_MASK) != 0)
		excl_chr = "X chromosome";
	    else if ((scope_mask & EXCL_Y_CHR_MASK) != 0)
		excl_chr = "Y chromosome";
	    value = "Exclude " + excl_chr;
	}
	builder.addCell(value);
	builder.endRow();

	builder.startRow();
	builder.addCell("Minimum support of alterations");
	builder.addCell(Utils.toString(min_alt));
	builder.endRow();

	builder.startRow();
	builder.addCell("Minimum breakpoint support");
	builder.addCell(Utils.toString(min_bkp));
	builder.endRow();

	builder.startRow();
	builder.addCell("Toleration");
	builder.addCell(Utils.toString(toleration));
	builder.endRow();

	builder.startRow();
	builder.addCell("Options");
	value = "";
	if (outlier)
	    value = add(value, "outliers");
	if (extended_NA)
	    value = add(value, "extends NA");
	if (widen_regions)
	    value = add(value, "widens regions");
	builder.addCell(Utils.toString(value));
	builder.endRow();

	builder.startRow();
	builder.addCell("Type of alterations");
	value = "";
	if ((alt_mask & GAINED_MASK) != 0)
	    value = add(value, "Gained");
	if ((alt_mask & LOST_MASK) != 0)
	    value = add(value, "Lost");
	if ((alt_mask & NORMAL_MASK) != 0)
	    value = add(value, "Normal");
	if ((alt_mask & AMPL_MASK) != 0)
	    value = add(value, "Amplicon");
	if (mergeGainedAmplicon)
	    value = add(value, "Merge gained and amplicon");
	if (ignoreAmplicon)
	    value = add(value, "Ignore amplicon");

	builder.addCell(value);
	builder.endRow();

	builder.endTable();

	builder.endDocument();
	ps.close();
    }

    static String add(String value, String add) {
	if (value.length() > 0)
	    value += " / ";
	value += add;
	return value;
    }

    void buildSupportReport(Vector graphElements, LinkedList regions,
			    File file, boolean isHTML,
			    String ext, Date date) throws Exception {
	file = new File(Utils.suppressExtension(file.getAbsolutePath()) +
			"_support" + ext);
	
	FileOutputStream os = new FileOutputStream(file);
	PrintStream ps = new PrintStream(os);
	
	String title = "Genome Alteration Support";

	ReportBuilder builder;
	if (isHTML)
	    builder = new HTMLReportBuilder(ps, title);
	else
	    builder = new CSVReportBuilder(ps, title, "\t");

	builder.startDocument(date);
	builder.startCenter();
	builder.addTitle1("Genome Alteration Support");
	builder.endCenter();
	builder.addHLine();

	builder.startTable();

	Vector supports = new Vector();
	builder.startRow();
	//	builder.addEmptyCell();
	builder.addCell("Array");
	int reg_size = regions.size();
	for (int n = 0; n < reg_size; n++) {
	    Region region = (Region)regions.get(n);
	    Vector v = null;
	    if (VAMPUtils.getType(region).equals(VAMPConstants.MINIMAL_REGION_TYPE) ||
		VAMPUtils.getType(region).equals(VAMPConstants.RECURRENT_REGION_TYPE)) {
		supports.add(region.getPropertyValue(GenomeAlterationOP.SupportVProp));
		builder.addCell(makeName(region), makeStyle(region, false));
	    }
	    else
		supports.add(null);
	}

	builder.endRow();

	int gr_size = graphElements.size();
	for (int m = 0; m < gr_size; m++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(m);
	    builder.startRow();
	    String id = (String)graphElement.getID();
	    builder.addCell(id);
	    for (int n = 0; n < reg_size; n++) {
		Vector support = (Vector)supports.get(n);
		if (support == null)
		    continue;
		int sz = support.size();
		boolean supp = false;
		for (int j = 0; j < sz; j++)
		    if (((GraphElement)support.get(j)).getID().equals(id)) {
			supp = true;
			break;
		    }

		builder.addCell(supp ? "1" : "0", "align=center " +
				(supp ? makeStyle((Region)regions.get(n), supp) :
				 ""));
	    }
	    builder.endRow();
	}

	builder.endTable();
	builder.endDocument();
	ps.close();
    }

    static String getRGB(Region region) {
	String rgb = Integer.toHexString(region.getColor().getRGB() & 0xffffff);
	while (rgb.length() < 6)
	    rgb = "0" + rgb;
	return rgb;
    }

    static String makeStyle(Region region, boolean supp) {
	if (supp)
	    return " style='color: #ffffff;' bgcolor=#888888";
	String style = "";
	if (region.getPropertyValue(VAMPProperties.GNLProp).equals("amplicon"))
	    style = "style='color: #ffffff;' ";
	return style + "bgcolor=#" + getRGB(region);
    }

    static String makeName(Region region) {
	/*
	  String name;
	  if (VAMPUtils.getType(region).equals(VAMPConstants.MINIMAL_REGION_TYPE))
	  name = "M_";
	  else
	  name = "R_";
	  String gnl = (String)region.getPropertyValue(VAMPConstants.GNLProp);
	  if (gnl.equals("gained"))
	  name += (mergeGainedAmplicon ? "G+A" : "G");
	  if (gnl.equals(LOST))
	  name += "L";
	  if (gnl.equals(AMPLICON))
	  name += "A";
	  if (gnl.equals(NORMAL))
	  name += "N";
	  name += "_" + (String)region.getPropertyValue(VAMPConstants.NameProp);
	  return name;
	*/
	return (String)region.getPropertyValue(VAMPProperties.NameProp);
    }

    void buildCoocReport(Vector graphElements, LinkedList regions,
			 File file, boolean isHTML,
			 String ext, Date date) throws Exception {
	file = new File(Utils.suppressExtension(file.getAbsolutePath()) +
			"_cooc" + ext);
	
	FileOutputStream os = new FileOutputStream(file);
	PrintStream ps = new PrintStream(os);
	
	String title = "Genome Alteration Cooccurrence";

	ReportBuilder builder;
	if (isHTML)
	    builder = new HTMLReportBuilder(ps, title);
	else
	    builder = new CSVReportBuilder(ps, title, "\t");

	builder.startDocument(date);
	builder.startCenter();
	builder.addTitle1("Genome Alteration Cooccurrence");
	builder.endCenter();
	builder.addHLine();

	builder.startTable();
	builder.startRow();
	builder.addCell("Chr");
	builder.addCell("Clone_Start");
	builder.addCell("Clone_End");
	builder.addCell("Pos_Start");
	builder.addCell("Pos_End");
	builder.addCell("Size");
	builder.addCell("Region_Name");
	builder.addCell("Index");

	int reg_size = regions.size();
	for (int n = 0; n < reg_size; n++) {
	    Region region = (Region)regions.get(n);
	    if (!candidateRegion(region))
		continue;
	    /*
	      builder.addCell((String)region.getPropertyValue(VAMPConstants.NameProp),
	      makeStyle(region, false));
	    */
	    builder.addCell("R" + Utils.toString(n+1),
			    makeStyle(region, false));
	}

	builder.endRow();
	Property suppVProp = GenomeAlterationOP.SupportVProp;
	for (int n = 0; n < reg_size; n++) {
	    Region region = (Region)regions.get(n);
	    if (!candidateRegion(region))
		continue;
	    builder.startRow();
	    builder.addCell((String)region.getPropertyValue(VAMPProperties.ChromosomeProp));
	    builder.addCell((String)region.getPropertyValue(VAMPProperties.CloneBeginProp));
	    builder.addCell((String)region.getPropertyValue(VAMPProperties.CloneEndProp));
	    builder.addCell((String)region.getPropertyValue(VAMPProperties.PositionChrBeginProp));
	    builder.addCell((String)region.getPropertyValue(VAMPProperties.PositionChrEndProp));
	    builder.addCell((String)region.getPropertyValue(VAMPProperties.RegionSizeProp));
	    builder.addCell((String)region.getPropertyValue(VAMPProperties.NameProp),
			    makeStyle(region, false));

	    builder.addCell("R" + Utils.toString(n+1),
			    "align=center " + makeStyle(region, false));

	    for (int j = 0; j < reg_size; j++) {
		Region region2 = (Region)regions.get(j);
		if (!candidateRegion(region2))
		    continue;
		int int_sz = 
		    intersects_size
		    ((Vector)region.getPropertyValue(suppVProp),
		     (Vector)region2.getPropertyValue(suppVProp));
		builder.addCell(Utils.toString(int_sz), 
				makeStyle(region2, region == region2) +
				" align=center");
		    
	    }
	    builder.endRow();
	}

	builder.endTable();

	builder.endDocument();
	ps.close();
    }

    static final String DEFAULT_GNL = "<DEFAULT>";

    static String getGNL(RODataElementProxy data) {
	String gnl = VAMPUtils.getGNL(data);
	if (gnl.equals(AMPLICON) && mergeGainedAmplicon)
	    return GAINED;
	return gnl;
    }

    String getGNL(GraphElement dataSet0, int n, boolean outlier, int incr) throws Exception {

	String chr = VAMPUtils.getChr(dataSet0.getDataProxy(n));
	//DataElement d = data[n];
	RODataElementProxy d = dataSet0.getDataProxy(n);
	int length = dataSet0.getProbeCount();
	boolean foundMissing = false;
	for (; n < length && n >= 0; n += incr) {
	    // added 10/03/06
	    // moved 20/07/07 from if (outlier || ...
	    if (!VAMPUtils.getChr(dataSet0.getDataProxy(n)).equals(chr)) {
		return foundMissing ? DEFAULT_GNL : null;
	    }

	    if (outlier || !VAMPUtils.isOutlier(dataSet0.getDataProxy(n))) {
		//String gnl = VAMPUtils.getGNL(data[n]);
		String gnl = getGNL(dataSet0.getDataProxy(n));
		
		if (gnl == null ||
		    VAMPUtils.isNA(dataSet0.getDataProxy(n)) ||
		    VAMPUtils.isMissing(dataSet0.getDataProxy(n))) {
		    foundMissing = true;
		    continue;
		}

		/*
		  if (gnl.equals(AMPLICON) && defaultGNL != null)
		  return defaultGNL;
		*/

		// PH suggestion : 17/05/06
		if (gnl.equals(AMPLICON)) {
		    if (ignoreAmplicon) {
			foundMissing = true;
			continue;
		    }
		    /*
		      if (mergeGainedAmplicon) {
		      return defaultGNL;
		      }
		    */
		}

		return gnl;
	    }
	}

	//return null;
	//return DEFAULT_GNL;
	return foundMissing ? DEFAULT_GNL : null;
    }

    static boolean foundMissing(GraphElement dataSet0, int n, boolean outlier,
				int incr) throws Exception {

	// we know that data[n] is a bkp

	String chr = VAMPUtils.getChr(dataSet0.getDataProxy(n));
	RODataElementProxy d = dataSet0.getDataProxy(n);
	boolean foundMissing = false;
	for (; n < dataSet0.getProbeCount() && n >= 0; n += incr) {

	    if (!VAMPUtils.getChr(dataSet0.getDataProxy(n)).equals(chr)) {
		System.err.println("FOUND_MISSING NOT POSSIBLE " + chr + " vs. " + VAMPUtils.getChr(dataSet0.getDataProxy(n)));
		// not possible
		return true;
	    }

	    if (VAMPUtils.isOutlier(dataSet0.getDataProxy(n))) {
		if (!outlier) {
		    return true;
		}
	    }
	    
	    String gnl = getGNL(dataSet0.getDataProxy(n));
		
	    if (gnl == null ||
		VAMPUtils.isNA(dataSet0.getDataProxy(n)) ||
		VAMPUtils.isMissing(dataSet0.getDataProxy(n))) {
		return true;
	    }
	    
	    if (gnl.equals(AMPLICON)) {
		if (ignoreAmplicon) {
		    return true;
		}
	    }

	    return false;
	}

	return false;
    }

    String getChr(GraphElement dataSet0, int n, int incr, String defaultChr) throws Exception {
	boolean foundMissing = false;
	int length = dataSet0.getProbeCount();
	if (TRACK_BUG_CHR) {
	    for (; n < length && n >= 0; n += incr) {
		return VAMPUtils.getChr(dataSet0.getDataProxy(n));
	    }
	}
	else {
	    for (; n < length && n >= 0; n += incr) {
		if (!VAMPUtils.isNA(dataSet0.getDataProxy(n)) && !VAMPUtils.isMissing(dataSet0.getDataProxy(n)))
		    return VAMPUtils.getChr(dataSet0.getDataProxy(n));
		foundMissing = true;
	    }

	}
	return foundMissing ? defaultChr : null;
    }

    boolean isInBreakpoint(GraphElement dataSet0, int n, boolean outlier, String gnl) throws Exception {
	if (!isInBreakpoint(dataSet0, n, outlier))
	    return false;
	if (TRACE_BKP_DETAILS)
	    System.out.println("possibly in " + dataSet0.getDataProxy(n).getID() + " gnl " +
			       gnl + " -> " +
			       getGNL(dataSet0, n, outlier, 1) + " at " + n);
	return gnl.equals(getGNL(dataSet0, n, outlier, 1));
    }

    boolean isOutBreakpoint(GraphElement dataSet0, int n, boolean outlier, String gnl) throws Exception {
	if (!isOutBreakpoint(dataSet0, n, outlier))
	    return false;

	if (TRACE_BKP_DETAILS)
	    System.out.println("possibly out " + dataSet0.getDataProxy(n).getID() + " gnl " +
			       gnl + " -> " +
			       getGNL(dataSet0, n, outlier, -1) + " at " + n);
	return gnl.equals(getGNL(dataSet0, n, outlier, -1));
    }

    boolean isInBreakpoint(GraphElement dataSet0, int n, boolean outlier) throws Exception {
	//DataElement elem = data[n];
	RODataElementProxy elem = dataSet0.getDataProxy(n);

	String chr_n = VAMPUtils.getChr(elem);
	String chr_n1 = getChr(dataSet0, n-1, -1, chr_n);

	if (chr_n1 == null || !chr_n.equals(chr_n1)) {
	    //System.out.println("in CHR brk at " + elem.getID() + " " + chr_n + " " + chr_n1);
	    return true;
	}

	if (!TRY_NA) {
	    if (VAMPUtils.isNA(elem))
		return false;

	    if (VAMPUtils.isMissing(elem))
		return false;
	}

	String gnl_n = getGNL(dataSet0, n, outlier, 1);
	String gnl_n1 = getGNL(dataSet0, n-1, outlier, -1);

	if (TRACE_BKP_DETAILS)
	    System.out.println("IN: " + dataSet0.getDataProxy(n).getID() + " " + gnl_n + " " +
			       gnl_n1);

	if (gnl_n1 == null)
	    return true;

	if (gnl_n1.equals(DEFAULT_GNL))
	    return false;

	if (gnl_n == null)
	    return false;

	if (gnl_n.equals(gnl_n1))
	    return false;

	return true;
    }

    boolean isOutBreakpoint(GraphElement dataSet0, int n, boolean outlier) throws Exception {
	//DataElement elem = data[n];
	RODataElementProxy elem = dataSet0.getDataProxy(n);

	String chr_n = VAMPUtils.getChr(elem);
	String chr_n1 = getChr(dataSet0, n+1, 1, chr_n);

	if (chr_n1 == null || !chr_n.equals(chr_n1)) {
	    //System.out.println("out CHR brk at " + elem.getID() + " " + chr_n + " " + chr_n1);
	    return true;
	}

	if (!TRY_NA) {
	    if (VAMPUtils.isNA(elem))
		return false;
	    
	    if (VAMPUtils.isMissing(elem))
		return false;
	}

	String gnl_n = getGNL(dataSet0, n, outlier, -1);
	String gnl_n1 = getGNL(dataSet0, n+1, outlier, 1);

	if (TRACE_BKP_DETAILS)
	    System.out.println("OUT: " + dataSet0.getDataProxy(n).getID() + " " + gnl_n + " " +
			       gnl_n1);
	if (gnl_n1 == null)
	    return true;

	if (gnl_n1.equals(DEFAULT_GNL))
	    return false;

	if (gnl_n == null)
	    return false;

	if (gnl_n.equals(gnl_n1))
	    return false;

	return true;
    }

    private GraphElement first(Vector graphElements) {
	return (GraphElement)graphElements.get(0);
    }

    private Vector [] computeBreakpoints(Vector graphElements, String gnl) throws Exception {

	int size = graphElements.size();
	GraphElement dataSet0 = first(graphElements);
	//int length = dataSet0.getData().length;
	int length = dataSet0.getProbeCount();

	Vector inBkp_v = new Vector();
	Vector outBkp_v = new Vector();

	for (int n = 0; n < length; n++) {
	    Breakpoint in = null;
	    Breakpoint out = null;
	    RODataElementProxy d = dataSet0.getDataProxy(n);
	    if (support(d)) {
		for (int m = 0; m < size; m++) {
		    GraphElement dataSet = (GraphElement)graphElements.get(m);
		    if (isInBreakpoint(dataSet, n, outlier, gnl)) {
			if (in == null)
			    in = new Breakpoint(Breakpoint.IN, n, gnl);
			in.addDataSet(dataSet);
		    }

		    if (isOutBreakpoint(dataSet, n, outlier, gnl)) {
			if (out == null)
			    out = new Breakpoint(Breakpoint.OUT, n, gnl);
			out.addDataSet(dataSet);
		    }
		}

		if (TRACE_BKP && size > 1) {
		    if (in != null)
			System.out.println("in breakpoint " +
					   in.getStringSupport() + " at " +
					   dataSet0.getDataProxy(n).getID() + " " +
					   ((GraphElement)graphElements.get(1)).getDataProxy(n).getID() +
					   " gnl=" + gnl);

		    
		    if (out != null)
			System.out.println("out breakpoint " +
					   out.getStringSupport() + " at " +
					   dataSet0.getDataProxy(n).getID() + " " +
					   ((GraphElement)graphElements.get(1)).getDataProxy(n).getID() +
					   " gnl=" + gnl);

		}
	    }

	    inBkp_v.add(in);
	    outBkp_v.add(out);
	}

	return new Vector[]{inBkp_v, outBkp_v};
    }

    private Vector [] computeCumulBreakpoints(Vector graphElements, String gnl,
					      Vector bkp_v[]) throws Exception {
	GraphElement dataSet0 = first(graphElements);
	int length = dataSet0.getProbeCount();
	//DataElement data[] = dataSet0.getData();

	Vector inBkp_v = bkp_v[0];
	Vector inCumBkp_v = new Vector();
	HashMap inCumBkp_map = new HashMap();

	for (int n = length-1; n >= 0; n--) {
	    Breakpoint bkpIn = Breakpoint.mergeInBkp(inBkp_v, inCumBkp_map, dataSet0, n, toleration);

	    if (TRACE_BKP) {
		if (bkpIn != null)
		    System.out.println("in cumul breakpoint " +
				       bkpIn.getStringSupport() + " at " +
				       dataSet0.getDataProxy(bkpIn.getInd()).getID() +
				       " gnl=" + gnl);

	    }

	    inCumBkp_v.add(bkpIn);
	}


	Vector tmp_v = new Vector();
	for (int n = inCumBkp_v.size() - 1; n >= 0; n--) {
	    tmp_v.add(inCumBkp_v.get(n));
	}

	inCumBkp_v = tmp_v;

	Vector outBkp_v = bkp_v[1];
	Vector outCumBkp_v = new Vector();
	HashMap outCumBkp_map = new HashMap();

	for (int n = 0; n < length; n++) {
	    Breakpoint bkpOut = Breakpoint.mergeOutBkp(outBkp_v, outCumBkp_map, dataSet0, n, toleration);

	    if (TRACE_BKP) {
		if (bkpOut != null)
		    System.out.println("out cumul breakpoint " +
				       bkpOut.getStringSupport() + " at " +
				       dataSet0.getDataProxy(bkpOut.getInd()).getID() +
				       " gnl=" + gnl);
	    }

	    outCumBkp_v.add(bkpOut);
	}

	return new Vector[]{inCumBkp_v, outCumBkp_v};
    }


    private Vector [] computeRecurrentBreakpoints(Vector graphElements,
						  String gnl,
						  Vector cumulBkp_v[],
						  boolean computeMinimal) throws Exception {

	GraphElement dataSet0 = first(graphElements);
	//int length = dataSet0.getData().length;
	int length = dataSet0.getProbeCount();
	Vector inCumBkp_v = cumulBkp_v[0];
	Vector outCumBkp_v = cumulBkp_v[1];
	boolean all_bkp = (computeMinimal ? false : NEW_RECREG_ALGO);

	// EV/PH patch 23/07/07
	//int TOLERATION = toleration;
	int TOLERATION = 0;
	// EOP

	// search recurrent breakpoints
	Vector recInBkp_v = new Vector();	
	//DataElement data[] = dataSet0.getData();
	for (int n = 0; n < length; n++) {
	    Breakpoint inCumBkp = (Breakpoint)inCumBkp_v.get(n);
	    String chr = VAMPUtils.getChr(dataSet0.getDataProxy(n));
	    int supCnt = (inCumBkp == null ? 0 : inCumBkp.getSupportCount());
	    if (supCnt >= min_bkp) {
		int j = 1;
		for (; j <= TOLERATION; j++) {
		    if (n + j >= length || !VAMPUtils.getChr(dataSet0.getDataProxy(n+j)).equals(chr))
			break;
		    Breakpoint inCumBkp2 = (Breakpoint)inCumBkp_v.get(n+j);
		    if (TRACE_BKP)
			System.out.println(dataSet0.getDataProxy(n+j).getID() + ", oldSupCnt: " +
					   supCnt + ", curSupCnt: " + (inCumBkp2 != null ?
								       inCumBkp2.getSupportCount() : -1));

		    if (inCumBkp2 == null ||
			inCumBkp2.getSupportCount() < supCnt) {
			boolean found = false;
			for (int k = j-2; k >= 1; k--) {
			    Breakpoint inCumBkp3 = (Breakpoint)inCumBkp_v.get(n+k);
			    if (TRACE_BKP)
				System.out.print("supp: " +
						 inCumBkp3.getSupportCount() + " vs. " + supCnt);

			    if (inCumBkp3.getSupportCount() != supCnt) {
				j = k+2;
				if (TRACE_BKP)
				    System.out.println("in j -> " + j);
				found = true;
				break;
			    }

			    if (TRACE_BKP)
				System.out.println("in <-- " + dataSet0.getDataProxy(n).getID());
			}

			/*
			  if (!found)
			  j = 1;
			*/
			if (TRACE_BKP)
			    System.out.println("break: j " + j);
			break;
		    }
		    supCnt = inCumBkp2.getSupportCount();
		}

		if (!hasBkp(recInBkp_v, n + j - 1)) {
		    Breakpoint bkp = (Breakpoint)inCumBkp_v.get(n + j - 1);
		    if (TRACE_BKP)
			System.out.println("recurrent in bkp " + bkp.getStringSupport() +
					   " at " + dataSet0.getDataProxy(bkp.getInd()).getID() + " chr " + VAMPUtils.getChr(dataSet0.getDataProxy(bkp.getInd())));
		    if (all_bkp) {
			if (bkp != null)
			    bkp.setRecurrent(true);
		    }
		    else
			recInBkp_v.add(bkp);
		}
		n += TOLERATION;
	    }
	}

	Vector recOutBkp_v = new Vector();	
	for (int n = 0; n < length; n++) {
	    Breakpoint outCumBkp = (Breakpoint)outCumBkp_v.get(n);
	    String chr = VAMPUtils.getChr(dataSet0.getDataProxy(n));
	    int supCnt = (outCumBkp == null ? 0 : outCumBkp.getSupportCount());
	    if (supCnt >= min_bkp) {
		int j = 1;
		for (; j <= TOLERATION; j++) {
		    if (n - j < 0 || !VAMPUtils.getChr(dataSet0.getDataProxy(n-j)).equals(chr))
			break;
		    Breakpoint outCumBkp2 = (Breakpoint)outCumBkp_v.get(n-j);
		    if (outCumBkp2 == null ||
			outCumBkp2.getSupportCount() < supCnt) {
			boolean found = false;
			for (int k = j-2; k >= 1; k--) {
			    Breakpoint outCumBkp3 = (Breakpoint)outCumBkp_v.get(n-k);
			    if (outCumBkp3.getSupportCount() != supCnt) {
				j = k+2;
				found = true;
				break;
			    }
			    if (TRACE_BKP)
				System.out.println("out --> " + dataSet0.getDataProxy(n).getID());
			}

			/*
			  if (!found)
			  j = 1;
			*/

			break;
		    }
		    supCnt = outCumBkp2.getSupportCount();
		}

		if (!hasBkp(recOutBkp_v, n - j + 1)) {
		    Breakpoint bkp = (Breakpoint)outCumBkp_v.get(n - j + 1);
		    if (TRACE_BKP)
			System.out.println("recurrent out bkp " + bkp.getStringSupport() +
					   " at " + dataSet0.getDataProxy(bkp.getInd()).getID() + " chr " + VAMPUtils.getChr(dataSet0.getDataProxy(bkp.getInd())));
		    // TBD : ne pas ajouter un BKP au meme endroit !
		    if (all_bkp) {
			if (bkp != null)
			    bkp.setRecurrent(true);
		    }
		    else
			recOutBkp_v.add(bkp);
		}
		n += TOLERATION;
	    }
	}

	if (!all_bkp && TRACE_BKP) {
	    System.out.println(getGNLString(gnl) + " recurrent in breakpoint count: " + recInBkp_v.size());
	    System.out.println(getGNLString(gnl) + " recurrent out breakpoint count: " + recOutBkp_v.size());
	}

	if (all_bkp) {
	    for (int n = 0; n < inCumBkp_v.size(); n++) {
		if (inCumBkp_v.get(n) != null)
		    recInBkp_v.add(inCumBkp_v.get(n));
	    }

	    for (int n = 0; n < outCumBkp_v.size(); n++) {
		if (outCumBkp_v.get(n) != null)
		    recOutBkp_v.add(outCumBkp_v.get(n));
	    }
	}

	return new Vector[] {recInBkp_v, recOutBkp_v};
    }

    private Vector [] computeRecurrentBreakpoints(Vector graphElements, String gnl, boolean computeMinimal) throws Exception {
	if (TRACE_BKP)
	    System.out.println("");

	Vector bkp_v[] = computeBreakpoints(graphElements, gnl);

	if (TRACE_BKP)
	    System.out.println("");

	Vector cumulBkp_v[] = computeCumulBreakpoints(graphElements, gnl, bkp_v);

	if (TRACE_BKP)
	    System.out.println("");

	Vector recBkp_v[] = computeRecurrentBreakpoints(graphElements, gnl, cumulBkp_v, computeMinimal);
	return recBkp_v;
    }

    private LinkedList computeMinimalRegions(GlobalContext globalContext,
					     GraphPanelSet panel_set,
					     GraphPanel panel,
					     Vector graphElements, String gnl,
					     Vector bkp_v) throws Exception {
	GraphElement ds0 = (GraphElement)graphElements.get(0);
	//DataElement data[] = ds0.getData();

	int size = bkp_v.size();

	Vector regions = new Vector();
	/*
	Vector bkp_v = new Vector();
	bkp_v.addAll(ori_bkp_v);
	for (; bkp_v.size() > 0; ) {
	*/
	for (int n = 0; n < size; n++) {
	    Breakpoint bkp = (Breakpoint)bkp_v.get(n);
	    if (bkp.isIN()) {
		String inChr = VAMPUtils.getChr(ds0.getDataProxy(bkp.ind));
		if (n+1 < size) {
		    Breakpoint bkp2 = (Breakpoint)bkp_v.get(n+1);
		    if (bkp2.isOUT() &&
			inChr.equals(VAMPUtils.getChr(ds0.getDataProxy(bkp2.ind)))) {
			Vector supp = Breakpoint.getMinimalSupport
			    (graphElements, bkp, bkp2, outlier);

			if (supp.size() >= min_alt &&
			    is_included(bkp.getSupport(), supp) &&
			    is_included(bkp2.getSupport(), supp)) {
			    regions.add(new MinimalRegion(globalContext, bkp, bkp2, supp, outlier));
			}
			else {
			    // 28/03/07: shoud add breakpoints for a second pass
			}
		    }
		}
	    }
	}
	    
	regions = mergeConsecutiveRegions(regions);

	// ADDED: 17/09/08 for testing
	regions = postFilterMinimalRegions(regions);

	LinkedList reg_list = new LinkedList();
	    
	for (int n = 0; n < regions.size(); n++) {
	    MinimalRegion r = (MinimalRegion)regions.get(n);
	    r.makeRegion(panel_set, panel, reg_list, graphElements, this);
	    if (TRACE_REG)
		System.out.println(r);
	}

	postAddRegions(reg_list, getGNLString(gnl));
	return reg_list;
    }

    private LinkedList computeRecurrentRegions(GlobalContext globalContext, GraphPanelSet panel_set,
					       GraphPanel panel,
					       Vector graphElements, String gnl,
					       Vector bkp_v) throws Exception {
	GraphElement ds0 = (GraphElement)graphElements.get(0);
	//DataElement data[] = ds0.getData();

	int size = bkp_v.size();

	if (TRACE_BKP) {
	    System.out.println("\nComputing recurrent regions for GNL " + gnl);
	    for (int n = 0; n < size; n++) {
		Breakpoint bkp = (Breakpoint)bkp_v.get(n);
		System.out.println(bkp.toString());
	    }
	}

	Vector regions = new Vector();
	for (int n = 0; n < size; n++) {
	    Breakpoint bkp = (Breakpoint)bkp_v.get(n);
	    if (bkp.isIN() && bkp.getSupportCount() >= min_alt) {
		// Note: bkp.getSupportCount() >= min_alt <=> isRecurrent()
		String inChr = VAMPUtils.getChr(ds0.getDataProxy(bkp.ind));
		for (int m = n+1; m < size; m++) {
		    Breakpoint bkp2 = (Breakpoint)bkp_v.get(m);
		    if (!inChr.equals(VAMPUtils.getChr(ds0.getDataProxy(bkp2.ind))))
			break;
		    if (NEW_RECREG_ALGO) {
			//System.out.println("OUT? " + bkp2.isOUT() + " " + bkp2.isRecurrent() + " " + m + "/" + size);

			if (bkp2.isOUT() && bkp2.isRecurrent()) {
			    //Vector inter = Breakpoint.getSupport(bkp, bkp2);
			    Vector inter = Breakpoint.getRecurrentSupport(bkp_v, n, m, outlier, min_alt, toleration);
			    if (inter.size() >= min_alt) {
				regions.add(new RecurrentRegion(globalContext, bkp, bkp2, inter));
			    }
			    // disconnected 20/07/07
			    //break;
			}

			// disconnected 27/07/07
			/*
			  if (bkp2.isIN()) {
			    bkp.suppressFromSupport(bkp2.getSupport());
			    if (bkp.getSupportCount() < min_alt)
				break;
			}
			*/
		    }
		    else {
			if (bkp2.isOUT()) {
			    Vector inter = Breakpoint.getSupport(bkp, bkp2);
			    if (inter.size() >= min_alt) {
				regions.add(new RecurrentRegion(globalContext, bkp, bkp2, inter));
			    }
			    break;
			}
		    }
		}
	    }
	}
	    
	regions = postFilterRecurrentRegions(regions);

	if (TRACE_BKP) {
	    System.out.println("\nRecurrent regions for GNL " + gnl);
	}

	LinkedList reg_list = new LinkedList();
	    
	for (int n = 0; n < regions.size(); n++) {
	    RecurrentRegion r = (RecurrentRegion)regions.get(n);
	    if (TRACE_REG)
		System.out.println(r);
	    r.makeRegion(panel_set, panel, reg_list, graphElements, this);
	}

	postAddRegions(reg_list, getGNLString(gnl));
	return reg_list;
    }


    private Vector postFilterRecurrentRegions(Vector regions) {

	int rgsize = regions.size();
	HashMap reg_map = new HashMap();

	if (TRACE_REG) {
	    System.out.println("\nBefore recurrent region filtering process:");
	}

	for (int n = 0; n < rgsize; n++) {
	    RecurrentRegion r = (RecurrentRegion)regions.get(n);
	    reg_map.put(r, new Boolean(true));
	    if (TRACE_REG) {
		System.out.println(r);
	    }
	}

	if (TRACE_REG) {
	    System.out.println("\nFiltering recurrent region process:");
	}

	Collections.sort(regions, new AlteredRegionComparator(false));

	// out breakpoints
	for (int n = 0; n < rgsize - 1; n++) {
	    RecurrentRegion r = (RecurrentRegion)regions.get(n);
	    for (int m = n+1; m < rgsize; m++) {
		RecurrentRegion r_n = (RecurrentRegion)regions.get(m);
		if (r.bkp2.ind - r_n.bkp2.ind > 1) {
		    break;
		}

		if (r.bkp2.ind - r_n.bkp2.ind == 1) {
		    if (TRACE_REG) {
			System.out.print("adjacent regions: " + r + " -- " +
					 r_n);
		    }

		    if (is_included(r_n.getSupport(), r.getSupport())) {
			if (TRACE_REG) {
			    System.out.println(" (second region filtered)");
			}
			reg_map.put(r_n, new Boolean(false));
		    }
		    else if (TRACE_REG) {
			System.out.println(" (no filtering)");
		    }
		}
	    }
	}

	Collections.sort(regions, new AlteredRegionComparator(true));

	// in breakpoints
	for (int n = 0; n < rgsize - 1; n++) {
	    RecurrentRegion r = (RecurrentRegion)regions.get(n);

	    for (int m = n+1; m < rgsize; m++) {
		RecurrentRegion r_n = (RecurrentRegion)regions.get(m);
		if (r_n.bkp1.ind - r.bkp1.ind > 1) {
		    break;
		}

		if (r_n.bkp1.ind - r.bkp1.ind == 1) {
		    if (TRACE_REG) {
			System.out.print("adjacent regions: " + r + " -- " +
					 r_n);
		    }

		    if (is_included(r_n.getSupport(), r.getSupport())) {
			if (TRACE_REG) {
			    System.out.println(" (second region filtered)");
			}
			reg_map.put(r_n, new Boolean(false));
		    }
		    else if (TRACE_REG) {
			System.out.println(" (no filtering)");
		    }
		}
	    }
	}

	if (TRACE_REG) {
	    System.out.println("\nFiltering results:");
	}

	Vector regions_r = new Vector();

	for (int n = 0; n < rgsize; n++) {
	    RecurrentRegion r = (RecurrentRegion)regions.get(n);
	    Boolean b = (Boolean)reg_map.get(r);
	    if (b.booleanValue()) {
		regions_r.add(r);
	    }
	    else if (TRACE_REG) {
		System.out.println(r + " filtered");
	    }
	}

	return regions_r;
    }

    static String makeCytogenName(Chromosome chr, Band band) {
	return chr.getName() + band.getArm() + band.getName();
    }

    static boolean isMinimal(String type) {
	return type.equals(VAMPConstants.MINIMAL_REGION_TYPE);
    }

    static void makeCytogen(GlobalContext globalContext,
			    String type, int cnt,
			    String organism, Region region, String gnl,
			    String b_schr, String e_schr,
			    double begin_x, double end_x,
			    boolean mergeChr) {
	Chromosome chr = VAMPUtils.getChromosome(globalContext, organism, b_schr);

	long offset_pos = (mergeChr ? 0 : chr.getOffsetPos());
	//System.out.println("get_band begin: " + chr.getName() + " " + (long)begin_x + " " + (long)offset_pos);
	Band s_band = chr.getBand((long)begin_x+offset_pos);
	region.setPropertyValue(CytogBeginProp,
				makeCytogenName(chr, s_band));

	chr = VAMPUtils.getChromosome(globalContext, organism, e_schr);
	String name = isMinimal(type) ? "MinReg" :
	    "RecReg";
	name += "_" + getCGNLString(gnl) + "_";
	name += chr.getName();
	//System.out.println("get_band end: " + chr.getName() + " " + (long)begin_x + " " + (long)offset_pos);
	Band e_band = chr.getBand((long)end_x+offset_pos);
	/*
	  if (s_band.equals(e_band)) {
	  name += s_band.getArm() + s_band.getName();
	  }
	  else if (s_band.getArm().equals(e_band.getArm()))
	  name += s_band.getArm();
	*/

	name += s_band.getArm() + s_band.getName();
	if (!s_band.equals(e_band))
	    name += "-" + chr.getName() + e_band.getArm() + e_band.getName();

	name += "_" + cnt;

	region.setPropertyValue(CytogEndProp,
				makeCytogenName(chr, e_band));

	region.setPropertyValue(VAMPProperties.NameProp, name);
    }

    static void makeCytogen(GlobalContext globalContext,
			    String organism, Mark mark,
			    String b_schr, String e_schr,
			    double pos_x) {
	Chromosome chr = VAMPUtils.getChromosome(globalContext, organism, b_schr);

	Band band = chr.getBand((long)pos_x);
	mark.setPropertyValue(CytogProp,
			      makeCytogenName(chr, band));
    }

    static class BreakpointComparator implements Comparator {

	public int compare(Object o1, Object o2) {
	    Breakpoint bkp1 = (Breakpoint)o1;
	    Breakpoint bkp2 = (Breakpoint)o2;
	    if (bkp1.ind < bkp2.ind)
		return -1;

	    if (bkp1.ind > bkp2.ind)
		return 1;

	    if (bkp1.type < bkp2.type)
		return -1;

	    if (bkp1.type > bkp2.type)
		return 1;

	    return bkp1.indice < bkp2.indice ? -1 : 1;
	}
    }

    static class AlteredRegionComparator implements Comparator {

	boolean cmp_in;

	AlteredRegionComparator(boolean cmp_in) {
	    this.cmp_in = cmp_in;
	}

	public int compare(Object o1, Object o2) {
	    AlteredRegion r1 = (AlteredRegion)o1;
	    AlteredRegion r2 = (AlteredRegion)o2;

	    if (cmp_in) {
		return r1.bkp1.ind - r2.bkp1.ind;
	    }

	    return r2.bkp2.ind - r1.bkp2.ind;
	}
    }

    static double getWideBeginX(GraphElement dataSet, int ind) throws Exception {
	double startx = dataSet.getDataProxy(ind).getPanGenPosX(dataSet);

	if (ind == 0 || !VAMPUtils.getChr(dataSet.getDataProxy(ind)).equals(VAMPUtils.getChr(dataSet.getDataProxy(ind-1))))
	    return startx;

	double endx = dataSet.getDataProxy(ind-1).getPanGenPosX(dataSet) + getPosSize(dataSet, dataSet.getDataProxy(ind-1));
	return startx < endx ? startx : endx;
    }

    static double getBeginX(GraphElement dataSet, int ind,
			    boolean widen_regions) throws Exception {
	if (widen_regions)
	    return getWideBeginX(dataSet, ind);
	return dataSet.getDataProxy(ind).getPanGenPosX(dataSet);
    }

    static double getWideEndX(GraphElement dataSet, int ind) throws Exception {
	double endx = dataSet.getDataProxy(ind).getPanGenPosX(dataSet) + getPosSize(dataSet, dataSet.getDataProxy(ind));
	if (ind == dataSet.getProbeCount() - 1 ||
	    !VAMPUtils.getChr(dataSet.getDataProxy(ind)).equals(VAMPUtils.getChr(dataSet.getDataProxy((ind+1)))))
	    return endx;

	double nextx = dataSet.getDataProxy(ind+1).getPanGenPosX(dataSet);
	return nextx < endx ? endx : nextx;
    }

    static double getEndX(GraphElement dataSet, int ind,
			  boolean widen_regions) throws Exception {
	if (widen_regions)
	    return getWideEndX(dataSet, ind);
	return dataSet.getDataProxy(ind).getPanGenPosX(dataSet) + getPosSize(dataSet, dataSet.getDataProxy(ind));
    }

    static final Color gColors[], lColors[], aColors[], nColors[];

    static final int COLOR_CNT = 4;

    static {
	gColors = new Color[COLOR_CNT];
	lColors = new Color[COLOR_CNT];
	aColors = new Color[COLOR_CNT];
	nColors = new Color[COLOR_CNT];

	gColors[0] = new Color(0xff7777);
	gColors[1] = new Color(0xff9999);
	gColors[2] = new Color(0xffbbbb);
	gColors[3] = new Color(0xffdddd);

	lColors[0] = new Color(0x77ff77);
	lColors[1] = new Color(0x99ff99);
	lColors[2] = new Color(0xbbffbb);
	lColors[3] = new Color(0xeeffee);

	aColors[0] = new Color(0x000088);
	aColors[1] = new Color(0x0000bb);
	aColors[2] = new Color(0x000088);
	aColors[3] = new Color(0x0000aa);

	nColors[0] = new Color(0xffff00);
	nColors[1] = new Color(0xffff77);
	nColors[2] = new Color(0xffffaa);
	nColors[3] = new Color(0xffffcc);
    }

    static Region makeRegion(GraphPanelSet panel_set,
			     GraphPanel panel, LinkedList regions,
			     double pos_begin, double pos_end,
			     String gnl) {

	if (pos_begin == pos_end)
	    return null;

	Mark begin, end;
	begin = panel_set.addMark(panel.getWhich(), pos_begin);
	end = panel_set.addMark(panel.getWhich(), pos_end);

	Region region = new Region(begin, end);

	if (gnl.equals(GAINED))
	    region.setColor(gColors[regions.size() % COLOR_CNT]);
	else if (gnl.equals(LOST))
	    region.setColor(lColors[regions.size() % COLOR_CNT]);
	else if (gnl.equals(AMPLICON))
	    region.setColor(aColors[regions.size() % COLOR_CNT]);
	else if (gnl.equals(NORMAL))
	    region.setColor(nColors[regions.size() % COLOR_CNT]);
	panel_set.addRegion(panel.getWhich(), region);
	regions.add(region);
	return region;
    }

    /*
      Mark makeMark(GraphPanelSet panel_set,
      GraphPanel panel, LinkedList marks,
      double pos, String gnl) {

      Mark mark = new Mark(pos);

      if (gnl.equals(GAINED))
      mark.setColor(VAMPResources.getColor(VAMPResources.GNL_GAINED_FG));
      else if (gnl.equals(LOST))
      mark.setColor(VAMPResources.getColor(VAMPResources.GNL_LOST_FG));
      else if (gnl.equals(AMPLICON))
      mark.setColor(VAMPResources.getColor(VAMPResources.GNL_AMPLICON_FG));
      else if (gnl.equals(NORMAL))
      mark.setColor(VAMPResources.getColor(VAMPResources.GNL_NORMAL_FG));

      panel_set.addMark(panel.getWhich(), mark);
      marks.add(mark);
      return mark;
      }
    */

    static class AlteredRegion {

	Breakpoint bkp1, bkp2;
	Vector supp;
	GlobalContext globalContext;

	protected AlteredRegion(GlobalContext globalContext) {
	    this.globalContext = globalContext;
	}

	boolean includeRegion(AlteredRegion r) {
	    return r.bkp1.ind >= bkp1.ind && r.bkp2.ind <= bkp2.ind;
	}

	public Vector getSupport() {
	    return supp;
	}

	protected void makeRegionPerform(String type, GraphPanelSet panel_set,
					 GraphPanel panel,
					 LinkedList regions,
					 Vector graphElements,
					 GenomeAlterationOP op) throws Exception {
	

	    GraphElement ds0 = (GraphElement)graphElements.get(0);
	    //DataElement data[] = ds0.getData();
	    double begin_x = getBeginX(ds0, bkp1.ind, op.widen_regions);
	    double end_x = getEndX(ds0, bkp2.ind, op.widen_regions);

	    Cytoband cytoband = MiniMapDataFactory.getCytoband
		(GlobalContext.getLastInstance(), "Human");
	    /*
	      System.out.println(type + " " + (long)begin_x + ":" + (long)end_x + "  chrs " +
	      VAMPUtils.getChr(data[bkp1.ind]) + " " +
	      VAMPUtils.getChr(data[bkp2.ind]));
	    */
	    Chromosome chr_b = cytoband.getChromosome((long)begin_x);
	    Chromosome chr_e1 = cytoband.getChromosome((long)ds0.getDataProxy(bkp2.ind).getPanGenPosX(ds0));
	    Chromosome chr_e2 = cytoband.getChromosome((long)end_x);

	    /*
	      System.out.println("chhhhhh: " +
	      chr_b.getName() + " " + chr_e1.getName() + " " + chr_e2.getName());
	    */

	    //if (findRegion(regions, begin_x, end_x))
	    //return;

	    Region region = makeRegion(panel_set, panel, regions,
				       begin_x, end_x, bkp1.gnl);
	    
	    if (region == null)
		return;

	    region.setPropertyValue(VAMPProperties.TypeProp, type);
	    
	    region.setPropertyValue(OverlappedProp,
				    Utils.toString(supp.size()) + " / " +
				    Utils.toString(graphElements.size()));

	    region.setPropertyValue(VAMPProperties.GNLProp, getGNLString(bkp1.gnl));

	    /*
	    // 21/01/05: bug: b+1 n'est pas bon si b == debut chr.
	    // 21/01/05: bug: e-1 n'est pas bon si e == fin chr.
	    // 31/01/05: corrected !
	    int b1, e1;
	    if (e - b > 1) { // added this test 21/12/05
	    b1 = b+1;
	    if (b == 0 ||
	    !VAMPUtils.getChr(data[b-1]).equals(VAMPUtils.getChr(data[b])))
	    b1 = b;
	    
	    e1 = e-1;
	    if (e == data.length - 1 ||
	    !VAMPUtils.getChr(data[e+1]).equals(VAMPUtils.getChr(data[e])))
	    e1 = e;
	    }
	    else {
	    b1 = b;
	    e1 = e;
	    }
	    */

	    int b1 = bkp1.ind;
	    int e1 = bkp2.ind;

	    
	    ds0.getDataProxy(b1).complete(ds0);
	    ds0.getDataProxy(e1).complete(ds0);

	    String s = (String)ds0.getDataProxy(b1).getID();
	    String nmc = (String)ds0.getDataProxy(b1).getPropertyValue(VAMPProperties.NmcProp);
	    if (nmc != null)
		s += " [" + nmc + "]";
	    region.setPropertyValue(VAMPProperties.CloneBeginProp, s);
	    s = (String)ds0.getDataProxy(e1).getID();
	    nmc = (String)ds0.getDataProxy(e1).getPropertyValue(VAMPProperties.NmcProp);
	    if (nmc != null)
		s += " [" + nmc + "]";
	    region.setPropertyValue(VAMPProperties.CloneEndProp, s);
	    // TBD !!
	    //region.setPropertyValue(DataProp, data);
	    region.setPropertyValue(VAMPProperties.OrganismProp, VAMPUtils.getOS(ds0));
	    region.setPropertyValue(VAMPProperties.GlobalContextProp,
				    panel_set.getView().getGlobalContext());
	    region.setPropertyValue(VAMPProperties.VectorArrayProp, graphElements);
	    region.setPropertyValue(UserModifiedProp, new Boolean(false));

	    int params[] = new int[PARAMS_CNT];
	    params[MIN_BKP_I] = op.min_bkp;
	    params[MIN_ALT_I] = op.min_alt;
	    params[TOLERATION_I] = (isMinimal(type) ? -1 : op.toleration);
	    params[OUTLIER_I] = op.outlier ? 1 : 0;
	    params[EXTENDED_NA_I] = op.extended_NA ? 1 : 0;
	    params[WIDEN_REGIONS_I] = op.widen_regions ? 1 : 0;
	    params[ALT_MASK_I] = op.alt_mask;
	    params[SCOPE_MASK_I] = op.scope_mask;

	    region.setPropertyValue(HParamsProp,
				    params);

	    region.setPropertyValue(ParamsProp,
				    "Min Alterations: " + op.min_alt + "\n" +
				    "Min Breakpoints: " + op.min_bkp + "\n" +
				    (!isMinimal(type) ? ("Toleration: " + op.toleration + "\n") : "") +
				    "Outlier: " + op.outlier + "\n" +
				    "ExtendedNA: " + op.extended_NA + "\n" +
				    "WideRegions: " + op.widen_regions);

	    /*
	      int sz = supp.size();

	      s = "";
	      for (int n = 0; n < sz; n++) {
	      GraphElement graphElem = (GraphElement)supp.get(n);
	      s += (n > 0 ? "\n" : "") + graphElem.getID();
	      }
	    */

	    region.setPropertyValue(SupportProp, Breakpoint.getStringNLSupport(supp));
	    region.setPropertyValue(SupportVProp, supp);

	    // disconnected 6/04/06
	    /*
	      region.getBegin().setTrigger(new MarkTrigger() {
	      public double setLocation(Mark mark, boolean mergeChr,
	      double posx) {
	      return AlterationOP.setRegionLocation(mark, mergeChr,
	      posx);
	      }
	      });

	      region.getEnd().setTrigger(new MarkTrigger() {
	      public double setLocation(Mark mark, boolean mergeChr,
	      double posx) {
	      return AlterationOP.setRegionLocation(mark, mergeChr,
	      posx);
	      }
	      });
	    */

	    double offset_x = getOffsetX(globalContext, ds0, ds0.getDataProxy(b1)); // rec: [b1]
	    region.setPropertyValue(VAMPProperties.PositionChrBeginProp,
				    Utils.toString((long)(begin_x - offset_x)));

	    region.setPropertyValue(VAMPProperties.PositionChrEndProp,
				    Utils.toString((long)(end_x - offset_x)));
	    
	    String b_schr = VAMPUtils.getChr(ds0.getDataProxy(b1));
	    String e_schr = VAMPUtils.getChr(ds0.getDataProxy(e1));
	    region.setPropertyValue(VAMPProperties.ChromosomeProp, b_schr);

	    makeCytogen(panel_set.getView().getGlobalContext(),
			type, regions.size(),
			VAMPUtils.getOS(ds0),
			region, bkp1.gnl, b_schr, e_schr, begin_x, end_x,
			VAMPUtils.isMergeChr((GraphElement)graphElements.get(0)));

	    ds0.getDataProxy(b1).release();
	    ds0.getDataProxy(e1).release();
	}
    }

    static double getOffsetX(GlobalContext globalContext, GraphElement ds, RODataElementProxy data) {
	Double d = (Double)data.getPropertyValue(VAMPProperties.MergeOffsetProp);
	if (d != null) {
	    return d.doubleValue();
	}

	String os = VAMPUtils.getOS(ds);

	if (os != null) {
	    Cytoband cytoband = MiniMapDataFactory.getCytoband(globalContext, os);
	    if (cytoband != null) {
		Chromosome chr = cytoband.getChromosome(VAMPUtils.getChr(data));
		if (chr != null) {
		    return chr.getOffsetPos();
		}
	    }
	}

	return 0;
    }

    static class MinimalRegion extends AlteredRegion {

	MinimalRegion(GlobalContext globalContext, Breakpoint bkp1, Breakpoint bkp2, Vector supp, boolean outlier) {
	    super(globalContext);
	    this.bkp1 = bkp1;
	    this.bkp2 = bkp2;
	    this.supp = supp;
	}

	MinimalRegion extend(boolean outlier) throws Exception {
	    bkp1.extendLeft(supp, outlier);
	    bkp2.extendRight(supp, outlier);
	    return this;
	}

	void makeRegion(GraphPanelSet panel_set,
			GraphPanel panel,
			LinkedList regions,
			Vector graphElements,
			GenomeAlterationOP op) throws Exception {
	    makeRegionPerform(VAMPConstants.MINIMAL_REGION_TYPE, panel_set, panel, regions, graphElements, op);
	}

	public String toString() {
	    return "minimal region: " + bkp1.toString() + " <-> " + bkp2.toString();
	}
    }

    static class RecurrentRegion extends AlteredRegion {

	RecurrentRegion(GlobalContext globalContext, Breakpoint bkp1, Breakpoint bkp2, Vector supp) {
	    super(globalContext);
	    this.bkp1 = bkp1;
	    this.bkp2 = bkp2;
	    this.supp = supp;
	}

	void makeRegion(GraphPanelSet panel_set,
			GraphPanel panel,
			LinkedList regions,
			Vector graphElements,
			GenomeAlterationOP op) throws Exception {
	    makeRegionPerform(VAMPConstants.RECURRENT_REGION_TYPE, panel_set, panel, regions, graphElements, op);
	}

	public String toString() {
	    return "recurrent region: " +  Breakpoint.getStringSupport(supp) +
		" {" + bkp1.toString() + " <-> " + bkp2.toString() + "}";
	}
    }

    static class Breakpoint {

	static long indice = 1;
	static final int IN = 1;
	static final int OUT = 2;

	private int ind, type;
	private Vector dataSet_v;
	private String gnl;
	private boolean is_recurrent;

	Breakpoint(int type, int ind, String gnl) {
	    this.type = type;
	    this.ind = ind;
	    this.gnl = gnl;
	    is_recurrent = false;
	    dataSet_v = new Vector();
	    indice++;
	}

	boolean isRecurrent() {
	    return is_recurrent;
	}

	void setRecurrent(boolean is_recurrent) {
	    this.is_recurrent = is_recurrent;
	}

	void addDataSet(GraphElement dataSet) {
	    dataSet_v.add(dataSet);
	}

	int getInd() {return ind;}
	boolean isIN() {return type == IN;}
	boolean isOUT() {return type == OUT;}

	public String toString() {
	    //return  (isRecurrent() ? "" : "not ") + "recurrent " + (type == OUT ? "out" : "in") + " breakpoint " + getStringSupport() + " at #" + ind + " gnl " + gnl;
	    return  "recurrent " + (type == OUT ? "out" : "in") + " breakpoint " + getStringSupport() + " at #" + ind + " gnl " + gnl;
	}

	Vector getSupport() {return dataSet_v;}
	int getSupportCount() {return dataSet_v.size();}

	static void suppressFromSupport(Vector i_supp, GraphElement dataSet) {
	    Vector supp = new Vector();
	    supp.add(dataSet);
	    suppressFromSupport(i_supp, supp);	    
	}

	static void suppressFromSupport(Vector i_supp, Vector supp) {
	    int sz = supp.size();

	    for (int n = 0; n < sz; n++) {
		if (i_supp.contains(supp.get(n))) {
		    i_supp.remove(supp.get(n));
		}
	    }
	}

	void suppressFromSupport(Vector supp) {
	    suppressFromSupport(dataSet_v, supp);
	    /*
	    int sz = supp.size();

	    for (int n = 0; n < sz; n++) {
		if (dataSet_v.contains(supp.get(n))) {
		    dataSet_v.remove(supp.get(n));
		}
	    }
	    */
	}

	static Vector getSupport(Breakpoint b1, Breakpoint b2) {
	    return intersects(b1.dataSet_v, b2.dataSet_v);
	}

	static String getStringNLSupport(Vector supp) {
	    int sz = supp.size();

	    String s = "";
	    for (int n = 0; n < sz; n++) {
		GraphElement graphElem = (GraphElement)supp.get(n);
		s += (n > 0 ? "\n" : "") + graphElem.getID();
	    }

	    return s;
	}

	static String getStringSupport(Vector supp) {
	    String s = supp.size() + "=[";
	    for (int n = 0; n < supp.size(); n++)
		s += (n > 0 ? ", " : "") + ((GraphElement)supp.get(n)).getID();
	    s += "]";
	    return s;
	}

	String getStringSupport() {
	    String s = dataSet_v.size() + "=[";
	    for (int n = 0; n < dataSet_v.size(); n++)
		s += (n > 0 ? ", " : "") + ((GraphElement)dataSet_v.get(n)).getID();
	    s += "]";
	    return s;
	}

	void mergeDataSets(Vector nDataSet_v) {
	    for (int m = 0; m < nDataSet_v.size(); m++) {
		if (!dataSet_v.contains(nDataSet_v.get(m)))
		    dataSet_v.add(nDataSet_v.get(m));
	    }
	}
	
	static private boolean acceptable(RODataElementProxy d, Breakpoint bkp,
					  boolean outlier) {
	    return (VAMPUtils.isNA(d) || VAMPUtils.isMissing(d) ||
		    (VAMPUtils.getGNL(d).equals(AMPLICON) && ignoreAmplicon) ||
		    (VAMPUtils.isOutlier(d) && !outlier));
	}

	static private boolean high_acceptable(RODataElementProxy d, Breakpoint bkp,
					       boolean outlier) {
	    String gnl = getGNL(d);
	    /*
	      if (mergeGainedAmplicon && bkp.gnl.equals(GAINED))
	      return true;
	    */
	    return gnl.equals(bkp.gnl);
	}

	void extendLeft(Vector supp, boolean outlier) throws Exception {
	    int minind = 0;

	    if (TRACE_BKP_DETAILS)
		System.out.println("extendLeft: ind-1: " + (ind-1));
	    for (int m = 0; m < supp.size(); m++) {
		GraphElement dset = (GraphElement)supp.get(m);
		int length = dset.getProbeCount();

		String chr = VAMPUtils.getChr(dset.getDataProxy(ind));
		int j = ind - 1;
		for (; j >= 0; j--) {
		    RODataElementProxy d = dset.getDataProxy(j);
		    if (!VAMPUtils.getChr(d).equals(chr) ||
			!acceptable(d, this, outlier) &&
			!high_acceptable(d, this, outlier))
			break;
		}

		if (TRACE_BKP_DETAILS)
		    System.out.println("j : " + j);
		if (minind <= j)
		    minind = j;
	    }

	    if (TRACE_BKP_DETAILS)
		System.out.println("extends left ? " + ind + " <- " + (minind+1));
	    ind = minind + 1;
	}

	void extendRight(Vector supp, boolean outlier) throws Exception {
	    int maxind = ((GraphElement)supp.get(0)).getProbeCount();

	    if (TRACE_BKP_DETAILS)
		System.out.println("extendRight: ind+1: " + (ind+1));
	    for (int m = 0; m < supp.size(); m++) {
		GraphElement dset = (GraphElement)supp.get(m);
		int length = dset.getProbeCount();

		String chr = VAMPUtils.getChr(dset.getDataProxy(ind));
		int j = ind + 1;
		for (; j < length; j++) {
		    RODataElementProxy d = dset.getDataProxy(j);
		    if (!VAMPUtils.getChr(d).equals(chr) ||
			!acceptable(d, this, outlier) &&
			!high_acceptable(d, this, outlier))
			break;
		}

		if (TRACE_BKP_DETAILS)
		    System.out.println("j : " + j);
		if (maxind >= j)
		    maxind = j;
	    }

	    if (TRACE_BKP_DETAILS)
		System.out.println("extends right ? " + ind + " -> " + (maxind-1));
	    ind = maxind - 1;
	}

	static private boolean support_manage(Vector supp, GraphElement dset,
					      boolean high,
					      Breakpoint bkp, Breakpoint bkp2,
					      boolean outlier) throws Exception {
	    if (high) {
		supp.add(dset);
		return true;
	    }

	    String chr = VAMPUtils.getChr(dset.getDataProxy(bkp.ind));
	    for (int j = bkp.ind - 1; j >= 0; j--) {
		RODataElementProxy d = dset.getDataProxy(j);
		if (!VAMPUtils.getChr(d).equals(chr))
		    break;
		if (acceptable(d, bkp, outlier))
		    ;
		else if (high_acceptable(d, bkp, outlier)) {
		    supp.add(dset);
		    return true;
		}
		else
		    break;
	    }
		
	    int length = dset.getProbeCount();

	    for (int j = bkp2.ind + 1; j < length; j++) {
		RODataElementProxy d = dset.getDataProxy(j);
		if (!VAMPUtils.getChr(d).equals(chr))
		    return false;
		if (acceptable(d, bkp, outlier))
		    ;
		else if (high_acceptable(d, bkp, outlier)) {
		    supp.add(dset);
		    return true;
		}
		else
		    return false;
	    }

	    return false;
	}

	static Vector getRecurrentSupport(Vector bkp_v,
					  int in_ind, int out_ind, boolean outlier, int min_alt, int toleration) throws Exception {
	    Breakpoint in_bkp = (Breakpoint)bkp_v.get(in_ind);
	    Breakpoint out_bkp  = (Breakpoint)bkp_v.get(out_ind);

	    int bkp_in_ind = in_bkp.getInd();
	    int bkp_out_ind = out_bkp.getInd();

	    Vector supp = Breakpoint.getSupport(in_bkp, out_bkp);

	    if (TRACE_BKP) {
		System.out.println("\nComputing Recurrent Support:");
		System.out.println("  initial support: " + Breakpoint.getStringSupport(supp) + " {" + in_bkp + " || " + out_bkp + "} toleration " + toleration);
	    }

	    if (supp.size() < min_alt) {
		if (TRACE_BKP) {
		    System.out.println("  support is less than min alt");
		}
		return supp;
	    }

	    /*
	    bkp_in_ind += toleration;
	    bkp_out_ind -= toleration;
	    */

	    int size = bkp_v.size();
	    for (int n = 0; n < size; n++) {
		Breakpoint bkp = (Breakpoint)bkp_v.get(n);

		int ind = bkp.getInd();
		if (bkp.isIN()) {
		    if (ind - bkp_in_ind <= toleration)
			continue;
		}
		else {
		    if (bkp_out_ind - ind <= toleration)
			continue;
		}

		if (bkp.getInd() <= bkp_in_ind || bkp.getInd() >= bkp_out_ind)
		  continue;

		Vector sp = bkp.getSupport();

		if (TRACE_BKP) {
		    System.out.println("  " + bkp + ":");
		}

		for (int m = 0; m < sp.size(); m++) {
		    GraphElement dataSet = (GraphElement)sp.get(m);
		    //DataElement data[] = dataSet.getData();

		    boolean missing;

		    if (bkp.isIN()) {
			missing = foundMissing(dataSet, bkp.ind-1, outlier, -1);
		    }
		    else {
			missing = foundMissing(dataSet, bkp.ind+1, outlier, 1);
		    }

		    if (TRACE_BKP) {
			System.out.println("    " + dataSet.getID() + " is "  +
					   (missing ? "missing" : "not missing (suppressed from support)"));
		    }


		    if (!missing) {
			Breakpoint.suppressFromSupport(supp, dataSet);
		    }
		}
	    }

	    if (TRACE_BKP) {
		System.out.println("  returning " + Breakpoint.getStringSupport(supp));
	    }

	    return supp;
	}

	static Vector getMinimalSupport(Vector graphElements, Breakpoint bkp,
					Breakpoint bkp2, boolean outlier) throws Exception {
	    Vector supp = new Vector();
	    int region_width = bkp2.ind - bkp.ind + 1;
	    for (int m = 0; m < graphElements.size(); m++) {
		GraphElement dset = (GraphElement)graphElements.get(m);
		int probe_cnt = 0;
		boolean high = false;
		for (int j = bkp.ind; j <= bkp2.ind; j++) {
		    RODataElementProxy d = dset.getDataProxy(j);
		    if (acceptable(d, bkp, outlier))
			probe_cnt++;
		    else if (high_acceptable(d, bkp, outlier)) {
			high = true;
			probe_cnt++;
		    }
		    else
			break;
		}

		if (probe_cnt == region_width)
		    support_manage(supp, dset, high, bkp, bkp2, outlier);
	    }

	    return supp;
	}

	/*
	static Breakpoint merge_old(Vector v, DataElement data[], int n,
				int m) {
	    String chr = VAMPUtils.getChr(data[n]);
	    Breakpoint mergeBkp = null;
	    boolean out = false;
	    if (n < m) {
		for (int i = n; i <= m && i < data.length; i++) {
		    if (!VAMPUtils.getChr(data[i]).equals(chr))
			break;
		    Breakpoint bkp = (Breakpoint)v.get(i);
		    if (bkp != null) {
			if (mergeBkp == null) {
			    mergeBkp = new Breakpoint(bkp.type, n, bkp.gnl);
			    out = bkp.isOUT();
			}
			else if (bkp.isOUT() != out)
			    break;
			mergeBkp.mergeDataSets(bkp.dataSet_v);
		    }
		}
	    }
	    else {
		for (int i = n; i >= m && i >= 0; i--) {
		    if (!VAMPUtils.getChr(data[i]).equals(chr))
			break;
		    Breakpoint bkp = (Breakpoint)v.get(i);
		    if (bkp != null) {
			if (mergeBkp == null) {
			    mergeBkp = new Breakpoint(bkp.type, n, bkp.gnl);
			    out = bkp.isOUT();
			}
			else if (bkp.isOUT() != out)
			    break;
			mergeBkp.mergeDataSets(bkp.dataSet_v);
		    }
		}
	    }
	    return mergeBkp;
	}
	*/

	/*
	static Breakpoint merge2(Vector v, DataElement data[], int n,
				 int m) {
	    Breakpoint bkp = (Breakpoint)v.get(n);

	    if (bkp == null) {
		return null;
	    }

	    String chr = VAMPUtils.getChr(data[n]);
	    Breakpoint mergeBkp = null;
	    boolean out = bkp.isOUT();

	    if (n < m) {
		// out breakpoints
		if (m >= data.length)
		    m = data.length - 1;
		for (int i = m; i >= n && i >= 0; i--) {
		    if (!VAMPUtils.getChr(data[i]).equals(chr))
			break;
		    bkp = (Breakpoint)v.get(i);
		    if (bkp != null) {
			if (mergeBkp == null) {
			    mergeBkp = new Breakpoint(bkp.type, i, bkp.gnl);
			}
			else if (bkp.isOUT() != out)
			    break;
			mergeBkp.mergeDataSets(bkp.dataSet_v);
		    }
		}
	    }
	    else {
		// in breakpoints
		if (m < 0)
		    m = 0;
		for (int i = m; i <= n && i < data.length; i++) {
		    if (!VAMPUtils.getChr(data[i]).equals(chr))
			break;
		    bkp = (Breakpoint)v.get(i);
		    if (bkp != null) {
			if (mergeBkp == null) {
			    mergeBkp = new Breakpoint(bkp.type, i, bkp.gnl);
			}
			else if (bkp.isOUT() != out)
			    break;
			mergeBkp.mergeDataSets(bkp.dataSet_v);
		    }
		}
	    }
	    return mergeBkp;
	}
	*/

	static Breakpoint mergeInBkp(Vector v, HashMap bkp_map,
				     GraphElement dataSet0, int n,
				     int toleration) throws Exception {
	    Breakpoint bkp = (Breakpoint)v.get(n);

	    if (bkp == null) {
		return null;
	    }

	    String chr = VAMPUtils.getChr(dataSet0.getDataProxy(n));
	    Breakpoint mergeBkp = null;
	    boolean out = bkp.isOUT();

	    // in breakpoints
	    int m = n - toleration;
	    if (m < 0)
		m = 0;

	    int length = dataSet0.getProbeCount();
	    for (int i = m; i <= n && i < length; i++) {
		if (VAMPUtils.getChr(dataSet0.getDataProxy(i)).equals(chr)) {
		    m = i;
		    break;
		}
	    }

	    for (int i = m; i <= n && i < length; i++) {
		if (!VAMPUtils.getChr(dataSet0.getDataProxy(i)).equals(chr))
		    break;

		bkp = (Breakpoint)v.get(i);
		if (bkp != null) {
		    if (mergeBkp == null) {
			if (bkp_map.get(new Integer(i)) != null)
			    return null;
			mergeBkp = new Breakpoint(bkp.type, i, bkp.gnl);
			bkp_map.put(new Integer(i), mergeBkp);
		    }
		    else if (bkp.isOUT() != out)
			break;
		    mergeBkp.mergeDataSets(bkp.dataSet_v);
		}
	    }
	    return mergeBkp;
	}

	static Breakpoint mergeOutBkp(Vector v, HashMap bkp_map,
				      GraphElement dataSet0, int n,
				      int toleration) throws Exception {
	    Breakpoint bkp = (Breakpoint)v.get(n);

	    if (bkp == null) {
		return null;
	    }

	    String chr = VAMPUtils.getChr(dataSet0.getDataProxy(n));
	    Breakpoint mergeBkp = null;
	    boolean out = bkp.isOUT();

	    int m = n + toleration;
	    int length = dataSet0.getProbeCount();
	    if (m >= length)
		m = length - 1;

	    for (int i = m; i >= n && i >= 0; i--) {
		if (VAMPUtils.getChr(dataSet0.getDataProxy(i)).equals(chr)) {
		    m = i;
		    break;
		}
	    }

	    for (int i = m; i >= n && i >= 0; i--) {
		if (!VAMPUtils.getChr(dataSet0.getDataProxy(i)).equals(chr))
		    break;

		bkp = (Breakpoint)v.get(i);
		if (bkp != null) {
		    if (mergeBkp == null) {
			if (bkp_map.get(new Integer(i)) != null)
			    return null;
			mergeBkp = new Breakpoint(bkp.type, i, bkp.gnl);
			bkp_map.put(new Integer(i), mergeBkp);
		    }
		    else if (bkp.isOUT() != out)
			break;
		    mergeBkp.mergeDataSets(bkp.dataSet_v);
		}
	    }

	    return mergeBkp;
	}

	static Breakpoint merge(Vector v, GraphElement dataSet0, int n,
				int toleration) throws Exception {
	    Breakpoint bkp = (Breakpoint)v.get(n);

	    if (bkp == null) {
		return null;
	    }

	    String chr = VAMPUtils.getChr(dataSet0.getDataProxy(n));
	    boolean out = bkp.isOUT();
	    String gnl = bkp.gnl;

	    Breakpoint mergeBkp = new Breakpoint(bkp.type, n, bkp.gnl);
	    //mergeBkp.mergeDataSets(bkp.dataSet_v); // merge in the loop

	    int length = dataSet0.getProbeCount();
	    for (int i = n - toleration; i <= n + toleration && i < length; i++) {
		if (i < 0)
		    continue;

		if (!VAMPUtils.getChr(dataSet0.getDataProxy(i)).equals(chr))
		    continue;

		bkp = (Breakpoint)v.get(i);
		if (bkp != null) {
		    if (bkp.isOUT() != out) {
			continue;
		    }

		    if (!bkp.gnl.equals(gnl)) {
			continue;
		    }

		    mergeBkp.mergeDataSets(bkp.dataSet_v);
		}
	    }
	
	    return mergeBkp;
	}
    }

    private void getParams(TreeMap params, int size) throws Exception {

	toleration = 0;
	outlier = false;

	compute_mask = 0;
	min_bkp = size;
	min_alt = size;
	alt_mask = 0;
	scope_mask = 0;

	Object vcompute_mask = params.get(COMPUTE_MASK);
	if (vcompute_mask != null)
	    compute_mask = ((Integer)vcompute_mask).intValue();

	Object vnumber;
	Double vpercent;

	vpercent = (Double)params.get(MIN_PERCENT_ALT);
	if (vpercent != null)
	    min_alt = (int)(size * vpercent.doubleValue()/100);

	vnumber = params.get(MIN_NUMBER_ALT);
	if (vnumber != null) {
	    if (!vnumber.equals(ALL))
		min_alt = ((Integer)vnumber).intValue();
	}

	//System.out.println("MIN_ALT " + min_alt);

	if ((compute_mask & RECURRENT_REGIONS_MASK) != 0) 
	    min_bkp = min_alt;
	else {
	    vpercent = (Double)params.get(MIN_PERCENT_BKP);
	    if (vpercent != null)
		min_bkp = (int)(size * vpercent.doubleValue()/100);

	    vnumber = params.get(MIN_NUMBER_BKP);
	    if (vnumber != null) {
		if (!vnumber.equals(ALL))
		    min_bkp = ((Integer)vnumber).intValue();
	    }
	}

	//System.out.println("MIN_BKP " + min_bkp);

	Object vsuppress_reg = params.get(SUPPRESS_REGIONS);
	if (vsuppress_reg != null)
	    suppress_reg = vsuppress_reg.equals(TRUE);
	    
	Object vtoleration = params.get(TOLERATION);
	if (vtoleration != null)
	    toleration = ((Integer)vtoleration).intValue();

	Object voutlier = params.get(OUTLIERS);
	if (voutlier != null)
	    outlier = voutlier.equals(TRUE);
	    
	Object vscope_mask = params.get(SCOPE_MASK);
	if (vscope_mask != null)
	    scope_mask = ((Integer)vscope_mask).intValue();

	Object valt_mask = params.get(ALT_MASK);
	if (valt_mask != null)
	    alt_mask = Integer.parseInt((String)valt_mask);

	Object vextended_NA = params.get(EXTENDED_NA);
	if (vextended_NA != null)
	    extended_NA = vextended_NA.equals(TRUE);

	Object vwiden_regions = params.get(WIDEN_REGIONS);
	if (vwiden_regions != null)
	    widen_regions = vwiden_regions.equals(TRUE);

	Object vres_mask = params.get(RESULT_MASK);
	if (vres_mask != null)
	    res_mask = Integer.parseInt((String)vres_mask);


	if (TRACE_BKP)
	    System.out.println("compute_mask: " + compute_mask +
			       " min_alt: " + min_alt +
			       " min_bkp: " + min_bkp +
			       " toleration: " + toleration +
			       " outlier: " + outlier +
			       " extended_NA: " + extended_NA +
			       " widen_regions: " + widen_regions +
			       " outlier: " + outlier +
			       " alt_mask: " + alt_mask + 
			       " scope_mask: " + alt_mask +
			       " res_mask: " + res_mask);
	
    }

    static boolean is_included(Vector supp1, Vector supp2) {
	for (int j = 0; j < supp1.size(); j++) {
	    if (!supp2.contains(supp1.get(j))) {
		return false;
	    }
	}
	return true;
    }

    static int intersects_size(Vector v1, Vector v2) {
	int cnt = 0;
	int v1_size = v1.size();
	for (int i = 0; i < v1_size; i++) {
	    if (v2.contains(v1.get(i)))
		cnt++;
	}

	return cnt;
    }

    static Vector intersects(Vector v1, Vector v2) {
	Vector v = new Vector();
	int v1_size = v1.size();
	for (int i = 0; i < v1_size; i++) {
	    if (v2.contains(v1.get(i)))
		v.add(v1.get(i));
	}

	return v;
    }

    private static double getPosSize(GraphElement dataSet, RODataElementProxy d) {
	double pos_sz = d.getPosSize(dataSet);
	if (pos_sz != 0)
	    return pos_sz;
	return 1;
    }

    static boolean hasRegions(View view, String type) {
	if (view == null)
	    return false;
	GraphPanelSet panel_set = view.getGraphPanelSet();
	Object regions[] = panel_set.getRegions().toArray();
	for (int n = 0; n < regions.length; n++) {
	    Region region = (Region)regions[n];
	    if (VAMPUtils.getType(region).equals(type))
		return true;
	}
	return false;
    }

    static boolean hasRecurrentRegions(View view) {
	return hasRegions(view, VAMPConstants.RECURRENT_REGION_TYPE);
    }

    static boolean hasMinimalRegions(View view) {
	return hasRegions(view, VAMPConstants.MINIMAL_REGION_TYPE);
    }

    void suppressRegions(GraphPanelSet panel_set,
			 GraphPanel panel) {

	Object regions[] = panel_set.getRegions().toArray();
	for (int n = 0; n < regions.length; n++) {
	    Region region = (Region)regions[n];
	    if (VAMPUtils.getType(region).equals(VAMPConstants.MINIMAL_REGION_TYPE)) {
		if ((compute_mask & MINIMAL_REGIONS_MASK) != 0 ||
		    suppress_reg)
		    panel_set.removeRegion(panel.getWhich(), region, true);
	    }
	    else if (VAMPUtils.getType(region).equals(VAMPConstants.RECURRENT_REGION_TYPE)) {
		if ((compute_mask & RECURRENT_REGIONS_MASK) != 0 ||
		    suppress_reg)
		    panel_set.removeRegion(panel.getWhich(), region, true);
	    }
	}

	Object marks[] = panel_set.getMarks().toArray();
	for (int n = 0; n < marks.length; n++) {
	    Mark mark = (Mark)marks[n];
	    if (VAMPUtils.getType(mark).equals(VAMPConstants.RECURRENT_BREAKPOINT_TYPE))
		panel_set.removeMark(panel.getWhich(), mark);
	}
    }

    void postAddRegions(LinkedList regions, String gnl) {
	if (regions == null)
	    return;
	int size = regions.size();
	String sz = Utils.toString(size);
	for (int n = 0; n < size; n++) {
	    Region region = (Region)regions.get(n);
	    region.setPropertyValue(NumProp,
				    Utils.toString(n+1) + " / " + sz +
				    " " + gnl);
	    //region.setPropertyValue(StatsProp, stats);
	}
    }

    private boolean hasBkp(Vector v, int ind) {
	int size = v.size();
	for (int n = 0; n < size; n++)
	    if (((Breakpoint)v.get(n)).ind == ind)
		return true;

	return false;
    }

    static boolean candidateRegion(Region region) {
	return VAMPUtils.getType(region).equals(VAMPConstants.MINIMAL_REGION_TYPE) ||
	    VAMPUtils.getType(region).equals(VAMPConstants.RECURRENT_REGION_TYPE);
    }

    public boolean useThread() {
	return true;
    }

    Vector mergeConsecutiveRegions(Vector o_regions) throws Exception {
	Vector regions = new Vector();
	MinimalRegion r0 = null;

	for (int n = 0; n < o_regions.size(); n++) {
	    MinimalRegion r = (MinimalRegion)o_regions.get(n);
	    if (r0 != null && (r0.bkp2.getInd() == r.bkp1.getInd() - 1 ||
			       r0.bkp2.getInd() == r.bkp1.getInd()) &&
		suppEquals(r0.supp, r.supp)) {
		r0.bkp2 = r.bkp2;
	    }
	    else {
		if (r0 != null) {
		    regions.add(r0.extend(outlier));
		}
		r0 = r;
	    }
	}

	if (r0 != null) {
	    regions.add(r0.extend(outlier));
	}

	if (TRACE_REG) {
	    System.out.println("merge consecutive regions - old region count:" + o_regions.size() + ", new region count: " + regions.size());
	}

	return regions;
    }

    static boolean suppEquals(Vector supp1, Vector supp2) {
	if (supp1.size() != supp2.size())
	    return false;

	if (supp1.equals(supp2))
	    return true;

	int size = supp1.size();
	for (int n = 0; n < size; n++) {
	    GraphElement dset1 = (GraphElement)supp1.get(n);
	    if (!supp2.contains(dset1))
		return false;
	}

	return true;
    }

    public boolean supportProfiles() {
	return true;
    }

    private Vector postFilterMinimalRegions(Vector regions) {

	int rgsize = regions.size();
	HashMap reg_map = new HashMap();

	if (TRACE_REG) {
	    System.out.println("\nBefore minimal region filtering process:");
	}

	for (int n = 0; n < rgsize; n++) {
	    MinimalRegion r = (MinimalRegion)regions.get(n);
	    reg_map.put(r, new Boolean(true));
	    if (TRACE_REG) {
		System.out.println(r);
	    }
	}

	if (TRACE_REG) {
	    System.out.println("\nFiltering minimal region process:");
	}

	Collections.sort(regions, new AlteredRegionComparator(false));

	// out breakpoints
	for (int n = 0; n < rgsize - 1; n++) {
	    MinimalRegion r = (MinimalRegion)regions.get(n);
	    for (int m = n+1; m < rgsize; m++) {
		MinimalRegion r_n = (MinimalRegion)regions.get(m);
		if (r.includeRegion(r_n) && is_included(r.getSupport(), r_n.getSupport())) {
		    reg_map.put(r, new Boolean(false));
		    if (TRACE_REG) {
			System.out.println("suppress region: " + r);
		    }
		}

		if (r_n.bkp2.ind < r.bkp1.ind) {
		    break;
		}
	    }
	}

	if (TRACE_REG) {
	    System.out.println("\nFiltering results:");
	}

	Vector regions_r = new Vector();

	for (int n = 0; n < rgsize; n++) {
	    MinimalRegion r = (MinimalRegion)regions.get(n);
	    Boolean b = (Boolean)reg_map.get(r);
	    if (b.booleanValue()) {
		regions_r.add(r);
	    }
	    else if (TRACE_REG) {
		System.out.println(r + " FILTERED");
	    }
	}

	return regions_r;
    }

    // --------------------------------------------------------------
}

