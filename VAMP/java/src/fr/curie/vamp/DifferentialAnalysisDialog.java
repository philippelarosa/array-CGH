
/*
 *
 * DifferentialAnalysisDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
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

class DifferentialAnalysisDialog extends StandardDialog implements ChangeListener {

    JComboBox testCB;
    JRadioButton signRB, confidenceRB;
    TextField pvalueTF;
    JRadioButton noneRB, bhRB, byRB;
    JRadioButton profileRB, htmlRB, csvRB;

    static final String DIFF_ANA_DIALOG = "DifferentialAnalysisDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(DIFF_ANA_DIALOG,
			  new DifferentialAnalysisDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements,
				    TreeMap oparams) {
	DifferentialAnalysisDialog mrDialog = (DifferentialAnalysisDialog)view.getGlobalContext().
	    get(DIFF_ANA_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    DifferentialAnalysisDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "Differential Analysis Parameteres", true, 1);

	addLine(makeLabel("Test:"));

	testCB = makeComboBox(new String[]{"Student", "Welch"});
	
	GridBagConstraints c;
	JPanel testPanel = new JPanel(new GridBagLayout());
	testPanel.setBackground(getBackground());
	c = Utils.makeGBC(0, 0);
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.WEST;
	testPanel.add(testCB, c);
	c = Utils.makeGBC(1, 0);
	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	testPanel.add(new JLabel(" "), c);

	addLine(testPanel);

	addPadLine();

	addLine(makeLabel("Colorbars:"));

	ButtonGroup group = new ButtonGroup();
	signRB = makeRadioButton("Use sign", group);
	confidenceRB = makeRadioButton("Use confidence", group);

	JPanel signPanel = new JPanel(new GridBagLayout());
	signPanel.setBackground(getBackground());
	c = Utils.makeGBC(0, 0);
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.WEST;
	signPanel.add(signRB, c);
	c = Utils.makeGBC(1, 0);
	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	signPanel.add(makeHelp("  color bar is red if the average in the bottom panel is greater\n  than the average in the top panel and green otherwise"), c);
	addLine(signPanel);

	JPanel confidPanel = new JPanel(new GridBagLayout());
	confidPanel.setBackground(getBackground());
	c = Utils.makeGBC(0, 0);
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.WEST;
	confidPanel.add(confidenceRB, c);
	c = Utils.makeGBC(1, 0);
	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	confidPanel.add(makeHelp("  color bar corresponds to the percentage\n  of profiles without missing values"), c);
			
	addLine(confidPanel);

	addPadLine();

	pvalueTF = makeTextField("", 4);
	addLine(makeLabel("Max P-Value"));
	
	JPanel pvaluePanel = new JPanel(new GridBagLayout());
	pvaluePanel.setBackground(getBackground());
	c = Utils.makeGBC(0, 0);
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.WEST;
	pvaluePanel.add(pvalueTF.getComponent(), c);
	c = Utils.makeGBC(1, 0);
	c.fill = GridBagConstraints.HORIZONTAL;
	c.anchor = GridBagConstraints.WEST;
	c.weightx = 1.0;
	pvaluePanel.add(new JLabel(" [0-1]"), c);
	addLine(pvaluePanel);
	addPadLine();

	group = new ButtonGroup();
	addLine(makeLabel("Multiple Testing:"));
	noneRB = makeRadioButton("None", group);
	bhRB = makeRadioButton("Benjamini-Hochberg", group);
	byRB = makeRadioButton("Benjamini-Yekutieli", group);
	addLine(noneRB);
	addLine(bhRB);
	addLine(byRB);

	addPadLine();
			
	addLine(makeLabel("Results:"));
	group = new ButtonGroup();
	profileRB = makeRadioButton("Display profile", group, true);
	profileRB.addChangeListener(this);
	addLine(profileRB);

	htmlRB = makeRadioButton("HTML report", group);
	htmlRB.addChangeListener(this);

	addLine(htmlRB);
	csvRB = makeRadioButton("CSV report", group);
	csvRB.addChangeListener(this);
	addLine(csvRB);
	
	addPadLine();

	signRB.setSelected(true);
	noneRB.setSelected(true);

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    params.put(DifferentialAnalysisOP.TEST_PARAM,
			       testCB.getSelectedItem());

		    String s = pvalueTF.getText().trim();
		    if (s.length() > 0) {
			double percent = -1;
			try {
			    percent = Utils.parseDouble(s);
			}
			catch(Exception exc) {
			}
		    
			if (percent < 0 || percent > 1) {
			    InfoDialog.pop(globalContext,
					   "Invalid p-value [0-1]: " + s);
			    params = null;
			    return;
			}
		    }

		    params.put(DifferentialAnalysisOP.PVALUE_PARAM, s);

		    int colorbars_mask = 0;

		    if (signRB.isSelected())
			colorbars_mask = DifferentialAnalysisOP.USE_SIGN_MASK;
		    else if (confidenceRB.isSelected())
			colorbars_mask = DifferentialAnalysisOP.USE_CONFIDENCE_MASK;
		    params.put(DifferentialAnalysisOP.COLORBARS_PARAM,
			       new Integer(colorbars_mask));

		    int mtest_mask = 0;

		    if (noneRB.isSelected())
			mtest_mask = DifferentialAnalysisOP.MTEST_NONE;
		    else if (bhRB.isSelected())
			mtest_mask = DifferentialAnalysisOP.MTEST_BH;
		    else if (byRB.isSelected())
			mtest_mask = DifferentialAnalysisOP.MTEST_BY;

		    params.put(DifferentialAnalysisOP.MTEST_PARAM,
			       new Integer(mtest_mask));

		    int result_mask = 0;

		    if (htmlRB.isSelected())
			result_mask = DifferentialAnalysisOP.HTML_REPORT;
		    else if (csvRB.isSelected())
			result_mask = DifferentialAnalysisOP.CSV_REPORT;
		    else if (profileRB.isSelected())
			result_mask = DifferentialAnalysisOP.PROFILE_DISPLAY;

		    params.put(DifferentialAnalysisOP.RESULT_PARAM,
			       new Integer(result_mask));

		    setVisible(false);
		}
	    });


	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = null;
		    setVisible(false);
		}
	    });
	
	epilogue();
    }

    private TreeMap _getParams(View view, Vector graphElements,
			       TreeMap oparams) {
	params = null;
	//params = oparams;
	int size = graphElements.size();
	Object v;

	if (oparams != null &&
	    (v = oparams.get(DifferentialAnalysisOP.TEST_PARAM)) != null)
	    testCB.setSelectedItem(v);

	if (oparams != null &&
	    (v = oparams.get(DifferentialAnalysisOP.PVALUE_PARAM)) != null)
	    pvalueTF.setText((String)v);


	if (oparams != null &&
	    (v = oparams.get(DifferentialAnalysisOP.COLORBARS_PARAM)) != null) {
	    int colorbars_mask = ((Integer)v).intValue();
	    signRB.setSelected((colorbars_mask & DifferentialAnalysisOP.USE_SIGN_MASK) != 0);
	    confidenceRB.setSelected((colorbars_mask & DifferentialAnalysisOP.USE_CONFIDENCE_MASK) != 0);
	}
	else {
	    signRB.setSelected(false);
	    confidenceRB.setSelected(false);
	}

	if (oparams != null &&
	    (v = oparams.get(DifferentialAnalysisOP.MTEST_PARAM)) != null) {
	    int mtest_mask = ((Integer)v).intValue();
	    noneRB.setSelected((mtest_mask & DifferentialAnalysisOP.MTEST_NONE) != 0);
	    bhRB.setSelected((mtest_mask & DifferentialAnalysisOP.MTEST_BH) != 0);
	    byRB.setSelected((mtest_mask & DifferentialAnalysisOP.MTEST_BY) != 0);
	}
	else {
	    noneRB.setSelected(false);
	    bhRB.setSelected(false);
	    byRB.setSelected(false);
	}

	if (oparams != null &&
	    (v = oparams.get(DifferentialAnalysisOP.RESULT_PARAM)) != null) {
	    int result_mask = ((Integer)v).intValue();
	    htmlRB.setSelected((result_mask & DifferentialAnalysisOP.HTML_REPORT) != 0);
	    csvRB.setSelected((result_mask & DifferentialAnalysisOP.CSV_REPORT) != 0);
	    profileRB.setSelected((result_mask & DifferentialAnalysisOP.PROFILE_DISPLAY) != 0);
	}
	else {
	    htmlRB.setSelected(false);
	    csvRB.setSelected(false);
	    profileRB.setSelected(false);
	}

	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
	confidenceRB.setEnabled(profileRB.isSelected());
	signRB.setEnabled(profileRB.isSelected());
    }
}
