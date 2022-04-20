
/*
 *
 * BreakpointFrequencyDialog.java
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

class BreakpointFrequencyDialog extends StandardDialog implements ChangeListener {

    JCheckBox showDensityCB, showBarplotsCB, showAssoCB;
    JRadioButton avgRB, percentRB, numberRB;
    JTextField percentTF;
    JComboBox numberCB;
    JLabel numberL;
    JRadioButton noAlRB, coocAlRB, exclAlRB;
    JRadioButton htmlRB, csvRB, profileRB;
    JComboBox viewCB;
    JCheckBox replaceCB;
    JCheckBox extendsNACB;
    JCheckBox regionFusionCB, fusionCB;
    JTextField pvalueTF, fusionTF, regionFusionTF;
    // for debug
    JTextField traceTF;

    static final String BREAKPOINT_FREQUENCY_DIALOG = "BreakpointFrequencyDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(BREAKPOINT_FREQUENCY_DIALOG,
			  new BreakpointFrequencyDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
	BreakpointFrequencyDialog dlg = (BreakpointFrequencyDialog)view.getGlobalContext().get(BREAKPOINT_FREQUENCY_DIALOG);
	return dlg._getParams(view, graphElements, oparams);
    }

    BreakpointFrequencyDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "Breakpoint Frequency Dialog", true,
	      3);

	addLine(makeLabel("Profile Display:"));
	showBarplotsCB = makeCheckBox("Show barplots", true);
	addLine(showBarplotsCB);
	showDensityCB = makeCheckBox("Show density curve", false);
	addLine(showDensityCB);
	showAssoCB = makeCheckBox("Show associations", true);
	addLine(showAssoCB);

	addLine(makeLabel("Parameters:"));
	extendsNACB = makeCheckBox("Extends NA", false);
	addLine(extendsNACB);

	addLine(makeLabel("Select regions for analysis:"));
	ButtonGroup group = new ButtonGroup();
	avgRB = makeRadioButton("Bkp frequency >= average",
				group);
	avgRB.addChangeListener(this);
	addLine(avgRB);
	percentRB = makeRadioButton("Bkp frequency >=",
				group);
	percentRB.addChangeListener(this);
	percentTF = new JTextField(5);
	addLine(percentRB, percentTF, makeLabel(" %"));

	numberRB = makeRadioButton("Bkp number >=",
				   group);
	numberRB.addChangeListener(this);

	numberCB = makeComboBox();
	numberL = makeLabel("");
	addLine(numberRB, numberCB, numberL);

	regionFusionCB = makeCheckBox("Region Fusion", false);
	regionFusionTF = new JTextField("");
	regionFusionCB.addChangeListener(this);
	addLine(regionFusionCB, regionFusionTF, makeLabel(" Mb"));

	addLine(makeLabel("Association analysis:"));
	group = new ButtonGroup();
	noAlRB = makeRadioButton("No analysis", group, true);
	noAlRB.addChangeListener(this);
	addLine(noAlRB);

	coocAlRB = makeRadioButton("Breakpoint co-occurence", group);
	coocAlRB.addChangeListener(this);
	addLine(coocAlRB);

	exclAlRB = makeRadioButton("Breakpoint exclusion", group);
	exclAlRB.addChangeListener(this);
	addLine(exclAlRB);

	fusionCB = makeCheckBox("Fusion", false);
	fusionTF = new JTextField("");
	fusionCB.addChangeListener(this);
	addLine(fusionCB, fusionTF, makeLabel(" Mb"));

	pvalueTF = new JTextField("");
	addLine(makeLabel("P-Value"), pvalueTF);
	//pvalueTF.setEnabled(false);

	addLine(makeLabel("Results:"));
	group = new ButtonGroup();
	profileRB = makeRadioButton("Display profile", group, true);
	viewCB = makeComboBox(new String[]
	    {BreakpointFrequencyOP.CURRENT_VIEW_PARAM,
	     BreakpointFrequencyOP.NEW_VIEW_PARAM});
	profileRB.addChangeListener(this);
	addLine(profileRB, viewCB);

	htmlRB = makeRadioButton("HTML report", group);
	htmlRB.addChangeListener(this);
	addLine(htmlRB);
	csvRB = makeRadioButton("CSV report", group);
	csvRB.addChangeListener(this);
	addLine(csvRB);
	
	addPadLine();

	replaceCB = makeCheckBox("Replace profile", false);
	replaceCB.addChangeListener(this);
	addLine(replaceCB);

	// for debug
	traceTF = new JTextField();
	addLine(makeLabel("Trace File:"), traceTF);
	//

	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = null;
		    setVisible(false);
		}
	    });

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    params.put(BreakpointFrequencyOP.SHOW_DENSITY_PARAM,
			       (showDensityCB.isSelected() ?
				BreakpointFrequencyOP.TRUE :
				BreakpointFrequencyOP.FALSE));

		    params.put(BreakpointFrequencyOP.SHOW_BARPLOTS_PARAM,
			       (showBarplotsCB.isSelected() ?
				BreakpointFrequencyOP.TRUE :
				BreakpointFrequencyOP.FALSE));

		    params.put(BreakpointFrequencyOP.EXTENDS_NA_PARAM,
			       extendsNACB.isSelected() ?
			       BreakpointFrequencyOP.TRUE :
			       BreakpointFrequencyOP.FALSE);
		    
		    params.put(BreakpointFrequencyOP.REGION_FUSION_PARAM,
			       regionFusionCB.isSelected() ?
			       BreakpointFrequencyOP.TRUE :
			       BreakpointFrequencyOP.FALSE);

		    if (regionFusionCB.isSelected()) {
			String s = regionFusionTF.getText();
			double fusion = -1;
			try {
			    fusion = Utils.parseDouble(s);
			}
			catch(Exception exc) {
			}
			
			if (fusion < 0) {
			    InfoDialog.pop(globalContext,
					   "Invalid region fusion value: " + s);
			    params = null;
			    return;
			}
			params.put(BreakpointFrequencyOP.REGION_FUSION_VALUE,
				   new Double(fusion));
		    }

		    String s = pvalueTF.getText();
		    double pvalue = -1;
		    try {
			pvalue = Utils.parseDouble(s);
		    }
		    catch(Exception exc) {
		    }

		    if (pvalue < 0 || pvalue > 1) {
			InfoDialog.pop(globalContext,
				       "Invalid P-Value: " + s);
			params = null;
			return;
		    }

		    params.put(BreakpointFrequencyOP.PVALUE_PARAM,
			       new Double(pvalue));

		    params.put(BreakpointFrequencyOP.FUSION_PARAM,
			       fusionCB.isSelected() ?
			       BreakpointFrequencyOP.TRUE :
			       BreakpointFrequencyOP.FALSE);

		    if (fusionCB.isSelected()) {
			s = fusionTF.getText();
			double fusion = -1;
			try {
			    fusion = Utils.parseDouble(s);
			}
			catch(Exception exc) {
			}
			
			if (fusion < 0) {
			    InfoDialog.pop(globalContext,
					   "Invalid fusion value: " + s);
			    params = null;
			    return;
			}
			params.put(BreakpointFrequencyOP.FUSION_VALUE,
				   new Double(fusion));
		    }

		    if (avgRB.isSelected()) {
			params.put(BreakpointFrequencyOP.SELECT_PARAM,
				   BreakpointFrequencyOP.AVERAGE);
			params.put(BreakpointFrequencyOP.SELECT_VALUE,
				   null);
		    }
		    else if (percentRB.isSelected()) {
			s = percentTF.getText();
			double percent = -1;
			try {
			    percent = Utils.parseDouble(s);
			}
			catch(Exception exc) {
			}

			if (percent < 0 || percent > 100) {
			    InfoDialog.pop(globalContext,
					   "Invalid percent value: " + s);
			    params = null;
			    return;
			}

			params.put(BreakpointFrequencyOP.SELECT_PARAM,
				   BreakpointFrequencyOP.PERCENT);
			params.put(BreakpointFrequencyOP.SELECT_VALUE,
				   percentTF.getText());
		    }
		    else {
			params.put(BreakpointFrequencyOP.SELECT_PARAM,
				   BreakpointFrequencyOP.NUMBER);

			params.put(BreakpointFrequencyOP.SELECT_VALUE,
				   numberCB.getSelectedItem());
		    }

		    params.put(BreakpointFrequencyOP.VIEW_PARAM,
			       viewCB.getSelectedItem());

		    if (noAlRB.isSelected())
			params.put(BreakpointFrequencyOP.ANALYSIS_PARAM,
				   new Integer(BreakpointFrequencyOP.NO_ANALYSIS));
		    else if (coocAlRB.isSelected())
			params.put(BreakpointFrequencyOP.ANALYSIS_PARAM,
				   new Integer(BreakpointFrequencyOP.COOC_ANALYSIS));
		    else
			params.put(BreakpointFrequencyOP.ANALYSIS_PARAM,
				   new Integer(BreakpointFrequencyOP.EXCL_ANALYSIS));
		    params.put(BreakpointFrequencyOP.SHOW_ASSO_PARAM,
			       showAssoCB.isSelected() ?
			       BreakpointFrequencyOP.TRUE :
			       BreakpointFrequencyOP.FALSE);

		    if (replaceCB.isEnabled() && replaceCB.isSelected())
			params.put(BreakpointFrequencyOP.REPLACE_PARAM,
				   BreakpointFrequencyOP.TRUE);
		    else
			params.put(BreakpointFrequencyOP.REPLACE_PARAM,
				   BreakpointFrequencyOP.FALSE);

		    params.put(BreakpointFrequencyOP.TRACE_FILE,
			       traceTF.getText());

		    int result;
		    if (htmlRB.isSelected())
			result = BreakpointFrequencyOP.HTML_REPORT;
		    else if (csvRB.isSelected())
			result = BreakpointFrequencyOP.CSV_REPORT;
		    else if (profileRB.isSelected())
			result = BreakpointFrequencyOP.PROFILE_DISPLAY;
		    else
			result = 0;

		    params.put(BreakpointFrequencyOP.RESULT_PARAM,
			       new Integer(result));

		    setVisible(false);
		}
	    });

	avgRB.setSelected(true);
	//fusionCB.setEnabled(false);
	epilogue();
    }

    private TreeMap _getParams(View view, Vector _graphElements, TreeMap oparams) {
	//params = oparams;
	params = null;
	Object v;

	Vector graphElements = BreakpointFrequencyOP.getGraphElements(_graphElements);
	replaceCB.setEnabled(graphElements != _graphElements);

	int size = graphElements.size();

	numberCB.removeAllItems();
	numberL.setText(" / " + size);
	for (int n = 1; n <= size; n++)
	    numberCB.addItem(new Integer(n));

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.SHOW_DENSITY_PARAM)) != null)
	    showDensityCB.setSelected(v.equals(BreakpointFrequencyOP.TRUE));
	else
	    showDensityCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.SHOW_BARPLOTS_PARAM)) != null)
	    showBarplotsCB.setSelected(v.equals(BreakpointFrequencyOP.TRUE));
	else
	    showBarplotsCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.EXTENDS_NA_PARAM)) != null)
	    extendsNACB.setSelected(v.equals(BreakpointFrequencyOP.TRUE));
	else
	    extendsNACB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.REGION_FUSION_PARAM)) != null) {
	    regionFusionCB.setSelected(v.equals(BreakpointFrequencyOP.TRUE));
	    v = oparams.get(BreakpointFrequencyOP.REGION_FUSION_VALUE);
	    if (v != null)
		regionFusionTF.setText(v.toString());
	}
	else
	    regionFusionCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.PVALUE_PARAM)) != null)
	    pvalueTF.setText(v.toString());

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.FUSION_PARAM)) != null) {
	    fusionCB.setSelected(v.equals(BreakpointFrequencyOP.TRUE));
	    v = oparams.get(BreakpointFrequencyOP.FUSION_VALUE);
	    if (v != null)
		fusionTF.setText(v.toString());
	}
	else
	    fusionCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.SELECT_PARAM)) != null) {
	    if (v.equals(BreakpointFrequencyOP.AVERAGE))
		avgRB.setSelected(true);
	    else if (v.equals(BreakpointFrequencyOP.PERCENT)) {
		percentRB.setSelected(true);
		v = oparams.get(BreakpointFrequencyOP.SELECT_VALUE);
		if (v != null)
		    percentTF.setText((String)v);
	    }
	    else if (v.equals(BreakpointFrequencyOP.NUMBER)) {
		numberRB.setSelected(true);
		v = oparams.get(BreakpointFrequencyOP.SELECT_VALUE);
		if (v != null)
		    numberCB.setSelectedItem(v);
	    }
	}
	else
	    avgRB.setSelected(true);

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.ANALYSIS_PARAM)) != null) {
	    int value = ((Integer)v).intValue();
	    if (value == BreakpointFrequencyOP.COOC_ANALYSIS)
		coocAlRB.setSelected(true);
	    else if (value == BreakpointFrequencyOP.EXCL_ANALYSIS)
		exclAlRB.setSelected(true);
	    else
		noAlRB.setSelected(true);
	}
	else
	    noAlRB.setSelected(true);

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.VIEW_PARAM)) != null)
	    viewCB.setSelectedItem(v);

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.SHOW_ASSO_PARAM)) != null) {
	    showAssoCB.setSelected(v.equals(BreakpointFrequencyOP.TRUE));
	}
	else
	    showAssoCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(BreakpointFrequencyOP.RESULT_PARAM)) != null) {
	    int result = ((Integer)v).intValue();
	    htmlRB.setSelected((result & BreakpointFrequencyOP.HTML_REPORT) != 0);
	    csvRB.setSelected((result & BreakpointFrequencyOP.CSV_REPORT) != 0);
	    profileRB.setSelected((result & BreakpointFrequencyOP.PROFILE_DISPLAY) != 0);
	}
	else {
	    htmlRB.setSelected(false);
	    csvRB.setSelected(false);
	    profileRB.setSelected(true);
	}
	pop();

	return params;
    }

    public void stateChanged(ChangeEvent e) {
	percentTF.setEnabled(percentRB.isSelected());
	numberCB.setEnabled(numberRB.isSelected());
	showAssoCB.setEnabled(profileRB.isSelected() &&
			      (coocAlRB.isSelected() || exclAlRB.isSelected()));

	showDensityCB.setEnabled(profileRB.isSelected());

	showBarplotsCB.setEnabled(profileRB.isSelected());
	viewCB.setEnabled(profileRB.isSelected());
	fusionCB.setEnabled(coocAlRB.isSelected() || exclAlRB.isSelected());
	fusionTF.setEnabled(fusionCB.isEnabled() && fusionCB.isSelected());
	replaceCB.setEnabled(profileRB.isEnabled() && profileRB.isSelected());
	if (replaceCB.isEnabled() && replaceCB.isSelected())
	    viewCB.setSelectedItem(BreakpointFrequencyOP.CURRENT_VIEW_PARAM);
	regionFusionCB.setEnabled(BreakpointFrequencyOP.SUPPORT_REGION_FUSION);
    }
}
