
/*
 *
 * ClusterDialog.java
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

class ClusterDialog extends StandardDialog implements ChangeListener {

    JRadioButton allClonesRB;
    JComboBox ratioCB;
    JRadioButton regionsRB;
    JCheckBox sizeCB;
    JCheckBox percentCB;
    //JTextField percentTF;

    JComboBox distCB;
    JComboBox algoCB;
    JLabel sizeEtcLabel;

    JCheckBox sexChrCB;

    JComboBox displayCB;

    static final String CLUSTER_DIALOG = "ClusterDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(CLUSTER_DIALOG,
			  new ClusterDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements,
				    TreeMap oparams) {
	ClusterDialog mrDialog = (ClusterDialog)view.getGlobalContext().
	    get(CLUSTER_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    ClusterDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "Clustering Options", true, 3);

	addLine(makeLabel("Scope"));
	ButtonGroup group = new ButtonGroup();
	allClonesRB = makeRadioButton("Probes", group);
	allClonesRB.addChangeListener(this);
	ratioCB = makeComboBox(new String[]{ClusterOP.GNL_PARAM,
					    ClusterOP.SMOOTHING_PARAM,
					    ClusterOP.PROBE_VALUE_PARAM});
	addLine(allClonesRB, ratioCB);

	regionsRB = makeRadioButton("Regions", group);
	regionsRB.addChangeListener(this);
	sizeCB = makeCheckBox("", false);
	JPanel panel = new JPanel();
	panel.add(sizeCB);
	panel.setBackground(getBackground());

	MLLabel l = new MLLabel();
	l.setFont(new Font("times", Font.PLAIN, 9));
	l.setBackground(getBackground());
	l.setText("Use all probe status (Gained/Lost\nColor Code) within the regions");
	panel.add(l);
	addLine(regionsRB, panel);


	/*

	sizeCB = makeCheckBox("Use all probe status (Gained/Lost", false);
	addLine(regionsRB, sizeCB);
	sizeEtcLabel = makeLabel("     Color Code) within the regions");
	addLine(makeLabel(""), sizeEtcLabel);
	*/

	percentCB = makeCheckBox("", false);
	//addLine(makeLabel(""), percentTF, makeLabel(" %"));
	sexChrCB = makeCheckBox("Exclude sex chromosomes", false);
	addLine(sexChrCB);

	//addPadLine();
	addSeparator();
	addLine(makeLabel("Method"));
	distCB = makeComboBox(new String[]{ClusterOP.EUCLIDIAN,
					   ClusterOP.PEARSON,
					   ClusterOP.MANHATTAN});

	distCB.setSelectedItem(ClusterOP.EUCLIDIAN);

	addLine(makeLabel("Object Distance"), distCB);
	algoCB = makeComboBox(new String[]{ClusterOP.SLINK,
					   ClusterOP.GAVG,
					   ClusterOP.CLINK,
					   ClusterOP.WARD});

	algoCB.setSelectedItem(ClusterOP.WARD);

	addLine(makeLabel("Cluster Distance"), algoCB);

	addSeparator();

	displayCB = makeComboBox(new String[]{ClusterOP.CLUSTERING_DENDROGRAM,
					      //ClusterOP.CLUSTERING_ONLY,
					      ClusterOP.DENDROGRAM_ONLY});

	addLine(makeLabel("Display"), displayCB);
	addPadLine();

	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();
		    params.put(ClusterOP.ALL_CLONES_PARAM,
			       new Boolean(allClonesRB.isSelected()));
		    params.put(ClusterOP.RATIO_PARAM,
			       ratioCB.getSelectedItem());
		    params.put(ClusterOP.REGIONS_PARAM,
			       new Boolean(regionsRB.isSelected()));
		    params.put(ClusterOP.SIZE_PARAM,
			       new Boolean(sizeCB.isSelected()));
		    params.put(ClusterOP.PERCENT_PARAM,
			       new Boolean(percentCB.isSelected()));

		    /*
		    String s = percentTF.getText().trim();
		    if (s.length() > 0) {
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
		    }

		    params.put(ClusterOP.PERCENT, s);
		    */

		    params.put(ClusterOP.DIST_PARAM,
			       distCB.getSelectedItem());
		    params.put(ClusterOP.ALGO_PARAM,
			       algoCB.getSelectedItem());
		    params.put(ClusterOP.SEX_CHR_PARAM,
			       new Boolean(sexChrCB.isSelected()));
		    params.put(ClusterOP.DISPLAY_PARAM,
			       displayCB.getSelectedItem());
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

	if (view.getRegions().size() == 0) {
	    allClonesRB.setSelected(true);
	    regionsRB.setEnabled(false);
	}
	else {
	    regionsRB.setEnabled(true);

	    if (oparams != null &&
		(v = oparams.get(ClusterOP.ALL_CLONES_PARAM)) != null)
		allClonesRB.setSelected(((Boolean)v).booleanValue());
	    else
		allClonesRB.setSelected(false);

	    if (oparams != null &&
		(v = oparams.get(ClusterOP.REGIONS_PARAM)) != null)
		regionsRB.setSelected(((Boolean)v).booleanValue());
	    else
		regionsRB.setSelected(false);
	}

	if (oparams != null &&
	    (v = oparams.get(ClusterOP.RATIO_PARAM)) != null)
	    ratioCB.setSelectedItem(v);

	if (oparams != null &&
	    (v = oparams.get(ClusterOP.SIZE_PARAM)) != null)
	    sizeCB.setSelected(((Boolean)v).booleanValue());
	else
	    sizeCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(ClusterOP.PERCENT_PARAM)) != null)
	    percentCB.setSelected(((Boolean)v).booleanValue());
	else
	    percentCB.setSelected(false);

	/*
	if (oparams != null &&
	    (v = oparams.get(ClusterOP.PERCENT)) != null)
	    percentTF.setText((String)v);
	*/

	if (oparams != null &&
	    (v = oparams.get(ClusterOP.DIST_PARAM)) != null)
	    distCB.setSelectedItem(v);

	if (oparams != null &&
	    (v = oparams.get(ClusterOP.ALGO_PARAM)) != null)
	    algoCB.setSelectedItem(v);

	if (oparams != null &&
	    (v = oparams.get(ClusterOP.SEX_CHR_PARAM)) != null)
	    sexChrCB.setSelected(((Boolean)v).booleanValue());
	else
	    sexChrCB.setSelected(false);

	if (oparams != null &&
	    (v = oparams.get(ClusterOP.DISPLAY_PARAM)) != null)
	    displayCB.setSelectedItem(v);

	pop();
	return params;
    }

    public void stateChanged(ChangeEvent e) {
	sizeCB.setEnabled(regionsRB.isSelected());
	//sizeEtcLabel.setEnabled(regionsRB.isSelected());
	//percentTF.setEnabled(regionsRB.isSelected());
    }
}

