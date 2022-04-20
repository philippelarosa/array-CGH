
/*
 *
 * CurrentViewHightlightMinimalRegionsMenuItem.java
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

class CurrentViewHightlightMinimalRegionsMenuItem extends ViewMenuItem {

    CurrentViewHightlightMinimalRegionsMenuItem(boolean hasSeparator) {
	super("Hightlight minimal alterations",
	      SUPPORT_ALL & ~(SUPPORT_DOUBLE_VIEW|SUPPORT_NEW_VIEW),
	      hasSeparator);
    }

    boolean hasActionListener() {return true;}
    boolean hasMenuListener() {return true;}

    void actionPerformed(View view, GraphPanel panel) {
	panel.setHightlightMinimalRegions(!panel.hightlightMinimalRegions());
	panel.repaint();
    }

    void menuSelected(View view, GraphPanel panel, JMenuItem menuItem) {
	LinkedList regions = panel.getRegions();
	boolean b = panel.hightlightMinimalRegions();
	menuItem.setText(b ? "Un-Hightlight Minimal Alterations" :
			 "Hightlight Minimal Alterations");
	menuItem.setEnabled(false);
	int size = regions.size();
	for (int i = 0; i < size; i++) {
	    Region region = (Region)regions.get(i);
	    if (VAMPUtils.getType(region).equals(VAMPConstants.MINIMAL_REGION_TYPE)) {
		menuItem.setEnabled(true);
		break;
	    }
	}	
    }
}


