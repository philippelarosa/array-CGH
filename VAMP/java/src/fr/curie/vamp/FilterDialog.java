
/*
 *
 * FilterDialog.java
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

class FilterDialog extends JDialog {

    private GlobalContext globalContext;
    private View view;
    private GraphPanel panel;
    private Property filter_prop;
    private JLabel titleLabel;
    private Color bgColor;
    private JRadioButton selectRB, filterRB, filternvRB;
    private JButton okB, applyB, cancelB;
    private TreeMap propMap;
    private JLabel foundLabel;

    // should be in resources
    static final int FLINE_COUNT = 6;
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

    class FilterItem {
	String item;
	String cb_item;

	FilterItem(String item) {
	    item = item.trim();
	    this.item = item;
	    /*
	    if (item.length() >= MAX_ITEM_LENGTH) {
		cb_item = item.substring(0, MAX_ITEM_LENGTH - 3);
		cb_item += "@@@";
	    }
	    else {
		cb_item = item;
	    }
	    */
	    cb_item = item;
	}

	public String toString() {return cb_item;}
	String getItem() {return item;}
    }

    private class FilterLine {
	private JComboBox openCB;
	private JComboBox propCB;
	private JComboBox opstdCB;
	private JComboBox opnumCB;
	private JComboBox valueCB;
	private JTextField valueTF;
	private JComboBox closeCB;
	private JComboBox andorCB;
	private int ind;
	private boolean enabled;

	FilterLine(JPanel mainPane, int ind, int x, int y) {
	    this.ind = ind;

	    GridBagConstraints c;
	    openCB = makeCB(new String[]{"", "("});
	    c = Utils.makeGBC(x, y); x += 2;
	    mainPane.add(openCB, c);

	    propCB = makeCB(new String[]{"prop1", "prop2"});
				     
	    c = Utils.makeGBC(x, y); x += 2;
	    mainPane.add(propCB, c);

	    opstdCB = makeCB(new String[]{"=", "!=",  "~", "!~"});
	    c = Utils.makeGBC(x, y); x += 2;
	    mainPane.add(opstdCB, c);

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
	    c.fill = GridBagConstraints.BOTH;
	    mainPane.add(valueTF, c);
	    valueTF.setVisible(false);
	    x += 2;

	    closeCB = makeCB(new String[]{"", " )"});
	    c = Utils.makeGBC(x, y); x += 2;
	    mainPane.add(closeCB, c);

	    andorCB = makeCB(new String[]{"", "and", "or"});
	    if (ind != flines.length-1) {
		c = Utils.makeGBC(x, y+1); x += 2;
		mainPane.add(andorCB, c);
	    }

	    setListeners();
	    setEnabled(ind == 0);
	}

	Property getProperty() {
	    String sitem = (String)propCB.getSelectedItem();
	    if (sitem == null || sitem.length() == 0 ||	propMap == null)
		return null;

	    return Property.getProperty(sitem);
	}

	String getValue() {
	    if (valueTF.isVisible())
		return valueTF.getText();
	    return ((FilterItem)valueCB.getSelectedItem()).getItem();
	}

	boolean isEND() {
	    return andorCB.getSelectedItem().equals("");
	}

	boolean isAND() {
	    return andorCB.getSelectedItem().equals("and");
	}

	boolean isOR() {
	    return andorCB.getSelectedItem().equals("or");
	}

	boolean isOpen() {
	    return !openCB.getSelectedItem().equals("");
	}

	boolean isClose() {
	    return !closeCB.getSelectedItem().equals("");
	}

	int getOP() {
	    String sop = (String)
		(opstdCB.isVisible() ? opstdCB.getSelectedItem() :
		 opnumCB.getSelectedItem());
	    return PropertyElement.getOP(sop);
	}

	private boolean isPropNum(Property prop) {
	    PropertyType type = prop.getType();
	    return type instanceof PropertyFloatNAType ||
		type instanceof PropertyFloatType ||
		type instanceof PropertyIntegerType ||
		type instanceof PropertyIntegerNAType;
	}

	private void setListeners() {
	    JComboBox cb[] = new JComboBox[]{opstdCB, opnumCB};
	    for (int n = 0; n < cb.length; n++) {
		cb[n].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    JComboBox cb = (JComboBox)e.getSource();
			    String sitem = (String)cb.getSelectedItem();
			    if ((sitem.equals("=") || sitem.equals("!=")) &&
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
	    
	    propCB.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			Property prop = getProperty();
			if (prop == null)
			    return;

			if (isPropNum(prop)) {
			    valueCB.setVisible(false);
			    valueTF.setVisible(true);
			    opnumCB.setVisible(true);
			    opstdCB.setVisible(false);
			    return;
			}

			TreeSet set = (TreeSet)propMap.get(prop);
			if (set != null) {
			    valueCB.removeAllItems();
			    Iterator it = set.iterator();
			    while (it.hasNext()) {
				String s = (String)it.next();
				valueCB.addItem(new FilterItem(s));
			    }
			}

			opnumCB.setVisible(false);
			opstdCB.setVisible(true);

			String sitem = (String)opstdCB.getSelectedItem();
			if (sitem != null && 
			    (sitem.equals("=") || sitem.equals("!="))) {
			    valueCB.setVisible(true);
			    valueTF.setVisible(false);
			}
			else {
			    valueCB.setVisible(false);
			    valueTF.setVisible(true);
			}
		    }
		});

	    andorCB.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			int n = ind+1;
			for (; n < flines.length; n++) {
			    if (flines[n-1].andorCB.getSelectedItem().
				equals(""))
				break;
			    flines[n].setEnabled(true);
			}

			for (; n < flines.length; n++)
			    flines[n].setEnabled(false);
		    }
		});
	}

	void setEnabled(boolean enabled) {
	    this.enabled = enabled;
	    openCB.setEnabled(enabled);
	    propCB.setEnabled(enabled);
	    opstdCB.setEnabled(enabled);
	    opnumCB.setEnabled(enabled);
	    valueCB.setEnabled(enabled);
	    valueTF.setEnabled(enabled);
	    closeCB.setEnabled(enabled);
	    andorCB.setEnabled(enabled);
	}

	void update(String propList[]) {
	    valueCB.removeAllItems();
	    //valueTF.setText("");
	    //andorCB.setSelectedItem("");

	    Object sitem = propCB.getSelectedItem();
	    if (sitem == null && propList.length > 0)
		sitem = propList[0];
	    propCB.removeAllItems();
	    for (int n = 0; n < propList.length; n++)
		propCB.addItem(propList[n]);

	    if (sitem != null)
		propCB.setSelectedItem(sitem);
	}
    }

    FilterLine flines[];

    static final String FILTER_DIALOG = "FilterDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(FILTER_DIALOG,
			  new FilterDialog(globalContext));
    }

    public static void pop(GlobalContext globalContext, String title,
			   View view, GraphPanel panel, Property filter_prop) {
	FilterDialog dialog =
	    (FilterDialog)globalContext.get(FILTER_DIALOG);
	dialog.pop(title, view, panel, filter_prop);
    }

    public static void update(GlobalContext globalContext, View view,
			      GraphPanel panel) {
	FilterDialog dialog =
	    (FilterDialog)globalContext.get(FILTER_DIALOG);
	if (dialog.isVisible() && dialog.view == view && dialog.panel == panel)
	    dialog.update_perform();
    }

    private static final int PADX = 5;
    private static final int PADY = 5;

    private FilterDialog(GlobalContext globalContext) {
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
	c.gridwidth = 10;
	mainPane.add(titleLabel, c);
	
	Utils.addPadPanel(mainPane, 0, y, PADX, PADY+PADY, bgColor);
	y++;

	String[] labels = new String[]{"(", "Property", "OP", "Value", ")", "and/or"};
	x = left_x;
	for (int n = 0; n < labels.length; n++) {
	    JLabel l = new JLabel(labels[n]);
	    c = Utils.makeGBC(x, y);
	    x++;
	    mainPane.add(l, c);
	    Utils.addPadPanel(mainPane, x, y, PADX, PADY, bgColor);
	    x++;
	}
	y++;

	Utils.addPadPanel(mainPane, 0, y, PADX, PADY, bgColor);
	y++;

	flines = new FilterLine[FLINE_COUNT];
	x = left_x;
	for (int n = 0; n < flines.length; n++) {
	    flines[n] = new FilterLine(mainPane, n, x, y);
	    y++;
	    Utils.addPadPanel(mainPane, 0, y, PADX, PADY, bgColor);
	    y++;
	}

	Utils.addPadPanel(mainPane, 0, y, PADX, PADY, bgColor);
	y++;

	x = 5;
	ButtonGroup group = new ButtonGroup();
	selectRB = new JRadioButton("Select");
	selectRB.setBackground(bgColor);
	selectRB.setSelected(true);
	group.add(selectRB);
	c = Utils.makeGBC(x, y);
	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = 5;
	y++;
	mainPane.add(selectRB, c);
	
	//	filterRB = new JRadioButton("Filter in current view");
	filterRB = new JRadioButton("Filter");
	filterRB.setBackground(bgColor);
	group.add(filterRB);
	c = Utils.makeGBC(x, y);
	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = 5;
	y++;
	mainPane.add(filterRB, c);
	
	filternvRB = new JRadioButton("Filter in new view");
	filternvRB.setBackground(bgColor);
	group.add(filternvRB);
	c = Utils.makeGBC(x, y);
	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = 5;
	y++;
	mainPane.add(filternvRB, c);

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
	c.gridwidth = 10;
	mainPane.add(buttonPane, c);

	Utils.addPadPanel(mainPane, 0, y, PADX, PADY+PADY, bgColor);
	y++;

	foundLabel = new JLabel();
	x = left_x;
	c = Utils.makeGBC(x, y);
	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = 5;
	mainPane.add(foundLabel, c);

	pack();
	Utils.augment(this, 1.5, 1.1);
    }

    private void pop(String title, View view, GraphPanel panel, Property filter_prop) {
	this.view = view;
	this.panel = panel;
	this.filter_prop = filter_prop;

	titleLabel.setText(title);
	foundLabel.setText("");
	selectRB.setSelected(true);

	update_perform();
	setVisible(true);
    }

    private void update_perform() {
	propMap = new TreeMap();

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

		Object value = entry.getValue();
		if (value != null) {
		    TreeSet set = (TreeSet)propMap.get(prop);
		    if (set == null) {
			set = new TreeSet();
			propMap.put(prop, set);
		    }
		    set.add(value.toString());
		}
	    }
	}

	String propList[] = make_prop_list();
	for (int n = 0; n < flines.length; n++)
	    flines[n].update(propList);

	filterRB.setEnabled(!panel.isReadOnly());
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

    abstract class FilterNode {

	abstract boolean eval(GraphElement elem);

    }

    class FilterLeaveNode extends FilterNode {
	Property prop;
	String value;
	double dvalue;
	int op;
	Pattern pattern;

	FilterLeaveNode(Property prop, int op, String value) {
	    this.prop = prop;
	    this.op = op;
	    this.value = value;

	    if (op == PropertyElement.PATTERN_EQUAL_OP ||
		op == PropertyElement.PATTERN_DIFF_OP)
		pattern = new Pattern(value, true);

	    if (op == PropertyElement.GT_OP || op == PropertyElement.GE_OP ||
		op == PropertyElement.LT_OP || op == PropertyElement.LE_OP)
		dvalue = Double.parseDouble(value);
	}

	boolean eval(GraphElement elem) {
	    return elem.matches(prop, op, (pattern != null ? (Object)pattern :
					   (Object)value));
	    /*
	    Object ovalue = elem.getPropertyValue(prop);
	    String svalue = (ovalue == null ? "" : ovalue.toString());
	    if (op == EQUAL_OP)
		return value.equals(svalue);

	    if (op == DIFF_OP)
		return !value.equals(svalue);

	    if (op == PATTERN_EQUAL_OP)
		return pattern.matches(svalue);

	    if (op == PATTERN_DIFF_OP)
		return !pattern.matches(svalue);
								
	    if (op == GT_OP || op == GE_OP || op == LT_OP || op == LE_OP) {
		double d = Double.parseDouble(svalue);

		if (op == GT_OP)
		    return d > dvalue;
		if (op == GE_OP)
		    return d >= dvalue;
		if (op == LT_OP)
		    return d < dvalue;
		if (op == LE_OP)
		    return d <= dvalue;
	    }

	    return false;
	    }
	    */
	}
    }

    class FilterBinNode extends FilterNode {
	FilterNode left, right;
	boolean or;

	FilterBinNode(boolean or, FilterNode left, FilterNode right) {
	    this.or = or;
	    this.right = right;
	    this.left = left;
	}

	boolean eval(GraphElement elem) {
	    if (or)
		return left.eval(elem) || right.eval(elem);
	    return left.eval(elem) && right.eval(elem);
	}
    }

    static final int NODE_TYPE = 1;
    static final int AND_TYPE = 2;
    static final int OR_TYPE = 3;
    static final int OPEN_TYPE = 4;
    static final int CLOSE_TYPE = 5;

    class StackParser {

	private class StackItem {
	    
	    FilterNode node;
	    int type;

	    StackItem(FilterNode node) {
		this.node = node;
		this.type = NODE_TYPE;
	    }
	    
	    StackItem(int type) {
		this.type = type;
	    }
	}

	Stack stack;

	StackParser() {
	    stack = new Stack();
	}

	void shift(FilterNode node) {
	    stack.push(new StackItem(node));
	}

	void shift(int type) {
	    stack.push(new StackItem(type));
	}

	FilterNode getTopNode() {
	    if (stack.size() != 1)
		return null;
	    StackItem item = (StackItem)stack.pop();
	    return item.node;
	}

	void reduce(boolean end) {
	    FilterNode node = null;
	    int op = 0;
	    while (stack.size() > 0) {
		StackItem item = (StackItem)stack.peek();
		if (item.type == NODE_TYPE) {
		    if (node == null)
			node = item.node;
		    else
			node = new FilterBinNode(op == OR_TYPE, item.node, node);
		}
		else if (item.type == AND_TYPE || item.type == OR_TYPE)
		    op = item.type;
		else if (item.type == OPEN_TYPE || stack.size() == 1) {
		    if (!end) {
			shift(node);
			break;
		    }
		}

		stack.pop();
		if (stack.size() == 0) {
		    shift(node);
		    break;
		}
	    }
	}
    }

    private FilterNode makeTree() {

	StackParser sparser = new StackParser();

	for (int n = 0; n < flines.length; n++) {
	    FilterLine fline = flines[n];
	    Property prop = fline.getProperty();
	    int op = fline.getOP();
	    if (prop == null || op == 0)
		break;

	    if (fline.isOpen())
		sparser.shift(OPEN_TYPE);

	    FilterLeaveNode node = new FilterLeaveNode
		(prop, op, fline.getValue());

	    sparser.shift(node);
	    if (fline.isClose())
		sparser.reduce(true);
	    else
		sparser.reduce(false);
	    
	    if (fline.isEND())
		break;

	    sparser.shift(fline.isOR() ? OR_TYPE : AND_TYPE);
	}

	sparser.reduce(true);
	FilterNode root_node = null;
	return sparser.getTopNode();
    }

    private void setFoundLabel(int sz) {
	foundLabel.setText(sz + " element" + (sz == 0 || sz > 1 ? "s" : "") +
			   " found");
    }

    private void apply_perform() {
	FilterNode tree = makeTree();
	if (tree == null) {
	    setFoundLabel(0);
	    return;
	}

	LinkedList list = panel.getGraphElements();
	int sz = list.size();

	LinkedList glist = new LinkedList();
	for (int n = 0; n < sz; n++) {
	    GraphElement ge = (GraphElement)list.get(n);
	    if (tree.eval(ge))
		glist.add(ge);
	}

	sz = glist.size();
	setFoundLabel(sz);

	if (selectRB.isSelected()) {
	    panel.selectAll(false);
	    for (int n = 0; n < sz; n++)
		((GraphElement)glist.get(n)).setSelected(true, panel.getCanvas());
	    panel.repaint();
	}
	else if (filterRB.isSelected()) {
	    StandardVMStatement vmstat = new StandardVMStatement
		(VMOP.getVMOP(VMOP.FILTER), panel);
	    vmstat.beforeExecute();
	    UndoVMStack.getInstance(panel).push(vmstat);

	    panel.setGraphElements(glist);
	    view.syncGraphElements();
	}
	else if (filternvRB.isSelected()) {
	    GraphPanelSet panelSet = view.getGraphPanelSet();

	    LinkedList glist_c = new LinkedList();
	    sz = glist.size();
	    for (int n = 0; n < sz; n++)
		glist_c.add(((GraphElement)glist.get(n)).clone());
	    ViewFrame vf = new ViewFrame(view.getGlobalContext(),
					 view.getName(),
					 panelSet.getPanelProfiles(),
					 panelSet.getPanelLayout(),
					 panelSet.getPanelLinks(),
					 null, null,
					 new LinkedList(),
					 Config.defaultDim,
					 null);
	    vf.getView().getGraphPanelSet().getPanel(panel.getWhich()).setGraphElements(glist_c);
	    vf.setVisible(true);
	    vf.getView().syncGraphElements();
	}
    }
}
