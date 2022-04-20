
/*
 *
 * CurrentViewBarplotMenuItem.java
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

class CurrentViewBarplotMenuItem extends ViewMenuItem {

    CurrentViewBarplotMenuItem(boolean hasSeparator) {
	super("Classic Barplot",
	      SUPPORT_ALL & ~(SUPPORT_DOUBLE_VIEW|SUPPORT_NEW_VIEW),
	      hasSeparator);
    }

    boolean hasActionListener() {return true;}
    boolean hasMenuListener() {return true;}

    void actionPerformed(View view, GraphPanel panel) {
	GraphElementDisplayer ds = panel.getDefaultGraphElementDisplayer();
	if (ds instanceof BarplotDataSetDisplayer) {
	    BarplotDataSetDisplayer hds = (BarplotDataSetDisplayer)ds;	    
	    hds.drawCentered(!hds.drawCentered());
	    panel.sync(true);
	}
    }

    void menuSelected(View view, GraphPanel panel, JMenuItem menuItem) {
	GraphElementDisplayer ds = panel.getDefaultGraphElementDisplayer();
	if (ds  instanceof BarplotDataSetDisplayer) {
	    BarplotDataSetDisplayer hds = (BarplotDataSetDisplayer)ds;	    
	    menuItem.setEnabled(true);
	    menuItem.setText(hds.drawCentered() ? "Classic Barplot" :
			     "Centered Barplot");
	}
	else
	    menuItem.setEnabled(false);
    }
}


