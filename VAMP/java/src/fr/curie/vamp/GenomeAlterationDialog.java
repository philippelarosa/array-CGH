
/*
 *
 * GenomeAlterationDialog.java
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

class GenomeAlterationDialog extends JDialog implements ChangeListener {

    JRadioButton minRegCB, recRegCB; // recBkpCB;
    JRadioButton allChrRB, exclSexChrRB;
    JCheckBox rmRegCB;
    JCheckBox exclXChrCB, exclYChrCB;
    JPanel mainPanel;
    JRadioButton minNumberAltRB, minPercentAltRB;
    JComboBox minNumberAltCB;
    JTextField minPercentAltTF;
    JCheckBox dspRegionCB, reportCB;
    JRadioButton csvRB, htmlRB;

    JRadioButton minNumberBkpRB, minPercentBkpRB;
    JComboBox minNumberBkpCB;
    JTextField minPercentBkpTF;
    View view;

    JTextField tolerationTF;
    JCheckBox outlierCB, extendedNACB, widenRegionCB;
    JCheckBox gainedCB, lostCB, amplCB, normalCB, mergeGainedAmplCB, ignoreAmplCB;
    JLabel countLB, count2LB;
    GlobalContext globalContext;
    static Font helpFont = new Font("MonoSpaced", Font.PLAIN, 9);

    TreeMap params;
    static final String GENOME_ALTERATION_DIALOG = "GenomeAlterationDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(GENOME_ALTERATION_DIALOG,
			  new GenomeAlterationDialog(new Frame(), globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
	GenomeAlterationDialog mrDialog = (GenomeAlterationDialog)view.getGlobalContext().get(GENOME_ALTERATION_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    private Component makePad() {
	return new JLabel();
    }

    private JButton makeButton(String s) {
	JButton b = new JButton(s);
	b.setFont(VAMPResources.getFont(VAMPResources.DIALOG_BUTTON_FONT));
	b.setForeground(VAMPResources.getColor
			(VAMPResources.DIALOG_BUTTON_FG));
	b.setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_BG));
	return b;
    }

    GenomeAlterationDialog(Frame f, GlobalContext _globalContext) {
	super(f, "Genome Alteration Dialog", Config.DEFAULT_MODAL);
	this.globalContext = _globalContext;
	Color bgColor = VAMPResources.getColor(VAMPResources.DIALOG_BG);

	getContentPane().setLayout(new BorderLayout());
	JPanel buttonPanel = new JPanel(new FlowLayout());
	JButton b = makeButton("OK");
	buttonPanel.add(b);
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (params == null)
			params = new TreeMap();

		    if (!minRegCB.isSelected() && !recRegCB.isSelected()) {
			InfoDialog.pop(globalContext,
				       "Must select Display Alterations");
			return;
		    }

		    params.put(GenomeAlterationOP.SUPPRESS_REGIONS,
			       rmRegCB.isSelected() ?
			       GenomeAlterationOP.TRUE :
			       GenomeAlterationOP.FALSE);


		    if (minNumberAltRB.isSelected()) {
			params.put(GenomeAlterationOP.MIN_NUMBER_ALT,
				   minNumberAltCB.getSelectedItem());
			params.put(GenomeAlterationOP.MIN_PERCENT_ALT,
				   null);
		    }
		    else {
			String s = minPercentAltTF.getText();
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

			params.put(GenomeAlterationOP.MIN_PERCENT_ALT,
				   new Double(percent));
			params.put(GenomeAlterationOP.MIN_NUMBER_ALT,
				   null);
		    }


		    if (minNumberBkpRB.isSelected()) {
			params.put(GenomeAlterationOP.MIN_NUMBER_BKP,
				   minNumberBkpCB.getSelectedItem());
			params.put(GenomeAlterationOP.MIN_PERCENT_BKP,
				   null);
		    }
		    else {
			String s = minPercentBkpTF.getText();
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

			params.put(GenomeAlterationOP.MIN_PERCENT_BKP,
				   new Double(percent));
			params.put(GenomeAlterationOP.MIN_NUMBER_BKP,
				   null);
		    }

		    String s = tolerationTF.getText();
		    int toleration = -1;
		    if (s.trim().length() == 0)
			toleration = 0;
		    else {
			toleration = -1;
			try {
			    toleration = Integer.parseInt(s);
			}
			catch(Exception exc) {
			}
		    
			if (toleration < 0) {
			    InfoDialog.pop(globalContext,
					   "Invalid toleration value: " + s);
			    params = null;
			    return;
			}
		    }

		    params.put(GenomeAlterationOP.TOLERATION,
			       new Integer(toleration));

		    params.put(GenomeAlterationOP.OUTLIERS,
			       outlierCB.isSelected() ?
			       GenomeAlterationOP.TRUE :
			       GenomeAlterationOP.FALSE);

		    params.put(GenomeAlterationOP.EXTENDED_NA,
			       extendedNACB.isSelected() ?
			       GenomeAlterationOP.TRUE :
			       GenomeAlterationOP.FALSE);

		    params.put(GenomeAlterationOP.WIDEN_REGIONS,
			       widenRegionCB.isSelected() ?
			       GenomeAlterationOP.TRUE :
			       GenomeAlterationOP.FALSE);


		    int compute_mask = 0;
		    if (minRegCB.isSelected())
			compute_mask |= GenomeAlterationOP.MINIMAL_REGIONS_MASK;
		    if (recRegCB.isSelected())
			compute_mask |= GenomeAlterationOP.RECURRENT_REGIONS_MASK;

		    params.put(GenomeAlterationOP.COMPUTE_MASK,
			       new Integer(compute_mask));

		    int scope_mask = 0;
		    if (allChrRB.isSelected())
			scope_mask |= GenomeAlterationOP.ALL_CHR_MASK;
		    else if (exclSexChrRB.isSelected()) {
			if (exclXChrCB.isSelected())
			    scope_mask |= GenomeAlterationOP.EXCL_X_CHR_MASK;
			if (exclYChrCB.isSelected())
			    scope_mask |= GenomeAlterationOP.EXCL_Y_CHR_MASK;
		    }

		    params.put(GenomeAlterationOP.SCOPE_MASK,
			       new Integer(scope_mask));

		    int alt_mask = 0;

		    if (gainedCB.isSelected())
			alt_mask |= GenomeAlterationOP.GAINED_MASK;
		    if (lostCB.isSelected())
			alt_mask |= GenomeAlterationOP.LOST_MASK;
		    if (amplCB.isSelected())
			alt_mask |= GenomeAlterationOP.AMPL_MASK;
		    if (normalCB.isSelected())
			alt_mask |= GenomeAlterationOP.NORMAL_MASK;
		    if (mergeGainedAmplCB.isSelected())
			alt_mask |= GenomeAlterationOP.MERGE_GAINED_AMPL_MASK;
		    if (ignoreAmplCB.isSelected())
			alt_mask |= GenomeAlterationOP.IGNORE_AMPL_MASK;

		    params.put(GenomeAlterationOP.ALT_MASK,
			       Utils.toString(alt_mask));

		    int res_mask = 0;

		    if (dspRegionCB.isSelected())
			res_mask |= GenomeAlterationOP.DISPLAY_REGIONS_MASK;
		    if (reportCB.isSelected() && csvRB.isSelected())
			res_mask |= GenomeAlterationOP.CSV_REPORT_MASK;
		    if (reportCB.isSelected() && htmlRB.isSelected())
			res_mask |= GenomeAlterationOP.HTML_REPORT_MASK;

		    params.put(GenomeAlterationOP.RESULT_MASK,
			       Utils.toString(res_mask));

		    setVisible(false);
		}
	    });


	b = makeButton("Cancel");
	buttonPanel.add(b);
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = null;
		    setVisible(false);
		}
	    });


	JPanel p = new JPanel(new FlowLayout());
	p.add(new JLabel("Genome Alteration Options"));
	p.setBackground(bgColor);
	getContentPane().add(p, BorderLayout.NORTH);
	getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	//	mainPanel = new JPanel(new GridLayout(33, 3));
	//	mainPanel = new JPanel(new GridLayout(31, 3));
	mainPanel = new JPanel(new GridLayout(30, 3));

	//	mainPanel.add(new JLabel(" Compute alterations:"));
	mainPanel.add(new JLabel(" Display alterations:"));
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	ButtonGroup group = new ButtonGroup();
	minRegCB = new JRadioButton("Minimal regions");
	group.add(minRegCB);

	minRegCB.setBackground(bgColor);
	mainPanel.add(minRegCB);

	rmRegCB = new JCheckBox("");
	rmRegCB.setBackground(bgColor);
	mainPanel.add(rmRegCB);
	mainPanel.add(makePad());

	recRegCB = new JRadioButton("Recurrent regions");
	group.add(recRegCB);

	recRegCB.setBackground(bgColor);
	mainPanel.add(recRegCB);
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	//recBkpCB.addChangeListener(this);

	mainPanel.add(makePad());
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	mainPanel.add(new JLabel(" Scope:"));
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	group = new ButtonGroup();
	allChrRB = new JRadioButton("All chromosomes");
	allChrRB.setBackground(bgColor);
	allChrRB.addChangeListener(this);
	group.add(allChrRB);
	mainPanel.add(allChrRB);
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	exclSexChrRB = new JRadioButton("Or:");
	exclSexChrRB.setBackground(bgColor);
	exclSexChrRB.addChangeListener(this);
	mainPanel.add(exclSexChrRB);
	group.add(exclSexChrRB);
	exclXChrCB = new JCheckBox("Exclude X chromosome");
	exclXChrCB.setBackground(bgColor);
	mainPanel.add(exclXChrCB);
	mainPanel.add(makePad());

	mainPanel.add(makePad());
	exclYChrCB = new JCheckBox("Exclude Y chromosome");
	exclYChrCB.setBackground(bgColor);
	mainPanel.add(exclYChrCB);
	mainPanel.add(makePad());

	mainPanel.add(new JLabel(" Minimum support of alteration"));
	mainPanel.add(new JLabel("s:"));
	mainPanel.add(makePad());

	group = new ButtonGroup();
	minNumberAltRB = new JRadioButton("Number");
	group.add(minNumberAltRB);
	minNumberAltRB.addChangeListener(this);
	minNumberAltRB.setBackground(bgColor);
	mainPanel.add(minNumberAltRB);

	minNumberAltCB = new JComboBox();
	minNumberAltCB.setBackground(bgColor);
	mainPanel.add(minNumberAltCB);
	countLB = new JLabel(" / xxx");
	mainPanel.add(countLB);

	minPercentAltRB = new JRadioButton("Percentage");
	group.add(minPercentAltRB);
	minPercentAltRB.addChangeListener(this);
	minPercentAltRB.setBackground(bgColor);

	mainPanel.add(minPercentAltRB);
	minPercentAltTF = new JTextField();
	mainPanel.add(minPercentAltTF);
	mainPanel.add(new JLabel(" % of arrays"));

	mainPanel.add(makePad());
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	mainPanel.add(new JLabel(" Minimum breakpoint support"));
	mainPanel.add(new JLabel("for minimal regions:"));
	mainPanel.add(makePad());

	group = new ButtonGroup();
	minNumberBkpRB = new JRadioButton("Number");
	group.add(minNumberBkpRB);
	minNumberBkpRB.addChangeListener(this);
	minNumberBkpRB.setBackground(bgColor);
	mainPanel.add(minNumberBkpRB);

	minNumberBkpCB = new JComboBox();
	minNumberBkpCB.setBackground(bgColor);
	//mainPanel.add(new JLabel("     Number"));
	mainPanel.add(minNumberBkpCB);
	count2LB = new JLabel(" / xxx");
	mainPanel.add(count2LB);

	minPercentBkpRB = new JRadioButton("Percentage");
	group.add(minPercentBkpRB);
	minPercentBkpRB.addChangeListener(this);
	minPercentBkpRB.setBackground(bgColor);

	mainPanel.add(minPercentBkpRB);
	minPercentBkpTF = new JTextField();
	mainPanel.add(minPercentBkpTF);
	mainPanel.add(new JLabel(" % of arrays"));

	mainPanel.add(makePad());
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	tolerationTF = new JTextField();
	//	mainPanel.add(new JLabel("     Tolerance (clone nb)"));
	mainPanel.add(new JLabel(" Toleration (clone number)"));
	mainPanel.add(tolerationTF);
	mainPanel.add(new JLabel(" on breakpoint position  "));

	mainPanel.add(makePad());
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	mainPanel.add(new JLabel(" Options:"));
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	outlierCB = new JCheckBox("outliers: ");
	outlierCB.setBackground(bgColor);
	mainPanel.add(outlierCB);
	MLLabel ml = new MLLabel();
	ml.setBackground(bgColor);
	ml.setFont(helpFont);
	ml.setText(GenomeAlterationOP.OUTLIER_HELP);
	mainPanel.add(ml);
	mainPanel.add(makePad());


	extendedNACB = new JCheckBox("extends NA: ");
	/*
	extendedNACB.setBackground(bgColor);
	mainPanel.add(extendedNACB);
	ml = new MLLabel();
	ml.setBackground(bgColor);
	ml.setFont(helpFont);
	ml.setText(GenomeAlterationOP.EXTENDED_NA_HELP);
	mainPanel.add(ml);
	mainPanel.add(makePad());

	extendedNACB.setEnabled(false);
	*/

	widenRegionCB = new JCheckBox("widens regions: ");
	widenRegionCB.setBackground(bgColor);
	mainPanel.add(widenRegionCB);
	ml = new MLLabel();
	ml.setBackground(bgColor);
	ml.setFont(helpFont);
	ml.setText(GenomeAlterationOP.WIDEN_REGION_HELP);
	mainPanel.add(ml);
	mainPanel.add(makePad());

	mainPanel.add(makePad());
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	mainPanel.add(new JLabel(" Types of alterations:"));
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	gainedCB = new JCheckBox("Gain", true);
	gainedCB.setBackground(bgColor);
	lostCB = new JCheckBox("Loss", true);
	lostCB.setBackground(bgColor);
	amplCB = new JCheckBox("Amplicon", true);
	amplCB.setBackground(bgColor);
	normalCB = new JCheckBox("Normal", false);
	normalCB.setBackground(bgColor);
	mergeGainedAmplCB = new JCheckBox("Merge Gain and Amplicon", false);
	mergeGainedAmplCB.setBackground(bgColor);
	ignoreAmplCB = new JCheckBox("Ignore amplicon", false);
	ignoreAmplCB.setBackground(bgColor);

	gainedCB.addChangeListener(this);
	lostCB.addChangeListener(this);
	amplCB.addChangeListener(this);
	normalCB.addChangeListener(this);
	mergeGainedAmplCB.addChangeListener(this);
	ignoreAmplCB.addChangeListener(this);

	mainPanel.add(gainedCB);
	mainPanel.add(lostCB);
	mainPanel.add(makePad());

	mainPanel.add(amplCB);
	mainPanel.add(normalCB);
	mainPanel.add(makePad());

	mainPanel.add(mergeGainedAmplCB);
	mainPanel.add(ignoreAmplCB);
	mainPanel.add(makePad());

	mainPanel.add(new JLabel(" Results:"));
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	dspRegionCB = new JCheckBox("Display regions", true);
	dspRegionCB.setBackground(bgColor);
	mainPanel.add(dspRegionCB);
	mainPanel.add(makePad());
	mainPanel.add(makePad());

	reportCB = new JCheckBox("Report:", true);
	reportCB.setBackground(bgColor);
	mainPanel.add(reportCB);

	group = new ButtonGroup();
	csvRB = new JRadioButton("CSV");
	csvRB.setBackground(bgColor);
	group.add(csvRB);
	mainPanel.add(csvRB);
	mainPanel.add(makePad());

	htmlRB = new JRadioButton("HTML");
	htmlRB.setBackground(bgColor);
	group.add(htmlRB);
	mainPanel.add(makePad());
	mainPanel.add(htmlRB);
	mainPanel.add(makePad());
	csvRB.setSelected(true);

	reportCB.addChangeListener(this);

	getContentPane().add(mainPanel, BorderLayout.CENTER);

	getContentPane().setBackground(bgColor);
	mainPanel.setBackground(bgColor);
	buttonPanel.setBackground(bgColor);
	minNumberAltRB.setSelected(true);
	minNumberBkpRB.setSelected(true);

	minRegCB.addChangeListener(this);
	recRegCB.addChangeListener(this);
	minRegCB.setSelected(true);
    }

    private TreeMap _getParams(View view, Vector graphElements, TreeMap oparams) {
	this.view = view;
	stateChanged(null);
	params = oparams;
	//params = null;

	int size = graphElements.size();
	countLB.setText(" / " + size + " arrays");
	count2LB.setText(" / " + size + " arrays");

	minNumberAltCB.removeAllItems();
	for (int n = 1; n <= size; n++)
	    minNumberAltCB.addItem(new Integer(n));
	minNumberAltCB.addItem(GenomeAlterationOP.ALL);

	minNumberBkpCB.removeAllItems();
	for (int n = 1; n <= size; n++)
	    minNumberBkpCB.addItem(new Integer(n));
	minNumberBkpCB.addItem(GenomeAlterationOP.ALL);

	Object v;
	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.MIN_NUMBER_ALT)) != null) {
	    minNumberAltCB.setSelectedItem(v);
	    minNumberAltRB.setSelected(true);
	    minPercentAltRB.setSelected(false);
	}
	else
	    minNumberAltCB.setSelectedItem(GenomeAlterationOP.ALL);

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.MIN_PERCENT_ALT)) != null) {
	    minPercentAltTF.setText(v.toString());
	    minPercentAltRB.setSelected(true);
	    minNumberAltRB.setSelected(false);
	}
	else
	    minPercentAltTF.setText("");

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.MIN_NUMBER_BKP)) != null) {
	    minNumberBkpCB.setSelectedItem(v);
	    minNumberBkpRB.setSelected(true);
	    minPercentBkpRB.setSelected(false);
	}
	else
	    minNumberBkpCB.setSelectedItem(Utils.toString(1));

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.MIN_PERCENT_BKP)) != null) {
	    minPercentBkpTF.setText(v.toString());
	    minPercentBkpRB.setSelected(true);
	    minNumberBkpRB.setSelected(false);
	}
	else
	    minPercentBkpTF.setText("");

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.OUTLIERS)) != null)
	    outlierCB.setSelected(v.equals
				  (GenomeAlterationOP.TRUE));
	else
	    outlierCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.EXTENDED_NA)) != null) {
	    extendedNACB.setSelected(v.equals
				     (GenomeAlterationOP.TRUE));
	}
	else
	    extendedNACB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.WIDEN_REGIONS)) != null)
	    widenRegionCB.setSelected(v.equals
				     (GenomeAlterationOP.TRUE));
	else
	    widenRegionCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.ALT_MASK)) != null) {
	    int alt_mask = Integer.parseInt((String)v);
	    gainedCB.setSelected((alt_mask & GenomeAlterationOP.GAINED_MASK) != 0);
	    lostCB.setSelected((alt_mask & GenomeAlterationOP.LOST_MASK) != 0);
	    amplCB.setSelected((alt_mask & GenomeAlterationOP.AMPL_MASK) != 0);
	    normalCB.setSelected((alt_mask & GenomeAlterationOP.NORMAL_MASK) != 0);
	    mergeGainedAmplCB.setSelected((alt_mask & GenomeAlterationOP.MERGE_GAINED_AMPL_MASK) != 0);
	    ignoreAmplCB.setSelected((alt_mask & GenomeAlterationOP.IGNORE_AMPL_MASK) != 0);
	}
	else {
	    gainedCB.setSelected(true);
	    lostCB.setSelected(true);
	    amplCB.setSelected(true);
	    normalCB.setSelected(false);
	    mergeGainedAmplCB.setSelected(false);
	    ignoreAmplCB.setSelected(false);
	}

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.SCOPE_MASK)) != null) {
	    int scope_mask = ((Integer)v).intValue();
	    allChrRB.setSelected((scope_mask & GenomeAlterationOP.ALL_CHR_MASK) != 0);
	    exclSexChrRB.setSelected((scope_mask & GenomeAlterationOP.ALL_CHR_MASK) == 0);
	    exclXChrCB.setSelected((scope_mask & GenomeAlterationOP.EXCL_X_CHR_MASK) != 0);
	    exclYChrCB.setSelected((scope_mask & GenomeAlterationOP.EXCL_Y_CHR_MASK) != 0);
	}
	else {
	    allChrRB.setSelected(true);
	    exclSexChrRB.setSelected(false);
	}

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.COMPUTE_MASK)) != null) {
	    int compute_mask = ((Integer)v).intValue();
	    minRegCB.setSelected((compute_mask & GenomeAlterationOP.MINIMAL_REGIONS_MASK) != 0);
	    recRegCB.setSelected((compute_mask & GenomeAlterationOP.RECURRENT_REGIONS_MASK) != 0);
	}
	else {
	    minRegCB.setSelected(false);
	    recRegCB.setSelected(false);
	    //recBkpCB.setSelected(false);
	}

	if (oparams != null &&
	    (v = oparams.get(GenomeAlterationOP.RESULT_MASK)) != null) {
	    int res_mask = Integer.parseInt((String)v);
	    dspRegionCB.setSelected((res_mask & GenomeAlterationOP.DISPLAY_REGIONS_MASK) != 0);
	    reportCB.setSelected((res_mask &
				  (GenomeAlterationOP.CSV_REPORT_MASK |
				   GenomeAlterationOP.HTML_REPORT_MASK)) != 0);
	    csvRB.setSelected((res_mask & GenomeAlterationOP.CSV_REPORT_MASK) != 0);
	    htmlRB.setSelected((res_mask & GenomeAlterationOP.HTML_REPORT_MASK) != 0);
	}
	else {
	    dspRegionCB.setSelected(true);
	    reportCB.setSelected(false);
	    csvRB.setSelected(false);
	    htmlRB.setSelected(false);
	}

	pack();

	Utils.centerOnScreen(this);
	return params;
    }

    public void stateChanged(ChangeEvent e) {
	minPercentAltTF.setEnabled(minPercentAltRB.isSelected());
	minNumberAltCB.setEnabled(minNumberAltRB.isSelected());

	minPercentBkpTF.setEnabled(minPercentBkpRB.isSelected());

	exclXChrCB.setEnabled(exclSexChrRB.isSelected());
	exclYChrCB.setEnabled(exclSexChrRB.isSelected());

	gainedCB.setEnabled(!mergeGainedAmplCB.isSelected());

	mergeGainedAmplCB.setEnabled(!amplCB.isSelected() &&
				     !gainedCB.isSelected() &&
				     !ignoreAmplCB.isSelected());
	ignoreAmplCB.setEnabled(!amplCB.isSelected() &&
				!mergeGainedAmplCB.isSelected());
	amplCB.setEnabled(!ignoreAmplCB.isSelected() &&
			  !mergeGainedAmplCB.isSelected());

	// TBD : synchroniser SA et SB dans le cas RR
	// TBD : disabler SA dans le cas uniquement BKP
	/*
	minNumberAltCB.setEnabled(true);
	minPercentAltTF.setEnabled(true);
	*/

	minNumberBkpCB.setEnabled(!recRegCB.isSelected() && minNumberBkpRB.isSelected());
	minPercentBkpTF.setEnabled(!recRegCB.isSelected() && minPercentAltRB.isSelected());

	if (minRegCB.isSelected()) {
	    rmRegCB.setText("Delete recurrent regions");
	    rmRegCB.setEnabled(GenomeAlterationOP.hasRecurrentRegions(view));

	}
	else {
	    rmRegCB.setText("Delete minimal regions");
	    rmRegCB.setEnabled(GenomeAlterationOP.hasMinimalRegions(view));
	}

	csvRB.setEnabled(reportCB.isSelected());
	htmlRB.setEnabled(reportCB.isSelected());
    }
}

