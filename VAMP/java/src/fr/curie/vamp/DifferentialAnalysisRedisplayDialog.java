
/*
 *
 * DifferentialAnalysisRedisplayDialog.java
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

class DifferentialAnalysisRedisplayDialog extends StandardDialog implements ChangeListener {

    JRadioButton signRB, confidenceRB;
    TextField pvalueTF;

    static final String DIFF_ANA_REDISPLAY_DIALOG = "DifferentialAnalysisRedisplayDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(DIFF_ANA_REDISPLAY_DIALOG,
			  new DifferentialAnalysisRedisplayDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements,
				    TreeMap oparams) {
	DifferentialAnalysisRedisplayDialog mrDialog = (DifferentialAnalysisRedisplayDialog)view.getGlobalContext().
	    get(DIFF_ANA_REDISPLAY_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    DifferentialAnalysisRedisplayDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "Differential Analysis Parameteres", true, 1);

	addLine(makeLabel("Colorbars:"));

	GridBagConstraints c;

	ButtonGroup group = new ButtonGroup();
	signRB = makeRadioButton("Use sign", group);
	confidenceRB = makeRadioButton("Use confidence", group);

	JPanel signPanel = new JPanel(new GridBagLayout());
	signPanel.setBackground(getBackground());
	c = Utils.makeGBC(0, 0);
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.WEST;
	signPanel.add(signRB, c);
	c = Utils.makeGBC(1, 0);
	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	signPanel.add(makeHelp("  color bar is red if the average in the bottom panel is greater\n  than the average in the top panel and green otherwise"), c);
	addLine(signPanel);

	JPanel confidPanel = new JPanel(new GridBagLayout());
	confidPanel.setBackground(getBackground());
	c = Utils.makeGBC(0, 0);
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.WEST;
	confidPanel.add(confidenceRB, c);
	c = Utils.makeGBC(1, 0);
	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	confidPanel.add(makeHelp("  color bar corresponds to the percentage\n  of profiles without missing values"), c);
			
	addLine(confidPanel);

	addPadLine();

	pvalueTF = makeTextField("", 4);
	addLine(makeLabel("Max P-Value"));
	
	JPanel pvaluePanel = new JPanel(new GridBagLayout());
	pvaluePanel.setBackground(getBackground());
	c = Utils.makeGBC(0, 0);
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.WEST;
	pvaluePanel.add(pvalueTF.getComponent(), c);
	c = Utils.makeGBC(1, 0);
	c.fill = GridBagConstraints.HORIZONTAL;
	c.anchor = GridBagConstraints.WEST;
	c.weightx = 1.0;
	pvaluePanel.add(new JLabel(" [0-1]"), c);
	addLine(pvaluePanel);

	addPadLine();

	/*
	ButtonGroup group = new ButtonGroup();
	signRB = makeRadioButton("Use sign", group);
	confidenceRB = makeRadioButton("Use confidence", group);
	addLine(signRB, makeHelp("color bar is red if the average\nin the bottom panel is greater than the average\nin the top panel and green otherwise"));
	addLine(confidenceRB, makeHelp("color bar corresponds to the percentage\nof profiles without missing values"));

	addPadLine();

	addLine(makeLabel("Max P-Value:"));
	pvalueTF = makeTextField("", 4);
	addLine(pvalueTF.getComponent(), makeLabel(" [0-1]"));
	addPadLine();
	*/
	addPadLine();

	signRB.setSelected(true);

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    String s = pvalueTF.getText().trim();
		    if (s.length() > 0) {
			double percent = -1;
			try {
			    percent = Utils.parseDouble(s);
			}
			catch(Exception exc) {
			}
		    
			if (percent < 0 || percent > 1) {
			    InfoDialog.pop(globalContext,
					   "Invalid p-value [0-1]: " + s);
			    params = null;
			    return;
			}
		    }

		    params.put(DifferentialAnalysisOP.PVALUE_PARAM, s);

		    int colorbars_mask = 0;

		    if (signRB.isSelected())
			colorbars_mask = DifferentialAnalysisOP.USE_SIGN_MASK;
		    else if (confidenceRB.isSelected())
			colorbars_mask = DifferentialAnalysisOP.USE_CONFIDENCE_MASK;
		    params.put(DifferentialAnalysisOP.COLORBARS_PARAM,
			       new Integer(colorbars_mask));

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
	    (v = oparams.get(DifferentialAnalysisOP.PVALUE_PARAM)) != null)
	    pvalueTF.setText((String)v);


	if (oparams != null &&
	    (v = oparams.get(DifferentialAnalysisOP.COLORBARS_PARAM)) != null) {
	    int colorbars_mask = ((Integer)v).intValue();
	    signRB.setSelected((colorbars_mask & DifferentialAnalysisOP.USE_SIGN_MASK) != 0);
	    confidenceRB.setSelected((colorbars_mask & DifferentialAnalysisOP.USE_CONFIDENCE_MASK) != 0);
	}
	else {
	    signRB.setSelected(false);
	    confidenceRB.setSelected(false);
	}

	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
    }
}
