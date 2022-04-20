
/*
 *
 * SortDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;

class SortDialog extends JDialog {

    private GlobalContext globalContext;
    private View view;
    private GraphPanel panel;
    private Property filter_prop;
    private JLabel titleLabel;
    private Color bgColor;
    private JButton okB, applyB, cancelB;
    private TreeSet propSet;
    private boolean sorting;

    // should be in resources
    static final int SLINE_COUNT = 4;

    private JComboBox sortCB[];
    private JComboBox adCB[];

    private JComboBox makeCB(String[] items) {
	JComboBox cb = new JComboBox(items);
	cb.setBackground(Color.WHITE);
	return cb;
    }

    private JComboBox makeCB() {
	JComboBox cb = new JComboBox();
	cb.setBackground(Color.WHITE);
	return cb;
    }

    static final String SORT_DIALOG = "SortDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(SORT_DIALOG,
			  new SortDialog(globalContext));
    }

    public static void pop(GlobalContext globalContext, String title,
			   View view, GraphPanel panel, Property filter_prop) {
	SortDialog dialog =
	    (SortDialog)globalContext.get(SORT_DIALOG);
	dialog.pop(title, view, panel, filter_prop);
    }

    public static void update(GlobalContext globalContext, View view,
			      GraphPanel panel) {
	SortDialog dialog =
	    (SortDialog)globalContext.get(SORT_DIALOG);
	if (dialog.isVisible() && dialog.view == view && dialog.panel == panel)
	    dialog.update_perform();
    }

    private static final int PADX = 5;
    private static final int PADY = 5;

    private SortDialog(GlobalContext globalContext) {
	super(new Frame(), VAMPUtils.getTitle() + ": Filter Panel");
	this.globalContext = globalContext;

	bgColor = VAMPResources.getColor(VAMPResources.DIALOG_BG);

	JPanel mainPane = new JPanel(new GridBagLayout());
	getContentPane().setBackground(bgColor);
	getContentPane().add(mainPane);
	mainPane.setBackground(bgColor);

	int x = 0;
	int y = 0;

	Utils.addPadPanel(mainPane, x, y, PADX, PADY, bgColor);
	x++;
	y++;
	int left_x = x;
	GridBagConstraints c;
	titleLabel = new JLabel();
	titleLabel.setFont(VAMPResources.getFont(VAMPResources.DIALOG_FONT));
	c = Utils.makeGBC(x, y);
	y++;
	c.anchor = GridBagConstraints.CENTER;
	c.gridwidth = 4;
	mainPane.add(titleLabel, c);
	
	Utils.addPadPanel(mainPane, 0, y, PADX, PADY+PADY, bgColor);
	Utils.addPadPanel(mainPane, left_x, y, PADX, PADY, bgColor);
	y++;


	sortCB = new JComboBox[SLINE_COUNT];
	adCB = new JComboBox[SLINE_COUNT];
	for (int n = 0; n < sortCB.length; n++) {
	    x = left_x+1;

	    sortCB[n] = makeCB();
	    c = Utils.makeGBC(x, y);
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.CENTER;
	    mainPane.add(sortCB[n], c);
	    x++;

	    Utils.addPadPanel(mainPane, x, y, PADX, PADY, bgColor);
	    x++;

	    adCB[n] = makeCB(new String[]{"Asc", "Desc"});
	    c = Utils.makeGBC(x, y);
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.CENTER;
	    mainPane.add(adCB[n], c);
	    y++;
	    Utils.addPadPanel(mainPane, 0, y, PADX, PADY, bgColor);
	    y++;
	}

	Utils.addPadPanel(mainPane, 0, y, PADX, PADY, bgColor);
	y++;

	JPanel buttonPane = new JPanel(new FlowLayout());
	buttonPane.setBackground(bgColor);
	okB = new JButton("OK");
	okB.setBackground(Color.WHITE);
	okB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    apply_perform();
		    setVisible(false);
		}
	    });
	buttonPane.add(okB);

	applyB = new JButton("Apply");
	applyB.setBackground(Color.WHITE);
	buttonPane.add(applyB);
	applyB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    apply_perform();
		}
	    });

	cancelB = new JButton("Cancel");
	cancelB.setBackground(Color.WHITE);
	cancelB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });
	buttonPane.add(cancelB);

	x = left_x;
	c = Utils.makeGBC(x, y);
	c.anchor = GridBagConstraints.CENTER;
	c.gridwidth = 5;
	mainPane.add(buttonPane, c);

	pack();
	Utils.augment(this, 1.5, 1.1);
    }

    private void pop(String title, View view, GraphPanel panel, Property filter_prop) {
	this.view = view;
	this.panel = panel;
	this.filter_prop = filter_prop;

	titleLabel.setText(title);

	update_perform();
	setVisible(true);
    }

    private void update_perform() {
	if (sorting)
	    return;

	propSet = new TreeSet();

	LinkedList list = panel.getGraphElements();
	int sz = list.size();

	for (int n = 0; n < sz; n++) {
	    GraphElement ge = (GraphElement)list.get(n);
	    TreeMap properties = ge.getProperties();
	    Iterator it = properties.entrySet().iterator();

	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		if (filter_prop == null) {
		    if (!prop.isInfoable() && prop != VAMPProperties.TypeProp)
			continue;
		}
		else if (prop.getPropertyValue(filter_prop) == null)
		    continue;

		propSet.add(prop.getName());
	    }
	}

	for (int n = 0; n < sortCB.length; n++) {
	    JComboBox cb = sortCB[n];
	    cb.removeAllItems();
	    cb.addItem("");
	    Iterator it = propSet.iterator();
	    while (it.hasNext())
		cb.addItem(it.next());
	    //cb.setSelectedItem("");
	}

    }
    
    class SortComparator implements Comparator {

	Property propV[];
	boolean asc[];
	int prop_cnt;

	SortComparator(TreeSet propSet) {
	    propV = new Property[sortCB.length];
	    asc = new boolean[sortCB.length];
	    for (prop_cnt = 0; prop_cnt < sortCB.length; prop_cnt++) {
		String s = (String)sortCB[prop_cnt].getSelectedItem();
		if (s == null || s.length() == 0)
		    break;
		propV[prop_cnt] = Property.getProperty(s);
		asc[prop_cnt] = adCB[prop_cnt].getSelectedItem().
		    equals("Asc");
	    }
	}

	public int compare(Object o1, Object o2) {
	    GraphElement ds1 = (GraphElement)o1;
	    GraphElement ds2 = (GraphElement)o2;

	    for (int n = 0; n < prop_cnt; n++) {
		Object p1 = ds1.getPropertyValue(propV[n]);
		Object p2 = ds2.getPropertyValue(propV[n]);
   
		if (p1 != null && p2 != null) {
		    int r;
		    PropertyType type = propV[n].getType();
		    if (type instanceof PropertyFloatNAType ||
			type instanceof PropertyFloatType ||
			type instanceof PropertyIntegerType ||
			type instanceof PropertyIntegerNAType) {
			if (p1.equals(VAMPProperties.NA) && p2.equals(VAMPProperties.NA))
			    r = 0;
			else if (p1.equals(VAMPProperties.NA))
			    r = -1;
			else if (p2.equals(VAMPProperties.NA))
			    r = 1;
			else {
			    double d1 = Double.parseDouble(p1.toString());
			    double d2 = Double.parseDouble(p2.toString());
			    r = (int)(d1 - d2);
			}
		    }
		    else {
			String sp1 = p1.toString();
			String sp2 = p2.toString();
			if (propV[n] == VAMPProperties.ChromosomeProp) {
			    sp1 = VAMPUtils.normChr(sp1);
			    sp2 = VAMPUtils.normChr(sp2);
			}
			
			r = sp1.compareToIgnoreCase(sp2);
		    }

		    if (r != 0)
			return (asc[n] ? r : -r);
		}
		else if (p1 == null && p2 == null)
		    continue;
		else if (p1 == null)
		    return (asc[n] ? -1 : 1);
		else if (p2 == null)
		    return (asc[n] ? 1 : -1);
	    }
	    
	    return Integer.parseInt(ds1.getOrder()) -
		Integer.parseInt(ds2.getOrder());
	}
    }

    private void apply_perform() {
	LinkedList list = panel.getGraphElements();
	int sz = list.size();
	for (int n = 0; n < sz; n++) {
	    GraphElement ge = (GraphElement)list.get(n);
	    ge.setOrder(n);
	}

	TreeSet tset = new TreeSet(new SortComparator(propSet));
	tset.addAll(list);

	LinkedList l = new LinkedList();
	l.addAll(tset);
	sorting = true;
	StandardVMStatement vmstat = new StandardVMStatement
	    (VMOP.getVMOP(VMOP.SORT), panel);
	vmstat.beforeExecute();
	UndoVMStack.getInstance(panel).push(vmstat);
	panel.setGraphElements(l);
	sorting = false;
    }
}
