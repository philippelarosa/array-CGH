
/*
 *
 * DifferentialAnalysisReportDialog.java
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

class DifferentialAnalysisReportDialog extends StandardDialog implements ChangeListener {

    JRadioButton htmlRB, csvRB;

    static final String DIFF_ANA_REPORT_DIALOG = "DifferentialAnalysisReportDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(DIFF_ANA_REPORT_DIALOG,
			  new DifferentialAnalysisReportDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements,
				    TreeMap oparams) {
	DifferentialAnalysisReportDialog mrDialog = (DifferentialAnalysisReportDialog)view.getGlobalContext().
	    get(DIFF_ANA_REPORT_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    DifferentialAnalysisReportDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "Differential Analysis Parameteres", true, 3);

	addLine(makeLabel("Report:"));
	ButtonGroup group = new ButtonGroup();

	htmlRB = makeRadioButton("HTML report", group);
	htmlRB.addChangeListener(this);

	addLine(htmlRB);
	csvRB = makeRadioButton("CSV report", group);
	csvRB.addChangeListener(this);
	addLine(csvRB);
	
	addPadLine();

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    int report_mask = 0;

		    if (htmlRB.isSelected())
			report_mask = DifferentialAnalysisReportOP.HTML_REPORT;
		    else if (csvRB.isSelected())
			report_mask = DifferentialAnalysisReportOP.CSV_REPORT;

		    params.put(DifferentialAnalysisReportOP.REPORT_PARAM,
			       new Integer(report_mask));

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
	    (v = oparams.get(DifferentialAnalysisReportOP.REPORT_PARAM)) != null) {
	    int report_mask = ((Integer)v).intValue();
	    csvRB.setSelected((report_mask & DifferentialAnalysisReportOP.CSV_REPORT) != 0);
	    htmlRB.setSelected((report_mask & DifferentialAnalysisReportOP.HTML_REPORT) != 0);
	}
	else {
	    csvRB.setSelected(false);
	    htmlRB.setSelected(false);
	}

	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
    }
}
