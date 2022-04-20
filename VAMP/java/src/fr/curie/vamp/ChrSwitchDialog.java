
/*
 *
 * ChrSwitchDialog.java
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

class ChrSwitchDialog extends JDialog {

    JPanel mainPanel;
    JComboBox chrCB, viewCB;
    GlobalContext globalContext;
    static final String ALL = "All";

    TreeMap params;
    static final String CHR_SWITCH_DIALOG = "ChrSwitchDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(CHR_SWITCH_DIALOG,
			  new ChrSwitchDialog(new Frame(), globalContext));
    }

    public static TreeMap getParams(View view, Vector graphElements, TreeMap oparams) {
	ChrSwitchDialog mrDialog = (ChrSwitchDialog)view.getGlobalContext().get(CHR_SWITCH_DIALOG);
	return mrDialog._getParams(view, graphElements, oparams);
    }

    private JButton makeButton(String s) {
	JButton b = new JButton(s);
	b.setFont(VAMPResources.getFont(VAMPResources.DIALOG_BUTTON_FONT));
	b.setForeground(VAMPResources.getColor
			(VAMPResources.DIALOG_BUTTON_FG));
	b.setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_BG));
	return b;
    }

    ChrSwitchDialog(Frame f, GlobalContext _globalContext) {
	super(f, "Chromosome Switch Dialog", Config.DEFAULT_MODAL);
	this.globalContext = _globalContext;
	Color bgColor = VAMPResources.getColor(VAMPResources.DIALOG_BG);

	getContentPane().setLayout(new BorderLayout());
	JPanel buttonPanel = new JPanel(new FlowLayout());
	JButton b = makeButton("OK");
	buttonPanel.add(b);
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    params = new TreeMap();

		    params.put(ChrSwitchOP.CHR_PARAM,
			       chrCB.getSelectedItem());

		    params.put(ChrSwitchOP.VIEW_PARAM,
			       viewCB.getSelectedItem());

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
	p.add(new JLabel("Chromosome Switch Dialog"));
	p.setBackground(bgColor);
	getContentPane().add(p, BorderLayout.NORTH);
	getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	mainPanel = new JPanel(new GridLayout(2, 4));

	JLabel l;

	mainPanel.add(new JLabel(""));
	l = new JLabel("Chromosome ");
	mainPanel.add(l);
	chrCB = new JComboBox();
	chrCB.setBackground(bgColor);
	mainPanel.add(chrCB);
	mainPanel.add(new JLabel(""));

	mainPanel.add(new JLabel(""));
	l = new JLabel("View");
	mainPanel.add(l);
	viewCB = new JComboBox(new String[]
	    {ChrSwitchOP.CURRENT_VIEW_PARAM, ChrSwitchOP.NEW_VIEW_PARAM});

	viewCB.setBackground(bgColor);
	mainPanel.add(viewCB);
	mainPanel.add(new JLabel(""));

	getContentPane().add(mainPanel, BorderLayout.CENTER);

	getContentPane().setBackground(bgColor);
	mainPanel.setBackground(bgColor);
	buttonPanel.setBackground(bgColor);
    }

    private TreeMap _getParams(View view, Vector graphElements, TreeMap oparams) {
	params = null;
	//params = oparams;
	chrCB.removeAllItems();

	String items[] = ChrSwitchOP.getChrList(view.getGlobalContext(),
						graphElements);
	chrCB.addItem("");
	chrCB.addItem(ALL);

	for (int n = 0; n < items.length; n++)
	    chrCB.addItem(items[n]);

	Object v;
	if (oparams != null &&
	    (v = oparams.get(ChrSwitchOP.CHR_PARAM)) != null)
	    chrCB.setSelectedItem(v);

	if (oparams != null &&
	    (v = oparams.get(ChrSwitchOP.VIEW_PARAM)) != null)
	    viewCB.setSelectedItem(v);

	pack();

	Utils.centerOnScreen(this);
	return params;
    }
}

