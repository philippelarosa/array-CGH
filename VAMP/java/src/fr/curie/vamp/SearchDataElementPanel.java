
/*
 *
 * SearchDataElementPanel.java
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

class SearchDataElementPanel extends SearchPanel {

    private MLLabel dataElementLabel;
    private JComboBox dataElementComboB;
    private JComboBox canvasComboB;
    private JTextField dataElementTF;
    private JButton findFirstB, findNextB;
    private JButton clearButton;
    private JCheckBox backwardCheckB, pinupInfoB, editPropB, putMarkB;
    private SearchDataElementCursor dataElementCursor;
    private JLabel notFoundLabel;
    private static final int SCAN_PROBE_COUNT = 20;

    SearchDataElementPanel(PanelProfile panelProfiles[]) {
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
	Utils.addPadPanel(this, x_tf+1, y, PADW, PADH, bgColor);

	// third line
	if (hasX(panelProfiles)) {
	    y++;
	    dataElementLabel = new MLLabel("Element");
	    setFGB(dataElementLabel);
	    dataElementLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
	    c = Utils.makeGBC(x_label, y);
	    c.gridwidth = 3;
	    add(dataElementLabel, c);

	    y++;
	    Utils.addPadPanel(this, 2, y, PADW, PADH, bgColor);

	    y++;
	    dataElementComboB = new JComboBox(defProps);
	    setFGB(dataElementComboB, buttonBg);
	    c = Utils.makeGBC(x_combo, y);
	    add(dataElementComboB, c);
	    dataElementTF = new JTextField(8);
	    setFGB(dataElementTF, buttonBg);
	    c = Utils.makeGBC(x_tf, y);
	    c.anchor = GridBagConstraints.WEST;
	    c.weightx = 0.5;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    add(dataElementTF, c);

	    // fourth line
	    y++;
	    Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);
	}

	/*
	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);
	y++;
	*/

	y += addHelp(x_tf, y);
	/*
	MLLabel helpL = new MLLabel();
	helpL.setFont(new Font("SansSerif", Font.PLAIN, 9));
	helpL.setText(HELP_MSG);
	c = Utils.makeGBC(x_tf, y);
	helpL.setBackground(bgColor);
	add(helpL, c);
	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);

	y++;
	*/

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

	pinupInfoB = new JCheckBox("Pin up info");
	setFGB(pinupInfoB);
	y++;
	c = Utils.makeGBC(x_combo, y);
	c.anchor = GridBagConstraints.WEST;
	add(pinupInfoB, c);

	editPropB = new JCheckBox("Edit properties");
	setFGB(editPropB);
	y++;
	c = Utils.makeGBC(x_combo, y);
	c.anchor = GridBagConstraints.WEST;
	add(editPropB, c);

	putMarkB = new JCheckBox("Put landmark");
	setFGB(putMarkB);
	y++;
	c = Utils.makeGBC(x_combo, y);
	c.anchor = GridBagConstraints.WEST;
	add(putMarkB, c);

	y++;
	Utils.addPadPanel(this, 0, y, PADW, 2, bgColor);

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

	if (dataElementComboB != null) {
	    dataElementComboB.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			resetCursors();
		    }
		});
	}

	findFirstB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (dataElementComboB != null) {
			if (dataElementComboB.getSelectedIndex() == VOID_INDEX) {
			    InfoDialog.pop(getSelectedCanvas().getGlobalContext(),
					   CHOOSE_PROP);
			}
			else {
			    dataElementCursor = getSelectedCanvas().searchDataElement
				(null, (String)dataElementComboB.getSelectedItem(),
				 dataElementTF.getText(),
				 backwardCheckB.isSelected(),
				 pinupInfoB.isSelected(),
				 editPropB.isSelected(),
				 putMarkB.isSelected());
			    
			    setFound(dataElementCursor != null);
			}
		    }
		    else
			setFound(false);
		}
	    });

	findNextB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (dataElementComboB != null) {
			if (dataElementComboB.getSelectedIndex() == VOID_INDEX) {
			    InfoDialog.pop(getSelectedCanvas().getGlobalContext(),
					   CHOOSE_PROP);
			}
			else {
			    dataElementCursor = getSelectedCanvas().searchDataElement
				(dataElementCursor, (String)dataElementComboB.getSelectedItem(),
				 dataElementTF.getText(),
				 backwardCheckB.isSelected(),
				 pinupInfoB.isSelected(),
				 editPropB.isSelected(),
				 putMarkB.isSelected());
			    setFound(dataElementCursor != null);
			}
		    }
		    else
			setFound(false);
		}
	    });

	clearButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (dataElementTF != null)
			dataElementTF.setText("");
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
	/*
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
	*/

	Iterator it = elem.getProperties().entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    String propName = prop.getName();
	    if (prop.isFindable() && !isIn(v, propName)) {
		v.add(propName);
	    }
	}

	return VAMPUtils.getType(elem);
    }

    public void update(int n) {
	if (canvas == null || canvas[n] == null)
	    return;
	Vector dataElementProps = new Vector();
	LinkedList graphElements = canvas[n].getGraphElements();
	TreeSet dataElementTxt_set = new TreeSet();

	int size = graphElements.size();

	String otype = null;
	boolean valid = true;
	for (int d = 0; d < size; d++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(d);

	    /*
	    String type = VAMPUtils.getType(graphElement);
	    if (otype != null && otype.equals(type)) {
		continue;
	    }
	    */

	    DataSet dataSet = graphElement.asDataSet();
	    if (dataSet == null) {
		valid = false;
		//continue;
		break;
	    }

	    //otype = type;
	    DataElement elems[] = dataSet.getData();
	    int max;
	    if (true) {
		max = SCAN_PROBE_COUNT;
		if (elems.length < max) {
		    max = elems.length;
		}
	    }
	    else {
		max = elems.length;
	    }

	    for (int e = 0; e < max; e++) {
		String s = updateProps(elems[e], dataElementProps);
		if (s != null) {
		    dataElementTxt_set.add(s);
		}
	    }
	}

	if (dataElementProps.size() == 0) {
	    valid = false;
	}

	dataElementComboB.setEnabled(valid);
	findFirstB.setEnabled(valid);
	clearButton.setEnabled(valid);
	backwardCheckB.setEnabled(valid);
	pinupInfoB.setEnabled(valid);
	editPropB.setEnabled(valid);
	putMarkB.setEnabled(valid);

	if (dataElementLabel != null)
	    dataElementLabel.setText(concat(dataElementTxt_set));

	if (dataElementComboB != null) {
	    updateCB(dataElementComboB, dataElementProps);
	}

	if (canvasComboB != null) {
	    canvasComboB.setSelectedIndex(n); // added 27/05/05
	}

	resetCursors();
    }

    private void setFound(boolean found) {
	if (!found) 
	    getToolkit().beep();
	notFoundLabel.setText(found ? " " : "Not found");
	findNextB.setEnabled(found);
    }

    private void resetCursors() {
	dataElementCursor = null;
	findNextB.setEnabled(false);
    }

    private GraphCanvas getSelectedCanvas() {
	if (canvasComboB != null)
	    return canvas[canvasComboB.getSelectedIndex()];
	return canvas[0];
    }

    void setDataElementText(String text) {
	dataElementTF.setText(text);
    }

    private boolean hasX(PanelProfile panelProfiles[]) {
	for (int n = 0; n < panelProfiles.length; n++)
	    if (panelProfiles[n].supportX())
		return true;
	
	return false;
    }
}
