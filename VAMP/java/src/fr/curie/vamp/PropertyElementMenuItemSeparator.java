
/*
 *
 * PropertyElementMenuItemSeparator.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;

class PropertyElementMenuItemSeparator extends PropertyElementMenuItem {

    static private PropertyElementMenuItemSeparator instance;
    
    static PropertyElementMenuItemSeparator getInstance() {
	if (instance == null)
	    instance = new PropertyElementMenuItemSeparator();
	return instance;
    }

    private PropertyElementMenuItemSeparator() {
	super("");
    }

    void buildJPopupMenu(View view,
			 JPopupMenu popup, JMenu subMenu,
			 PropertyElement elem) {
	if (subMenu != null)
	    subMenu.addSeparator();
	else
	    popup.addSeparator();
    }
}
