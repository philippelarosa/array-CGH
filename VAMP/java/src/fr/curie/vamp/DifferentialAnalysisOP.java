
/*
 *
 * DifferentialAnalysisOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;

class DifferentialAnalysisOP extends GraphElementListOperation {

    static final String NAME = "Differential Analysis";

    static final String TRUE = "true";
    static final String FALSE = "false";

    static final String TEST_PARAM = "Test";

    static final String PVALUE_PARAM = "PValue";

    static final String COLORBARS_PARAM = "Colorbars";
    static final int USE_SIGN_MASK = 0x1;
    static final int USE_CONFIDENCE_MASK = 0x2;

    static final String MTEST_PARAM = "MultipleTesting";
    static final int MTEST_NONE = 0x10;
    static final int MTEST_BH = 0x20;
    static final int MTEST_BY = 0x40;

    static final String RESULT_PARAM = "Results";
    static final int HTML_REPORT = 0x100;
    static final int CSV_REPORT = 0x200;
    static final int PROFILE_DISPLAY = 0x400;

    static final String SYSCFG_NAME = "diffAnalysis";

    static final boolean USE_POST = false;
    static final String RUNNING_MESSAGE = "Computing differential analysis...";
    static final String DONE_MESSAGE = "Differential analysis done !";

    static final Property confidenceProp = Property.getProperty("Confidence");
    static final Property statisticProp = Property.getProperty("Statistic");
    static final Property mlog10PValueProp = Property.getProperty("MinusLog10Pvalue");
    static final Property pvalueProp = Property.getProperty("Pvalue");
    static final Property correctProp = Property.getProperty("Pvalue Correction");
    static final Property testProp = Property.getProperty("Test");
    static final Property useProp = Property.getProperty("Use");

    boolean useConfidence = false;
    String correct_str;
    String pvalue_str;
    String test_str;

    public String[] getSupportedInputTypes() {
	//	return null;
	// warning: modified 25/05/05
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE, VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE};
    }

    public String getReturnedType() {
	return null;
    }

    DifferentialAnalysisOP() {
	super(NAME, SHOW_MENU | ON_ALL_AUTO);
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	if (view.getGraphPanelSet().getPanelCount() != 2)
	    return false;

	GraphPanelSet graphPanelSet = view.getGraphPanelSet();
	Vector graphElements1, graphElements2;
	graphElements1 = Utils.listToVector(graphPanelSet.getPanel(0).getGraphElements());
	graphElements2 = Utils.listToVector(graphPanelSet.getPanel(1).getGraphElements());
	graphElements1 = getGraphElements(graphPanelSet.getPanel(0), graphElements1, autoApply);
	graphElements2 = getGraphElements(graphPanelSet.getPanel(1), graphElements2, autoApply);

	SystemConfig sysCfg = (SystemConfig)view.getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);
	return sysCfg.getParameter(SYSCFG_NAME + ":URL") != null &&
	    graphElements1.size() >= 2 && graphElements2.size() >= 2 &&
	    getChr(graphElements1, graphElements2) != null;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	GraphPanelSet graphPanelSet = view.getGraphPanelSet();
	// must be moved to mayApply or a trig boolean beforeApply
	if (graphPanelSet.getPanelCount() != 2) {
	    InfoDialog.pop(view.getGlobalContext(), "Differential analysis " +
			   "must be used in a double view");
	    return graphElements;
	}
	
	Vector graphElements1, graphElements2;
	graphElements1 = Utils.listToVector(graphPanelSet.getPanel(0).getGraphElements());
	graphElements2 = Utils.listToVector(graphPanelSet.getPanel(1).getGraphElements());
	graphElements1 = getGraphElements(graphPanelSet.getPanel(0), graphElements1, autoApply);
	graphElements2 = getGraphElements(graphPanelSet.getPanel(1), graphElements2, autoApply);

	if (graphElements1.size() == 0 || graphElements2.size() == 0)
	    return null;

	String chr_s = getChr(graphElements1, graphElements2);
	if (chr_s == null) {
	    InfoDialog.pop(view.getGlobalContext(), "cannot apply " +
			   "clustering on different chromosomes");
	    return null;
	}


	int colorbar_mask = ((Integer)params.get(COLORBARS_PARAM)).intValue();
	useConfidence = (colorbar_mask & USE_CONFIDENCE_MASK) != 0;

	int result_mask = ((Integer)params.get(RESULT_PARAM)).intValue();
	if (result_mask == 0)
	    return null;

	SystemConfig sysCfg = (SystemConfig)view.getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);
	String cgi_name = sysCfg.getParameter(SYSCFG_NAME + ":URL");
	String surl = cgi_name;

	String data = "action=ana_diff";

	data += "&url1=" + makeURLName(graphElements1);
	data += "&url2=" + makeURLName(graphElements2);

	String pvalue = (String)params.get(PVALUE_PARAM);
	pvalue_str = pvalue;

	String test = (String)params.get(TEST_PARAM);
	test_str = test;

	data += "&test=" + test;
	data += "&chr=" + chr_s;
	//	data += "&pvalue=" + (pvalue.trim().length() > 0 ? pvalue : "1");
	data += "&pvalue=1.0";

	String correct;

	int mtest_mask = ((Integer)params.get(MTEST_PARAM)).intValue();

	if ((mtest_mask & MTEST_BH) != 0) {
	    correct = "BH";
	    correct_str = "Benjamini-Hochberg";
	}
	else if ((mtest_mask & MTEST_BY) != 0) {
	    correct = "BY";
	    correct_str = "Benjamini-Yekutieli";
	}
	else {
	    correct = "none";
	    correct_str = "None";
	}

	data += "&correct=" + correct;

	String method = sysCfg.getParameter(SYSCFG_NAME + ":method");

	if (method == null || !method.equalsIgnoreCase("post")) {
	    surl += "?" + data;
	    data = null;
	}

	System.out.println("Differential Analysis URL: " + surl + " :: " + data);

	try {
	    InputStream is = Utils.openStream(surl, data);
	    boolean merge = chr_s.equals("all");
	    GraphElementListOperation op = null;

	    if (merge)
		op = GraphElementListOperation.get(MergeChrOP.DIFFANA_NAME);

	    if ((result_mask & PROFILE_DISPLAY) != 0) {
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
	    }
	    else {
		XMLArrayDataFactory array_factory =
		    new XMLArrayDataFactory(view.getGlobalContext(), null);

		LinkedList graphElemList = array_factory.getData(is);
		if (graphElemList != null) {
		    InfoDialog.pop(view.getGlobalContext(), DONE_MESSAGE);
		    DataSet dataSet = null;
		    if (merge) {
			Vector elems = Utils.listToVector(graphElemList);
			Vector v = op.apply(view, panel, elems,
					    op.getDefaultParams(view, elems),
					    true);

			if (v != null && v.size() == 1) {
			    dataSet = (DataSet)v.get(0);
			}
		    }
		    else if (graphElemList.size() == 1)
			dataSet = (DataSet)graphElemList.get(0);

		    if (dataSet == null)
			return null;

		    updateProperties(dataSet, pvalue_str, correct_str, test_str);

		    DifferentialAnalysisReportOP.buildReport(dataSet, (result_mask & HTML_REPORT) != 0);
		}
	    }

        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(view.getGlobalContext(), e.getMessage());
	    return null;
        }
	return null;
    }

    /*
      void buildReport_old(DataSet dataSet, boolean isHTML, TreeMap _params) throws java.io.FileNotFoundException {

      DataElement data[] = dataSet.getData();

      File file = DialogUtils.openFileChooser(new java.awt.Frame(), "Save", 0,
      "Differential Analysis: " +
      (isHTML ? "HTML" : "CSV") +
      " Report");
	
      if (file == null)
      return;

      String ext = isHTML ? ".html" : ".csv";

      if (!Utils.hasExtension(file.getName(), ext))
      file = new File(file.getAbsolutePath() + ext);

      FileOutputStream os = new FileOutputStream(file);
      PrintStream ps = new PrintStream(os);

      ReportBuilder builder;
      if (isHTML)
      builder = new HTMLReportBuilder(ps, "Differential Analysis");
      else
      builder = new CSVReportBuilder(ps, "Differential Analysis");

      builder.startDocument();

      builder.addTitle3("Parameters");

      builder.addText("Test: " + dataSet.getPropertyValue(testProp));
      builder.addVPad();
      builder.addText("P-Value: " + dataSet.getPropertyValue(pvalueProp));
      builder.addVPad();

      builder.addText("Multiple Testing: " + dataSet.getPropertyValue(correctProp));

      builder.addVPad(3);

      builder.startTable(new String[]{"Name", "Chr", "Confidence",
      "Statistic", "P-Value / -log10"});

      StandardColorCodes cc = (StandardColorCodes)VAMPUtils.getColorCodes
      (dataSet);

      double beta = cc.getNormalMin() + (cc.getNormalMax() - cc.getNormalMin())/2;
      double alpha = cc.getMax() - beta;
	    
      for (int n = 0; n < data.length; n++) {
      builder.startRow();
      builder.addCell(data[n].getID().toString());
      builder.addCell(VAMPUtils.getChr(data[n]));
      String s = (String)data[n].getPropertyValue(confidenceProp);
      double v = Utils.parseDouble(s);
      double confidence_ratio = alpha * v + beta;

      builder.addCell(Utils.performRound(data[n].getPropertyValue(confidenceProp)), getStyle(confidence_ratio, cc));

      s = (String)data[n].getPropertyValue(statisticProp);
      v = Utils.parseDouble(s);
			    
      double stat_ratio = v >= 0 ? cc.getMax() : cc.getMin();

      builder.addCell(Utils.performRound(data[n].getPropertyValue(statisticProp)), getStyle(stat_ratio, cc));
      builder.addCell(Utils.performRound(data[n].getPropertyValue(pvalueProp)) + " / " + Utils.performRound(data[n].getPropertyValue(mlog10PValueProp)));
      builder.endRow();
      }
	
      builder.endTable();
      builder.endDocument();
      }
    */

    ViewFrame buildProfileView(GlobalContext globalContext, GraphPanel panel,
			       boolean merge) {
	BarplotDataSetDisplayer histo_dsp = new BarplotDataSetDisplayer(true);

	PanelProfile panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     histo_dsp,
	     (merge ? Config.defaultChromosomeNameAxisDisplayer :
	      Config.defaultGenomicPositionAxisDisplayer),
	     null,
	     false,
	     /*
	       (merge ? GraphElementListOperation.get(MergeChrOP.DIFFANA_NAME) :
	       GraphElementListOperation.get(ChrAxisOP.NAME)),
	     */
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	    
	PanelProfile panelProfiles[] = new PanelProfile[]{panelProfile};

	ViewFrame vf = new ViewFrame(globalContext,
				     "Differential Analysis",
				     panelProfiles,
				     null,
				     null,
				     null, null,
				     new LinkedList(),
				     Config.defaultDim,
				     null);

	/*
	  LinkedList list = Utils.vectorToList(rGraphElements);
	  vf.getView().getGraphPanelSet().getPanel(panel != null ? panel.getWhich() : 0).setGraphElements(list);
	  vf.setVisible(true);

	  vf.getView().syncGraphElements();
	*/

	return vf;
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

    static final HashMap view_map = new HashMap();

    public TreeMap getDefaultParams(View view, Vector graphElements) {
	TreeMap params = (TreeMap)view_map.get(view);
	if (params != null)
	    return params;

	params = new TreeMap();

	params.put(TEST_PARAM, "Student");
	params.put(PVALUE_PARAM, "1.0");
	params.put(COLORBARS_PARAM, new Integer(USE_SIGN_MASK));
	params.put(MTEST_PARAM, new Integer(MTEST_NONE));
	params.put(RESULT_PARAM, new Integer(PROFILE_DISPLAY));

	view_map.put(view, params);
	return params;
    }

    public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = DifferentialAnalysisDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	if (params != null)
	    view_map.put(view, params);
	return params;
    }

    public boolean mayApplyOnReadOnlyPanel() {
	return true;
    }

    static String getChr(Vector graphElements1, Vector graphElements2) {
	String chr = VAMPUtils.getChr((GraphElement)graphElements1.get(0));

	int size = graphElements1.size();
	for (int n = 1; n < size; n++) {
	    String chr1 = VAMPUtils.getChr((GraphElement)graphElements1.get(n));
	    if (chr1 == null || !chr1.equals(chr)) {
		return null;
	    }
	}

	size = graphElements2.size();
	for (int n = 0; n < size; n++) {
	    String chr2 = VAMPUtils.getChr((GraphElement)graphElements2.get(n));
	    if (chr2 == null || !chr2.equals(chr)) {
		return null;
	    }
	}

	if (chr.indexOf(",") >= 0)
	    return "all";
	return chr;
    }

    public boolean useThread() {
	return true;
    }
   
    public String getMessage() {
	return RUNNING_MESSAGE;
    }


    public static void updateProperties(DataSet dataSet, String pvalue_str,
					String correct_str,
					String test_str) {
	if (correct_str != null)
	    dataSet.setPropertyValue(correctProp, correct_str);
	if (pvalue_str != null)
	    dataSet.setPropertyValue(pvalueProp, pvalue_str);
	if (test_str != null)
	    dataSet.setPropertyValue(testProp, test_str);

	dataSet.setPropertyValue(VAMPProperties.NameProp, "-log10(p-value)");
    }

    public static void filterPValue(DataSet dataSet, double pvalue) {
	DataElement data[] = dataSet.getData();

	int clone_cnt = 0;
	for (int n = 0; n < data.length; n++) {
	    DataElement d = data[n];
	    double data_pvalue = Utils.parseDouble((String)d.getPropertyValue(pvalueProp));
	    if (data_pvalue < pvalue) {
		d.removeProperty(VAMPProperties.MissingProp);
		clone_cnt++;
	    }
	    else {
		d.setPropertyValue(VAMPProperties.MissingProp, "true");
	    }
	}

	dataSet.setPropertyValue(VAMPProperties.CloneCountProp, new Integer(clone_cnt));
    }

    public void postPerform(LinkedList graphElements) {
	int size = graphElements.size();
	if (size != 1)
	    return;

	DataSet dataSet = (DataSet)graphElements.get(0);
	rebuild(dataSet, useConfidence, pvalue_str, correct_str, test_str);
    }

    public static void rebuild(DataSet dataSet, boolean useConfidence,
			       String pvalue_str) {

	rebuild(dataSet, useConfidence, pvalue_str, null, null);
    }

    public static void rebuild(DataSet dataSet, boolean useConfidence,
			       String pvalue_str, String correct_str,
			       String test_str) {

	double pvalue = Utils.parseDouble(pvalue_str);
	filterPValue(dataSet, pvalue);

	StandardColorCodes cc = (StandardColorCodes)VAMPUtils.getColorCodes
	    (dataSet);

	String max = "" + cc.getMax();
	String min = "" + cc.getMin();

	double beta = cc.getNormalMin() + (cc.getNormalMax() - cc.getNormalMin())/2;
	double alpha = cc.getMax() - beta;
	    
	updateProperties(dataSet, pvalue_str, correct_str, test_str);
	dataSet.setPropertyValue(useProp, useConfidence ? "Confidence" : "Sign");

	DataElement data[] = dataSet.getData();
	for (int n = 0; n < data.length; n++) {
	    DataElement d = data[n];
	    d.removeProperty(VAMPProperties.RatioProp);
	    if (useConfidence) {
		String s = (String)d.getPropertyValue(confidenceProp);
		double v = Utils.parseDouble(s);
		double confidence_ratio = alpha * v + beta;
		d.setPropertyValue(VAMPProperties.ColorCodeProp, Utils.toString(confidence_ratio));
	    }
	    else {
		String s = (String)d.getPropertyValue(statisticProp);
		double v = Utils.parseDouble(s);
		if (v >= 0)
		    d.setPropertyValue(VAMPProperties.ColorCodeProp, max);
		else
		    d.setPropertyValue(VAMPProperties.ColorCodeProp, min);
	    }

	    d.maskProperty(VAMPProperties.RatioProp);
	    d.maskProperty(VAMPProperties.IsNAProp);
	}
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}

    /*
      String getStyle(double d, StandardColorCodes cc) {
      try {
      java.awt.Color c = cc.getColor(d);
      return "style='background-color: #" + ColorResourceBuilder.RGBString(c.getRGB()) + "'";
      }
      catch(Exception e) {
      e.printStackTrace();
      return "";
      }
      }
    */
}
