
/*
 *
 * PrintTextEditor.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;

class PrintTextEditor extends JFrame {

    JTextArea textEditor;
    //JEditorPane textEditor;
    PrintTextComponentArea textComponent;
    PrintPreviewerPanel printPreviewerPanel;

    PrintTextEditor(PrintPreviewerPanel _printPreviewerPanel) {
	super("Print Text Editor");
	this.printPreviewerPanel = _printPreviewerPanel;

	textEditor = new JTextArea();
	JPanel mainPanel = new JPanel(new BorderLayout());

	JScrollPane pane = new JScrollPane(textEditor);
	mainPanel.add(pane, BorderLayout.CENTER);
	JPanel buttonPanel = new JPanel(new FlowLayout());
	JButton b = new JButton("OK");
	b.setFont(VAMPResources.getFont(VAMPResources.INFO_PANEL_TEXT_FONT));
	b.setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_BG));
	b.setForeground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_FG));

	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    textComponent.setTemplate(textEditor.getText());
		    setVisible(false);
		    printPreviewerPanel.repaint();
		}
	    });

	buttonPanel.add(b);
	b = new JButton("Cancel");
	b.setFont(VAMPResources.getFont(VAMPResources.INFO_PANEL_TEXT_FONT));
	b.setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_BG));
	b.setForeground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_FG));

	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });

	buttonPanel.add(b);
	buttonPanel.setBackground(Color.LIGHT_GRAY);
	mainPanel.add(buttonPanel, BorderLayout.SOUTH);

	setContentPane(mainPanel);
	setSize(400, 400);
	setLocation((Utils.screenSize.width - getSize().width) / 2,
		    (Utils.screenSize.height - getSize().height) / 2);
	setVisible(false);

    }

    void pop(PrintTextComponentArea textComponent) {
	this.textComponent = textComponent;
	textEditor.setText(textComponent.getTemplate());
	setVisible(true);
    }
}

