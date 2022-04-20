
/*
 *
 * KaryoAnalysisDialog.java
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

class KaryoAnalysisDialog extends StandardDialog implements ChangeListener {

    JCheckBox skipOutCB, skipEmptyProfCB;
    JComboBox sortAlgoCB;
    //JRadioButton sameViewRB, newViewRB;
    JRadioButton gainRB, ampliconRB, mergeGainAmpliconRB;
    JCheckBox gainAmpliconCB, lossCB;

    static final String KARYO_DIALOG = "Karyo";

    public static void init(GlobalContext globalContext) {
	globalContext.put(KARYO_DIALOG,
			  new KaryoAnalysisDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
	KaryoAnalysisDialog mrDialog = (KaryoAnalysisDialog)view.getGlobalContext().get(KARYO_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    KaryoAnalysisDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "Karyotype Analysis",
	      true, 3, "Karyotype Analysis Dialog");

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    int alt_mask = 0;
		    if (gainAmpliconCB.isSelected()) {
			alt_mask |= KaryoAnalysisOP.GAIN_AMPLICON_MASK;
			if (gainRB.isSelected())
			    alt_mask |= KaryoAnalysisOP.GAIN_MASK;
			       
			if (ampliconRB.isSelected())
			    alt_mask |= KaryoAnalysisOP.AMPLICON_MASK;
			
			if (mergeGainAmpliconRB.isSelected())
			    alt_mask |= KaryoAnalysisOP.MERGE_GAIN_AMPLICON_MASK;
		    }
			       
		    if (lossCB.isSelected())
			alt_mask |= KaryoAnalysisOP.LOSS_MASK;
			       
		    params.put(KaryoAnalysisOP.ALT_MASK_PARAM, new Integer(alt_mask));
		    params.put(KaryoAnalysisOP.SKIP_OUTLIERS_PARAM,
			       new Boolean(skipOutCB.isSelected()));

		    params.put(KaryoAnalysisOP.SKIP_EMPTY_PROFILES_PARAM,
			       new Boolean(skipEmptyProfCB.isSelected()));

		    params.put(KaryoAnalysisOP.SORT_ALGO_PARAM,
			       sortAlgoCB.getSelectedItem());

		    /*
		    params.put(KaryoAnalysisOP.SAME_VIEW_PARAM,
			       new Boolean(sameViewRB.isSelected()));

		    params.put(KaryoAnalysisOP.NEW_VIEW_PARAM,
			       new Boolean(newViewRB.isSelected()));
		    */

		    setVisible(false);
		}
	    });


	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = null;
		    setVisible(false);
		}
	    });


	addLine(makeLabel("Types of alterations:"));

	gainAmpliconCB = makeCheckBox("Gain / Amplicon", true);

	ButtonGroup group = new ButtonGroup();

	gainRB = makeRadioButton("Gain", group);
	addLine(gainAmpliconCB, gainRB);

	ampliconRB = makeRadioButton("Amplicon", group);
	addLine(makeLabel(""), ampliconRB);

	mergeGainAmpliconRB = makeRadioButton("Merge Gain/Amplicon", group);
	addLine(makeLabel(""), mergeGainAmpliconRB);

	lossCB = makeCheckBox("Loss", true);
	addLine(lossCB);

	addPadLine();

	addLine(makeLabel("Global Parameters"));
	skipOutCB = makeCheckBox("Skip outliers", false);
	addLine(skipOutCB);

	skipEmptyProfCB = makeCheckBox("Skip empty profiles", false);
	addLine(skipEmptyProfCB);

	addPadLine();

	addLine(makeLabel("Sort Algorithm"));
	sortAlgoCB = makeComboBox(new String[]{KaryoAnalysisOP.NO_SORT_ALGO, KaryoAnalysisOP.STD_SORT_ALGO});
	addLine(sortAlgoCB);

	/*
	addPadLine();
	addLine(makeLabel("Karyotype View"));

	group = new ButtonGroup();
	sameViewRB = makeRadioButton("Same View", group);
	addLine(sameViewRB);
	newViewRB = makeRadioButton("New View", group);
	addLine(newViewRB);
	*/

	addPadLine();

	gainAmpliconCB.addChangeListener(this);

	epilogue();
    }

    private TreeMap _getParams(View view, Vector graphElements, TreeMap oparams) {
	Object v;

	if (oparams != null &&
	    (v = oparams.get(KaryoAnalysisOP.ALT_MASK_PARAM)) != null) {
	    int alt_mask = ((Integer)v).intValue();
	    gainRB.setSelected((alt_mask & KaryoAnalysisOP.GAIN_MASK) != 0);
	    ampliconRB.setSelected((alt_mask & KaryoAnalysisOP.AMPLICON_MASK) != 0);
	    mergeGainAmpliconRB.setSelected((alt_mask & KaryoAnalysisOP.MERGE_GAIN_AMPLICON_MASK) != 0);
	    lossCB.setSelected((alt_mask & KaryoAnalysisOP.LOSS_MASK) != 0);
	    gainAmpliconCB.setSelected((alt_mask & KaryoAnalysisOP.GAIN_AMPLICON_MASK) != 0);
	}
	else {
	    gainAmpliconCB.setSelected(true);
	    gainRB.setSelected(true);
	    lossCB.setSelected(true);
	}

	if (oparams != null &&
	    (v = oparams.get(KaryoAnalysisOP.SKIP_OUTLIERS_PARAM)) != null) {
	    skipOutCB.setSelected(((Boolean)v).booleanValue());
	}
	else
	    skipOutCB.setSelected(true);

	if (oparams != null &&
	    (v = oparams.get(KaryoAnalysisOP.SKIP_EMPTY_PROFILES_PARAM)) != null) {
	    skipEmptyProfCB.setSelected(((Boolean)v).booleanValue());
	}
	else
	    skipEmptyProfCB.setSelected(true);

	if (oparams != null &&
	    (v = oparams.get(KaryoAnalysisOP.SORT_ALGO_PARAM)) != null) {
	    sortAlgoCB.setSelectedItem(v);
	}
	else
	    sortAlgoCB.setSelectedIndex(0);

	/*
	if (oparams != null &&
	    (v = oparams.get(KaryoAnalysisOP.SAME_VIEW_PARAM)) != null) {
	    sameViewRB.setSelected(((Boolean)v).booleanValue());
	}
	else
	    sameViewRB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(KaryoAnalysisOP.NEW_VIEW_PARAM)) != null) {
	    newViewRB.setSelected(((Boolean)v).booleanValue());
	}
	else
	    newViewRB.setSelected(false);
	*/

	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
	gainRB.setEnabled(gainAmpliconCB.isSelected());
	ampliconRB.setEnabled(gainAmpliconCB.isSelected());
	mergeGainAmpliconRB.setEnabled(gainAmpliconCB.isSelected());
    }
}

