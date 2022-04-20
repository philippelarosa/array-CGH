
/*
 *
 * ThresholdsPanel.java
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
import java.io.*;

class ThresholdsPanel extends JPanel {

    private View view;
    private JComboBox modeCB;
    private JButton apply_b, restore_b;

    private Thresholds lastthresholds = null;

    private JTextField minTF, maxTF;
    private Hashtable tf_ht = new Hashtable();

    private Color bgColor =
	VAMPResources.getColor(VAMPResources.THRESHOLD_PANEL_BG);
    private Color buttonBg =
	VAMPResources.getColor(VAMPResources.THRESHOLD_PANEL_BUTTON_BG);
    private Font labelFont =
	VAMPResources.getFont(VAMPResources.THRESHOLD_PANEL_LABEL_FONT);
    private Font comboFont =
	VAMPResources.getFont(VAMPResources.SEARCH_PANEL_BUTTON_FONT);

    private JTextField makeTF() {
	JTextField tf = new JTextField(6);
	tf.setFont(labelFont);

	tf.addKeyListener(new KeyAdapter() {
		public void keyReleased(KeyEvent e) {
		    sync((JTextField)e.getSource());
		}
	    });

	tf.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JTextField tf = (JTextField)e.getSource();
		    sync(tf);
		}
	    });

	return tf;
    }

    private boolean remove = false;

    private JPanel makePanel() {
	JPanel panel = new JPanel();
	panel.setBackground(bgColor);
	return panel;
    }

    private JButton makeButton(String name) {
	JButton b = new JButton(name);
	b.setFont(labelFont);
	b.setBackground(buttonBg);
	return b;
    }

    private final int GLOBAL_MODE = 0;
    private final int LOCAL_MODE = 1;

    ThresholdsPanel(View _view) {
	this.view = _view;

	setBackground(bgColor);
	setLayout(new BorderLayout());

	JPanel topPanel = makePanel();
	modeCB = new JComboBox(new String[]{"Global", "Local"});

	modeCB.setFont(comboFont);
	modeCB.setBackground(Color.WHITE);

	modeCB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (modeCB.getSelectedIndex() == GLOBAL_MODE) {
			remove = false;
			globalSync();
			restore_b.setText("Restore Default");
		    }
		    else if (modeCB.getSelectedIndex() == LOCAL_MODE) {
			localSync();
		    }
		}
	    });

	topPanel.add(modeCB);

	JPanel centerPanel = makePanel();
	centerPanel.setLayout(new GridLayout(2, 1));

	JPanel maxPanel = makePanel();
	JLabel max_l = new JLabel("Max");
	max_l.setFont(labelFont);
	maxPanel.add(max_l);
	maxTF = makeTF();
	maxPanel.add(maxTF);

	JPanel minPanel = makePanel();
	JLabel min_l = new JLabel("Min");
	min_l.setFont(labelFont);
	minPanel.add(min_l);
	minTF = makeTF();
	minPanel.add(minTF);

	centerPanel.add(maxPanel);
	centerPanel.add(minPanel);

	JPanel buttonPanel = makePanel();
	apply_b = makeButton("Apply");
	apply_b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (modeCB.getSelectedIndex() == LOCAL_MODE) 
			localModeApply();
		    else if (modeCB.getSelectedIndex() == GLOBAL_MODE)
			globalModeApply();
		}
	    });

	restore_b = makeButton("Restore Default");

	restore_b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (remove) {
			removeLocal();
		    }
		    else if (modeCB.getSelectedIndex() == LOCAL_MODE) {
			setGlobal();
		    }
		    else if (modeCB.getSelectedIndex() == GLOBAL_MODE) {
			restoreDefault();
		    }
		}
	    });

	buttonPanel.add(apply_b);
	buttonPanel.add(restore_b);
	
	JPanel northPanel = makePanel();
	northPanel.setLayout(new BorderLayout());
	JPanel padPanel = makePanel();
	padPanel.setPreferredSize(new Dimension(1, 20));
	northPanel.add(padPanel, BorderLayout.NORTH);
	northPanel.add(topPanel, BorderLayout.CENTER);

	JPanel southPanel = makePanel();
	southPanel.setLayout(new BorderLayout());
	southPanel.add(buttonPanel, BorderLayout.CENTER);
	padPanel = makePanel();
	padPanel.setPreferredSize(new Dimension(1, 20));
	southPanel.add(padPanel, BorderLayout.SOUTH);
	
	add(northPanel, BorderLayout.NORTH);
	add(centerPanel, BorderLayout.CENTER);
	add(southPanel, BorderLayout.SOUTH);
	globalSync();
    }

    void sync() {
	if (getLocalThresholds() != null)
	    modeCB.setSelectedIndex(LOCAL_MODE);
	else
	    modeCB.setSelectedIndex(GLOBAL_MODE);
    }

    // tf_ht management
    private static final int MAX_TF_SIZE = 5;

    private Double getDouble(JTextField tf) {
	return (Double)tf_ht.get(tf);
    }

    private Double getDouble(Thresholds thresholds, JTextField tf) {
	Double d = getDouble(tf);
	/*
	return thresholds.isLog() ?
	    new Double(Utils.pow(d.doubleValue())) : d;
	*/
	return d;
    }

    private void empty(JTextField tf) {
	tf_ht.put(tf, new Double(-1));
	tf.setText("");
    }

    private void sync(JTextField tf) {

	try {
	    Double d = new Double(tf.getText());
	    tf_ht.put(tf, d);
	}
	catch(NumberFormatException e) {
	}
    }

    private String toString(double d) {
	String s = Utils.toString(d);
	int idx = s.lastIndexOf('.');
	if (idx < s.length()-4)
	    return s.substring(0, idx+4);
	return s;
    }

    private void sync(JTextField tf, double d) {
	tf_ht.put(tf, new Double(d));
	String s = toString(d);
	tf.setText(s.substring(0, s.length() > MAX_TF_SIZE ?
			       MAX_TF_SIZE : s.length()));
    }

    // sync management

    private void localSync() {
	if (modeCB.getSelectedIndex() != LOCAL_MODE) return;

	Thresholds thresholds = getLocalThresholds();
	sync(thresholds);

	if (thresholds != null) {
	    remove = true;
	    restore_b.setText("Remove Local");
	}
	else {
	    remove = false;
	    restore_b.setText("Set Global");
	}
    }

    private void globalSync() {
	if (modeCB.getSelectedIndex() != GLOBAL_MODE) return;
	Thresholds thresholds = getThresholds();
	sync(thresholds);
    }

    void sync(Thresholds thresholds) {
	if (lastthresholds == thresholds) return;
	lastthresholds = thresholds;

	int idx = modeCB.getSelectedIndex();
	if (thresholds == null) {
	    modeCB.setSelectedIndex(idx);
	    empty(minTF);
	    empty(maxTF);
	    return;
	}

	modeCB.removeAllItems();
	modeCB.addItem("Global " + thresholds.getName());
	modeCB.addItem("Local");
	modeCB.setSelectedIndex(idx);

	sync(maxTF, thresholds.getMax());
	sync(minTF, thresholds.getMin());
    }

    private void localModeApply() {
	Thresholds thresholds = getThresholds();
	if (thresholds == null) return;

	double min = getDouble(minTF).doubleValue();
	double max = getDouble(maxTF).doubleValue();

	Thresholds thrLog, thrLin;
	if (thresholds.isLog()) {
	    thrLog = new Thresholds(false, "", true, min, max);
	    thrLin = new Thresholds(false, "", false,
				    Utils.pow(min), Utils.pow(max));
	}
	else {
	    thrLog = new Thresholds(false, "", true,
				    Utils.log(min), Utils.log(max));
	    thrLin = new Thresholds(false, "", false,
				    min, max);
	}

	Vector graphElements =
	    view.getGraphPanelSet().getSelectedGraphElements(View.ALL);

	int size = graphElements.size();
	for (int n = 0; n < size; n++)
	    VAMPUtils.setLocalThresholds((GraphElement)graphElements.get(n), thrLog, thrLin);

	localSync();

	syncViews(view);
    }

    private void globalModeApply() {
	Thresholds thresholds = getThresholds();
	if (thresholds == null) {
	    return;
	}

	Resources resources = VAMPResources.resources;

	String resPrefix = VAMPResources.getResPrefix(thresholds);

	if (thresholds.isLog()) {
	    resources.add(resPrefix + VAMPResources.THRESHOLD_LOG_MINY,
			  getDouble(thresholds, minTF));

	    resources.add(resPrefix + VAMPResources.THRESHOLD_LOG_MAXY,
			  getDouble(thresholds, maxTF));
	}
	else {
	    resources.add(resPrefix + VAMPResources.THRESHOLD_MINY,
			  getDouble(thresholds, minTF));

	    resources.add(resPrefix + VAMPResources.THRESHOLD_MAXY,
			  getDouble(thresholds, maxTF));
	}

	Thresholds.init(view.getGlobalContext());

	syncViews(view);
    }

    private void setGlobal() {
	sync(getThresholds());
    }

    private void removeLocal() {
	Vector graphElements =
	    view.getGraphPanelSet().getSelectedGraphElements(View.ALL);
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    VAMPUtils.removeLocalThresholds((GraphElement)graphElements.get(n));
	}

	sync();
	syncViews(view);
	//view.repaint();
    }

    private Thresholds getThresholds() {
	Vector graphElements = view.getSelectedGraphElements(View.ALL);

	int size = graphElements.size();
	if (size == 0) return null;

	Thresholds lastthresholds = null;

	for (int n = 0; n < size; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);
	    Thresholds thresholds = VAMPUtils.getThresholds(graphElement);
	    if (thresholds == null || thresholds.isHidden()) return null;

	    if (lastthresholds != null && lastthresholds != thresholds)
		return null;

	    lastthresholds = thresholds;
	}

	return lastthresholds;
    }

    private Thresholds getLocalThresholds() {
	Vector graphElements = view.getSelectedGraphElements(View.ALL);

	Thresholds thresholds = getThresholds();
	if (thresholds == null) return null;
	Property thrProp;
	if (thresholds.isLog())
	    thrProp = VAMPProperties.ThresholdsLogProp;
	else
	    thrProp = VAMPProperties.ThresholdsLinProp;
	    
	int size = graphElements.size();
	if (size > 0) {
	    thresholds = (Thresholds)
		((GraphElement)graphElements.get(0)).getPropertyValue(thrProp);
	    if (thresholds != null) {
		for (int n = 1; n < size; n++) {
		    Thresholds ds_thr = (Thresholds)
			((GraphElement)graphElements.get(n)).getPropertyValue(thrProp);
		    if (ds_thr == null || !thresholds.equals(ds_thr)) {
			thresholds = null;
			break;
		    }
		}
	    }
	}
	else
	    thresholds = null;

	return thresholds;
    }


    private void restoreDefault() {
	Thresholds thresholds = getThresholds();
	if (thresholds == null) {
	    return;
	}

	String resPrefix = VAMPResources.getResPrefix(thresholds);
	Resources resources = VAMPResources.resources;

	boolean r;
	if (thresholds.isLog()) {
	    r = setDefaultValue(resources, resPrefix + VAMPResources.THRESHOLD_LOG_MINY) && setDefaultValue(resources, resPrefix + VAMPResources.THRESHOLD_LOG_MAXY);
		
	}
	else {
	    r = setDefaultValue(resources, resPrefix + VAMPResources.THRESHOLD_MINY) &&	setDefaultValue(resources, resPrefix + VAMPResources.THRESHOLD_MAXY);
	    
	}

	if (r) {
	    Thresholds.init(view.getGlobalContext());
	    syncViews(view);
	}
    }

    static boolean setDefaultValue(Resources resources, String name) {
	if (VAMPResources.getResourceItem(name) == null) {
	    return false;
	}

	resources.add(name,
		      VAMPResources.getResourceItem(name).getDefaultValue());
	return true;
    }

    private void syncViews(View view) {
	View.syncAllGraphElements(view.getGlobalContext(), true);
	View.syncAll(view.getGlobalContext(), true);
    }
}

