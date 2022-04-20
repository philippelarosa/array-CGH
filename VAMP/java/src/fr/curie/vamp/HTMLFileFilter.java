
/*
 *
 * HTMLFileFilter.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.io.*;
import javax.swing.filechooser.*;

class HTMLFileFilter extends javax.swing.filechooser.FileFilter {

    public boolean accept(File f) {
	return f.isDirectory() || Utils.isHTMLFile(f.getName());
    }

    public String getDescription() {
	return "HTML";
    }
}
