
/*
 *
 * AnnotDisplayDialog.java
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

class AnnotDisplayDialog extends JDialog {

    private GlobalContext globalContext;
    private View view;
    private GraphPanel panel;
    private Property filter_prop;
    private JLabel titleLabel;
    private JLabel hiddenLabel;
    private Color bgColor;
    private JButton okB, applyB, removeB, cancelB;
    private TreeMap propMap;
    private static final Color DEFAULT_COLOR = Color.WHITE;

    private static boolean AUTO_APPLY = true;
    private boolean do_not_apply = false;

    private static final String HIDDEN_LABEL = "[ Hidden ]";
    private static final String NOT_HIDDEN_LABEL = "          ";
    // should be in resources
    static final int ADLINE_COUNT = 8;
    static final int MAX_ITEM_LENGTH = 20;

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

    class AnnotDisplayItem {
	String item;
	String cb_item;

	AnnotDisplayItem(String item) {
	    item = item.trim();
	    this.item = item;
	    cb_item = item;
	}

	public String toString() {return cb_item;}
	String getItem() {return item;}
    }

    private class AnnotDisplayLine {
	private JComboBox opstdCB;
	private JComboBox opnumCB;
	private JComboBox valueCB;
	private JTextField valueTF;
	private JButton colorB;
	private int ind;
	private boolean enabled;

	AnnotDisplayLine(JPanel mainPane, int ind, int x, int y) {
	    this.ind = ind;

	    GridBagConstraints c;

	    opstdCB = makeCB(new String[]{"=", "!=",  "~", "!~"});
	    c = Utils.makeGBC(x, y);
	    x++;
	    mainPane.add(opstdCB, c);

	    Utils.addPadPanel(mainPane, x, y, PADX, PADY, bgColor);
	    x++;

	    opnumCB = makeCB(new String[]{"=", "!=", "<", "<=", ">", ">="});
	    mainPane.add(opnumCB, c);
	    opnumCB.setVisible(false);

	    valueCB = makeCB();
	    c = Utils.makeGBC(x, y);
	    mainPane.add(valueCB, c);
	    int h = valueCB.getPreferredSize().height;
	    valueCB.setPreferredSize(new Dimension(150, h));

	    valueTF = new JTextField(10);
	    c = Utils.makeGBC(x, y);
	    x++;
	    c.fill = GridBagConstraints.BOTH;
	    mainPane.add(valueTF, c);
	    valueTF.setVisible(false);

	    Utils.addPadPanel(mainPane, x, y, PADX, PADY, bgColor);
	    x++;

	    colorB = new JButton();
	    colorB.setBackground(DEFAULT_COLOR);
	    colorB.setPreferredSize(new Dimension(12, 12));
	    c = Utils.makeGBC(x, y);
	    c.fill = GridBagConstraints.NONE;
	    mainPane.add(colorB, c);
	    
	    colorB.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			Color color = JColorChooser.showDialog
			    (getThis(),
			     (String)getValue(),
			     colorB.getBackground());
			if (color != null) {
			    colorB.setBackground(color);
			    if (AUTO_APPLY)
				apply_perform();
			}
		}
	    });

	    setListeners();
	}

	Color getColor() {
	    return colorB.getBackground();
	}

	boolean hasValue(int op, String value) {
	    if (!isOPCheckBox(op))
		return true;

	    if (valueTF.isVisible())
		return valueTF.getText().equals(value);
	    int cnt = valueCB.getItemCount();
	    for (int n = 0; n < cnt; n++) {
		AnnotDisplayItem item = (AnnotDisplayItem)valueCB.getItemAt(n);
		if (item.toString().equals(value))
		    return true;
	    }

	    return false;
	}

	private boolean isOPCheckBox(int op) {
	    return op == PropertyElement.EQUAL_OP ||
		op == PropertyElement.DIFF_OP;
	}

	String getValue() {
	    if (valueTF.isVisible())
		return valueTF.getText();
	    AnnotDisplayItem item = (AnnotDisplayItem)valueCB.getSelectedItem();
	    return item != null ? item.getItem() : null;
	}

	int getOP() {
	    String sop = (String)
		(opstdCB.isVisible() ? opstdCB.getSelectedItem() :
		 opnumCB.getSelectedItem());
	    return PropertyElement.getOP(sop);
	}

	private void setListeners() {
	    JComboBox cb[] = new JComboBox[]{opstdCB, opnumCB};
	    for (int n = 0; n < cb.length; n++) {
		cb[n].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    JComboBox cb = (JComboBox)e.getSource();
			    String sitem = (String)cb.getSelectedItem();
			    if (isOPCheckBox(PropertyElement.getOP(sitem)) &&
				opstdCB.isVisible()) {
				valueCB.setVisible(true);
				valueTF.setVisible(false);
			    }
			    else {
				valueCB.setVisible(false);
				valueTF.setVisible(true);
			    }
			}
		    });
	    }

	    colorB.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    }
		});
	}

	void setEnabled(boolean enabled) {
	    this.enabled = enabled;
	    opstdCB.setEnabled(enabled);
	    opnumCB.setEnabled(enabled);
	    valueCB.setEnabled(enabled);
	    valueTF.setEnabled(enabled);
	}

	void update(Property prop, PropertyAnnot annots[]) {
	    if (isPropNum(prop)) {
		//valueCB.setVisible(false);
		//valueTF.setVisible(true);
		opnumCB.setVisible(true);
		opstdCB.setVisible(false);
		//return;
	    }
	    else {
		opnumCB.setVisible(false);
		opstdCB.setVisible(true);
	    }

		    
	    TreeSet set = (TreeSet)propMap.get(prop);
	    if (set != null) {
		valueCB.removeAllItems();
		valueCB.addItem(new AnnotDisplayItem(""));
		Iterator it = set.iterator();
		while (it.hasNext()) {
		    String s = (String)it.next();
		    valueCB.addItem(new AnnotDisplayItem(s));
		}
	    }
		    
	    //opnumCB.setVisible(false);
	    //opstdCB.setVisible(true);
		    
	    String sitem = (String)opstdCB.getSelectedItem();
	    if (sitem != null && isOPCheckBox(PropertyElement.getOP(sitem))) {
		valueCB.setVisible(true);
		valueTF.setVisible(false);
	    }
	    else {
		valueCB.setVisible(false);
		valueTF.setVisible(true);
	    }

	    colorB.setBackground(DEFAULT_COLOR);
	    setOP(PropertyElement.EQUAL_OP);
	    valueTF.setVisible(false);
	    valueTF.setText("");
	    valueCB.setVisible(true);

	    if (annots == null)
		return;

	    for (int n = 0; n < annots.length; n++) {
		int op = annots[n].getOP();
		if (annots[n].getProperty() == prop &&
		    annots[n].getInd() == ind &&
		    hasValue(op, annots[n].getValue())) {
		    Color c = annots[n].getColor();
		    colorB.setBackground(c == null ? DEFAULT_COLOR : c);
		    setOP(op);
		    setValue(op, annots[n].getValue());
		}
	    }
	}

	void setOP(int op) {
	    String sop = PropertyElement.getStringOP(op);
	    // mind: test depends on isPropNum
	    if (opstdCB.isVisible()) {
		opstdCB.setSelectedItem(sop);
	    }
	    else {
		opnumCB.setSelectedItem(sop);
	    }
	}

	void setValue(int op, String value) {
	    if (isOPCheckBox(op)) {
		valueCB.setVisible(true);
		valueTF.setVisible(false);
		int cnt = valueCB.getItemCount();
		for (int i = 0; i < cnt; i++) {
		    AnnotDisplayItem item = (AnnotDisplayItem)valueCB.getItemAt(i);
		    if (item.toString().equals(value)) {
			valueCB.setSelectedItem(item);
			return;
		    }
		}
	    }
	    else {
		valueCB.setVisible(false);
		valueTF.setVisible(true);
		valueTF.setText(value);
	    }
	}

	void update(String propList[]) {
	    valueCB.removeAllItems();
	    colorB.setBackground(DEFAULT_COLOR);
	    /*
	    setOP(PropertyElement.EQUAL_OP);
	    valueTF.setVisible(false);
	    valueCB.setVisible(true);
	    */
	}
    }

    JCheckBox hideCB;
    JComboBox propCB, modeCB;

    private boolean isPropNum(Property prop) {
	PropertyType type = prop.getType();
	return type instanceof PropertyFloatNAType ||
	    type instanceof PropertyFloatType ||
	    type instanceof PropertyIntegerType ||
	    type instanceof PropertyIntegerNAType;
    }

    Property getProperty() {
	String sitem = (String)propCB.getSelectedItem();
	if (sitem == null || sitem.length() == 0 ||	propMap == null)
	    return null;
	
	return Property.getProperty(sitem);
    }

    void update(String propList[]) {
	Object sitem = propCB.getSelectedItem();
	if (sitem == null && propList.length > 0)
	    sitem = propList[0];
	propCB.removeAllItems();
	for (int n = 0; n < propList.length; n++)
	    propCB.addItem(propList[n]);
	
	if (sitem != null)
	    propCB.setSelectedItem(sitem);
    }

    AnnotDisplayLine adlines[];

    static final String ANNOT_DISPLAY_DIALOG = "AnnotDisplayDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(ANNOT_DISPLAY_DIALOG,
			  new AnnotDisplayDialog(globalContext));
    }

    public static void pop(GlobalContext globalContext, String title,
			   View view, GraphPanel panel, Property filter_prop) {
	AnnotDisplayDialog dialog =
	    (AnnotDisplayDialog)globalContext.get(ANNOT_DISPLAY_DIALOG);
	dialog.pop(title, view, panel, filter_prop);
    }

    public static void update(GlobalContext globalContext, View view,
			      GraphPanel panel) {
	AnnotDisplayDialog dialog =
	    (AnnotDisplayDialog)globalContext.get(ANNOT_DISPLAY_DIALOG);
	if (dialog.isVisible() && dialog.view == view && dialog.panel == panel)
	    dialog.update_perform();
    }

    private static final int PADX = 5;
    private static final int PADY = 5;

    JPanel mainPane;

    private AnnotDisplayDialog(GlobalContext globalContext) {
	super(new Frame(), VAMPUtils.getTitle() + ": Sample Annotation Panel");
	this.globalContext = globalContext;

	bgColor = VAMPResources.getColor(VAMPResources.DIALOG_BG);

	mainPane = new JPanel(new GridBagLayout());
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
	c.gridwidth = 10;
	mainPane.add(titleLabel, c);
	
	Utils.addPadPanel(mainPane, 0, y, PADX, PADY+PADY, bgColor);
	y++;

	propCB = makeCB(new String[]{"prop1", "prop2"});
				     
	c = Utils.makeGBC(x, y);
	mainPane.add(propCB, c);

	modeCB = makeCB(new String[]{"Global", "Local to view"});
				     
	c = Utils.makeGBC(x+2, y);
	c.gridwidth = 2;
	mainPane.add(modeCB, c);

	c = Utils.makeGBC(x+3, y);
	c.gridwidth = 3;
	hiddenLabel = new JLabel(NOT_HIDDEN_LABEL);
	hiddenLabel.setFont(new Font("MonoSpaced", Font.BOLD, 20));
	hiddenLabel.setForeground(Color.GRAY);
	//hiddenLabel.setVisible(false);
	mainPane.add(hiddenLabel, c);

	y++;

	Utils.addPadPanel(mainPane, 0, y, PADX, PADY+PADY, bgColor);
	y++;

	propCB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    updateProp();
		    /*
		    Property prop = getProperty();
		    if (prop == null)
			return;

		    PropertyAnnot annots[] = prop.getAnnotations(view);
		    for (int n = 0; n < adlines.length; n++)
			adlines[n].update(prop, annots);
		    boolean r = do_not_apply;
		    do_not_apply = true;
		    boolean is_hidden = prop.isPropertyAnnotHidden(view); 
		    hideCB.setSelected(is_hidden);
		    //hiddenLabel.setVisible(is_hidden);
		    hiddenLabel.setText(is_hidden ? HIDDEN_LABEL :
					NOT_HIDDEN_LABEL);
		    do_not_apply = r;
		    */
		}
	    });

	modeCB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    view.setAnnotGlobal(modeCB.getSelectedIndex() == 0);
		    update_perform();
		    if (!do_not_apply)
			apply_perform();
		}
	    });

	adlines = new AnnotDisplayLine[ADLINE_COUNT];
	x = left_x;
	for (int n = 0; n < adlines.length; n++) {
	    adlines[n] = new AnnotDisplayLine(mainPane, n, x, y);
	    y++;
	    Utils.addPadPanel(mainPane, 0, y, PADX, PADY, bgColor);
	    y++;
	}

	hideCB = new JCheckBox("Hide Annotations");
	hideCB.setBackground(bgColor);
	c = Utils.makeGBC(0, y);
	c.gridwidth = 2;
	mainPane.add(hideCB, c);
	hideCB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Property prop = getProperty();
		    if (!do_not_apply) {
			prop.setPropertyAnnotHidden(view, !prop.isPropertyAnnotHidden(view));
			view.repaint();
		    }
		    boolean is_hidden = prop.isPropertyAnnotHidden(view);
		    hiddenLabel.setText(is_hidden ? HIDDEN_LABEL :
					NOT_HIDDEN_LABEL);
		}
	    });

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

	removeB = new JButton("Remove");
	removeB.setBackground(Color.WHITE);
	buttonPane.add(removeB);
	removeB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    getProperty().setAnnotations(view, null);
		    update_perform();
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
	c.gridwidth = 10;
	mainPane.add(buttonPane, c);

	Utils.addPadPanel(mainPane, 0, y, PADX, PADY+PADY, bgColor);
	y++;

	pack();
	Utils.augment(this, 1.3, 1.1);
    }

    private void pop(String title, View view, GraphPanel panel,
		     Property filter_prop) {
	this.view = view;
	this.panel = panel;
	this.filter_prop = filter_prop;

	titleLabel.setText(title);

	update_perform();
	setVisible(true);
    }

    private void update_perform() {
	propMap = new TreeMap();

	boolean b = do_not_apply;
	do_not_apply = true;

	modeCB.setSelectedIndex(view.isAnnotGlobal() ? 0 : 1);

	do_not_apply = b;

	LinkedList list;
	if (view.isAnnotGlobal()) {
	    LinkedList view_list = View.getViewList(globalContext);
	    list = new LinkedList();
	    int sz = view_list.size();
	    for (int n = 0; n < sz; n++) {
		list.addAll(((View)view_list.get(n)).getGraphPanelSet().getGraphElements(View.ALL));
	    }		
	}
	else
	    list = view.getGraphPanelSet().getGraphElements(View.ALL);

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

		Object value = entry.getValue();
		TreeSet set = (TreeSet)propMap.get(prop);
		if (value != null) {
		    if (set == null) {
			set = new TreeSet();
			propMap.put(prop, set);
		    }
		    set.add(value.toString());
		}
	    }
	}

	String propList[] = make_prop_list();
	for (int n = 0; n < adlines.length; n++)
	    adlines[n].update(propList);

	update(propList);

	//display_prop_map();
    }
    
    private String[] make_prop_list() {
	String propList[] = new String[propMap.size()];
	Iterator it = propMap.entrySet().iterator();
	for (int n = 0; it.hasNext(); n++) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    propList[n] = prop.getName();
	}

	return propList;
    }

    private void display_prop_map() {
	Iterator it = propMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    System.out.println("prop " + prop.getName() + ":");
	    TreeSet set = (TreeSet)entry.getValue();
	    Iterator it2 = set.iterator();
	    while (it2.hasNext()) {
		System.out.println("\t" + it2.next());
	    }
	}
    }

    private PropertyAnnot[] makeAnnots(Property prop) {
	
	Vector annot_v = new Vector();
	for (int n = 0; n < adlines.length; n++) {
	    AnnotDisplayLine adline = adlines[n];
	    String value = adline.getValue();
	    if (value == null)
		continue;
	    
	    if (((String)value).trim().length() != 0) {
		PropertyAnnot pannot = new PropertyAnnot(prop,
							 //n,
							 adline.getOP(),
							 value,
							 adline.getColor());
		annot_v.add(pannot);
	    }
	}

	if (annot_v.size() == 0)
	    return null;

	PropertyAnnot annots[] = new PropertyAnnot[annot_v.size()];
	for (int n = 0; n < annots.length; n++)
	    annots[n] = (PropertyAnnot)annot_v.get(n);

	return annots;
    }

    private void apply_perform() {
	view.setAnnotDisplayFilterProp(filter_prop);
	Property prop = getProperty();
	if (prop != null) {
	    PropertyAnnot annots[] = makeAnnots(prop);
	    prop.setAnnotations(view, annots);
	    if (annots != null)
		panel.showHideEastY(true);
	}
	updateProp();
	view.repaint();
    }

    JDialog getThis() {return this;}

    private void updateProp() {
	Property prop = getProperty();
	if (prop == null)
	    return;

	PropertyAnnot annots[] = prop.getAnnotations(view);
	for (int n = 0; n < adlines.length; n++)
	    adlines[n].update(prop, annots);
	boolean r = do_not_apply;
	do_not_apply = true;
	boolean is_hidden = prop.isPropertyAnnotHidden(view); 
	hideCB.setSelected(is_hidden);
	hiddenLabel.setText(is_hidden ? HIDDEN_LABEL :
			    NOT_HIDDEN_LABEL);
	do_not_apply = r;
    }
}
