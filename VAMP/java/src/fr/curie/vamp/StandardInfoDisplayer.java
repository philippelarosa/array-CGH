
/*
 *
 * StandardInfoDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

class StandardInfoDisplayer extends InfoDisplayer {

    private JPanel panel = new JPanel(new GridBagLayout());
    private boolean displayElem;

    StandardInfoDisplayer() {
	this(true);
    }

    StandardInfoDisplayer(boolean displayElem) {
	this.displayElem = displayElem;
    }

    private class AnnotGE {
	PropertyAnnot annot;
	GraphElement graphElem;
	AnnotGE(PropertyAnnot annot, GraphElement graphElem) {
	    this.annot = annot;
	    this.graphElem = graphElem;
	}
    }

    public JPanel displayAnnotLegendPanel(JComponent parent, View view) {
	panel.removeAll();
	GridBagConstraints c;

	Property filter_prop = view.getAnnotDisplayFilterProp();
	LinkedList graphElements = view.getGraphElements(View.ALL);
	TreeMap annot_map = new TreeMap();
	int sz = graphElements.size();
	for (int n = 0; n < sz; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);
	    TreeMap properties = graphElement.getProperties();
	    Iterator it = properties.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		if (!prop.isInfoable())
		    continue;

		if (!prop.isEligible(view, filter_prop))
		    continue;

		PropertyAnnot annot = prop.getPropertyAnnot(view,
							    graphElement);
		if (annot == null)
		    continue;
		String tag = prop.getName() + " " +
		    PropertyElement.getStringOP(annot.getOP()) + " " +
		    annot.getValue();
		if (annot_map.get(tag) != null)
		    continue;
		annot_map.put(tag, new AnnotGE(annot, graphElement));
	    }
	}

	Font fontText = VAMPResources.getFont(VAMPResources.INFO_PANEL_TEXT_FONT);
	Font fontType = VAMPResources.getFont(VAMPResources.INFO_PANEL_TYPE_FONT);

	Color bgColor = VAMPResources.getColor
	    (VAMPResources.INFO_PANEL_BG);

	int y = 0;
	c = Utils.makeGBC(0, y);
	JLabel l1 = new JLabel("Annotation Legend" +
			       (filter_prop != null ? " (" + filter_prop.getName() + ")" : ""));
	l1.setFont(fontType);
	c = Utils.makeGBC(0, y);
	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.BOTH;
	c.gridwidth = 2;
	panel.add(l1, c);
	y++;
	Utils.addPadPanel(panel, 0, y, 1, 1, bgColor);
	y++;

	Iterator it = annot_map.entrySet().iterator();
	TreeMap tree = new TreeMap();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    String tag = (String)entry.getKey();
	    AnnotGE age = (AnnotGE)entry.getValue();
	    MLLabel l = new MLLabel();
	    l.setBackground(age.annot.getColor(age.graphElem));
	    l.setText("  ");
	    c = Utils.makeGBC(0, y);
	    c.anchor = GridBagConstraints.WEST;
	    c.fill = GridBagConstraints.BOTH;
	    c.ipadx = 5;
	    panel.add(l, c);
	    //Utils.addPadPanel(panel, 1, y, 3, 1, bgColor);

	    l1 = new JLabel(tag);
	    l1.setBackground(bgColor);
	    l1.setFont(fontText);
	    l1.setText("  " + tag);

	    c = Utils.makeGBC(1, y);
	    c.anchor = GridBagConstraints.WEST;
	    c.fill = GridBagConstraints.BOTH;
	    c.ipadx = 5;
	    c.weightx = 1.;
	    panel.add(l1, c);

	    y++;
	    Utils.addPadPanel(panel, 0, y, 1, 1, bgColor);
	    y++;
	}

	c = Utils.makeGBC(0, y);
	c.gridwidth = 2;
	JPanel padPanel = new JPanel();
	padPanel.setBackground(bgColor);
	Dimension size = parent.getSize();
	padPanel.setPreferredSize(new Dimension(size.width-20, 1));
	panel.add(padPanel, c);
	panel.setBackground(bgColor);
	return panel;
    }

    public JPanel display(JComponent parent, GraphElement set,
			  String propName, String sop, String value,
			  Color color) {
	panel.removeAll();

	PropertyElement elem = new PropertyElement();
	elem.setPropertyValue(VAMPProperties.TypeProp, "Annotation");
	elem.setPropertyValue(Property.getProperty("Array"), set.getID());
	/*
	elem.setPropertyValue(Property.getProperty("Property"), propName);
	elem.setPropertyValue(Property.getProperty("Operator"), sop);
	elem.setPropertyValue(Property.getProperty("Value"), value);
	*/
	elem.setPropertyValue(Property.getProperty("Property"),
			      propName + " " + sop + " " + value);
	elem.setPropertyValue(Property.getProperty("Color"),
			      color);
	int y = display(elem, panel, 0, false);
	
	Color bgColor = VAMPResources.getColor
	    (VAMPResources.INFO_PANEL_BG);
	GridBagConstraints c = Utils.makeGBC(0, y);
	c.gridwidth = 2;
	JPanel padPanel = new JPanel();
	padPanel.setBackground(bgColor);
	Dimension size = parent.getSize();
	padPanel.setPreferredSize(new Dimension(size.width-20, 1));
	panel.add(padPanel, c);
	panel.setBackground(bgColor);
	return panel;
    }

    public JPanel display(JComponent parent, GraphElement set,
			  PropertyElement elem, Region region, Mark mark,
			  boolean pinnedUp) {
	panel.removeAll();
	int y = 0;

	Color bgColor = (pinnedUp ? 
			 VAMPResources.getColor
			 (VAMPResources.INFO_PANEL_PINNED_UP_BG) :
			 VAMPResources.getColor
			 (VAMPResources.INFO_PANEL_BG));
	y = display(region, panel, y, pinnedUp);
	y = display(mark, panel, y, pinnedUp);

	y = display(set, panel, y, pinnedUp);

	if (displayElem) {
	    y = display(elem, panel, y, pinnedUp);
	}

	GridBagConstraints c = Utils.makeGBC(0, y);
	c.gridwidth = 2;
	JPanel padPanel = new JPanel();
	padPanel.setBackground(bgColor);
	Dimension size = parent.getSize();
	padPanel.setPreferredSize(new Dimension(size.width-20, 1));
	panel.add(padPanel, c);
	panel.setBackground(bgColor);
	return panel;
    }

    private int display(PropertyElement elem, JPanel panel, int y,
			boolean pinnedUp) {
	if (elem == null)
	    return y;

	Iterator it = elem.getProperties().entrySet().iterator();
	JLabel label;
	    
	Object value = elem.getPropertyValue(VAMPProperties.TypeProp);
	GridBagConstraints c;
	Font fontText = VAMPResources.getFont(VAMPResources.INFO_PANEL_TEXT_FONT);
	Font fontType = VAMPResources.getFont(VAMPResources.INFO_PANEL_TYPE_FONT);
	if (value != null) {
	    label = new JLabel(VAMPProperties.TypeProp.getInfoValue(value));
	    label.setFont(fontType);
	    label.setForeground(VAMPResources.getColor
				(VAMPResources.INFO_TITLE_FG));
	    c = Utils.makeGBC(0, y);
	    c.gridwidth = 2;
	    c.anchor = GridBagConstraints.CENTER;
	    c.fill = GridBagConstraints.BOTH;
	    panel.add(label, c);
	    y++;
	}

	Color bgColor = (pinnedUp ? 
			 VAMPResources.getColor
			 (VAMPResources.INFO_PANEL_PINNED_UP_BG) :
			 VAMPResources.getColor
			 (VAMPResources.INFO_PANEL_BG));

	Vector ordProperties = PropertyManager.getInstance().makePropertyList
	    (VAMPUtils.getType(elem), elem.getProperties());

	int sz1 = ordProperties.size();
	for (int m = 0; m < sz1; m++) {
	    Property prop = (Property)ordProperties.get(m);
	    if (!prop.isInfoable())
		continue;

	    if (elem.isPropertyMasked(prop))
		continue;

	    Object prop_value = elem.getPropertyValue(prop);
	    if (prop_value == null || prop_value.equals("")) continue;
	    label = new JLabel(prop.getName());
	    label.setFont(fontText);
	    label.setForeground(VAMPResources.getColor
				(VAMPResources.INFO_PROPERTY_FG));
	    c = Utils.makeGBC(0, y);
	    c.ipadx = 10;
	    c.ipady = 5;
	    c.fill = GridBagConstraints.BOTH;
	    c.anchor = GridBagConstraints.WEST;
	    panel.add(label, c);

	    c = Utils.makeGBC(1, y);
	    c.anchor = GridBagConstraints.WEST;
	    MLLabel mlabel = new MLLabel();
	    if (prop_value instanceof Color) {
		mlabel.setBackground((Color)prop_value);
		mlabel.setText("   ");
		//c.fill = GridBagConstraints.VERTICAL;
	    }
	    else {
		c.fill = GridBagConstraints.BOTH;
		mlabel.setFont(fontText);
		mlabel.setForeground(VAMPResources.getColor
				     (VAMPResources.INFO_VALUE_FG));
		mlabel.setBackground(bgColor);
		mlabel.setText(Utils.performRound(prop.getInfoValue(prop_value)));
	    }

	    panel.add(mlabel, c);
	    y++;
	}
	return y;
    }
}


