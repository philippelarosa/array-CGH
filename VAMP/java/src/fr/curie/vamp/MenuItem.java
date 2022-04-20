
/*
 *
 * MenuItem.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import java.util.*;

abstract class MenuItem {
    String name;
    boolean sep;
    JMenuItem item;
    JMenu jmenu;

    MenuItem(String name, boolean sep) {
	this.name = name;
	this.sep = sep;
    }

    void buildMenu(JMenu menu, JMenu subMenu) {
	JMenuItem oitem = item;
	item = new JMenuItem(name);
	addItem(menu, subMenu, item);
    }

    protected void addItem(JMenu menu, JMenu subMenu, JMenuItem item) {
	if (subMenu != null)
	    menu = subMenu;
	if (sep)
	    menu.addSeparator();
	menu.add(item);
	jmenu = menu;
    }

    String getName() {return name;}

    JMenuItem getMenuItem() {return item;}

    void makeMenuItems(LinkedList item_list) {
	item_list.add(this);
    }

    JMenu getMenu() {return jmenu;}
}
