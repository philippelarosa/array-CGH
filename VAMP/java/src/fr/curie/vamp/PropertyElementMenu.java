
/*
 *
 * PropertyElementMenu.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import java.util.*;

class PropertyElementMenu extends PropertyElementMenuItem {

    // list of PropertyElementMenu item
    LinkedList items = new LinkedList();

    PropertyElementMenu(String name) {
	super(name);
    }

    PropertyElementMenu() {
	this("");
    }

    void add(PropertyElementMenuItem item) {
	items.add(item);
    }
    
    void buildJPopupMenu(View view,
			 JPopupMenu popup, PropertyElement elem) {
	int size = items.size();
	for (int i = 0; i < size; i++) {
	    PropertyElementMenuItem item =
		(PropertyElementMenuItem)items.get(i);
	    item.buildJPopupMenu(view, popup, null, elem);
	}
    }

    void buildJPopupMenu(View view,
			 JPopupMenu popup, JMenu subMenu,
			 PropertyElement elem) {
	JMenu subMenu2 = new JMenu(elem.fromTemplate(name));
	addItem(popup, subMenu, subMenu2);
	int size = items.size();
	for (int i = 0; i < size; i++) {
	    PropertyElementMenuItem item =
		(PropertyElementMenuItem)items.get(i);
	    item.buildJPopupMenu(view, popup, subMenu2, elem);
	}
    }
}
