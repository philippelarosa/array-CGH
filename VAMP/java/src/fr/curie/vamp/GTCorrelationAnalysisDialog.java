
/*
 *
 * GTCorrelationAnalysisDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

class GTCorrelationAnalysisDialog extends StandardDialog implements ChangeListener {

    JRadioButton allRB, regionRB;
    JRadioButton smtRB, gnlRB, ratioRB;
    JRadioButton pearsonRB, spearmanRB;
    JTextField thresholdTF, pvalueTF;
    JRadioButton htmlCB, htmlFullCB, csvCB, profileCB;
    JRadioButton profileCcCB, profilePvCB, profileFwerWgCB, profileFwerBcCB, profileFdrWgCB, profileFdrBcCB;

    static final String GTCA_DIALOG = "GTCA";

    public static void init(GlobalContext globalContext) {
	globalContext.put(GTCA_DIALOG,
			  new GTCorrelationAnalysisDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
	GTCorrelationAnalysisDialog mrDialog = (GTCorrelationAnalysisDialog)view.getGlobalContext().get(GTCA_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    GTCorrelationAnalysisDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "GT(CA) Correlation Analysis Dialog",
	      true, 3);

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    params.put(GTCorrelationAnalysisOP.SCOPE_PARAM,
			       (allRB.isSelected() ?
			       GTCorrelationAnalysisOP.ALL :
				GTCorrelationAnalysisOP.REGIONS));

		    params.put(GTCorrelationAnalysisOP.CRITERIA_PARAM,
			       smtRB.isSelected() ?
			       GTCorrelationAnalysisOP.SMOOTHING_VALUE :
			       GTCorrelationAnalysisOP.RATIO_VALUE);

		    String s = "";
		    if (pearsonRB.isSelected())
			s = GTCorrelationAnalysisOP.PEARSON;
		    else if (spearmanRB.isSelected())
			s = GTCorrelationAnalysisOP.SPEARMAN;

		    params.put(GTCorrelationAnalysisOP.CORRELATION_PARAM, s);

		    // s = thresholdTF.getText();
// 		    double v = -1;
// 		    try {
// 			v = Utils.parseDouble(s);
// 		    }
// 		    catch(Exception exc) {
// 		    }

// 		    if (v < 0. || v > 1.) {
// 			InfoDialog.pop(globalContext,
// 				       "Invalid correlation value: " + s);
// 			params = null;
// 			return;
// 		    }

// 		    params.put(GTCorrelationAnalysisOP.THRESHOLD_PARAM, s);

// 		    s = pvalueTF.getText();
// 		    v = -1;
// 		    try {
// 			v = Utils.parseDouble(s);
// 		    }
// 		    catch(Exception exc) {
// 		    }

// 		    if (v < 0. || v > 1.) {
// 			InfoDialog.pop(globalContext,
// 				       "Invalid p-value: " + s);
// 			params = null;
// 			return;
// 		    }

// 		    params.put(GTCorrelationAnalysisOP.PVALUE_PARAM, s);

		    int result_mask = 0;
		    String result = "";
		    // if (profileCcCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.CorrelCoef;
// 		    if (profilePvCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.Pvalue;
// 		    if (profileFwerWgCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.FwerWg;
// 		    if (profileFwerBcCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.FwerBc;
// 		    if (profileFdrWgCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.FdrWg;
// 		    if (profileFdrBcCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.FdrBc;
// 		    params.put(GTCorrelationAnalysisOP.RESULT_PARAM,
// 			       new Integer(result_mask));

		    if (profileCcCB.isSelected())
			result += GTCorrelationAnalysisOP.CORREL;
		    if (profilePvCB.isSelected())
			result += GTCorrelationAnalysisOP.PVALUE;
		    if (profileFwerWgCB.isSelected())
			result += GTCorrelationAnalysisOP.FWERWG;
		    if (profileFwerBcCB.isSelected())
			result += GTCorrelationAnalysisOP.FWERBC;
		    if (profileFdrWgCB.isSelected())
			result += GTCorrelationAnalysisOP.FDRWG;
		    if (profileFdrBcCB.isSelected())
			result += GTCorrelationAnalysisOP.FDRBC;

 		    params.put(GTCorrelationAnalysisOP.RESULT_PARAM, result);

		    setVisible(false);
		}
	    });


	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = null;
		    setVisible(false);
		}
	    });


	addLine(makeLabel(" Scope:"));

	ButtonGroup group = new ButtonGroup();
	allRB = makeRadioButton("All", group);
	allRB.addChangeListener(this);
	addLine(allRB);

	regionRB = makeRadioButton("Regions", group);
	regionRB.addChangeListener(this);
	regionRB.setEnabled(false);
	addLine(regionRB);

	addPadLine();

	addLine(makeLabel(" Criteria:"));

	group = new ButtonGroup();
	ratioRB = makeRadioButton("Ratio", group);
	ratioRB.addChangeListener(this);
	addLine(ratioRB);
	smtRB = makeRadioButton("Smoothing", group);
	smtRB.addChangeListener(this);
	addLine(smtRB);

// 	gnlRB = makeRadioButton("GNL", group);
// 	gnlRB.addChangeListener(this);
// 	addLine(gnlRB);

	

	addPadLine();

	addLine(makeLabel(" Correlation:"));

	group = new ButtonGroup();
	pearsonRB = makeRadioButton("Pearson", group);
	addLine(pearsonRB);
	spearmanRB = makeRadioButton("Spearman", group);
	addLine(spearmanRB);
	pearsonRB.setSelected(true);

	addPadLine();

	// addLine(makeLabel(" Min Correlation:"));

// 	thresholdTF = new JTextField();
// 	addLine(thresholdTF, makeLabel("[0,1]"));

// 	addLine(makeLabel(" Max P-Value:"));

// 	pvalueTF = new JTextField();
// 	addLine(pvalueTF, makeLabel("[0,1]"));

// 	addPadLine();

	addLine(makeLabel(" Results:"));

	group = new ButtonGroup();
// 	htmlCB = makeRadioButton("HTML report: ", group);
// 	addLine(htmlCB, makeHelp("HTML report help"));

// 	htmlFullCB = makeRadioButton("HTML full report: ", group);
// 	addLine(htmlFullCB, makeHelp("HTML full report help"));

// 	csvCB = makeRadioButton("CSV report: ", group);
// 	addLine(csvCB, makeHelp("export CSV help"));

	profileCcCB = makeRadioButton("Correlation coefficient", group);
	addLine(profileCcCB);

	profilePvCB = makeRadioButton("p-value", group);
	addLine(profilePvCB);

	profileFwerWgCB = makeRadioButton("FWER - whole genome", group);
	addLine(profileFwerWgCB);

	profileFwerBcCB = makeRadioButton("FWER - by Chromosome", group);
	addLine(profileFwerBcCB);

	profileFdrWgCB = makeRadioButton("FDR - whole genome", group);
	addLine(profileFdrWgCB);

	profileFdrBcCB = makeRadioButton("FDR - by Chromosome", group);
	addLine(profileFdrBcCB);

	profileCcCB.setSelected(true);

	addPadLine();

	allRB.setSelected(true);
	epilogue();
    }

    private TreeMap _getParams(View view, Vector graphElements, TreeMap oparams) {
	Object v;
	if (view.getRegions().size() == 0) {
	    allRB.setSelected(true);
	    regionRB.setEnabled(false);
	}
	else {
	    regionRB.setEnabled(true);
	    if (oparams != null &&
		(v = oparams.get(GTCorrelationAnalysisOP.SCOPE_PARAM)) != null) {
		allRB.setSelected(v.equals(GTCorrelationAnalysisOP.ALL));
	    }
	    else
		allRB.setSelected(true);
	}

	if (oparams != null &&
	    (v = oparams.get(GTCorrelationAnalysisOP.CRITERIA_PARAM)) != null) {
	    smtRB.setSelected(v.equals(GTCorrelationAnalysisOP.SMOOTHING_VALUE));
	}
	else
	    smtRB.setSelected(true);

	if (oparams != null &&
	    (v = oparams.get(GTCorrelationAnalysisOP.CORRELATION_PARAM)) != null) {
	    pearsonRB.setSelected(v.equals(GTCorrelationAnalysisOP.PEARSON));
	    spearmanRB.setSelected(v.equals(GTCorrelationAnalysisOP.SPEARMAN));
	}
	else
	    smtRB.setSelected(true);

// 	if (oparams != null &&
// 	    (v = oparams.get(GTCorrelationAnalysisOP.THRESHOLD_PARAM)) != null) {
// 	    thresholdTF.setText((String)v);
// 	}
// 	else
// 	    thresholdTF.setText("");

// 	if (oparams != null &&
// 	    (v = oparams.get(GTCorrelationAnalysisOP.PVALUE_PARAM)) != null) {
// 	    pvalueTF.setText((String)v);
// 	}
// 	else
// 	    pvalueTF.setText("");

	if (oparams != null &&
	    (v = oparams.get(GTCorrelationAnalysisOP.RESULT_PARAM)) != null) {
	    ;
	    // int result_mask = ((Integer)v).intValue();
	    // htmlCB.setSelected((result_mask & GTCorrelationAnalysisOP.HTML_REPORT) != 0);
// 	    htmlFullCB.setSelected((result_mask & GTCorrelationAnalysisOP.HTML_FULL_REPORT) != 0);
	   //  csvCB.setSelected((result_mask & GTCorrelationAnalysisOP.CSV_REPORT) != 0);
	    // profileCB.setSelected((result_mask & GTCorrelationAnalysisOP.) != 0); 
           //  profileCcCB.setSelected((result_mask & GTCorrelationAnalysisOP.CorrelCoef) != 0); 
	  //   profilePvCB.setSelected((result_mask & GTCorrelationAnalysisOP.Pvalue) != 0); 
// 	    profileFwerWgCB.setSelected((result_mask & GTCorrelationAnalysisOP.FwerWg) != 0); 
// 	    profileFwerBcCB.setSelected((result_mask & GTCorrelationAnalysisOP.FwerBc) != 0); 
// 	    profileFdrWgCB.setSelected((result_mask & GTCorrelationAnalysisOP.FdrWg) != 0); 
// 	    profileFdrBcCB.setSelected((result_mask & GTCorrelationAnalysisOP.FdrBc) != 0); 
	}
	else {
	   //  htmlCB.setSelected(false);
// 	    htmlFullCB.setSelected(false);
	  //csvCB.setSelected(false);
	    ;
	  //  profileCcCB.setSelected(true);
// 	   profilePvCB.setSelected(false);
// 	   profileFwerWgCB.setSelected(false);
// 	   profileFwerBcCB.setSelected(false);
// 	   profileFdrWgCB.setSelected(false);
// 	   profileFdrBcCB.setSelected(false);
	}


	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
    }
}

