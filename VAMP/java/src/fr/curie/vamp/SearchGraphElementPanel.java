
/*
 *
 * SearchGraphElementPanel.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

class SearchGraphElementPanel extends SearchPanel {

    private MLLabel graphElementLabel;
    private JComboBox graphElementComboB;
    private JComboBox canvasComboB;
    private JTextField graphElementTF;
    private JButton findAllB, findFirstB, findNextB;
    private JButton clearButton;
    private JCheckBox backwardCheckB;
    private SearchGraphElementCursor graphElementCursor;

    private JLabel notFoundLabel;

    SearchGraphElementPanel(PanelProfile panelProfiles[]) {
	setBackground(bgColor);
	setLayout(panelProfiles);
	this.canvas = new GraphCanvas[panelProfiles.length];
    }

    private void setLayout(PanelProfile panelProfiles[]) {
	setLayout(new GridBagLayout());
	GridBagConstraints c;

	// first line
	int y = 0;
	int x_label = 1, x_combo = 1, x_tf = 3;

	Color buttonBg = VAMPResources.getColor(VAMPResources.SEARCH_PANEL_BUTTON_BG);
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);
	if (panelProfiles.length > 1) {
	    y++;
	    canvasComboB = new JComboBox();
	    canvasComboB.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			update(canvasComboB.getSelectedIndex());
		    }
		});

	    for (int n = 0; n < panelProfiles.length; n++) {
		canvasComboB.addItem(panelProfiles[n].getName());
		
		setFGB(canvasComboB, buttonBg);
		c = Utils.makeGBC(x_label, y);
		c.gridwidth = 3;
		add(canvasComboB, c);
		y++;
		Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);
	    }
	}

	// second line
	y++;
	graphElementLabel = new MLLabel("Set");
	setFGB(graphElementLabel);
	graphElementLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
	c = Utils.makeGBC(x_label, y);
	c.gridwidth = 3;
	add(graphElementLabel, c);

	y++;
	Utils.addPadPanel(this, 2, y, PADW, PADH, bgColor);

	y++;
	graphElementComboB = new JComboBox(defProps);
	setFGB(graphElementComboB, buttonBg);
	c = Utils.makeGBC(x_combo, y);
	add(graphElementComboB, c);
	Utils.addPadPanel(this, 4, 0, PADW, PADH, bgColor);
	graphElementTF = new JTextField(10);
	setFGB(graphElementTF, buttonBg);
	c = Utils.makeGBC(x_tf, y);
	c.anchor = GridBagConstraints.WEST;
	c.weightx = 1.;
	c.fill = GridBagConstraints.HORIZONTAL;
	add(graphElementTF, c);
	Utils.addPadPanel(this, 6, 0, PADW, PADH, bgColor);

	// third line
	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);

	y += addHelp(x_tf, y);

	/*
	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);
	*/

	findAllB = new JButton("Find All");
	setFGB(findAllB, buttonBg);
	c = Utils.makeGBC(x_combo, y);
	add(findAllB, c);

	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);

	// third line
	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);
	findFirstB = new JButton();
	setFGB(findFirstB, buttonBg);
	c = Utils.makeGBC(x_combo, y);
	add(findFirstB, c);

	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);
	findNextB = new JButton();
	setFGB(findNextB, buttonBg);
	c = Utils.makeGBC(x_tf, y);
	add(findNextB, c);

	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);

	y++;
	JLabel backwardLabel = new JLabel("Search backwards");

	c = Utils.makeGBC(x_combo, y);
	setFGB(backwardLabel);
	add(backwardLabel, c);
	backwardCheckB = new JCheckBox();
	setFGB(backwardCheckB);
	c = Utils.makeGBC(x_combo, y+1);
	add(backwardCheckB, c);

	clearButton = new JButton("Clear");
	setFGB(clearButton, buttonBg);
	c = Utils.makeGBC(x_tf, y);
	c.gridheight = 2;
	add(clearButton, c);

	setBackward(backwardCheckB.isSelected());

	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);

	y++;
	notFoundLabel = new JLabel(" ");
	notFoundLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
	c = Utils.makeGBC(x_combo, y);
	add(notFoundLabel, c);

	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);

	setListeners();
    }

    private void setListeners() {
	backwardCheckB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setBackward(((JCheckBox)e.getSource()).isSelected());
		}
	    });

	graphElementComboB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    resetCursors();
		}
	    });

	findAllB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (graphElementComboB.getSelectedIndex() != VOID_INDEX) {
			graphElementCursor = getSelectedCanvas().searchGraphElement
			    (null, (String)graphElementComboB.getSelectedItem(),
			     graphElementTF.getText(), false, true);
			setFound(graphElementCursor != null);
		    }
		    else {
			InfoDialog.pop(getSelectedCanvas().getGlobalContext(),
				       CHOOSE_PROP);
			setFound(false);
		    }
		}
	    });

	findFirstB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (graphElementComboB.getSelectedIndex() != VOID_INDEX) {
			graphElementCursor = getSelectedCanvas().searchGraphElement
			    (null, (String)graphElementComboB.getSelectedItem(),
			     graphElementTF.getText(),
			     backwardCheckB.isSelected(), false);
			setFound(graphElementCursor != null);
		    }
		    else {
			InfoDialog.pop(getSelectedCanvas().getGlobalContext(),
				       CHOOSE_PROP);
			setFound(false);
		    }
		}
	    });

	findNextB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (graphElementComboB.getSelectedIndex() != VOID_INDEX) {
			graphElementCursor = getSelectedCanvas().searchGraphElement
			    (graphElementCursor, (String)graphElementComboB.getSelectedItem(),
			     graphElementTF.getText(),
			     backwardCheckB.isSelected(), false);
			setFound(graphElementCursor != null);
		    }
		    else
			setFound(false);
		}
	    });

	clearButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    graphElementTF.setText("");
		}
	    });
    }

    void setBackward(boolean value) {
	if (value) {
	    findFirstB.setText("Find last");
	    findNextB.setText("Find prev");
	}
	else {
	    findFirstB.setText("Find first");
	    findNextB.setText("Find next");
	}
    }

    private String updateProps(PropertyElement elem, Vector v) {
	TreeMap properties = elem.getProperties();
	Iterator it = properties.entrySet().iterator();
	String txt = null;
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    //  String propName = prop.getName() + (prop.isID() ? " (ID)" : "");
	    String propName = prop.getName();
	    if (propName.equalsIgnoreCase("type"))
		txt = (String)entry.getValue();
	    else if (prop.isFindable() && !isIn(v, propName)) {
		v.add(propName);
	    }
	}
	return txt;
    }

    public void update(int n) {
	if (canvas == null || canvas[n] == null)
	    return;

	Vector graphElementProps = new Vector();
	LinkedList graphElements = canvas[n].getGraphElements();
	TreeSet graphElementTxt_set = new TreeSet();

	int size = graphElements.size();

	String otype = null;
	for (int d = 0; d < size; d++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(d);
	    String type = VAMPUtils.getType(graphElement);
	    if (otype != null && otype.equals(type))
		continue;
	    otype = type;
	    String s = updateProps(graphElement, graphElementProps);
	    if (s != null)
		graphElementTxt_set.add(s);

	    DataSet dataSet = graphElement.asDataSet();
	    if (dataSet == null)
		continue;
	}

	//System.out.println("graphElementText: " +
	//concat(graphElementTxt_set));
	graphElementLabel.setText(concat(graphElementTxt_set));

	updateCB(graphElementComboB, graphElementProps);
	resetCursors();
    }

    private void setFound(boolean found) {
	if (!found)
	    getToolkit().beep();
	notFoundLabel.setText(found ? " " : "Not found");
	findNextB.setEnabled(found);
    }

    private void resetCursors() {
	graphElementCursor = null;
	findNextB.setEnabled(false);
    }

    private GraphCanvas getSelectedCanvas() {
	if (canvasComboB != null)
	    return canvas[canvasComboB.getSelectedIndex()];
	return canvas[0];
    }

    void setGraphElementText(String text) {
	graphElementTF.setText(text);
    }

    private boolean hasX(PanelProfile panelProfiles[]) {
	return false;
    }
}
