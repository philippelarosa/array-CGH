
/*
 *
 * CurrentViewSyncMenuItem.java
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

class CurrentViewSyncMenuItem extends ViewMenuItem {

    CurrentViewSyncMenuItem(boolean hasSeparator) {
	super("Recompute View",
	      SUPPORT_ALL & ~(SUPPORT_DOUBLE_VIEW|SUPPORT_NEW_VIEW),
	      hasSeparator);
    }

    boolean hasActionListener() {return true;}
    boolean hasMenuListener() {return false;}

    void actionPerformed(View view, GraphPanel panel) {
	view.syncGraphElements();
    }

    void menuSelected(View view, GraphPanel panel, JMenuItem menuItem) { }
}


