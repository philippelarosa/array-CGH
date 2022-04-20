
/*
 *
 * ImportTestDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 * TEMPORARY CLASS
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import fr.curie.vamp.data.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.gui.*;
import fr.curie.vamp.gui.optim.*;

class ImportTestDialog extends JDialog {

    //    private static String HARD_CODED_LIST[] = new String[]{"1701502_RBc2237dr_normalized", "1702102_RBc2411bj_normalized", "1702602_RBc2956ba_normalized", "1703402_RBc3019rf_normalized", "1703502_RBc3143se_normalized", "1703802_RBc3209ae_normalized", "1703902_RBc3350sf_normalized", "1704002_RBc6084pl_normalized", "1704102_RBc6108dc_normalized", "1704302_RBc6825mcw_normalized", "1704402_RBc6992bs_normalized", "1704802_RBc0001er_normalized", "56032_C34_normalized", "56517_A13_normalized", "62734_GAUYAN_normalized", "65481_NB117_normalized", "92243_A50_normalized", "92582_C08_normalized", "92751_D09_normalized", "93971_C21_normalized", "93976_B15_normalized", "C01", "HNEURO_NB18.1479_normalized", "HNEURO_NB189.6159_normalized", "HNEURO_NB236.6375_normalized", "HNEURO_NB249_2.6354_normalized", "HSEIN_293T.13937"};

    private static String HARD_CODED_LIST[] = new String[]{
	"HNEURO_0.1.4756_NO_NA",
	"HNEURO_0.1.4756",
	"HNEURO_106C.1307",
	"HNEURO_ANTMAT.202",
	"HNEURO_ANTMAT.4303",
	"HNEURO_BJG0G1.14696"
    };

    static ImportTestDialog instance;

    private JList list;
    private View view;
    private int nn;

    static ImportTestDialog getInstance() {
	if (instance == null)
	    instance = new ImportTestDialog();
	return instance;
    }

    private ImportTestDialog() {
	super(new Frame(), "Large Profiles Import Test");
	list = new JList(HARD_CODED_LIST);
	list.setVisibleRowCount(HARD_CODED_LIST.length);
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(list, BorderLayout.CENTER);
	JPanel panel = new JPanel();

	JButton okButton = new JButton("OK");
	panel.add(okButton);
	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Object o[] = list.getSelectedValues();
		    String names[] = new String[o.length];
		    for (int n = 0; n < o.length; n++) {
			names[n] = (String)o[n];
		    }
		    importProfiles(names);
		    setVisible(false);
		}
	    });

	JButton cancelButton = new JButton("Cancel");
	panel.add(cancelButton);
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });


	getContentPane().add(panel, BorderLayout.SOUTH);
	pack();
    }

    static void pop(View view, int nn) {
	ImportTestDialog dialog = getInstance();
	dialog.view = view;
	dialog.nn = nn;
	dialog.setVisible(true);

	//testDir();
    }

    static private void setAxisDisplayer(GraphPanelSet panelSet, Profile profile) {
	if (VAMPUtils.isMergeChr(profile) &&
	    !(panelSet.getPanel(0).getDefaultAxisDisplayer() instanceof
	      ChromosomeNameAxisDisplayer)) {
	    panelSet.setDefaultAxisDisplayer
		(new ChromosomeNameAxisDisplayer
		 (VAMPUtils.getAxisName(profile), 1., 0.1, false));
	}
    }

    static private void setColorCodes(Profile profile) {
	Object value = profile.getPropertyValue(VAMPProperties.RatioScaleProp);
	
	if (value == null) {
	    value = profile.getPropertyValue(VAMPProperties.RatioProp);
	    if (value == null) {
		return;
	    }

	    profile.setPropertyValue(VAMPProperties.RatioScaleProp, value);
	    profile.removeProperty(VAMPProperties.RatioProp);
	}

	String type = VAMPUtils.getType(profile);
	boolean is_log = value.equals(VAMPConstants.RatioScale_L);
	if (type.equals(VAMPConstants.CHIP_CHIP_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_CHIP_CHIP, false);
	}
	else if (type.equals(VAMPConstants.GTCA_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_GTCA, false);
	}
	else if (type.equals(VAMPConstants.SNP_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_SNP, false);
	}
	else if (type.equals(VAMPConstants.GENOME_ANNOT_TYPE)) {
	}
	else {
	    profile.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_CGH, false);
	}
    }

    void importProfiles(String names[]) {
	importProfiles(view, view.getGraphPanelSet().getPanel(nn), names);
    }

    static void importProfiles(View view, GraphPanel panel, String names[]) {
	long ms0 = System.currentTimeMillis();

	GraphPanelSet panelSet = view.getGraphPanelSet();
	LinkedList l = new LinkedList();

	//l.addAll(panelSet.getGraphElements(nn));
	l.addAll(panel.getGraphElements());

	try {
	    for (int n = 0; n < names.length; n++) {
		String name = names[n];
		ProfileUnserializer unserialProf = new ProfileUnserializer(name);
		
		Profile profile = unserialProf.readProfile();
		GraphicProfile graphicProfile = new GraphicProfile(profile);
		
		profile.setUnserializingPolicy(Profile.CACHE_PROBES);

		profile.setPropertyValue(VAMPProperties.LargeProfileProp, new Boolean(true));
		profile.getProbe(0, true, true).addProp(VAMPProperties.LargeProfileProp, new Boolean(true));

		//profile.setGraphElementDisplayer(new ProfileDisplayer());

		setAxisDisplayer(panelSet, profile);
		setColorCodes(profile);

		l.add(profile);
	    }

	    //panelSet.setGraphElements(l, nn);
	    panel.setGraphElements(l);
	    //GraphPanel panel = panelSet.getPanel(nn);
	    panel.getCanvas().readaptSize();
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
	long ms1 = System.currentTimeMillis();
	System.out.println(((ms1-ms0)/1000.) + " seconds");
    }
}
