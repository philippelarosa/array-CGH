
/*
 *
 * JMenu_PopupMenu.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;

class JMenu_PopupMenu {

    JPopupMenu popup;
    JMenu menu;

    JMenu_PopupMenu(JPopupMenu popup) {
	this.popup = popup;
	this.menu = null;
    }

    JMenu_PopupMenu(JMenu menu ) {
	this.popup = null;
	this.menu = menu;
    }

    void add(JComponent comp) {
	if (popup != null)
	    popup.add(comp);
	else
	    menu.add(comp);
    }

    void add(String name) {
	if (popup != null)
	    popup.add(name);
	else
	    menu.add(name);
    }

    void addSeparator() {
	if (popup != null)
	    popup.addSeparator();
	else
	    menu.addSeparator();
    }

    Component getComponent(int idx) {
	if (popup != null)
	    //return popup.getComponent(idx);
	    return popup.getSubElements()[idx].getComponent();
	return menu.getMenuComponent(idx);
    }
}
