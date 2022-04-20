
/*
 *
 * ConfirmDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.io.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class ConfirmDialog extends JDialog implements ActionListener {

    static int maxLength = 85;
    static final String OK = "_OK_";
    static final String CANCEL = "_CANCEL_";
    MLLabel label;
    Action action;
    Object actionArg;
    JButton okButton;
    JButton cancelButton;
    static final String CONFIRM_DIALOG = "ConfirmDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(CONFIRM_DIALOG, new ConfirmDialog(new Frame()));
    }

    private JPanel addPadPanel(int x, int y) {
	return addPadPanel(x, y, 0., 0.);
    }

    private JPanel addPadPanel(int x, int y, double wx, double wy) {
	GridBagConstraints c = Utils.makeGBC(x, y);
	c.weightx = wx;
	c.weighty = wy;
	JPanel p = new JPanel();
	p.setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BG));
	getContentPane().add(p, c);
	return p;
    }

    public ConfirmDialog(Frame f) {
	super(f, "CGH", true);

	GridBagConstraints c;

	getContentPane().setLayout(new GridBagLayout());
	label = new MLLabel(maxLength);
	label.setFont(VAMPResources.getFont(VAMPResources.DIALOG_FONT));
	label.setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BG));	
	addLabel();

	addPadPanel(0, 1, 1.0, 0.0);

	okButton = new JButton("OK");
	c = Utils.makeGBC(1, 1);
	c.anchor = GridBagConstraints.CENTER;
	getContentPane().add(okButton, c);
	okButton.setActionCommand(OK);
	okButton.addActionListener(this);

	addPadPanel(2, 1);

	cancelButton = new JButton("Cancel");
	c = Utils.makeGBC(3, 1);
	c.anchor = GridBagConstraints.CENTER;
	getContentPane().add(cancelButton, c);
	cancelButton.setActionCommand(CANCEL);
	cancelButton.addActionListener(this);

	addPadPanel(4, 1, 1.0, 0.0);
	addPadPanel(0, 2);
    
	getContentPane().setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BG));
	setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BG));
    }

    private void addLabel() {
	GridBagConstraints c;
	c = Utils.makeGBC(0, 0);
	c.gridwidth = 5;
	c.weighty = 1.0;
	c.anchor = GridBagConstraints.CENTER;
	c.fill = GridBagConstraints.NONE;
	getContentPane().add(label, c);
    }    

    public static void pop(GlobalContext globalContext,
			   String info, Action action, Object actionArg) {
	pop(globalContext, info, action, actionArg, "OK", "Cancel");
    }

    public static void pop(GlobalContext globalContext,
			   String info, Action action, Object actionArg,
			   String okText, String cancelText) {
	if (globalContext == null) return;

	ConfirmDialog confirmDialog = (ConfirmDialog)globalContext.get(CONFIRM_DIALOG);
	confirmDialog.okButton.setText(okText);
	confirmDialog.cancelButton.setText(cancelText);
	confirmDialog.remove(confirmDialog.label);
	confirmDialog.label.setText(info);
	confirmDialog.addLabel();
	confirmDialog.pack();
	Utils.augment(confirmDialog);
	confirmDialog.action = action;
	confirmDialog.actionArg = actionArg;
	Utils.centerOnScreen(confirmDialog);
    }

    public void actionPerformed(ActionEvent e) {
	JButton b = (JButton)e.getSource();
	String s = b.getActionCommand();
	b.repaint();
	setVisible(false);

	if (s.equals(OK))
	    action.perform(actionArg);
    }
}
