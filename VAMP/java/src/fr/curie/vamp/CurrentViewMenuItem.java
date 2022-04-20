
/*
 *
 * CurrentViewMenuItem.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import java.awt.*;
import java.util.*;

class CurrentViewMenuItem extends ViewMenuItem {

    private GraphElementDisplayer defaultGraphElementDisplayer;
    private AxisDisplayer defaultAxisDisplayer;
    private GraphElementListOperation autoApplyDSLOP;

    CurrentViewMenuItem(String defaultName,
			GraphElementDisplayer defaultGraphElementDisplayer,
			AxisDisplayer defaultAxisDisplayer,
			GraphElementListOperation autoApplyDSLOP,
			int supportFlags,
			boolean addSeparator) {
	super(defaultName, supportFlags|SUPPORT_CURRENT_VIEW, addSeparator);
	this.defaultGraphElementDisplayer = defaultGraphElementDisplayer;
	this.defaultAxisDisplayer = defaultAxisDisplayer;
	this.autoApplyDSLOP = autoApplyDSLOP;
    }

    GraphElementDisplayer getDefaultGraphElementDisplayer() {return defaultGraphElementDisplayer;}
    GraphElementListOperation getAutoApplyDSLOP() { return  autoApplyDSLOP; }
    AxisDisplayer getDefaultAxisDisplayer() {return defaultAxisDisplayer;}

    void actionPerformed(View view, GraphPanel panel) { }

    private void setEnabled(JMenuItem menuItem, boolean enabled,
			    String from) {
	/*
	System.out.println("setEnabled: " + menuItem.getText() + " : " +
			   enabled + " (" + from + ")");
	*/
	menuItem.setEnabled(enabled);
	//menuItem.setEnabled(true);
    }

    void menuSelected(View view, GraphPanel panel, JMenuItem menuItem) {
	if (alwaysDisable()) {
	    //setEnabled(menuItem, false, "#0");
	    menuItem.setEnabled(false);
	    return;
	}

	LinkedList graphElements = panel.getGraphElements();

	if ((supportFlags & SUPPORT_PROFILE) == 0) {
	    for (int n = 0; n < graphElements.size(); n++) {
		if (((GraphElement)graphElements.get(n)).asProfile() != null) {
		    setEnabled(menuItem, false, "#0");
		    return;
		}
	    }
	}

	if (defaultGraphElementDisplayer != null) {
	    if (!defaultGraphElementDisplayer.checkGraphElements(graphElements)) {
		setEnabled(menuItem, false, "#1");
		return;
	    }

	    if (defaultAxisDisplayer == null &&
		!defaultGraphElementDisplayer.isCompatible
		(panel.getDefaultAxisDisplayer())) {
		setEnabled(menuItem, false, "#2");
		return;
	    }
	}

	if (defaultAxisDisplayer != null) {
	    if (!defaultAxisDisplayer.checkGraphElements(graphElements)) {
		setEnabled(menuItem, false, "#3");
		return;
	    }
	    
	    if (defaultGraphElementDisplayer == null &&
		!defaultAxisDisplayer.isCompatible
		(panel.getDefaultGraphElementDisplayer())) {
		setEnabled(menuItem, false, "#4");
		return;
	    }
	}

	if (autoApplyDSLOP != null) {
	    if (graphElements.size() == 0)
		setEnabled(menuItem, true, "#5");
	    else
		setEnabled(menuItem, View.checkDSLOP(view, panel, autoApplyDSLOP,
						     graphElements, false),
			   "#6");
	    return;
	}

	setEnabled(menuItem, true, "#7");
    }
}

