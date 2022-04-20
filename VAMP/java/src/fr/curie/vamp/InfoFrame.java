
/*
 *
 * InfoFrame.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

import java.io.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class InfoFrame extends JFrame {
    
    JTextArea textArea;

    static final String INFO_FRAME = "InfoFrame";

    public static void init(GlobalContext globalContext) {
	globalContext.put(INFO_FRAME, new InfoFrame());
    }

    static InfoFrame getInstance(GlobalContext globalContext) {
	return (InfoFrame)globalContext.get(INFO_FRAME);
    }

    private InfoFrame() {
	super("Information Window");
	getContentPane().setLayout(new BorderLayout());
	textArea = new JTextArea();
	JScrollPane scroll = new JScrollPane(textArea);
	getContentPane().add(scroll, BorderLayout.CENTER);
	setSize(400, 500);
    }

    public static void pop(GlobalContext globalContext) {
	InfoFrame infoFrame = getInstance(globalContext);
	infoFrame.setVisible(true);
    }

    void addText(String str) {
	textArea.append(str);
	System.out.println("textArea: " + textArea.getText());
    }
}
