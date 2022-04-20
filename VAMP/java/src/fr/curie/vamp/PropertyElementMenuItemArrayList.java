
/*
 *
 * PropertyElementMenuItemArrayList.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import javax.swing.*;

class PropertyElementMenuItemArrayList extends PropertyElementMenuItem {

    PropertyElementMenuItemArrayList() {
	super("");
    }

    void buildJPopupMenu(View view,
			 JPopupMenu menu, JMenu subMenu,
			 PropertyElement elem) {
	Vector v = (Vector)elem.getPropertyValue(VAMPProperties.VectorArrayProp);
	if (v == null)
	    return;
	JMenu sm = new JMenu("Array List");
	addItem(menu, subMenu, sm);
	int size = v.size();
	int menu_max_items = VAMPResources.getInt(VAMPResources.MENU_MAX_ITEMS);
	for (int i = 0; i < size; i++) {
	    if (!(v.get(i) instanceof GraphElement)) {
		continue;
	    }

	    GraphElement graphElement = (GraphElement)v.get(i);
	    JMenuItem item = new JMenuItem((String)graphElement.getID());
	    if (i != 0 && (i % menu_max_items) == 0) {
		JMenu tsm = new JMenu("...");
		sm.add(tsm);
		sm = tsm;
	    }
	    sm.add(item);
	}
    }
}
