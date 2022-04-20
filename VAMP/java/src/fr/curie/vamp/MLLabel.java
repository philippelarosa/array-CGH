
/*
 *
 * MLLabel.java
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
import java.util.*;
import javax.swing.*;

public class MLLabel extends JPanel {

    private String text;
    //private JLabel labels[];
    private int splitLen = 0;

    MLLabel() {
	setText("");
    }

    MLLabel(int splitLen) {
	this.splitLen = splitLen;
	setText("");
    }

    MLLabel(String text, int splitLen) {
	this.splitLen = splitLen;
	setText(text);
    }

    MLLabel(String text) {
	setText(text);
    }

    static int MAX_LINES = 25;

    JLabel makeLabel(String text) {
	JLabel label = new JLabel(text);
	label.setBackground(getBackground());
	label.setForeground(getForeground());
	label.setFont(getFont());
	return label;
    }

    void setText(String text) {
	String lines[] = Utils.getLines(text, splitLen);
	int line_length;
	boolean overflow;
	if (lines.length > MAX_LINES) {
	    line_length = MAX_LINES; 
	    overflow = true; 
	}
	else {
	    line_length = lines.length;
	    overflow = false;
	}

	removeAll();
	//labels = new JLabel[line_length];
	setLayout(new GridLayout(line_length + (overflow ? 1 : 0), 1));
	for (int i = 0; i < line_length; i++)
	    add(makeLabel(lines[i]));
	
	if (overflow)
	    add(makeLabel("[...]"));
    }

    String getText() {
	return text;
    }
}
