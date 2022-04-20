
/*
 *
 * ColorLegendPanel.java
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

class ColorLegendPanel extends JPanel {

    static final String SEL_MSG = "One or more profiles must be selected";

    class LC {
	private JLabel label;
	private JComponent comp;

	LC(JLabel label, JComponent comp) {
	    this.label = label;
	    this.comp = comp;
	}

	void setVisible(boolean visible) {
	    label.setVisible(visible);
	    comp.setVisible(visible);
	}

	void setText(String s) {
	    label.setText("  " + s.trim() + "  ");
	}

	JTextField tf() {return (JTextField)comp;}
	JButton b() {return (JButton)comp;}
    }

    private final int GLOBAL_MODE = 0;
    private final int LOCAL_MODE = 1;
    private boolean remove = false;
    private ColorCodes lastcc = null;

    private Hashtable tf_ht = new Hashtable();
    private LC amplicon_lc, max_lc, normal_max_lc, normal_min_lc, min_lc;
    private LC max_color_b, normal_color_b, min_color_b, amplicon_color_b,
	homoz_color_b;
    private JButton apply_b, restore_b;
    private JCheckBox mode_cb;
    private JLabel mode_label;
    private Color bgColor =
	VAMPResources.getColor(VAMPResources.COLOR_LEGEND_PANEL_BG);
    private View view;
    private Color buttonBg =
	VAMPResources.getColor(VAMPResources.COLOR_LEGEND_PANEL_BUTTON_BG);
    private Font labelFont =
	VAMPResources.getFont(VAMPResources.COLOR_LEGEND_PANEL_LABEL_FONT);

    private JLabel title;
    private JComboBox modeCB;
    boolean selected;

    ColorLegendPanel(View _view) {
	this.view = _view;
	setBackground(bgColor);
	setLayout(new GridBagLayout());

	GridBagConstraints c;
	final int x0 = 0, y0 = 5;
	int x, y;

	Font font = VAMPResources.getFont(VAMPResources.SEARCH_PANEL_BUTTON_FONT);
	x = 0;
	y = 0;
	Utils.addPadPanel(this, x, y, 1, 3, bgColor);

	title = new JLabel();
	title.setFont(font);
	y++;
	c = Utils.makeGBC(0, y);
	c.gridwidth = 6;
	c.anchor = GridBagConstraints.CENTER;
	add(title, c);

	y++;
	Utils.addPadPanel(this, x, y, 1, 3, bgColor);

	modeCB = new JComboBox(new String[]{"Global", "Local"});
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

	y++;
	modeCB.setFont(font);
	modeCB.setBackground(Color.WHITE);
	c = Utils.makeGBC(0, y);
	c.gridwidth = 6;
	c.anchor = GridBagConstraints.CENTER;
	add(modeCB, c);

	y++;

	Utils.addPadPanel(this, x, y, 1, 2, bgColor);

	y = y0;

	amplicon_lc = makeLTF("Amplicon", x0, y);
	y++;

	max_lc = makeLTF("Gain", x0, y);
	y++;

	normal_max_lc = makeLTF("Normal Max", x0, y);
	y++;

	normal_min_lc = makeLTF("Normal Min", x0, y);
	y++;

	min_lc = makeLTF("Loss", x0, y);

	int x1 = x0+3;

	y = y0;

	x = x1;

	enabled(false);

	amplicon_color_b = makeLB("Amplicon Color", x1, y);
	amplicon_color_b.b().setBackground(Color.BLACK);
	x++;
	c = Utils.makeGBC(x, y);
	add(makeLabel(""), c);
	y++;

	max_color_b = makeLB("Gain Color", x1, y);
	max_color_b.b().setBackground(Color.BLACK);
	y++;

	normal_color_b = makeLB("Normal Color", x1, y); // ou Heteroz
	normal_color_b.b().setBackground(Color.BLACK);
	y++;

	min_color_b = makeLB("Loss Color", x1, y);
	min_color_b.b().setBackground(Color.BLACK);
	y++;

	homoz_color_b = makeLB("HomoZ Color", x1, y);
	homoz_color_b.b().setBackground(Color.BLACK);
	homoz_color_b.setVisible(false);

	x = x1;
	c = Utils.makeGBC(x, y);
	c.gridwidth = 2;
	c.anchor = GridBagConstraints.EAST;
	mode_label = makeLabel("Continuous");
	add(mode_label, c);

	mode_cb = new JCheckBox();
	mode_cb.setSelected(false);
	mode_cb.setBackground(bgColor);
	x += 2;
	c = Utils.makeGBC(x, y);
	add(mode_cb, c);

	// right margin
	Utils.addPadPanel(this, x1+4, 0, 1, 1, bgColor);

	y++;
	Utils.addPadPanel(this, x0, y, 1, 5, bgColor);
	y++;
	x = 1;
	apply_b = new JButton("Apply");
	apply_b.setFont(labelFont);
	apply_b.setBackground(buttonBg);
	apply_b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (modeCB.getSelectedIndex() == LOCAL_MODE) 
			localModeApply();
		    else if (modeCB.getSelectedIndex() == GLOBAL_MODE)
			globalModeApply();
		}
	    });

	c = Utils.makeGBC(x, y);
	add(apply_b, c);

	x = x1;
	restore_b = new JButton("Restore Default");
	restore_b.setFont(labelFont);
	restore_b.setBackground(buttonBg);
	restore_b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (remove)
			removeLocal();
		    else if (modeCB.getSelectedIndex() == LOCAL_MODE)
			setGlobal();
		    else if (modeCB.getSelectedIndex() == GLOBAL_MODE)
			restoreDefault();
		}
	    });

	c = Utils.makeGBC(x, y);
	c.gridwidth = 3;
	add(restore_b, c);
	globalSync();
	enabled(false);
   }

    private JLabel makeLabel(String s) {
	JLabel l = new JLabel("  " + s.trim() + "  ");
	l.setFont(labelFont);
	return l;
    }

    private LC makeLTF(String s, int x, int y) {
	GridBagConstraints c = Utils.makeGBC(x, y);
	c.gridwidth = 2;
	c.ipady = 8;
	c.anchor = GridBagConstraints.EAST;
	JLabel label = makeLabel(s);
	add(label, c);
	JTextField tf = new JTextField(1);
	
	tf.setFont(labelFont);
	x += 2;
	c = Utils.makeGBC(x, y);
	c.weightx = 1.;
	c.fill = GridBagConstraints.HORIZONTAL;
	add(tf, c);

	tf.addKeyListener(new KeyAdapter() {
		public void keyReleased(KeyEvent e) {
		    if (!selected) {
			InfoDialog.pop(view.getGlobalContext(), SEL_MSG);
			((JTextField)e.getSource()).setText("");
		    }
		    else
			sync((JTextField)e.getSource());
		}
	    });

	tf.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JTextField tf = (JTextField)e.getSource();
		    sync(tf);
		}
	    });

	return new LC(label, tf);
    }

    private LC makeLB(String s, int x, int y) {
	GridBagConstraints c = Utils.makeGBC(x, y);
	c.ipady = 8;
	c.gridwidth = 2;
	c.anchor = GridBagConstraints.EAST;
	JLabel label = makeLabel(s);
	add(label, c);
	JButton b = new JButton();
	b.setPreferredSize(new Dimension(12, 12));
	x += 2;
	c = Utils.makeGBC(x, y);
	c.fill = GridBagConstraints.NONE;
	add(b, c);
				 
	b.addActionListener(new ActionListenerWrapper(s) {
		public void actionPerformed(ActionEvent e) {
		    if (!selected)
			InfoDialog.pop(view.getGlobalContext(), SEL_MSG);
		    else {
			JButton b = (JButton)e.getSource();
			Color color = JColorChooser.showDialog(view,
							       (String)getValue(),
							       b.getBackground());
			if (color != null)
			    b.setBackground(color);
		    }
		}
	    });

	return new LC(label, b);
    }

    private ColorCodes getColorCodes() {
	Vector graphElements = view.getSelectedGraphElements(View.ALL);

	int size = graphElements.size();
	if (size == 0) return null;

	ColorCodes lastcc = null;

	for (int n = 0; n < size; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);
	    ColorCodes cc = VAMPUtils.getGlobalColorCodes(graphElement);
	    if (cc == null) return null;

	    if (lastcc != null && lastcc != cc)
		return null;

	    lastcc = cc;
	}

	return lastcc;
    }

    private ColorCodes getLocalCC() {
	Vector graphElements = view.getSelectedGraphElements(View.ALL);

	ColorCodes cc = getColorCodes();
	if (cc == null) return null;
	Property ccProp;
	if (cc instanceof StandardColorCodes) {
	    if (((StandardColorCodes)cc).isLog)
		ccProp = VAMPProperties.CCLogProp;
	    else
		ccProp = VAMPProperties.CCLinProp;
	}
	else
	    ccProp = VAMPProperties.CCLinProp;
	    
	int size = graphElements.size();
	if (size > 0) {
	    cc = (ColorCodes)
		((GraphElement)graphElements.get(0)).getPropertyValue(ccProp);
	    if (cc != null) {
		for (int n = 1; n < size; n++) {
		    ColorCodes ds_cc = (ColorCodes)
			((GraphElement)graphElements.get(n)).getPropertyValue(ccProp);
		    if (ds_cc == null || !cc.equals(ds_cc)) {
			cc = null;
			break;
		    }
		}
	    }
	}
	else
	    cc = null;

	return cc;
    }

    public void sync() {
	if (getLocalCC() != null)
	    modeCB.setSelectedIndex(LOCAL_MODE);
	else
	    modeCB.setSelectedIndex(GLOBAL_MODE);
    }

    private void localSync() {
	if (modeCB.getSelectedIndex() != LOCAL_MODE) return;

	ColorCodes cc = getLocalCC();
	sync(cc);

	if (cc != null) {
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
	ColorCodes cc = getColorCodes();
	sync(cc);
    }

    void sync(ColorCodes cc) {
	if (lastcc == cc)
	    return;

	lastcc = cc;
	int idx = modeCB.getSelectedIndex();
	if (cc == null) {
	    enabled(false);
	    modeCB.setSelectedIndex(idx);
	    empty(amplicon_lc.tf());
	    empty(max_lc.tf());
	    empty(normal_max_lc.tf());
	    empty(normal_min_lc.tf());
	    empty(min_lc.tf());
	
	    amplicon_color_b.b().setBackground(Color.BLACK);
	    max_color_b.b().setBackground(Color.BLACK);
	    normal_color_b.b().setBackground(Color.BLACK);
	    min_color_b.b().setBackground(Color.BLACK);
	    homoz_color_b.b().setBackground(Color.BLACK);
	    mode_cb.setSelected(false);
	    return;
	}

	modeCB.removeAllItems();
	title.setText(cc.getName());
	enabled(true);

	modeCB.addItem("Global");
	modeCB.addItem("Local");
	modeCB.setSelectedIndex(idx);

	if (cc instanceof LOHColorCodes) {
	    LOHColorCodes lcc = (LOHColorCodes)cc;
	    amplicon_lc.setVisible(false);
	    amplicon_color_b.setVisible(false);

	    sync(max_lc.tf(), lcc.getMax());
	    sync(normal_min_lc.tf(), lcc.getMin());
	    sync(normal_max_lc.tf(), lcc.getLOH());
	
	    max_color_b.b().setBackground(new Color(lcc.getMaxRGB()));
	    normal_color_b.b().setBackground(new Color(lcc.getLOHRGB()));
	    min_color_b.b().setBackground(new Color(lcc.getMinRGB()));

	    mode_cb.setSelected(false);
	    normal_max_lc.setText("LOH");
	    normal_max_lc.setVisible(true);
	    min_lc.setVisible(false);

	    max_lc.setText("Max");
	    normal_min_lc.setText("Min");

	    max_color_b.setText("Max Color");
	    normal_color_b.setText("LOH Color");
	    min_color_b.setText("Min Color");

	    mode_cb.setVisible(false);
	    mode_label.setVisible(false);
	    //	    homoz_color_b.setVisible(true);
	    homoz_color_b.setVisible(false);
	}
	else if (cc instanceof StandardColorCodes) {
	    StandardColorCodes scc = (StandardColorCodes)cc;
	    sync(amplicon_lc.tf(), scc.getAmplicon());
	    sync(max_lc.tf(), scc.getMax());
	    sync(normal_max_lc.tf(), scc.getNormalMax());
	    sync(normal_min_lc.tf(), scc.getNormalMin());
	    mode_cb.setSelected(scc.isContinuous());

	    amplicon_lc.setVisible(true);
	    amplicon_color_b.setVisible(true);
	    
	    normal_max_lc.setVisible(true);
	    min_lc.setVisible(true);

	    amplicon_lc.setText("Amplicon");
	    max_lc.setText("Gain");
	    normal_max_lc.setText("Normal Max");
	    normal_min_lc.setText("Normal Min");
	    min_lc.setText("Loss");

	    sync(min_lc.tf(), scc.getMin());
	
	    amplicon_color_b.setText("Amplicon Color");
	    max_color_b.setText("Gain Color");
	    normal_color_b.setText("Normal Color");
	    min_color_b.setText("Loss Color");

	    amplicon_color_b.b().setBackground(new Color(scc.getAmpliconRGB()));
	    max_color_b.b().setBackground(new Color(scc.getMaxRGB()));
	    normal_color_b.b().setBackground(new Color(scc.getNormalRGB()));
	    min_color_b.b().setBackground(new Color(scc.getMinRGB()));

	    mode_cb.setVisible(true);
	    mode_label.setVisible(true);
	    homoz_color_b.setVisible(false);
	}
    }

    private String toString(double d) {
	String s = Utils.toString(d);
	int idx = s.lastIndexOf('.');
	if (idx < s.length()-4)
	    return s.substring(0, idx+4);
	return s;
    }

    static void setDefaultValue(Resources resources, String name) {
	resources.add(name,
		      VAMPResources.getResourceItem(name).getDefaultValue());

    }

    private Double getDouble(JTextField tf) {
	return (Double)tf_ht.get(tf);
    }

    private Double getDouble(ColorCodes cc, JTextField tf) {
	Double d = getDouble(tf);
	return cc instanceof StandardColorCodes &&
	    ((StandardColorCodes)cc).isLog() ? new Double(Utils.pow(d.doubleValue())) : d;
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

    private static final int MAX_TF_SIZE = 5;

    private void sync(JTextField tf, double d) {
	tf_ht.put(tf, new Double(d));
	String s = toString(d);
	tf.setText(s.substring(0, s.length() > MAX_TF_SIZE ?
			       MAX_TF_SIZE : s.length()));
    }

    private void localModeApply() {
	ColorCodes cc = getColorCodes();
	if (cc == null) return;

	ColorCodes ccLog = null, ccLin = null;
	if (cc instanceof LOHColorCodes) {
	    ccLog = new LOHColorCodes
		(view.getGlobalContext(),
		 "",
		 "",
		 getDouble(normal_min_lc.tf()).doubleValue(),
		 getDouble(normal_max_lc.tf()).doubleValue(),
		 getDouble(max_lc.tf()).doubleValue(),
		 VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
		 min_color_b.b().getBackground(),
		 normal_color_b.b().getBackground(),
		 max_color_b.b().getBackground());
	    ccLin = ccLog;
	}
	else if (cc instanceof StandardColorCodes) {
	    StandardColorCodes scc = (StandardColorCodes)cc;
	    if (scc.isLog()) {
		ccLog = new StandardColorCodes
		    (view.getGlobalContext(),
		     true,
		     "",
		     "",
		     getDouble(min_lc.tf()).doubleValue(),
		     getDouble(normal_min_lc.tf()).doubleValue(),
		     getDouble(normal_max_lc.tf()).doubleValue(),
		     getDouble(max_lc.tf()).doubleValue(),
		     getDouble(amplicon_lc.tf()).doubleValue(),
		     VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
		     min_color_b.b().getBackground(),
		     normal_color_b.b().getBackground(),
		     max_color_b.b().getBackground(),
		     amplicon_color_b.b().getBackground(),
		     mode_cb.isSelected());

		ccLin = new StandardColorCodes
		    (view.getGlobalContext(),
		     false,
		     "",
		     "",
		     Utils.pow(getDouble(min_lc.tf()).doubleValue()),
		     Utils.pow(getDouble(normal_min_lc.tf()).doubleValue()),
		     Utils.pow(getDouble(normal_max_lc.tf()).doubleValue()),
		     Utils.pow(getDouble(max_lc.tf()).doubleValue()),
		     Utils.pow(getDouble(amplicon_lc.tf()).doubleValue()),
		     VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
		     min_color_b.b().getBackground(),
		     normal_color_b.b().getBackground(),
		     max_color_b.b().getBackground(),
		     amplicon_color_b.b().getBackground(),
		     mode_cb.isSelected());
	    }
	    else {
		ccLog = new StandardColorCodes
		    (view.getGlobalContext(),
		     true,
		     "",
		     "",
		     Utils.log(getDouble(min_lc.tf()).doubleValue()),
		     Utils.log(getDouble(normal_min_lc.tf()).doubleValue()),
		     Utils.log(getDouble(normal_max_lc.tf()).doubleValue()),
		     Utils.log(getDouble(max_lc.tf()).doubleValue()),
		     Utils.log(getDouble(amplicon_lc.tf()).doubleValue()),
		     VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
		     min_color_b.b().getBackground(),
		     normal_color_b.b().getBackground(),
		     max_color_b.b().getBackground(),
		     amplicon_color_b.b().getBackground(),
		     mode_cb.isSelected());

		ccLin = new StandardColorCodes
		    (view.getGlobalContext(),
		     false,
		     "",
		     "",
		     getDouble(min_lc.tf()).doubleValue(),
		     getDouble(normal_min_lc.tf()).doubleValue(),
		     getDouble(normal_max_lc.tf()).doubleValue(),
		     getDouble(max_lc.tf()).doubleValue(),
		     getDouble(amplicon_lc.tf()).doubleValue(),
		     VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
		     min_color_b.b().getBackground(),
		     normal_color_b.b().getBackground(),
		     max_color_b.b().getBackground(),
		     amplicon_color_b.b().getBackground(),
		     mode_cb.isSelected());

	    }
	}

	Vector graphElements =
	    view.getGraphPanelSet().getSelectedGraphElements(View.ALL);
	int size = graphElements.size();
	for (int n = 0; n < size; n++)
	    VAMPUtils.setLocalColorCodes((GraphElement)graphElements.get(n), ccLog, ccLin);

	localSync();

	syncViews(view);
    }

    private void globalModeApply() {
	ColorCodes cc = getColorCodes();
	if (cc == null) return;
	Resources resources = VAMPResources.resources;

	String resPrefix = VAMPResources.getResPrefix(cc);

	if (cc instanceof LOHColorCodes) {
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_YMIN,
			  getDouble(cc, normal_min_lc.tf()));
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_LOH,
			  getDouble(cc, normal_max_lc.tf()));
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_YMAX,
			  getDouble(cc, max_lc.tf()));

	    resources.add(resPrefix + VAMPResources.COLOR_CODE_MIN_FG,
			  min_color_b.b().getBackground());
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_LOH_FG,
			  normal_color_b.b().getBackground());
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_MAX_FG,
			  max_color_b.b().getBackground());
	}
	else if (cc instanceof StandardColorCodes) {
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_YMIN,
			  getDouble(cc, min_lc.tf()));

	    resources.add(resPrefix + VAMPResources.COLOR_CODE_YNORMAL_MIN,
			  getDouble(cc, normal_min_lc.tf()));

	    resources.add(resPrefix + VAMPResources.COLOR_CODE_YNORMAL_MAX,
			  getDouble(cc, normal_max_lc.tf()));

	    resources.add(resPrefix + VAMPResources.COLOR_CODE_YMAX,
			  getDouble(cc, max_lc.tf()));
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_AMPLICON,
			  getDouble(cc, amplicon_lc.tf()));

	    resources.add(resPrefix + VAMPResources.COLOR_CODE_MIN_FG,
			  min_color_b.b().getBackground());
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_NORMAL_FG,
			  normal_color_b.b().getBackground());
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_MAX_FG,
			  max_color_b.b().getBackground());
	    resources.add(resPrefix + VAMPResources.COLOR_CODE_AMPLICON_FG,
			  amplicon_color_b.b().getBackground());

	    resources.add(resPrefix + VAMPResources.COLOR_CODE_CONTINUOUS_MODE,
			  new Boolean(mode_cb.isSelected()));

	}
	ColorCodes.init(view.getGlobalContext());

	syncViews(view);
    }

    private void setGlobal() {
	sync(getColorCodes());
    }

    private void restoreDefault() {
	ColorCodes cc = getColorCodes();
	if (cc == null) return;

	String resPrefix = VAMPResources.getResPrefix(cc);
	Resources resources = VAMPResources.resources;
	if (cc instanceof LOHColorCodes) {
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_YMIN);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_LOH);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_YMAX);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_MIN_FG);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_LOH_FG);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_MAX_FG);
	}
	else if (cc instanceof StandardColorCodes) {
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_YMIN);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_YNORMAL_MIN);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_YNORMAL_MAX);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_YMAX);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_AMPLICON);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_MIN_FG);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_NORMAL_FG);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_MAX_FG);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_AMPLICON_FG);
	    setDefaultValue(resources, resPrefix + VAMPResources.COLOR_CODE_CONTINUOUS_MODE);
	}
	ColorCodes.init(view.getGlobalContext());

	syncViews(view);
    }

    private void removeLocal() {
	Vector graphElements =
	    view.getGraphPanelSet().getSelectedGraphElements(View.ALL);
	int size = graphElements.size();
	for (int n = 0; n < size; n++)
	    VAMPUtils.removeLocalColorCodes((GraphElement)graphElements.get(n));

	sync();
	view.repaint();
    }

    private void syncViews(View view) {
	view.syncGraphElements(false);
	View.syncAll(view.getGlobalContext(), true);
    }

    private void enabled(boolean enabled) {
	selected = enabled;
	if (!enabled)
	    title.setText(SEL_MSG);
	/*
	if (apply_b != null && restore_b != null) {
	    apply_b.setEnabled(enabled);
	    restore_b.setEnabled(enabled);
	}
	*/
    }
}
