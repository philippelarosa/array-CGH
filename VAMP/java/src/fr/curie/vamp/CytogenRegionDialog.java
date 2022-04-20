
/*
 *
 * CytogenRegionDialog.java
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

class CytogenRegionDialog extends StandardDialog implements ActionListener {

    JComboBox osCB;
    JComboBox beginCB;
    JComboBox endCB;
    JButton colorB;

    static final String CYTOGEN_REGION_DIALOG = "CytogenRegionDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(CYTOGEN_REGION_DIALOG,
			  new CytogenRegionDialog(globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
	CytogenRegionDialog dlg = (CytogenRegionDialog)view.getGlobalContext().get(CYTOGEN_REGION_DIALOG);
	return dlg._getParams(view, graphElements, oparams);
    }

    CytogenRegionDialog(GlobalContext _globalContext) {
	super(new Frame(), _globalContext, "Cytogen Region Dialog", true,
	      3);

	addPadLine();

	osCB = makeComboBox(new String[]{"Human", "Mouse"});
	addLine(makeLabel("Organism"), osCB);

	beginCB = makeComboBox();
			       
	addPadLine();
	addLine(makeLabel("Begin"), beginCB);
	endCB = makeComboBox(new String[]{"2p1", "2p2"});

	addPadLine();
	addLine(makeLabel("End"), endCB);
	addPadLine();

	colorB = new JButton();
	addLine(makeLabel("Color"), colorB);
	colorB.setBackground(VAMPResources.getColor(VAMPResources.REGION_BG));
	addPadLine();

	colorB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Color color = JColorChooser.showDialog
			(new Frame(),
			 "Region Color",
			 colorB.getBackground());
		    if (color != null)
			colorB.setBackground(color);
		}
	    });
			       
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = null;
		    setVisible(false);
		}
	    });

	okButton.setLabel("Create Region");
	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    params.put(CytogenRegionOP.OS_PARAM,
			       osCB.getSelectedItem());

		    params.put(CytogenRegionOP.BEGIN_PARAM,
			       beginCB.getSelectedItem());

		    params.put(CytogenRegionOP.END_PARAM,
			       endCB.getSelectedItem());

		    params.put(CytogenRegionOP.COLOR_PARAM,
			       colorB.getBackground());

		    setVisible(false);
		}
	    });

	osCB.setSelectedItem("Human");
	osCB.addActionListener(this);
	beginCB.addActionListener(this);
	//endCB.addActionListener(this);

	epilogue();
    }

    private TreeMap _getParams(View view, Vector _graphElements, TreeMap oparams) {
	params = null;
	//params = oparams;
	Object v;

	if (oparams != null &&
	    (v = oparams.get(CytogenRegionOP.OS_PARAM)) != null)
	    osCB.setSelectedItem(v);

	if (oparams != null &&
	    (v = oparams.get(CytogenRegionOP.BEGIN_PARAM)) != null)
	    beginCB.setSelectedItem(v);

	if (oparams != null &&
	    (v = oparams.get(CytogenRegionOP.END_PARAM)) != null)
	    endCB.setSelectedItem(v);

	if (oparams != null &&
	    (v = oparams.get(CytogenRegionOP.COLOR_PARAM)) != null)
	    colorB.setBackground((Color)v);

	update((String)osCB.getSelectedItem());
	pop();

	return params;
    }

    static private Cytoband cytoband;

    void update(String organism) {
	cytoband = MiniMapDataFactory.getCytoband
	    (globalContext, organism);

	if (cytoband == null)
	    return;

	Vector chrV = cytoband.getChrV();
	int size = chrV.size();
	beginCB.removeAllItems();
	endCB.removeAllItems();
	for (int n = 0; n < size; n++) {
	    Chromosome chr = (Chromosome)chrV.get(n);
	    Band bands[] = chr.getBands();
	    for (int j = 0; j < bands.length; j++) {
		String name = getName(chr, bands[j]);
		beginCB.addItem(name);
		endCB.addItem(name);
	    }
	}
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == osCB) {
	    if (cytoband == null ||
		!cytoband.getName().equals(osCB.getSelectedItem())) {
		update((String)osCB.getSelectedItem());
	    }
	    return;
	}

	if (e.getSource() == beginCB) {
	    int idx = beginCB.getSelectedIndex();
	    endCB.removeAllItems();
	    int item_cnt = beginCB.getItemCount();
	    for (int i = idx; i < item_cnt; i++)
		endCB.addItem(beginCB.getItemAt(i));
	}
    }

    static String getName(Chromosome chr, Band band) {
	return chr.getName() + band.getArm() + band.getName();
    }

    static long getBegin(String begin, boolean off) {
	Vector chrV = cytoband.getChrV();

	int size = chrV.size();

	for (int n = 0; n < size; n++) {
	    Chromosome chr = (Chromosome)chrV.get(n);
	    Band bands[] = chr.getBands();
	    for (int j = 0; j < bands.length; j++) {
		if (getName(chr, bands[j]).equals(begin)) 
		    return (off ? chr.getOffsetPos() : 0) + bands[j].getBegin();
	    }
	}

	return -1;
    }

    static long getEnd(String end, boolean off) {
	Vector chrV = cytoband.getChrV();

	int size = chrV.size();

	for (int n = 0; n < size; n++) {
	    Chromosome chr = (Chromosome)chrV.get(n);
	    Band bands[] = chr.getBands();
	    for (int j = 0; j < bands.length; j++) {
		if (getName(chr, bands[j]).equals(end))
		    return (off ? chr.getOffsetPos() : 0) + bands[j].getEnd();
	    }
	}

	return -1;
    }
}
