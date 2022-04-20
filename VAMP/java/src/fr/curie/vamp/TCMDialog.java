
/*
 *
 * TCMDialog.java
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

class TCMDialog extends StandardDialog implements ChangeListener {

    JTextField neighborhoodTF, significanceTF;
    JRadioButton htmlCB, csvCB, profileCB;

    static final String TCM_DIALOG = "TCM";

    public static void init(GlobalContext globalContext) {
	globalContext.put(TCM_DIALOG,
			  new TCMDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements,
				    TreeMap oparams) {
	TCMDialog mrDialog = (TCMDialog)view.getGlobalContext().get
	    (TCM_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    TCMDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "TCM Dialog", true, 3);

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    String s = neighborhoodTF.getText();
		    int iv = -1;
		    try {
			iv = Utils.parseInt(s);
		    }
		    catch(Exception exc) {
		    }

		    if (iv < 0) {
			InfoDialog.pop(globalContext,
				       "Invalid neighborhood value: " + s);
			params = null;
			return;
		    }

		    params.put(TCMOP.NEIGHBORHOOD_PARAM, new Integer(iv));

		    s = significanceTF.getText();
		    double dv = -1;
		    try {
			dv = Utils.parseDouble(s);
		    }
		    catch(Exception exc) {
		    }

		    if (dv < 0. || dv > 1.) {
			InfoDialog.pop(globalContext,
				       "Invalid significance: " + s);
			params = null;
			return;
		    }

		    params.put(TCMOP.SIGNIFICANCE_PARAM, new Double(dv));

		    int result_mask = 0;

		    if (htmlCB.isSelected())
			result_mask = TCMOP.HTML_REPORT;
		    else if (csvCB.isSelected())
			result_mask = TCMOP.CSV_REPORT;
		    else if (profileCB.isSelected())
			result_mask = TCMOP.PROFILE_DISPLAY;

		    params.put(TCMOP.RESULT_PARAM,
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


	neighborhoodTF = new JTextField();
	addLine(makeLabel("Neighborhood"), neighborhoodTF, makeLabel(" x 2"));

	significanceTF = new JTextField();
	addLine(makeLabel("Significance"), significanceTF, makeLabel("[0,1]"));

	addPadLine();

	addLine(makeLabel(" Results:"));

	ButtonGroup group = new ButtonGroup();
	profileCB = makeRadioButton("Display Profile: ", group);
	addLine(profileCB, makeHelp("Display profile help"));

	htmlCB = makeRadioButton("HTML report: ", group);
	addLine(htmlCB, makeHelp("HTML report help"));

	csvCB = makeRadioButton("CSV report: ", group);
	addLine(csvCB, makeHelp("export CSV help"));

	profileCB.setSelected(true);

	epilogue();
    }

    private TreeMap _getParams(View view, Vector graphElements, TreeMap oparams) {
	Object v;

	if (oparams != null &&
	    (v = oparams.get(TCMOP.NEIGHBORHOOD_PARAM)) != null) {
	    neighborhoodTF.setText(v.toString());
	}
	else
	    neighborhoodTF.setText("");

	if (oparams != null &&
	    (v = oparams.get(TCMOP.SIGNIFICANCE_PARAM)) != null) {
	    significanceTF.setText(v.toString());
	}
	else
	    significanceTF.setText("");

	if (oparams != null &&
	    (v = oparams.get(TCMOP.RESULT_PARAM)) != null) {
	    int result_mask = ((Integer)v).intValue();
	    htmlCB.setSelected((result_mask & TCMOP.HTML_REPORT) != 0);
	    csvCB.setSelected((result_mask & TCMOP.CSV_REPORT) != 0);
	    profileCB.setSelected((result_mask & TCMOP.PROFILE_DISPLAY) != 0);
	}
	else {
	    htmlCB.setSelected(false);
	    csvCB.setSelected(false);
	    profileCB.setSelected(false);
	}


	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
    }
}

