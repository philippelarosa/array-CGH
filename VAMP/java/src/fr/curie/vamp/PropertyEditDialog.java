
/*
 *
 * PropertyEditDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import fr.curie.vamp.data.Probe;

class PropertyEditDialog extends JDialog {

    private GlobalContext globalContext;
    private View view;
    private PropertyElement elem;
    private Probe probe;
    private Vector itemV;
    private Color bgColor;
    private static final int TEXT_FIELD_COLUMNS = 12;
    private boolean init = true;

    static final String PROPERTY_EDIT_DIALOG = "PropertyEditDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(PROPERTY_EDIT_DIALOG,
			  new PropertyEditDialog(globalContext));
    }

    public static void pop(GlobalContext globalContext, View view,
			   PropertyElement elem, Probe probe) {
	PropertyEditDialog dialog =
	    (PropertyEditDialog)globalContext.get(PROPERTY_EDIT_DIALOG);
	dialog.pop(view, elem, probe);
    }

    public static void unpop(GlobalContext globalContext) {
	PropertyEditDialog dialog =
	    (PropertyEditDialog)globalContext.get(PROPERTY_EDIT_DIALOG);
	dialog.setVisible(false);
    }

    private PropertyEditDialog(GlobalContext globalContext) {
	super(new Frame(), VAMPUtils.getTitle() + ": Property Editor");

	this.globalContext = globalContext;
    }

    String lastType = null;

    void pop(View view, PropertyElement elem, Probe probe) {
	this.view = view;
	this.elem = elem;
	this.probe = probe;

	bgColor = VAMPResources.getColor(VAMPResources.DIALOG_BG);
	//Container mainPane = getContentPane();
	Container mainPane = new JPanel(new BorderLayout());
	mainPane.setBackground(bgColor);

	JPanel titlePane = makeTitlePane();
	titlePane.setBackground(bgColor);
	mainPane.add(titlePane, BorderLayout.NORTH);

	JPanel propPane = makePropPane();
	propPane.setBackground(bgColor);
	JScrollPane scrollPane = new JScrollPane(propPane);
	mainPane.add(scrollPane, BorderLayout.CENTER);
	
	JPanel buttonPane = makeButtonPane();
	buttonPane.setBackground(bgColor);
	mainPane.add(buttonPane, BorderLayout.SOUTH);
	
	setContentPane(mainPane);

	if (init) {
	    setSize(450, 700);
	}

	/*
	if (!isVisible()) {
	    pack();
	    Utils.augment(this, 1.1, 1.05);
	}
	*/


	lastType = VAMPUtils.getType(elem);
	
	alignTextFields();

	if (init)
	    Utils.centerOnScreen(this, false);
	init = false;

	setVisible(true);
    }

    static final int MAX_LEN = 40;

    JPanel makeTitlePane() {
	JPanel titlePane = new JPanel();
	String type = VAMPUtils.getType(elem);
	String id = (String)elem.getID();
	if (id.length() > MAX_LEN)
	    id = id.substring(0, MAX_LEN) + "...";

	id = id.replaceAll("\n", " ");

	String s;
	if (id != null)
	    s = type + " " + id;
	else
	    s = type;

	JLabel label = new JLabel(s);
	label.setFont
	    (VAMPResources.getFont(VAMPResources.INFO_PANEL_TITLE_FONT));
	titlePane.add(label);
	return titlePane;
    }

    JPanel makePropPane() {
	itemV = new Vector();

	JPanel propPane = new JPanel(new GridBagLayout());
	GridBagConstraints c;

	Vector ordProperties = PropertyManager.getInstance().makePropertyList
	    (VAMPUtils.getType(elem), elem.getProperties());
	int sz1 = ordProperties.size();

	int n = 0;
	/*
	TreeMap properties = elem.getProperties();
	Iterator it = properties.entrySet().iterator();
	*/
	boolean skipNoEditable = false;
	Font textFont =
	    VAMPResources.getFont(VAMPResources.INFO_PANEL_TEXT_FONT);

	for (int m = 0; m < sz1; m++) {
	    Property prop = (Property)ordProperties.get(m);
	    System.out.println(prop.getName() + " " + elem.getPropertyValue(prop));
	}

	for (int m = 0; m < sz1; m++) {
	    Property prop = (Property)ordProperties.get(m);
	    /*
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    */
	    if (!prop.isInfoable()) continue;
	    if (skipNoEditable && !prop.isEditable()) continue;

	    //Object value = entry.getValue();
	    Object value = elem.getPropertyValue(prop);
	    JLabel label = new JLabel(prop.getName());
	    c = Utils.makeGBC(0, 2*n);
	    c.anchor = GridBagConstraints.WEST;
	    propPane.add(label, c);

	    PropertyType propType = prop.getType();
	    Component comp;
	    // 1/2/08: if probe != null, no property is editable
	    if (probe == null && prop.isEditable()) {
		comp = propType.makeComponent(value);
	    }
	    else
		comp = new MLLabel();

	    comp.setFont(textFont);

	    if (comp instanceof MLLabel) {
		if (value != null)
		    ((MLLabel)comp).setText(value.toString());
		comp.setBackground(bgColor);
	    }

	    JPanel padPane = new JPanel();
	    padPane.setSize(10, 1);
	    padPane.setBackground(bgColor);

	    c = Utils.makeGBC(1, 2 * n);
	    propPane.add(padPane, c);

	    c = Utils.makeGBC(2, 2 * n);
	    c.anchor = GridBagConstraints.WEST;
	    propPane.add(comp, c);

	    padPane = new JPanel();
	    padPane.setSize(2, 5);
	    padPane.setBackground(bgColor);

	    c = Utils.makeGBC(1, 2 * n + 1);
	    propPane.add(padPane, c);

	    if (prop.isEditable())
		itemV.add(new Item(prop, value, comp));
	    n++;
	}

	return propPane;
    }

    JPanel makeButtonPane() {
	JPanel buttonPane = new JPanel(new FlowLayout());

	JButton ok = new JButton("OK");
	ok.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    perform(true);
		}
	    });
	buttonPane.add(ok);

	JButton apply = new JButton("Apply");
	apply.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    perform(false);
		}
	    });
	buttonPane.add(apply);

	JButton cancel = new JButton("Cancel");
	cancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });
	buttonPane.add(cancel);

	return buttonPane;
    }

    class Item {
	Property prop;
	Object initial_value;
	Component comp;

	Item(Property prop, Object initial_value, Component comp) {
	    this.prop = prop;
	    this.initial_value = initial_value;
	    this.comp = comp;
	}

	void reset(Object initial_value) {
	    this.initial_value = initial_value;
	}
    }

    void perform(boolean hide) {
	int size = itemV.size();
	System.out.println("probe: " + probe);
	if (check()) {
	    System.out.println("checked");
	    for (int n = 0; n < size; n++) {
		Item item = (Item)itemV.get(n);
		Property prop = item.prop;
		String value = getValue(item.comp);
		
		if (!item.initial_value.equals(value)) {
		    System.out.println("differed: " + prop.getName());
		    elem.setPropertyValue(prop, value);
		    if (probe != null) {
			probe.addProp(prop, value);
			System.out.println("value: " + probe.getProp(prop));
		    }
		    item.reset(value);
		}
	    }

	    if (probe != null)
		probe.print();

	    System.out.println("has Edited properties of this propertyElement");
	    view.sync(true);
	    if (hide)
		setVisible(false);
	}
    }


    boolean check() {
	int size = itemV.size();
	boolean ok = true;
	for (int n = 0; n < size; n++) {
	    Item item = (Item)itemV.get(n);
	    Property prop = item.prop;
	    String value = getValue(item.comp);

	    if (!item.initial_value.equals(value)) {
		/*
		System.out.println("has changed " + prop.getName() + " -> " +
				   value + " from " +
				   item.initial_value);
		*/

		if (!prop.getType().checkValue(value)) {
		    ok = false;
		    InfoDialog.pop(globalContext,
				   "Property " + prop.getName() +
				   ": value " + value + " is incorrect");
		}
	    }
	}

	return ok;
    }

    static String getValue(Component comp) {
	if (comp instanceof JComboBox)
	    return (String)((JComboBox)comp).getSelectedItem();

	return ((JTextComponent)comp).getText().trim();
    }

    private void alignTextFields() {
	int size = itemV.size();
	for (int n = 0; n < size; n++) {
	    Item item = (Item)itemV.get(n);
	    if (!(item.comp instanceof JTextField)) continue;
	    JTextField tf = (JTextField)item.comp;
	    if (tf.getColumns() < TEXT_FIELD_COLUMNS)
		tf.setColumns(TEXT_FIELD_COLUMNS);
	}
    }
}
