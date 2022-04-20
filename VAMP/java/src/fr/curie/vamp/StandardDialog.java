
/*
 *
 * StandardDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.TreeMap;

public class StandardDialog extends JDialog {

    protected GlobalContext globalContext;
    protected Color BG, buttonFG, buttonBG;
    protected Font buttonFont; 
    protected Font helpFont;
    protected JButton okButton, applyButton, cancelButton;
    protected int lineWidth;
    protected int lineCount;
    protected JPanel mainPanel;
    protected String titleWindow;
    protected TreeMap params;

    protected class TextField {

	JPanel panel;
	JTextField tf;
	TextField(String text, int size) {
	    panel = new JPanel(new BorderLayout());
	    panel.setBackground(BG);
	    tf = new JTextField(text, size);
	    panel.add(tf, BorderLayout.EAST);
	}
	
	String getText() {return tf.getText();}
	void setText(String text) {tf.setText(text);}
	JComponent getComponent() {return panel;}
    }

    public StandardDialog(Frame f, GlobalContext globalContext, String name,
			  boolean modal, int lineWidth, boolean addApply, String title) {
	super(f, title, modal);
	this.globalContext = globalContext;
	this.lineWidth = lineWidth;
	lineCount = 0;
	BG = VAMPResources.getColor(VAMPResources.DIALOG_BG);
	buttonFont = VAMPResources.getFont(VAMPResources.DIALOG_BUTTON_FONT);
	buttonFG = VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_FG);
	buttonBG = VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_BG);
	helpFont = new Font("MonoSpaced", Font.PLAIN, 9);

	makePanels(name, addApply);
    }

    public StandardDialog(Frame f, GlobalContext globalContext, String name,
		   boolean modal, int lineWidth, String title) {
	this(f, globalContext, name, modal, lineWidth, false, title);
    }

    public StandardDialog(Frame f, GlobalContext globalContext, String name,
		   boolean modal, int lineWidth) {
	this(f, globalContext, name, modal, lineWidth, false, name);
    }

    private JPanel makeVPadPanel() {
	JPanel padPanel = new JPanel();
	padPanel.setBackground(BG);
	padPanel.setSize(20, 1);
	return padPanel;
    }

    private void makePanels(String name, boolean addApply) {
	getContentPane().setLayout(new BorderLayout());

	JPanel titlePanel = new JPanel(new FlowLayout());
	titlePanel.add(new JLabel(name));
	titlePanel.setBackground(BG);

	getContentPane().add(titlePanel, BorderLayout.NORTH);

	JPanel buttonPanel = new JPanel(new FlowLayout());
	okButton = makeButton("OK");
	buttonPanel.add(okButton);
	buttonPanel.setBackground(BG);

	if (addApply) {
	    applyButton = makeButton("Apply");
	    buttonPanel.add(applyButton);
	}

	cancelButton = makeButton("Cancel");
	buttonPanel.add(cancelButton);

	getContentPane().add(makeVPadPanel(), BorderLayout.WEST);
	getContentPane().add(makeVPadPanel(), BorderLayout.EAST);

	getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	mainPanel = new JPanel();
	getContentPane().add(mainPanel, BorderLayout.CENTER);

	getContentPane().setBackground(BG);
	mainPanel.setBackground(BG);
    }

    protected Component makePad() {
	return new JLabel();
    }

    protected Component makeSeparator() {
	JPanel outer_p = new JPanel(new GridBagLayout());
	outer_p.setBackground(BG);

	JPanel inner_p = new JPanel();
	inner_p.setBackground(Color.GRAY);
	GridBagConstraints c = Utils.makeGBC(0, 0);
	inner_p.setPreferredSize(new Dimension(1, 1));
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.;

	outer_p.add(inner_p, c);

	return outer_p;
    }

    protected void addPads(int n) {
	while (n-- > 0)
	    mainPanel.add(makePad());
    }

    protected void addSeparators(int n) {
	while (n-- > 0)
	    mainPanel.add(makeSeparator());
    }

    protected JButton makeButton(String name) {
	JButton b = new JButton(name);
	b.setFont(buttonFont);
	b.setForeground(buttonFG);
	b.setBackground(buttonBG);
	return b;
    }

    protected JRadioButton makeRadioButton(String name, ButtonGroup group) {
	return makeRadioButton(name, group, false);
    }

    protected JRadioButton makeRadioButton(String name, ButtonGroup group,
					   boolean selected) {
	JRadioButton rb = new JRadioButton(name);
	group.add(rb);
	//rb.setFont(buttonFont);
	rb.setForeground(buttonFG);
	rb.setBackground(BG);
	rb.setSelected(selected);
	return rb;
    }

    protected JComboBox makeComboBox(String [] items) {
	JComboBox cb = new JComboBox(items);
	cb.setBackground(BG);
	return cb;
    }

    protected JComboBox makeComboBox() {
	JComboBox cb = new JComboBox();
	cb.setBackground(BG);
	return cb;
    }

    protected JLabel makeLabel(String name) {
	JLabel l = new JLabel(name);
	l.setBackground(BG);
	return l;
    }

    protected MLLabel makeMLLabel(String name) {
	MLLabel l = new MLLabel(name);
	l.setBackground(BG);
	return l;
    }

    protected MLLabel makeHelp(String text) {
	MLLabel ml = new MLLabel();
	ml.setBackground(BG);
	ml.setFont(helpFont);
	ml.setText(text);
	return ml;
    }

    protected JLabel makeTitle(String name) {
	JLabel l = new JLabel(name);
	l.setBackground(BG);
	return l;
    }

    protected JCheckBox makeCheckBox(String name, boolean selected) {
	JCheckBox cb = new JCheckBox(name, selected);
	cb.setBackground(BG);
	return cb;
    }

    protected TextField makeTextField(String text, int size) {
	TextField tf = new TextField(text, size);
	return tf;
    }

    protected void addLine(JComponent comp) {
	mainPanel.add(comp);
	addPads(lineWidth-1);
	lineCount++;
    }

    protected void addLine(JComponent comp1, JComponent comp2) {
	mainPanel.add(comp1);
	mainPanel.add(comp2);
	addPads(lineWidth-2);
	lineCount++;
    }

    protected void addLine(JComponent comp1, JComponent comp2,
			   JComponent comp3) {
	mainPanel.add(comp1);
	mainPanel.add(comp2);
	mainPanel.add(comp3);
	addPads(lineWidth-3);
	lineCount++;
    }

    protected void addLine(JComponent comps[]) {
	for (int n = 0; n < comps.length; n++)
	    mainPanel.add(comps[n]);
	addPads(lineWidth-comps.length);
	lineCount++;
    }

    protected void addPadLine() {
	addPads(lineWidth);
	lineCount++;
    }

    protected void addSeparator() {
	addSeparators(lineWidth);
	lineCount++;
    }

    protected void epilogue() {
	mainPanel.setLayout(new GridLayout(lineCount, lineWidth));
	pack();
    }

    protected void pop() {
	params = null;
	Utils.centerOnScreen(this);
    }
}
