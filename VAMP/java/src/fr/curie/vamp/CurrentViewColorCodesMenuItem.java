
/*
 *
 * CurrentViewColorCodesMenuItem.java
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

class CurrentViewColorCodesMenuItem extends ViewMenuItem {

    CurrentViewColorCodesMenuItem(boolean addSeparator) {
	super("Gained/Lost Color Codes",
	      SUPPORT_ALL & ~(SUPPORT_DOUBLE_VIEW|SUPPORT_NEW_VIEW),
	      addSeparator);
    }

    boolean hasActionListener() {return true;}
    boolean hasMenuListener() {return true;}

    void actionPerformed(View view, GraphPanel panel) {
	GraphElementDisplayer ds = panel.getDefaultGraphElementDisplayer();
	if (ds != null && ds instanceof CommonDataSetDisplayer) {
	    CommonDataSetDisplayer sds = (CommonDataSetDisplayer)ds;
	    sds.setGNLColorCodes(!sds.isGNLColorCodes());
	    view.syncGraphElements(false);
	}
    }

    void menuSelected(View view, GraphPanel panel, JMenuItem menuItem) {
	menuItem.setText(getDefaultName());
	menuItem.setEnabled(false);

	GraphElementDisplayer ds = panel.getDefaultGraphElementDisplayer();

	if (ds != null && ds instanceof CommonDataSetDisplayer) {
	    CommonDataSetDisplayer sds = (CommonDataSetDisplayer)ds;
	    String text = (sds.isGNLColorCodes() ? "Default Color Codes" :
			   "Gained/Lost Color Codes");
	    if (sds.isGNLColorCodes())
		menuItem.setEnabled(true);
	    else
		menuItem.setEnabled(VAMPUtils.hasProperty(view, VAMPProperties.GNLProp));
	    menuItem.setText(text);
	}
    }
}
