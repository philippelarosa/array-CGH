
/*
 *
 * GTCorrelationAnalysisDialog.java
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

class GTCorrelationAnalysisRedisplayDialog extends StandardDialog implements ChangeListener {

    JRadioButton allRB, regionRB;
    JRadioButton smtRB, gnlRB, ratioRB;
    JRadioButton pearsonRB, spearmanRB;
    JTextField thresholdTF, pvalueTF;
    JRadioButton profileCcCB, profilePvCB, profileFwerWgCB, profileFwerBcCB, profileFdrWgCB, profileFdrBcCB;

    static final String GTCA_REDISPLAY_DIALOG = "GTCorrelationAnalysisRedisplayDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(GTCA_REDISPLAY_DIALOG,
			  new GTCorrelationAnalysisRedisplayDialog(globalContext));
    }

   

    public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
	GTCorrelationAnalysisRedisplayDialog mrDialog = (GTCorrelationAnalysisRedisplayDialog)view.getGlobalContext().get(GTCA_REDISPLAY_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }


    // public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
// 	GTCorrelationAnalysisDialog mrDialog = (GTCorrelationAnalysisDialog)view.getGlobalContext().get(GTCA_DIALOG);
// 	return mrDialog._getParams(view, graphElements, oparams);
//     }

    GTCorrelationAnalysisRedisplayDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "GT(CA) Correlation Analysis Redisplay", true, 3);

	ButtonGroup group = new ButtonGroup();
	addLine(makeLabel(" Results:"));

	// group = new ButtonGroup();

	profileCcCB = makeRadioButton("Correlation coefficient", group);
	addLine(profileCcCB);

	profilePvCB = makeRadioButton("p-value", group);
	addLine(profilePvCB);

	profileFwerWgCB = makeRadioButton("FWER - whole genome", group);
	addLine(profileFwerWgCB);

	profileFwerBcCB = makeRadioButton("FWER - by Chromosome", group);
	addLine(profileFwerBcCB);

	profileFdrWgCB = makeRadioButton("FDR - whole genome", group);
	addLine(profileFdrWgCB);

	profileFdrBcCB = makeRadioButton("FDR - by Chromosome", group);
	addLine(profileFdrBcCB);

	profileCcCB.setSelected(true);


	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();
		    // int result_mask = 0;
// 		    if (profileCcCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.CorrelCoef;
// 		    if (profilePvCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.Pvalue;
// 		    if (profileFwerWgCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.FwerWg;
// 		    if (profileFwerBcCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.FwerBc;
// 		    if (profileFdrWgCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.FdrWg;
// 		    if (profileFdrBcCB.isSelected())
// 			result_mask |= GTCorrelationAnalysisOP.FdrBc;
// 		    params.put(GTCorrelationAnalysisOP.RESULT_PARAM,
// 			       new Integer(result_mask));

		    String result = "";
		    if (profileCcCB.isSelected())
			result += GTCorrelationAnalysisOP.CORREL;
		    if (profilePvCB.isSelected())
			result += GTCorrelationAnalysisOP.PVALUE;
		    if (profileFwerWgCB.isSelected())
			result += GTCorrelationAnalysisOP.FWERWG;
		    if (profileFwerBcCB.isSelected())
			result += GTCorrelationAnalysisOP.FWERBC;
		    if (profileFdrWgCB.isSelected())
			result += GTCorrelationAnalysisOP.FDRWG;
		    if (profileFdrBcCB.isSelected())
			result += GTCorrelationAnalysisOP.FDRBC;
		    params.put(GTCorrelationAnalysisOP.RESULT_PARAM, result);

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

    private TreeMap _getParams(View view, Vector graphElements, TreeMap oparams) {
	Object v;
	
	if (oparams != null &&
	    (v = oparams.get(GTCorrelationAnalysisOP.RESULT_PARAM)) != null) {
	    // int result_mask = ((Integer)v).intValue();
	    ;
	}

	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
    }
}

