
/*
 *
 * CurrentViewHightlightRecurrentRegionsMenuItem.java
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

class CurrentViewHightlightRecurrentRegionsMenuItem extends ViewMenuItem {

    CurrentViewHightlightRecurrentRegionsMenuItem(boolean hasSeparator) {
	super("Hightlight recurrent alterations",
	      SUPPORT_ALL & ~(SUPPORT_DOUBLE_VIEW|SUPPORT_NEW_VIEW),
	      hasSeparator);
    }

    boolean hasActionListener() {return true;}
    boolean hasMenuListener() {return true;}

    void actionPerformed(View view, GraphPanel panel) {
	panel.setHightlightRecurrentAlterations(!panel.hightlightRecurrentAlterations());
	panel.repaint();
    }

    void menuSelected(View view, GraphPanel panel, JMenuItem menuItem) {
	boolean b = panel.hightlightRecurrentAlterations();
	menuItem.setText(b ? "Un-Hightlight Recurrent Alterations" :
			 "Hightlight Recurrent Alterations");
	menuItem.setEnabled(false);

	LinkedList regions = panel.getRegions();
	int size = regions.size();
	for (int i = 0; i < size; i++) {
	    Region region = (Region)regions.get(i);
	    if (VAMPUtils.getType(region).equals(VAMPConstants.RECURRENT_REGION_TYPE)) {
		menuItem.setEnabled(true);
		return;
	    }
	}	

	LinkedList marks = panel.getMarks();
	size = marks.size();
	for (int i = 0; i < size; i++) {
	    Mark mark = (Mark)marks.get(i);
	    if (VAMPUtils.getType(mark).equals(VAMPConstants.RECURRENT_BREAKPOINT_TYPE)) {
		menuItem.setEnabled(true);
		return;
	    }
	}	
    }
}


