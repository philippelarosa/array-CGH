
/*
 *
 * InfoDialog.java
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

public class InfoDialog extends JDialog implements ActionListener {

    MLLabel label;
    //    static int maxLength = 85;
    static int maxLength = 95;
    static final String INFO_DIALOG = "InfoDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(INFO_DIALOG, new InfoDialog(new Frame()));
    }

    public InfoDialog(Frame f) {
	super(f, "CGH", true);

	GridBagConstraints c;

	getContentPane().setLayout(new GridBagLayout());	label = new MLLabel(maxLength);
	label.setFont(VAMPResources.getFont(VAMPResources.DIALOG_FONT));
	label.setBackground(VAMPResources.getColor
			    (VAMPResources.DIALOG_BG));

	addLabel();

	JButton b = new JButton("OK");
	b.setFont(VAMPResources.getFont(VAMPResources.DIALOG_BUTTON_FONT));
	b.setForeground(VAMPResources.getColor
			(VAMPResources.DIALOG_BUTTON_FG));
	b.setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_BG));

	c = Utils.makeGBC(0, 1);
	c.anchor = GridBagConstraints.CENTER;
	c.fill = GridBagConstraints.NONE;
	getContentPane().add(b, c);

	c = Utils.makeGBC(0, 2);
	JPanel p = new JPanel();
	p.setSize(10, 20);
	getContentPane().add(p, c);
	Color bg = VAMPResources.getColor(VAMPResources.DIALOG_BG);
	p.setBackground(bg);

	getContentPane().setBackground(bg);
	setBackground(bg);

	b.addActionListener(this);
	setSize(100, 100);
    }

    private void addLabel() {
	GridBagConstraints c;
	c = Utils.makeGBC(0, 0);
	c.weighty = 1;
	c.anchor = GridBagConstraints.CENTER;
	c.fill = GridBagConstraints.NONE;
	c.ipady = 20;
	getContentPane().add(label, c);
  
    }    

    public static void pop(GlobalContext globalContext, String info,
			   Exception e) {
	pop(globalContext, info + ":\n" + e.toString());
    }

    public static void pop(GlobalContext globalContext, String info) {
	System.err.println(info);
	if (globalContext == null) return;

	InfoDialog infoDialog = (InfoDialog)globalContext.get(INFO_DIALOG);
	infoDialog.remove(infoDialog.label);
	infoDialog.label.setText(info);
	infoDialog.addLabel();
	infoDialog.setSize(100, 100);
	infoDialog.pack();
	    
	Utils.augment(infoDialog);
	Utils.centerOnScreen(infoDialog);
    }

    public void actionPerformed(ActionEvent e) {
	setVisible(false);
    }
}
