
/*
 *
 * Menu.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import java.util.*;

public class Menu extends MenuItem {

    // list of Menu item
    LinkedList items = new LinkedList();

    Menu(String name) {
	this(name, false);
    }

    Menu(String name, boolean sep) {
	super(name, sep);
    }

    void add(MenuItem item) {
	items.add(item);
    }
    
    void buildMenu(JMenu menu) {
	int size = items.size();
	for (int i = 0; i < size; i++) {
	    MenuItem item = (MenuItem)items.get(i);
	    item.buildMenu(menu, null);
	}
    }

    void buildMenu(JMenu menu, JMenu subMenu) {
	JMenu subMenu2 = new JMenu(name);
	addItem(menu, subMenu, subMenu2);
	int size = items.size();
	for (int i = 0; i < size; i++) {
	    MenuItem item = (MenuItem)items.get(i);
	    item.buildMenu(menu, subMenu2);
	}
    }

    void makeMenuItems(LinkedList item_list) {
	int size = items.size();
	for (int i = 0; i < size; i++) {
	    MenuItem item = (MenuItem)items.get(i);
	    item.makeMenuItems(item_list);
	}
    }

}
