
/*
 *
 * DifferentialAnalysisReportOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;

class DifferentialAnalysisReportOP extends GraphElementListOperation {

    static final String NAME = "Differential Analysis Reporting";

    static final String REPORT_PARAM = "Report";
    static final int CSV_REPORT = 0x100;
    static final int HTML_REPORT = 0x200;

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.DIFFANA_TYPE, VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE};
    }

public String getReturnedType() {
	return null;
    }

    DifferentialAnalysisReportOP() {
	super(NAME, SHOW_MENU | ON_ALL_AUTO);
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {
	return graphElements.size() == 1;
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {

	int report_mask = ((Integer)params.get(REPORT_PARAM)).intValue();
	if (report_mask == 0)
	    return graphElements;
	try {
	    buildReport((DataSet)graphElements.get(0), (report_mask & HTML_REPORT) != 0);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(view.getGlobalContext(), e.getMessage());
	    return null;
	}

	return graphElements;
    }

    static void buildReport(DataSet dataSet, boolean isHTML) throws java.io.FileNotFoundException {

	DataElement data[] = dataSet.getData();
	File file = DialogUtils.openFileChooser(new java.awt.Frame(), "Save", 0,
						"Differential Analysis: " +
						(isHTML ? "HTML" : "CSV") +
						" Report", true);
	
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
	/*
	builder.addText("Test: " + params.get(TEST_PARAM));
	builder.addVPad();
	builder.addText("P-Value: " + params.get(PVALUE_PARAM));
	builder.addVPad();

	int mtest_mask = ((Integer)params.get(MTEST_PARAM)).intValue();
	String correct;

	if ((mtest_mask & MTEST_BH) != 0)
	    correct = "Benjamini-Hochberg";
	else if ((mtest_mask & MTEST_BY) != 0)
	    correct = "Benjamini-Yekutieli";
	else
	    correct = "None";

	builder.addText("Multiple Testing: " + correct);
	*/

	builder.addText("Test: " + dataSet.getPropertyValue(DifferentialAnalysisOP.testProp));
	builder.addVPad();
	builder.addText("P-Value: " + dataSet.getPropertyValue(DifferentialAnalysisOP.pvalueProp));
	builder.addVPad();

	builder.addText("Multiple Testing: " + dataSet.getPropertyValue(DifferentialAnalysisOP.correctProp));

	builder.addVPad(3);

	builder.startTable(new String[]{"Name", "Chr", "Confidence",
					"Statistic", "P-Value / -log10"});

	StandardColorCodes cc = (StandardColorCodes)VAMPUtils.getColorCodes
	    (dataSet);

	double beta = cc.getNormalMin() + (cc.getNormalMax() - cc.getNormalMin())/2;
	double alpha = cc.getMax() - beta;
	    
	for (int n = 0; n < data.length; n++) {

	    if (VAMPUtils.isMissing(data[n]))
		continue;

	    builder.startRow();
	    builder.addCell(data[n].getID().toString());
	    builder.addCell(VAMPUtils.getChr(data[n]));
	    String s = (String)data[n].getPropertyValue(DifferentialAnalysisOP.confidenceProp);
	    double v = Utils.parseDouble(s);
	    double confidence_ratio = alpha * v + beta;

	    builder.addCell(Utils.performRound(data[n].getPropertyValue(DifferentialAnalysisOP.confidenceProp)), getStyle(confidence_ratio, cc));

	    s = (String)data[n].getPropertyValue(DifferentialAnalysisOP.statisticProp);
	    v = Utils.parseDouble(s);
			    
	    double stat_ratio = v >= 0 ? cc.getMax() : cc.getMin();

	    builder.addCell(Utils.performRound(data[n].getPropertyValue(DifferentialAnalysisOP.statisticProp)), getStyle(stat_ratio, cc));
	    builder.addCell(Utils.performRound(data[n].getPropertyValue(DifferentialAnalysisOP.pvalueProp)) + " / " + Utils.performRound(data[n].getPropertyValue(DifferentialAnalysisOP.mlog10PValueProp)));
	    builder.endRow();
	}
	
	builder.endTable();
	builder.endDocument();
    }

    static final HashMap view_map = new HashMap();

public TreeMap getDefaultParams(View view, Vector graphElements) {
	TreeMap params = (TreeMap)view_map.get(view);
	if (params != null)
	    return params;

	params = new TreeMap();

	params.put(REPORT_PARAM, new Integer(HTML_REPORT));

	view_map.put(view, params);
	return params;
    }

public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = DifferentialAnalysisReportDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	if (params != null)
	    view_map.put(view, params);
	return params;
     }

    static String getStyle(double d, StandardColorCodes cc) {
	try {
	    java.awt.Color c = cc.getColor(d);
	    return "style='background-color: #" + ColorResourceBuilder.RGBString(c.getRGB()) + "'";
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return "";
	}
    }
}
