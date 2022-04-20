
/*
 *
 * FrAGLDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
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

class FrAGLDialog extends StandardDialog implements ChangeListener {

    JRadioButton ratioRB, gnlRB;
    JRadioButton gainRB, ampliconRB, mergeGainAmpliconRB;
    JRadioButton ratioAverageRB, confidenceRB;
    JCheckBox gainAmpliconCB, lossCB;
    JCheckBox displayCB, reportCB;
    JRadioButton htmlRB, csvRB;
    JCheckBox profileCB, karyoCB;
    JCheckBox detailsCB;

    JComboBox supportAltCB;
    JLabel countAltLB;
    JRadioButton numberAltRB, percentAltRB;
    JTextField percentAltTF;

    JComboBox valueConfCB;
    JLabel countConfLB;
    JRadioButton numberConfRB, percentConfRB;
    JTextField percentConfTF;

    static Vector gnl_v;
    static {
	gnl_v = new Vector();
	gnl_v.add(VAMPProperties.GNLProp);
    }

    static final String FRAGL_DIALOG = "FrAGLDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(FRAGL_DIALOG,
			  new FrAGLDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements,
				    TreeMap oparams) {
	FrAGLDialog mrDialog = (FrAGLDialog)view.getGlobalContext().
	    get(FRAGL_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    FrAGLDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext,
	      "Frequency of Amplicon, Gain and Loss", true, 3, "FrAGL Dialog");

	addLine(makeLabel("Based on:"));
	ButtonGroup group = new ButtonGroup();

	ratioRB = makeRadioButton("Ratio", group);
	addLine(ratioRB, makeHelp("Probe status is defined with the Color Legend\n" +
				  "thresholds NormalMin, NormalMax and Amplicon"));
	gnlRB = makeRadioButton("Status", group);
	// exemple:
	gnlRB.setToolTipText("<html><body bgcolor='white'><table cellspacing=5 cellpadding=5><tr><td>Probe is defined with the Color Legend<br>thresholds <i>NormalMin, NormalMax</i> and <i>Amplicon</i></td></tr></table>");
	
	addLine(gnlRB, makeHelp("Gained / Lost Color Codes (GNL)"));

	//addPadLine();

	addPadLine();

	addLine(makeLabel("Types of alterations:"));

	gainAmpliconCB = makeCheckBox("Gain / Amplicon", true);

	group = new ButtonGroup();

	gainRB = makeRadioButton("Gain", group);
	addLine(gainAmpliconCB, gainRB);

	ampliconRB = makeRadioButton("Amplicon", group);
	addLine(makeLabel(""), ampliconRB);

	mergeGainAmpliconRB = makeRadioButton("Merge Gain/Amplicon", group);
	addLine(makeLabel(""), mergeGainAmpliconRB);

	lossCB = makeCheckBox("Loss", true);
	addLine(lossCB);

	addPadLine();
	addLine(makeLabel("Color bars:"));

	group = new ButtonGroup();
	ratioAverageRB = makeRadioButton("Use ratio average", group);
	addLine(ratioAverageRB,
		makeHelp("color bar corresponds to the ratio average\nof probes with the alteration"));
	confidenceRB = makeRadioButton("Use confidence", group);
	addLine(confidenceRB, makeHelp("color bar corresponds to the percentage\nof profiles without missing values"));

	addPadLine();
	group = new ButtonGroup();
	addLine(makeLabel("Minimum support of alterations:"));
	numberAltRB = makeRadioButton("Number", group);
	supportAltCB = makeComboBox();
	countAltLB = makeLabel(" / xxx");
	addLine(numberAltRB, supportAltCB, countAltLB);
	percentAltRB = makeRadioButton("Percentage", group);
	percentAltTF = new JTextField();
	addLine(percentAltRB, percentAltTF, makeLabel(" %"));

	addPadLine();
	group = new ButtonGroup();
	addLine(makeLabel("Minimum value of confidence:"),
		makeHelp("the confidence corresponds to the percentage\nof profiles without missing values"));

	numberConfRB = makeRadioButton("Number", group);
	valueConfCB = makeComboBox();
	countConfLB = makeLabel(" / xxx");
	addLine(numberConfRB, valueConfCB, countConfLB);
	percentConfRB = makeRadioButton("Percentage", group);
	percentConfTF = new JTextField();
	addLine(percentConfRB, percentConfTF, makeLabel(" %"));

	/*
	JPanel panel = new JPanel();
	panel.setBackground(getBackground());
	panel.add(supportAltCB);
	panel.add(countAltLB);
	addLine(panel);
	*/

	addPadLine();
	addLine(makeLabel("Results:"));
	displayCB = makeCheckBox("Display: ", true);

	profileCB = makeCheckBox("Profile", true);
	karyoCB = makeCheckBox("Karyotype", false);
	addLine(displayCB, profileCB);
	addLine(makeLabel(""), karyoCB);

	addPadLine();
	group = new ButtonGroup();
	reportCB = makeCheckBox("Report: ", true);
	htmlRB = makeRadioButton("HTML report", group);
	addLine(reportCB, htmlRB);

	csvRB = makeRadioButton("CSV report", group);
	addLine(makeLabel(""), csvRB);

	detailsCB = makeCheckBox("Report Details", false);
	addLine(makeLabel(""), detailsCB);
	addPadLine();

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    params = new TreeMap();

		    params.put(FrAGLOP.GNL_PARAM,
			       (gnlRB.isSelected() ?
				FrAGLOP.TRUE : FrAGLOP.FALSE));

		    params.put(FrAGLOP.RATIO_PARAM,
			       (ratioRB.isSelected() ?
				FrAGLOP.TRUE : FrAGLOP.FALSE));

		    if (numberAltRB.isSelected()) {
			params.put(FrAGLOP.MIN_SUPPORT_ALT_PARAM,
				   supportAltCB.getSelectedItem());
			params.put(FrAGLOP.MIN_PERCENT_ALT_PARAM,
				   null);
		    }
		    else {
			String s = percentAltTF.getText();
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

			params.put(FrAGLOP.MIN_PERCENT_ALT_PARAM,
				   new Double(percent));
			params.put(FrAGLOP.MIN_SUPPORT_ALT_PARAM,
				   null);
		    }


		    if (numberConfRB.isSelected()) {
			params.put(FrAGLOP.MIN_VALUE_CONF_PARAM,
				   valueConfCB.getSelectedItem());
			params.put(FrAGLOP.MIN_PERCENT_CONF_PARAM,
				   null);
		    }
		    else {
			String s = percentConfTF.getText();
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

			params.put(FrAGLOP.MIN_PERCENT_CONF_PARAM,
				   new Double(percent));
			params.put(FrAGLOP.MIN_VALUE_CONF_PARAM,
				   null);
		    }


		    params.put(FrAGLOP.DETAILS_PARAM,
			       (detailsCB.isSelected() ?
				FrAGLOP.TRUE : FrAGLOP.FALSE));

		    int alt_mask = 0;
		    if (gainAmpliconCB.isSelected()) {
			alt_mask |= FrAGLOP.GAIN_AMPLICON_MASK;
			if (gainRB.isSelected())
			    alt_mask |= FrAGLOP.GAIN_MASK;
			       
			if (ampliconRB.isSelected())
			    alt_mask |= FrAGLOP.AMPLICON_MASK;
			
			if (mergeGainAmpliconRB.isSelected())
			    alt_mask |= FrAGLOP.MERGE_GAIN_AMPLICON_MASK;
		    }
			       
		    if (lossCB.isSelected())
			alt_mask |= FrAGLOP.LOSS_MASK;
			       
		    params.put(FrAGLOP.ALT_MASK_PARAM, new Integer(alt_mask));

		    int color_mask = 0;
		    if (ratioAverageRB.isSelected())
			color_mask |= FrAGLOP.USE_RATIO_AVERAGE_MASK;
		    if (confidenceRB.isSelected())
			color_mask |= FrAGLOP.USE_CONFIDENCE_MASK;

		    params.put(FrAGLOP.COLOR_MASK_PARAM,
			       new Integer(color_mask));

		    int result_mask = 0;
		    if (htmlRB.isSelected())
			result_mask |= FrAGLOP.HTML_REPORT;
		    if (csvRB.isSelected())
			result_mask |= FrAGLOP.CSV_REPORT;
		    if (displayCB.isSelected()) {
			if (profileCB.isSelected())
			    result_mask |= FrAGLOP.DISPLAY_PROFILE;
			if (karyoCB.isSelected())
			    result_mask |= FrAGLOP.DISPLAY_KARYO;
		    }

		    if (reportCB.isSelected())
			params.put(FrAGLOP.REPORT_PARAM, new Boolean(true));

		    if (displayCB.isSelected())
			params.put(FrAGLOP.DISPLAY_PARAM, new Boolean(true));

		    params.put(FrAGLOP.RESULT_PARAM,
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
	

	displayCB.addChangeListener(this);
	reportCB.addChangeListener(this);
	htmlRB.addChangeListener(this);
	csvRB.addChangeListener(this);
	gainAmpliconCB.addChangeListener(this);
	numberAltRB.addChangeListener(this);
	numberConfRB.addChangeListener(this);
	percentAltRB.addChangeListener(this);
	percentConfRB.addChangeListener(this);

	confidenceRB.setSelected(true);
	numberAltRB.setSelected(true);
	numberConfRB.setSelected(true);
	htmlRB.setSelected(true);
	reportCB.setSelected(false);

	epilogue();
    }

    private TreeMap _getParams(View view, Vector graphElements,
			       TreeMap oparams) {
	params = null;
	//params = oparams;

	Object v = null;

	int size = graphElements.size();

	countAltLB.setText(" / " + size);
	supportAltCB.removeAllItems();
	for (int n = 1; n <= size; n++)
	    supportAltCB.addItem(new Integer(n));
	supportAltCB.addItem(FrAGLOP.ALL);

	countConfLB.setText(" / " + size);
	valueConfCB.removeAllItems();
	for (int n = 1; n <= size; n++)
	    valueConfCB.addItem(new Integer(n));
	valueConfCB.addItem(FrAGLOP.ALL);

	if (oparams != null &&
	    (v = oparams.get(FrAGLOP.MIN_SUPPORT_ALT_PARAM)) != null) {
	    supportAltCB.setSelectedItem(v);
	    numberAltRB.setSelected(true);
	    percentAltRB.setSelected(false);
	}
	else
	    supportAltCB.setSelectedItem(FrAGLOP.ALL);

	if (oparams != null &&
	    (v = oparams.get(FrAGLOP.MIN_PERCENT_ALT_PARAM)) != null) {
	    percentAltTF.setText(v.toString());
	    numberAltRB.setSelected(false);
	    percentAltRB.setSelected(true);
	}
	else
	    percentAltTF.setText("");

	if (oparams != null &&
	    (v = oparams.get(FrAGLOP.MIN_VALUE_CONF_PARAM)) != null) {
	    valueConfCB.setSelectedItem(v);
	    numberConfRB.setSelected(true);
	    percentConfRB.setSelected(false);
	}
	else
	    valueConfCB.setSelectedItem(FrAGLOP.ALL);

	if (oparams != null &&
	    (v = oparams.get(FrAGLOP.MIN_PERCENT_CONF_PARAM)) != null) {
	    percentConfTF.setText(v.toString());
	    numberConfRB.setSelected(false);
	    percentConfRB.setSelected(true);
	}
	else
	    percentConfTF.setText("");

	boolean hasGNL = VAMPUtils.hasProperty(view, gnl_v);

	if (!hasGNL) {
	    gnlRB.setSelected(false);
	    gnlRB.setEnabled(false);
	}
	else if (oparams != null &&
	    (v = oparams.get(FrAGLOP.GNL_PARAM)) != null) {
	    gnlRB.setSelected(v.equals(FrAGLOP.TRUE));
	}
	else
	    gnlRB.setSelected(false);

	if (!hasGNL)
	    ratioRB.setSelected(true);
	else if (oparams != null &&
	    (v = oparams.get(FrAGLOP.RATIO_PARAM)) != null) {
	    ratioRB.setSelected(v.equals(FrAGLOP.TRUE));
	}
	else
	    ratioRB.setSelected(true);

	if (oparams != null &&
	    (v = oparams.get(FrAGLOP.DETAILS_PARAM)) != null) {
	    detailsCB.setSelected(v.equals(FrAGLOP.TRUE));
	}
	else
	    detailsCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(FrAGLOP.ALT_MASK_PARAM)) != null) {
	    int alt_mask = ((Integer)v).intValue();
	    gainRB.setSelected((alt_mask & FrAGLOP.GAIN_MASK) != 0);
	    ampliconRB.setSelected((alt_mask & FrAGLOP.AMPLICON_MASK) != 0);
	    mergeGainAmpliconRB.setSelected((alt_mask & FrAGLOP.MERGE_GAIN_AMPLICON_MASK) != 0);
	    lossCB.setSelected((alt_mask & FrAGLOP.LOSS_MASK) != 0);
	    gainAmpliconCB.setSelected((alt_mask & FrAGLOP.GAIN_AMPLICON_MASK) != 0);
	}
	else {
	    gainAmpliconCB.setSelected(true);
	    gainRB.setSelected(true);
	    lossCB.setSelected(true);
	}

	if (oparams != null &&
	    (v = oparams.get(FrAGLOP.COLOR_MASK_PARAM)) != null) {
	    int color_mask = ((Integer)v).intValue();
	    ratioAverageRB.setSelected((color_mask & FrAGLOP.USE_RATIO_AVERAGE_MASK) != 0);
	    confidenceRB.setSelected((color_mask & FrAGLOP.USE_CONFIDENCE_MASK) != 0);
	}
	else {
	    ratioAverageRB.setSelected(true);
	    confidenceRB.setSelected(false);
	}

	if (oparams != null &&
	    (v = oparams.get(FrAGLOP.RESULT_PARAM)) != null) {
	    int result_mask = ((Integer)v).intValue();
	    htmlRB.setSelected((result_mask & FrAGLOP.HTML_REPORT) != 0);
	    csvRB.setSelected((result_mask & FrAGLOP.CSV_REPORT) != 0);
	    if ((result_mask & FrAGLOP.DISPLAY_PROFILE) != 0 ||
		(result_mask & FrAGLOP.DISPLAY_KARYO) != 0) {
		displayCB.setSelected(true);
		if ((result_mask & FrAGLOP.DISPLAY_PROFILE) != 0)
		    profileCB.setSelected(true);
		if ((result_mask & FrAGLOP.DISPLAY_KARYO) != 0)
		    karyoCB.setSelected(true);
	    }
	    else
		displayCB.setSelected(false);
	}
	else {
	    htmlRB.setSelected(false);
	    csvRB.setSelected(false);
	    displayCB.setSelected(false);
	}

	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
	profileCB.setEnabled(displayCB.isSelected());
	karyoCB.setEnabled(displayCB.isSelected());

	htmlRB.setEnabled(reportCB.isSelected());
	csvRB.setEnabled(reportCB.isSelected());
	detailsCB.setEnabled(reportCB.isSelected());

	gainRB.setEnabled(gainAmpliconCB.isSelected());
	ampliconRB.setEnabled(gainAmpliconCB.isSelected());
	mergeGainAmpliconRB.setEnabled(gainAmpliconCB.isSelected());

	percentAltTF.setEnabled(percentAltRB.isSelected());
	supportAltCB.setEnabled(numberAltRB.isSelected());

	percentConfTF.setEnabled(percentConfRB.isSelected());
	valueConfCB.setEnabled(numberConfRB.isSelected());
    }
}
