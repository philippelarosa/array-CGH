
/*
 *
 * DialogUtils.java
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
import java.util.*;
import java.io.*;

class DialogUtils {
    static JComponent makePad() {return new JLabel();}

    static void makePad(JPanel p) {
	p.add(makePad());
    }

    static void jumpLine(JPanel p, int grid_width) {
	while (grid_width-- > 0)
	    makePad(p);
    }

    static JRadioButton makeRadio(JPanel p, String name, ButtonGroup group,
				  ChangeListener listener) {
	JRadioButton b = new JRadioButton(name);
	group.add(b);
	b.setBackground(p.getBackground());
	if (listener != null)
	    b.addChangeListener(listener);
	return b;
    }

    static void pad(JPanel panel, String where) {
	pad(panel, where, 20, 20);
    }

    static void pad(JPanel panel, String where, int width, int height) {
	JPanel p = new JPanel();
	p.setBackground(panel.getBackground());
	p.setPreferredSize(new Dimension(width, height));
	panel.add(p, where);
    }

    static String dirPath = null;
    static final int XML_FILE_FILTER = 1;
    static final int HTML_FILE_FILTER = 2;
    static XMLFileFilter xmlFileFilter = new XMLFileFilter();
    static HTMLFileFilter htmlFileFilter = new HTMLFileFilter();

    static JFileChooser getFileChooser(String buttonName, int filter_type,
				       String title) {
	JFileChooser fileChooser = new JFileChooser(dirPath);
	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fileChooser.setBackground(VAMPResources.getColor
				  (VAMPResources.DIALOG_BG));

	if (filter_type == XML_FILE_FILTER)
	    fileChooser.setFileFilter(xmlFileFilter);
	else if (filter_type == HTML_FILE_FILTER)
	    fileChooser.setFileFilter(htmlFileFilter);
	else
	    fileChooser.setFileFilter(null);

	if (title != null)
	    fileChooser.setDialogTitle(title);

	fileChooser.setApproveButtonText(buttonName);
	return fileChooser;
    }

    static File openFileChooser(Frame frame, String buttonName,
				int filter_type, boolean save) {
	return openFileChooser(frame, buttonName, filter_type, null, save);
    }

    static File openFileChooser(Frame frame, String buttonName,
				int filter_type, String title, boolean save) {
	JFileChooser fc = getFileChooser(buttonName, filter_type, title); 
	//	int returnVal = fc.showOpenDialog(frame);
	int returnVal = (save ? fc.showSaveDialog(frame) : fc.showOpenDialog(frame));
	//int returnVal = fc.showDialog(frame, buttonName);
	File file = fc.getSelectedFile();
	if (file != null && file.getParentFile() != null)
	    dirPath = file.getParentFile().getAbsolutePath();

	if (returnVal == JFileChooser.APPROVE_OPTION)
	    return file;

	return null;
    }
}

