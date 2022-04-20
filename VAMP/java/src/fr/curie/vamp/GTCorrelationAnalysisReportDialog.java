
/*
 *
 * GTCorrelationAnalysisReportDialog.java
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

class GTCorrelationAnalysisReportDialog extends StandardDialog implements ChangeListener {

    JRadioButton allRB, regionRB;
    JRadioButton smtRB, gnlRB, ratioRB;
    JRadioButton pearsonRB, spearmanRB;
    JTextField thresholdTF, pvalueTF;
    JRadioButton htmlCB, htmlFullCB, csvCB, profileCB;

    static final String GTCA_DIALOG = "GTCA";

    public static void init(GlobalContext globalContext) {
	globalContext.put(GTCA_DIALOG,
			  new GTCorrelationAnalysisDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
	GTCorrelationAnalysisDialog mrDialog = (GTCorrelationAnalysisDialog)view.getGlobalContext().get(GTCA_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    GTCorrelationAnalysisReportDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "GT(CA) Correlation Analysis Dialog",
	      true, 3);

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

// 		

// 		    params.put(GTCorrelationAnalysisOP.PVALUE_PARAM, s);

		    int result_mask = 0;
		    if (htmlCB.isSelected())
			result_mask |= GTCorrelationAnalysisOP.HTML_REPORT;
		    if (htmlFullCB.isSelected())
			result_mask |= GTCorrelationAnalysisOP.HTML_FULL_REPORT;
		    if (csvCB.isSelected())
			result_mask |= GTCorrelationAnalysisOP.CSV_REPORT;
		    
		    params.put(GTCorrelationAnalysisOP.RESULT_PARAM,
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



	addLine(makeLabel(" Report:"));

	group = new ButtonGroup();
	htmlCB = makeRadioButton("HTML report: ", group);
	addLine(htmlCB, makeHelp("HTML report help"));

	htmlFullCB = makeRadioButton("HTML full report: ", group);
	addLine(htmlFullCB, makeHelp("HTML full report help"));

	csvCB = makeRadioButton("CSV report: ", group);
	addLine(csvCB, makeHelp("export CSV help"));


	addPadLine();

	allRB.setSelected(true);
	epilogue();
    }

    private TreeMap _getParams(View view, Vector graphElements, TreeMap oparams) {
	Object v;
	


	if (oparams != null &&
	    (v = oparams.get(GTCorrelationAnalysisOP.RESULT_PARAM)) != null) {
	    int result_mask = ((Integer)v).intValue();
	    htmlCB.setSelected((result_mask & GTCorrelationAnalysisOP.HTML_REPORT) != 0);
	    htmlFullCB.setSelected((result_mask & GTCorrelationAnalysisOP.HTML_FULL_REPORT) != 0);
	    csvCB.setSelected((result_mask & GTCorrelationAnalysisOP.CSV_REPORT) != 0);
	}
	else {
	    htmlCB.setSelected(false);
	    htmlFullCB.setSelected(false);
	    csvCB.setSelected(false);
	}


	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
    }
}

