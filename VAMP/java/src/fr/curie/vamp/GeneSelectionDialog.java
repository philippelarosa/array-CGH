
/*
 *
 * GeneSelectionDialog.java
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

class GeneSelectionDialog extends JDialog implements ChangeListener {

    JPanel mainPanel;
    JRadioButton bypassRB, overlapRB, nooverlapRB;
    JRadioButton allRB, regionRB, selRegionRB;
    JRadioButton htmlCB, csvCB, profileCB;
    JComboBox altTypeCB;
    GlobalContext globalContext;
    static Font helpFont = new Font("MonoSpaced", Font.PLAIN, 9);

    TreeMap params;
    static final String GENE_SELECTION_DIALOG = "Gene Selection";

    public static void init(GlobalContext globalContext) {
	globalContext.put(GENE_SELECTION_DIALOG,
			  new GeneSelectionDialog(new Frame(), globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
	GeneSelectionDialog mrDialog = (GeneSelectionDialog)view.getGlobalContext().get(GENE_SELECTION_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    private Component makePad() {
	return new JLabel();
    }

    private void makePad(JPanel panel, int n) {
	while (n-- > 0)
	    panel.add(makePad());
    }

    private JButton makeButton(String s) {
	JButton b = new JButton(s);
	b.setFont(VAMPResources.getFont(VAMPResources.DIALOG_BUTTON_FONT));
	b.setForeground(VAMPResources.getColor
			(VAMPResources.DIALOG_BUTTON_FG));
	b.setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_BG));
	return b;
    }

    GeneSelectionDialog(Frame f, GlobalContext _globalContext) {
	super(f, "Gene Selection Dialog", Config.DEFAULT_MODAL);
	this.globalContext = _globalContext;
	Color bgColor = VAMPResources.getColor(VAMPResources.DIALOG_BG);

	getContentPane().setLayout(new BorderLayout());
	JPanel buttonPanel = new JPanel(new FlowLayout());
	JButton b = makeButton("OK");
	buttonPanel.add(b);
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    if (allRB.isSelected())
			params.put(GeneSelectionOP.SCOPE_PARAM,
				   GeneSelectionOP.ALL);
		    else if (regionRB.isSelected())
			params.put(GeneSelectionOP.SCOPE_PARAM,
				   GeneSelectionOP.REGIONS);
		    else
			params.put(GeneSelectionOP.SCOPE_PARAM,
				   GeneSelectionOP.SEL_REGIONS);

		    if (bypassRB.isSelected())
			params.put(GeneSelectionOP.OVERLAP_PARAM,
				   GeneSelectionOP.BYPASS);
		    else if (overlapRB.isSelected())
			params.put(GeneSelectionOP.OVERLAP_PARAM,
				   GeneSelectionOP.OVERLAP);
		    else
			params.put(GeneSelectionOP.OVERLAP_PARAM,
				   GeneSelectionOP.NOOVERLAP);

		    /*
		    params.put(GeneSelectionOP.ALT_TYPE_PARAM,
			       altTypeCB.getSelectedItem());
		    */

		    int result;
		    if (htmlCB.isSelected())
			result = GeneSelectionOP.HTML_REPORT;
		    else if (csvCB.isSelected())
			result = GeneSelectionOP.CSV_REPORT;
		    else if (profileCB.isSelected())
			result = GeneSelectionOP.PROFILE_DISPLAY;
		    else
			result = 0;

		    params.put(GeneSelectionOP.RESULT_PARAM,
			       new Integer(result));

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
	p.add(new JLabel("Gene Selection Dialog"));
	p.setBackground(bgColor);
	getContentPane().add(p, BorderLayout.NORTH);
	getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	mainPanel = new JPanel(new GridLayout(15, 3));

	mainPanel.add(new JLabel(" Consider:"));
	makePad(mainPanel, 2);

	ButtonGroup group = new ButtonGroup();
	bypassRB = new JRadioButton("All genes");
	group.add(bypassRB);
	bypassRB.addChangeListener(this);
	bypassRB.setBackground(bgColor);
	mainPanel.add(bypassRB);
	makePad(mainPanel, 2);

	overlapRB = new JRadioButton("Only genes overlapping with probes");
	group.add(overlapRB);
	overlapRB.addChangeListener(this);
	overlapRB.setBackground(bgColor);
	mainPanel.add(overlapRB);
	makePad(mainPanel, 2);

	nooverlapRB = new JRadioButton("Only genes not overlapping with any probe");
	group.add(nooverlapRB);
	nooverlapRB.addChangeListener(this);
	nooverlapRB.setBackground(bgColor);
	mainPanel.add(nooverlapRB);
	makePad(mainPanel, 2);

	makePad(mainPanel, 3);

	mainPanel.add(new JLabel(" From:"));
	makePad(mainPanel, 2);

	group = new ButtonGroup();
	allRB = new JRadioButton("Anywhere");
	group.add(allRB);
	allRB.addChangeListener(this);
	allRB.setBackground(bgColor);
	mainPanel.add(allRB);
	makePad(mainPanel, 2);

	regionRB = new JRadioButton("Regions only");
	group.add(regionRB);
	regionRB.addChangeListener(this);
	regionRB.setBackground(bgColor);
	mainPanel.add(regionRB);
	makePad(mainPanel, 2);

	selRegionRB = new JRadioButton("Selected Regions only");
	group.add(selRegionRB);
	selRegionRB.addChangeListener(this);
	selRegionRB.setBackground(bgColor);

	mainPanel.add(selRegionRB);
	makePad(mainPanel, 2);

	makePad(mainPanel, 3);

	/*
	mainPanel.add(new JLabel(" Type of alterations:"));
	makePad(mainPanel, 2);

	altTypeCB = new JComboBox(new String[]{
	    GeneSelectionOP.ANY_ALT,
	    GeneSelectionOP.ALTERED_ALT,
	    GeneSelectionOP.GAINED_ALT,
	    GeneSelectionOP.LOST_ALT});

	altTypeCB.setBackground(Color.WHITE);
	mainPanel.add(altTypeCB);
	mainPanel.add(makePad());
	makePad(mainPanel, 1);

	makePad(mainPanel, 3);
	*/

	mainPanel.add(new JLabel(" Results:"));
	makePad(mainPanel, 2);

	group = new ButtonGroup();
	htmlCB = new JRadioButton("HTML report: ");
	group.add(htmlCB);
	htmlCB.setBackground(bgColor);
	mainPanel.add(htmlCB);
	MLLabel ml = new MLLabel();
	ml.setBackground(bgColor);
	ml.setFont(helpFont);
	ml.setText("HTML report help");
	mainPanel.add(ml);
	makePad(mainPanel, 1);

	csvCB = new JRadioButton("CSV report: ");
	csvCB.setBackground(bgColor);
	group.add(csvCB);
	mainPanel.add(csvCB);
	ml = new MLLabel();
	ml.setBackground(bgColor);
	ml.setFont(helpFont);
	ml.setText("export CSV help");
	mainPanel.add(ml);
	makePad(mainPanel, 1);

	profileCB = new JRadioButton("Display Profile: ");
	profileCB.setBackground(bgColor);
	group.add(profileCB);
	mainPanel.add(profileCB);
	ml = new MLLabel();
	ml.setBackground(bgColor);
	ml.setFont(helpFont);
	ml.setText("display profile help");
	mainPanel.add(ml);
	makePad(mainPanel, 1);

	makePad(mainPanel, 3);

	getContentPane().add(mainPanel, BorderLayout.CENTER);

	getContentPane().setBackground(bgColor);
	mainPanel.setBackground(bgColor);
	buttonPanel.setBackground(bgColor);
	allRB.setSelected(true);
    }

    private TreeMap _getParams(View view, Vector graphElements, TreeMap oparams) {
	Object v;
	if (view.getRegions().size() == 0) {
	    allRB.setSelected(true);
	    regionRB.setEnabled(false);
	    selRegionRB.setEnabled(false);
	}
	else {
	    regionRB.setEnabled(true);
	    selRegionRB.setEnabled(false);
	    for (int n = 0; n < view.getRegions().size(); n++)
		if (((Region)view.getRegions().get(n)).isSelected()) {
		    selRegionRB.setEnabled(true);
		}

	    if (oparams != null &&
		(v = oparams.get(GeneSelectionOP.SCOPE_PARAM)) != null) {
		allRB.setSelected(v.equals(GeneSelectionOP.ALL));
		regionRB.setSelected(v.equals(GeneSelectionOP.REGIONS));
		selRegionRB.setSelected(v.equals(GeneSelectionOP.SEL_REGIONS));
	    }
	    else
		allRB.setSelected(true);
	}

	//	if (graphElements.size() == 1) {
	if (false) {
	    bypassRB.setSelected(true);
	    overlapRB.setEnabled(false);
	    nooverlapRB.setEnabled(false);
	}
	else {
	    overlapRB.setEnabled(true);
	    nooverlapRB.setEnabled(true);

	    if (oparams != null &&
	    (v = oparams.get(GeneSelectionOP.OVERLAP_PARAM)) != null) {
		bypassRB.setSelected(v.equals(GeneSelectionOP.BYPASS));
		overlapRB.setSelected(v.equals(GeneSelectionOP.OVERLAP));
		nooverlapRB.setSelected(v.equals(GeneSelectionOP.NOOVERLAP));
	    }
	    else
		bypassRB.setSelected(true);
	}

	/*
	if (oparams != null &&
	    (v = oparams.get(GeneSelectionOP.ALT_TYPE_PARAM)) != null) {
	    altTypeCB.setSelectedItem((String)v);
	}
	else
	    altTypeCB.setSelectedIndex(0);
	*/

	if (oparams != null &&
	    (v = oparams.get(GeneSelectionOP.RESULT_PARAM)) != null) {
	    int result = ((Integer)v).intValue();
	    htmlCB.setSelected((result & GeneSelectionOP.HTML_REPORT) != 0);
	    csvCB.setSelected((result & GeneSelectionOP.CSV_REPORT) != 0);
	    profileCB.setSelected((result & GeneSelectionOP.PROFILE_DISPLAY) != 0);
	}
	else {
	    htmlCB.setSelected(false);
	    csvCB.setSelected(false);
	    profileCB.setSelected(true);
	}

	pack();

	Utils.centerOnScreen(this);
	return params;
    }

    public void stateChanged(ChangeEvent e) {
    }
}

